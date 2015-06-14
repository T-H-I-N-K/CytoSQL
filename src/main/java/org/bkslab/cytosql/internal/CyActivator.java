package org.bkslab.cytosql.internal;

import org.bkslab.cytosql.internal.tasks.CytoSQLTaskFactory;
import org.osgi.framework.BundleContext;

import static org.cytoscape.work.ServiceProperties.COMMAND;
import static org.cytoscape.work.ServiceProperties.COMMAND_NAMESPACE;
import static org.cytoscape.work.ServiceProperties.ENABLE_FOR;
import static org.cytoscape.work.ServiceProperties.INSERT_SEPARATOR_BEFORE;
import static org.cytoscape.work.ServiceProperties.IN_MENU_BAR;
import static org.cytoscape.work.ServiceProperties.MENU_GRAVITY;
import static org.cytoscape.work.ServiceProperties.PREFERRED_MENU;
import static org.cytoscape.work.ServiceProperties.TITLE;

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
		cytoSQLTaskFactoryProps.setProperty(TITLE, "CytoSQL");
		cytoSQLTaskFactoryProps.setProperty(PREFERRED_MENU, "Apps.CytoSQL");
		cytoSQLTaskFactoryProps.setProperty(IN_MENU_BAR, "false");
		cytoSQLTaskFactoryProps.setProperty(MENU_GRAVITY, "1.0");
		cytoSQLTaskFactoryProps.setProperty(COMMAND, mode);
		cytoSQLTaskFactoryProps.setProperty(COMMAND_NAMESPACE, "CytoSQL");

		
		
		registerService(
				context,
				cytoSQLTaskFactory,
				NetworkViewTaskFactory.class,
				cytoSQLTaskFactoryProps);
	}
}
