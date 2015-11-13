# CytoSQL
Create, Enrich, and Extend Networks through SQL Database Queries

## Create Network
Create a network from an SQL query

### Inputs
* Database connection info
* SQL query
* a map of query columns to nodes, node attributes, and edge attributes
* are the new edges directed

### Result
Creates a new network from the data retrieved from the database

## Expand Network
Expand a given network with additional nodes and edges from an SQL query

### Inputs
* A set of selected nodes in a network
* Database connection info
* SQL query with a single bind parameter
* a map of query columns to nodes, node attributes, and edge attributes
* are the new edge directed

### Result
* The query is executed binding a single 


# Database Connection Information
To connect to a database requires making the Java driver available, and then specifying the appropriate connection parameters.

## SQLite Databases
### Driver:The SQLite driver is distributed with the CytoSQL package
### Connection Info:
#### driver: 'org.sqlite.JDBC'
#### url: 'jdbc:sqlite:<file_name.db3>'




# TODO
* After setting database parameters, check the connection
* Set save state of the database connection info hidden table
* Have forms adjust when the dimensions of the windows are changed.

* This connection has closed error





# Structure of new CytoSQL

* DBConnectionInfo
** Tunables for database connection information
** Retrieves information in Network if it's not specified

* DBConnectionManager?

* DBQuery
** makeConnection: DBConnectionInfo -> java.sql.Connection
** getSchemas: -> List<String>
** getTables: schema?, tableType? -> List<String>
** getTableColumn: tableName -> javax.swing.table.TableModel  (Not sure about this)
** getResults: sql -> ResultSet
** printResultSet: resultSet, OutputStream


* How should data in cytoscape be mapped to database
** Version 1
***  given query nodes and map column, do query and list of edges 


* CreateNetworkTask
** Tunables
*** sql_query, column_map, network_title, is_directed

** run:
*** get connection from DBConnecitonManager
*** initialize DBQuery
*** get result set
*** create network

* ExpandNetwork
** Tunables
*** sql_query, column_map, is_directed

** run:
*** get selected nodes
*** get connection from DBConnectionManager
*** initialize DBQuery
*** get result set
*** add data to network

** retriveData
*** conn, nodes, bind_params, sql
*** query the database once for each node
*** binding the columns from the node table to the query


# Structure of old CytoSQL


* DBConnectionInfo
** Static path to database connection information, supports reading and writing connection information to the file
** Should probably take Scooter's suggestion and store it with the Network in a auxilary table

* Query.AlgoClass
** java.sql.Connection
** java.sql.ResultSet

** BatchType
*** EXPAND, UPDATE_NODE_ATTR, UPDATE_EDGE_ATTR
** processBatchQuery
***  sql, bindvarMap, type
***  -> call [ExpandNetwork,UpdateNodeAttributes,UpdateEdgeAttributes]Query.processBatchResults

** Attributes are a Cytoscape global namespace for storing node and edge data 

** getAttributeTypes
*** ResultSet, possibilities, startCol
*** sets nodeColumns[0] for column with label "source node" and nodeColumn[1] for column with label "target node"
***  uses chooseAttributeType dialogbox to get user selection

** createNetwork
*** SQLResult Map<String, ResultSet>
***   Should map "UNSPECIFIED" to ResultSet with at least 2 columns
***   write to temp file '<col1> interacts <col2>' for each row, and call createNetworkFromFile
      
      
** Don't worry about
*** showSummary
*** map:
*** nodeColumns: Setup in getAttributeTypes, accessed in createNetwork, writeSIFFromDatabase

* ExpandNetworkQuery
** processsBatchResults: sql, bindvarMap, conn -> Map< node_id, ResultSet > 
*** Execute query for each node in selection and return a map of the results

** expandNetwork: Map<String, ResultSet> SQLResult
*** -> for each (key, rs) -> addNewNodesAndAttributes(key, rs, getAttributeTypes(...), getNewNodeType(...))

** getNewNodeType: col_name -> String of ['Target node', 'Source node'] through dialog box
** getAttributeTypes: ResultSet, possibilities, startCol -> Map<String, String>
*** for each column [startCol, colCount] chooseAttributeType( column_label, temp_possibilities) through a dialog box, if it's a node or edge attribute

** bindvarMapInitialize
*** sql -> Map<Integer, String> bind_var_index, node/edge_attribute_name
***  -> counts bind vars and calls mapBindVars for each (showInputDialog)


