package org.bkslab.cytosql.internal;

import org.bkslab.cytosql.internal.CytoSQLTask;

import org.cytoscape.work.TaskIterator;
import org.cytoscape.task.AbstractNetworkViewTaskFactory;
import org.cytoscape.view.model.CyNetworkView;

public class CytoSQLTaskFactory extends AbstractNetworkViewTaskFactory {

	public TaskIterator createTaskIterator(CyNetworkView networkView){
		return new TaskIterator(new CytoSQLTask(networkView) );
	}
}