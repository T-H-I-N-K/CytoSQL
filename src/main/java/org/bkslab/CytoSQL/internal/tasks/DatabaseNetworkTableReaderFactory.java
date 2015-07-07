package org.bkslab.CytoSQL.internal.tasks;



import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyTableFactory;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.session.CyNetworkNaming;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;




public class DatabaseNetworkTableReaderFactory extends AbstractTaskFactory {


	private final CyTableFactory cyTableFactory;
	private final CyTableManager cyTableManager;
	private final CyNetworkFactory cyNetworkFactory;
	private final CyNetworkManager cyNetworkManager;
	private final CyNetworkNaming cyNetworkNaming;
		
	public DatabaseNetworkTableReaderFactory(
			final CyTableFactory cyTableFactory,
			final CyTableManager cyTableManager,
			final CyNetworkManager cyNetworkManager,
			final CyNetworkFactory cyNetworkFactory,
			final CyNetworkNaming cyNetworkNaming
		) {
		this.cyTableFactory = cyTableFactory;
		this.cyTableManager = cyTableManager;
		this.cyNetworkFactory = cyNetworkFactory;
		this.cyNetworkManager = cyNetworkManager;
		this.cyNetworkNaming = cyNetworkNaming;
	}
	
	@Override
	public TaskIterator createTaskIterator() {
		return new TaskIterator(new DatabaseNetworkTableReader(
				cyTableFactory, cyTableManager, cyNetworkManager, cyNetworkFactory, cyNetworkNaming));
	}
}


