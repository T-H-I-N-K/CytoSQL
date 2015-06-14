package org.bkslab.cytosql.internal.query;

import org.bkslab.cytosql.internal.prefs.DBConnectionInfo;
import org.bkslab.cytosql.internal.prefs.CustomTableModel;
import org.bkslab.cytosql.internal.util.Util;
import org.bkslab.cytosql.internal.util.Options;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;

import javax.swing.JOptionPane;
import javax.swing.ProgressMonitor;
import javax.swing.table.TableModel;



import org.cytoscape.model.CyNetwork;
import cytoscape.Cytoscape;
import cytoscape.data.CyAttributes;
import cytoscape.plugin.PluginManager;

public class AlgoClass {

  // Main processing of SQL queries

  // Create network happens here

  // Expand network and update attributes gets delegated to Query classes

  private static Connection conn = null;
  private static ResultSet rs = null;

  public static Connection getConnection(){
//	  if(conn==null) createDBConnection();
	  return conn;
  }
  public static void setConnection(Connection argconn){
	  conn=argconn;
  }
  public static enum BatchType {
    EXPAND, UPDATE_NODE_ATTR, UPDATE_EDGE_ATTR
  };

  public static Map<String, ResultSet> processBatchQuery(String sql, Object bindvarMap, BatchType type) {
    createDBConnection();
    Map<String, ResultSet> previewResult=null;
    try {
      switch (type) {
        case EXPAND:
          previewResult=ExpandNetworkQuery.processBatchResults(sql, (Map<Integer, String>) bindvarMap, conn);
          break;
        case UPDATE_NODE_ATTR:
          previewResult=UpdateNodeAttributesQuery.processBatchResults(sql, (Map<Integer, String>)bindvarMap, conn);
          break;
        case UPDATE_EDGE_ATTR:
          previewResult=UpdateEdgeAttributesQuery.processBatchResults(sql, (Map<Integer, String[]>)bindvarMap, conn);
          break;
        default:
          break;
      }
    } catch (SQLException e) {
      JOptionPane.showMessageDialog(null, "Error excuting SQL statement:\n" + e.getMessage());
      throw new RuntimeException(e);
    }
    return previewResult;
  }

  /**
   * processQuery is called by CytoSQLApp as soon as the "run" button is
   * pressed. this method is responsible for creating the network out of the
   * query, using the other methods from AlgoClass.
   * 
   * @param sql
   * @throws IOException
   */

  
  public static void createNetwork(Map<String, ResultSet> SQLResult) throws Exception{

      File f;
      try {
        f = File.createTempFile("cytoplugin", ".sif");
      } catch (IOException e) {
        throw new RuntimeException("Error creating temp file", e);
      }
      
	  ResultSet tmpRs=SQLResult.get("UNSPECIFIED");
      int colCount = tmpRs.getMetaData().getColumnCount();
      if (colCount < 2) {
        JOptionPane.showMessageDialog(null,
            "Query returns < 2 columns. Can't build graph", "Query check",
            JOptionPane.ERROR_MESSAGE);
        // get attribute types and attributes if more than two columns
      } else {   	  
    	//<Hai DNV
    	ArrayList<Object> possibilities=new ArrayList();
    	possibilities.add("source node");
    	possibilities.add("target node");
    	possibilities.add("source node attribute");
    	possibilities.add("target node attribute");    	
    	possibilities.add("edge attribute");
 	
    	map = getAttributeTypes(tmpRs, possibilities, 1);
        try {
          f.deleteOnExit();
          PrintWriter w = null;
          try {
            w = new PrintWriter(new BufferedWriter(new FileWriter(f)), true);
          } catch (IOException e) {
            throw new RuntimeException("Error creating writer", e);
          }
          try {
            final ProgressMonitor pm = new ProgressMonitor(null,
        		   "Creating network", null, 1, Integer.MAX_VALUE);
            int progress = 0;

            while (tmpRs.next()) {
              w.println(tmpRs.getObject(nodeColumns[0]) + " interacts " + tmpRs.getObject(nodeColumns[1]));
              setAttributesForCurrentRow(tmpRs);
              progress += 1;
              pm.setProgress(progress);
            }
            progress=Integer.MAX_VALUE;
            pm.setProgress(progress);
          }finally {
            w.close();
          }
          CyNetwork network = Cytoscape.createNetworkFromFile(f
              .getAbsolutePath());
          Cytoscape.createNetworkView(network);
          showSummary(network);
        } finally {
          f.delete();
        }
      }
  }
  
  public static Map<String, ResultSet> processQuery(String sql) {

    createDBConnection();
    Map<String, ResultSet> previewResult=new HashMap<String, ResultSet>();    
    // get the resultset and check for minimal amount of columns
    String correctPath = PluginManager.getPluginManager()
    	.getPluginManageDirectory().getAbsolutePath();
    System.out.println("correctPath: " + correctPath);
    try{
	    ResultSet tmpRs = getresults(sql);
	    previewResult.put("UNSPECIFIED", tmpRs);      
    }catch(SQLException e){
        JOptionPane.showMessageDialog(null, "Error executing SQL statement");
    }catch (Exception e) {
        JOptionPane.showMessageDialog(null, "Error executing SQL statement");
    }
    return previewResult;
  }

  private static void showSummary(CyNetwork network) {
    String summary = String.format(
        "Created a new network with %d nodes and %d edges.\n", network
            .getNodeCount(), network.getEdgeCount());
    int nrEdgeAttr = 0;
    int nrSNodeAttr = 0;
    int nrTNodeAttr = 0;

    ArrayList<String> edgeAttr = new ArrayList<String>();
    ArrayList<String> SnodeAttr = new ArrayList<String>();
    ArrayList<String> TnodeAttr = new ArrayList<String>();
    for (String s : map.keySet()) {
      if (map.get(s).equals("source node attribute")) {
        nrSNodeAttr++;
        SnodeAttr.add(s);
      } else if (map.get(s).equals("target node attribute")) {
        nrTNodeAttr++;
        TnodeAttr.add(s);
      } else if (map.get(s).equals("edge attribute")) {
        nrEdgeAttr++;
        edgeAttr.add(s);
      }
    }
    summary += String.format("Added %d edge attribute", nrEdgeAttr);
    if (nrEdgeAttr == 0) {
      summary += "s.\n";
    } else if (nrEdgeAttr == 1) {
      summary += ": " + Util.join(edgeAttr, ", ") + ".\n";
    } else if (nrEdgeAttr > 1) {
      summary += "s: " + Util.join(edgeAttr, ", ") + ".\n";
    }
    summary += String.format("Added %d source node attribute", nrSNodeAttr);
    if (nrSNodeAttr == 0) {
      summary += "s.\n";
    } else if (nrSNodeAttr == 1) {
      summary += ": " + Util.join(SnodeAttr, ", ") + ".\n";
    } else if (nrSNodeAttr > 1) {
      summary += "s: " + Util.join(SnodeAttr, ", ") + ".\n";
    }
    summary += String.format("Added %d target node attributes", nrTNodeAttr);
    if (nrTNodeAttr == 0) {
      summary += ".\n";
    } else if (nrTNodeAttr == 1) {
      summary += ": " + Util.join(TnodeAttr, ", ") + ".\n";
    } else if (nrTNodeAttr > 1) {
      summary += ": " + Util.join(TnodeAttr, ", ") + ".\n";
    }
    JOptionPane.showMessageDialog(null, summary, "Summary",
        JOptionPane.PLAIN_MESSAGE);
  }

  private static void createDBConnection() { //"private" used by Koenv
    // get connection specification from the properties file
    Properties defaultProps = null;
    defaultProps = DBConnectionInfo.getCurrentConnection();
    System.out.println(defaultProps.getProperty("driver").trim() + "\n"
        + defaultProps.getProperty("url").trim() + "\n"
        + defaultProps.getProperty("dbname").trim() + "\n"
        + defaultProps.getProperty("username").trim() + "\n"
        + defaultProps.getProperty("password").trim());

    // try to make the connection with the specifications
    try {
      makeconnection(defaultProps.getProperty("driver").trim(), defaultProps
          .getProperty("url").trim(),
          defaultProps.getProperty("dbname").trim(), defaultProps.getProperty(
              "username").trim(), defaultProps.getProperty("password").trim());
      System.out.println("connection is:" + conn);
    } catch (Exception e) {
      JOptionPane.showMessageDialog(null, "Couldn't create connection.\n" + e);
      e.printStackTrace();
      throw new RuntimeException(e);
    }
  }

  /**
   * This method stands in for making the connection with the SQL database.
   */

  public static void makeconnection(String driver, String url, String dbName,
      String userName, String password) throws SQLException {
	  
	int idx=driver.indexOf("-CUSTOM_DRIVER"); //database connection specification provided by user
	if(idx>=0){
	  driver=driver.substring(0, idx);
	}
  
    try {
      Class.forName(driver).newInstance();
    } catch (InstantiationException e) {
      JOptionPane.showMessageDialog(null,
          "Instantiation exception for connection to DB.\n" + e);
    } catch (IllegalAccessException e) {
      JOptionPane.showMessageDialog(null, "Cannot access DB connection.\n" + e);
    } catch (ClassNotFoundException e) {
      JOptionPane.showMessageDialog(null, "Driver class not found.\n" + e);
    }
    if(idx>=0){ //CUSTOM USER DEFINED DRIVER
        conn = DriverManager.getConnection(url, userName, password);
    }else{ //Built-in JDBC driver
    	conn = DriverManager.getConnection(url + "/" + dbName, userName, password);
    }
  }

  /**
   * This method retrieves the query result as a resultset.
   */
  public static ResultSet getresults(String sql) throws SQLException {
    Statement st = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE); //HAE: 8 Mar 2010 TEMPORARILLY CHANGED
    ResultSet rs = st.executeQuery(sql);
    return rs;
  }

  /**
   * This method gets the required data out of the resultset object, and prints
   * it to a tab-delimited file.
   */
  public static void printResultSet(ResultSet rs) throws SQLException {

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
   * this method writes the first two columns of rs as a siffile
   */
  public static void writeSIFFromDatabase(PrintWriter w, ResultSet rs)
      throws SQLException {
    do {
      w.println(rs.getObject(nodeColumns[0]) + " interacts " + rs.getObject(nodeColumns[1]));

    } while (rs.next());
  }

  /**
   * this method puts every attribute from a specific column in a hashmap
   */
  private static Map<String, String> map;
  private static int[] nodeColumns; //Hai DNV

/** Koenv
  public static Map<String, String> getAttributeTypes(ResultSet rs,
      Object[] possibilities, int startCol) throws SQLException {
*/
  public static Map<String, String> getAttributeTypes(ResultSet rs,
	      ArrayList<Object> possibilities, int startCol) throws SQLException {
  	int colCount = rs.getMetaData().getColumnCount();
    int sourceNodeChosen=0; //Hai DNV
    int targetNodeChosen=0; //Hai DNV
    Map<String, String> map = new HashMap<String, String>();
    nodeColumns=new int[2];
    while((sourceNodeChosen==0) || (targetNodeChosen==0)){//Hai DNV
    	ArrayList<Object> tempPos=new ArrayList<Object>(possibilities);//Hai DNV
    	sourceNodeChosen=0;//Hai DNV
    	targetNodeChosen=0;//Hai DNV
	    for (int i = startCol; i <= colCount; i++) {
	      String name = rs.getMetaData().getColumnLabel(i);
	      // vraag type
	      String type = chooseAttributeType(name, tempPos);
	      map.put(name, type);
	      //<Hai DNV
	      if(type.equals("source node")) { //possibilities[0] is always "source node"
	    	  tempPos.remove("source node"); 
	    	  sourceNodeChosen=1;
	    	  nodeColumns[0]=i;
	      }
	      if(type.equals("target node")) { //possibilities[1] is always "target node"
	    	  tempPos.remove("target node");
	    	  targetNodeChosen=1;
	    	  nodeColumns[1]=i;
	      }
	      //Hai DNV>
	    }
	    if(sourceNodeChosen==0 || targetNodeChosen==0)
	        JOptionPane.showMessageDialog(Cytoscape.getDesktop(),
	                "You have not specified two columns as source and target nodes", "Do it again please!",
	                JOptionPane.ERROR_MESSAGE);
    }
    return map;
  }

  /**
   * this method asks the user if it's a node or edge attribute
   */
  public static String chooseAttributeType(String name, ArrayList<Object> possibilities) {
    String s = (String)JOptionPane.showInputDialog(null, name
        + " is used as a:", "Define attribute type",
        JOptionPane.PLAIN_MESSAGE, null, possibilities.toArray(), possibilities.toArray()[0]);
    return s;
  }

  /**
   * this method sets the node or edge attributes
   */
  public static void setAttributesForCurrentRow(final ResultSet rs)
      throws SQLException {

    CyAttributes node_attr = Cytoscape.getNodeAttributes();
    CyAttributes edge_attr = Cytoscape.getEdgeAttributes();

    int colCount = rs.getMetaData().getColumnCount();

//    for (int i = 3; i <= colCount; i++) { //Koenv
    for (int i = 1; i <= colCount; i++) { //Hai DNV
      if(i!=nodeColumns[0] && i!=nodeColumns[1]){
	      String name = rs.getMetaData().getColumnLabel(i);
	      String type = map.get(name);
	      CyAttributes attr;
	      if (type.equals("source node attribute")
	          || type.equals("target node attribute"))
	        attr = node_attr;
	      else
	        attr = edge_attr;
	
	      String id;
	      if (type.equals("source node attribute"))
	        //id = rs.getString(1); //Koenv
	        id = rs.getString(nodeColumns[0]); //Hai DNV
	      else if (type.equals("target node attribute"))
	        //id = rs.getString(2); //Koenv
	    	id = rs.getString(nodeColumns[1]); //Hai DNV
	      else
	//        id = rs.getString(1) + " (interacts) " + rs.getString(2);//Koenv
	          id = rs.getString(nodeColumns[0]) + " (interacts) " + rs.getString(nodeColumns[1]);//Koenv
	      if (rs.getObject(i) == null)
	        continue; // can't put null attribute
	      int t = rs.getMetaData().getColumnType(i);
	
	      if (t == Types.VARCHAR) {
	        // System.out.println("got a varchar");
	        attr.setAttribute(id, name, rs.getString(i));
	      } else if (t == Types.DOUBLE || t == Types.REAL) {
	        // System.out.println("got a double");
	        attr.setAttribute(id, name, rs.getDouble(i));
	
	      } else if (t == Types.FLOAT) {
	        // System.out.println("got a float");
	        attr.setAttribute(id, name, rs.getDouble(i));
	        // node_attr.setUserEditable(rs.getString(1), true);
	        // node_attr.setUserVisible(rs.getString(1), true);
	      } else if (t == Types.INTEGER) {
	        // System*/.out.println("got a integer");
	        attr.setAttribute(id, name, rs.getInt(i));
	      } else {
	        attr.setAttribute(id, name, rs.getString(i));
	      }
	    }
    }
  }//for loop
  
  //FOR autocompletion the SQL Query (table_names and column_names).

  /**
   * @return tables list, filtered by schema
   */
  public static synchronized List getTables(String schema,String tableType) {
	
    ArrayList list = new ArrayList();
    ResultSet rset = null;
    try {
      rset = getConnection().getMetaData().getTables(
          null, //schema.length()==0?null:schema.toUpperCase(),
          schema.length()==0?null:schema.toUpperCase(),
          null,
          new String[]{tableType});
      while (rset.next())
        try {
//          JOptionPane.showMessageDialog(null, "DA VAO DAY");
//        list.add((schema.length()>0?schema.toUpperCase()+".":"")+rset.getString(3));
          list.add(rset.getString(3));
        } catch (SQLException ex1) {
          ex1.printStackTrace();
        }
    } catch (Exception ex) {
      ex.printStackTrace();
    }
    finally {
      try {
        Statement stmt = rset==null?null:rset.getStatement();
        try {
          rset.close();
        }
        catch (Exception ex3) {
        }
        try {
          stmt.close();
        }
        catch (Exception ex4) {
        }
      }
      catch (Exception ex2) {
      }
    }
    return list;
  }

  /**
   * @return catalogs list
   */
  public static synchronized List getSchemas() {
	
    ArrayList list = new ArrayList();
    ResultSet rset = null;
    try {
      rset = getConnection().getMetaData().getSchemas();
      while(rset.next())
        list.add(rset.getString(1));
    } catch (Exception ex) {
      if (ex.getMessage().indexOf("Caratteristica opzionale non implementata")==-1)
        ex.printStackTrace();
    }
    finally {
      try {
        Statement stmt = rset==null?null:rset.getStatement();
        try {
          rset.close();
        }
        catch (Exception ex3) {
        }
        try {
          stmt.close();
        }
        catch (Exception ex4) {
        }
      }
      catch (Exception ex1) {
      }
    }
    return list;
  }

  /**
   * @param tableName table name
   * @return table columns
   */
  public static synchronized TableModel getTableColumns(String tableName) {
	 
    CustomTableModel model = new CustomTableModel(new String[]{
      Options.getInstance().getResource("column"),
      Options.getInstance().getResource("data type"),
      Options.getInstance().getResource("pk"),
      Options.getInstance().getResource("null?"),
      Options.getInstance().getResource("default")
    },new Class[]{
      String.class,
      String.class,
      Integer.class,
      Boolean.class,
      String.class
    });
    try {
      Hashtable pk = new Hashtable();
      String tName = tableName;
      String schema = null;
      if (tName.indexOf(".")>-1) {
        schema = tName.substring(0,tName.indexOf("."));
        tName = tName.substring(tName.indexOf(".")+1);
      }
      ResultSet rset0 = null;
      try {
        rset0 = getConnection().getMetaData().getPrimaryKeys(null, schema,
            tName.toString());
        while (rset0.next()) {
          pk.put(rset0.getString(4), rset0.getString(5));
        }
      }
      catch (SQLException ex1) {
//        JOptionPane.showMessageDialog(parent,"Error while fetching PKs:\n"+ex1.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);
        if (ex1.getMessage().indexOf("Driver does not support this function")==-1)
          ex1.printStackTrace();
      }
      finally {
        try {
        Statement stmt = rset0==null?null:rset0.getStatement();
          try {
            rset0.close();
          }
          catch (Exception ex3) {
          }
          try {
            stmt.close();
          }
          catch (Exception ex4) {
          }
        }
        catch (Exception ex1) {
        }
      }

      Hashtable defaults = new Hashtable();
      ResultSet rset1 = null;
      try {
        rset1 = getConnection().getMetaData().getColumns(null, schema, tName, null);
        String colValue = null;
        String colName = null;
        while (rset1.next()) {
          try {
            colName = rset1.getString(4);
            colValue = rset1.getString(13);
            if (colValue != null) {
              defaults.put(colName,colValue);
            }
          }
          catch (SQLException ex2) {
          }
        }
      }
      catch (SQLException ex1) {
//        JOptionPane.showMessageDialog(parent,"Error while fetching PKs:\n"+ex1.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);
        if (ex1.getMessage().indexOf("Driver does not support this function")==-1)
          ex1.printStackTrace();
      }
      finally {
        try {
        Statement stmt = rset1==null?null:rset1.getStatement();
          try {
            rset1.close();
          }
          catch (Exception ex3) {
          }
          try {
            stmt.close();
          }
          catch (Exception ex4) {
          }
        }
        catch (Exception ex1) {
        }
      }

      ResultSet rset = null;
      try {
        rset = getConnection().createStatement().executeQuery(
            "select * from " + tableName);
        Vector data = new Vector();

        String type = null;
        for (int i = 0; i < rset.getMetaData().getColumnCount(); i++) {
          Vector row = new Vector();
          row.add(rset.getMetaData().getColumnName(i + 1));
          type = rset.getMetaData().getColumnTypeName(i + 1);
          if ( (rset.getMetaData().getColumnType(i + 1) == Types.VARCHAR ||
                rset.getMetaData().getColumnType(i + 1) == Types.LONGVARCHAR ||
                rset.getMetaData().getColumnType(i + 1) == Types.CHAR) &&
              rset.getMetaData().getPrecision(i + 1) == 0) // case MySQL...
            type += "(" + rset.getMetaData().getColumnDisplaySize(i + 1) + ")";
          else if (rset.getMetaData().getColumnType(i + 1) == Types.BIGINT ||
                   rset.getMetaData().getColumnType(i + 1) == Types.CHAR ||
                   rset.getMetaData().getColumnType(i + 1) == Types.INTEGER ||
                   rset.getMetaData().getColumnType(i + 1) ==
                   Types.LONGVARBINARY ||
                   rset.getMetaData().getColumnType(i + 1) == Types.NUMERIC &&
                   rset.getMetaData().getPrecision(i + 1) > 0 &&
                   rset.getMetaData().getScale(i + 1) == 0 ||
                   rset.getMetaData().getColumnType(i + 1) == Types.SMALLINT ||
                   rset.getMetaData().getColumnType(i + 1) == Types.VARCHAR ||
                   rset.getMetaData().getColumnType(i + 1) == Types.LONGVARCHAR)
            type += "(" + rset.getMetaData().getPrecision(i + 1) + ")";
          else if (rset.getMetaData().getColumnType(i + 1) == Types.DECIMAL ||
                   rset.getMetaData().getColumnType(i + 1) == Types.DOUBLE ||
                   rset.getMetaData().getColumnType(i + 1) == Types.FLOAT ||
                   rset.getMetaData().getColumnType(i + 1) == Types.NUMERIC &&
                   rset.getMetaData().getPrecision(i + 1) > 0 ||
                   rset.getMetaData().getColumnType(i + 1) == Types.REAL)
            type += "(" + rset.getMetaData().getPrecision(i + 1) + "," +
                rset.getMetaData().getScale(i + 1) + ")";
          row.add(type);
          row.add(pk.containsKey(rset.getMetaData().getColumnName(i + 1)) ?
                  new
                  Integer(pk.get(rset.getMetaData().getColumnName(i + 1)).toString().
                          trim()) : null);
          row.add(new Boolean(rset.getMetaData().isNullable(i + 1) ==
                              ResultSetMetaData.columnNullable));
          row.add(defaults.get(rset.getMetaData().getColumnName(i + 1)));
          data.add(row);
        }
        model.setDataVector(data);
        return model;
      }
      catch (Exception ex1) {
//        JOptionPane.showMessageDialog(parent,"Error while fetching PKs:\n"+ex1.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);
        if (ex1.getMessage().indexOf("Driver does not support this function")==-1)
          ex1.printStackTrace();
      }
      finally {
        try {
        Statement stmt = rset==null?null:rset.getStatement();
          try {
            rset.close();
          }
          catch (Exception ex3) {
          }
          try {
            stmt.close();
          }
          catch (Exception ex4) {
          }
        }
        catch (Exception ex1) {
        }
      }
    } catch (Exception ex) {
      ex.printStackTrace();
    }
    return model;
  }

  
  
  
}
