package org.bkslab.cytosql.internal.gui;

import org.bkslab.cytosql.internal.prefs.DBConnectionInfo;
import org.bkslab.cytosql.internal.query.AlgoClass;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.HashMap;
import java.util.Properties;
import java.util.Map;
import java.util.Vector;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JPasswordField;
import javax.swing.WindowConstants;
import javax.swing.border.Border;
import javax.swing.BorderFactory;

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
/**
 * this class takes care of the GUI for the database specification management,
 * and the processing of the changes.
 * 
 * 
 */
public class DbSpecs extends javax.swing.JFrame {

  private JLabel driverLabel = new JLabel();
  private JComboBox driverComboBox;

  private JLabel urlLabel = new JLabel();
  private JPanel userDefinedPanel;
  private JPanel builtinPanel;
  private JTextField urlText = new JTextField();

  private JLabel urlPortLabel =new JLabel();
  private JTextField urlPortText = new JTextField();
  
  private JLabel databaseLabel = new JLabel();
  private JTextField databaseText = new JTextField();

  private JLabel userLabel = new JLabel();
  private JComboBox dbSelect;
  private JButton cancelButton;
  private JTextField userText = new JTextField();

  private JLabel passwordLabel = new JLabel();
  private JPasswordField passwordText = new JPasswordField();

  private JButton okButton = new javax.swing.JButton();

  private JLabel userDriverLabel=new JLabel();
  private JTextField userDriverText=new JTextField();
  
  private JLabel userURLLabel=new JLabel();
  private JTextField userURLText=new JTextField();
  
  private static Map<String, String> driverURLmap;
  private CytoSQLApp parent;
  
  private static void initDriverURLmap(){
	  driverURLmap=new HashMap<String, String>();
	  driverURLmap.put("com.mysql.jdbc.Driver", "jdbc:mysql://");
	  driverURLmap.put("org.postgresql.Driver", "jdbc:postgresql://");
	  driverURLmap.put("COM.ibm.db2.jdbc.app.DB2Driver","jdbc:db2://"); //for IBM DB2
  }
	  
  public DbSpecs(CytoSQLApp argParent) {
    super();
    this.parent=argParent;
    this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    this.getContentPane().setLayout(null);
    this.setResizable(false);
    this.setTitle("Specify Database Connection Info");

    driverLabel.setText("Driver: ");
    urlLabel.setText("Database host: ");
    urlPortLabel.setText("Port number:");
    databaseLabel.setText("Database name: ");
    userLabel.setText("Username: ");
    userDriverLabel.setText("Driver: ");
    userURLLabel.setText("URL");
    okButton.setText("OK");
    okButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        okButtonActionPerformed(evt);
      }
    });

    getContentPane().add(passwordText);
    passwordText.setBounds(150, 335, 431, 22);
    getContentPane().add(passwordLabel);
    passwordLabel.setBounds(12, 338, 138, 15);
    passwordLabel.setText("Password");
    getContentPane().add(userText);
    userText.setBounds(150, 302, 431, 22);
    getContentPane().add(userLabel);
    userLabel.setBounds(12, 305, 138, 15);
//    getContentPane().add(databaseText);
//    databaseText.setBounds(144, 135, 431, 22);
//    getContentPane().add(databaseLabel);
//    databaseLabel.setBounds(20, 138, 138, 15);
//    getContentPane().add(urlText);
//    urlText.setBounds(156, 44, 248, 22);
//    getContentPane().add(urlPortLabel);
//    urlPortLabel.setBounds(395, 104, 81, 19);

//    getContentPane().add(urlLabel);
//    urlLabel.setBounds(20, 105, 110, 15);
    getContentPane().add(okButton);
    okButton.setBounds(12, 371, 94, 22);
//    getContentPane().add(driverLabel);
//    driverLabel.setBounds(20, 73, 117, 15);
//    getContentPane().add(urlText);
    getContentPane().add(getBuiltinPanel());
//    getContentPane().add(urlPortText);
//    urlPortText.setBounds(488, 103, 84, 22);

//    urlText.setBounds(142, 102, 235, 22);
    {
      dbSelect = new JComboBox();
      getContentPane().add(dbSelect);
      dbSelect.setModel(initDBSelect());
      dbSelect.setBounds(12, 12, 575, 22);
      dbSelect.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent evt) {
          dbSelectActionPerformed(evt);
        }
      });
    }
    {
      cancelButton = new JButton();
      getContentPane().add(cancelButton);
      getContentPane().add(getUserDefinedPanel());
      cancelButton.setText("Cancel");
      cancelButton.setBounds(493, 371, 94, 22);
      cancelButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent evt) {
          cancelButtonActionPerformed(evt);
        }
      });
    }
    initDriverURLmap();
    initValues(DBConnectionInfo.getCurrentConnection());
    pack();
    this.setSize(595, 421);
  }

  private ComboBoxModel initDBSelect() {
    String favorites = DBConnectionInfo.getConnections();
    String[] favs;
    if (favorites.equals("")) {
      favs = new String[0];
    } else if (favorites.contains(";")) {
      favs = favorites.split(";");
    } else {
      favs = new String[] { favorites };
    }
    String[] items = new String[favs.length + 3];
    items[0] = "Custom";
    items[1] = "Save to favorites...";
    items[2] = "Remove from favorites...";
    System.arraycopy(favs, 0, items, 3, favs.length);
    DefaultComboBoxModel m = new DefaultComboBoxModel(items);
    return m;
  }

  public void initValues(Properties props) {
	if(props.isEmpty()){//To fix the error raised at the very first time when DBConnectionInfo file is not exit
	    urlText.setText("");
	    urlPortText.setText("");	    
	//    urlText.setText(props.getProperty("url", "")); used by Koenv
	    databaseText.setText(props.getProperty("dbname", ""));	    
	    userDriverText.setEditable(false);
	    userDriverText.setText("");
	    userURLText.setEditable(false);
	    userURLText.setText("");

	    urlText.setEditable(true);
	    urlPortText.setEditable(true);
	    databaseText.setEditable(true);
	}else{
		String driver=props.getProperty("driver").trim();
		int idx=driver.indexOf("-CUSTOM_DRIVER");
		if(idx >=0){
			  driver=driver.substring(0, idx);
			  userDriverText.setEditable(true);
			  userDriverText.setText(driver);
			  userURLText.setEditable(true);
			  userURLText.setText(props.getProperty("url", ""));
			  
			  urlText.setEditable(false);
			  urlPortText.setEditable(false);
			  databaseText.setEditable(false);
			  
			  driverComboBox.setSelectedItem("custom jdbc database driver");
		}else{
		    driverComboBox.setSelectedItem(props.getProperty("driver", "com.mysql.jdbc.Driver"));
		    String fullURL=props.getProperty("url", "");
		    String strHost="";
		    String strPort="";
		    if(!fullURL.equals("")){
			    String[] urlArray=fullURL.split("//");
			    strHost=urlArray[1];
			    if(strHost.indexOf(":") != -1){
			    	String[] arrayHost=strHost.split(":");
			    	strHost=arrayHost[0];
			    	strPort=arrayHost[1];
			    }
			}
		    
		    urlText.setText(strHost);
		    urlPortText.setText(strPort);
		    
		//    urlText.setText(props.getProperty("url", "")); used by Koenv
		    databaseText.setText(props.getProperty("dbname", ""));
		    
		    userDriverText.setEditable(false);
		    userDriverText.setText("");
		    userURLText.setEditable(false);
		    userURLText.setText("");
	
		    urlText.setEditable(true);
		    urlPortText.setEditable(true);
		    databaseText.setEditable(true);
		}
	    userText.setText(props.getProperty("username", ""));
	    passwordText.setText(props.getProperty("password", ""));
	}
  }

  // when "ok" is pressed, the text in the different fields are used to
  // set the current connection
  public void okButtonActionPerformed(final java.awt.event.ActionEvent evt) {
	  boolean isPreformatedDriver=true;
	  if(driverComboBox.getSelectedItem().toString().equals("custom jdbc database driver")) isPreformatedDriver=false;
	  Properties props = getPropValues();
	  if(isWrightConnectionInfo(props, isPreformatedDriver)){
		  DBConnectionInfo.setCurrentConnection(props);
		  parent.initColumnTablePopupMenu();
		  this.dispose();
	  }
  }

  private boolean isWrightConnectionInfo(Properties props, boolean isPreformatedDriver){
	  Connection conn = null;
	  // try to make the connection with the specifications
	  String driver=props.getProperty("driver").trim();
	  int idx=driver.indexOf("-CUSTOM_DRIVER");
	  if(idx>=0){
		  driver=driver.substring(0, idx);
	  }
	  try {
		  try {
			  Class.forName(driver).newInstance();
		  } catch (InstantiationException e) {
			  JOptionPane.showMessageDialog(null,
					  "Instantiation exception for connection to DB.\n" + e);
			  return false;
		  } catch (IllegalAccessException e) {
		          JOptionPane.showMessageDialog(null, "Cannot access DB connection.\n" + e);
		          return false;
		  } catch (ClassNotFoundException e) {
		          JOptionPane.showMessageDialog(null, "Driver class not found.\n" + e);
		          return false;
		  }
		  if(isPreformatedDriver)
			  conn = DriverManager.getConnection(props.getProperty("url").trim() + "/" + props.getProperty("dbname").trim(),
					  props.getProperty("username").trim(), props.getProperty("password").trim());
		  else 
			  conn = DriverManager.getConnection(props.getProperty("url").trim(),
					  props.getProperty("username").trim(), props.getProperty("password").trim());
		  AlgoClass.setConnection(conn);
	  }catch (Exception e) {
		  JOptionPane.showMessageDialog(null, "Couldn't create connection.\n" + e);
//		  e.printStackTrace();
//		  throw new RuntimeException(e);
		  return false;
	  }
	  return true;
  }
  private Properties getPropValues() {
    String driver = driverComboBox.getSelectedItem().toString();
    String strHost=null;
    String url=null;
    String dbname="ALREADY_SPECIFIED";
    if(!driver.equals("custom jdbc database driver")){
	    strHost=urlText.getText();
	    String strPort=urlPortText.getText();
	    url=(String)driverURLmap.get(driver)+strHost;
	    if(!strPort.equals("")) url=url+":"+strPort;
	//    String url = urlText.getText(); Used by Koenv
	    dbname = databaseText.getText();
    }else{
    	driver=userDriverText.getText()+"-CUSTOM_DRIVER";
    	url=userURLText.getText();
    }
    
    String username = userText.getText();
    String password = passwordText.getText();

    Properties props = new Properties();
    props.setProperty("driver", driver);
    props.setProperty("url", url);
    props.setProperty("dbname", dbname);
    props.setProperty("username", username);
    props.setProperty("password", password);

    return props;
  }

  private String getCurrentName() {
	  
	  if(driverComboBox.getSelectedItem().toString().equals("custom jdbc database driver")){
		  return userText.getText()+"@"+ userURLText.getText()+"-CUSTOM_DRIVER";
	  }else{
		  String driver = driverComboBox.getSelectedItem().toString();
		  String strHost=urlText.getText();
		  String strPort=urlPortText.getText();
		  String url=(String)driverURLmap.get(driver)+strHost;
		  if(!strPort.equals("")) url=url+":"+strPort;
	
		  return userText.getText() + "@" + url + "/"
	        + databaseText.getText();
	  }
  }

  private void cancelButtonActionPerformed(ActionEvent evt) {
    this.dispose();
  }

  private void driverSelectActionPerformed(ActionEvent evt){
	  if(driverComboBox.getSelectedItem().toString().equals("custom jdbc database driver")){
		  urlText.setText("");
		  urlText.setEditable(false);
		  urlPortText.setText("");
		  urlPortText.setEditable(false);
		  databaseText.setText("");
		  databaseText.setEditable(false);
		  
		  userDriverText.setEditable(true);
		  userURLText.setEditable(true);
	  }else{
		  urlText.setEditable(true);
		  urlPortText.setEditable(true);
		  databaseText.setEditable(true);
		  
		  userDriverText.setEditable(false);
		  userURLText.setEditable(false);
		  userDriverText.setText("");
		  userURLText.setText("");
	  }
  }
  private void dbSelectActionPerformed(ActionEvent evt) {
    switch (dbSelect.getSelectedIndex()){
      case 0: // Custom -- do nothing
        break;
      case 1: // Save -- add to favorites
        Properties props = getPropValues();
        DBConnectionInfo.addConnection(props, getCurrentName());
        DBConnectionInfo.setCurrentConnection(props);
        dbSelect.setModel(initDBSelect());
        break;
      case 2: // Remove -- remove from favorites
        DBConnectionInfo.removeConnection(getCurrentName());
        Properties pr = new Properties();
        initValues(pr);
        
        urlText.setEditable(true);
        urlPortText.setEditable(true);
        databaseText.setEditable(true);

        userDriverText.setEditable(false);
        userURLText.setEditable(false);
        userDriverText.setText("");
        userURLText.setText("");

        DBConnectionInfo.setCurrentConnection(pr);
        dbSelect.setModel(initDBSelect());
      default:
        Properties p = DBConnectionInfo.getConnection((String)dbSelect
            .getSelectedItem());
        initValues(p);
        DBConnectionInfo.setCurrentConnection(p);
        break;
    }
    dbSelect.setSelectedIndex(0);
  }
  
  private JComboBox getDriverComboBox() {
	  if(driverComboBox == null) {
		  ComboBoxModel driverComboBoxModel = 
			  new DefaultComboBoxModel(
					  new String[] { "com.mysql.jdbc.Driver", "org.postgresql.Driver","custom jdbc database driver" });
		  driverComboBox = new JComboBox();
		  driverComboBox.setModel(driverComboBoxModel);
//		  driverComboBox.setEditable(true);
		  driverComboBox.setBounds(141, 28, 422, 25);
	      driverComboBox.addActionListener(new ActionListener() {
	          public void actionPerformed(ActionEvent evt) {
	            driverSelectActionPerformed(evt);
	          }
	        });

	  }
	  return driverComboBox;
  }
  
  private JPanel getBuiltinPanel() {
	  if(builtinPanel == null) {
		  builtinPanel = new JPanel();
		  builtinPanel.setLayout(null);
		  builtinPanel.setBounds(10, 42, 574, 137);
		  Border border = BorderFactory.createTitledBorder("preformatted database specifications");
		  builtinPanel.setBorder(border);
		  builtinPanel.setVisible(true);
		  
		  builtinPanel.add(driverLabel);
		  driverLabel.setBounds(12, 32, 117, 15);
		  builtinPanel.add(getDriverComboBox());
		  
		  builtinPanel.add(urlLabel);
		  urlLabel.setBounds(12, 59, 111, 25);

		  builtinPanel.add(urlText);
		  urlText.setBounds(143, 62, 237, 26);
		  urlText.setEditable(true);
		  
		  builtinPanel.add(urlPortLabel);
		  urlPortLabel.setBounds(392, 61, 93, 26);
		  
		  builtinPanel.add(urlPortText);
		  urlPortText.setBounds(485, 62, 78, 26);
		  urlPortText.setEditable(true);
		  
		  builtinPanel.add(databaseLabel);
		  databaseLabel.setBounds(12, 96, 117, 26);

		  builtinPanel.add(databaseText);
		  databaseText.setBounds(141, 97, 422, 26);
		  databaseText.setEditable(true);
	  }
	  return builtinPanel;
  }
  
  private JPanel getUserDefinedPanel() {
	  if(userDefinedPanel == null) {
		  userDefinedPanel = new JPanel();
		  userDefinedPanel.setLayout(null);
		  userDefinedPanel.setBounds(12, 191, 572, 98);
		  Border border = BorderFactory.createTitledBorder("custom database specification");
		  userDefinedPanel.setBorder(border);
		  userDefinedPanel.setVisible(true);
		  
		  userDefinedPanel.add(userDriverLabel);
		  userDriverLabel.setBounds(12, 26, 129, 21);
		  userDefinedPanel.add(userDriverText);
		  userDriverText.setBounds(141, 24, 419, 26);
		  userDefinedPanel.add(userURLLabel);
		  userURLLabel.setBounds(12, 59, 123, 26);
		  userDefinedPanel.add(userURLText);
		  userURLText.setBounds(141, 60, 419, 26);
		  
		  userDriverText.setEditable(false);
		  userURLText.setEditable(false);
	  }
	  return userDefinedPanel;
  }

}
