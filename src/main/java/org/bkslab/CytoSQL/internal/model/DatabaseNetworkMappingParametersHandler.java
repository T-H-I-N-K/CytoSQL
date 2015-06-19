package org.bkslab.CytoSQL.internal.model;

import java.awt.Component;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.swing.GroupLayout;

import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;

import javax.swing.BorderFactory;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import org.cytoscape.work.Tunable;
import org.cytoscape.work.swing.AbstractGUITunableHandler;
import org.bkslab.CytoSQL.internal.model.DatabaseNetworkMappingParameters;

public class DatabaseNetworkMappingParametersHandler extends AbstractGUITunableHandler {

	private JPanel controlPanel;
	private JLabel label;
	private JTextField sqlText;
	
	private int counter;
	
	public DatabaseNetworkMappingParametersHandler(
			Field field,
			Object instance,
			Tunable tunable) {
		super(field, instance, tunable);
		
		init();
		counter = 0;
	}
	
	public DatabaseNetworkMappingParametersHandler(
			final Method getter, final Method setter, final Object instance, final Tunable tunable){
		super(getter, setter, instance, tunable);
		
		init();
		counter = 0;
	}
	
	private void init() {
		setGUI();
		
		updateFieldPanel(panel, label, controlPanel, horizontal);

	}
	
	private void setGUI(){
		label = new JLabel();
		label.setText("Database Network Mapping Parameters");
		
		sqlText = new JTextField();
		sqlText.setFont(new java.awt.Font("Monospaced", 0, 11));
	    sqlText.setBounds(13, 80, 916, 159);
	    sqlText.setText("");
		
		controlPanel = new JPanel();
		
		final GroupLayout layout = new GroupLayout(controlPanel);
		controlPanel.setLayout(layout);
		layout.setAutoCreateContainerGaps(false);
		layout.setAutoCreateGaps(true);

		layout.setHorizontalGroup(layout.createSequentialGroup()
				.addComponent(sqlText, DEFAULT_SIZE, 400, Short.MAX_VALUE)
		);
		layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER, false)
				.addComponent(sqlText, PREFERRED_SIZE, 800, PREFERRED_SIZE)
		);
	}
	
	
	@Override
	public void update(){
		sqlText.setText("Counter: " + counter);
		counter = counter + 1;
				
	}
	

	@Override
	public void handle() {
		
		DatabaseNetworkMappingParameters dnmp = new DatabaseNetworkMappingParameters();
		try {
			setValue(dnmp);
		} catch (IllegalAccessException | InvocationTargetException e) {
			// TODO Auto-generated catch block
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
