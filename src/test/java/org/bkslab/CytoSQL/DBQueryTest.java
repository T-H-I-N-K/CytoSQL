package org.bkslab.CytoSQL;

import static org.junit.Assert.*;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import org.bkslab.CytoSQL.internal.model.DBConnectionInfo;
import org.bkslab.CytoSQL.internal.model.DBQuery;
import org.junit.Before;
import org.junit.Test;

public class DBQueryTest {

	@Before
	public void setUp() {
		//initialize SQLite Driver
		try {
			Class.forName("org.sqlite.JDBC").newInstance();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}
	 
	 
	@Test
	public void TestDBQueryGetResults() {

		final String url = DatabaseHelper.CreateSimpleNetwork();
		
		try {
			DBQuery dbQuery = new DBQuery(new DBConnectionInfo("org.sqlite.JDBC", url, "", "", "", ""));
			ResultSet resultSet = dbQuery.getResults("SELECT * FROM network;");
			assertEquals(resultSet.next(), true);
			ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
			assertEquals(resultSetMetaData.getColumnCount(), 2);
			String source = resultSet.getString(1);
			assertFalse(resultSet.wasNull());
			String target = resultSet.getString(2);
			assertFalse(resultSet.wasNull());
			assertEquals(source, "a");
			assertEquals(target, "b");
			assertEquals(resultSet.next(), true);
			assertEquals(resultSet.next(), true);
			assertEquals(resultSet.next(), true);
			assertEquals(resultSet.next(), true);
			assertEquals(resultSet.next(), false);
			dbQuery.close();
		} catch (SQLException e) {
			e.printStackTrace();
			fail();
		}
		DatabaseHelper.cleanupDatabase(url);
	}

}
