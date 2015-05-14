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

import javax.swing.JOptionPane;
import javax.swing.ProgressMonitor;

import org.cytoscape.model.CyEdge;
import cytoscape.Cytoscape;
import cytoscape.data.CyAttributes;

public class UpdateEdgeAttributesQuery {
  // Case 1: Update Selected Edges attributes
  // Caveat: what if resultset for a node returns more than one row? Especially
  // if they are different?
  private static int selectedNodesNo;	
  public static void updateEdgeAttribute(Map<String, ResultSet> SQLResult) throws Exception{
	  final ProgressMonitor pm = new ProgressMonitor(null,
			  "Updating edge attributes", null, 1, selectedNodesNo);
	  int progress = 0;
	  for(String key : SQLResult.keySet()){
		  ResultSet rs=SQLResult.get(key);
	      addEdgeAttributes(key, rs);
	      progress += 1;
	      pm.setProgress(progress);
	  }
	  Cytoscape.firePropertyChange(Cytoscape.ATTRIBUTES_CHANGED, null, null);
	  showMultipleWarning();
	  multiple.clear();
  }
  
  public static Map<Integer, String[]> bindvarMapInitialize(String sql){
	  int numBindVars = countQuestionMarks(sql);    
	  Map<Integer, String[]> bindvarMap = mapBindVars(numBindVars);
	  return bindvarMap;
}
  
  public static Map<String, ResultSet> processBatchResults(String sql, Map<Integer, String[]>bindvarMap, Connection conn)
      throws SQLException {
    ResultSet rs = null;
/**
    int numBindVars = countQuestionMarks(sql);    
    Map<Integer, String[]> bindvarMap = mapBindVars(numBindVars);
*/
    int numBindVars=bindvarMap.size();
    Map<String, ResultSet> previewResult=new HashMap<String, ResultSet>();
    // prepare statement
    Set<CyEdge> selectedEdges = (Set<CyEdge>)Cytoscape.getCurrentNetwork()
        .getSelectedEdges();
    if (selectedEdges.isEmpty()) {
      Cytoscape.getCurrentNetwork().selectAllEdges();
      selectedEdges = Cytoscape.getCurrentNetwork().getSelectedEdges();
    }
    selectedNodesNo=selectedEdges.size();

    for (CyEdge edge : selectedEdges) {
      PreparedStatement p = conn.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
      String id = edge.getIdentifier();
      System.out.println("### Initializing bind vars for edge id " + id);
      initBindVars(edge, numBindVars, bindvarMap, p);
      System.out.println("### prepared statement " + p);
      rs = p.executeQuery();
      previewResult.put(id, rs);
    }
    return previewResult;
  }
//Hai DNV. For preview the SQL result
  public static ResultSet getSQLResultSet4oneSelectedEdge(String sql, Connection conn)
	throws SQLException {
	  ResultSet rs=null;
	  int numBindVars = countQuestionMarks(sql);

	  Map<Integer, String[]> bindvarMap = mapBindVars(numBindVars);

	    // prepare statement
	  PreparedStatement p = conn.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
	  Set<CyEdge> selectedEdges = (Set<CyEdge>)Cytoscape.getCurrentNetwork()
	  	.getSelectedEdges();
	  if (selectedEdges.isEmpty()) {
	      Cytoscape.getCurrentNetwork().selectAllEdges();
	      selectedEdges = Cytoscape.getCurrentNetwork().getSelectedEdges();
	  }
	  CyEdge edge= selectedEdges.iterator().next();
	  System.out.println("### Initializing bind vars for edge id " + edge.getIdentifier());
	  initBindVars(edge, numBindVars, bindvarMap, p);
	  System.out.println("### prepared statement " + p);
	  rs = p.executeQuery();
	  return rs;
  }
  private static void initBindVars(CyEdge edge, int numBindVars,
      Map<Integer, String[]> bindvarMap, PreparedStatement p)
      throws SQLException {

    CyAttributes node_attr = Cytoscape.getNodeAttributes();
    CyAttributes edge_attr = Cytoscape.getEdgeAttributes();

    CyAttributes attr = null;

    for (int i = 0; i < numBindVars; i++) {
      String attributeEntity = bindvarMap.get(i)[0];
      String attributeName = bindvarMap.get(i)[1];
      System.out.println(attributeEntity + " = attributeEntity");
      System.out.println(attributeName + " = attributeName");
      Object attributeValue = null;

      String id = null;
      if (attributeEntity.equals("Source node of selected edge")) {
        id = edge.getSource().getIdentifier();
        attr = node_attr;
      } else if (attributeEntity.equals("Target node of selected edge")) {
        id = edge.getTarget().getIdentifier();
        attr = node_attr;
      } else if (attributeEntity.equals("Selected edge")) {
        id = edge.getIdentifier();
        attr = edge_attr;
      } else {
        JOptionPane.showMessageDialog(null,
            "Wrong attribute entity! This shouldn't happen. Call developer.");
        throw new RuntimeException();
      }

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

  private static Map<Integer, String[]> mapBindVars(int numBindVars) {

    CyAttributes node_attr = Cytoscape.getNodeAttributes();
    CyAttributes edge_attr = Cytoscape.getEdgeAttributes();

    // collect names of node attributes
    String[] node_atrr_names = node_attr.getAttributeNames();
    String[] node_attr_options = new String[node_atrr_names.length + 1];
    node_attr_options[0] = "ID"; // node identifier
    System.arraycopy(node_atrr_names, 0, node_attr_options, 1,
        node_atrr_names.length);

    // collect names of edge attributes
    String[] edge_atrr_names = edge_attr.getAttributeNames();
    String[] edge_attr_options = new String[edge_atrr_names.length + 1];
    edge_attr_options[0] = "ID"; // node identifier
    System.arraycopy(edge_atrr_names, 0, edge_attr_options, 1,
        edge_atrr_names.length);

    Map<Integer, String[]> bindvarMap = new HashMap<Integer, String[]>();

    // for each bindvar
    for (int i = 0; i < numBindVars; i++) {
      // first ask if it has to be an attribute of source-node,
      // target-node, edge
      String entity = (String)JOptionPane.showInputDialog(null, "Bind variable " + i
          + " must be mapped to an attribute of the:", "Map bind variables",
          JOptionPane.PLAIN_MESSAGE, null, new String[] {
              "Source node of selected edge", "Target node of selected edge",
              "Selected edge" }, "Source node of selected edge");

      String[] options;
      if (entity.equals("Selected edge"))
        options = edge_attr_options;
      else
        options = node_attr_options;

      // then ask for the attribute
      String s = (String)JOptionPane.showInputDialog(null,
          "Choose the " + entity + " attribute mapped to bind variable " + i,
          "Map bind variables", JOptionPane.PLAIN_MESSAGE, null, options,
          options[0]);

      // put into bindvarMap with info
      bindvarMap.put(i, new String[] { entity, s });
    }
    return bindvarMap;
  }

  private static ArrayList<String> multiple = new ArrayList<String>();

  private static void addEdgeAttributes(String id, ResultSet rs)
      throws SQLException {
    int colCount = rs.getMetaData().getColumnCount();
    CyAttributes attr = Cytoscape.getEdgeAttributes();
    int counter = 0;
    
    while (rs.next()) {
      counter++;
      if(counter==2) break; //Only use the first record for updating the node
      for (int i = 1; i <= colCount; i++) {
        String name = rs.getMetaData().getColumnLabel(i);

        if (rs.getObject(i) == null) //HAE: Added on 6 Mar 2010. Just ignore updating edge's attribute with Null value, then continue
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
          .format("Query returned more than 1 row for the following edge(s): "
              + Util.join(multiple, ", ") + ".\n");
      JOptionPane.showMessageDialog(null, text, "Warning",
          JOptionPane.WARNING_MESSAGE);
    }
  }
}
