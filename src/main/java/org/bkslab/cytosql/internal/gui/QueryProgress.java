package org.bkslab.cytosql.internal.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JProgressBar;

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
public class QueryProgress extends JFrame {
  private static final int MAX_PING_COUNT = 4;
  private JLabel label;
  private JProgressBar pb;
  private JButton okButton;
  private int pingCount = 0;

  public QueryProgress() {
    super();
    this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    this.setResizable(false);
    getContentPane().setLayout(null);
    this.setTitle("Query Progress Bar");
    {
      label = new JLabel("CytoSQL");
      getContentPane().add(label);
      label.setBounds(25, 12, 282, 26);
    }
    {
      pb = new JProgressBar(0, 20);
      getContentPane().add(pb);
      pb.setIndeterminate(true);
      pb.setBounds(25, 52, 282, 25);
    }
    {
      okButton = new JButton();
      getContentPane().add(okButton);
      okButton.setText("OK");
      okButton.setBounds(25, 92, 117, 22);
      okButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent evt) {
          okButtonActionPerformed(evt);
        }
      });
    }
    this.pack();
    this.setSize(329, 148);
    this.setVisible(true);

  }

  public void ping() {
    String counter = ".";
    for (int i = 0; i < pingCount; i++) {
      counter += ".";
    }
    String str = "<html>" + "<font color=\"#FF0000\">" + "<b>"
        + "Query processing" + counter + "</b>" + "</font>" + "</html>";
    label.setText(str);
    pingCount++;
    if (pingCount > MAX_PING_COUNT)
      pingCount = 0;
  }

  public void setFinished() {
    pb.setIndeterminate(false);
    pb.setValue(0);
    String str = "<html>" + "<font color=\"#FF0000\">" + "<b>"
        + "Query completed." + "</b>" + "</font>" + "</html>";
    label.setText(str);
  }

  public static void main(String[] args) {
    final int interval = 300;
    final QueryProgress spb = new QueryProgress();
    spb.setVisible(true);
    // String str = "<html>" + "<font color=\"#008000\">" + "<b>"
    // + "Query is in process......." + "</b>" + "</font>" + "</html>";
    // spb.label.setText(str);
    for (int i = 0; i <= 20; i++) {
      final int j = i;
      try {
        Thread.sleep(interval);
      } catch (InterruptedException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
      spb.ping();

    }
    spb.setFinished();
  }

  private void okButtonActionPerformed(ActionEvent evt) {
    this.dispose();
  }

}
