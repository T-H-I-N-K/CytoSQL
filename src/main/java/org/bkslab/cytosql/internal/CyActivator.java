package org.bkslab.cytosql.internal;

import org.bkslab.cytosql.internal.CytoSQLTaskFactory;
import org.osgi.framework.BundleContext;

import org.cytoscape.application.swing.CySwingApplication;

import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.task.NetworkViewTaskFactory;
import java.util.Properties;

public class CyActivator extends AbstractCyActivator {
	
	public CyActivator() {
		super();
	}
		
	@Override
	public void start(BundleContext context) throws Exception {
		CytoSQLTaskFactory cytoSQLTaskFactory = new CytoSQLTaskFactory();
		Properties cytoSQLTaskFactoryProps = new Properties();
		cytoSQLTaskFactoryProps.setProperty("preferredMenu", "Apps");
		cytoSQLTaskFactoryProps.setProperty("title", "Network Expander");
		registerService(
				context,
				cytoSQLTaskFactory,
				NetworkViewTaskFactory.class,
				cytoSQLTaskFactoryProps);
	}
}
