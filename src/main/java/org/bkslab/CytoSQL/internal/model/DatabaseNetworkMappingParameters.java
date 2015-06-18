package org.bkslab.CytoSQL.internal.model;

import java.sql.ResultSet;
import java.util.List;
import java.util.Map;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.work.Tunable;

public class DatabaseNetworkMappingParameters {
// would like to derive from AbstractMappingParameters, but it isn't exposed through the API

	// copied from AbtractMappingParameters
	
	// what is this ID used for?
	//public static final String ID = "name";
	
	private static final String DEF_LIST_DELIMITER = "|";
	private static final String DEF_INTERACTION = "pp";
	
//	@Tunable(description="Which attributes are list attributes.")
//	public Class<?>[] listAttributeTypes;
	
//	@Tunable
//	public String listDelimiter;
	
//	@Tunable
//	public Map<String, List<String>> attr2id;
	
//	@Tunable
//	public Map<String, String> networkTitle2ID = null;

	@Tunable(description="Node Join Column", groups="Network Mapping")
	public String nodeJoinColumnName;
	

	
	// these are the columns in the returned SQL ResultSet
	@Tunable(description="Source column", groups="Network Mapping")
	public int source;
	
	@Tunable(description="Target column", groups="Network Mapping")
	public int target;
	
	@Tunable(description="Interaction column", groups="Network Mapping")
	public int interaction;
	
	@Tunable(description="Default interaction", groups="Network Mapping")
	public String defInteraction;
		
	@Tunable(description="Is the added network mutable?", groups="Network Mapping")
	public boolean isMutable;
	
	@Tunable(description="Is the network directed?", groups="Network Mapping")
	public boolean isDirected;
	
	public DatabaseNetworkMappingParameters() {}
	
	public DatabaseNetworkMappingParameters(
//			final String listDelimiter,
//			final Class<?>[] listAttributeTypes,
			final int source,
			final int target,
			final int interaction,
			final String defInteraction,
			final String nodeJoinColumnName,
			final boolean isMutable,
			final boolean isDirected) {
		
//		this.listAttributeTypes = listAttributeTypes;
		
//		if (listDelimiter == null) {
//			this.listDelimiter = DEF_LIST_DELIMITER;
//		} else {
//			this.listDelimiter = listDelimiter;
//		}
				
		this.source = source;
		this.target = target;
		this.interaction = interaction;
		this.defInteraction = defInteraction;
		this.nodeJoinColumnName = nodeJoinColumnName;
		this.isMutable = isMutable;
		this.isDirected = isDirected;
	}
	


//	public String getListDelimiter() {
//		return this.listDelimiter;
//	}
	
//	public boolean isListAttribute(final int i) {
//		return this.listAttributeTypes[i] == null;
//	}
	
//	public Class<?> getListAttributeType(final int i){
//		return this.listAttributeTypes[i];
//	}
	
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
	
	public String getNodeJoinColumnName() {
		return nodeJoinColumnName;
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
	
	
	public void validate(CyNetwork network, ResultSet resultSet) throws Exception{
		if(this.source == this.target){
			throw new Exception("The source and target columns cannot be the same.");
		}
		
		if(this.source == this.interaction){
			throw new Exception("The source and interaction columns cannot be the same.");
		}
		
		if(this.target == this.interaction){
			throw new Exception("The target and interaction columns cannot be the same.");
		}
		
		int ncol = resultSet.getMetaData().getColumnCount();
		if(this.source < 1 | this.source > ncol){
			throw new Exception("The source column is '" + this.source + "', but it must be in the range [0" + "," + ncol + "]");
		}
		
		if(this.target < 1 | this.target > ncol){
			throw new Exception("The target column is '" + this.target + "', but it must be in the range [0" + "," + ncol + "]");
		}

		if(this.target < 1 | this.target > ncol){
			throw new Exception("The interaction column is '" + this.interaction + "', but it must be in the range [0" + "," + ncol + "]");
		}

//		if(ncol != listAttributeTypes.length){
//			throw new Exception(
//				"The listAttributeTypes has '" + listAttributeTypes.length + "', " +
//				"but this does not match the number of columns in the query '" + ncol + "'");
//		}
		
	}
	
}
