package org.bkslab.cytosql.internal.gui;

import org.bkslab.cytosql.internal.gui.SQLTextArea.ColoredThread;
import org.bkslab.cytosql.internal.prefs.DBConnectionInfo;
import org.bkslab.cytosql.internal.prefs.QueryFavorites;
import org.bkslab.cytosql.internal.prefs.QueryHistory;
import org.bkslab.cytosql.internal.query.AlgoClass;
import org.bkslab.cytosql.internal.query.ExpandNetworkQuery;
import org.bkslab.cytosql.internal.query.UpdateEdgeAttributesQuery;
import org.bkslab.cytosql.internal.query.UpdateNodeAttributesQuery;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.BorderLayout;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;//Hai DNV
import java.util.Map;
import java.awt.Color;
import java.awt.event.MouseWheelListener;
import java.awt.event.MouseWheelEvent;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JWindow;
import javax.swing.ListSelectionModel;
import javax.swing.WindowConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelListener;
import javax.swing.plaf.basic.BasicComboBoxRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.JToolTip;
import javax.swing.ToolTipManager;


import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.Enumeration;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.plaf.metal.MetalToolTipUI;

//import javax.swing.table.TableModel;

import com.sun.org.apache.bcel.internal.generic.ALOAD;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import cytoscape.Cytoscape;

//import apple.awt.CToolkit;

/**
 * This code was edited or generated using CloudGarden's Jigloo SWT/Swing GUI
 * Builder, which is free for non-commercial use. If Jigloo is being used
 * commercially (ie, by a corporation, company or business for any purpose
 * whatever) then you should purchase a license for each developer using Jigloo.
 * Please visit www.cloudgarden.com for details. Use of Jigloo implies
 * acceptance of these licensing terms. A COMMERCIAL LICENSE HAS NOT BEEN
 * PURCHASED FOR THIS MACHINE, SO JIGLOO OR THIS CODE CANNOT BE USED LEGALLY FOR
 * ANY CORPORATE OR COMMERCIAL PURPOSE.
 */
public class CytoSQLApp extends JFrame {

  private static final String HELP_SQL_URL = "http://www.ptools.ua.ac.be/CytoSQL/help";
  // This is the main CytoSQL GUI Class

  private JButton runButton;
  private JComboBox CBFavorites;
  private JComboBox CBHistory;
  private JButton adjustButton;
  private JButton closeButton;

  private JLabel sqlLabel;
  private JSeparator separator2;
  private JLabel helpLabelSQL;
  private JScrollPane sqScrollPane;
  private JButton updateEdgeAttrButton;
  private JButton addAttrButton;
  private JTable previewSQLresult;
  private JSeparator jSeparator2;
  private JLabel jLabel3;
  private JSeparator jSeparator1;
  private JLabel jLabel2;
  private JLabel jLabel1;
  private JScrollPane previewSQLScrollPane;
  private JLabel jLabel5;
  private JLabel jLabel4;
  private JButton launchButton;
  private JButton expandButton;

//  private JTextArea sqlText;
  private SQLTextArea sqlText;
  private JSeparator separator;

  private boolean dotPressed = false;

  JWindow tableMenu = new JWindow();
  private JCheckBox chkAutoCompletion;
  JWindow colMenu = new JWindow();
  JList tables = new JList();
  JList cols = new JList();
  JScrollPane tableScrollPane = new JScrollPane(tables);
  JScrollPane colScrollPane = new JScrollPane(cols);
  DefaultListModel tablesModel = new DefaultListModel();
  DefaultListModel colsModel = new DefaultListModel();
  Thread t=null;
  
  private static Vector<String> tooltips=new Vector<String>();
  private static int selectedOperation;
  private static Map<String, ResultSet> SQLResult;
  {
    // Set Look & Feel
    try {
      javax.swing.UIManager.setLookAndFeel(javax.swing.UIManager
          .getSystemLookAndFeelClassName());
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public CytoSQLApp() {
    super();
    initGUI();
//    t=new initializeThread();
/**    
    new Thread() {
        public void run() {
          initialize(); //Pop-up menu containing "table_name" and "column_names" for auto-completion for SQL Query.          
          try {
            sleep(500);
          }
          catch (InterruptedException ex) {
          }
          sqlText.requestFocus();
        }
      }.start();
*/      
  }

  private void initGUI() {

    sqlLabel = new JLabel();
    runButton = new JButton();
    adjustButton = new JButton();
    closeButton = new JButton();
    separator = new JSeparator();

    getContentPane().setLayout(null);
    setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    this.setTitle("CytoSQL");
    this.setResizable(false);
    this.addWindowListener(new WindowAdapter() {
      public void windowClosed(WindowEvent evt) {
        thisWindowClosed(evt);
      }
    });
    {
      CBFavorites = new JComboBox();
      getContentPane().add(CBFavorites);
      favoritesChanged();
      CBFavorites.setBounds(12, 255, 448, 25);
      CBFavorites.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent evt) {
          CBFavoritesActionPerformed(evt);
        }
      });
    }
    {
      CBHistory = new JComboBox();
//      CBHistory.setRenderer(new MyComboBoxRenderer()); //Replace the default Render object to draw the selected items.
 
      getContentPane().add(CBHistory);
      getContentPane().add(runButton);
      getContentPane().add(sqlLabel);
      getContentPane().add(separator);
      getContentPane().add(closeButton);
      getContentPane().add(adjustButton);
      getContentPane().add(getSeparator2());
      getContentPane().add(getExpandButton());
      getContentPane().add(getAddAttrButton());
      getContentPane().add(getUpdateEdgeAttrButton());
      
//      getContentPane().add(getSqScrollPane());
      getContentPane().add(getSQLTextArea());
      
      getContentPane().add(getHelpLabelSQL());
      getContentPane().add(getlaunchButton());
      getContentPane().add(getPreviewSQLScrollPane(), BorderLayout.CENTER);
      getContentPane().add(getJLabel1());
      getContentPane().add(getJLabel2());
      getContentPane().add(getJSeparator1());
      getContentPane().add(getJLabel3());
      {
    	  getContentPane().add(getJSeparator2());
    	  getContentPane().add(getJLabel4());
    	  getContentPane().add(getJLabel5());
    	  getContentPane().add(getChkAutoCompletion());
      }
      //      getContentPane().add(getPreviewSQLresult());
      separator.setBounds(6, 371, 923, 12);
//      sqlText.setBounds(13, 80, 916, 159); Temporarily deleted on 12 Mar 2010
//      sqlText.setLineWrap(true); Temporarily deleted on 12 Mar 2010
      sqlText.setFont(new java.awt.Font("Monospaced", 0, 11));
      sqlText.setBounds(13, 80, 916, 159);
      sqlText.setText("");

 //     sqlText.addFocusListener(new CytoSQLApp_editor_focusAdapter(this));
 //     sqlText.addMouseListener(new CytoSQLApp_editor_mouseAdapter(this));
      sqlText.addKeyListener(new CytoSQLApp_editor_keyAdapter(this));

      
      CBHistory.setModel(initCBHistory());
      CBHistory.setBounds(481, 255, 448, 25);
      CBHistory.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent evt) {
          CBHistoryActionPerformed(evt);
        }
      });
    }

    sqlLabel.setText("2. Enter SQL query:");
    sqlLabel.setBounds(15, 49, 348, 22);

    runButton.setText("Create Network");
    runButton.setBounds(12, 338, 222, 25);
    runButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        runButtonActionPerformed(evt);
      }
    });
    adjustButton.setText("database settings");
    adjustButton.setBounds(143, 9, 164, 25);
    adjustButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(final java.awt.event.ActionEvent evt) {
        adjustButtonActionPerformed(evt);
      }
    });
    closeButton.setText("Close");
    closeButton.setBounds(835, 615, 95, 25);
    closeButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        closeButtonActionPerformed(evt);
      }
    });
    pack();
    this.setSize(942, 670);
  }

  public void favoritesChanged() {
    CBFavorites.setModel(initCBFavorites());
  }

  private ComboBoxModel initCBFavorites() {
    String[] favs = QueryFavorites.getInstance().getFavorites();
    String[] items = new String[favs.length + 3];
    items[0] = "Select query from favorites...";
    items[1] = "Save to favorites...";
    items[2] = "Search & Edit favorites...";
    System.arraycopy(favs, 0, items, 3, favs.length);
    DefaultComboBoxModel m = new DefaultComboBoxModel(items);
    return m;
  }

  private ComboBoxModel initCBHistory() {
    String[] hist = QueryHistory.getInstance().toArray();
    String[] items = new String[hist.length + 3];
    items[0] = "Select query from history...";
    items[1] = "Clear history...";
    items[2] = "Search & Edit history";
    System.arraycopy(hist, 0, items, 3, hist.length);
    
    for(int i=0; i<items.length; i++)
    	tooltips.add(items[i]);
    
    DefaultComboBoxModel m = new DefaultComboBoxModel(items);
    return m;
  }
  public void runButtonActionPerformed(final java.awt.event.ActionEvent evt) {

    final String query = sqlText.getText();
    System.out.println(query);
 
    // final QueryPreviewDialog q = new QueryPreviewDialog(this, query);
    // q.setVisible(true);
    
    
    setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

    Thread t = new Thread(new Runnable() {
      public void run() {
        QueryProgress qp = new QueryProgress();
        SQLResult=AlgoClass.processQuery(query);
        previewSQLResult(SQLResult);
        selectedOperation=0; //CREATE NETWORK
        jLabel5.setText("CREATE_NETWORK OPERATION WAS SELECTED");
        qp.setFinished();
      }
    });
    t.start();
    setCursor(null);

    updateHistory(query);
  }

  private void expandButtonActionPerformed(ActionEvent evt) {
    final String query = sqlText.getText();
    System.out.println(query);

    if (!query.contains("?")) {
      JOptionPane.showMessageDialog(null,
          "SQL query needs at least one bind variable...");
      throw new RuntimeException();
    }
    setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
    new Thread(new Runnable() {
      public void run() {
    	Map<Integer, String> bindvarMap=ExpandNetworkQuery.bindvarMapInitialize(query);
    	QueryProgress qp = new QueryProgress();
        SQLResult=AlgoClass.processBatchQuery(query, bindvarMap, AlgoClass.BatchType.EXPAND);
        previewSQLResult(SQLResult);
        selectedOperation=1; //EXPAND_NETWORK
        jLabel5.setText("EXPAND_NETWORK_OPERATION WAS SELECTED");
        qp.setFinished();
      }
    }).start();
    setCursor(null);
    updateHistory(query);
  }

  private void addAttrButtonActionPerformed(ActionEvent evt) {
    final String query = sqlText.getText();
    System.out.println(query);

    if (!query.contains("?")) {
      JOptionPane.showMessageDialog(null,
          "SQL query needs at least one bind variable...");
      throw new RuntimeException();
    }
    setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
    new Thread(new Runnable() {
      public void run() {
      	Map<Integer, String> bindvarMap=UpdateNodeAttributesQuery.bindvarMapInitialize(query);
    	QueryProgress qp = new QueryProgress();
        SQLResult=AlgoClass
            .processBatchQuery(query, bindvarMap, AlgoClass.BatchType.UPDATE_NODE_ATTR);
        previewSQLResult(SQLResult);
        selectedOperation=2; //UPDATE_NODE_ATTR
        jLabel5.setText("UPDATE_NODE_ATTRIBUTES OPERATION WAS SELECTED");
        qp.setFinished();
      }
    }).start();
    setCursor(null);
    updateHistory(query);
  }

  private void updateEdgeAttrButtonActionPerformed(ActionEvent evt) {
    final String query = sqlText.getText();
    System.out.println(query);

    if (!query.contains("?")) {
      JOptionPane.showMessageDialog(null,
          "SQL query needs at least one bind variable...");
      throw new RuntimeException();
    }
    setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
    new Thread(new Runnable() {
      public void run() {
      	Map<Integer, String[]> bindvarMap=UpdateEdgeAttributesQuery.bindvarMapInitialize(query);
        QueryProgress qp = new QueryProgress();
    	SQLResult=AlgoClass
            .processBatchQuery(query, bindvarMap, AlgoClass.BatchType.UPDATE_EDGE_ATTR);
        previewSQLResult(SQLResult);
        selectedOperation=3; //UPDATE_EDGE_ATTR
        jLabel5.setText("UPDATE_EDGE_ATTRIBUTES OPERATION WAS SELECTED");
        qp.setFinished();
      }
    }).start();
    setCursor(null);
    updateHistory(query);
  }
  private void updateHistory(String query) {
    QueryHistory.getInstance().add(query);
    CBHistory.setModel(initCBHistory());
    QueryHistory.getInstance().saveHistoryToFile();
  }

  private void previewSQLResult(Map<String, ResultSet> previewResult){
	  Vector<String> columnNames=new Vector<String>();
	  Vector<Vector<Object>> rowsData=new Vector<Vector<Object>>();
	  boolean isAllColumns=false;
	  for(String key: previewResult.keySet()){
          try {
    		  ResultSet rs=previewResult.get(key);
    		  int columnCnt=rs.getMetaData().getColumnCount();
//    		  JOptionPane.showMessageDialog(null, key+":"+rs);
    		  int columnNumbers=0;
        	  columnNumbers=rs.getMetaData().getColumnCount();
        	  if(isAllColumns==false){
	        	  for(int i=1; i<=columnNumbers;i++)
	        		  columnNames.addElement(rs.getMetaData().getColumnLabel(i));
        	  }
        	  isAllColumns=true;
        	  while (rs.next()) {
        		  Vector<Object> row=new Vector<Object>();
            	  for(int i=1;i<=columnNumbers;i++)
            		  row.addElement(rs.getObject(i));
            	  rowsData.addElement(row);
              }
        	  rs.beforeFirst();
//       	  break; //Show only for the first selected Node or Edge updated
          }catch (SQLException e) {
    		  JOptionPane.showMessageDialog(null, "Database Error in CytoSQLApp:\n" + e);
          }
	  }
	  getPreviewSQLresult().setModel(new DefaultTableModel(rowsData, columnNames));
  }
  private void launchButtonActionPerformed(ActionEvent evt){
	  try {	    	
		  switch (selectedOperation) {
		  case 0:
			  AlgoClass.createNetwork(SQLResult);
			  break;
		  case 1:
			  ExpandNetworkQuery.expandNetwork(SQLResult);
			  break;
		  case 2:
			  UpdateNodeAttributesQuery.updateNodeAttributes(SQLResult);
			  break;
		  case 3:
			  UpdateEdgeAttributesQuery.updateEdgeAttribute(SQLResult);
			  break;
		  default:
			  JOptionPane.showMessageDialog(null, "Please choose one operation to execute");
			  break;
		  }
	  }catch(SQLException e){
		  JOptionPane.showMessageDialog(null, "Database Error:\n" + e.getMessage());
	  }catch (Exception e) {
		  throw new RuntimeException(e);
	  }finally{
		  selectedOperation=-1;
		  jLabel5.setText("NO OPERATION WAS SELECTED");
		  try{
			  AlgoClass.getConnection().close();
		  }catch(SQLException e){
			  JOptionPane.showMessageDialog(null, "Couldn't close database connection:\n" + e.getMessage());
		  }catch(Exception e){
			  JOptionPane.showMessageDialog(null, "Couldn't close database connection:\n" + e.getMessage());
		  }
	  }
	  
/**
	  //TODO Hai DNV: preview the SQL statement's result and store it into a JTable component
	  final String query=sqlText.getText();
	  Connection conn = null;
	  Vector<String> columnNames=new Vector<String>();
	  Vector<Vector<Object>> rowsData=new Vector<Vector<Object>>();
	  {//Make a connection to database
		    Properties defaultProps = null;
		    defaultProps = DBConnectionInfo.getCurrentConnection();
		    System.out.println(defaultProps.getProperty("driver").trim() + "\n"
		        + defaultProps.getProperty("url").trim() + "\n"
		        + defaultProps.getProperty("dbname").trim() + "\n"
		        + defaultProps.getProperty("username").trim() + "\n"
		        + defaultProps.getProperty("password").trim());
		    // try to make the connection with the specifications
		    try {
		      try {
		          Class.forName(defaultProps.getProperty("driver").trim()).newInstance();
		        } catch (InstantiationException e) {
		          JOptionPane.showMessageDialog(null,
		              "Instantiation exception for connection to DB.\n" + e);
		        } catch (IllegalAccessException e) {
		          JOptionPane.showMessageDialog(null, "Cannot access DB connection.\n" + e);
		        } catch (ClassNotFoundException e) {
		          JOptionPane.showMessageDialog(null, "Driver class not found.\n" + e);
		        }
		        conn = DriverManager.getConnection(defaultProps.getProperty("url").trim() + "/" + defaultProps.getProperty("dbname").trim(),
		        		defaultProps.getProperty("username").trim(), defaultProps.getProperty("password").trim());
		        System.out.println("Connection = " + conn);

		        System.out.println("connection is:" + conn);
		    }catch (Exception e) {
		    	JOptionPane.showMessageDialog(null, "Couldn't create connection.\n" + e);
		    	e.printStackTrace();
		    	throw new RuntimeException(e);
		    }
	  }//End of making a connection to database
	  try{
		  ResultSet rs = null;
		  if(!query.contains("?")){ //Build up a new network
			    Statement st = conn.createStatement();
			    rs = st.executeQuery(query);
		  }else{
			    Set<CyNode> selectedNodes = (Set<CyNode>)Cytoscape.getCurrentNetwork()
		        .getSelectedNodes();
			    if (selectedNodes.isEmpty()) {
			    	Cytoscape.getCurrentNetwork().selectAllNodes();
			    	selectedNodes = Cytoscape.getCurrentNetwork().getSelectedNodes();
			    }
			  	if(!selectedNodes.isEmpty()){
				  	Object[] actionPoss=new Object[]{"Expand Network","Update Edge Attributes","Update Node Attributes"};
				    String actionType = (String)JOptionPane.showInputDialog(Cytoscape.getDesktop(),
				            "Please choose one action type you want to do with the above query", "Define action type",
				            JOptionPane.PLAIN_MESSAGE, null, actionPoss, actionPoss[0]);
				    if(actionType.equals("Expand Network")){
				    	rs=ExpandNetworkQuery.getSQLResultSet4oneSelectedNode(query, conn);
				    }else if(actionType.equals("Update Edge Attributes")){
				    	rs=UpdateEdgeAttributesQuery.getSQLResultSet4oneSelectedEdge(query, conn);
				    }else if(actionType.equals("Update Node Attributes")){
				    	rs=UpdateNodeAttributesQuery.getSQLResultSet4oneSelectedNode(query, conn);
				    }
			  	}else{
			  		JOptionPane.showMessageDialog(null, "This query is only valid on a specific network");
			  	}
		  }
		  int columnNumbers=0;rs.getMetaData().getColumnCount();
          try {
        	  columnNumbers=rs.getMetaData().getColumnCount();
        	  for(int i=1; i<=columnNumbers;i++)
        		  columnNames.addElement(rs.getMetaData().getColumnLabel(i));
        	  while (rs.next()) {
            	  Vector<Object> row=new Vector<Object>();
            	  for(int i=1;i<=columnNumbers;i++)
            		  row.addElement(rs.getObject(i));
            	  rowsData.addElement(row);
              }
          }finally {
              rs.close();
          }
	  }catch (SQLException e) {
		  JOptionPane.showMessageDialog(null, "Database Error:\n" + e.getMessage());
		  throw new RuntimeException(e);
	  }finally {
	      try {
	          // System.out.println(conn);
	          conn.close();
	          System.out.println("### Connection closed");
	        } catch (SQLException e) {
	          JOptionPane.showMessageDialog(null, "Couldn't close DB connection");
	        } catch (Exception e) {
	          JOptionPane.showMessageDialog(null, "Couldn't close DB connection");
	      }
	  }
	  getPreviewSQLresult().setModel(new DefaultTableModel(rowsData, columnNames));
*/	  
  }
  /*
   * calls DbSpecs class
   */
  public void adjustButtonActionPerformed(final java.awt.event.ActionEvent evt) {
    final DbSpecs dbs = new DbSpecs(this);
    java.awt.EventQueue.invokeLater(new Runnable() {
      public void run() {
        dbs.setVisible(true);
      }
    });
  }
	
  private void CBHistoryActionPerformed(ActionEvent evt) {
    switch (CBHistory.getSelectedIndex()) {
      case 0: // select -- do nothing
        break;
      case 1: // clear -- remove all
        QueryHistory.getInstance().clearHistory();
        QueryHistory.getInstance().saveHistoryToFile();
        CBHistory.setModel(initCBHistory());
        break;
      case 2: // search -- show search frame
          java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
              new SearchHistoryFrames(CytoSQLApp.this).setVisible(true);
            }
          });
          break;
      default:
        sqlText.setText((String)CBHistory.getSelectedItem());
    }
    CBHistory.setSelectedIndex(0);
  }

  private void thisWindowClosed(WindowEvent evt) {
    QueryHistory.getInstance().saveHistoryToFile();
  }

  private void CBFavoritesActionPerformed(ActionEvent evt) {
    switch (CBFavorites.getSelectedIndex()) {
      case 0: // select -- do nothing
        break;
      case 1: // save -- add to favorites
        QueryFavorites.getInstance().addToFavorites((String)sqlText.getText());
        QueryFavorites.getInstance().saveFavoritesToFile();
        favoritesChanged();
        break;
      case 2: // edit -- show edit frame
        java.awt.EventQueue.invokeLater(new Runnable() {
          public void run() {
            new EditFavoritesFrame(CytoSQLApp.this).setVisible(true);
          }
        });
        break;
      default:
        sqlText.setText((String)CBFavorites.getSelectedItem());
        break;
    }
    CBFavorites.setSelectedIndex(0);
  }

  private void closeButtonActionPerformed(ActionEvent evt) {
    this.dispose();
  }
  private JSeparator getSeparator2() {
    if (separator2 == null) {
      separator2 = new JSeparator();
      separator2.setBounds(12, 292, 917, 12);
    }
    return separator2;
  }

  private JButton getExpandButton() {
    if (expandButton == null) {
      expandButton = new JButton();
      expandButton.setText("Expand network");
      expandButton.setBounds(246, 336, 222, 29);
      expandButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent evt) {
          expandButtonActionPerformed(evt);
        }
      });
    }
    return expandButton;
  }

  private JButton getAddAttrButton() {
    if (addAttrButton == null) {
      addAttrButton = new JButton();
      addAttrButton.setText("Update node attributes");
      addAttrButton.setBounds(480, 336, 222, 29);
      addAttrButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent evt) {
          addAttrButtonActionPerformed(evt);
        }
      });
    }
    return addAttrButton;
  }

  private JButton getUpdateEdgeAttrButton() {
    if (updateEdgeAttrButton == null) {
      updateEdgeAttrButton = new JButton();
      updateEdgeAttrButton.setText("Update edge attributes");
      updateEdgeAttrButton.setBounds(714, 336, 222, 29);
      updateEdgeAttrButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent evt) {
          updateEdgeAttrButtonActionPerformed(evt);
        }
      });
    }
    return updateEdgeAttrButton;
  }
/** Temporarily deleted on 12 Mar 2010
  private JScrollPane getSqScrollPane() {
    if (sqScrollPane == null) {
      sqScrollPane = new JScrollPane();
      sqScrollPane.setBounds(13, 80, 916, 163);
      {
    	  sqlText = new JTextArea();
          sqScrollPane.setViewportView(sqlText);
    	  sqlText.setColumns(20);
    	  sqlText.setRows(5);
      }
    }
    return sqScrollPane;
  }
 */
  private SQLTextArea getSQLTextArea() {
	    if (sqlText == null) {
	    	  sqlText = new SQLTextArea();
	      }
	    return sqlText;
	  }
  public SQLTextArea getSQLText(){
	  return sqlText;
  }
  private JLabel getHelpLabelSQL() {
    if (helpLabelSQL == null) {
      ToolTipManager.sharedInstance().setDismissDelay(9000);
      helpLabelSQL = new JLabel(){
    	  @Override
          public JToolTip createToolTip() {
              JScrollableToolTip tip = new JScrollableToolTip(400, 100, this);
              tip.setComponent(this);
              return tip;
          }
      };
      helpLabelSQL.setToolTipText("For help, just click on the \"help\" or visit the following address:"+HELP_SQL_URL);
      helpLabelSQL.setText("help");
      helpLabelSQL.setBounds(900, 9, 30, 16);
      helpLabelSQL.setForeground(new java.awt.Color(0, 0, 255));
      helpLabelSQL.setFont(new java.awt.Font("Lucida Grande", 1, 13));
      helpLabelSQL.addMouseListener(new MouseAdapter() {
        public void mouseExited(MouseEvent evt) {
          setCursor(null);
        }

        public void mouseEntered(MouseEvent evt) {
          setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }

        public void mouseClicked(MouseEvent evt) {
          cytoscape.util.OpenBrowser.openURL(HELP_SQL_URL);
        }
      });
    }
    return helpLabelSQL;
  }
  
  public JButton getlaunchButton() {
	  if(launchButton == null) {
		  launchButton = new JButton();
		  launchButton.setText("import");
		  launchButton.setBounds(137, 613, 159, 29);
		  launchButton.addActionListener(new ActionListener() {
	          public void actionPerformed(ActionEvent evt) {
	        	  launchButtonActionPerformed(evt);
	          }
	        });

	  }
	  return launchButton;
  }
  
  private JTable getPreviewSQLresult() {
	  if(previewSQLresult == null) {
		  previewSQLresult = new JTable();
	  }
	  return previewSQLresult;
  }
  
  private JScrollPane getPreviewSQLScrollPane() {
	  if(previewSQLScrollPane == null) {
		  previewSQLScrollPane = new JScrollPane(getPreviewSQLresult());
//		  getPreviewSQLresult().setFillsViewportHeight(true);
		  previewSQLScrollPane.setBounds(12, 423, 917, 162);
	  }
	  return previewSQLScrollPane;
  }
  
  private JLabel getJLabel1() {
	  if(jLabel1 == null) {
		  jLabel1 = new JLabel();
		  jLabel1.setText("3. Select one Operation:");
		  jLabel1.setBounds(12, 310, 159, 22);
	  }
	  return jLabel1;
  }
  
  private JLabel getJLabel2() {
	  if(jLabel2 == null) {
		  jLabel2 = new JLabel();
		  jLabel2.setText("4. SQL result preview:");
		  jLabel2.setBounds(12, 389, 141, 22);
	  }
	  return jLabel2;
  }
  
  private JSeparator getJSeparator1() {
	  if(jSeparator1 == null) {
		  jSeparator1 = new JSeparator();
		  jSeparator1.setBounds(12, 597, 918, 12);
	  }
	  return jSeparator1;
  }
  
  private JLabel getJLabel3() {
	  if(jLabel3 == null) {
		  jLabel3 = new JLabel();
		  jLabel3.setText("5. Import results:");
		  jLabel3.setBounds(12, 615, 113, 22);
	  }
	  return jLabel3;
  }
  
  private JSeparator getJSeparator2() {
	  if(jSeparator2 == null) {
		  jSeparator2 = new JSeparator();
		  jSeparator2.setBounds(15, 37, 917, 12);
	  }
	  return jSeparator2;
  }
  
  private JLabel getJLabel4() {
	  if(jLabel4 == null) {
		  jLabel4 = new JLabel();
		  jLabel4.setText("1. Define database:");
		  jLabel4.setBounds(15, 9, 194, 22);
	  }
	  return jLabel4;
  }
  
  private JLabel getJLabel5() {
	  if(jLabel5 == null) {
		  jLabel5 = new JLabel();
		  jLabel5.setText("NO OPERATION WAS SELECTED");
		  jLabel5.setBounds(169, 310, 753, 22);
	  }
	  return jLabel5;
  }

/** For customized multilined and scrollable Tooltips- But it doesn't work
  class MyComboBoxRenderer extends BasicComboBoxRenderer {
	    public Component getListCellRendererComponent(JList list, Object value,
	        int index, boolean isSelected, boolean cellHasFocus) {
	      if (isSelected) {
	        setBackground(list.getSelectionBackground());
	        setForeground(list.getSelectionForeground());
	        if (-1 < index) {
	          list.setToolTipText((String)(tooltips.toArray()[index]));
	        }
	      } else {
	        setBackground(list.getBackground());
	        setForeground(list.getForeground());
	      }
	      setFont(list.getFont());
	      setText((value == null) ? "" : value.toString());
	      return this;
	    }
	  }
*/
  public class JScrollableToolTip extends JToolTip implements MouseWheelListener{
	    private JTextArea tipArea;
	    private JScrollPane scrollpane;
	    private JComponent comp;
	 
	    /** Creates a tool tip. */
	    public JScrollableToolTip(final int width, final int height) {
	        this(width, height, null);
	    }
	 
	    private JScrollableToolTip(final int width, final int height, final JComponent comp) {
	        this.comp = comp;
	        setPreferredSize(new Dimension(width, height));
	        setLayout(new BorderLayout());
	        tipArea = new JTextArea();
	        tipArea.setLineWrap(true);
	        tipArea.setWrapStyleWord(true);
	        tipArea.setEditable(false);
	        tipArea.setBackground(new Color(255, 255, 204));
	        scrollpane = new JScrollPane(tipArea);
	        add(scrollpane);
	        if(comp != null){
	            comp.addMouseWheelListener(this);
	        }
	    }
	 
	    public void mouseWheelMoved(final MouseWheelEvent e) {
	        scrollpane.dispatchEvent(e);
	        MouseEvent e2 = new MouseEvent(comp, MouseEvent.MOUSE_MOVED, 0, 0, 0, 0, 0, false);
	        comp.dispatchEvent(e2);
	    }
	 
	    @Override
	    public void setTipText(final String tipText) {
	        String oldValue = this.tipArea.getText();
	        tipArea.setText(tipText);
	        tipArea.setCaretPosition(0);
	        firePropertyChange("tiptext", oldValue, tipText);
	    }
	 
	    @Override
	    public String getTipText() {
	        return tipArea == null ? "" : tipArea.getText();
	    }
	 
	    @Override
	    protected String paramString() {
	        String tipTextString = (tipArea.getText() != null ? tipArea.getText() : "");
	 
	        return super.paramString() +
	                ",tipText=" + tipTextString;
	    }
  }
  
/**
  class MultiLineToolTip extends JToolTip {
	  public MultiLineToolTip() {
	    setUI(new MultiLineToolTipUI());
	  }
	}

	class MultiLineToolTipUI extends MetalToolTipUI {
	  private String[] strs;

	  private int maxWidth = 0;

	  public void paint(Graphics g, JComponent c) {
	    FontMetrics metrics = Toolkit.getDefaultToolkit().getFontMetrics(
	        g.getFont());
	    Dimension size = c.getSize();
	    g.setColor(c.getBackground());
	    g.fillRect(0, 0, size.width, size.height);
	    g.setColor(c.getForeground());
	    if (strs != null) {
	      for (int i = 0; i < strs.length; i++) {
	        g.drawString(strs[i], 3, (metrics.getHeight()) * (i + 1));
	      }
	    }
	  }

	  public Dimension getPreferredSize(JComponent c) {
	    FontMetrics metrics = Toolkit.getDefaultToolkit().getFontMetrics(
	        c.getFont());
	    String tipText = ((JToolTip) c).getTipText();
	    if (tipText == null) {
	      tipText = "";
	    }
	    BufferedReader br = new BufferedReader(new StringReader(tipText));
	    String line;
	    int maxWidth = 0;
	    Vector v = new Vector();
	    try {
	      while ((line = br.readLine()) != null) {
	        int width = SwingUtilities.computeStringWidth(metrics, line);
	        maxWidth = (maxWidth < width) ? width : maxWidth;
	        v.addElement(line);
	      }
	    } catch (IOException ex) {
	      ex.printStackTrace();
	    }
	    int lines = v.size();
	    if (lines < 1) {
	      strs = null;
	      lines = 1;
	    } else {
	      strs = new String[lines];
	      int i = 0;
	      for (Enumeration e = v.elements(); e.hasMoreElements(); i++) {
	        strs[i] = (String) e.nextElement();
	      }
	    }
	    int height = metrics.getHeight() * lines;
	    this.maxWidth = maxWidth;
	    return new Dimension(maxWidth + 6, height + 4);
	  }
	}
	*/
  class CytoSQLApp_editor_keyAdapter extends java.awt.event.KeyAdapter {
	  CytoSQLApp adaptee;

	  CytoSQLApp_editor_keyAdapter(CytoSQLApp adaptee) {
	    this.adaptee = adaptee;
	  }

	  public void keyPressed(KeyEvent e) {
		  if(chkAutoCompletion.isSelected())
			  adaptee.editor_keyPressed(e);
	  }
  
	  public void keyTyped(KeyEvent e) {
		  if(chkAutoCompletion.isSelected())
			  adaptee.editor_keyTyped(e);
	  }

	}
/**
	class CytoSQLApp_editor_mouseAdapter extends java.awt.event.MouseAdapter {
		CytoSQLApp adaptee;

		CytoSQLApp_editor_mouseAdapter(CytoSQLApp adaptee) {
	    this.adaptee = adaptee;
	  }
	  public void mouseClicked(MouseEvent e) {
	    adaptee.editor_mouseClicked(e);
	  }
	}
*/	
  /**
	  void editor_mouseClicked(MouseEvent e) {
		    tableMenu.setVisible(false);
		    colMenu.setVisible(false);

		    if (SwingUtilities.isRightMouseButton(e)) {
		      // view popup menu...
		      JPopupMenu menu = new JPopupMenu();
		      JMenuItem formatCodeOnOneRowMenu = new JMenuItem(Options.getInstance().getResource("format sql on one row"));
		      formatCodeOnOneRowMenu.addActionListener(new ActionListener() {
		        public void actionPerformed(ActionEvent ev) {
		          try {
		            String sql = sqlText.getText();
		            int pos = 0;
		            while (pos < sql.length()) {
		              if (sql.charAt(pos) == '\n') {
		                if (pos < sql.length() - 1) {
		                  sql = sql.substring(0, pos) + sql.substring(pos + 1);
		                }
		                else {
		                  sql = sql.substring(0, pos);
		                }
		              }
		              else {
		                pos++;
		              }
		            }
		            sqlText.setText(sql);
		          }
		          catch (Exception ex) {
		            ex.printStackTrace();
		          }
		        }
		      });
		      JMenuItem formatCodeOnMoreRowsMenu = new JMenuItem(Options.getInstance().getResource("format sql on more rows"));
		      formatCodeOnMoreRowsMenu.addActionListener(new ActionListener() {
		        public void actionPerformed(ActionEvent ev) {
		          try {
		            String sql = sqlText.getText();
		            int count = 0;
		            int pos = 0;
		            int apixCount = 0;
		            while (pos < sql.length()) {
		              if (sql.charAt(pos) == '\'') {
		                apixCount++;
		                if (pos>0 && sql.charAt(pos-1)=='\'')
		                  apixCount = apixCount -2;
		              }
		              if (count>80) {
		                if ((sql.charAt(pos)==' ' || sql.charAt(pos)==',') && apixCount%2==0) {
		                  count = 0;
		                  if (pos<sql.length()-1)
		                    sql = sql.substring(0,pos+1)+"\n"+sql.substring(pos+1);
		                  else
		                    sql = sql.substring(0,pos);
		                }
		              }
		              pos++;
		              count++;
		            }

		            sqlText.setText(sql);
		          }
		          catch (Exception ex) {
		            ex.printStackTrace();
		          }
		        }
		      });
		      menu.add(formatCodeOnOneRowMenu);
		      menu.add(formatCodeOnMoreRowsMenu);
		      menu.show(e.getComponent(),e.getX(), e.getY());
		    }
		  }
	  */
	  void editor_keyTyped(KeyEvent e) {
		    if (colMenu.isVisible())
		      e.consume();
		    else if (tableMenu.isVisible())
		      e.consume();
		  }
  

	  void editor_keyPressed(KeyEvent e) {
		    try {
		      dotPressed = false;
		      if (e.getKeyCode() == e.VK_F9) {
//		        executeButton_actionPerformed(null);
		      }
		      if (e.getKeyCode() == e.VK_SPACE && e.isControlDown() && !tableMenu.isVisible()) {
		        e.consume();
		        // view popup menu containing tables list...
/**		        
		        new Thread() {
		            public void run() {
//		              initialize(); //Pop-up menu containing "table_name" and "column_names" for auto-completion for SQL Query.
		              try {
		                sleep(500);
		              }
		              catch (InterruptedException ex) {
		              }
		              try{
		            	initialize(); //Pop-up menu containing "table_name" and "column_names" for auto-completion for SQL Query.
				        tableMenu.setLocation(
					            sqlText.modelToView(sqlText.getCaretPosition()).x,
					            sqlText.modelToView(sqlText.getCaretPosition()).y+150
					            );
				        tableMenu.setSize(300, 150);
				        tableMenu.setVisible(true);
				        tables.requestFocus();
				        tables.setSelectedIndex(0);
				        sqlText.requestFocus();
		              }catch(Exception ex){
		              }
		            }
		          }.start();
*/		          
		        tableMenu.setLocation(
		            sqlText.modelToView(sqlText.getCaretPosition()).x,
		            sqlText.modelToView(sqlText.getCaretPosition()).y+150
		            );
		        tableMenu.setSize(300, 150);
		        tableMenu.setVisible(true);
		        tables.requestFocus();
		        tables.setSelectedIndex(0);
		      }
		      else if (tableMenu.isVisible()) {
		        e.consume();
		        if (e.getKeyCode()==e.VK_UP && tables.getSelectedIndex()>0)
		          tables.setSelectedIndex(tables.getSelectedIndex()-1);
		        else if (e.getKeyCode()==e.VK_DOWN && tables.getSelectedIndex()<tablesModel.getSize()-1)
		          tables.setSelectedIndex(tables.getSelectedIndex()+1);
		        else if (e.getKeyCode()==e.VK_PAGE_UP) {
		          int i = tables.getSelectedIndex();
		          int w = tables.getLastVisibleIndex()-tables.getFirstVisibleIndex()+1;
		          i = i-w;
		          if (i<0)
		            i=0;
		          tables.setSelectedIndex(i);
		        }
		        else if (e.getKeyCode()==e.VK_PAGE_DOWN) {
		          int i = tables.getSelectedIndex();
		          int w = tables.getLastVisibleIndex()-tables.getFirstVisibleIndex()+1;
		          i = i+w;
		          if (i-1>tables.getModel().getSize())
		            i=tables.getModel().getSize()-1;
		          tables.setSelectedIndex(i);
		        }
		        else if (e.getKeyCode()==e.VK_ESCAPE) {
		          tableMenu.setVisible(false);
		          sqlText.requestFocus();
		        }
		        else if (e.getKeyCode()==e.VK_ENTER) {
		          int pos = sqlText.getCaretPosition();
		          sqlText.setText(
		        	sqlText.getText().substring(0,pos)+
		            tables.getSelectedValue()+
		            sqlText.getText().substring(pos)
		          );
		          tableMenu.setVisible(false);
		          sqlText.requestFocus();
		          sqlText.setCaretPosition(pos+tables.getSelectedValue().toString().length());
		        }
		        else {
		          for(int i=0;i<tables.getModel().getSize();i++)
		            if (tables.getModel().getElementAt(i).toString().toLowerCase().startsWith(String.valueOf(e.getKeyChar()).toLowerCase())) {
		              tables.setSelectedIndex(i);
		              break;
		            }
		          e.consume();
		        }
		      }
		      else if (e.getKeyChar() == '.' && !colMenu.isVisible()) {
		        dotPressed = true;
		        Thread t = new Thread() {
		          public void run() {
		            try {
		              sleep(500);
		            }
		            catch (InterruptedException ex) {
		            }
		            if (dotPressed) {
		              try {
		                // view popup menu containing columns list...
		                String text = sqlText.getText();
		                int start = 0;
		                while (start < text.length() &&
		                       text.indexOf(" ", start + 1) > -1) {
		                  if (text.indexOf(" ", start + 1) < sqlText.getCaretPosition()) {
		                    start = text.indexOf(" ", start + 1);
		                  }
		                  else {
		                    break;
		                  }
		                }
		                String table = text.substring(start, sqlText.getCaretPosition() - 1);
		                if (table.indexOf(",")!=-1)
		                  table = table.substring(table.indexOf(",")+1);
		                initCols(table);

		                colMenu.setLocation(
		                	sqlText.modelToView(sqlText.getCaretPosition()).x,
		                	sqlText.modelToView(sqlText.getCaretPosition()).y+150
		                    );
		                colMenu.setSize(300, 150);
		                colMenu.setVisible(true);
		                cols.requestFocus();
		                cols.setSelectedIndex(0);

		              }
		              catch (Exception ex) {
		              }
		            }
		          }
		        };
		        t.start();
		      }
		      else if (colMenu.isVisible()) {
		        e.consume();
		        if (e.getKeyCode()==e.VK_UP && cols.getSelectedIndex()>0)
		          cols.setSelectedIndex(cols.getSelectedIndex()-1);
		        else if (e.getKeyCode()==e.VK_DOWN && cols.getSelectedIndex()<colsModel.getSize()-1)
		          cols.setSelectedIndex(cols.getSelectedIndex()+1);
		        else if (e.getKeyCode()==e.VK_PAGE_UP) {
		          int i = cols.getSelectedIndex();
		          int w = cols.getLastVisibleIndex()-cols.getFirstVisibleIndex()+1;
		          i = i-w;
		          if (i<0)
		            i=0;
		          cols.setSelectedIndex(i);
		        }
		        else if (e.getKeyCode()==e.VK_PAGE_DOWN) {
		          int i = cols.getSelectedIndex();
		          int w = cols.getLastVisibleIndex()-cols.getFirstVisibleIndex()+1;
		          i = i+w;
		          if (i-1>cols.getModel().getSize())
		            i=cols.getModel().getSize()-1;
		          cols.setSelectedIndex(i);
		        }
		        else if (e.getKeyCode()==e.VK_ESCAPE) {
		          colMenu.setVisible(false);
		          sqlText.requestFocus();
		        }
		        else if (e.getKeyCode()==e.VK_ENTER) {
		          int pos = sqlText.getCaretPosition();
		          sqlText.setText(
		        	sqlText.getText().substring(0,pos)+
		            cols.getSelectedValue()+
		            sqlText.getText().substring(pos)
		          );
		          colMenu.setVisible(false);
		          sqlText.requestFocus();
		          sqlText.setCaretPosition(pos+cols.getSelectedValue().toString().length());
		        }
		        else {
		          for(int i=0;i<cols.getModel().getSize();i++)
		            if (cols.getModel().getElementAt(i).toString().toLowerCase().startsWith(String.valueOf(e.getKeyChar()).toLowerCase())) {
		              cols.setSelectedIndex(i);
		              break;
		            }
		          e.consume();
		        }
		      }

		    }
		    catch (Exception ex) {
		      ex.printStackTrace();
		    }
		  }

	  //FOR: Used for auto-completion of SQL Query.
	  
	  /**
	   * Prepare a tables combo-box.
	   */
	  public void initColumnTablePopupMenu(){
		    t=new initializeThread();
		    t.start();
	  }
	  private void initialize() {
	    // create the tables combo-box...
		tablesModel.clear();
	    java.util.List tablesList = AlgoClass.getTables(DBConnectionInfo.getCurrentConnection().getProperty("dbname"),"TABLE");
	    String tableName = null;
	    for(int i=0;i<tablesList.size();i++) {
	      tableName = tablesList.get(i).toString();
	      tablesModel.addElement(tableName);
	    }
	    tables.setModel(tablesModel);
	    tables.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	    tables.revalidate();
	    tables.addMouseListener(new MouseAdapter() {
	      public void mouseClicked(MouseEvent e) {
	        int pos = sqlText.getCaretPosition();
	        sqlText.setText(
	          sqlText.getText().substring(0,pos)+
	          tables.getSelectedValue()+
	          sqlText.getText().substring(pos)
	        );
	        tableMenu.setVisible(false);
	        sqlText.requestFocus();
	        sqlText.setCaretPosition(pos+tables.getSelectedValue().toString().length());
	      }
	    });
	    tables.addListSelectionListener(new ListSelectionListener() {
	      public void valueChanged(ListSelectionEvent e) {
	        try {
	            tables.scrollRectToVisible(tables.getCellBounds(tables.getSelectedIndex(), tables.getSelectedIndex()));
	        }
	        catch (Exception ex) {
	        }
	      }
	    });
	    tableMenu.getContentPane().add(tableScrollPane,BorderLayout.CENTER);

	    // create the columns combo-box...
	    cols.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	    cols.addMouseListener(new MouseAdapter() {
	      public void mouseClicked(MouseEvent e) {
	        int pos = sqlText.getCaretPosition();
	        sqlText.setText(
	        	sqlText.getText().substring(0,pos)+
	            cols.getSelectedValue()+
	            sqlText.getText().substring(pos)
	        );
	        colMenu.setVisible(false);
	        sqlText.requestFocus();
	        sqlText.setCaretPosition(pos+ cols.getSelectedValue().toString().length());
	      }
	    });
	    cols.addListSelectionListener(new ListSelectionListener() {
	      public void valueChanged(ListSelectionEvent e) {
	        try {
	            cols.scrollRectToVisible(cols.getCellBounds(cols.getSelectedIndex(), cols.getSelectedIndex()));
	        }
	        catch (Exception ex) {
	        }
	      }
	    });

	    colMenu.getContentPane().add(colScrollPane,BorderLayout.CENTER);
	  }


	  /**
	   * Method called when user press the "." key.
	   * @param tableName table name to use to fetch its columns
	   */
	  private void initCols(String tableName) {
	    // populate the columns combo-box...
	    if (tableName.indexOf(".")>-1)
	      tableName = tableName.substring(tableName.indexOf(".")+1);
	    TableModel colsList = AlgoClass.getTableColumns(tableName);
	    colsModel.removeAllElements();
	    for(int i=0;i<colsList.getRowCount();i++) {
	      String colName = colsList.getValueAt(i,0).toString();
	      colsModel.addElement(colName);
	    }
	    cols.setModel(colsModel);
	    cols.revalidate();
	    cols.repaint();
	  }
	  
	  private JCheckBox getChkAutoCompletion() {
		  if(chkAutoCompletion == null) {
			  chkAutoCompletion = new JCheckBox();
			  chkAutoCompletion.setText("Auto Completion");
			  chkAutoCompletion.setBounds(797, 49, 139, 23);
			  chkAutoCompletion.addActionListener(new ActionListener() {
			      public void actionPerformed(ActionEvent e) {
			    	  if(!chkAutoCompletion.isSelected()){
			    		  tableMenu.setVisible(false);
			    		  colMenu.setVisible(false);
			    	  }
			      }
			    });
		  }
		  return chkAutoCompletion;
	  }

	  class initializeThread extends Thread{
		        public void run() {
		          initialize(); //Pop-up menu containing "table_name" and "column_names" for auto-completion for SQL Query.          
		          try {
		            sleep(500);
		          }
		          catch (InterruptedException ex) {
		          }
		          sqlText.requestFocus();
		        }
	  }
}
