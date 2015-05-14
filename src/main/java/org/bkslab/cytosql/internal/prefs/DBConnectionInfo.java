package org.bkslab.cytosql.internal.prefs;


import org.bkslab.cytosql.internal.util.en_decrypt;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import javax.swing.JOptionPane;

import cytoscape.plugin.PluginManager;

public class DBConnectionInfo {
  private static final String correctPath = PluginManager.getPluginManager()
      .getPluginManageDirectory().getAbsolutePath();
  private static final String filePath = correctPath.concat("/CytoSQL/connections");

  /**
   * This method stands in for getting the connection info from a properties
   * file in the working directory.
   */
  public static Properties getCurrentConnection() {
    Properties allProps = readPropsFromFile();

    Properties props = new Properties();
    props.setProperty("driver", allProps.getProperty("current-driver",
        "com.mysql.jdbc.Driver"));
    props.setProperty("url", allProps.getProperty("current-url", ""));
    props.setProperty("dbname", allProps.getProperty("current-dbname", ""));
    props.setProperty("username", allProps.getProperty("current-username", ""));
    String decryptedPass="";
    try{
    	decryptedPass=en_decrypt.decrypt(allProps.getProperty("current-password", ""));
    }catch(Exception e){
    	System.out.println(e.getStackTrace());
    }
    props.setProperty("password", decryptedPass);
    return props;
  }

  public static void setCurrentConnection(Properties props) {
    // read all connections
	try{
	    Properties allProps = readPropsFromFile();
	
	    // add new connection info
	    allProps.setProperty("current-driver", props.getProperty("driver", ""));
	    allProps.setProperty("current-url", props.getProperty("url", ""));
	    allProps.setProperty("current-dbname", props.getProperty("dbname", ""));
	    allProps.setProperty("current-username", props.getProperty("username", ""));
	    allProps.setProperty("current-password", en_decrypt.encrypt(props.getProperty("password", "")));
	
	    // save
	    savePropsToFile(allProps);
	}catch(Exception e){
		System.out.println(e.getStackTrace());
	}
  }

  public static Properties getConnection(String name) {
    Properties allProps = readPropsFromFile();

    Properties props = new Properties();
    props.setProperty("driver", allProps.getProperty(name + "-" + "driver",
        "com.mysql.jdbc.Driver"));
    props.setProperty("url", allProps.getProperty(name + "-" + "url", ""));
    props
        .setProperty("dbname", allProps.getProperty(name + "-" + "dbname", ""));
    props.setProperty("username", allProps.getProperty(name + "-" + "username",
        ""));
    String decryptedPass="";
    try{
    	decryptedPass=en_decrypt.decrypt(allProps.getProperty(name + "-" + "password",""));
    }catch(Exception e){
    	System.out.println(e.getStackTrace());
    }
    props.setProperty("password", decryptedPass);

    return props;
  }

  public static void addConnection(Properties props, String name) {

    String driver = props.getProperty("driver");
    String url = props.getProperty("url");
    String dbname = props.getProperty("dbname");
    String username = props.getProperty("username");

    if (name.equals("") || driver.equals("") || url.equals("")
        || dbname.equals("") || username.equals("")) {
      JOptionPane
          .showMessageDialog(null,
              "Cannot save connection info.  Need driver, url, username and database name.");
      return;
    }

    // read all connections
    Properties allProps = readPropsFromFile();

    // add name to favorites
    String favs = allProps.getProperty("favorites", "");
    if (favs.contains(name)) {
      // just overwrite, don't need to add name to favorites again
    } else {
      if (favs.equals("")) {
        allProps.setProperty("favorites", name);
      } else {
        allProps.setProperty("favorites", favs + ";" + name);
      }
    }
    try{
	    // add new connection info
	    allProps.setProperty(name + "-" + "driver", props.getProperty("driver"));
	    allProps.setProperty(name + "-" + "url", props.getProperty("url"));
	    allProps.setProperty(name + "-" + "dbname", props.getProperty("dbname"));
	    allProps
	        .setProperty(name + "-" + "username", props.getProperty("username"));
	    allProps
	        .setProperty(name + "-" + "password", en_decrypt.encrypt(props.getProperty("password")));
	
	    // set as current
	    setCurrentConnection(props);
	
	    // save all connections
	    savePropsToFile(allProps);
    }catch(Exception e){
    	System.out.println(e.getStackTrace());
    }
  }

  public static void removeConnection(String name) {

    // read all connections
    Properties allProps = readPropsFromFile();

    String favs = allProps.getProperty("favorites","");
    if (favs.equals("") || !favs.contains(name)) {
      return;
    } else {
      String[] vals = favs.split(";");
      if (vals.length == 1) {
        // we only had one favorite -- remove favorites
        if (!vals[0].equals(name)) {
          JOptionPane.showMessageDialog(null,
              "This shouldn't happen. Call developer.");
          return;
        }
        allProps.remove("favorites");
      } else {
        // shorten the favorites list
        String newfavs = "";
        for (String s : vals) {
          if (s.equals(name))
            continue;
          if (newfavs.equals("")) {
            newfavs = s;
          } else {
            newfavs = newfavs + ";" + s;
          }
        }
        allProps.setProperty("favorites", newfavs);
      }
    }

    // remove connection info
    allProps.remove(name + "-" + "driver");
    allProps.remove(name + "-" + "url");
    allProps.remove(name + "-" + "dbname");
    allProps.remove(name + "-" + "username");
    allProps.remove(name + "-" + "password");

    // save all connections
    savePropsToFile(allProps);
  }

  public static String getConnections() {
    // read all connections
    Properties allProps = readPropsFromFile();

    return allProps.getProperty("favorites", "");
  }

  private static Properties readPropsFromFile() {
    // create empty file if not found
    File f = new File(filePath);
    if (!f.exists()) {
      if (f.getParent() != null) {
        new File(f.getParent()).mkdirs();
      }
      try {
        f.createNewFile();
      } catch (IOException e) {
        JOptionPane.showMessageDialog(null, "Couldn't create connection file");
      }
    }

    Properties props = new Properties();
    try {
      FileInputStream in = new FileInputStream(f);
      try {
        props.load(in);
      } catch (IOException e1) {
        JOptionPane.showMessageDialog(null, "Can't read favorites file\n" + e1);
        throw new RuntimeException(e1);
      } finally {
        try {
          in.close();
        } catch (IOException e) {
          JOptionPane.showMessageDialog(null,
              "Couldn't close connectionfile.\n" + e);
          throw new RuntimeException(e);
        }
      }
    } catch (FileNotFoundException e) {
      JOptionPane.showMessageDialog(null,
          "Can't find connection file.  This shouldn't happen here. Call developer.\n"
              + e);
      throw new RuntimeException(e);
    }
    return props;
  }

  private static void savePropsToFile(Properties props) {
    File f = new File(filePath);
    if (!f.exists()) {
      if (f.getParent() != null) {
        new File(f.getParent()).mkdirs();
      }
      try {
        f.createNewFile();
      } catch (IOException e) {
        JOptionPane.showMessageDialog(null, "Couldn't create connection file");
      }
    }

    FileOutputStream out;
    try {
      out = new FileOutputStream(f);
      try {
        props.store(out, "CytoSQLConnectionInfo");
      } catch (IOException e) {
        JOptionPane.showMessageDialog(null, "Can't store connection info.\n"
            + e);
        throw new RuntimeException(e);
      } finally {
        try {
          out.close();
        } catch (IOException e) {
          JOptionPane.showMessageDialog(null,
              "Couldn't close connectionfile.\n" + e);
          throw new RuntimeException(e);
        }
      }
    } catch (FileNotFoundException e) {
      JOptionPane
          .showMessageDialog(
              null,
              "Can't find connection file.  This shouldn't happen here.. call developer ;-).\n"
                  + e);
      throw new RuntimeException(e);
    }
  }
}
