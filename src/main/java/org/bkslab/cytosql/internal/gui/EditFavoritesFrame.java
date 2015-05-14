package org.bkslab.cytosql.internal.gui;

import org.bkslab.cytosql.internal.prefs.QueryFavorites;
import org.bkslab.cytosql.internal.prefs.QueryHistory;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Vector;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.ListModel;
import javax.swing.WindowConstants;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;

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
public class EditFavoritesFrame extends javax.swing.JFrame {

	{
		//Set Look & Feel
		try {
			javax.swing.UIManager.setLookAndFeel("com.jgoodies.looks.plastic.PlasticLookAndFeel");
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

  private JList favoritesList;
  private JButton deleteButton;
  private JButton editButton;
  private JButton closeButton;
  private JButton duplicateButton;
  private JScrollPane listScrollPane;
  
  private QueryFavorites qf;
  private CytoSQLApp app;
  private JSeparator jSeparator2;
  private JSeparator jSeparator1;
  private JButton searchButton;
  private JTextField txtSearchField;

  public EditFavoritesFrame(CytoSQLApp caller) {
    super();
    initGUI();
    qf = QueryFavorites.getInstance();
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
        deleteButton.setBounds(154, 504, 133, 22);
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
        duplicateButton.setBounds(299, 504, 133, 22);
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
        editButton.setBounds(12, 504, 133, 22);
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
        closeButton.setBounds(535, 504, 133, 22);
        closeButton.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent evt) {
            closeButtonActionPerformed(evt);
          }
        });
      }
      pack();
      this.setSize(680, 558);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private ComboBoxModel initFavoritesList() {
    String[] favs = QueryFavorites.getInstance().getFavorites();
    DefaultComboBoxModel m = new DefaultComboBoxModel(favs);
    return m;
  }

  private void editButtonActionPerformed(ActionEvent evt) {
    int index = favoritesList.getSelectedIndex();
    String s = JOptionPane.showInputDialog(this, "New input", favoritesList
        .getSelectedValue());
    if (s == null) {
      return;
    } else if (qf.contains(s)) {
      JOptionPane.showMessageDialog(null,
          "Favorites already contains this statement");
      return;
    }
    qf.replaceFavorite(index, s);
    favoritesList.setModel(initFavoritesList());
    favoritesList.setSelectedIndex(index);
  }

  private void deleteButtonActionPerformed(ActionEvent evt) {
    int i = favoritesList.getSelectedIndex();
    if (i == favoritesList.getModel().getSize() - 1) {
      i = i - 1;
    }
    for (Object o : favoritesList.getSelectedValues()) {
      qf.remove((String)o);
    }
    favoritesList.setModel(initFavoritesList());
    favoritesList.setSelectedIndex(i);
  }

  private void duplicateButtonActionPerformed(ActionEvent evt) {
    int index = favoritesList.getSelectedIndex();
    String s = JOptionPane.showInputDialog(this, "New input", favoritesList
        .getSelectedValue());
    if (s == null) {
      return;
    } else if (qf.contains(s)) {
      JOptionPane.showMessageDialog(null,
          "Favorites already contains this statement");
      return;
    }
    qf.addToFavorites(s);
    favoritesList.setModel(initFavoritesList());
    favoritesList.setSelectedIndex(index);
  }

  private void closeButtonActionPerformed(ActionEvent evt) {
    this.dispose();
  }

  private void thisWindowClosed(WindowEvent evt) {
    qf.saveFavoritesToFile();
    app.favoritesChanged();
  }

  private JScrollPane getJScrollPane1() {
    if (listScrollPane == null) {
      listScrollPane = new JScrollPane();
      listScrollPane.setBounds(12, 12, 656, 430);
      {
        ListModel FavoritesListModel = initFavoritesList();
        favoritesList = new JList();
        listScrollPane.setViewportView(favoritesList);
        favoritesList.setModel(FavoritesListModel);
        favoritesList.setBounds(18, 8, 396, 317);
        favoritesList.setPreferredSize(new java.awt.Dimension(587, 369));
        
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
        favoritesList.addListSelectionListener(listSelectionListener);

      }
    }
    return listScrollPane;
  }
  
  public JTextField getTxtSearchField() {
	  if(txtSearchField == null) {
		  txtSearchField = new JTextField();
		  txtSearchField.setBounds(13, 459, 491, 30);
	  }
	  return txtSearchField;
  }
  
  public JButton getSearchButton() {
	  if(searchButton == null) {
		  searchButton = new JButton();
		  searchButton.setText("Search");
		  searchButton.setBounds(535, 461, 134, 24);
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
//	  Pattern p=Pattern.compile(keyword);
	  if(keyword != null || keyword !=""){
		  String[] currentFav = QueryFavorites.getInstance().getFavorites();
		  Vector<String> satisfiedFav= new Vector<String>();
		  for(int i=0; i<currentFav.length;i++){
//			  Matcher m=p.matcher(currentHist[i]);
			  if(currentFav[i].indexOf(keyword)>=0) satisfiedFav.add(currentFav[i]);
		  }
		  favoritesList.setModel(new DefaultComboBoxModel(satisfiedFav.toArray()));
	  }
  }

  public JSeparator getJSeparator1() {
	  if(jSeparator1 == null) {
		  jSeparator1 = new JSeparator();
		  jSeparator1.setBounds(12, 449, 656, 10);
	  }
	  return jSeparator1;
  }
  
  private JSeparator getJSeparator2() {
	  if(jSeparator2 == null) {
		  jSeparator2 = new JSeparator();
		  jSeparator2.setBounds(12, 495, 655, 11);
	  }
	  return jSeparator2;
  }
}
