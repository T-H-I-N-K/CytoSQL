package org.bkslab.CytoSQL;

import java.util.HashMap;

import org.bkslab.CytoSQL.internal.model.PropertiesSaver;
import org.cytoscape.model.CyTableFactory;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.model.NetworkTestSupport;
import org.cytoscape.model.TableTestSupport;
import org.junit.Test;

import static org.mockito.Mockito.mock;


public class PropertiesSaverTest {

	private TableTestSupport support = new TableTestSupport();
	
	
	@Test
	public void testSaveProperties(){
		HashMap<String, Object> properties1 = new HashMap<String, Object>();
		properties1.put("key1", 1);
		properties1.put("key2", 2);
		
		CyTableFactory cyTableFactory = support.getTableFactory();
		CyTableManager cyTableManager = mock(CyTableManager.class);
		
		PropertiesSaver propertiesSaver = new PropertiesSaver(cyTableFactory, cyTableManager);
		
		try {
			propertiesSaver.saveProperties("properties1", "key1", properties1);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
