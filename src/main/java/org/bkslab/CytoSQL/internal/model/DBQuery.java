package org.bkslab.CytoSQL.internal.model;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Types;
import java.sql.SQLException;
import java.sql.Connection;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.commons.dbutils.DbUtils;
import org.bkslab.CytoSQL.internal.tasks.DatabaseNetworkParser;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;

import javax.swing.JOptionPane;
import javax.swing.table.TableModel;

public class DBQuery {

	private Connection conn;
	private Statement st;
	private ResultSet rs;
	private String schema;
	
	
	public DBQuery(Connection conn, final String schema) throws SQLException{
		this.conn = conn;
		this.schema = schema;
	}

	public void close(){
		DbUtils.closeQuietly(conn, st, rs);
	}
	

	private String getSchema(){
		if(schema.length() == 0){
			return null;
		} else {
			return schema.toUpperCase();
		}
	}
	
	
	public static int countQuestionMarks(String sql) {
		int count = 0;
		int fromindex = 0;
		while (true) {
			int index = sql.indexOf("?", fromindex);
			if (index == -1)
				break;
			count++;
			fromindex = index + 1;
		}
		return count;
	}

	/**
	 * This method retrieves the query result as a ResultSet.
	 */
	public ResultSet getResults(String sql) throws SQLException {
		st = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
		rs = st.executeQuery(sql);
		return rs;
	}

	// TODO validate tableName and keyColumnName are not injection attacks
	public void copyToTempTable(
			CyNetwork network,
			List<CyNode> nodes,
			final String tableName) throws Exception {
		
		
		List<String> columnsToCreate = new ArrayList<String>();
		
		try {


			if(nodes.isEmpty()){
				return;
			}
			Statement createTempTableStmt = conn.createStatement();
			String sql = "CREATE TEMPORARY TABLE " + tableName + " (";
			
			for(CyColumn column : network.getDefaultNodeTable().getColumns()){
				if(column.getName() == "SUID"){ continue;}
				if(column.getName() == "selected"){ continue;}				

				
				Class<?> cytoscapeColClass = column.getType();
				
				int SQLType;
				try{
					SQLType = DatabaseNetworkParser.CytoscapeTypeToSQLType( cytoscapeColClass);
				} catch(Exception e){
					continue;
				}
					
				switch(SQLType){ 
				case Types.INTEGER:
					if(columnsToCreate.size() > 0){ sql += ", "; }
					sql += "\"" + column.getName() + "\" INTEGER";
					break;
				case Types.BIGINT:
					if(columnsToCreate.size() > 0){ sql += ", "; }
					sql += "\"" + column.getName() + "\" BIGINT";
					break;
				case Types.DOUBLE:
					if(columnsToCreate.size() > 0){ sql += ", "; }
					sql += "\"" + column.getName() + "\" DOUBLE PRECISION";
					break;
				case Types.VARCHAR:
					if(columnsToCreate.size() > 0){ sql += ", "; }
					sql += "\"" + column.getName() + "\" VARCHAR";
					break;
				case Types.BOOLEAN:
					if(columnsToCreate.size() > 0){ sql += ", "; }
					sql += "\"" + column.getName() + "\" BOOLEAN";
					break;
				default:
					// TODO how to handle lists?
					System.err.println("Unrecogized sql type for cytoscape type " + cytoscapeColClass);
					continue;
				}
				columnsToCreate.add(column.getName());
			}
			if(columnsToCreate.size() == 0){
				return;
			}
			
			sql += ");";
			System.out.println("SQL: " + sql);
			createTempTableStmt.executeUpdate(sql);
			createTempTableStmt.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
			PreparedStatement insertStmt;
			insertStmt = conn.prepareStatement(
				"INSERT INTO " + tableName + " VALUES ( " + String.join(", ",  Collections.nCopies(columnsToCreate.size(),  "?")) + ");");
			for(CyNode node : nodes){
				Map<String, Object> nodeValues = network.getRow(node).getAllValues();
				int colIndex = 1;
				for(String colName : columnsToCreate){
					insertStmt.setObject(colIndex, nodeValues.get(colName));
					colIndex++;
				}
				insertStmt.executeUpdate();
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void deleteTempTable(
		final String tableName){
		boolean containsTable = false;
		try {
			if(this.conn.getMetaData().getDriverName().indexOf("PostgreSQL") >= 0){
				List<String> tempTables = getTables("TEMPORARY TABLE");
				System.out.println("Temporary Tables:");
				for(String tempTable : tempTables){
					System.out.println("\t" + tempTable);
				}
				
				List<String> allTables = getTables("TABLE");
				System.out.println("All tables:\n");
				for(String tbl : allTables){
					System.out.println("\t" + tbl);
				}
				
				if(getTables("TEMPORARY TABLE").contains(tableName)){
					containsTable = true;
				}
			} else {
				if(getTables("TABLE").contains(tableName)){
					containsTable = true;
				}
			}
		} catch (SQLException e1) {
			System.out.println("Unable to delete temporary table because the connection has been lost.\n");
			e1.getMessage();
			e1.printStackTrace();
			return;
		}
		
		if(!containsTable){
			return;
		}
		
		try {
			Statement createTempTableStmt = conn.createStatement();
			createTempTableStmt.executeUpdate(
				"DROP TABLE " + tableName + ";");
			createTempTableStmt.close();
		} catch (SQLException e) {
			System.out.println("Unable to delete temporary table:\n");
			e.getMessage();
		}
	}
	
	
	/**
	 * This method gets the required data out of the ResultSet object, and
	 * prints it to an output stream
	 */
	public static void printResultSet(ResultSet rs, OutputStream os)
			throws SQLException {

		// channels result to desktop
		PrintWriter q;
		try {
			q = new PrintWriter(new BufferedWriter(new FileWriter(
					"/home/kimh/Desktop/query.txt")), true);
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, "Error creating file");
			throw new RuntimeException();
		}

		try {
			// gets amount of rows in resultset object rs
			int colCount = rs.getMetaData().getColumnCount();

			// before and after the loop, the counter is reset to the first
			// row
			if (!rs.first()) {
				return; // if empty set
			}
			do {
				for (int i = 1; i <= colCount; i++) {
					q.print(rs.getObject(i));
					q.print("\t");
				}
				q.println();
			} while (rs.next());
			rs.first();
		} finally {
			q.close();
		}
	}

	
	/**
	 * @return catalogs list
	 */
	public synchronized List<String> getSchemas() {

		ArrayList<String> list = new ArrayList<String>();
		ResultSet rset = null;
		try {
			rset = conn.getMetaData().getSchemas();
			while (rset.next())
				list.add(rset.getString(1));
		} catch (Exception ex) {
			if (ex.getMessage().indexOf(
					"Caratteristica opzionale non implementata") == -1)
				ex.printStackTrace();
		} finally {
			try {
				Statement stmt = rset == null ? null : rset.getStatement();
				try {
					rset.close();
				} catch (Exception ex3) {
				}
				try {
					stmt.close();
				} catch (Exception ex4) {
				}
			} catch (Exception ex1) {
			}
		}
		return list;
	}
	
	/**
	 * @return tables list, filtered by schema
	 */
	public synchronized List<String> getTables(final String tableType) {
		return getTables(tableType, false);
	}
	
	public synchronized List<String> getTables(final String tableType, boolean withSchema) {

		ArrayList<String> list = new ArrayList<String>();
		ResultSet rset = null;
		try {
			rset = conn.getMetaData().getTables(
				null, getSchema(), null, new String[] { tableType });
			while (rset.next())
				try {
					String tableName = "";
					if(withSchema){
						String schema = rset.getString(2);
						if(schema != null){
							tableName += schema + ".";
						}
					}
					tableName += rset.getString(3);
					list.add(tableName);
				} catch (SQLException ex1) {
					ex1.printStackTrace();
				}
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			try {
				Statement stmt = rset == null ? null : rset.getStatement();
				try {
					rset.close();
				} catch (Exception ex3) {
				}
				try {
					stmt.close();
				} catch (Exception ex4) {
				}
			} catch (Exception ex2) {
			}
		}
		return list;
	}


	private synchronized Hashtable<String, String> getPrimaryKeys(final String schema, final String tName){
		Hashtable<String, String> pk = new Hashtable<String, String>();
		
		ResultSet rset0 = null;
		try {
			rset0 = conn.getMetaData().getPrimaryKeys(null, schema, tName.toString());
			while (rset0.next()) {
				pk.put(rset0.getString(4), rset0.getString(5));
			}
		} catch (SQLException ex1) {
			if (ex1.getMessage().indexOf(
					"Driver does not support this function") == -1)
				ex1.printStackTrace();
		} finally {
			try {
				Statement stmt = rset0 == null ? null : rset0
						.getStatement();
				try {
					rset0.close();
				} catch (Exception ex3) {
				}
				try {
					stmt.close();
				} catch (Exception ex4) {
				}
			} catch (Exception ex1) {
			}
		}
		return pk;
	}
	
	
	private synchronized Hashtable<String, String> getDefaultValues(final String schema, final String tName){
		Hashtable<String, String> defaults = new Hashtable<String, String>();
		ResultSet rset1 = null;
		try {
			rset1 = conn.getMetaData().getColumns(null, schema, tName, null);
			String colValue = null;
			String colName = null;
			while (rset1.next()) {
				try {
					colName = rset1.getString(4);
					colValue = rset1.getString(13);
					if (colValue != null) {
						defaults.put(colName, colValue);
					}
				} catch (SQLException ex2) {
				}
			}
		} catch (SQLException ex1) {
			// JOptionPane.showMessageDialog(parent,"Error while fetching PKs:\n"+ex1.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);
			if (ex1.getMessage().indexOf(
					"Driver does not support this function") == -1)
				ex1.printStackTrace();
		} finally {
			try {
				Statement stmt = rset1 == null ? null : rset1
						.getStatement();
				try {
					rset1.close();
				} catch (Exception ex3) {
				}
				try {
					stmt.close();
				} catch (Exception ex4) {
				}
			} catch (Exception ex1) {
			}
		}
		return defaults;
	}
	

	/**
	 * @param tableName
	 *            table name
	 * @return table columns
	 */
	public synchronized TableModel getTableColumns(String tableName) {

		CustomTableModel model = new CustomTableModel(
				new String[] { "column","data type", "pk", "null?", "default" },
				new Class<?>[] { String.class, String.class, Integer.class, Boolean.class, String.class });
		
		String tName = tableName;
		String schema = null;
		if (tName.indexOf(".") > -1) {
			schema = tName.substring(0, tName.indexOf("."));
			tName = tName.substring(tName.indexOf(".") + 1);
		}
		
		Hashtable<String, String> pk = getPrimaryKeys(schema, tName);
		Hashtable<String, String> defaults = getDefaultValues(schema, tName);
		
		
		try {

			ResultSet rset = null;
			try {
				rset = conn.createStatement().executeQuery(
						"select * from " + tableName);
				Vector<Vector<Object>> data = new Vector<Vector<Object>>();

				String type = null;
				for (int i = 1; i <= rset.getMetaData().getColumnCount(); i++) {
					
					final String colName = rset.getMetaData().getColumnName(i);
					final int colType = rset.getMetaData().getColumnType(i);
					final int colPrecision = rset.getMetaData().getPrecision(i);
					final int colScale = rset.getMetaData().getScale(i);
					final int colIsNullable = rset.getMetaData().isNullable(i);
					
					Vector<Object> row = new Vector<Object>();
					row.add(colName);
					
					type = colName;
					if ((colType == Types.VARCHAR
							|| colType == Types.LONGVARCHAR
							|| colType == Types.CHAR)
							&& colPrecision == 0){ // for MySQL
						type += "("+ rset.getMetaData().getColumnDisplaySize(i) + ")";
					} else if (colType == Types.BIGINT
							|| colType == Types.CHAR
							|| colType == Types.INTEGER
							|| colType == Types.LONGVARBINARY
							|| colType == Types.NUMERIC
							&& colPrecision > 0
							&& colScale == 0
							|| colType == Types.SMALLINT
							|| colType == Types.VARCHAR
							|| colType == Types.LONGVARCHAR){
						type += "(" + colPrecision + ")";
					} else if (colType == Types.DECIMAL
							|| colType == Types.DOUBLE
							|| colType == Types.FLOAT
							|| colType == Types.NUMERIC
							&& colPrecision > 0
							|| colType == Types.REAL){
						type += "(" + colPrecision + "," + colScale + ")";
					}
					row.add(type);
					
					row.add(pk.containsKey(colName) ?
						new Integer(pk.get(colName).toString().trim()) :
						null);
					row.add(new Boolean(colIsNullable == ResultSetMetaData.columnNullable));
					row.add(defaults.get(colName));
					data.add(row);
				}
				model.setDataVector(data);
				return model;
			} catch (Exception ex1) {
				if (ex1.getMessage().indexOf("Driver does not support this function") == -1)
					ex1.printStackTrace();
			} finally {
				try {
					Statement stmt = rset == null ? null : rset.getStatement();
					try {
						rset.close();
					} catch (Exception ex3) {
					}
					try {
						stmt.close();
					} catch (Exception ex4) {
					}
				} catch (Exception ex1) {
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return model;
	}

	
	
}
