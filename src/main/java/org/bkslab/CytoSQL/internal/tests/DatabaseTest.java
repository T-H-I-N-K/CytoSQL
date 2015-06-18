package org.bkslab.CytoSQL.internal.tests;

import java.io.IOException;
import java.io.PrintStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.swing.JOptionPane;

/**
 * This is a sample Cytoscape plugin making a connection with an SQL database,
 * in order to enable a query that can be converted into a network.
 */
public class DatabaseTest {

  // set parameters for the database connection
  Connection conn = null;
  String url = "jdbc:mysql://struisvogel.cmi.ua.ac.be/";
  String dbName = "PPI_At_gdj1_v2";
  String driver = "com.mysql.jdbc.Driver";
  String userName = "kimh";
  String password = "luna";

  public static void main(String[] args) throws IOException {

    DatabaseTest t = new DatabaseTest();
    
    try {
      t.makeconnection();

      String sql = t.makequery();
      ResultSet rs = t.getresults(sql);
      
      rs.first();
      Object o = rs.getObject(1);
     //System.out.println(o.getClass());
     // if (o.getClass().equals(String.class))
    	 
    	 if (o instanceof String) {
			String s = (String)o;
			
		}
     ((Integer)o).equals("lll");
      
      t.printResultSet(rs);
      t.closeconnection();
    } catch (SQLException e) {
      throw new RuntimeException("Database Error", e);
    }

  }
  /**
   * This method stands in for making the connection with the SQL database.
   */
  public void makeconnection() throws SQLException {
    try {
      Class.forName(driver).newInstance();
    } catch (InstantiationException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (IllegalAccessException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (ClassNotFoundException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    conn = DriverManager.getConnection(url + dbName, userName, password);
    // JOptionPane.showMessageDialog(null, "Connected to the database");
  }

  /**
   * This method gets the SQL query through a dialog box.
   */
  public String makequery() throws IOException {

    String sql;
    sql = JOptionPane.showInputDialog(null, "Enter the SQL query:");
    return sql;

  }

  /**
   * This method retrieves the query result as a resultset, which is an object.
   */
  public ResultSet getresults(String sql) throws SQLException {
    Statement st = conn.createStatement();
    ResultSet rs = st.executeQuery(sql);
    return rs;
  }

  /**
   * This method gets the required data out of the resultset object, and prints
   * it to a tab-delimited file.
   */
  public void printResultSet(ResultSet rs) throws SQLException {

    // channels result to stdout
    PrintStream q = System.out;

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
   * This method closes the connection with the database.
   */
  public void closeconnection() throws SQLException {

    conn.close();

    JOptionPane.showMessageDialog(null, "Disconnected from database");
  }

}
