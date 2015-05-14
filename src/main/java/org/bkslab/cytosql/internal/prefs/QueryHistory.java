org.bkslab.cytosql.internal.prefs;

import org.bkslab.cytosql.internal.util.FileIterator;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedList;

import javax.swing.JOptionPane;

import cytoscape.plugin.PluginManager;

public class QueryHistory {

  private static final int MAX_ENTRIES = 15;

  private static final String correctPath = PluginManager.getPluginManager()
      .getPluginManageDirectory().getAbsolutePath();
  private static final String filePath = correctPath.concat("/CytoSQL/query_history");

  private static final long serialVersionUID = -1428798823029951787L;

  private LinkedList<String> history = new LinkedList<String>();

  // singleton
  private static QueryHistory q = new QueryHistory();

  private QueryHistory() {
    super();
    // read in existing history
    initHistoryFromFile();
  }

  public static QueryHistory getInstance() {
    return q;
  }

  public String[] toArray() {
    return history.toArray(new String[history.size()]);
  }

  public void clearHistory() {
    history.clear();
  }
  
  public boolean contains(String elem) {
	    return history.contains(elem.replaceAll(System.getProperty("line.separator"), ""));
	  }

  public void remove(String s) {
	    history.remove(s.replaceAll(System.getProperty("line.separator"), ""));
	  }

  public void replaceFavorite(int index, String newVal) {
	    if (newVal == null)
	      return;
	    history.set(index, newVal.replaceAll(System.getProperty("line.separator"), ""));
	  }

  public void saveHistoryToFile() {
    File f = new File(filePath);
    if (!f.exists()) {
      if (f.getParent() != null) {
        new File(f.getParent()).mkdirs();
      }
      try {
        f.createNewFile();
      } catch (IOException e) {
        JOptionPane.showMessageDialog(null, "Couldn't create history file");
      }
    }

    // write out history file with most recent entry last, max MAX_ENTRIES entries
    int start = history.size() > MAX_ENTRIES ? MAX_ENTRIES - 1
        : history.size() - 1;
    try {
      PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(f)));
      try {
        for (int i = start; i >= 0; i--) {
          pw.println(history.get(i));
        }
      } finally {
        pw.close();
      }
    } catch (IOException e) {
      JOptionPane.showMessageDialog(null,
          "Couldn't open history file for writing\n" + e);
    }
  }

  private void initHistoryFromFile() {
    if (!new File(filePath).exists()) {
      return;
    }
    try {
      FileIterator f = new FileIterator(filePath);
      try {
        for (String s : f) {
          history.addFirst(s);
        }
      } finally {
        f.close();
      }
    } catch (IOException e) {
      JOptionPane.showMessageDialog(null, "Can't read history file\n" + e);
    }
  }
  
  public void add(String string) {
    String s = string.replaceAll(System.getProperty("line.separator"), "");
    if (history.contains(s)) {
      // bump up to most recent
      history.remove(s);
      history.addFirst(s);
    } else {
      history.addFirst(s);
    }
  }
}
