package org.bkslab.cytosql.internal.gui;

import org.bkslab.cytosql.internal.prefs.QueryHistory;

import java.util.Vector;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.JToolTip;
import javax.swing.ListModel;
import javax.swing.WindowConstants;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;


/**
* This code was edited or generated using CloudGarden's Jigloo
* SWT/Swing GUI Builder, which is free for non-commercial
* use. If Jigloo is being used commercially (ie, by a corporation,
* company or business for any purpose whatever) then you
* should purchase a license for each developer using Jigloo.
* Please visit www.cloudgarden.com for details.
* Use of Jigloo implies acceptance of these licensing terms.
* A COMMERCIAL LICENSE HAS NOT BEEN PURCHASED FOR
* THIS MACHINE, SO JIGLOO OR THIS CODE CANNOT BE USED
* LEGALLY FOR ANY CORPORATE OR COMMERCIAL PURPOSE.
*/
public class SearchHistoryFrames extends javax.swing.JFrame{
	  private JList historyList;
	  private JSeparator jSeparator2;
	  private JSeparator jSeparator1;
	  private JButton searchButton;
	  private JTextField txtSearchField;
	  private JButton deleteButton;
	  private JButton editButton;
	  private JButton closeButton;
	  private JButton duplicateButton;
	  private JScrollPane listScrollPane;
	  
	  private QueryHistory qh;
	  private CytoSQLApp app;
	 

	  public SearchHistoryFrames(CytoSQLApp caller) {
	    super();
	    initGUI();
	    qh = QueryHistory.getInstance();
	    app = caller;
	  }

	  private void initGUI() {
	    try {
	      setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
	      getContentPane().setLayout(null);
	      this.setResizable(false);
	      this.addWindowListener(new WindowAdapter() {
	        public void windowClosed(WindowEvent evt) {
	          thisWindowClosed(evt);
	        }
	      });
	      {
	        deleteButton = new JButton();
	        getContentPane().add(deleteButton);
	        deleteButton.setText("Delete");
	        deleteButton.setBounds(156, 501, 133, 22);
	        deleteButton.addActionListener(new ActionListener() {
	          public void actionPerformed(ActionEvent evt) {
	            deleteButtonActionPerformed(evt);
	          }
	        });
	      }
	      {
	        duplicateButton = new JButton();
	        getContentPane().add(duplicateButton);
	        duplicateButton.setText("Duplicate");
	        duplicateButton.setBounds(300, 501, 133, 22);
	        duplicateButton.addActionListener(new ActionListener() {
	          public void actionPerformed(ActionEvent evt) {
	            duplicateButtonActionPerformed(evt);
	          }
	        });
	      }
	      {
	        editButton = new JButton();
	        getContentPane().add(editButton);
	        editButton.setText("Edit");
	        editButton.setBounds(12, 501, 133, 22);
	        editButton.addActionListener(new ActionListener() {
	          public void actionPerformed(ActionEvent evt) {
	            editButtonActionPerformed(evt);
	          }
	        });
	      }
	      {
	        closeButton = new JButton();
	        getContentPane().add(closeButton);
	        getContentPane().add(getJScrollPane1());
	        getContentPane().add(getTxtSearchField());
	        getContentPane().add(getSearchButton());
	        getContentPane().add(getJSeparator1());
	        getContentPane().add(getJSeparator2());
	        closeButton.setText("Close");
	        closeButton.setBounds(536, 501, 133, 22);
	        closeButton.addActionListener(new ActionListener() {
	          public void actionPerformed(ActionEvent evt) {
	            closeButtonActionPerformed(evt);
	          }
	        });
	      }
	      pack();
	      this.setSize(680, 554);
	    } catch (Exception e) {
	      e.printStackTrace();
	    }
	  }

	  private ComboBoxModel initHistoryList() {
	    String[] hist = QueryHistory.getInstance().toArray();
	    DefaultComboBoxModel m = new DefaultComboBoxModel(hist);
	    return m;
	  }

	  private void editButtonActionPerformed(ActionEvent evt) {
	    int index = historyList.getSelectedIndex();
	    String s = JOptionPane.showInputDialog(this, "New input", historyList
	        .getSelectedValue());
	    if (s == null) {
	      return;
	    } else if (qh.contains(s)) {
	      JOptionPane.showMessageDialog(null,
	          "Favorites already contains this statement");
	      return;
	    }
	    qh.replaceFavorite(index, s);
	    historyList.setModel(initHistoryList());
	    historyList.setSelectedIndex(index);
	  }

	  private void deleteButtonActionPerformed(ActionEvent evt) {
	    int i = historyList.getSelectedIndex();
	    if (i == historyList.getModel().getSize() - 1) {
	      i = i - 1;
	    }
	    for (Object o : historyList.getSelectedValues()) {
	      qh.remove((String)o);
	    }
	    historyList.setModel(initHistoryList());
	    historyList.setSelectedIndex(i);
	  }

	  private void duplicateButtonActionPerformed(ActionEvent evt) {
	    int index = historyList.getSelectedIndex();
	    String s = JOptionPane.showInputDialog(this, "New input", historyList
	        .getSelectedValue());
	    if (s == null) {
	      return;
	    } else if (qh.contains(s)) {
	      JOptionPane.showMessageDialog(null,
	          "History already contains this statement");
	      return;
	    }
	    qh.add(s);
	    historyList.setModel(initHistoryList());
	    historyList.setSelectedIndex(index);
	  }

	  private void closeButtonActionPerformed(ActionEvent evt) {
	    this.dispose();
	  }

	  private void thisWindowClosed(WindowEvent evt) {
	    qh.saveHistoryToFile();
	    app.favoritesChanged();
	  }

	  private JScrollPane getJScrollPane1() {
	    if (listScrollPane == null) {
	      listScrollPane = new JScrollPane();
	      listScrollPane.setBounds(12, 12, 656, 430);
	      {
	        ListModel HistoryListModel = initHistoryList();
	        historyList = new JList();	        
	        listScrollPane.setViewportView(historyList);
	        historyList.setModel(HistoryListModel);
	        historyList.setBounds(18, 8, 396, 317);
	        historyList.setPreferredSize(new java.awt.Dimension(587, 369));
	        
	        ListSelectionListener listSelectionListener = new ListSelectionListener() {
	            public void valueChanged(ListSelectionEvent listSelectionEvent) {
	                boolean adjust = listSelectionEvent.getValueIsAdjusting();
	                if (!adjust) {
	                  JList list = (JList) listSelectionEvent.getSource();
	                  int selections[] = list.getSelectedIndices();
	                  Object selectionValues[] = list.getSelectedValues();
	                  if(selectionValues.length > 1) 
	                      JOptionPane.showMessageDialog(null, "Please select only one query to execute");
	                  else
	                	  app.getSQLText().setText((String)selectionValues[0]);
	                }
	            }
	        };
	        historyList.addListSelectionListener(listSelectionListener);
	      }
	    }
	    return listScrollPane;
	  }
	  
	  public JTextField getTxtSearchField() {
		  if(txtSearchField == null) {
			  txtSearchField = new JTextField();
			  txtSearchField.setBounds(12, 457, 496, 28);
		  }
		  return txtSearchField;
	  }
	  
	  public JButton getSearchButton() {
		  if(searchButton == null) {
			  searchButton = new JButton();
			  searchButton.setText("Search");
			  searchButton.setBounds(536, 459, 132, 24);
			  searchButton.addActionListener(new ActionListener() {
				  public void actionPerformed(ActionEvent evt) {
					  searchButtonActionPerformed(evt);
				  }
			  });
		  }
		  return searchButton;
	  }
	  
	  private void searchButtonActionPerformed(ActionEvent evt){
		  String keyword=txtSearchField.getText();
//		  Pattern p=Pattern.compile(keyword);
		  if(keyword != null || keyword !=""){
			  String[] currentHist = QueryHistory.getInstance().toArray();
			  Vector<String> satisfiedHist= new Vector<String>();
			  for(int i=0; i<currentHist.length;i++){
//				  Matcher m=p.matcher(currentHist[i]);
				  if(currentHist[i].indexOf(keyword)>=0) satisfiedHist.add(currentHist[i]);
			  }
			  historyList.setModel(new DefaultComboBoxModel(satisfiedHist.toArray()));
		  }
	  }
	  private JSeparator getJSeparator1() {
		  if(jSeparator1 == null) {
			  jSeparator1 = new JSeparator();
			  jSeparator1.setBounds(12, 449, 653, 10);
		  }
		  return jSeparator1;
	  }
	  
	  private JSeparator getJSeparator2() {
		  if(jSeparator2 == null) {
			  jSeparator2 = new JSeparator();
			  jSeparator2.setBounds(13, 492, 652, 4);
		  }
		  return jSeparator2;
	  }
}
