package org.bkslab.CytoSQL;

import java.sql.DriverManager;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import org.bkslab.CytoSQL.internal.model.DBConnectionInfo;
import org.bkslab.CytoSQL.internal.model.DBConnectionManager;
import org.bkslab.CytoSQL.internal.model.DBQuery;
import org.bkslab.CytoSQL.internal.model.DatabaseNetworkMappingParameters;
import org.bkslab.CytoSQL.internal.tasks.DatabaseNetworkParser;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.NetworkTestSupport;
import org.cytoscape.work.TaskMonitor;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DatabaseNetworkParserTest {

	private NetworkTestSupport support = new NetworkTestSupport();
	
	@Test
	public void parseTest(){
		
		TaskMonitor taskMonitor = mock(TaskMonitor.class);
		
		
		final String url = DatabaseHelper.CreateSimpleNetwork();
		final String sqlQuery = "SELECT * FROM NETWORK;";
		
		DBQuery dbQuery;
		try {
			dbQuery = new DBQuery(DriverManager.getConnection(url, "", ""), "");

			DBConnectionManager dbConnectionManager = mock(DBConnectionManager.class);
			when(dbConnectionManager.getDBQuery()).thenReturn(dbQuery);
		
			DatabaseNetworkMappingParameters dnmp = new DatabaseNetworkMappingParameters(
					sqlQuery, "", 1, 2, -1, "pp", "", true, false);
			DatabaseNetworkParser parser = new DatabaseNetworkParser(dbConnectionManager, dnmp);
			CyNetwork network = support.getNetwork();

			parser.parse(taskMonitor, network, sqlQuery);


			int nodeCount = network.getNodeCount();
			int edgeCount = network.getEdgeCount();
			assertEquals(nodeCount, 4);
			assertEquals(edgeCount, 5);
			
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			fail();
		} 
		

	}
	
	
	@Test
	public void parseExtendPostgresTest() {
		TaskMonitor taskMonitor = mock(TaskMonitor.class);
		
		CyNetwork network = support.getNetwork();
		CyNode node1 = network.addNode();
		network.getRow(node1).set(CyNetwork.NAME, "b");
		network.getRow(node1).set(CyNetwork.SELECTED, true);
		
		
		final String sqlQuery = "SELECT name AS source, name AS target FROM selected_nodes;";		
		DatabaseNetworkMappingParameters dnmp = new DatabaseNetworkMappingParameters(sqlQuery, "", 1, 2, -1, "pp", "name", true, false);
		
		try {
			DBQuery dbQuery = new DBQuery(
				DriverManager.getConnection("jdbc:postgresql://localhost/momeara", "momeara", "che8ga5R"), "");

			DBConnectionManager dbConnectionManager = mock(DBConnectionManager.class);
			when(dbConnectionManager.getDBQuery()).thenReturn(dbQuery);
				
			DatabaseNetworkParser parser = new DatabaseNetworkParser(dbConnectionManager, dnmp);
			parser.addSelectedNodes(network);
			parser.parse(taskMonitor, network, sqlQuery);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			fail();
		}
		int nodeCount = network.getNodeCount();
		int edgeCount = network.getEdgeCount();
		assertEquals(nodeCount, 1);
		assertEquals(edgeCount, 1);
	}
	
	
	
	@Test
	public void parseExtendTest(){
		TaskMonitor taskMonitor = mock(TaskMonitor.class);
		
		CyNetwork network = support.getNetwork();
		CyNode node1 = network.addNode();
		network.getRow(node1).set(CyNetwork.NAME, "b");
		network.getRow(node1).set(CyNetwork.SELECTED, true);
		
		final String url = DatabaseHelper.CreateSimpleNetwork();
		
		final String sqlQuery = "SELECT network.source, network.target FROM selected_nodes LEFT JOIN network ON selected_nodes.name = network.source;";		
		DatabaseNetworkMappingParameters dnmp = new DatabaseNetworkMappingParameters(sqlQuery, "", 1, 2, -1, "pp", "name", true, false);
		
		try {
			DBQuery dbQuery = new DBQuery(
				DriverManager.getConnection(url, "", ""), "");

			DBConnectionManager dbConnectionManager = mock(DBConnectionManager.class);
			when(dbConnectionManager.getDBQuery()).thenReturn(dbQuery);
			
			
			DatabaseNetworkParser parser = new DatabaseNetworkParser(dbConnectionManager, dnmp);
			parser.addSelectedNodes(network);
			parser.parse(taskMonitor, network, sqlQuery);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			fail();
		}
		int nodeCount = network.getNodeCount();
		int edgeCount = network.getEdgeCount();
		assertEquals(nodeCount, 2);
		assertEquals(edgeCount, 1);
	}
	
	
	
}
