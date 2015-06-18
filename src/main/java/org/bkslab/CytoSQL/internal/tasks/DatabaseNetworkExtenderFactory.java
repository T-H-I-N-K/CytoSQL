package org.bkslab.CytoSQL.internal.tasks;



import org.cytoscape.task.AbstractNetworkViewTaskFactory;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.TaskIterator;







public class DatabaseNetworkExtenderFactory extends AbstractNetworkViewTaskFactory {

	
	public DatabaseNetworkExtenderFactory() {}
	
	@Override
	public TaskIterator createTaskIterator(CyNetworkView view) {
		return new TaskIterator(new DatabaseNetworkExtender(view));
	}
}


	

