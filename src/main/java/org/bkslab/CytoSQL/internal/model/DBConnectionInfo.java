package org.bkslab.CytoSQL.internal.model;

import org.cytoscape.work.Tunable;
import org.cytoscape.work.util.ListSingleSelection;


public class DBConnectionInfo {

	// Database connection info
	@Tunable(description = "driver", groups = "Database Connection", gravity = 1)
	public String driver = "org.sqlite.JDBC";
	
	@Tunable(description = "url", groups = "Database Connection", gravity = 2)
	public String url = "jdbc:sqlite:/tmp/test.db3";
	
	@Tunable(description = "user", groups = "Database Connection", gravity = 4)
	public String user = "";
	
	@Tunable(description = "password", groups = "Database Connection", gravity = 5)
	public String password = "";
	
	@Tunable(description = "database", groups = "Database Connection", gravity = 6)
	public String database = "";
	
	@Tunable(description = "postgres_schema", groups = "Database Connection", gravity = 7)
	public String postgres_schema = "";

	public DBConnectionInfo() {}
	
	public DBConnectionInfo(
		final String driver,
		final String url,
		final String user,
		final String password,
		final String database,
		final String postgres_schema
	){
		
		this.driver = driver;
		this.url = url;
		this.user = password;
		this.database = database;
		this.postgres_schema = postgres_schema;
		
	}
	
	
	
	public void validate(){
		
		
	}
	
	
}
