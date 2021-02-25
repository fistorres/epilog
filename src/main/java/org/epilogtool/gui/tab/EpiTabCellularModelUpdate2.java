package org.epilogtool.gui.tab;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.tree.TreePath;

import org.colomoto.biolqm.LogicalModel;
import org.colomoto.biolqm.NodeInfo;
import org.colomoto.biolqm.tool.simulation.grouping.ModelGrouping;
import org.colomoto.biolqm.widgets.PriorityClassPanel;
import org.colomoto.biolqm.widgets.PanelChangedEventListener;
import org.epilogtool.common.Txt;
import org.epilogtool.common.Web;
import org.epilogtool.core.Epithelium2;
import org.epilogtool.core.EpitheliumUpdateSchemeIntra2;
import org.epilogtool.core.Rates;
import org.epilogtool.gui.EpiGUI.TabChangeNotifyProj;
import org.epilogtool.gui.color.ColorUtils;
import org.epilogtool.gui.widgets.JComboWideBox;
import org.epilogtool.project.Project;
import org.w3c.tools.resources.BooleanAttribute;

import jdd.util.sets.Set;

public class EpiTabCellularModelUpdate2 extends EpiTabDefinitions implements HyperlinkListener {
	private static final long serialVersionUID = 1176575422084167530L;

	private EpitheliumUpdateSchemeIntra2 updateSchemes;

	private LogicalModel selModel;
	private TabProbablyChanged tpc;
	private Map<LogicalModel, PriorityClassPanel> mModel2PCP;

	private JPanel jpNorth;
	private JPanel jpNorthLeft;
	private JPanel jpNorthRight; 
	private JPanel jpNRTop;
	private JPanel jpNRBottom;
	
	private JRadioButton jrPC; 
	private JRadioButton jrRates;
	
	private ButtonGroup groupRole;
	private JRadioButton jrSelected;
	
	private ButtonGroup ratesProb;
	private JRadioButton jrEqui;
	private JRadioButton jrNonEqui;


	private PriorityClassPanel jpPriorityPanel;

	public EpiTabCellularModelUpdate2(Epithelium2 e, TreePath path, TabChangeNotifyProj tabChanged) {
		super(e, path, tabChanged);
	} 

	public void initialize() {

		this.center.setLayout(new BorderLayout());

		this.jpNorth = new JPanel(new BorderLayout());
		this.center.add(this.jpNorth, BorderLayout.NORTH);

		// Model selection JPanel
		this.jpNorthLeft = new JPanel(new FlowLayout());
		List<LogicalModel> modelList = new ArrayList<LogicalModel>(this.epithelium.getEpitheliumGrid().getModelSet());
		JComboBox<String> jcbSBML = this.newModelCombobox(modelList);
		this.jpNorthLeft.add(jcbSBML);
		this.jpNorthLeft.setBorder(BorderFactory.createTitledBorder(Txt.get("s_MODEL_SELECT")));
		this.jpNorth.add(this.jpNorthLeft, BorderLayout.WEST);
		
		// North Right panels
		this.jpNorthRight = new JPanel();
		this.jpNorthRight.setLayout(new BoxLayout(this.jpNorthRight, BoxLayout.PAGE_AXIS));
		this.jpNRTop = new JPanel(new FlowLayout());
		this.jpNRBottom = new JPanel(new FlowLayout());

		this.jpNorthRight.add(this.jpNRTop, BorderLayout.NORTH);
		this.jpNorthRight.add(this.jpNRBottom, BorderLayout.SOUTH);
		this.jpNorth.add(this.jpNorthRight, BorderLayout.CENTER);
		this.center.add(this.jpNorth, BorderLayout.NORTH);
		
		// radio buttons - North Right Top
		this.groupRole = new ButtonGroup();
		
		this.jrPC = new JRadioButton("Priority Classes");
		this.jrPC.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				jrSelected = jrPC;
				updateCenterPanel();
			}
		});
	
		this.ratesProb = new ButtonGroup();
		
		this.jrEqui = new JRadioButton("Equiprobable");
		this.jrEqui.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				updateCenterPanel();
			}
		});
	
		this.jrNonEqui = new JRadioButton("Non-equiprobable");
		this.jrNonEqui.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				updateCenterPanel();
			}
		});
		
		this.ratesProb.add(jrEqui);
		this.ratesProb.add(jrNonEqui);
		
		this.jrRates = new JRadioButton("Random");
		this.jrRates.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				jrSelected = jrRates;
				updateCenterPanel();
			}
		});

		this.jrNonEqui.setSelected(true);
		
		this.groupRole.add(this.jrPC);
		this.groupRole.add(this.jrRates);

		this.jpNRTop.add(this.jrPC);
		this.jpNRTop.add(this.jrRates);


		//				

		this.tpc = new TabProbablyChanged();
		this.mModel2PCP = new HashMap<LogicalModel, PriorityClassPanel>();
 
		this.updateSchemes = new EpitheliumUpdateSchemeIntra2();
		for (LogicalModel m : modelList) {
			
			Boolean active = this.epithelium.getActive(m);
			ModelGrouping mpc = this.epithelium.getPriorityClasses(m);
			Rates rates = this.epithelium.getRates(m);
			Boolean uniform = null;
			
			if (mpc != null) {
				this.updateSchemes.addModelPriorityClasses(mpc.clone());	
			}
			if (rates != null) {
				this.updateSchemes.addModelRates(rates.clone());
				uniform = this.updateSchemes.getModelRates(m).isUniform();
			}
			if (active == null)
				active = true; // PC
			this.updateSchemes.setActive(m, active);
			if (active) {
				this.jrSelected = this.jrPC;
				this.jrPC.setSelected(true);
				} else {
					this.jrSelected = this.jrRates;
					this.jrRates.setSelected(true);
					if (uniform) {
						this.jrEqui.setSelected(true);
					}
				}
			}

		this.updateCenterPanel();
		this.isInitialized = true;

	}
	
	private void addRemoveProb(Boolean show) {
		if (show) {
			jpNRBottom.add(jrEqui);
			jpNRBottom.add(jrNonEqui);
		} else {
			jpNRBottom.removeAll();
		}
		this.revalidate();
		this.repaint();
	}
	
	private PriorityClassPanel getPriorityClassPanel(LogicalModel m) {
		if (!this.mModel2PCP.containsKey(m)) {
			ModelGrouping mpc = this.updateSchemes.getModelPriorityClasses(m);
			PriorityClassPanel pcp = new PriorityClassPanel(mpc, false);
			pcp.addActionListener(new PanelChangedEventListener() {
				@Override
				public void panelChangedOccurred() {
					tpc.setChanged();
				}
			});
			this.mModel2PCP.put(m, pcp);
		}
		return this.mModel2PCP.get(m);
	}

	private void paintRateSchemePanel(Boolean uniform) {
		
		Map<NodeInfo, Double> rates;
		Rates rateObj = null;
		
		if(this.updateSchemes.getModelRates(this.selModel) == null || uniform) {
			this.updateSchemes.addModelRates(this.selModel);
			rateObj = this.updateSchemes.getModelRates(this.selModel);
			rates = rateObj.getRates();
		} else {			
			rateObj = this.updateSchemes.getModelRates(this.selModel);
			rates = rateObj.getRates();
		}
	
		JPanel toCenter = new JPanel(new GridBagLayout());
		
		GridBagConstraints gbc = new GridBagConstraints();
		int nodes = 0;
		for (NodeInfo node: rates.keySet()) { 
			
			Double nodeRate = rates.get(node);
			// Double nodeRate = rateObj.getNodeRate(node.getName());
			if (!node.isInput()) {
				nodes += 1;
				gbc.gridy = nodes;
				gbc.ipady = 5;
				gbc.insets.bottom = 5;
				gbc.gridx = 0;
				gbc.anchor = GridBagConstraints.WEST;

				toCenter.add(new JLabel(node.getNodeID() + ": "),gbc);
				gbc.gridx = 1;
				JTextField jtf = new JTextField(nodeRate.toString());
				jtf.setToolTipText(node.getNodeID());
				
				// !!!!! 
				if (uniform) {
					jtf.disable();
				}  else {
					validateTextRates(jtf);
				}
				jtf.setColumns(10);
				jtf.addKeyListener(new KeyListener() {
					@Override
					public void keyTyped(KeyEvent e) {
					}
	
					@Override
					public void keyReleased(KeyEvent e) {
						JTextField jtf = (JTextField) e.getSource();
						validateTextRates(jtf);
					}
	
					@Override
					public void keyPressed(KeyEvent e) {
					}
			});
			toCenter.add(jtf, gbc);
			this.center.add(toCenter, BorderLayout.CENTER);
			}
		}
		this.updateSchemes.addModelRates(rateObj);
		tpc.setChanged();
		center.revalidate();
		center.repaint();
	}
	
	private void cleanCenterPanel() {
		BorderLayout layout = (BorderLayout) center.getLayout();
		Component centerComp = layout.getLayoutComponent(BorderLayout.CENTER);
		if (centerComp != null) {
			center.remove(centerComp);
			center.revalidate();
			center.repaint();
		}
	}
			
	private JComboBox<String> newModelCombobox(List<LogicalModel> modelList) {
		// Model selection list
		String[] saSBML = new String[modelList.size()];
		for (int i = 0; i < modelList.size(); i++) {
			saSBML[i] = Project.getInstance().getProjectFeatures().getModelName(modelList.get(i));
		}
		JComboBox<String> jcb = new JComboWideBox<String>(saSBML);
		jcb.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				@SuppressWarnings("unchecked")
				JComboBox<String> jcb = (JComboBox<String>) e.getSource();
				selModel = Project.getInstance().getProjectFeatures().getModel((String) jcb.getSelectedItem());
				updateCenterPanel();
			}
		});
		this.selModel = Project.getInstance().getProjectFeatures().getModel((String) jcb.getItemAt(0));
		return jcb;
	}
	
	private void validateTextRates(JTextField jtf) {
		String nodeName = jtf.getToolTipText();
		String text = jtf.getText();
		try {
			Double rate = Double.parseDouble(text);
			jtf.setBackground(Color.white);
			this.updateSchemes.getModelRates(this.selModel).setNodeRate(nodeName, rate);
			}
		catch(NumberFormatException er) {
			 jtf.setBackground(ColorUtils.LIGHT_RED);
			 }
		
		tpc.setChanged();

	}

	private void updatePriorityPanel() {
		this.jpPriorityPanel = this.getPriorityClassPanel(this.selModel);
		this.jpPriorityPanel.updatePriorityList();
		this.center.add(this.jpPriorityPanel, BorderLayout.CENTER);
		this.center.revalidate();
		// Repaint
		this.center.repaint();
		this.jpPriorityPanel.repaint();
	}
	
	private void updateCenterPanel() {
		cleanCenterPanel();
		if (this.jrSelected == this.jrPC) {
			this.updateSchemes.setActive(this.selModel, true);
			this.addRemoveProb(false);
			this.updatePriorityPanel();
		} else if (this.jrSelected == this.jrRates) {
			// 
			this.updateSchemes.setActive(this.selModel, false);
			this.addRemoveProb(true);
			this.paintRateSchemePanel(this.jrEqui.isSelected());
		}
		tpc.setChanged();
	}

	@Override
	protected void buttonReset() { 
		this.mModel2PCP.clear(); 
		this.updateSchemes = new EpitheliumUpdateSchemeIntra2();
		for (LogicalModel m : this.epithelium.getEpitheliumGrid().getModelSet()) { 
			
			ModelGrouping mpc = this.epithelium.getPriorityClasses(m);
			Rates rates = this.epithelium.getRates(m);
			Boolean active = this.epithelium.getActive(m);
			Boolean uniform = null;
			
			if (mpc != null) {
				this.updateSchemes.addModelPriorityClasses(mpc.clone());
			}
			if (rates != null) {
				this.updateSchemes.addModelRates(rates.clone());
			}
			if (active != null) {
				this.updateSchemes.setActive(m, active);
				
				if (m.equals(this.selModel)) {
					if (active) {
						this.jrSelected = this.jrPC;
						this.jrPC.setSelected(true);
					} else {
						uniform = rates.isUniform();
						this.jrSelected = this.jrRates;
						this.jrRates.setSelected(true);
						if (uniform) {
							this.jrEqui.setSelected(true);
						} else {
							this.jrNonEqui.setSelected(true);
						}
					}
				}
			}
		}
		updateCenterPanel();
	}

	@Override
	protected void buttonAccept() {
		
		for (LogicalModel m : this.updateSchemes.getModelSet()) {
			Boolean active = this.updateSchemes.getModelActive(m);
			ModelGrouping mpc = this.updateSchemes.getModelPriorityClasses(m);
			Rates rates = this.updateSchemes.getModelRates(m);

			if (mpc != null) {
				this.epithelium.setPriorityClasses(mpc.clone());
			}
			if (rates != null) {
				this.epithelium.setRates(rates.clone());
			}
			this.epithelium.setActive(m, active);
		}
	}

	@Override
	protected boolean isChanged() {
		for (LogicalModel m : this.updateSchemes.getModelSet()) {
			
			ModelGrouping pcGUI = this.updateSchemes.getModelPriorityClasses(m);
			ModelGrouping pcEpi = this.epithelium.getPriorityClasses(m);
			
			Rates ratesGUI = this.updateSchemes.getModelRates(m);
			Rates ratesEpi = this.epithelium.getRates(m);
			
			Boolean activeGUI = this.updateSchemes.getModelActive(m);
			Boolean activeEpi = this.epithelium.getActive(m);
			
			if(ratesEpi == null) {
				return true;
			}
			
			if (activeGUI != activeEpi || !ratesGUI.equals(ratesEpi) || !pcGUI.equals(pcEpi)) {
				return true;
			}
		}
		return false;
	} 

	@Override
	public void applyChange() {
		
		List<LogicalModel> modelList = new ArrayList<LogicalModel>(this.epithelium.getEpitheliumGrid().getModelSet());
		EpitheliumUpdateSchemeIntra2 newPCs = new EpitheliumUpdateSchemeIntra2();
		for (LogicalModel m : modelList) {
			if (this.updateSchemes.getModelSet().contains(m)) {
				// Already exists
				ModelGrouping mpc = this.updateSchemes.getModelPriorityClasses(m);
				Rates rates = this.updateSchemes.getModelRates(m);
				Boolean active = this.updateSchemes.getModelActive(m);
				if(mpc != null) {
					newPCs.addModelPriorityClasses(mpc);
				}
				if (rates != null) {
					newPCs.addModelRates(this.updateSchemes.getModelRates(m));
				}
				newPCs.setActive(m, active);
			} else {
				// Adds a new one
				//
				newPCs.addModelPriorities(m);
				newPCs.addModelRates(m);
				// pc
				newPCs.setActive(m, true);
			}
		}
		this.updateSchemes = newPCs;
		this.jpNorthLeft.removeAll();
		this.jpNorthLeft.add(this.newModelCombobox(modelList));
		this.mModel2PCP.clear();
		this.updateCenterPanel();
	}

	@Override
	public void hyperlinkUpdate(HyperlinkEvent event) {
		if (event.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
			Web.openURI(event.getDescription());
		}
	}
}
