package org.bkslab.CytoSQL.internal.tasks;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.io.IOException;

import org.apache.commons.lang3.StringEscapeUtils;



import org.bkslab.CytoSQL.internal.model.DBConnectionManager;
import org.bkslab.CytoSQL.internal.model.DBQuery;
import org.bkslab.CytoSQL.internal.model.DatabaseNetworkMappingParameters;
import org.bkslab.CytoSQL.internal.model.TaskUtils;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableUtil;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.work.TaskMonitor;

public class DatabaseNetworkParser {
	

	// Database parameters
	private final DatabaseNetworkMappingParameters dnmp;
	private DBQuery dbQuery;
	
	private boolean isCanceled;
	
	public DatabaseNetworkParser(
			final DBConnectionManager dbConnectionManager,
			DatabaseNetworkMappingParameters dnmp){

		this.dnmp = dnmp;
		try {
			dbQuery = dbConnectionManager.getDBQuery();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	

	
	public void parseAddNodeAttributes(
		TaskMonitor taskMonitor,
		CyNetwork network,
		CyEventHelper eventHelper,
		CyNetworkViewManager networkViewManager,
		VisualMappingManager visualMappingManager,
		String sqlQuery) throws Exception {
		taskMonitor.setTitle("Add node attributes from SQL query.");
		taskMonitor.setProgress(0.0);

		ResultSet resultSet = dbQuery.getResults(sqlQuery);
		ResultSetMetaData metaData = resultSet.getMetaData();
		
		if(dnmp == null){
			throw new NullPointerException("No DatabaseNetworkMappingParameters specified.");
		} else {
			dnmp.validate(network, resultSet);
		}
		
		Map<Object, CyNode> existingNodes = identifyExistingNodes(
				network,
				this.dnmp.getNodeJoinColumnName());
		Map<Object, CyNode> newNodes = new HashMap<Object, CyNode>();
				
		addNodeAttributeColumns(network, metaData);
		
		while(resultSet.next()){
			if(isCanceled){
				System.out.println("Loading canceled.");
				resultSet.close();
				throw new IOException("Network loading process canceled by user.");
			}
			
			//add the node
			Object nodeName = resultSet.getObject(dnmp.getSourceIndex());
			if(resultSet.wasNull()) continue;
			
			CyNode node = TaskUtils.getNode(network, existingNodes, nodeName);	
			addNodeAttributes(network, node, resultSet, this.dnmp);
		}
		
		resultSet.close();
		
		TaskUtils.updateCytoscapeNodeViews(
			eventHelper,
			networkViewManager,
			visualMappingManager,
			network,
			newNodes.values());
		
		taskMonitor.setProgress(1.0);
	}
	
	public void parseNetworkExtender(
		TaskMonitor taskMonitor,
		CyNetwork network,
		CyEventHelper eventHelper,
		CyNetworkViewManager networkViewManager,
		VisualMappingManager visualMappingManager,
		String sqlQuery
		) throws Exception {
		taskMonitor.setTitle("Extending a network from SQL query.");
		taskMonitor.setProgress(0.0);

		ResultSet resultSet = dbQuery.getResults(sqlQuery);
		ResultSetMetaData metaData = resultSet.getMetaData();
		
		Map<Object, CyNode> existingNodes = identifyExistingNodes(network, this.dnmp.getNodeJoinColumnName());
		Map<Object, CyNode> newNodes = new HashMap<Object, CyNode>();
		Map<Object, CyEdge> newEdges = new HashMap<Object, CyEdge>();
		
		
		addSourceAttributeColumns(network, metaData, dnmp);
		addTargetAttributeColumns(network, metaData, dnmp);
		addEdgeAttributeColumns(network, metaData, dnmp);
				
		while(resultSet.next()){
			if(isCanceled){
				System.out.println("Loading canceled.");
				resultSet.close();
				throw new IOException("Network loading process canceled by user.");
			}
			
			// add the nodes
			Object sourceNodeName = resultSet.getObject(dnmp.getSourceIndex());
			CyNode source = null;
			if(sourceNodeName != null){
				source = TaskUtils.getNode(network, existingNodes, sourceNodeName);
				addSourceNodeAttributes(network, source, newNodes, resultSet, dnmp);
			}

			Object targetNodeName = resultSet.getObject(dnmp.getTargetIndex());
			CyNode target = null;
			if(targetNodeName != null){
				target = TaskUtils.getNode(network, existingNodes, targetNodeName);
				addTargetNodeAttributes(network, target, newNodes, resultSet, dnmp);
			}
				
			// Single column nodes list.  Just add nodes.
			if(source == null || target == null) continue;
			
			//add the edge
			CyEdge edge = network.addEdge(source, target, dnmp.isDirected());
			addEdgeAttributes(network, source, target, edge, resultSet, dnmp);
				
		}
		
		resultSet.close();
		
		TaskUtils.updateCytoscapeNodeViews(
			eventHelper,
			networkViewManager,
			visualMappingManager,
			network,
			newNodes.values());
		
		TaskUtils.updateCytoscapeEdgeViews(
			eventHelper,
			networkViewManager,
			visualMappingManager,
			network,
			newEdges.values());
		
		taskMonitor.setProgress(1.0);
		
		
	}
	
	
	private Map<Object, CyNode> identifyExistingNodes(CyNetwork network, final String joinColumnName){
		Map<Object, CyNode> existingNodes = new HashMap<Object, CyNode>(10000);
		final List<CyNode> nodes = network.getNodeList();
		
		for(final CyNode node : nodes){
			final Object keyValue = network.getRow(node).getRaw(joinColumnName);
			if(keyValue != null){
				existingNodes.put(keyValue, node);
			}
		}
		return existingNodes;
	}
	
	
	private void addNodeAttributes(
		CyNetwork network,
		CyNode node,
		ResultSet resultSet,
		DatabaseNetworkMappingParameters dnmp
		) throws SQLException, IOException{
	
		if(node == null) return;
		
		ResultSetMetaData metaData = resultSet.getMetaData();
		
		for (int i = 1; i <= metaData.getColumnCount(); i++) {
			if(!dnmp.isNodeAttribute(i)) continue;
			
			Object value = resultSet.getObject(i);
			if(resultSet.wasNull()) continue;
			
			if(value.getClass() == String.class){
				value = StringEscapeUtils.unescapeJava((String)value);
			}

			network.getRow(node).set(metaData.getColumnLabel(i), value);
		}
		
	}
	
	private boolean addSourceNodeAttributes(
		CyNetwork network,
		CyNode node,
		Map<Object, CyNode> newNodes,
		ResultSet resultSet,
		DatabaseNetworkMappingParameters dnmp
		) throws SQLException, IOException{
	
		ResultSetMetaData metaData = resultSet.getMetaData();
		
		if(node == null) return false;
		
		Object nodeName = resultSet.getObject(dnmp.getSourceIndex());
		if (newNodes.containsKey(nodeName)) return false;
		newNodes.put(nodeName, node);
		
		for (int i = 1; i <= metaData.getColumnCount(); i++) {
			if(!dnmp.isSourceAttribute(i, metaData.getColumnLabel(i))) continue;
			
			Object value = resultSet.getObject(i);
			if(resultSet.wasNull()) continue;
			
			if(value.getClass() == String.class){
				value = StringEscapeUtils.unescapeJava((String)value);
			}

			network.getRow(node).set(dnmp.getSourceColumnName(metaData.getColumnLabel(i)), value);
		}
		return true;
		
	}
	
	// return true if attributes for the node were added
	private boolean addTargetNodeAttributes(
		CyNetwork network,
		CyNode node,
		Map<Object, CyNode> newNodes,
		ResultSet resultSet,
		DatabaseNetworkMappingParameters dnmp
		) throws SQLException, IOException{
	
		if(node == null) return false;
		
		Object nodeName = resultSet.getObject(dnmp.getTargetIndex());
		if (newNodes.containsKey(nodeName)) return false;
		newNodes.put(nodeName, node);
	
		ResultSetMetaData metaData = resultSet.getMetaData();			
		for (int i = 1; i <= metaData.getColumnCount(); i++) {
			if(!dnmp.isTargetAttribute(i, metaData.getColumnLabel(i))) continue;
			
			Object value = resultSet.getObject(i);
			if(resultSet.wasNull()) continue;
			
			if(value.getClass() == String.class){
				value = StringEscapeUtils.unescapeJava((String)value);
			}

			network.getRow(node).set(dnmp.getTargetColumnName(metaData.getColumnLabel(i)), value);
		}
		return true;
		
	}

	private void addEdgeAttributes(
		CyNetwork network,
		CyNode source,
		CyNode target,
		CyEdge edge,
		ResultSet resultSet,
		DatabaseNetworkMappingParameters dnmp
		) throws SQLException, IOException{
		
		ResultSetMetaData metaData = resultSet.getMetaData();
		
		// maybe we can just rely on the sql query result to have columns named 'interaction' and 'name'?
		String interaction;
		if(dnmp.getInteractionIndex() == -1){
			interaction = dnmp.getDefaultInteraction();
		} else {
			interaction = resultSet.getString(dnmp.getInteractionIndex());
			if(resultSet.wasNull()){
				interaction = dnmp.getDefaultInteraction();
			}
		}
							
		network.getRow(edge).set("interaction", interaction);
		String edgeName = 
			network.getRow(source).get(CyNetwork.NAME, String.class) + 
			" ("+interaction+") " +
			network.getRow(target).get(CyNetwork.NAME, String.class);
		network.getRow(edge).set(CyNetwork.NAME, edgeName);

		// add the edge attributes
		for (int i = 1; i <= metaData.getColumnCount(); i++) {
			if(!dnmp.isEdgeAttribute(i, metaData.getColumnLabel(i))) continue;
			
			Object value = resultSet.getObject(i);
			if(resultSet.wasNull()) continue;
			
			if(value.getClass() == String.class){
				value = StringEscapeUtils.unescapeJava((String)value);
			} else if(value.getClass() == Float.class){
				value = new Double(((Float)value).doubleValue());
			}
			
			network.getRow(edge).set(metaData.getColumnLabel(i), value);
		}
	}
	
	
	private void addNodeAttributeColumns(CyNetwork network, ResultSetMetaData metaData) throws SQLException{

		CyTable nodeAttributesTable = network.getDefaultNodeTable();
		
		// add edge attributes columns to the network
		for(int i = 1; i <= metaData.getColumnCount(); i++){	
			if(!dnmp.isNodeAttribute(i)) continue;
			
			Class<?> attributeType;
			try{
				attributeType = SQLTypeToCytoscapeType(metaData.getColumnType(i));
			} catch(Exception e) {
				throw new SQLException(e.getMessage() + " For node column '" + metaData.getColumnLabel(i) + "'");
			}

			try{
				nodeAttributesTable.createColumn(metaData.getColumnLabel(i), attributeType, dnmp.isMutable());
			} catch(IllegalArgumentException e) {
				// column already exists, do nothing.
			}
		}
	}
	
	private void addSourceAttributeColumns(
		CyNetwork network,
		ResultSetMetaData metaData,
		DatabaseNetworkMappingParameters dnmp
		) throws SQLException{

		CyTable nodeAttributesTable = network.getDefaultNodeTable();
		
		// add edge attributes columns to the network
		for(int i = 1; i <= metaData.getColumnCount(); i++){	
			if(!dnmp.isSourceAttribute(i, metaData.getColumnLabel(i))) continue;
			
			Class<?> attributeType;
			try{
				attributeType = SQLTypeToCytoscapeType(metaData.getColumnType(i));
			} catch(Exception e) {
				throw new SQLException(e.getMessage() + " For source column '" + metaData.getColumnLabel(i) + "'");
			}

			try{
				nodeAttributesTable.createColumn(metaData.getColumnLabel(i), attributeType, dnmp.isMutable());
			} catch(IllegalArgumentException e) {
				// column already exists, do nothing.
			}
		}
	}
	
	private void addTargetAttributeColumns(
		CyNetwork network,
		ResultSetMetaData metaData,
		DatabaseNetworkMappingParameters dnmp
		) throws SQLException{

		CyTable nodeAttributesTable = network.getDefaultNodeTable();
		
		// add edge attributes columns to the network
		for(int i = 1; i <= metaData.getColumnCount(); i++){	
			if(!dnmp.isTargetAttribute(i, metaData.getColumnLabel(i))) continue;
			
			Class<?> attributeType;
			try{
				attributeType = SQLTypeToCytoscapeType(metaData.getColumnType(i));
			} catch(Exception e) {
				throw new SQLException(e.getMessage() + " For target column '" + metaData.getColumnLabel(i) + "'");
			}

			try{
				nodeAttributesTable.createColumn(metaData.getColumnLabel(i), attributeType, dnmp.isMutable());
			} catch(IllegalArgumentException e) {
				// column already exists, do nothing.
			}
		}
	}
	
	
	private void addEdgeAttributeColumns(
		CyNetwork network,
		ResultSetMetaData metaData,
		DatabaseNetworkMappingParameters dnmp
		) throws SQLException{

		CyTable edgeAttributesTable = network.getDefaultEdgeTable();
		
		// add edge attributes columns to the network
		for(int i = 1; i <= metaData.getColumnCount(); i++){	
			if(!dnmp.isEdgeAttribute(i, metaData.getColumnLabel(i))) continue;		

			Class<?> attributeType;
			try{
				attributeType = SQLTypeToCytoscapeType(metaData.getColumnType(i));
			} catch(Exception e) {
				throw new SQLException(e.getMessage() + " For column '" + metaData.getColumnLabel(i) + "'");
			}

			try{
				edgeAttributesTable.createColumn(metaData.getColumnLabel(i), attributeType, dnmp.isMutable());
			} catch(IllegalArgumentException e) {
				// column already exists, do nothing.
				// is there a better way to test if a column exists?
			}

		}
	}

	
	public static Class<?> SQLTypeToCytoscapeType(int sqlType) throws SQLException{
		switch(sqlType){
		case Types.BIT:
			return Boolean.class;
		case Types.TINYINT:
		case Types.SMALLINT:
		case Types.INTEGER:
			return Integer.class;
		case Types.BIGINT:
			return Long.class;
		case Types.DECIMAL:
			System.out.println("Warning: converting JDBC type DECIMAL to java 'Double'");
			return Double.class;
		case Types.NUMERIC:
			System.out.println("Warning: converting JDBC type NUMERIC to java 'Double'");
			return Double.class;
		case Types.REAL:
		case Types.FLOAT:
			return Double.class; // should these be 'Float' for cytoscape?
		case Types.DOUBLE:
			return Double.class;
		case Types.CHAR:
		case Types.VARCHAR:
		case Types.LONGVARCHAR:
			return String.class;
		default:
			throw new SQLException("Unrecognized column type '" + sqlType +"'.");
		}
	}
	
	public static int CytoscapeTypeToSQLType(Class<?> cytoscapeType) throws Exception{
		if(cytoscapeType == Integer.class){ return Types.INTEGER; }
		else if(cytoscapeType == Long.class){ return Types.BIGINT; }
		else if(cytoscapeType == Double.class){ return Types.DOUBLE; }
		else if(cytoscapeType == String.class){ return Types.VARCHAR; }
		else if(cytoscapeType == Boolean.class){ return Types.BOOLEAN; }
		else {
			throw new Exception("Unrecognized column type: '" + cytoscapeType.getSimpleName() + "'.");
		}
	}
	
//	// Add values to list column
//	// If the list column already exists, this type should match.
//	private void addList(final CyNetwork network, final CyEdge edge, final ResultSet resultSet, final int i) throws SQLException {
//		
//		final String[] parts = (resultSet.getString(i).replace("\"", "")).split(dnmp.getListDelimiter());
//		final String colName = resultSet.getMetaData().getColumnLabel(i);
//		
//		Class<?> type = dnmp.getListAttributeType(i);
//		if(type == Boolean.class){
//			List<Boolean> list = network.getRow(edge).getList(colName, Boolean.class);
//			if (list == null) list = new ArrayList<Boolean>();
//			for (String listItem : parts) { list.add(new Boolean(listItem.trim())); }
//			network.getRow(edge).set(colName, list);
//		} else if(type == Integer.class) {
//			List<Integer> list = network.getRow(edge).getList(colName, Integer.class);
//			if (list == null) list = new ArrayList<Integer>();
//			for (String listItem : parts) { list.add(new Integer(listItem.trim())); }
//			network.getRow(edge).set(colName, list);
//		} else if(type == Double.class) {
//			List<Double> list = network.getRow(edge).getList(colName, Double.class);
//			if (list == null) list = new ArrayList<Double>();
//			for (String listItem : parts) { list.add(new Double(listItem.trim())); }
//			network.getRow(edge).set(colName, list);
//		} else {
//			List<String> list = network.getRow(edge).getList(colName, String.class);
//			if (list == null) list = new ArrayList<String>();
//			for (String listItem : parts) { list.add(listItem.trim()); }
//			network.getRow(edge).set(colName, list);				
//		}
//	}
	
	public void addSelectedNodes(CyNetwork network){
		cleanSelectedNodes();
		
		List<CyNode> selected_nodes = CyTableUtil.getNodesInState(network, "selected", true);
		try {
			dbQuery.copyToTempTable(network, selected_nodes, "selected_nodes");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
		
	public void cleanSelectedNodes(){
		dbQuery.deleteTempTable("selected_nodes");
	}
	
	public void close(){
		this.dbQuery.close();
	}
	
	public void cancel(){
		this.isCanceled = true;
	}

}
