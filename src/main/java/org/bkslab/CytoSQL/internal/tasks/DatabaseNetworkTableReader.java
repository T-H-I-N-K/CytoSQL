package org.bkslab.CytoSQL.internal.tasks;

import java.util.Collection;
import java.util.Map;

import org.bkslab.CytoSQL.internal.model.DBConnectionInfo;
import org.bkslab.CytoSQL.internal.model.DBConnectionManager;
import org.bkslab.CytoSQL.internal.model.DatabaseNetworkMappingParameters;
import org.bkslab.CytoSQL.internal.model.PropertiesSaver;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyTableFactory;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.session.CyNetworkNaming;
import org.cytoscape.view.layout.CyLayoutAlgorithm;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ContainsTunables;
import org.cytoscape.work.ProvidesTitle;
import org.cytoscape.work.Task;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;


public class DatabaseNetworkTableReader extends AbstractTask {
	
	private final DBConnectionManager dbConnectionManager;
	
	// Database parameters
	private DBConnectionInfo dbConnectionInfo;
	
	@Tunable
	public DatabaseNetworkMappingParameters dnmp;

	
	private DatabaseNetworkParser parser;
	
	private boolean isCanceled;

	private final CyNetworkManager cyNetworkManager;
	private final CyNetworkFactory cyNetworkFactory;
	private final CyNetworkNaming cyNetworkNaming;
	private final CyNetworkViewManager cyNetworkViewManager;
	private final CyNetworkViewFactory cyNetworkViewFactory;
	private final CyLayoutAlgorithmManager cyLayoutAlgorithmManager;
	
	@ProvidesTitle	
	public String getTitle() {
		return "Create Network from Database Query";
	}
	
	public DatabaseNetworkTableReader(
		final DBConnectionManager dbConnectionManager,
		final CyNetworkManager cyNetworkManager,
		final CyNetworkFactory cyNetworkFactory,
		final CyNetworkNaming cyNetworkNaming,
		final CyNetworkViewManager cyNetworkViewManager,
		final CyNetworkViewFactory cyNetworkViewFactory,
		final CyLayoutAlgorithmManager cyLayoutAlgorithmManager) {
		
		this.dbConnectionManager = dbConnectionManager;
		this.cyNetworkManager = cyNetworkManager;
		this.cyNetworkFactory = cyNetworkFactory;
		this.cyNetworkNaming = cyNetworkNaming;
		this.cyNetworkViewManager = cyNetworkViewManager;
		this.cyNetworkViewFactory = cyNetworkViewFactory;
		this.cyLayoutAlgorithmManager = cyLayoutAlgorithmManager;

		dbConnectionInfo = this.dbConnectionManager.getDBConnectionInfo();
		
		dnmp = new DatabaseNetworkMappingParameters();

	}
	

	public void run(final TaskMonitor taskMonitor) {
		
		taskMonitor.setProgress(0.0);
		taskMonitor.setTitle("Creating network from SQL query.");

		taskMonitor.setProgress(0.2);
		CyNetwork network = createNetwork(taskMonitor);

		if(network == null){
			return;
		}
		
		taskMonitor.setProgress(0.8);	
		createNetworkView(taskMonitor, network);
		taskMonitor.setProgress(1);	

	}
	
	
	private CyNetwork createNetwork(TaskMonitor taskMonitor) {
		final String suggestedName = cyNetworkNaming.getSuggestedNetworkTitle(dnmp.newNetworkName);
		CyNetwork network = cyNetworkFactory.createNetwork();
		network.getRow(network).set(CyNetwork.NAME, suggestedName);
		
		

		parser = new DatabaseNetworkParser(dbConnectionManager, dnmp);

		try {
			parser.parse(taskMonitor, network, dnmp.sqlQuery);
		} catch(Exception e){
			System.out.println("Failed to parse SQL query into network:\n" + e.getMessage());
			network.dispose();
			return null;
		}
	
		taskMonitor.setProgress(.9);
		if(isCanceled){
			network.dispose();
			return null;
		}
		cyNetworkManager.addNetwork(network);
		return network;
	}
	
	private void createNetworkView(TaskMonitor taskMonitor, CyNetwork network){
		
		final Collection<CyNetworkView> views = cyNetworkViewManager.getNetworkViews(network);
		CyNetworkView view = null;
		if(views.size() != 0)
			view = views.iterator().next();
		
		if (view == null) {
			// create a new view for the network
			view = cyNetworkViewFactory.createNetworkView(network);
			cyNetworkViewManager.addNetworkView(view);
		} else {
			System.out.println("networkView already existed.");
		}
		
		// copied from org.cytoscape.tableimport.internal.task.CombineReaderAndMappingTask
		CyLayoutAlgorithm layout = cyLayoutAlgorithmManager.getDefaultLayout();
		TaskIterator itr = layout.createTaskIterator(
			view, layout.getDefaultLayoutContext(), CyLayoutAlgorithm.ALL_NODE_VIEWS, "");
		Task nextTask = itr.next();
		try {
			nextTask.run(taskMonitor);
		} catch (Exception e) {
			throw new RuntimeException("Could not finish layout", e);
		}
		
		taskMonitor.setProgress(1.0);
				
	}
	
	
	@Override
	public void cancel(){
		this.isCanceled = true;
		if(parser != null){
			parser.cancel();
		}
	}

}
