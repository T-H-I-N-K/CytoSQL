package org.bkslab.CytoSQL.internal.tasks;

import org.bkslab.CytoSQL.internal.model.DBConnectionInfo;
import org.bkslab.CytoSQL.internal.model.DBConnectionManager;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.task.AbstractNetworkTask;
import org.cytoscape.work.ProvidesTitle;
import org.cytoscape.work.TaskMonitor;

public class AddSelectedNodesTask extends AbstractNetworkTask {

	private final DBConnectionManager dbConnectionManager;
	
	private DatabaseNetworkParser parser;
	
	private boolean isCanceled;
	
	@ProvidesTitle	
	public String getTitle() {
		return "Copying selected nodes to database";
	}
	
	public AddSelectedNodesTask(
		CyNetwork network,
		final DBConnectionManager dbConnectionManager){
		super(network);
		
		this.dbConnectionManager = dbConnectionManager;
	}
			
	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		taskMonitor.setProgress(0.0);
		taskMonitor.setTitle("COpying selected nodes to database.");
		
		parser = new DatabaseNetworkParser(dbConnectionManager, null);
		
		if(isCanceled){
			return;
		}
		
		parser.addSelectedNodes(network);
		taskMonitor.setProgress(1.0d);
	}
	
}
