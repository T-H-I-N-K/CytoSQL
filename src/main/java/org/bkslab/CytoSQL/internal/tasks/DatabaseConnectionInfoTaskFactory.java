package org.bkslab.CytoSQL.internal.tasks;

import org.bkslab.CytoSQL.internal.model.DBConnectionManager;
import org.cytoscape.model.CyTableFactory;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;

public class DatabaseConnectionInfoTaskFactory extends AbstractTaskFactory {

	DBConnectionManager dbConnectionManager;
	
	public DatabaseConnectionInfoTaskFactory(
		final DBConnectionManager dbConnectionManager){
		this.dbConnectionManager = dbConnectionManager; 
	}
	
	@Override
	public TaskIterator createTaskIterator() {
		return new TaskIterator(
			new DatabaseConnectionInfoTask(dbConnectionManager));

	}


	
	
	
}
