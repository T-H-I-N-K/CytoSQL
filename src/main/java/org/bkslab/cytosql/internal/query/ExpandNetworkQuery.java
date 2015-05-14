package org.bkslab.cytosql.internal.query;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.swing.JOptionPane;
import javax.swing.ProgressMonitor;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import cytoscape.Cytoscape;
import cytoscape.data.CyAttributes;

public class ExpandNetworkQuery {
  // Case 2: Extend network based on Selected Nodes
  // For each selected node:
  // First column becomes new node in network connected to corresponding
  // selected node
  // either as a source node or target node (user can specify)
  // Rest of the columns become attributes for:
  // original selected node
  // newly created node
  // newly created edge

  private static int selectedNodesNo;
  public static void expandNetwork(Map<String, ResultSet> SQLResult) throws Exception{
	  final ProgressMonitor pm = new ProgressMonitor(null,
			  "Expanding network", null, 1, selectedNodesNo);
	  int progress = 0;
	  Map<String, String> attrmap = null;
	  String newNodeType = null;
	  
	  for(String key : SQLResult.keySet()){
		  ResultSet rs=SQLResult.get(key);
		  if (attrmap == null) {
	        	//<Hai DNV
	      	int colCount = rs.getMetaData().getColumnCount();
	          if (colCount < 1) {
	              JOptionPane.showMessageDialog(null,
	                  "Query returns has no column. Can't expand graph", "Query check",
	                  JOptionPane.ERROR_MESSAGE);
	              return;
	          }
	        	ArrayList<Object> possibilities=new ArrayList();
	        	possibilities.add("newly added node");
	        	possibilities.add("newly added node attribute");
	        	possibilities.add("original selected node attribute");
	        	possibilities.add("edge attribute");    	
	        	attrmap = getAttributeTypes(rs, possibilities, 1);
	        	//Hai DNV>
	        }
	        System.out.println("#### End batchresults");
	        // if first query executed --> get new node type: source or target
	        if (newNodeType == null)
	          newNodeType = getNewNodeType(rs.getMetaData().getColumnLabel(newNodeColumnInd));
	        addNewNodesAndAttributes(key, rs, attrmap, newNodeType);
	        progress += 1;
	        pm.setProgress(progress);
	  }
	  Cytoscape.getCurrentNetworkView().updateView();
  }
  
  public static Map<Integer, String> bindvarMapInitialize(String sql){
	    int numBindVars = countQuestionMarks(sql);
	    CyAttributes node_attr = Cytoscape.getNodeAttributes();
	    // ## FOR NOW ONLY FOR NODES
	    CyAttributes attr = node_attr;
	    Map<Integer, String> bindvarMap = mapBindVars(numBindVars, attr);
	    // prepare statement
	    return bindvarMap;
  }
  public static Map<String, ResultSet> processBatchResults(String sql, Map<Integer, String> bindvarMap, Connection conn)
      throws SQLException {
/** TEMP DELETED	  
    int numBindVars = countQuestionMarks(sql);
    CyAttributes node_attr = Cytoscape.getNodeAttributes();
    // ## FOR NOW ONLY FOR NODES
    CyAttributes attr = node_attr;
    Map<Integer, String> bindvarMap = mapBindVars(numBindVars, attr);
*/
    CyAttributes node_attr = Cytoscape.getNodeAttributes();
    // ## FOR NOW ONLY FOR NODES
    CyAttributes attr = node_attr;
    int numBindVars=bindvarMap.size();
    
    // prepare statement
    Map<String, ResultSet> previewResult=new HashMap<String, ResultSet>();
    Set<CyNode> selectedNodes = (Set<CyNode>)Cytoscape.getCurrentNetwork()
        .getSelectedNodes();
    if (selectedNodes.isEmpty()) {
      Cytoscape.getCurrentNetwork().selectAllNodes();
      selectedNodes = Cytoscape.getCurrentNetwork().getSelectedNodes();
    }
    selectedNodesNo=selectedNodes.size();

    for (CyNode node : selectedNodes) {
   	  PreparedStatement p = conn.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
      String id = node.getIdentifier();
      System.out.println("### Initializing bind vars for node id " + id);
      initBindVars(id, numBindVars, attr, bindvarMap, p);
      System.out.println("### prepared statement " + p);
      ResultSet rs = p.executeQuery();
      previewResult.put(id, rs);
    }
    return previewResult;
  }

  //Hai DNV. For preview SQL result
  public static ResultSet getSQLResultSet4oneSelectedNode(String sql, Connection conn)
  	throws SQLException {
	    ResultSet rs = null;
	    int numBindVars = countQuestionMarks(sql);

	    CyAttributes node_attr = Cytoscape.getNodeAttributes();
	    // ## FOR NOW ONLY FOR NODES
	    CyAttributes attr = node_attr;
	    Map<Integer, String> bindvarMap = mapBindVars(numBindVars, attr);
	    // prepare statement
	    PreparedStatement p = conn.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
	    Set<CyNode> selectedNodes = (Set<CyNode>)Cytoscape.getCurrentNetwork()
	        .getSelectedNodes();
	    if (selectedNodes.isEmpty()) {
	      Cytoscape.getCurrentNetwork().selectAllNodes();
	      selectedNodes = Cytoscape.getCurrentNetwork().getSelectedNodes();
	    }
	    String id = selectedNodes.iterator().next().getIdentifier();
	    System.out.println("### Initializing bind vars for node id " + id);
	    initBindVars(id, numBindVars, attr, bindvarMap, p);
	    System.out.println("### prepared statement " + p);
	    rs = p.executeQuery();
	    return rs;
  }
  
  private static String getNewNodeType(String colName4newNode) {
    String s = (String)JOptionPane.showInputDialog(null,
        " The newly added nodes which is specified by column \'"+colName4newNode+"\' is:", "Define node type",
        JOptionPane.PLAIN_MESSAGE, null, new Object[] { "Source node",
            "Target node" }, "Source node");
    return s;
  }

  private static void initBindVars(String id, int numBindVars,
      CyAttributes attr, Map<Integer, String> bindvarMap, PreparedStatement p)
      throws SQLException {
    for (int i = 0; i < numBindVars; i++) {
      String attributeName = bindvarMap.get(i);
      System.out.println(attributeName + " = attributeName");
      Object attributeValue = null;
      if (attributeName.equals("ID")) {
        System.out.println("assigning id to ID ");
        attributeValue = id;
      } else {
        switch (attr.getType(attributeName)) {
          case CyAttributes.TYPE_BOOLEAN:
            attributeValue = attr.getBooleanAttribute(id, attributeName);
            break;
          case CyAttributes.TYPE_INTEGER:
            attributeValue = attr.getIntegerAttribute(id, attributeName);
            break;
          case CyAttributes.TYPE_FLOATING:
            attributeValue = attr.getDoubleAttribute(id, attributeName);
            break;
          case CyAttributes.TYPE_STRING:
            attributeValue = attr.getStringAttribute(id, attributeName);
            break;
          case CyAttributes.TYPE_SIMPLE_LIST:
          case CyAttributes.TYPE_SIMPLE_MAP:
          case CyAttributes.TYPE_COMPLEX:
          case CyAttributes.TYPE_UNDEFINED:
            JOptionPane.showMessageDialog(null,
                "Cannot handle this attribute type!");
            throw new RuntimeException();
            // break;
        }
      }
      System.out.println(attributeValue + " = attributeValue");
      p.setObject(i + 1, attributeValue);
    }
  }

  private static Map<Integer, String> mapBindVars(int numBindVars,
      CyAttributes attr) {
    // map bindvars onto node attributes
    Map<Integer, String> bindvarMap = new HashMap<Integer, String>();
    String[] names = attr.getAttributeNames();
    String[] options = new String[names.length + 1];
    options[0] = "ID"; // node identifier
    System.arraycopy(names, 0, options, 1, names.length);
    if (names.length == 0) {
      JOptionPane.showMessageDialog(null,
          "No attributes to map onto bind vars!");
      throw new RuntimeException();
    }
    for (int i = 0; i < numBindVars; i++) {
      String s = (String)JOptionPane.showInputDialog(null,
          "Choose attribute mapped to bind variable " + i,
          "Map bind variables", JOptionPane.PLAIN_MESSAGE, null, options,
          options[0]);
      bindvarMap.put(i, s);
    }
    return bindvarMap;
  }

  private static void addNewNodesAndAttributes(String id, ResultSet rs,
      Map<String, String> attrmap, String newNodeType) throws SQLException {
    // create new nodes and new edges for first results column
    CyNetwork network = Cytoscape.getCurrentNetwork();

    CyNode originalNode = Cytoscape.getCyNode(id);
//    JOptionPane.showMessageDialog(null, originalNode + " = originalNode");
    int colCount = rs.getMetaData().getColumnCount();
    while (rs.next()) {
      CyNode newNode = Cytoscape.getCyNode(rs.getString(newNodeColumnInd), true); //previous default value is 1 by Koenv
//      JOptionPane.showMessageDialog(null, newNode + " = newNode");
      network.addNode(newNode);
      Cytoscape.firePropertyChange(Cytoscape.NETWORK_MODIFIED, null, null);

      CyEdge edge = null;
      if (newNodeType.equals("Source node")) {
        edge = Cytoscape.getCyEdge(newNode, originalNode, "interaction",
            "interacts", true);
      } else if (newNodeType.equals("Target node")) {
        edge = Cytoscape.getCyEdge(originalNode, newNode, "interaction",
            "interacts", true);
      } else {
        throw new RuntimeException("Error in finding node type");
      }
      network.addEdge(edge);
      System.out.println(edge + " = edge");
      if (colCount > 1)
        setAttributesForCurrentRow(id, rs, attrmap, newNodeType, colCount);
    }
    Cytoscape.firePropertyChange(Cytoscape.NETWORK_MODIFIED, null, null);
    Cytoscape.firePropertyChange(Cytoscape.ATTRIBUTES_CHANGED, null, null);
  }

  private static void setAttributesForCurrentRow(String id, ResultSet rs,
      Map<String, String> attrmap, String newNodeType, int colCount)
      throws SQLException {

    CyAttributes node_attr = Cytoscape.getNodeAttributes();
    CyAttributes edge_attr = Cytoscape.getEdgeAttributes();

    // add rest of attributes per column
    for (int i = 1; i <= colCount; i++) { //previously "i=2" was used by Koenv
    	if(i!=newNodeColumnInd){
    		System.out.println("### getting rest of columns");
    		String attrName = rs.getMetaData().getColumnLabel(i);
    		System.out.println(attrName + " = attrName");
    		String type = attrmap.get(attrName);
    		System.out.println(type + " = type");
    		CyAttributes attr;
    		if (type.equals("original selected node attribute") //"Selected Node" used by Koenv
    				|| type.equals("newly added node attribute"))
    			attr = node_attr;
    		else
    			attr = edge_attr;
		
    		String id2;
    		if (type.equals("original selected node attribute")) { //"Selected Node" used by Koenv
    			id2 = id;
    		} else if (type.equals("newly added node attribute")) { //"Newly created Node (first results column)" used by Koenv
    			id2 = rs.getString(newNodeColumnInd); //1 used by Koenv
    		} else if (type.equals("edge attribute")) {
    			if (newNodeType.equals("Source node")) {
    				id2 = rs.getString(newNodeColumnInd) + " (interacts) " + id; //1 used by Koenv rather than "newNodeColumnId"
    			} else {
    				id2 = id + " (interacts) " + rs.getString(newNodeColumnInd); //1 used by Koenv rather than "newNodeColumnId"
    			}
    		} else {
    			throw new RuntimeException("couldn't assign attribute");
    		}
		
    		System.out.println(id2 + " = id2");
    		int t = rs.getMetaData().getColumnType(i);
    		if (t == Types.VARCHAR) {
    			attr.setAttribute(id2, attrName, rs.getString(i));
    		} else if (t == Types.DOUBLE || t == Types.REAL) {
    			attr.setAttribute(id2, attrName, rs.getDouble(i));
    		} else if (t == Types.FLOAT) {
    			attr.setAttribute(id2, attrName, rs.getDouble(i));
    		} else if (t == Types.INTEGER) {
    			attr.setAttribute(id2, attrName, rs.getInt(i));
    		} else {
    			attr.setAttribute(id2, attrName, rs.getString(i));
    		}
    	}
    }
  }

  private static int countQuestionMarks(String s) {
    int count = 0;
    int fromindex = 0;
    while (true) {
      int index = s.indexOf("?", fromindex);
      if (index == -1)
        break;
      count++;
      fromindex = index + 1;
    }
    return count;
  }
  //<Hai DNV
  private static int newNodeColumnInd=0;
  private static Map<String, String> getAttributeTypes(ResultSet rs,
	      ArrayList<Object> possibilities, int startCol) throws SQLException {
  	int colCount = rs.getMetaData().getColumnCount();
    int newNodeChosen=0; //Hai DNV
    Map<String, String> map = new HashMap<String, String>();
    while(newNodeChosen==0){//Hai DNV
    	ArrayList<Object> tempPos=new ArrayList<Object>(possibilities);//Hai DNV
    	newNodeChosen=0;//Hai DNV
	    for (int i = startCol; i <= colCount; i++) {
	      String name = rs.getMetaData().getColumnLabel(i);
	      // vraag type
	      String type = chooseAttributeType(name, tempPos);
	      map.put(name, type);
	      if(type.equals("newly added node")) {
	    	  tempPos.remove("newly added node");
	    	  newNodeChosen=1;
	    	  newNodeColumnInd=i;
	      }
	    }
	    if(newNodeChosen==0)
	        JOptionPane.showMessageDialog(null,
	                "You have not specified a column as newly created nodes", "Do it again please!",
	                JOptionPane.ERROR_MESSAGE);
    }
    return map;
  }

  /**
   * this method asks the user if it's a node or edge attribute
   */
  private static String chooseAttributeType(String name, ArrayList<Object> possibilities) {
    String s = (String)JOptionPane.showInputDialog(null, name
        + " is used as a:", "Define attribute type",
        JOptionPane.PLAIN_MESSAGE, null, possibilities.toArray(), possibilities.toArray()[0]);
    return s;
  }
  //Hai DNV>
}
