package org.bkslab.CytoSQL.internal.model;

import java.util.Map;

import org.cytoscape.model.CyTableFactory;
import org.cytoscape.model.CyTableManager;

public class DBConnectionManager {

	PropertiesSaver propertiesSaver;
	
	public DBConnectionManager(
		final CyTableFactory cyTableFactory,
		final CyTableManager cyTableManager){
		propertiesSaver = new PropertiesSaver(cyTableFactory, cyTableManager);
	}
	
	public DBConnectionInfo getDBConnectionInfo(){
		return getDBConnectionInfo(DBConnectionInfo.SAVER_TABLE_DEFAULT_KEY);
	}
	
	public DBConnectionInfo getDBConnectionInfo(final String connection_key){
		Map<String, Object>  dbConnectionInfoProperties = propertiesSaver.getProperties(
				DBConnectionInfo.SAVER_TABLE,
				connection_key);
		
		if(dbConnectionInfoProperties == null){
			System.out.println("Database Connection Info could not be found.");
			return new DBConnectionInfo();
		} else {
			return new DBConnectionInfo(dbConnectionInfoProperties);
		}
	}
	
	public void saveDBConnectionInfo(final DBConnectionInfo dbConnectionInfo){
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
