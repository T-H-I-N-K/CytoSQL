package org.bkslab.cytosql.internal.model;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.sql.DriverManager;
import java.sql.ResultSet;
//import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Connection;
import java.sql.Statement;
//import java.sql.Types;
import java.util.ArrayList;
//import java.util.Hashtable;
import java.util.List;
//import java.util.Vector;

import org.apache.commons.dbutils.DbUtils;

import javax.swing.JOptionPane;
//import javax.swing.table.TableModel;

//import org.bkslab.cytosql.internal.prefs.CustomTableModel;
//import org.bkslab.cytosql.internal.util.Options;

public class DBQuery {

	Connection conn;

	public DBQuery(final DBConnectionInfo conn_info) throws SQLException{
		makeConnection(
			conn_info.driver.getSelectedValue(),
			conn_info.url,
			conn_info.database,
			conn_info.user,
			conn_info.password);
	}

	public void close(){
		DbUtils.closeQuietly(conn);
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
		Statement st = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
				ResultSet.CONCUR_UPDATABLE);
		ResultSet rs = st.executeQuery(sql);
		DbUtils.closeQuietly(st);
		return rs;
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
	 * @return tables list, filtered by schema
	 */
	public synchronized List<String> getTables(String schema, String tableType) {

		ArrayList<String> list = new ArrayList<String>();
		ResultSet rset = null;
		try {
			rset = getConnection().getMetaData().getTables(
					null, // schema.length()==0?null:schema.toUpperCase(),
					schema.length() == 0 ? null : schema.toUpperCase(), null,
					new String[] { tableType });
			while (rset.next())
				try {
					// JOptionPane.showMessageDialog(null, "DA VAO DAY");
					// list.add((schema.length()>0?schema.toUpperCase()+".":"")+rset.getString(3));
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

//	/**
//	 * @param tableName
//	 *            table name
//	 * @return table columns
//	 */
//	public synchronized TableModel getTableColumns(String tableName) {
//
//		CustomTableModel model = new CustomTableModel(new String[] {
//				Options.getInstance().getResource("column"),
//				Options.getInstance().getResource("data type"),
//				Options.getInstance().getResource("pk"),
//				Options.getInstance().getResource("null?"),
//				Options.getInstance().getResource("default") }, new Class[] {
//				String.class, String.class, Integer.class, Boolean.class,
//				String.class });
//		try {
//			Hashtable<String, String> pk = new Hashtable<String, String>();
//			String tName = tableName;
//			String schema = null;
//			if (tName.indexOf(".") > -1) {
//				schema = tName.substring(0, tName.indexOf("."));
//				tName = tName.substring(tName.indexOf(".") + 1);
//			}
//			ResultSet rset0 = null;
//			try {
//				rset0 = getConnection().getMetaData().getPrimaryKeys(null,
//						schema, tName.toString());
//				while (rset0.next()) {
//					pk.put(rset0.getString(4), rset0.getString(5));
//				}
//			} catch (SQLException ex1) {
//				// JOptionPane.showMessageDialog(parent,"Error while fetching PKs:\n"+ex1.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);
//				if (ex1.getMessage().indexOf(
//						"Driver does not support this function") == -1)
//					ex1.printStackTrace();
//			} finally {
//				try {
//					Statement stmt = rset0 == null ? null : rset0
//							.getStatement();
//					try {
//						rset0.close();
//					} catch (Exception ex3) {
//					}
//					try {
//						stmt.close();
//					} catch (Exception ex4) {
//					}
//				} catch (Exception ex1) {
//				}
//			}
//
//			Hashtable<String, String> defaults = new Hashtable<String, String>();
//			ResultSet rset1 = null;
//			try {
//				rset1 = getConnection().getMetaData().getColumns(null, schema,
//						tName, null);
//				String colValue = null;
//				String colName = null;
//				while (rset1.next()) {
//					try {
//						colName = rset1.getString(4);
//						colValue = rset1.getString(13);
//						if (colValue != null) {
//							defaults.put(colName, colValue);
//						}
//					} catch (SQLException ex2) {
//					}
//				}
//			} catch (SQLException ex1) {
//				// JOptionPane.showMessageDialog(parent,"Error while fetching PKs:\n"+ex1.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);
//				if (ex1.getMessage().indexOf(
//						"Driver does not support this function") == -1)
//					ex1.printStackTrace();
//			} finally {
//				try {
//					Statement stmt = rset1 == null ? null : rset1
//							.getStatement();
//					try {
//						rset1.close();
//					} catch (Exception ex3) {
//					}
//					try {
//						stmt.close();
//					} catch (Exception ex4) {
//					}
//				} catch (Exception ex1) {
//				}
//			}
//
//			ResultSet rset = null;
//			try {
//				rset = getConnection().createStatement().executeQuery(
//						"select * from " + tableName);
//				Vector<String> data = new Vector<String>();
//
//				String type = null;
//				for (int i = 0; i < rset.getMetaData().getColumnCount(); i++) {
//					Vector<Object> row = new Vector<Object>();
//					row.add(rset.getMetaData().getColumnName(i + 1));
//					type = rset.getMetaData().getColumnTypeName(i + 1);
//					if ((rset.getMetaData().getColumnType(i + 1) == Types.VARCHAR
//							|| rset.getMetaData().getColumnType(i + 1) == Types.LONGVARCHAR || rset
//							.getMetaData().getColumnType(i + 1) == Types.CHAR)
//							&& rset.getMetaData().getPrecision(i + 1) == 0) // case
//																			// MySQL...
//						type += "("
//								+ rset.getMetaData()
//										.getColumnDisplaySize(i + 1) + ")";
//					else if (rset.getMetaData().getColumnType(i + 1) == Types.BIGINT
//							|| rset.getMetaData().getColumnType(i + 1) == Types.CHAR
//							|| rset.getMetaData().getColumnType(i + 1) == Types.INTEGER
//							|| rset.getMetaData().getColumnType(i + 1) == Types.LONGVARBINARY
//							|| rset.getMetaData().getColumnType(i + 1) == Types.NUMERIC
//							&& rset.getMetaData().getPrecision(i + 1) > 0
//							&& rset.getMetaData().getScale(i + 1) == 0
//							|| rset.getMetaData().getColumnType(i + 1) == Types.SMALLINT
//							|| rset.getMetaData().getColumnType(i + 1) == Types.VARCHAR
//							|| rset.getMetaData().getColumnType(i + 1) == Types.LONGVARCHAR)
//						type += "(" + rset.getMetaData().getPrecision(i + 1)
//								+ ")";
//					else if (rset.getMetaData().getColumnType(i + 1) == Types.DECIMAL
//							|| rset.getMetaData().getColumnType(i + 1) == Types.DOUBLE
//							|| rset.getMetaData().getColumnType(i + 1) == Types.FLOAT
//							|| rset.getMetaData().getColumnType(i + 1) == Types.NUMERIC
//							&& rset.getMetaData().getPrecision(i + 1) > 0
//							|| rset.getMetaData().getColumnType(i + 1) == Types.REAL)
//						type += "(" + rset.getMetaData().getPrecision(i + 1)
//								+ "," + rset.getMetaData().getScale(i + 1)
//								+ ")";
//					row.add(type);
//					row.add(pk.containsKey(rset.getMetaData().getColumnName(
//							i + 1)) ? new Integer(pk
//							.get(rset.getMetaData().getColumnName(i + 1))
//							.toString().trim()) : null);
//					row.add(new Boolean(
//							rset.getMetaData().isNullable(i + 1) == ResultSetMetaData.columnNullable));
//					row.add(defaults.get(rset.getMetaData()
//							.getColumnName(i + 1)));
//					data.add(row);
//				}
//				model.setDataVector(data);
//				return model;
//			} catch (Exception ex1) {
//				// JOptionPane.showMessageDialog(parent,"Error while fetching PKs:\n"+ex1.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);
//				if (ex1.getMessage().indexOf(
//						"Driver does not support this function") == -1)
//					ex1.printStackTrace();
//			} finally {
//				try {
//					Statement stmt = rset == null ? null : rset.getStatement();
//					try {
//						rset.close();
//					} catch (Exception ex3) {
//					}
//					try {
//						stmt.close();
//					} catch (Exception ex4) {
//					}
//				} catch (Exception ex1) {
//				}
//			}
//		} catch (Exception ex) {
//			ex.printStackTrace();
//		}
//		return model;
//	}

}
