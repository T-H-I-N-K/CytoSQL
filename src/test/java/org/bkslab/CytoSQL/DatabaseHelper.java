package org.bkslab.CytoSQL;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.UUID;

public class DatabaseHelper {

	
	// Execute a query on a given temporary database, if none is given create new database
	public static String updateTempDB(String url, final String query){
		if(url == ""){
			url = "jdbc:sqlite:///tmp/CytoSQL_" + UUID.randomUUID() + ".db3"; 
		}
		
		try {
			Connection conn = DriverManager.getConnection(url,"","");
	        Statement st = conn.createStatement();
	        st.executeUpdate(query);
	        st.close();
	        conn.close();
		} catch (Exception e){
			e.printStackTrace();
		}
        return url;
	}
	
	public static String CreateSimpleNetwork(){
		String url = updateTempDB("", "CREATE TABLE network (source TEXT, target TEXT);");
		updateTempDB(url, "INSERT INTO network VALUES ('a', 'b');");
		updateTempDB(url, "INSERT INTO network VALUES ('a', 'c');");
		updateTempDB(url, "INSERT INTO network VALUES ('a', 'd');");
		updateTempDB(url, "INSERT INTO network VALUES ('b', 'c');");
		updateTempDB(url, "INSERT INTO network VALUES ('c', 'd');");
		return url;
	}
	
	
	public static void cleanupDatabase(final String url){
		File db = new File(url);
		db.delete();
	}
}
