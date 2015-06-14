package org.bkslab.cytosql.internal.tasks;

import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;

import org.bkslab.cytosql.internal.model.QueryMode;
import org.bkslab.cytosql.internal.tasks.CytoSQLTask;

public class CytoSQLTaskFactory extends AbstractTaskFactory {

	private QueryMode query_mode;
	
	public CytoSQLTaskFactory(
		QueryMode query_mode
	){
		this.query_mode = query_mode;
	}
	
	public TaskIterator createTaskIterator(){
		return new TaskIterator(
			new CytoSQLTask(query_mode)
		);
	}
}