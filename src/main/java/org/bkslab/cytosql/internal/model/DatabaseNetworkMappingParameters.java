package org.bkslab.cytosql.internal.model;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;
import java.util.Map;

import org.cytoscape.work.Tunable;

public class DatabaseNetworkMappingParameters {
// would like to derive from AbstractMappingParameters, but it isn't exposed through the API

	// copied from AbtractMappingParameters
	
	// what is this ID used for?
	//public static final String ID = "name";
	
	private static final String DEF_LIST_DELIMITER = "|";
	private static final String DEF_INTERACTION = "pp";
	
	@Tunable
	private Class<?>[] listAttributeTypes;
	
	@Tunable
	private String listDelimiter;
	
	@Tunable
	private Map<String, List<String>> attr2id;
	
	@Tunable
	private Map<String, String> networkTitle2ID = null;

	
	@Tunable
	private boolean isMutable;
	
	@Tunable
	private boolean isDirected;
	
	// these are the columns in the returned SQL ResultSet
	@Tunable
	private int source;
	
	@Tunable
	private int target;
	
	@Tunable
	private int interaction;
	
	@Tunable
	private String defInteraction;
	
	public DatabaseNetworkMappingParameters(){}
	
	public DatabaseNetworkMappingParameters(
			final String listDelimiter,
			final Class<?>[] listAttributeTypes,
			final int source,
			final int target,
			final int interaction,
			final String defInteraction,
			final int startNumber,
			final boolean isMutable,
			final boolean isDirected) throws Exception{
		
		this.listAttributeTypes = listAttributeTypes;
		
		if (listDelimiter == null) {
			this.listDelimiter = DEF_LIST_DELIMITER;
		} else {
			this.listDelimiter = listDelimiter;
		}
				
		this.source = source;
		this.target = target;
		this.interaction = interaction;
		this.defInteraction = defInteraction;
		this.isMutable = isMutable;
		this.isDirected = isDirected;
	}
	


	public String getListDelimiter() {
		return this.listDelimiter;
	}
	
	public boolean isListAttribute(final int i) {
		return this.listAttributeTypes[i] == null;
	}
	
	public Class<?> getListAttributeType(final int i){
		return this.listAttributeTypes[i];
	}
	
	public int getSourceIndex() {
		return source;
	}

	public int getTargetIndex() {
		return target;
	}

	public int getInteractionIndex() {
		return interaction;
	}

	public String getDefaultInteraction() {
		if (defInteraction == null) {
			return DEF_INTERACTION;
		} else {
			return defInteraction;
		}
	}
	
	public boolean isEdgeAttribute(final int i){
		return (i != getSourceIndex())  && (i != getTargetIndex()) &&	(i != getInteractionIndex());
	}
	
	public boolean isMutable() {
		return isMutable;
	}
	
	public boolean isDirected() {
		return isDirected;
	}
	}
	
}
