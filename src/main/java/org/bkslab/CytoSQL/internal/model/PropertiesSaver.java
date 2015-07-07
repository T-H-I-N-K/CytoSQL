package org.bkslab.CytoSQL.internal.model;

import java.util.Map;

import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableFactory;
import org.cytoscape.model.CyTableManager;


public class PropertiesSaver {
	
	private CyTableFactory tableFactory;
	private CyTableManager cyTableManager;
	
	public PropertiesSaver(
		CyTableFactory tableFactory,
		CyTableManager cyTableManager){
		this.tableFactory = tableFactory;
		this.cyTableManager = cyTableManager;
	}

	
	// Save the properties to the specified global table and primary key value 
	public void saveProperties(
		final String tableTitle,
		final String primaryKey,
		Map<String, Object> properties) throws Exception {
		
		CyTable table = getTable(tableTitle);
		if(table == null){
			table = createTable(tableTitle, primaryKey, properties);
		}
		saveToTable(table, properties);
	}

	// get properties from specified global table and primary key value
	// returns null if no such table exits
	public Map<String, Object> getProperties(
		final String tableTitle,
		final String primaryKeyValue){
		
		CyTable table = getTable(tableTitle);
		if(table == null) return null;
		return getFromTable(table, primaryKeyValue);
	}
	
	public void clearAllProperites(final String tableTitle){
		CyTable table = getTable(tableTitle);
		if(table == null) return;
		
		cyTableManager.deleteTable(table.getSUID());
	}
	
	private CyTable getTable(final String tableTitle){
		for (CyTable globalTable : cyTableManager.getGlobalTables()){
			if (globalTable.getTitle() == tableTitle){
				return globalTable;
			}
		}
		return null;
	}
		
	private CyTable createTable(
		final String tableTitle,
		final String primaryKey,
		final Map<String, Object> properties) throws Exception{
		
		if(!properties.containsKey(primaryKey)){
			throw new Exception("Cannot create PropertiesSaver table " + tableTitle + " because the properties doesn't have an entry corresponding to the primary key: '" + primaryKey + "'.");
		}
		
		CyTable table = tableFactory.createTable(
			tableTitle, primaryKey, properties.get(primaryKey).getClass(), false, true);
		
		for(Map.Entry<String, Object> entry : properties.entrySet()){
			if(entry.getKey() == primaryKey) continue;
			table.createColumn(entry.getKey(), entry.getValue().getClass(), false);
		}
		
		cyTableManager.addTable(table);
		return table;
	}
	
	
	// add the properties to the specified table, adding columns if necessary
	private void saveToTable(CyTable table, Map<String, Object> properties){
		
		// add columns if needed:
		for(Map.Entry<String,Object> entry : properties.entrySet()){
			if(table.getColumn(entry.getKey()) == null){
				table.createColumn(entry.getKey(), entry.getValue().getClass(), false);
			}
		}
		
		
		final String primaryKey = table.getPrimaryKey().getName();
		final Object primaryKeyValue = properties.get(primaryKey);
		CyRow row = table.getRow(primaryKeyValue);
		
		for(Map.Entry<String, Object> entry : properties.entrySet()){
			if(entry.getKey() == primaryKey) continue;
			row.set(entry.getKey(), entry.getValue());
		}
	}
		
	private Map<String, Object> getFromTable(CyTable table, Object primaryKeyValue){
		final String primaryKey = table.getPrimaryKey().getName();
		for(CyRow row : table.getAllRows()){
			Object value = row.getRaw(primaryKey);
			if(primaryKeyValue.equals(value)){
				return row.getAllValues();
			}
		}
		return null;
	}
}
