package org.bkslab.CytoSQL.internal.tasks;

import org.cytoscape.model.CyTableFactory;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;

public class DatabaseConnectionInfoTaskFactory extends AbstractTaskFactory {

	CyTableFactory cyTableFactory;
	CyTableManager cyTableManager;
	
	
	public DatabaseConnectionInfoTaskFactory(
		CyTableFactory cyTableFactory,
		CyTableManager cyTableManager){
		this.cyTableFactory = cyTableFactory;
		this.cyTableManager = cyTableManager;
	}
	
	@Override
	public TaskIterator createTaskIterator() {
		return new TaskIterator(
			new DatabaseConnectionInfoTask(cyTableFactory, cyTableManager));

	}


	
	
	
}
