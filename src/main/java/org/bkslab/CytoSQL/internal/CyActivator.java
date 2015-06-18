package org.bkslab.CytoSQL.internal;

import org.bkslab.CytoSQL.internal.tasks.DatabaseNetworkExtenderFactory;
import org.bkslab.CytoSQL.internal.tasks.DatabaseNetworkTableReaderFactory;
import org.osgi.framework.BundleContext;

import static org.cytoscape.work.ServiceProperties.COMMAND;
import static org.cytoscape.work.ServiceProperties.COMMAND_NAMESPACE;
import static org.cytoscape.work.ServiceProperties.IN_MENU_BAR;
import static org.cytoscape.work.ServiceProperties.MENU_GRAVITY;
import static org.cytoscape.work.ServiceProperties.PREFERRED_MENU;
import static org.cytoscape.work.ServiceProperties.TITLE;

import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.session.CyNetworkNaming;
import org.cytoscape.task.NetworkViewTaskFactory;
import org.cytoscape.work.TaskFactory;

import java.util.Properties;

public class CyActivator extends AbstractCyActivator {
	
	public CyActivator() {
		super();
	}
		
	@Override
	public void start(BundleContext context) throws Exception {
		///////////////////
		CyNetworkManager cyNetworkManagerServiceRef = getService(context,CyNetworkManager.class);
		CyNetworkFactory cyNetworkFactoryServiceRef = getService(context,CyNetworkFactory.class);
		CyNetworkNaming cyNetworkNamingServiceRef = getService(context,CyNetworkNaming.class);

		DatabaseNetworkTableReaderFactory databaseNetworkTableReaderFactory = new DatabaseNetworkTableReaderFactory(
			cyNetworkManagerServiceRef,
			cyNetworkFactoryServiceRef,
			cyNetworkNamingServiceRef);
			
		Properties databaseNetworkTableReaderFactoryProps = new Properties();		
		databaseNetworkTableReaderFactoryProps.setProperty(TITLE, "Create Network From Database Query");
		databaseNetworkTableReaderFactoryProps.setProperty(PREFERRED_MENU, "Apps.CytoSQL");
		databaseNetworkTableReaderFactoryProps.setProperty(IN_MENU_BAR, "false");
		databaseNetworkTableReaderFactoryProps.setProperty(MENU_GRAVITY, "1.0");
		databaseNetworkTableReaderFactoryProps.setProperty(COMMAND, "CreateNetworkFromDatabaseQuery");
		databaseNetworkTableReaderFactoryProps.setProperty(COMMAND_NAMESPACE, "CytoSQL");
		registerService(context,databaseNetworkTableReaderFactory,TaskFactory.class, databaseNetworkTableReaderFactoryProps);
		
		
		DatabaseNetworkExtenderFactory setNetworkBackgroundColorTaskFactory = new DatabaseNetworkExtenderFactory();
		Properties databaseNetworkExtenderFactoryProps = new Properties();
		databaseNetworkExtenderFactoryProps.setProperty(PREFERRED_MENU,"Apps.CytoSQL");
		databaseNetworkExtenderFactoryProps.setProperty(TITLE,"Extend Network by Query");
		databaseNetworkExtenderFactoryProps.setProperty(COMMAND, "ExtendNetworkFromQuery");
		databaseNetworkExtenderFactoryProps.setProperty(COMMAND_NAMESPACE, "CytoSQL");
		registerService(context,setNetworkBackgroundColorTaskFactory,NetworkViewTaskFactory.class, databaseNetworkExtenderFactoryProps);
		
	}
}

