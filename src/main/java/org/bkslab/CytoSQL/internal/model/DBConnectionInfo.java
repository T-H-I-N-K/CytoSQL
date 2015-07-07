package org.bkslab.CytoSQL.internal.model;

import java.util.HashMap;
import java.util.Map;

import org.cytoscape.work.Tunable;
import org.cytoscape.work.util.ListSingleSelection;


public class DBConnectionInfo {

	public static String SAVER_TABLE = "CytoSQL_DatabaseConnectionInfo";
	public static String SAVER_TABLE_KEY_COLUMN = "id";
	public static String SAVER_TABLE_DEFAULT_KEY = "default";
	
	
	// Database connection info
	@Tunable(description = "Parameter ID:", gravity = 1)
	public String id = "default";
	
	@Tunable(description = "driver", groups = "Database Connection", gravity = 2)
	public String driver = "org.sqlite.JDBC";
	
	@Tunable(description = "url", groups = "Database Connection", gravity = 3)
	public String url = "jdbc:sqlite:/tmp/test.db3";
	
	@Tunable(description = "user", groups = "Database Connection", gravity = 4)
	public String user = "";
	
	@Tunable(description = "password", groups = "Database Connection", gravity = 5)
	public String password = "";
	
	@Tunable(description = "database", groups = "Database Connection", gravity = 6)
	public String database = "";
	
	@Tunable(description = "schema", groups = "Database Connection", gravity = 7)
	public String schema = "";

	public DBConnectionInfo() {}
	
	public DBConnectionInfo(
		final String id,
		final String driver,
		final String url,
		final String user,
		final String password,
		final String database,
		final String schema
	){
		this.id = id;
		this.driver = driver;
		this.url = url;
		this.user = user;
		this.password = password;
		this.database = database;
		this.schema = schema;
		
	}
	
	public DBConnectionInfo(
		Map<String, Object> properties){
		setProperties(properties);
	}
	
	
	public void setProperties(
		Map<String, Object> properties){
		
		
		if(properties == null) throw new NullPointerException();
		this.id = (String) properties.get("id");
		this.driver = (String) properties.get("driver");
		this.url = (String) properties.get("url");
		this.user = (String) properties.get("user");
		this.password = (String) properties.get("password");
		this.database = (String) properties.get("database");
		this.schema = (String) properties.get("schema");
	}
	
	public Map<String, Object> getProperties(){
		HashMap<String, Object> properties = new HashMap<String, Object>();
		properties.put("id", this.id);
		properties.put("driver", this.driver);
		properties.put("url",  this.url);
		properties.put("user",  this.user);
		properties.put("password", this.password);
		properties.put("database", this.database);
		properties.put("schema",  this.schema);
		return properties;
	}
	
	
	public void validate(){
		
		
	}
	
	
}
