package org.bkslab.CytoSQL.internal.model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Map;

import javax.swing.JOptionPane;

import org.apache.commons.dbutils.DbUtils;
import org.cytoscape.model.CyTableFactory;
import org.cytoscape.model.CyTableManager;

public class DBConnectionManager {

	private final PropertiesSaver propertiesSaver;
	private DBQuery dbQuery;
	
	public DBConnectionManager(
		final CyTableFactory cyTableFactory,
		final CyTableManager cyTableManager){
		propertiesSaver = new PropertiesSaver(cyTableFactory, cyTableManager);
	}
	
	public void close(){
		dbQuery.close();
		dbQuery = null;
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
		
		// next time a new dbQuery is requested, reconnect to the database.
		dbQuery.close();
		dbQuery = null;
	}
	
	// this is public to populate the UI with saved data.
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
	
	
	public DBQuery getDBQuery() throws Exception{
		if(dbQuery == null){
			DBConnectionInfo dbConnectionInfo = getDBConnectionInfo(DBConnectionInfo.SAVER_TABLE_DEFAULT_KEY);
			Connection connection = makeConnection(dbConnectionInfo);
			dbQuery = new DBQuery(connection, dbConnectionInfo.schema);
		}
		return dbQuery;
	}


	private Connection makeConnection(
		final DBConnectionInfo dbConnectionInfo) throws Exception{


		int idx = dbConnectionInfo.driver.indexOf("-CUSTOM_DRIVER"); // database connection
													// specification provided by
													// user
		if (idx >= 0) {
			dbConnectionInfo.driver = dbConnectionInfo.driver.substring(0, idx);
		}

		try {
			Class.forName(dbConnectionInfo.driver).newInstance();
		} catch (InstantiationException e) {
			JOptionPane.showMessageDialog(null,
					"Instantiation exception for connection to DB.\n" + e);
		} catch (IllegalAccessException e) {
			JOptionPane.showMessageDialog(null,
					"Cannot access DB connection.\n" + e);
		} catch (ClassNotFoundException e) {
			JOptionPane
					.showMessageDialog(null, "Driver class not found.\n" + e);
		}
		Connection conn;
		if (idx >= 0) { // CUSTOM USER DEFINED DRIVER
			conn = DriverManager.getConnection(
				dbConnectionInfo.url,
				dbConnectionInfo.user,
				dbConnectionInfo.password);
		} else { // Built-in JDBC driver
			conn = DriverManager.getConnection(
				dbConnectionInfo.url + "/" + dbConnectionInfo.database,
				dbConnectionInfo.user,
				dbConnectionInfo.password);
		}
		return conn;
	}
	
}
