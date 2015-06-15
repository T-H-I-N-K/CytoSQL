package org.bkslab.cytosql.internal.tasks;

import java.io.InputStream;
import java.util.Properties;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.io.CyFileFilter;
import org.cytoscape.io.read.AbstractInputStreamTaskFactory;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.property.CyProperty;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;

public class DatabaseNetworkTableReaderFactory extends AbstractTaskFactory {

	private final CyApplicationManager cyApplicationManager;
	private final CyNetworkViewFactory cyNetworkViewFactory;
	private final CyNetworkFactory cyNetworkFactory;
	private final CyLayoutAlgorithmManager layouts;
	private final CyNetworkManager cyNetworkManager;
	private final CyRootNetworkManager cyRootNetworkManager;
	
	private final CyProperty<Properties> prop;
	
	public DatabaseNetworkTableReaderFactory(
			final CyApplicationManager cyApplicationManager,
			final CyNetworkViewFactory cyNetworkViewFactory,
			final CyNetworkFactory cyNetworkFactory,
			final CyLayoutAlgorithmManager layouts,
			final CyProperty<Properties> prop,
			final CyNetworkManager cyNetworkManager,
			final CyRootNetworkManager cyRootNetworkManager
		) {
		this.cyApplicationManager = cyApplicationManager;
		this.cyNetworkFactory = cyNetworkFactory;
		this.cyNetworkViewFactory = cyNetworkViewFactory;
		this.layouts = layouts;
		this.prop = prop;
		this.cyNetworkManager = cyNetworkManager;
		this.cyRootNetworkManager = cyRootNetworkManager;
	}

	@Override
	public TaskIterator createTaskIterator() {
		return new TaskIterator(new DatabaseNetworkTableReader(cyApplicationManager,
				cyNetworkViewFactory, cyNetworkFactory, layouts, prop, cyNetworkManager, cyRootNetworkManager));
	}
}