package org.bkslab.CytoSQL.internal.tasks;

import org.bkslab.CytoSQL.internal.model.DBConnectionManager;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.task.AbstractNetworkTask;
import org.cytoscape.work.ProvidesTitle;
import org.cytoscape.work.TaskMonitor;

public class AddSelectedNodesTask extends AbstractNetworkTask {

	private final DBConnectionManager dbConnectionManager;
	
	private DatabaseNetworkParser parser;
	
	private boolean isCanceled;
	
	public static enum Mode {
		ADD_TO_DATABASE,
		CLEAN_UP_DATABASE
	}
	
	private final Mode mode;
	
	@ProvidesTitle	
	public String getTitle() {
		return "Copying selected nodes to database";
	}
	
	public AddSelectedNodesTask(
		CyNetwork network,
		final DBConnectionManager dbConnectionManager,
		final Mode mode){
		super(network);
		
		this.dbConnectionManager = dbConnectionManager;
		this.mode = mode;
	}
			
	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		taskMonitor.setProgress(0.0);
		
		parser = new DatabaseNetworkParser(dbConnectionManager, null);
		if(isCanceled){return;}
		
		if(mode == Mode.ADD_TO_DATABASE){
			taskMonitor.setTitle("Adding selected nodes to database.");
			parser.addSelectedNodes(network);
			taskMonitor.setProgress(1.0d);
		} else if(mode == Mode.CLEAN_UP_DATABASE){
			taskMonitor.setTitle("Adding selected nodes to database.");	
			parser.cleanSelectedNodes();
			taskMonitor.setProgress(1.0d);

		} else {
			throw new Exception("Unrecognized mode");
		}
	}
	
}
