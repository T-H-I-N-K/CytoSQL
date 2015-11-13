package org.bkslab.CytoSQL.internal.model;

import java.sql.ResultSet;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.work.Tunable;

public class DatabaseNetworkMappingParameters {	
	
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

	public String sqlQuery;
	
	public String newNetworkName;
	
	//@Tunable(description="Node Join Column", groups="Network Mapping")
	public String nodeJoinColumnName;
	
	// these are the columns in the returned SQL ResultSet
	//@Tunable(description="Source column", groups="Network Mapping")
	public int source;
	
	//@Tunable(description="Target column", groups="Network Mapping")
	public int target;
	
	//@Tunable(description="Interaction column", groups="Network Mapping")
	public int interaction;
	
	//@Tunable(description="Default interaction", groups="Network Mapping")
	public String defInteraction;

	//@Tunable(description="Source column pattern", groups="Network Mapping")
	public String sourceColumnPatternString;
	
	private Pattern sourceColumnPattern;

	//@Tunable(description="Target column pattern", groups="Network Mapping")
	public String targetColumnPatternString;
	
	private Pattern targetColumnPattern;

	//@Tunable(description="Is the added network mutable?", groups="Network Mapping")
	public boolean isMutable;
	
	//@Tunable(description="Is the network directed?", groups="Network Mapping")
	public boolean isDirected;
	
	public DatabaseNetworkMappingParameters() {}
	
	public DatabaseNetworkMappingParameters(
//			final String listDelimiter,
//			final Class<?>[] listAttributeTypes,
			final String sqlQuery,
			final String newNetworkName,
			final int source,
			final int target,
			final int interaction,
			final String defInteraction,
			final String nodeJoinColumnName,
			final String sourceColumnPatternString,
			final String targetColumnPatternString,
			final boolean isMutable,
			final boolean isDirected) {
		
//		this.listAttributeTypes = listAttributeTypes;
		
//		if (listDelimiter == null) {
//			this.listDelimiter = DEF_LIST_DELIMITER;
//		} else {
//			this.listDelimiter = listDelimiter;
//		}
		this.sqlQuery = sqlQuery;
		this.newNetworkName = newNetworkName;
		this.source = source;
		this.target = target;
		this.interaction = interaction;
		this.defInteraction = defInteraction;
		this.nodeJoinColumnName = nodeJoinColumnName;
		this.sourceColumnPatternString = sourceColumnPatternString;
		this.sourceColumnPattern = Pattern.compile(sourceColumnPatternString);
		
		this.targetColumnPatternString = targetColumnPatternString;
		this.targetColumnPattern = Pattern.compile(targetColumnPatternString);
		
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
	
	public String getSQLQuery() {
		return sqlQuery;
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
	
	public String getNodeJoinColumnName() {
		return nodeJoinColumnName;
	}
	
	public boolean isNodeAttribute(final int i){
		return (i != getSourceIndex());
	}
	
	public boolean isSourceAttribute(final int i, final String columnName){
		Matcher m = this.sourceColumnPattern.matcher(columnName);
		return (i != getSourceIndex() && m.matches());
	}

	public String getSourceColumnName(final String columnName){
		Matcher m = this.sourceColumnPattern.matcher(columnName);
		if(m.matches()){
			return m.group(1);
		} else {
			return null;
		}
	}
	
	public boolean isTargetAttribute(final int i, final String columnName){
		Matcher m = this.targetColumnPattern.matcher(columnName);
		return (i != getTargetIndex() && m.matches());
	}

	public String getTargetColumnName(final String columnName){
		Matcher m = this.targetColumnPattern.matcher(columnName);
		if(m.matches()){
			return m.group(1);
		} else {
			return null;
		}
	}
	
	public boolean isEdgeAttribute(final int i, final String columnName){
		return 
			!isSourceAttribute(i, columnName) &&
			!isTargetAttribute(i, columnName) &&
			(i != getInteractionIndex());
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
	
	public void saveIntoNetwork(CyNetwork network) {
		
	}
	
	
}
