package org.bkslab.CytoSQL.internal.tasks;



import org.bkslab.CytoSQL.internal.model.DBConnectionManager;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyTableFactory;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.session.CyNetworkNaming;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;




public class DatabaseNetworkTableReaderFactory extends AbstractTaskFactory {


	private final DBConnectionManager dbConnectionManager;
	private final CyNetworkFactory cyNetworkFactory;
	private final CyNetworkManager cyNetworkManager;
	private final CyNetworkNaming cyNetworkNaming;
	private final CyNetworkViewManager cyNetworkViewManager;
	private final CyNetworkViewFactory cyNetworkViewFactory;
	private final CyLayoutAlgorithmManager cyLayoutAlgorithmManager;
	private final CyEventHelper cyEventHelper;
	private final VisualMappingManager cyVisualMappingManager;
		
	public DatabaseNetworkTableReaderFactory(
			final DBConnectionManager dbConnectionManager,
			final CyNetworkManager cyNetworkManager,
			final CyNetworkFactory cyNetworkFactory,
			final CyNetworkNaming cyNetworkNaming,
			final CyNetworkViewManager cyNetworkViewManager,
			final CyNetworkViewFactory cyNetworkViewFactory,
			final CyLayoutAlgorithmManager cyLayoutAlgorithmManager,
			final CyEventHelper cyEventHelper,
			final VisualMappingManager cyVisualMappingManager
		) {
		this.dbConnectionManager = dbConnectionManager;
		this.cyNetworkFactory = cyNetworkFactory;
		this.cyNetworkManager = cyNetworkManager;
		this.cyNetworkNaming = cyNetworkNaming;
		this.cyNetworkViewManager = cyNetworkViewManager;
		this.cyNetworkViewFactory = cyNetworkViewFactory;
		this.cyLayoutAlgorithmManager = cyLayoutAlgorithmManager;
		this.cyEventHelper = cyEventHelper;
		this.cyVisualMappingManager = cyVisualMappingManager;
	}
	
	@Override
	public TaskIterator createTaskIterator() {
		return new TaskIterator(new DatabaseNetworkTableReader(
				dbConnectionManager,
				cyNetworkManager,
				cyNetworkFactory,
				cyNetworkNaming,
				cyNetworkViewManager,
				cyNetworkViewFactory,
				cyLayoutAlgorithmManager,
				cyEventHelper,
				cyVisualMappingManager));
	}
}


