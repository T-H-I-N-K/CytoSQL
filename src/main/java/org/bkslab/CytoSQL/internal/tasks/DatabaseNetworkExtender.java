package org.bkslab.CytoSQL.internal.tasks;

import java.util.List;

import org.bkslab.CytoSQL.internal.model.DBConnectionInfo;
import org.bkslab.CytoSQL.internal.model.DBConnectionManager;
import org.bkslab.CytoSQL.internal.model.DatabaseNetworkMappingParameters;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTableUtil;
import org.cytoscape.task.AbstractNetworkViewTask;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.ContainsTunables;
import org.cytoscape.work.ProvidesTitle;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;

public class DatabaseNetworkExtender extends AbstractNetworkViewTask {

	private final DBConnectionManager dbConnectionManager;
	
	// Database parameters
	private DBConnectionInfo dbConnectionInfo;
		
	@Tunable
	public DatabaseNetworkMappingParameters dnmp;

	
	private DatabaseNetworkParser parser;
	
	private boolean isCanceled;
	
	private final CyLayoutAlgorithmManager cyLayoutAlgorithmManager;
	
	@ProvidesTitle	
	public String getTitle() {
		return "Extend Network from Database Query";
	}
	
	
	public DatabaseNetworkExtender(
			CyNetworkView view,
			final DBConnectionManager dbConnectionManager,
			final CyLayoutAlgorithmManager cyLayoutAlgorithmManager) {
		super(view);
		
		this.dbConnectionManager = dbConnectionManager;

		this.cyLayoutAlgorithmManager = cyLayoutAlgorithmManager;
		
		dbConnectionInfo = dbConnectionManager.getDBConnectionInfo();

		dnmp = new DatabaseNetworkMappingParameters();
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		taskMonitor.setProgress(0.0);
		taskMonitor.setTitle("Extending network with SQL query.");

		CyNetwork network = view.getModel();

		try {
			parser = new DatabaseNetworkParser(dbConnectionInfo, dnmp);
			parser.parse(taskMonitor, network, dnmp.sqlQuery);
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
