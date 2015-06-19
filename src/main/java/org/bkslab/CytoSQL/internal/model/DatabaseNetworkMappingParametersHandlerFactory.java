package org.bkslab.CytoSQL.internal.model;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.cytoscape.work.Tunable;
import org.cytoscape.work.swing.GUITunableHandlerFactory;
import org.bkslab.CytoSQL.internal.model.DatabaseNetworkMappingParametersHandler;



public class DatabaseNetworkMappingParametersHandlerFactory implements GUITunableHandlerFactory<DatabaseNetworkMappingParametersHandler> {

	public DatabaseNetworkMappingParametersHandlerFactory(){}
	
	
	@Override
	public DatabaseNetworkMappingParametersHandler createTunableHandler(Field field,
			Object instance, Tunable tunable) {
		
		if(!DatabaseNetworkMappingParameters.class.isAssignableFrom(field.getType())){
			return null;
		}

		return new DatabaseNetworkMappingParametersHandler(field, instance, tunable);
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
