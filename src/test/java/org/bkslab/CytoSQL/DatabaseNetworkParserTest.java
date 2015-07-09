package org.bkslab.CytoSQL;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import org.bkslab.CytoSQL.internal.model.DBConnectionInfo;
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


public class DatabaseNetworkParserTest {

	private NetworkTestSupport support = new NetworkTestSupport();
	
	@Test
	public void parseTest(){
		final String url = DatabaseHelper.CreateSimpleNetwork();
		final String sqlQuery = "SELECT * FROM NETWORK;";
		
		
		DBConnectionInfo connInfo = new DBConnectionInfo(
			"default", "org.sqlite.JDBC", url, "", "", "", "");
		DatabaseNetworkMappingParameters dnmp = new DatabaseNetworkMappingParameters(
			sqlQuery, "", 1, 2, -1, "pp", "", true, false);
		DatabaseNetworkParser parser = new DatabaseNetworkParser(connInfo, dnmp);
		CyNetwork network = support.getNetwork();
		TaskMonitor taskMonitor = mock(TaskMonitor.class);
		try {
			parser.parse(taskMonitor, network, sqlQuery);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			fail();
		}
		int nodeCount = network.getNodeCount();
		int edgeCount = network.getEdgeCount();
		assertEquals(nodeCount, 4);
		assertEquals(edgeCount, 5);
	}
	
	
	@Test
	public void parseExtendPostgresTest() {
		TaskMonitor taskMonitor = mock(TaskMonitor.class);
		
		CyNetwork network = support.getNetwork();
		CyNode node1 = network.addNode();
		network.getRow(node1).set(CyNetwork.NAME, "b");
		network.getRow(node1).set(CyNetwork.SELECTED, true);
		
		
		
		DBConnectionInfo connInfo = new DBConnectionInfo("default", "org.postgresql.Driver", "jdbc:postgresql://localhost", "momeara", "che8ga5R", "momeara", "sea_chembl");		
		final String sqlQuery = "SELECT name AS source, name AS target FROM selected_nodes;";		
		DatabaseNetworkMappingParameters dnmp = new DatabaseNetworkMappingParameters(sqlQuery, "", 1, 2, -1, "pp", "name", true, false);
		
		try {
			DatabaseNetworkParser parser = new DatabaseNetworkParser(connInfo, dnmp);
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
		DBConnectionInfo connInfo = new DBConnectionInfo("default", "org.sqlite.JDBC", url, "", "", "", "");
		DatabaseNetworkMappingParameters dnmp = new DatabaseNetworkMappingParameters(sqlQuery, "", 1, 2, -1, "pp", "name", true, false);
		
		try {
			DatabaseNetworkParser parser = new DatabaseNetworkParser(connInfo, dnmp);
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
