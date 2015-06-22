package org.bkslab.CytoSQL.internal.model;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Vector;

import javax.swing.GroupLayout;

import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;

import javax.swing.BorderFactory;
import javax.swing.GroupLayout.Alignment;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.text.BadLocationException;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.JWindow;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.ListSelectionModel;

import org.cytoscape.work.Tunable;
import org.cytoscape.work.swing.AbstractGUITunableHandler;
import org.bkslab.CytoSQL.internal.model.DatabaseNetworkMappingParameters;
import org.bkslab.CytoSQL.internal.model.DBQuery;

public class DatabaseNetworkMappingParametersHandler extends AbstractGUITunableHandler {

	private JPanel controlPanel;
	private JLabel label;
	
	private JLabel sqlTextLabel;
	private JCheckBox autoCompletion;
	private SQLTextArea sqlText;
	private JComboBox<String> favorites;
	private JComboBox<String> history;
	private JSeparator separator1;
	private JComboBox<String> operation;
	private JLabel previewLabel;
	private JTable previewResult;
	private JScrollPane previewPane;
	
	// PopUps
	private JWindow tablePopUpWindow;
	private JList<String> tablePopUpList;
	private DefaultListModel<String> tablePopUpListModel;
	private JScrollPane tablePopUpScrollPane;
	
	private JWindow columnPopUpWindow;
	private JList<String> columnPopUpList;
	private DefaultListModel<String> columnPopUpListModel;	
	private JScrollPane columnPopUpScrollPane;
	private Thread popUpThread=null;
	
	// for getting help with building the query
	// make sure that it is initialized before calling this handler
	private DBQuery dbQuery;
	
	private boolean dotPressed = false;
	
	public DatabaseNetworkMappingParametersHandler(
			Field field,
			Object instance,
			Tunable tunable) {
		super(field, instance, tunable);
		
		init();
	}
	
	public DatabaseNetworkMappingParametersHandler(
			final Method getter, final Method setter, final Object instance, final Tunable tunable){
		super(getter, setter, instance, tunable);
		
		init();
	}
	
	public void setDBQuery(DBQuery dbQuery) {
		this.dbQuery = dbQuery;
	}
	
	private DBQuery getDBQuery() throws Exception{
		if(dbQuery == null){
			throw new Exception("A database connection has not been established yet");
		}
		return dbQuery;
	}
	
	private void init() {
				
		initializeLayout();
		initializeActionListeners();
		
		updateFieldPanel(panel, label, controlPanel, horizontal);

	}
	
	private void initializeLayout(){
		label = new JLabel();
		label.setText("Database Network Mapping Parameters");
		
		sqlTextLabel = new JLabel();
		sqlTextLabel.setText("Enter SQL query:");
		
		autoCompletion = new JCheckBox();
		autoCompletion.setText("Auto Completion");
		
		sqlText = new SQLTextArea();
		
	    tablePopUpWindow = new JWindow();
	    tablePopUpList = new JList<String>();
	    tablePopUpListModel = new DefaultListModel<String>();
	    tablePopUpScrollPane = new JScrollPane(tablePopUpList);

	    tablePopUpWindow = new JWindow();	    	    
	    columnPopUpList = new JList<String>();
	    columnPopUpListModel = new DefaultListModel<String>();
	    columnPopUpScrollPane = new JScrollPane(columnPopUpList);
	    	    
	    favorites = new JComboBox<String>();
	    updateFavorites();
	    
	    history = new JComboBox<String>();
	    history.setModel(initHistory());
	    
	    separator1 = new JSeparator();

	    operation = new JComboBox<String>();
	    operation.setModel(initOperation());
	    
	    previewLabel = new JLabel();
	    previewLabel.setText("SQL results preview:");
	    
	    previewResult = new JTable();
	    previewPane = new JScrollPane(previewResult);
	    
		controlPanel = new JPanel();
		
		final GroupLayout layout = new GroupLayout(controlPanel);
		controlPanel.setLayout(layout);
		layout.setAutoCreateContainerGaps(true);
		layout.setAutoCreateGaps(true);
		
		layout.setHorizontalGroup(
			layout.createSequentialGroup()
			.addComponent(sqlTextLabel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
			.addComponent((JScrollPane)sqlText, 100, DEFAULT_SIZE, 1000)
			.addGroup(
				layout.createParallelGroup(GroupLayout.Alignment.CENTER)
				.addComponent(favorites, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				.addComponent(history, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE))
			.addComponent(separator1, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
			.addComponent(operation, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
			.addComponent(previewPane, 100, DEFAULT_SIZE, 1000));
		
		layout.setVerticalGroup(
			layout.createParallelGroup(GroupLayout.Alignment.LEADING)
			.addGroup(
				layout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addComponent(sqlTextLabel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE))
			.addComponent((JScrollPane)sqlText, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
			.addGroup(
				layout.createSequentialGroup()
				.addComponent(favorites, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				.addPreferredGap(ComponentPlacement.RELATED)
				.addComponent(history, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE))
			.addComponent(separator1, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
			.addComponent(previewPane, 100, DEFAULT_SIZE, 1000));
	}
	
	private void initializeActionListeners(){
		sqlText.addKeyListener(new KeyAdapter(){
			public void keyPressed(KeyEvent e) {
				try {
					dotPressed = false;
					if (e.getKeyCode() == KeyEvent.VK_SPACE && e.isControlDown() && !tablePopUpWindow.isVisible()) {
						popUpShow(tablePopUpWindow, tablePopUpList);
						e.consume();
					} else if (tablePopUpWindow.isVisible()) {
						popUpKeyPressed(tablePopUpWindow, tablePopUpList, e);
					} else if (e.getKeyChar() == KeyEvent.VK_DECIMAL && !columnPopUpWindow.isVisible()) {
						columnPopupShow(e);
						e.consume();
					} else if (columnPopUpWindow.isVisible()) {
						popUpKeyPressed(columnPopUpWindow, columnPopUpList, e);
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}	

			public void keyTyped(KeyEvent e) {
				if (!autoCompletion.isSelected()) return;
				if(tablePopUpWindow.isVisible() | columnPopUpWindow.isVisible()){
					e.consume();
				}
			}
		});
		
		autoCompletion.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(!autoCompletion.isSelected()){
					tablePopUpWindow.setVisible(false);
					columnPopUpWindow.setVisible(false);
				}
			}
		});
		
	    tablePopUpList.addMouseListener(new MouseAdapter() {
	    	public void mouseClicked(MouseEvent e) {
	    		int pos = sqlText.getCaretPosition();
	    		sqlText.setText(
	    			sqlText.getText().substring(0,pos) +
	    			tablePopUpList.getSelectedValue() +
	    			sqlText.getText().substring(pos));
	    		
		        tablePopUpWindow.setVisible(false);
		        sqlText.requestFocus();
		        sqlText.setCaretPosition(pos+tablePopUpList.getSelectedValue().toString().length());
	    	}
	    });
		
	    tablePopUpList.addListSelectionListener(new ListSelectionListener() {
	    	public void valueChanged(ListSelectionEvent e) {
	    		try {
		        	tablePopUpList.scrollRectToVisible(
		        		tablePopUpList.getCellBounds(
		        			tablePopUpList.getSelectedIndex(),
		        			tablePopUpList.getSelectedIndex()));
		        }
	    		catch (Exception ex) {}
	    	}
	    });
		
		favorites.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent evt) {
				favoritesActionPerformed(evt);
			}
		});
		
		history.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				historyActionPerformed(evt);
			}
		});

	}
	

	private ComboBoxModel<String> initFavorites() {
		String[] favs = new String[0]; // = QueryFavorites.getInstance().getFavorites();
		String[] items = new String[favs.length + 3];
		items[0] = "Select query from favorites...";
		items[1] = "Save to favorites...";
		items[2] = "Search & Edit favorites...";
		System.arraycopy(favs, 0, items, 3, favs.length);
		DefaultComboBoxModel<String> m = new DefaultComboBoxModel<String>(items);
		return m;
	}
	public void updateFavorites() {
		favorites.setModel(initFavorites());
	}
	private void favoritesActionPerformed(ActionEvent evt) {
//		switch (favorites.getSelectedIndex()) {
//		case 0: // select -- do nothing
//			break;
//		case 1: // save -- add to favorites
//			QueryFavorites.getInstance().addToFavorites((String)sqlText.getText());
//			QueryFavorites.getInstance().saveFavoritesToFile();
//			updateFavorites();
//			break;
//		case 2: // edit -- show edit frame
//			java.awt.EventQueue.invokeLater(new Runnable() {
//				public void run() {
//					new EditFavoritesFrame(CytoSQLApp.this).setVisible(true);
//				}
//			});
//			break;
//		default:
//			sqlText.setText((String)favorites.getSelectedItem());
//			break;
//		}
		favorites.setSelectedIndex(0);
	}
	

	private ComboBoxModel<String> initHistory() {
		String[] hist = new String[0]; //queryHistory.getInstance().toArray();
		String[] items = new String[hist.length + 3];
		items[0] = "Select query from history...";
		items[1] = "Clear history...";
		items[2] = "Search & Edit history";
		System.arraycopy(hist, 0, items, 3, hist.length);
		 
		DefaultComboBoxModel<String> m = new DefaultComboBoxModel<String>(items);
		return m;
	}
	private void updateHistory(String query) {
//		QueryHistory.getInstance().add(query);
//		history.setModel(initHistory());
//		QueryHistory.getInstance().saveHistoryToFile();
	}
	private void historyActionPerformed(ActionEvent evt) {
//		switch (history.getSelectedIndex()) {
//		case 0: // select -- do nothing
//			break;
//		case 1: // clear -- remove all
//			QueryHistory.getInstance().clearHistory();
//			QueryHistory.getInstance().saveHistoryToFile();
//			history.setModel(initHistory());
//			break;
//		case 2: // search -- show search frame
//			java.awt.EventQueue.invokeLater(new Runnable() {
//				public void run() {
//					new SearchHistoryFrames(CytoSQLApp.this).setVisible(true);
//				}
//			});
//			break;
//		default:
//			sqlText.setText((String)history.getSelectedItem());
//		}
		history.setSelectedIndex(0);
	}
	
	
	
	private ComboBoxModel<String> initOperation() {
		String[] items = new String[2];
		items[0] = "Create Network";
		items[1] = "Extend Network";

		DefaultComboBoxModel<String> m = new DefaultComboBoxModel<String>(items);
		return m;
	}

	
	private void popUpInitialize(){
		popUpThread = new Thread(){
			public void run(){
				try {
					tablePopUpInitialize();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				columnPopUpInitialize();
				try{
					sleep(500);				
				} catch (InterruptedException ex){}
			}
			private void tablePopUpInitialize() throws Exception{
				tablePopUpListModel.clear();
			    List<String> tablesList = getDBQuery().getTables("TABLE");
			    		
			    for(String tableName : tablesList){
			      tablePopUpListModel.addElement(tableName);
			    }
			    tablePopUpList.setModel(tablePopUpListModel);
			    tablePopUpList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			    tablePopUpList.revalidate();
			    tablePopUpWindow.getContentPane().add(tablePopUpScrollPane,BorderLayout.CENTER);

			}
			private void columnPopUpInitialize(){
			    columnPopUpList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			    columnPopUpList.addMouseListener(new MouseAdapter() {
			      public void mouseClicked(MouseEvent e) {
			        int pos = sqlText.getCaretPosition();
			        sqlText.setText(
			        	sqlText.getText().substring(0,pos)+
			            columnPopUpList.getSelectedValue()+
			            sqlText.getText().substring(pos)
			        );
			        columnPopUpWindow.setVisible(false);
			        sqlText.requestFocus();
			        sqlText.setCaretPosition(pos+ columnPopUpList.getSelectedValue().toString().length());
			      }
			    });
			    columnPopUpList.addListSelectionListener(new ListSelectionListener() {
			      public void valueChanged(ListSelectionEvent e) {
			        try {
			        	columnPopUpList.scrollRectToVisible(
			        		columnPopUpList.getCellBounds(
			        			columnPopUpList.getSelectedIndex(),
			        			columnPopUpList.getSelectedIndex()));
			        }
			        catch (Exception ex) {
			        }
			      }
			    });

			    columnPopUpWindow.getContentPane().add(columnPopUpScrollPane,BorderLayout.CENTER);
			}
		};
		popUpThread.start();
	}
	
	private void popUpShow(JWindow popUpWindow, JList<String> popUpList) {
		try {
			popUpWindow.setLocation(
				sqlText.modelToView(sqlText.getCaretPosition()).x,
				sqlText.modelToView(sqlText.getCaretPosition()).y + 150);
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
		popUpWindow.setSize(300, 150);
		popUpWindow.setVisible(true);
		popUpList.requestFocus();
		popUpList.setSelectedIndex(0);
	}
	private void popUpKeyPressed(JWindow popUpWindow, JList<String> popUpList, KeyEvent e){
		e.consume();
		int i = popUpList.getSelectedIndex();
		int n = popUpList.getModel().getSize();
		int nVisible = popUpList.getLastVisibleIndex() - popUpList.getFirstVisibleIndex() + 1;
		switch(e.getKeyCode()){
		case KeyEvent.VK_UP:
			if(i > 0) popUpList.setSelectedIndex(i - 1);
			break;
		case KeyEvent.VK_DOWN:
			if(i < n - 1) popUpList.setSelectedIndex(i + 1);
			break;
		case KeyEvent.VK_PAGE_UP:
			i = i - nVisible;
			if (i < 0) i = 0;
			popUpList.setSelectedIndex(i);
			break;
		case KeyEvent.VK_PAGE_DOWN:
			i = i + nVisible;
			if (i - 1 > n) i = n - 1;
			popUpList.setSelectedIndex(i);
			break;
		case KeyEvent.VK_ESCAPE:
			popUpWindow.setVisible(false);
			sqlText.requestFocus();
			break;
		case KeyEvent.VK_ENTER:
			int pos = sqlText.getCaretPosition();
			sqlText.setText(sqlText.getText().substring(0, pos)
					+ i
					+ sqlText.getText().substring(pos));
			popUpWindow.setVisible(false);
			sqlText.requestFocus();
			sqlText.setCaretPosition(
					pos + popUpList.getSelectedValue().toString().length());
			break;
		default:
			for (int j = 0; j < n; j++){
				if (popUpList
					.getModel()
					.getElementAt(j)
					.toString()
					.toLowerCase()
					.startsWith(String.valueOf(e.getKeyChar()).toLowerCase())) {
					popUpList.setSelectedIndex(j);
					break;
				}
				e.consume();
			}
		}
	}

		
	private void columnPopupShow(KeyEvent e){
		dotPressed = true;
		Thread t = new Thread() {
			public void run() {
				try {
					sleep(500);
				} catch (InterruptedException ex) {}
				if (!dotPressed) return;
				
				try {
					// view PopUp menu containing columns list...
					String text = sqlText.getText();
					int start = 0;
					while (start < text.length() && text.indexOf(" ", start + 1) > -1) {
						if (text.indexOf(" ", start + 1) < sqlText.getCaretPosition()) {
							start = text.indexOf(" ", start + 1);
						} else {
							break;
						}
					}
					String tableName = text.substring(start,sqlText.getCaretPosition() - 1);
					if (tableName.indexOf(",") != -1){
						tableName = tableName.substring(tableName.indexOf(",") + 1);
					}
					
					if (tableName.indexOf(".") > -1){
						tableName = tableName.substring(tableName.indexOf(".") + 1);
					}
					TableModel colsList = getDBQuery().getTableColumns(tableName);
					columnPopUpListModel.removeAllElements();
					for(int i=0;i<colsList.getRowCount();i++) {
						String colName = colsList.getValueAt(i,0).toString();
						columnPopUpListModel.addElement(colName);
					}
					columnPopUpList.setModel(columnPopUpListModel);
					columnPopUpList.revalidate();
					columnPopUpList.repaint();
					
					popUpShow(columnPopUpWindow, columnPopUpList);
				} catch (Exception ex) {}
			}
		};
		t.start();
	}

	

	private void updatePreviewResult(ResultSet resultSet){
		Vector<String> columnNames=new Vector<String>();
		Vector<Vector<Object>> rows=new Vector<Vector<Object>>();
		try {
			int nCol = resultSet.getMetaData().getColumnCount();
			for(int i=1; i <= nCol; i++){
				columnNames.set(i, resultSet.getMetaData().getColumnLabel(i));
			}
			while(resultSet.next()){
				Vector<Object> row = new Vector<Object>(nCol);
				for(int i=1; i<= nCol; i++){
					row.set(i, resultSet.getObject(i));
				}
				rows.add(row);
			}
		} catch (SQLException e){
			System.out.println("Database error in CytoSQL:" + e);
		}
		previewResult.setModel(new DefaultTableModel(rows, columnNames));
	}
		
	
	
	@Override
	public void update(){
		sqlText.setText("");
				
	}
	
	@Override
	public void handle() {
		
		DatabaseNetworkMappingParameters dnmp = new DatabaseNetworkMappingParameters(
			sqlText.getText(),
			"new Network",
			1,
			2,
			3,
			"pp",
			"name",
			true,
			false);
		try {
			setValue(dnmp);
		} catch (IllegalAccessException | InvocationTargetException e) {
			e.printStackTrace();
		}
	}
	
	
	// copied from org.cytoscape.work.internal.tunables.utils.GUIDefaults because it's not accessible f
	private static void updateFieldPanel(final JPanel p, final Component label, final Component control,
			final boolean horizontalForm) {
//		if (p instanceof TunableFieldPanel) {
//			((TunableFieldPanel)p).setControl(control);
//			
//			if (label instanceof JLabel)
//				((TunableFieldPanel)p).setLabel((JLabel)label);
//			else if (label instanceof JTextArea)
//				((TunableFieldPanel)p).setMultiLineLabel((JTextArea)label);
//			
//			return;
//		}
		
		p.removeAll();
		
		final GroupLayout layout = new GroupLayout(p);
		p.setLayout(layout);
		layout.setAutoCreateContainerGaps(false);
		layout.setAutoCreateGaps(true);
		
		if (horizontalForm) {
			p.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 2));
			
			layout.setHorizontalGroup(layout.createParallelGroup(Alignment.LEADING, true)
					.addComponent(label, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
					.addComponent(control, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
			);
			layout.setVerticalGroup(layout.createSequentialGroup()
					.addComponent(label, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addComponent(control, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
			);
		} else {
			p.setBorder(BorderFactory.createEmptyBorder(2, 0, 2, 0));
			
			final Alignment vAlign = control instanceof JPanel || control instanceof JScrollPane ? 
					Alignment.LEADING : Alignment.CENTER;
			
			int w = Math.max(label.getPreferredSize().width, control.getPreferredSize().width);
			int gap = w - control.getPreferredSize().width; // So the label and control are centered
			
			layout.setHorizontalGroup(layout.createSequentialGroup()
					.addGroup(layout.createParallelGroup(Alignment.TRAILING, true)
							.addComponent(label, w, w, Short.MAX_VALUE)
					)
					.addGroup(layout.createParallelGroup(Alignment.LEADING, true)
							.addGroup(layout.createSequentialGroup()
									.addComponent(control, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
									.addGap(gap, gap, Short.MAX_VALUE)
							)
					)
			);
			layout.setVerticalGroup(layout.createParallelGroup(vAlign, false)
					.addComponent(label)
					.addComponent(control)
			);
		}
	}

}
