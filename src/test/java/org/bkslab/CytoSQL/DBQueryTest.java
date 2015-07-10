package org.bkslab.CytoSQL;

import static org.junit.Assert.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import org.bkslab.CytoSQL.internal.model.DBConnectionInfo;
import org.bkslab.CytoSQL.internal.model.DBConnectionManager;
import org.bkslab.CytoSQL.internal.model.DBQuery;
import org.bkslab.CytoSQL.internal.model.DatabaseNetworkMappingParameters;
import org.bkslab.CytoSQL.DatabaseHelper;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DBQueryTest {

	
	@Before
	public void setUp() {
		//initialize SQLite Driver
		try {
			Class.forName("org.sqlite.JDBC").newInstance();
		} catch (InstantiationException e) {
			System.err.println("Failed to instantiate the SQLite JDBC driver.");
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			System.err.println("Failed to access the SQLite JDBC driver.");
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			System.err.println("Failed to locate the SQLite JDBC driver.");			
			e.printStackTrace();
		} 
		
		//initialize PostreSQL Driver
		try {
			Class.forName("org.postgresql.Driver").newInstance();
		} catch (InstantiationException e) {
			System.err.println("Failed to instantiate the postgres JDBC driver.");
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			System.err.println("Failed to access the postgres JDBC driver.");
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			System.err.println("Failed to locate the postgres JDBC driver.");
			String classpath = System.getProperty("java.class.path");
			System.err.println("Class path: \n" + classpath);
			e.printStackTrace();
		} 
	}

	//@Test
	public void TestPostgresGetSchemas() {

		Connection conn;
		try {
			conn = DriverManager.getConnection("jdbc:postgresql://localhost/momeara", "momeara", "che8ga5R");
			DBQuery dbQuery = new DBQuery(conn, "sea_chembl18");
			for(String schema : dbQuery.getSchemas()){
				System.out.println("Schema: " + schema);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			fail();
		}

	}
	 
	//@Test
	public void TestPostgresDBQueryGetResults() {

		try {
			Connection conn;
			conn = DriverManager.getConnection("jdbc:postgresql://localhost/momeara", "momeara", "che8ga5R");
			DBQuery dbQuery = new DBQuery(conn, "sea_chembl18");
			ResultSet resultSet = dbQuery.getResults("SELECT * FROM sea_chembl18.scores LIMIT 4;");
			assertEquals(resultSet.next(), true);
			ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
			assertEquals(resultSetMetaData.getColumnCount(), 5);
			String source = resultSet.getString(1);
			assertFalse(resultSet.wasNull());
			String target = resultSet.getString(2);
			assertFalse(resultSet.wasNull());
			//assertEquals(source, "a");
			//assertEquals(target, "b");
			assertEquals(resultSet.next(), true);
			assertEquals(resultSet.next(), true);
			assertEquals(resultSet.next(), true);
			assertEquals(resultSet.next(), false);
			dbQuery.close();
		} catch (SQLException e) {
			e.printStackTrace();
			fail();
		}
	}

	
	@Test
	public void TestSQLiteDBQueryGetResults() {
		
		final String url = DatabaseHelper.CreateSimpleNetwork();		
		try {
			Connection conn = DriverManager.getConnection(url, "", "");
			DBQuery dbQuery = new DBQuery(conn, "");
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
