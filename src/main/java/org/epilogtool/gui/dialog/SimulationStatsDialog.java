package org.epilogtool.gui.dialog;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashSet;
import java.util.Set;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import org.colomoto.biolqm.LogicalModel;
import org.epilogtool.common.Txt;
import org.epilogtool.gui.EpiGUI;
import org.epilogtool.project.Project;
import org.epilogtool.project.Simulation;

public class SimulationStatsDialog extends EscapableDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5605431368225388380L;
	public final Simulation simulation; 
	public final int iteration;
	
	public Set<String> modelsSelected;
	public JPanel jpSouth; 
	public JPanel jpCenter;
	
	public JButton saveAs; 
	public JButton buttonCancel; 
	
	public SimulationStatsDialog(Simulation sim, int it) {
		this.simulation = sim;
		this.iteration = it;
		this.modelsSelected = new HashSet<String>();
		
		this.setLayout(new BorderLayout());
		
		this.jpCenter = new JPanel();
		this.jpCenter.setLayout(new BoxLayout(this.jpCenter,BoxLayout.PAGE_AXIS));
		this.jpCenter.setBorder(new EmptyBorder(10, 10, 0, 10));
		this.jpCenter.add(new JLabel("Select models to save phenotype stats:"));

		
		Set<LogicalModel> models = this.simulation.getCurrentGrid().getModelSet();
		for (LogicalModel model : models) {
			JCheckBox m = new JCheckBox(Project.getInstance().getModelName(model));
			m.addActionListener(new ActionListener() {
			    @Override
			    public void actionPerformed(ActionEvent event) {
			    	
			        JCheckBox cb = (JCheckBox) event.getSource();
			        if (cb.isSelected()) {
			        	modelsSelected.add(cb.getText());
			        	enableSave();
			        } else {
			        	modelsSelected.remove(cb.getText());
			        	enableSave();
			        }
			    }
			});
			this.jpCenter.add(m);
		}
		
		this.jpSouth = new JPanel();
		this.saveAs = new JButton(Txt.get("s_SAVEAS"));
		this.saveAs.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

			try {
				EpiGUI.getInstance().savePhenosCSV(simulation, iteration, modelsSelected);
			} catch (Exception ex) {
				EpiGUI.getInstance().userMessageError(Txt.get("s_MENU_CANNOT_SAVE"), 
						Txt.get("s_MENU_SAVE_AS"));
			}
		}
		});
		
		this.buttonCancel = new JButton(Txt.get("s_CANCEL"));
		this.buttonCancel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		});
		this.jpSouth.add(this.saveAs);
		this.jpSouth.add(this.buttonCancel);
		
		this.add(this.jpCenter, BorderLayout.CENTER);
		this.add(this.jpSouth, BorderLayout.SOUTH);
		this.enableSave();
		
	}
	
	public void enableSave() {
		this.saveAs.setEnabled(!this.modelsSelected.isEmpty());
	}
	
	@Override
	public void focusComponentOnLoad() {
		// TODO Auto-generated method stub
		
	}

}
