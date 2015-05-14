package org.bkslab.cytosql.internal.prefs;


import org.bkslab.cytosql.internal.util.FileIterator;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import javax.swing.JOptionPane;

import cytoscape.plugin.PluginManager;

public class QueryFavorites {
  private static final String correctPath = PluginManager.getPluginManager()
      .getPluginManageDirectory().getAbsolutePath();
  private static final String filePath = correctPath.concat("/CytoSQL/query_favorites");

  // singleton
  private static QueryFavorites q = new QueryFavorites();

  private ArrayList<String> favorites = new ArrayList<String>();

  private QueryFavorites() {
    initFavoritesFromFile();
  }

  public static QueryFavorites getInstance() {
    return q;
  }

  public String[] getFavorites() {
    return favorites.toArray(new String[favorites.size()]);
  }

  public void remove(String s) {
    favorites.remove(s.replaceAll(System.getProperty("line.separator"), ""));
  }

  public boolean contains(String elem) {
    return favorites.contains(elem.replaceAll(System.getProperty("line.separator"), ""));
  }

  // Save to favorites
  public void addToFavorites(String s) {
    // append to list
    if (s == null || s.equals("") || favorites.contains(s))
      return;
    favorites.add(s.replaceAll(System.getProperty("line.separator"), ""));
  }

  public void replaceFavorite(int index, String newVal) {
    if (newVal == null)
      return;
    favorites.set(index, newVal.replaceAll(System.getProperty("line.separator"), ""));
  }

  public void initFavoritesFromFile() {
    if (!new File(filePath).exists()) {
      return;
    }
    try {
      FileIterator f = new FileIterator(filePath);
      try {
        for (String s : f) {
          favorites.add(s);
        }
      } finally {
        f.close();
      }
    } catch (IOException e) {
      JOptionPane.showMessageDialog(null, "Can't read favorites file\n" + e);
    }
  }

  public void saveFavoritesToFile() {
    File f = new File(filePath);
    if (!f.exists()) {
      if (f.getParent() != null) {
        new File(f.getParent()).mkdirs();
      }
      try {
        f.createNewFile();
      } catch (IOException e) {
        JOptionPane.showMessageDialog(null, "Couldn't create favorites file");
      }
    }

    // append to file
    try {
      PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(f)));
      try {
        for (String s : favorites) {
          pw.println(s);
        }
      } finally {
        pw.close();
      }
    } catch (IOException e) {
      JOptionPane.showMessageDialog(null,
          "Couldn't open favorites file for writing\n" + e);
    }
  }
}
