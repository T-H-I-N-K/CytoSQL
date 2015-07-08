package org.bkslab.CytoSQL.internal.tasks;

import java.util.Map;

import org.bkslab.CytoSQL.internal.model.DBConnectionInfo;
import org.bkslab.CytoSQL.internal.model.DBConnectionManager;
import org.bkslab.CytoSQL.internal.model.PropertiesSaver;
import org.cytoscape.model.CyTableFactory;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ContainsTunables;
import org.cytoscape.work.ProvidesTitle;
import org.cytoscape.work.TaskMonitor;


public class DatabaseConnectionInfoTask extends AbstractTask {

	DBConnectionManager dbConnectionManager;
	
	
	// Database parameters
	@ContainsTunables
	public DBConnectionInfo dbConnectionInfo;
	
	@ProvidesTitle	
	public String getTitle() {
		return "Set Database Connection Info";
	}
	
	
	public DatabaseConnectionInfoTask(
		final DBConnectionManager dbConnectionManager
		){
		this.dbConnectionManager = dbConnectionManager;
		dbConnectionInfo = dbConnectionManager.getDBConnectionInfo();		
	}
	
	
	public void run(final TaskMonitor taskMonitor) {
		taskMonitor.setTitle("Saving CytoSQL Database Connection Info");
		dbConnectionManager.saveDBConnectionInfo(dbConnectionInfo);
	}
}
