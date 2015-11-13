package org.bkslab.CytoSQL.internal.tasks;



import org.bkslab.CytoSQL.internal.model.DBConnectionManager;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.task.AbstractNetworkViewTaskFactory;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;



public class DatabaseNetworkExtenderFactory extends AbstractTaskFactory {

	private final DBConnectionManager dbConnectionManager;
	private final CyEventHelper eventHelper;
	private final CyNetworkViewManager networkViewManager;
	private final VisualMappingManager visualMappingManager;
	private final CyApplicationManager applicationManager;
	
	
	public DatabaseNetworkExtenderFactory(
		final DBConnectionManager dbConnectionManager,
		final CyEventHelper eventHelper,
		final CyNetworkViewManager networkViewManager,
		final CyApplicationManager applicationManager,
		final VisualMappingManager visualMappingManager
	) {
		
		this.dbConnectionManager = dbConnectionManager;
		this.eventHelper = eventHelper;
		this.networkViewManager = networkViewManager;
		this.applicationManager = applicationManager;
		this.visualMappingManager = visualMappingManager;
	}
	
	@Override
	public TaskIterator createTaskIterator() {
		return new TaskIterator(
			
			new AddSelectedNodesTask(
					applicationManager.getCurrentNetwork(),
					dbConnectionManager,
					AddSelectedNodesTask.Mode.ADD_TO_DATABASE),
				new DatabaseNetworkExtender(
					dbConnectionManager,
					this.eventHelper,
					this.networkViewManager,
					this.visualMappingManager,
					applicationManager.getCurrentNetwork()),
				new AddSelectedNodesTask(
					applicationManager.getCurrentNetwork(),
					dbConnectionManager,
					AddSelectedNodesTask.Mode.CLEAN_UP_DATABASE));
	}
}


	

