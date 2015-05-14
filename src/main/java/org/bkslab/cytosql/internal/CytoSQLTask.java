package org.bkslab.cytosql.internal;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTableUtil;
import org.cytoscape.task.AbstractNetworkViewTask;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.TaskMonitor;





public class CytoSQLTask extends AbstractNetworkViewTask {

	public CytoSQLTask(CyNetworkView view) {
		super(view);
	}

	@Override
	public void run(TaskMonitor taskMonitor)  {

	}
	
}