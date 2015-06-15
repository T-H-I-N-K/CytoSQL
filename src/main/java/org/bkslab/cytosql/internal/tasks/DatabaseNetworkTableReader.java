package org.bkslab.cytosql.internal.tasks;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.io.InputStream;
import java.io.IOException;
import java.io.ByteArrayInputStream;

import org.bkslab.cytosql.internal.model.DBConnectionInfo;
import org.bkslab.cytosql.internal.model.DBQuery;
import org.bkslab.cytosql.internal.model.DatabaseNetworkMappingParameters;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.io.read.AbstractCyNetworkReader;
import org.cytoscape.io.read.CyNetworkReader;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.model.subnetwork.CySubNetwork;
import org.cytoscape.session.CyNetworkNaming;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ContainsTunables;
import org.cytoscape.work.ProvidesTitle;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.util.ListSingleSelection;

public class DatabaseNetworkTableReader extends AbstractCyNetworkReader {
	
	// Database parameters
	@ContainsTunables
	DBConnectionInfo dbConnectionInfo;
	
	// SQL query
	@Tunable(description="SQL Query")
	String sqlQuery;

	@ContainsTunables
	DatabaseNetworkMappingParameters dnmp;
	
	@Tunable(description = "Name of new network:")
	String name;
	
	private boolean isCanceled;

	private final String networkCollectionName;
	
	private CyRootNetwork rootNetwork;
	private CyNetwork network;
	private final CyNetworkNaming networkNaming; 
		
	// Data to keep track of nodes as they are created
	private Map<Object, CyNode> nMap;
	
	@ProvidesTitle
	public String getTitle() {
		return "Load Network from Database Query";
	}
	
	public DatabaseNetworkTableReader(
		final String networkCollectionName,
		final CyApplicationManager cyApplicationManager,
		final CyNetworkFactory cyNetworkFactory,
		final CyNetworkManager cyNetworkManager,
		final CyRootNetworkManager cyRootNetworkManager) {

		super(
			(InputStream) new ByteArrayInputStream(null),
			cyApplicationManager.getDefaultNetworkViewRenderer().getNetworkViewFactory(),
			cyNetworkFactory, cyNetworkManager, cyRootNetworkManager);
		
		this.cyApplicationManager = cyApplicationManager;
		
		this.networkCollectionName = networkCollectionName;

		this.networkNaming = networkNaming;

		// initialized with a Tunables
		dbConnectionInfo = new DBConnectionInfo();
		dnmp = new DatabaseNetworkMappingParameters();

	}
	
	
	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		taskMonitor.setTitle("Creating table '" + this.name + "' from SQL query.");
		taskMonitor.setProgress(0.0);

		CySubNetwork network = createSubNetwork();
		
		DatabaseNetworkParser parser = new DatabaseNetworkParser(
				nMap,
				dbConnectionInfo,
				dnmp,
				sqlQuery);
		parser.parse(taskMonitor, network);
		
		taskMonitor.setProgress(1.0);
				
	}
	
	private CySubNetwork createSubNetwork(){
		if(networkCollectionName != null) {
			ListSingleSelection<String> rootList = getRootNetworkList();
			if(rootList.getPossibleValues().contains(networkCollectionName)) {
				// Collection already exists.
				rootList.setSelectedValue(networkCollectionName);
			}
		}
		
		CyRootNetwork rootNetwork = getRootNetwork();

		// Select Network Collection
		// 1. Check from Tunable
		// 2. If not available, use optional parameter
		CySubNetwork subNetwork;
		if (rootNetwork != null) {
			// Root network exists
			subNetwork = rootNetwork.addSubNetwork();			
		} else {
			// Need to create new network with new root.
			subNetwork = (CySubNetwork) cyNetworkFactory.createNetwork();
		}
		return subNetwork;
	}
	
	
	@Override
	public CyNetwork[] getNetworks(){
		final CyNetwork[] result = new CyNetwork[1];
		result[0] = network;
		return result;
	}
	
	@Override
	public CyNetworkView buildCyNetworkView(final CyNetwork network){
		final CyNetworkView view = this.cyNetworkViewFactory.createNetworkView(network);
		return view;
	}
	
	public void setRootNetwork(CyRootNetwork rootNetwork) {
		this.rootNetwork = rootNetwork;
	}
	
	public void setNodeMap(Map<Object, CyNode> nMap){
		this.nMap = nMap;
	}
}
