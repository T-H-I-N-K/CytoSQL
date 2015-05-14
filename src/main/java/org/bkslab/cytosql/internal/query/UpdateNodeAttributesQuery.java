package org.bkslab.cytosql.internal.query;

import org.bkslab.cytosql.internal.util.Util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import javax.swing.JOptionPane;
import javax.swing.ProgressMonitor;

import org.cytoscape.model.CyNode;
import cytoscape.Cytoscape;
import cytoscape.data.CyAttributes;

public class UpdateNodeAttributesQuery {
  // Case 1: Update Selected Nodes attributes
  // Caveat: what if resultset for a node returns more than one row? Especially
  // if they are different?

  private static int selectedNodesNo;
  public static void updateNodeAttributes(Map<String, ResultSet>SQLResult) throws Exception{
	  final ProgressMonitor pm = new ProgressMonitor(null,
			  "Updating node attributes", null, 1, selectedNodesNo);
	  int progress = 0;
	  for(String key : SQLResult.keySet()){
		  ResultSet rs=SQLResult.get(key);
	      addNodeAttributes(key, rs);
	      progress += 1;
	      pm.setProgress(progress);
	  }
	  Cytoscape.firePropertyChange(Cytoscape.ATTRIBUTES_CHANGED, null, null);
	  showMultipleWarning();
	  multiple.clear();
  }
  
  public static Map<Integer, String> bindvarMapInitialize(String sql){
	  int numBindVars = countQuestionMarks(sql);
	  CyAttributes node_attr = Cytoscape.getNodeAttributes();
	  CyAttributes edge_attr = Cytoscape.getEdgeAttributes();
	  // ## FOR NOW ONLY FOR NODES
	  CyAttributes attr = node_attr;
	  Map<Integer, String> bindvarMap = mapBindVars(numBindVars, attr);

	  return bindvarMap;
}

  public static Map<String, ResultSet> processBatchResults(String sql, Map<Integer, String> bindvarMap, Connection conn)
      throws SQLException {
//    ResultSet rs = null;
/**	  
    int numBindVars = countQuestionMarks(sql);
    CyAttributes node_attr = Cytoscape.getNodeAttributes();
    CyAttributes edge_attr = Cytoscape.getEdgeAttributes();
    // ## FOR NOW ONLY FOR NODES
    CyAttributes attr = node_attr;
    Map<Integer, String> bindvarMap = mapBindVars(numBindVars, attr);
*/
	int numBindVars=bindvarMap.size();
	CyAttributes node_attr = Cytoscape.getNodeAttributes();
	CyAttributes edge_attr = Cytoscape.getEdgeAttributes();
	  // ## FOR NOW ONLY FOR NODES
	CyAttributes attr = node_attr;

    Map<String, ResultSet> previewResult=new HashMap<String, ResultSet>();
    // prepare statement
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
//      JOptionPane.showMessageDialog(null,"### prepared statement " + p);
      ResultSet rs = p.executeQuery();
      previewResult.put(id, rs);
    }
    return previewResult;
  }
//Hai DNV. For preview SQL result.
  public static ResultSet getSQLResultSet4oneSelectedNode(String sql, Connection conn)
	throws SQLException {
	  ResultSet rs=null;
	  int numBindVars = countQuestionMarks(sql);

	  CyAttributes node_attr = Cytoscape.getNodeAttributes();
	  CyAttributes edge_attr = Cytoscape.getEdgeAttributes();
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

  private static ArrayList<String> multiple = new ArrayList<String>();

  private static void addNodeAttributes(String id, ResultSet rs)
      throws SQLException {
    int colCount = rs.getMetaData().getColumnCount();
    CyAttributes attr = Cytoscape.getNodeAttributes();
    int counter = 0;

    while (rs.next()) {
      counter++;
      if (counter==2) break; //Use only the first record in duplicated set for updating node.
      for (int i = 1; i <= colCount; i++) {
        String name = rs.getMetaData().getColumnLabel(i);

        if (rs.getObject(i) == null) //HAE: Added on 6 Mar 2010. Just ignore updating node's attribute with Null value, then continue
        	continue; // can't put null attribute

        int t = rs.getMetaData().getColumnType(i);
        if (t == Types.VARCHAR) {
          attr.setAttribute(id, name, rs.getString(i));
        } else if (t == Types.DOUBLE || t == Types.REAL) {
          attr.setAttribute(id, name, rs.getDouble(i));
        } else if (t == Types.FLOAT) {
          attr.setAttribute(id, name, rs.getDouble(i));
          // node_attr.setUserEditable(rs.getString(1), true);
          // node_attr.setUserVisible(rs.getString(1), true);
        } else if (t == Types.INTEGER) {
          attr.setAttribute(id, name, rs.getInt(i));
        } else {
          attr.setAttribute(id, name, rs.getString(i));
        }
      }
    }
    if (counter > 1) {
      multiple.add(id);
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

  private static void showMultipleWarning() {
    if (multiple.size() > 0) {
      String text = String
          .format("Query returned more than 1 row for the following node(s): "
              + Util.join(multiple, ", ") + ".\n");
      JOptionPane.showMessageDialog(null, text, "Warning",
          JOptionPane.WARNING_MESSAGE);
    }
  }
}
