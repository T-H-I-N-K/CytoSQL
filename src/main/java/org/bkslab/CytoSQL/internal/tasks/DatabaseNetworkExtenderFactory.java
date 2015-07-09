package org.bkslab.CytoSQL.internal.tasks;



import org.bkslab.CytoSQL.internal.model.DBConnectionManager;
import org.cytoscape.task.AbstractNetworkViewTaskFactory;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.TaskIterator;



public class DatabaseNetworkExtenderFactory extends AbstractNetworkViewTaskFactory {

	private final DBConnectionManager dbConnectionManager;
	private final CyLayoutAlgorithmManager cyLayoutAlgorithmManager;
	
	
	public DatabaseNetworkExtenderFactory(
		final DBConnectionManager dbConnectionManager,
		CyLayoutAlgorithmManager cyLayoutAlgorithmManager) {
		
		this.dbConnectionManager = dbConnectionManager;
		this.cyLayoutAlgorithmManager = cyLayoutAlgorithmManager;
	}
	
	@Override
	public TaskIterator createTaskIterator(CyNetworkView view) {
		return new TaskIterator(
			new AddSelectedNodesTask(view.getModel(), dbConnectionManager),
			new DatabaseNetworkExtender(view, dbConnectionManager, cyLayoutAlgorithmManager));
	}
}


	

