package org.bkslab.CytoSQL.internal.tasks;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.io.IOException;

import org.bkslab.CytoSQL.internal.model.DBConnectionInfo;
import org.bkslab.CytoSQL.internal.model.DBQuery;
import org.bkslab.CytoSQL.internal.model.DatabaseNetworkMappingParameters;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableUtil;
import org.cytoscape.model.subnetwork.CySubNetwork;
import org.cytoscape.work.TaskMonitor;

public class DatabaseNetworkParser {
	
	// Data to keep track of nodes as they are created
	private Map<Object, CyNode> nMap;
	
	// Database parameters
	private final DBConnectionInfo dbConnectionInfo;
	private final DatabaseNetworkMappingParameters dnmp;
	
	private DBQuery dbQuery;
	
	private boolean isCanceled;
	
	public DatabaseNetworkParser(
			DBConnectionInfo dbConnectionInfo,
			DatabaseNetworkMappingParameters dnmp){

		this.dbConnectionInfo = dbConnectionInfo;
		this.dnmp = dnmp;
		try {
			dbQuery = new DBQuery(dbConnectionInfo);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	public void parse(TaskMonitor taskMonitor, CyNetwork network, String sqlQuery) throws Exception {
		taskMonitor.setTitle("Creating table from SQL query.");
		taskMonitor.setProgress(0.0);

		prepareNodeMap(network);
		
		ResultSet resultSet = dbQuery.getResults(sqlQuery);
		
		validateParameters(network, resultSet);
		
		populateNetwork(network, resultSet);
		
		resultSet.close();
		
		taskMonitor.setProgress(1.0);
	}
	
	private void prepareNodeMap(CyNetwork network){
		nMap = new HashMap<Object, CyNode>(10000);
		final List<CyNode> nodes = network.getNodeList();
		
		for(final CyNode node : nodes){
			final Object keyValue = network.getRow(node).getRaw(this.dnmp.getNodeJoinColumnName());
			if(keyValue != null){
				nMap.put(keyValue, node);
			}
		}
	}
	
	
	private void validateParameters(CyNetwork network, ResultSet resultSet) throws Exception {
		if (dbConnectionInfo == null)
			throw new NullPointerException("No database connection specified.");

		if(dnmp == null){
			throw new NullPointerException("No DatabaseNetworkMappingParameters specified.");
		}
		
		dnmp.validate(network, resultSet);
	}
	

	
	private void populateNetwork(CyNetwork network, ResultSet resultSet) throws SQLException, IOException{
		
		ResultSetMetaData metaData = resultSet.getMetaData();
				
		addEdgeAttributeColumns(network, metaData);
		
		while(resultSet.next()){
			if(isCanceled){
				System.out.println("Loading canceled.");
				resultSet.close();
				network.dispose();
				network = null;
				throw new IOException("Network loading process canceled by user.");
			}
			
			// add the nodes
			final CyNode source = createNode(network, resultSet, dnmp.getSourceIndex());
			final CyNode target = createNode(network, resultSet, dnmp.getTargetIndex());
			
			// Single column nodes list.  Just add nodes.
			if(source == null || target == null) continue;
			

			//add the edge
			CyEdge edge = network.addEdge(source, target, dnmp.isDirected());

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
				if(!dnmp.isEdgeAttribute(i)) continue;
				
				Object value = resultSet.getObject(i);
				if(resultSet.wasNull()) continue;
				
//				if(dnmp.isListAttribute(i)){
//					addList(network, edge, resultSet, i);
//				} else {
					network.getRow(edge).set(metaData.getColumnLabel(i), value);
//				}
			}
				
		}
	}
	
	private void addEdgeAttributeColumns(CyNetwork network, ResultSetMetaData metaData) throws SQLException{

		CyTable edgeAttributesTable = network.getDefaultEdgeTable();
		
		// add edge attributes columns to the network
		for(int i = 1; i <= metaData.getColumnCount(); i++){	
			if(!dnmp.isEdgeAttribute(i)) continue;
			
//			if(!dnmp.isListAttribute(i)){
				Class<?> attributeType;
				try{
					attributeType = SQLTypeToCytoscapeType(metaData.getColumnType(i));
				} catch(Exception e) {
					throw new SQLException(e.getMessage() + " For column '" + metaData.getColumnLabel(i) + "'");
				}

				edgeAttributesTable.createListColumn(metaData.getColumnLabel(i), attributeType, dnmp.isMutable());
//			} else {
//				Class<?> listAttributeType;
//				try{
//					listAttributeType = dnmp.getListAttributeType(i);
//				} catch(Exception e) {
//					throw new SQLException(e.getMessage() + " For list column '" + metaData.getColumnLabel(i) + "'");
//				}
//
//				edgeAttributesTable.createColumn(metaData.getColumnLabel(i), listAttributeType, dnmp.isMutable());
//			}	
		}
	}
	
	private CyNode createNode(CyNetwork network, ResultSet resultSet, int nodeIndex) throws SQLException{
		if(nodeIndex == -1) return null;
		
		String nodeName = resultSet.getString(nodeIndex);
		if(resultSet.wasNull()) return null;
		
		if (this.nMap.get(nodeName) == null){
			// node does not exist yet, create it
			CyNode node = network.addNode();
			network.getRow(node).set(CyNetwork.NAME, nodeName);
			this.nMap.put(nodeName, network.getNode(node.getSUID()));
			return node;
		}
		else {// already existed in parent network
			CyNode parentNode = this.nMap.get(nodeName);
			CySubNetwork subnet = (CySubNetwork) network;
			subnet.addNode(parentNode);
			CyNode existingNode = subnet.getNode(parentNode.getSUID());
			return existingNode;
		}
	}
	
	public static Class<?> SQLTypeToCytoscapeType(int sqlType) throws SQLException{
		switch(sqlType){
		case Types.INTEGER:
			return Integer.class;
		case Types.BIGINT:
			return Long.class;
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
