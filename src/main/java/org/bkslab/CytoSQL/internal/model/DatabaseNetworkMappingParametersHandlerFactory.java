package org.bkslab.CytoSQL.internal.model;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.cytoscape.work.Tunable;
import org.cytoscape.work.swing.GUITunableHandlerFactory;
import org.bkslab.CytoSQL.internal.model.DatabaseNetworkMappingParametersHandler;



public class DatabaseNetworkMappingParametersHandlerFactory implements GUITunableHandlerFactory<DatabaseNetworkMappingParametersHandler> {

	private final DBConnectionManager dbConnectionManager; 
	
	public DatabaseNetworkMappingParametersHandlerFactory(
		final DBConnectionManager dbConnectionManager	
		){
		this.dbConnectionManager = dbConnectionManager;
		
	}
	
	
	@Override
	public DatabaseNetworkMappingParametersHandler createTunableHandler(
			Field field,
			Object instance,
			Tunable tunable) {
		
		if(!DatabaseNetworkMappingParameters.class.isAssignableFrom(field.getType())){
			return null;
		}

		return new DatabaseNetworkMappingParametersHandler(field, instance, tunable, dbConnectionManager);
	}



	@Override
	public DatabaseNetworkMappingParametersHandler createTunableHandler(Method getter,
			Method setter, Object instance, Tunable tunable) {
		
		if(!DatabaseNetworkMappingParameters.class.isAssignableFrom(getter.getReturnType())){
			return null;
		}
		return new DatabaseNetworkMappingParametersHandler(getter, setter, instance, tunable);
	}

}
