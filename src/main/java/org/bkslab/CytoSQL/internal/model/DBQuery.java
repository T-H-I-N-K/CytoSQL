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
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import org.apache.commons.dbutils.DbUtils;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;

import javax.swing.JOptionPane;
import javax.swing.table.TableModel;

public class DBQuery {

	private Connection conn;
	private Statement st;
	private ResultSet rs;
	private DBConnectionInfo dbConnectionInfo;
	
	public DBQuery(final DBConnectionInfo dbConnectionInfo) throws SQLException{
		makeConnection(
			dbConnectionInfo.driver,
			dbConnectionInfo.url,
			dbConnectionInfo.database,
			dbConnectionInfo.user,
			dbConnectionInfo.password);
		this.dbConnectionInfo = dbConnectionInfo;
	}

	public void close(){
		DbUtils.closeQuietly(conn, st, rs);
	}
	
	public Connection getConnection(){
		return conn;
	}
	

	/**
	 * This method stands in for making the connection with the SQL database.
	 */

	private void makeConnection(String driver, String url, String dbName,
			String userName, String password) throws SQLException {

		int idx = driver.indexOf("-CUSTOM_DRIVER"); // database connection
													// specification provided by
													// user
		if (idx >= 0) {
			driver = driver.substring(0, idx);
		}

		try {
			Class.forName(driver).newInstance();
		} catch (InstantiationException e) {
			JOptionPane.showMessageDialog(null,
					"Instantiation exception for connection to DB.\n" + e);
		} catch (IllegalAccessException e) {
			JOptionPane.showMessageDialog(null,
					"Cannot access DB connection.\n" + e);
		} catch (ClassNotFoundException e) {
			JOptionPane
					.showMessageDialog(null, "Driver class not found.\n" + e);
		}
		if (idx >= 0) { // CUSTOM USER DEFINED DRIVER
			conn = DriverManager.getConnection(url, userName, password);
		} else { // Built-in JDBC driver
			conn = DriverManager.getConnection(url + "/" + dbName, userName,
					password);
		}
	}

	private String getSchema(){
		if(dbConnectionInfo.schema.length() == 0){
			return null;
		} else {
			return dbConnectionInfo.schema.toUpperCase();
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
			final String tableName,
			final String keyColumnName) {
		try {
			Statement createTempTableStmt = conn.createStatement();
			createTempTableStmt.executeUpdate(
				"CREATE TEMPORARY TABLE " + tableName + " ( " + keyColumnName + " STRING );");
			createTempTableStmt.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
			PreparedStatement insertStmt;
			insertStmt = conn.prepareStatement(
				"INSERT INTO " + tableName +" ( " + keyColumnName + " ) VALUES ( ? );");
			for(CyNode node : nodes){
				insertStmt.setString(1, network.getRow(node).get(keyColumnName, String.class));
				insertStmt.executeUpdate();
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void deleteTempTable(
		final String tableName){
	
		try {
			Statement createTempTableStmt = conn.createStatement();
			createTempTableStmt.executeUpdate(
				"DROP TABLE " + tableName + ";");
			createTempTableStmt.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
			rset = getConnection().getMetaData().getSchemas();
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

		ArrayList<String> list = new ArrayList<String>();
		ResultSet rset = null;
		try {
			rset = getConnection().getMetaData().getTables(
				null, getSchema(), null, new String[] { tableType });
			while (rset.next())
				try {
					list.add(rset.getString(3));
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
			rset0 = getConnection().getMetaData().getPrimaryKeys(null,
					schema, tName.toString());
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
			rset1 = getConnection().getMetaData().getColumns(null, schema,
					tName, null);
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
				rset = getConnection().createStatement().executeQuery(
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
