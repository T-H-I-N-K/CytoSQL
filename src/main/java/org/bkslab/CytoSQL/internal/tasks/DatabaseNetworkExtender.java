package org.bkslab.CytoSQL.internal.tasks;

import java.util.List;

import org.bkslab.CytoSQL.internal.model.DBConnectionInfo;
import org.bkslab.CytoSQL.internal.model.DatabaseNetworkMappingParameters;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTableUtil;
import org.cytoscape.task.AbstractNetworkViewTask;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.ContainsTunables;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;

public class DatabaseNetworkExtender extends AbstractNetworkViewTask {

	// Database parameters
	@ContainsTunables
	public DBConnectionInfo dbConnectionInfo;
		
	@ContainsTunables
	public DatabaseNetworkMappingParameters dnmp;

	
	private DatabaseNetworkParser parser;
	
	private boolean isCanceled;
	
	
	public DatabaseNetworkExtender(CyNetworkView view) {
		super(view);
		
		dbConnectionInfo = new DBConnectionInfo();
		dnmp = new DatabaseNetworkMappingParameters();
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		taskMonitor.setTitle("Extending network with SQL query.");
		taskMonitor.setProgress(0.0);
		CyNetwork network = view.getModel();

		try {
			parser = new DatabaseNetworkParser(dbConnectionInfo, dnmp);
			parser.addSelectedNodes(network);
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
