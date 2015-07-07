package org.bkslab.CytoSQL.internal.tasks;

import java.util.Map;

import org.bkslab.CytoSQL.internal.model.DBConnectionInfo;
import org.bkslab.CytoSQL.internal.model.PropertiesSaver;
import org.cytoscape.model.CyTableFactory;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ContainsTunables;
import org.cytoscape.work.ProvidesTitle;
import org.cytoscape.work.TaskMonitor;


public class DatabaseConnectionInfoTask extends AbstractTask {

	PropertiesSaver propertiesSaver;
	
	
	// Database parameters
	@ContainsTunables
	public DBConnectionInfo dbConnectionInfo;
	
	@ProvidesTitle	
	public String getTitle() {
		return "Set Database Connection Info";
	}
	
	
	public DatabaseConnectionInfoTask(
		CyTableFactory cyTableFactory,
		CyTableManager cyTableManager
			){
		propertiesSaver = new PropertiesSaver(cyTableFactory, cyTableManager);
		Map<String, Object>  dbConnectionInfoProperties = propertiesSaver.getProperties(
				DBConnectionInfo.SAVER_TABLE,
				DBConnectionInfo.SAVER_TABLE_DEFAULT_KEY);
		
		if(dbConnectionInfoProperties == null){
			dbConnectionInfo = new DBConnectionInfo();
		} else {
			dbConnectionInfo = new DBConnectionInfo(dbConnectionInfoProperties);
		}
		
	}
	
	
	public void run(final TaskMonitor taskMonitor) {
		taskMonitor.setTitle("Saving CytoSQL Database Connection Info");
		try{
			propertiesSaver.saveProperties(
				DBConnectionInfo.SAVER_TABLE,
				DBConnectionInfo.SAVER_TABLE_KEY_COLUMN,
				dbConnectionInfo.getProperties());
		} catch(Exception e) {
			System.out.println("Unable to save the database connection info into the cytoscape file.\n" + e.getMessage());
		}
	}
}
