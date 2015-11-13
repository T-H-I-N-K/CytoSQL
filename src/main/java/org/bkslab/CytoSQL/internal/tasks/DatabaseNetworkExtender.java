package org.bkslab.CytoSQL.internal.tasks;

import org.bkslab.CytoSQL.internal.model.DBConnectionManager;
import org.bkslab.CytoSQL.internal.model.DatabaseNetworkMappingParameters;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ProvidesTitle;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;

public class DatabaseNetworkExtender extends AbstractTask {

	private final DBConnectionManager dbConnectionManager;
	private CyEventHelper eventHelper;
	private VisualMappingManager visualMappingManager;
	private CyNetworkViewManager networkViewManager;
	private CyNetwork network;
		
	@Tunable
	public DatabaseNetworkMappingParameters dnmp;

	
	private DatabaseNetworkParser parser;
	
	private boolean isCanceled;
	
	@ProvidesTitle	
	public String getTitle() {
		return "Extend Network from Database Query";
	}
	
	
	public DatabaseNetworkExtender(
			final DBConnectionManager dbConnectionManager,
			final CyEventHelper eventHelper,
			final CyNetworkViewManager networkViewManager,
			final VisualMappingManager visualMappingManager,
			CyNetwork network
	) {
		this.network = network;
		this.eventHelper = eventHelper;
		this.networkViewManager = networkViewManager;
		this.visualMappingManager = visualMappingManager;
		
		this.dbConnectionManager = dbConnectionManager;

		dnmp = new DatabaseNetworkMappingParameters();
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		taskMonitor.setProgress(0.0);
		taskMonitor.setTitle("Extending network with SQL query.");

		try {
			parser = new DatabaseNetworkParser(dbConnectionManager, dnmp);
			parser.parseNetworkExtender(
				taskMonitor,
				this.network,
				this.eventHelper,
				this.networkViewManager,
				this.visualMappingManager,
				dnmp.sqlQuery);
		} catch(Exception e){
			System.out.println("Failed to parse SQL query into network:\n" + e.getMessage());
			return;
		}
	
		taskMonitor.setProgress(.9);
		if(isCanceled){
			return;
		}
		
		taskMonitor.setProgress(1.0);
		
		
	}

}
