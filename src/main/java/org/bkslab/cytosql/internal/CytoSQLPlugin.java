package islab.ppi;

import islab.ppi.gui.CytoSQLApp;

import java.awt.event.ActionEvent;
import java.sql.ResultSet;

import cytoscape.Cytoscape;
import cytoscape.plugin.CytoscapePlugin;
import cytoscape.util.CytoscapeAction;

// This comes form the Cytoscape plugin development template
// CytoSQLApp is our starting point


/**
 * This cytoscape plugin makes a connection with an SQL database,
 * in order to enable a query that can be converted into a network.
 */
public class CytoSQLPlugin extends CytoscapePlugin {

	/**
	 * This constructor creates an action and adds it to the Plugins menu.
	 */
	public CytoSQLPlugin() {
		// create a new action to respond to menu activation
		CytoSQLAction action = new CytoSQLAction();
		// set the preferred menu
		action.setPreferredMenu("Plugins");
		// and add it to the menus
		Cytoscape.getDesktop().getCyMenus().addAction(action);
	}

	/**
	 * This class gets attached to the menu item.
	 */
	public class CytoSQLAction extends CytoscapeAction {

		private static final long serialVersionUID = 1L;

		ResultSet rs = null;

		// Connection conn = null;

		// String url =
		// "jdbc:mysql://localhost:8889/";//"jdbc:mysql://struisvogel.cmi.ua.ac.be/";
		//
		// String dbName = "ppi";//"PPI_At_gdj1_v2";
		//
		// String driver = "com.mysql.jdbc.Driver";
		//
		// String userName = "root";//"kimh";
		//
		// String password = "root";//"luna";

		/**
		 * The constructor sets the text that should appear on the menu item.
		 */
		public CytoSQLAction() {
			super("CytoSQL");
		}

		/**
		 * This method is called when the user selects the menu item:
		 * MyCytoSQLApp is called.
		 */
		public void actionPerformed(ActionEvent ae) {
			
			final CytoSQLApp u = new CytoSQLApp();

			java.awt.EventQueue.invokeLater(new Runnable() {
				public void run() {
					u.setVisible(true);
				}
			});

		}

	}

}
