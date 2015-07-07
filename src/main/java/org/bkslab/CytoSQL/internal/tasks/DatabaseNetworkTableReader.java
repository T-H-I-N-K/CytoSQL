package org.bkslab.CytoSQL.internal.tasks;

import java.util.Map;

import org.bkslab.CytoSQL.internal.model.DBConnectionInfo;
import org.bkslab.CytoSQL.internal.model.DatabaseNetworkMappingParameters;
import org.bkslab.CytoSQL.internal.model.PropertiesSaver;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyTableFactory;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.session.CyNetworkNaming;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ContainsTunables;
import org.cytoscape.work.ProvidesTitle;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;


public class DatabaseNetworkTableReader extends AbstractTask {
	
	PropertiesSaver propertiesSaver;
	
	// Database parameters
	public DBConnectionInfo dbConnectionInfo;
	
	@Tunable
	public DatabaseNetworkMappingParameters dnmp;

	
	private DatabaseNetworkParser parser;
	
	private boolean isCanceled;

	private CyNetworkManager cyNetworkManager;
	private CyNetworkFactory cyNetworkFactory;
	private CyNetworkNaming cyNetworkNaming;
	
	@ProvidesTitle	
	public String getTitle() {
		return "Create Network from Database Query";
	}
	
	public DatabaseNetworkTableReader(
		final CyTableFactory cyTableFactory,
		final CyTableManager cyTableManager,
		final CyNetworkManager cyNetworkManager,
		final CyNetworkFactory cyNetworkFactory,
		final CyNetworkNaming cyNetworkNaming) {
		
		this.cyNetworkManager = cyNetworkManager;
		this.cyNetworkFactory = cyNetworkFactory;
		this.cyNetworkNaming = cyNetworkNaming;

		// initialized with a Tunables
		
		propertiesSaver = new PropertiesSaver(cyTableFactory, cyTableManager);
		Map<String, Object>  dbConnectionInfoProperties = propertiesSaver.getProperties(
				DBConnectionInfo.SAVER_TABLE,
				DBConnectionInfo.SAVER_TABLE_DEFAULT_KEY);
		
		if(dbConnectionInfoProperties == null){
			System.out.println("Database Connection Info could not be found.");
			dbConnectionInfo = new DBConnectionInfo();
		} else {
			dbConnectionInfo = new DBConnectionInfo(dbConnectionInfoProperties);
		}
		
		dnmp = new DatabaseNetworkMappingParameters();

	}
	

	public void run(final TaskMonitor taskMonitor) {
		final String suggestedName = cyNetworkNaming.getSuggestedNetworkTitle(dnmp.newNetworkName);
		taskMonitor.setTitle("Creating network '" + suggestedName + "' from SQL query.");
		taskMonitor.setProgress(0.0);
		
		CyNetwork network = cyNetworkFactory.createNetwork();
		network.getRow(network).set(CyNetwork.NAME, suggestedName);
		
		taskMonitor.setProgress(0.2);

		parser = new DatabaseNetworkParser(dbConnectionInfo, dnmp);

		try {
			parser.parse(taskMonitor, network, dnmp.sqlQuery);
		} catch(Exception e){
			System.out.println("Failed to parse SQL query into network:\n" + e.getMessage());
			network.dispose();
			return;
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
		if(parser != null){
			parser.cancel();
		}
	}

}
