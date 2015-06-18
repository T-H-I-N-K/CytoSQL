package org.bkslab.CytoSQL.internal.tasks;



import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.session.CyNetworkNaming;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;




public class DatabaseNetworkTableReaderFactory extends AbstractTaskFactory {


	private final CyNetworkFactory cyNetworkFactory;
	private final CyNetworkManager cyNetworkManager;
	private final CyNetworkNaming cyNetworkNaming;
	
	public DatabaseNetworkTableReaderFactory(
			final CyNetworkManager cyNetworkManager,
			final CyNetworkFactory cyNetworkFactory,
			final CyNetworkNaming cyNetworkNaming
		) {
		this.cyNetworkFactory = cyNetworkFactory;
		this.cyNetworkManager = cyNetworkManager;
		this.cyNetworkNaming = cyNetworkNaming;
	}
	
	@Override
	public TaskIterator createTaskIterator() {
		return new TaskIterator(new DatabaseNetworkTableReader(
				cyNetworkManager, cyNetworkFactory, cyNetworkNaming));
	}
}


