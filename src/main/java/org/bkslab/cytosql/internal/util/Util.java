package org.bkslab.cytosql.internal.util;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.List;

public class Util {
  public static String join(List<String> seq, String sep) {
    if (seq.isEmpty())
      return "";
    if (seq.size() == 1)
      return seq.get(0).toString();
    StringBuilder sb = new StringBuilder();
    sb.append(seq.get(0));
    for (int i = 1; i < seq.size(); i++) {
      sb.append(sep);
      sb.append(seq.get(i));
    }
    return sb.toString();
  }

  public static void writeResultSet(ResultSet rs) {
    try {
      ResultSetMetaData md = rs.getMetaData();
      int cols = md.getColumnCount();
      for (int i = 1; i <= cols; i++) {
        System.out.print(md.getColumnLabel(i) + "\t");
      }
      System.out.println();
      while (rs.next()) {
        for (int i = 1; i <= cols; i++) {
          System.out.print(rs.getString(i) + "\t");
        }
        System.out.println();
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }
}
