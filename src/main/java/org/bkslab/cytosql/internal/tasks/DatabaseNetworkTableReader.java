package org.bkslab.cytosql.internal.tasks;

import org.bkslab.cytosql.internal.model.DBConnectionInfo;
import org.bkslab.cytosql.internal.model.DatabaseNetworkMappingParameters;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.session.CyNetworkNaming;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ContainsTunables;
import org.cytoscape.work.ProvidesTitle;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;

public class DatabaseNetworkTableReader extends AbstractTask {
	
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

	
	private final DatabaseNetworkParser parser;
	
	private boolean isCanceled;

	private CyNetworkManager cyNetworkManager;
	private CyNetworkFactory cyNetworkFactory;
	private CyNetworkNaming cyNetworkNaming;
	
	@ProvidesTitle
	public String getTitle() {
		return "Create Network from Database Query";
	}
	
	public DatabaseNetworkTableReader(
		final CyNetworkManager cyNetworkManager,
		final CyNetworkFactory cyNetworkFactory,
		final CyNetworkNaming cyNetworkNaming) {
		
		this.cyNetworkManager = cyNetworkManager;
		this.cyNetworkFactory = cyNetworkFactory;
		this.cyNetworkNaming = cyNetworkNaming;

		// initialized with a Tunables
		dbConnectionInfo = new DBConnectionInfo();
		dnmp = new DatabaseNetworkMappingParameters();

		parser = new DatabaseNetworkParser(
				dbConnectionInfo,
				dnmp,
				sqlQuery);
	}
	
	
	public void run(final TaskMonitor taskMonitor) {
		final String suggestedName = cyNetworkNaming.getSuggestedNetworkTitle(this.name);
		taskMonitor.setTitle("Creating network '" + suggestedName + "' from SQL query.");
		taskMonitor.setProgress(0.0);
		
		CyNetwork network = cyNetworkFactory.createNetwork();
		network.getRow(network).set(CyNetwork.NAME, suggestedName);
		
		taskMonitor.setProgress(0.2);
		
		try {
			parser.parse(taskMonitor, network);
		} catch(Exception e){
			System.out.println("Failed to parse SQL query into network:\n" + e.getMessage());
			network.dispose();
		}
	
		taskMonitor.setProgress(.9);
		if(isCanceled){
			network.dispose();
			return;
		}
		cyNetworkManager.addNetwork(network);
		
		taskMonitor.setProgress(1.0);
				
	}
	
	@Override
	public void cancel(){
		this.isCanceled = true;
		parser.cancel();
	}

}
