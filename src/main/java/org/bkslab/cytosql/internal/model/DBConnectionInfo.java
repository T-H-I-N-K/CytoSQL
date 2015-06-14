package org.bkslab.cytosql.internal.model;

import org.cytoscape.work.Tunable;
import org.cytoscape.work.util.ListMultipleSelection;
import org.cytoscape.work.util.ListSingleSelection;


public class DBConnectionInfo {

	// Database connection info
	@Tunable(description = "driver", groups = "Database Connection", gravity = 1)
	public ListSingleSelection<String> driver = null;
	
	@Tunable(description = "url", groups = "Database Connection", gravity = 2)
	public String url = "";
	
	@Tunable(description = "user", groups = "Database Connection", gravity = 4)
	public String user = "";
	
	@Tunable(description = "password", groups = "Database Connection", gravity = 5)
	public String password = "";
	
	@Tunable(description = "database", groups = "Database Connection", gravity = 6)
	public String database = "";
	
	@Tunable(description = "postgres_schema", groups = "Database Connection", gravity = 7)
	public String postgres_schema = "";

	
	
}
