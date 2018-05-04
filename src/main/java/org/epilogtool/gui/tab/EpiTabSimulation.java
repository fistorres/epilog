package org.epilogtool.gui.tab;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.tree.TreePath;

import org.colomoto.biolqm.LogicalModel;
import org.colomoto.biolqm.NodeInfo;
import org.epilogtool.FileSelectionHelper;
import org.epilogtool.OptionStore;
import org.epilogtool.common.ObjectComparator;
import org.epilogtool.common.Txt;
import org.epilogtool.core.Epithelium;
import org.epilogtool.core.EpitheliumGrid;
import org.epilogtool.gui.EpiGUI.ProjChangeNotifyTab;
import org.epilogtool.gui.EpiGUI.SimulationEpiClone;
import org.epilogtool.gui.color.ColorUtils;
import org.epilogtool.gui.dialog.EnumNodePercent;
import org.epilogtool.gui.dialog.EnumOrderNodes;
import org.epilogtool.gui.widgets.GridInformation;
import org.epilogtool.gui.widgets.JComboCheckBox;
import org.epilogtool.gui.widgets.VisualGridSimulation;
import org.epilogtool.io.ButtonFactory;
import org.epilogtool.io.EpiLogFileFilter;
import org.epilogtool.io.FileIO;
import org.epilogtool.project.Project;
import org.epilogtool.project.Simulation;

public class EpiTabSimulation extends EpiTabTools {
	private static final long serialVersionUID = -1993376856622915249L;

	private VisualGridSimulation visualGridSimulation;
	private Simulation simulation;
	
	private EpitheliumGrid epiGridClone;

	private JPanel jpRCenter;
	private JPanel jpLeftTop;
	private JPanel jpLeft;

	private GridInformation gridInformation;

	private JComboCheckBox jccbSBML;

	private Set<String> lModelVisibleComps;
	private Map<String, Boolean> mSelCheckboxes;

	private Map<String, JCheckBox> mNodeID2Checkbox;
	private Map<String, JButton> mNodeID2JBColor;

	private List<String> nodesSelected;
	
	private SimulationEpiClone simEpiClone;
	
	private int iUserBurst;
	private int iCurrSimIter;
	private JLabel jlStep;
	private JLabel jlAttractor;
	private JButton jbRewind;
	private JButton jbBack;
	private JButton jbForward;
	private JButton jbFastFwr;
	private JButton jbRestart;
	

	public EpiTabSimulation(Epithelium e, TreePath path, ProjChangeNotifyTab projChanged,
			SimulationEpiClone simEpiClone) {
		super(e, path, projChanged);
		this.simEpiClone = simEpiClone;
	}


	/**
	 * Creates the InitialConditionsPanel, the first time the tab is created.
	 * 
	 */
	public void initialize() {
		
		this.center.setLayout(new BorderLayout());
		this.south.setLayout(new BorderLayout());
		
		this.iUserBurst = 30;
		this.iCurrSimIter = 0;

		this.epiGridClone = this.epithelium.getEpitheliumGrid().clone();
		this.mSelCheckboxes = new HashMap<String, Boolean>();
		this.mNodeID2Checkbox = new HashMap<String, JCheckBox>();
		this.mNodeID2JBColor = new HashMap<String, JButton>();
		
		this.simulation = new Simulation(this.epithelium.clone());
		this.gridInformation = new GridInformation(this.epithelium.getIntegrationFunctions());
		this.nodesSelected = new ArrayList<String>();
		
		this.visualGridSimulation = new VisualGridSimulation(this.simulation.getGridAt(0), this.nodesSelected,
				this.gridInformation);

		this.center.add(this.visualGridSimulation, BorderLayout.CENTER);

		this.jpLeft = new JPanel(new BorderLayout());

		this.jpLeftTop = new JPanel();
		this.jpLeftTop.setLayout(new BoxLayout(this.jpLeftTop, BoxLayout.Y_AXIS));
		
		this.south.setBackground(Color.black);
		
		//South Panel
		
		//Restart
		
		JPanel jpRestart = new JPanel();
		this.jbRestart = ButtonFactory.getNoMargins(Txt.get("s_TAB_SIM_RESTART"));
		this.jbRestart.setToolTipText(Txt.get("s_TAB_SIM_RESTART_DESC"));
		this.jbRestart.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				restartSimulationTab();
				jbRestart.setBackground(jbBack.getBackground());
			}
		});
		jpRestart.add(this.jbRestart);
		
		this.south.add(jpRestart, BorderLayout.LINE_START);
		
		//Other Buttons
		
		JPanel jpButtons = new JPanel(new BorderLayout());
		JPanel jpButtonsC = new JPanel();
		jpButtons.add(jpButtonsC, BorderLayout.CENTER);
		
		
		this.jbRewind = ButtonFactory.getImageNoBorder("media_step_0.png");//media_rewind-26x24.png");
		this.jbRewind.setToolTipText(Txt.get("s_TAB_SIM_BACK_DESC"));
		this.jbRewind.setEnabled(false);
		this.jbRewind.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				simulationRewind();
			}
		});
		jpButtonsC.add(this.jbRewind);

		this.jbBack = ButtonFactory.getImageNoBorder("media_step_back-24x24.png");
		this.jbBack.setToolTipText(Txt.get("s_TAB_SIM_BACK1_DESC"));
		this.jbBack.setEnabled(false);
		this.jbBack.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				simulationStepBack();
			}
		});
		jpButtonsC.add(this.jbBack);

		this.jbForward = ButtonFactory.getImageNoBorder("media_step_forward-24x24.png");
		this.jbForward.setToolTipText(Txt.get("s_TAB_SIM_FWR1_DESC"));
		this.jbForward.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				simulationStepFwr();
			}
		});
		jpButtonsC.add(this.jbForward);

		JTextField jtSteps = new JTextField("" + this.iUserBurst);
		jtSteps.setToolTipText(Txt.get("s_TAB_SIM_BURST_DESC"));
		jtSteps.setColumns(3);
		jtSteps.addKeyListener(new KeyListener() {
			@Override
			public void keyTyped(KeyEvent e) {
			}

			@Override
			public void keyReleased(KeyEvent e) {
				JTextField jtf = (JTextField) e.getSource();
				try {
					iUserBurst = Integer.parseInt(jtf.getText());
					jtf.setBackground(Color.WHITE);
				} catch (NumberFormatException nfe) {
					jtf.setBackground(ColorUtils.LIGHT_RED);
				}
			}

			@Override
			public void keyPressed(KeyEvent e) {
			}
		});
		jpButtonsC.add(jtSteps);

		this.jbFastFwr = ButtonFactory.getImageNoBorder("media_fast_forward-26x24.png");
		this.jbFastFwr.setToolTipText(Txt.get("s_TAB_SIM_FWR_DESC"));
		this.jbFastFwr.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				simulationFastFwr();
			}
		});
		jpButtonsC.add(this.jbFastFwr);

		JPanel jpButtonsR = new JPanel();
		JButton jbClone = ButtonFactory.getNoMargins(Txt.get("s_TAB_SIM_CLONE"));
		jbClone.setToolTipText(Txt.get("s_TAB_SIM_CLONE_DESC"));
		jbClone.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				cloneEpiWithCurrGrid();
			}
		});
		jpButtonsR.add(jbClone);

		// Button to save an image from the simulated grid
		JButton jbPicture = ButtonFactory.getImageNoBorder("fotography-24x24.png");
		jbPicture.setToolTipText(Txt.get("s_TAB_SIM_SAVE"));
		jbPicture.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				saveEpiGrid2File();
			}
		});
		jpButtonsR.add(jbPicture);

		// Button to save all simulated grid images
		JButton jbSaveAll = ButtonFactory.getImageNoBorder("fotography-mult-24x24.png");
		jbSaveAll.setToolTipText(Txt.get("s_TAB_SIM_SAVE_ALL"));
		jbSaveAll.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				saveAllEpiGrid2File();
			}
		});

		jpButtonsR.add(jbSaveAll);

		jpButtons.add(jpButtonsR, BorderLayout.LINE_END);
		this.south.add(jpButtons, BorderLayout.LINE_END);
		
		//Iteration
		JPanel jpIteration = new JPanel(new GridBagLayout());
		jpIteration.setBorder(BorderFactory.createLineBorder(Color.GRAY));
		
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridy = 0;
		gbc.gridx = 0;
		gbc.ipadx = gbc.ipady = 5;
		gbc.anchor = GridBagConstraints.WEST;
		jpIteration.add(new JLabel("Iteration:"), gbc);
		gbc.gridx = 1;
		this.jlStep = new JLabel("" + this.iCurrSimIter);
		jpIteration.add(this.jlStep, gbc);
		gbc.gridy = 1;
		gbc.gridx = 0;
		gbc.gridwidth = 2;
		this.jlAttractor = new JLabel("");
		this.jlAttractor.setForeground(Color.RED);
		this.setGridGUIStable(false);
		jpIteration.add(this.jlAttractor, gbc);

		this.south.add(jpIteration, BorderLayout.CENTER);
		
		// ---------------------------------------------------------------------------
		// Model selection jcomboCheckBox

		List<LogicalModel> modelList = new ArrayList<LogicalModel>(this.epithelium.getEpitheliumGrid().getModelSet());
		JCheckBox[] items = new JCheckBox[modelList.size()];
		for (int i = 0; i < modelList.size(); i++) {
			items[i] = new JCheckBox(Project.getInstance().getProjectFeatures().getModelName(modelList.get(i)));
			items[i].setSelected(false);
		}
		this.jccbSBML = new JComboCheckBox(items);
		this.jpLeftTop.add(this.jccbSBML);

		// ---------------------------------------------------------------------------
		// Select/Deselect active nodes Buttons

		this.jpLeftTop.setBorder(BorderFactory.createTitledBorder("Model selection"));
		this.jpLeft.add(this.jpLeftTop, BorderLayout.NORTH);

		//JButton select all
		JPanel rrTopSel = new JPanel(new FlowLayout());
		JButton jbSelectAll = new JButton("Select All");
		jbSelectAll.setMargin(new Insets(0, 0, 0, 0));
		jbSelectAll.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				for (String nodeID : lModelVisibleComps) {
					if (mNodeID2Checkbox.containsKey(nodeID)) {
						mNodeID2Checkbox.get(nodeID).setSelected(true);
					}
					nodesSelected.add(nodeID);

				}
				visualGridSimulation.paintComponent(visualGridSimulation.getGraphics());
			}
		});
		rrTopSel.add(jbSelectAll);
		
		//JButton deselect all
		JButton jbDeselectAll = new JButton("Deselect All");
		jbDeselectAll.setMargin(new Insets(0, 0, 0, 0));
		jbDeselectAll.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				for (String nodeID : lModelVisibleComps) {
					if (mNodeID2Checkbox.containsKey(nodeID)) {
						mNodeID2Checkbox.get(nodeID).setSelected(false);
					}
					nodesSelected.remove(nodeID);
				}
				visualGridSimulation.paintComponent(visualGridSimulation.getGraphics());
			}
		});
		rrTopSel.add(jbDeselectAll);
		
		// ---------------------------------------------------------------------------
		// Components Panel
		
		JPanel jpLeftCenter = new JPanel(new BorderLayout());
		jpLeftCenter.setBorder(BorderFactory.createTitledBorder("Components"));

		jpLeftCenter.add(rrTopSel, BorderLayout.NORTH);

		this.jpRCenter = new JPanel();
		this.jpRCenter.setLayout(new BoxLayout(jpRCenter, BoxLayout.Y_AXIS));
		JScrollPane jsLeftCenter = new JScrollPane(this.jpRCenter);
		jsLeftCenter.setBorder(BorderFactory.createEmptyBorder());
		jsLeftCenter.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		this.jccbSBML.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JComboCheckBox jccb = (JComboCheckBox) e.getSource();
				jccb.updateSelected();
				updateComponentList(jccb.getSelectedItems());
			}
		});

		jpLeftCenter.add(jsLeftCenter, BorderLayout.CENTER);
		this.jpLeft.add(jpLeftCenter, BorderLayout.CENTER);


		JPanel jpLeftAggreg = new JPanel(new BorderLayout());
		jpLeftAggreg.add(this.jpLeft, BorderLayout.LINE_START);
		jpLeftAggreg.add(this.gridInformation, BorderLayout.LINE_END);

		this.center.add(jpLeftAggreg, BorderLayout.LINE_START);
		updateComponentList(this.jccbSBML.getSelectedItems());
		this.isInitialized = true;
	}
	// ---------------------------------------------------------------------------
	// End initialize
	

	protected void restartSimulationTab() {
		
		this.simulation = new Simulation(this.epithelium.clone());
		this.simulationRewind();
		
		for (int i = 0; i < this.south.getComponentCount(); i++) {
			Component c = this.south.getComponent(i);
			if (c instanceof JTextPane) {
				this.south.remove(i);
				break;
			}
		}

		//TODO: Test integration functions; perturbations; initial conditions, integration inputs; priorities; update scheme
		
		this.south.repaint();
		this.repaint();
		this.revalidate();
		
	}


	/**
	 * Creates the panel with the selection of the components to display.
	 * 
	 * @param sNodeIDs
	 *            : List with the nodes names to be written
	 * @param titleBorder
	 *            : String with the title of the panel
	 */
	private void setComponentTypeList(List<NodeInfo> lNodes, String titleBorder, List<LogicalModel> listModels) {
		JPanel jpRRC = new JPanel(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(5, 5, 4, 0);
		jpRRC.setBorder(BorderFactory.createTitledBorder(titleBorder));
		// Collections.sort(nodeList, ObjectComparator.STRING); // orders the numbers
		int y = 0;
		
		String orderPref = (String) OptionStore.getOption("PrefsAlphaOrderNodes");
		
		if (orderPref != null && orderPref.equals(EnumOrderNodes.ALPHA.toString())) {
			lNodes = getAlphaOrderedNodes(lNodes);
		}
		
		for (NodeInfo node : lNodes) {
			for (LogicalModel m : listModels) {
				if (m.getComponents().contains(node) && !this.epithelium.isIntegrationComponent(node)) {
					this.lModelVisibleComps.add(node.getNodeID());
					this.getCompMiniPanel(jpRRC, gbc, y++, node);
					break;
				}
			}
		}
		this.jpRCenter.add(jpRRC);
	}

	private List<NodeInfo> getAlphaOrderedNodes( List<NodeInfo> lNodes) {
		//TODO: Project.getinstance().getProjectPreferences.getNodeInfo(String, LogicalModel)
		//Faz sentido? afinal proibimos que um ficheiro esteja carregado quando tem o mesmo nome e ranges de valores diferentes!
		
		List<String> lNodeID = new ArrayList<String>();
		List<NodeInfo> lOrderedNods = new ArrayList<NodeInfo>();
		
		for (NodeInfo node: lNodes) {
			lNodeID.add(node.getNodeID());
		}

//		lNodeID = lNodeID.stream().sorted().collect(Collectors.toList()); //First presents the capital letter, then the smaller
		Collections.sort(lNodeID, ObjectComparator.STRING); //Orders alphabetically, not case-sensitive
		
		for (String nodeID: lNodeID) {

			for (NodeInfo node: lNodes) {
				if (node.getNodeID().equals(nodeID)) {
					lOrderedNods.add(node);
					continue;
				}
			}
		}
		
		return lOrderedNods;
	}
	/**
	 * Updates components check selection list, once the selected model to display
	 * is changed.
	 * 
	 * @param modelNames
	 */
	private void updateComponentList(List<String> modelNames) {
		List<LogicalModel> lModels = new ArrayList<LogicalModel>();
		for (String modelName : modelNames) {
			lModels.add(Project.getInstance().getProjectFeatures().getModel(modelName));
		}
		

		this.lModelVisibleComps = new HashSet<String>();
		this.jpRCenter.removeAll();

		List<NodeInfo> lInternal = new ArrayList<NodeInfo>(
				Project.getInstance().getProjectFeatures().getModelsNodeInfos(lModels, false));
		List<NodeInfo> lInputs = new ArrayList<NodeInfo>(
				Project.getInstance().getProjectFeatures().getModelsNodeInfos(lModels, true));
//		for (int i = lInputs.size() - 1; i >= 0; i--) {
//			if (this.epithelium.isIntegrationComponent(lInputs.get(i))) {
//				lInputs.remove(i);
//			}
//		}

		if (!lInternal.isEmpty())
			this.setComponentTypeList(lInternal, "Internal", lModels);
		if (!lInputs.isEmpty())
			this.setComponentTypeList(lInputs, "Inputs", lModels);

		visualGridSimulation.paintComponent(visualGridSimulation.getGraphics());
		this.jpRCenter.revalidate();
		this.jpRCenter.repaint();
	}

	/**
	 * Creates the inner panel of each component (checkbox and color)
	 * 
	 * @param jp
	 * @param gbc
	 * @param y
	 * @param nodeID
	 */
	private void getCompMiniPanel(JPanel jp, GridBagConstraints gbc, int y, NodeInfo node) {
		String nodeID = node.getNodeID();
		EpitheliumGrid grid = this.epiGridClone;

		gbc.gridy = y;
		gbc.anchor = GridBagConstraints.WEST;

		// ----------------------------------------------------------------------------
		gbc.gridx = 0;
		jp.add(new JLabel(nodeID), gbc);

		// ----------------------------------------------------------------------------
		gbc.gridx = 1;

		JButton jbColor = this.mNodeID2JBColor.get(nodeID);
		if (jbColor == null) {
			jbColor = new JButton();
			jbColor.setToolTipText(nodeID);
			jbColor.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					setNewColor((JButton) e.getSource());
				}
			});
			this.mNodeID2JBColor.put(nodeID, jbColor);
		}
		jbColor.setBackground(Project.getInstance().getProjectFeatures().getNodeColor(nodeID));
		jp.add(jbColor, gbc);

		// ----------------------------------------------------------------------------
		gbc.gridx = 2;

		JCheckBox jcb = this.mNodeID2Checkbox.get(nodeID);
		if (jcb == null) {
			this.mSelCheckboxes.put(nodeID, false);
			// node percentage is the checkbox text
			String nodePercent = "";
			String percPref = (String) OptionStore.getOption("PrefsNodePercent");
			if (percPref != null && percPref.equals(EnumNodePercent.YES.toString())) {
				nodePercent = grid.getPercentage(nodeID);
			}
			jcb = new JCheckBox(nodePercent);
			jcb.setToolTipText(nodeID);
			jcb.setSelected(this.mSelCheckboxes.get(nodeID));
			jcb.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					JCheckBox jcb = (JCheckBox) e.getSource();
					String nodeID = jcb.getToolTipText();
					mSelCheckboxes.put(nodeID, jcb.isSelected());
					if (jcb.isSelected()) {
						nodesSelected.add(nodeID);
					} else {
						nodesSelected.remove(nodeID);
					}

					visualGridSimulation.paintComponent(visualGridSimulation.getGraphics());
				}
			});
			this.mNodeID2Checkbox.put(nodeID, jcb);
		}
		jp.add(jcb, gbc);
	}


	/**
	 * Changes the color assigned to a node.
	 * 
	 * @param jb
	 */
	private void setNewColor(JButton jb) {
		String nodeID = jb.getToolTipText();
		Color newColor = JColorChooser.showDialog(jb, "Color chooser - " + nodeID, jb.getBackground());
		if (newColor != null && !newColor.equals(Project.getInstance().getProjectFeatures().getNodeColor(nodeID))) {
			jb.setBackground(newColor);
			Project.getInstance().getProjectFeatures().setNodeColor(nodeID, newColor);
			this.projChanged.setChanged(this);
			if (this.nodesSelected.contains(nodeID)) {
				// Paint only if NodeID is selected!!
				visualGridSimulation.paintComponent(visualGridSimulation.getGraphics());
			}
		}
	}
	
	/**
	 * 
	 */
	// get current simulation step
	private void saveEpiGrid2File() {
		String ext = "png";
		String filename = FileSelectionHelper.saveFilename(ext);
		if (filename != null) {
			filename += (filename.endsWith("." + ext) ? "" : "." + ext);
			FileIO.writeEpitheliumGrid2File(filename, this.visualGridSimulation, ext);
		}
	}

	// get all the simulation steps
	private void saveAllEpiGrid2File() {
		JFileChooser fc = new JFileChooser();
		fc.setFileFilter(new EpiLogFileFilter("png"));
		if (fc.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
			String file = fc.getSelectedFile().getAbsolutePath();
			String ext = "png";
			file += (file.endsWith(ext) ? "" : "." + ext);
			for (int i = 0; i <= this.iCurrSimIter; i++) {
				String file_name = file.replace(".", "_" + i + ".");
				EpitheliumGrid grid = this.simulation.getGridAt(i);
				this.visualGridSimulation.setEpitheliumGrid(grid);
				FileIO.writeEpitheliumGrid2File(file_name, this.visualGridSimulation, ext);
			}
		}
	}
	
	private void cloneEpiWithCurrGrid() {
		this.simEpiClone.cloneEpithelium(this.epithelium, this.simulation.getGridAt(this.iCurrSimIter));
	}
	
	private void simulationFastFwr() {
		EpitheliumGrid nextGrid = this.simulation.getGridAt(this.iCurrSimIter);
		for (int i = 0; i < this.iUserBurst; i++) {
			nextGrid = this.simulation.getGridAt(this.iCurrSimIter + 1);
			if (this.simulation.isStableAt(this.iCurrSimIter + 1)) {
				setGridGUIStable(true);
				break;
			} else {
				int len = this.simulation.getTerminalCycleLen();
				if (len > 0) {
					this.setGUITerminalCycle(len);
					break;
				}
			}
			this.iCurrSimIter++;
		}
		this.visualGridSimulation.setEpitheliumGrid(nextGrid);
		this.jlStep.setText("" + this.iCurrSimIter);
		this.jbRewind.setEnabled(true);
		this.jbBack.setEnabled(true);
		String nodePercent = (String) OptionStore.getOption("PrefsNodePercent");
		if (nodePercent != null && nodePercent.equals(EnumNodePercent.YES.toString())) {
			nextGrid.updateNodeValueCounts();
		}
		this.updateComponentList(this.jccbSBML.getSelectedItems());
		// Re-Paint
		this.repaint();
	}
	
	private void simulationStepFwr() {
		EpitheliumGrid nextGrid = this.simulation.getGridAt(this.iCurrSimIter + 1);
		if (this.simulation.isStableAt(this.iCurrSimIter + 1)) {
			setGridGUIStable(true);
		} else {
			this.iCurrSimIter++;
			this.visualGridSimulation.setEpitheliumGrid(nextGrid);
			this.jlStep.setText("" + this.iCurrSimIter);
			this.setGUITerminalCycle(this.simulation.getTerminalCycleLen());
		}
		this.jbRewind.setEnabled(true);
		this.jbBack.setEnabled(true);
		String nodePercent = (String) OptionStore.getOption("PrefsNodePercent");
		if (nodePercent != null && nodePercent.equals(EnumNodePercent.YES.toString())) {
			nextGrid.updateNodeValueCounts();
		}
		this.updateComponentList(this.jccbSBML.getSelectedItems());
		// Re-Paint
		this.repaint();
	}
	
	private void simulationRewind() {
		this.iCurrSimIter = 0;
		this.jlStep.setText("" + this.iCurrSimIter);
		EpitheliumGrid firstGrid = this.simulation.getGridAt(this.iCurrSimIter);
		this.visualGridSimulation.setEpitheliumGrid(firstGrid);
		setGridGUIStable(false);
		this.jbRewind.setEnabled(false);
		this.jbBack.setEnabled(false);
		this.jbForward.setEnabled(true);
		this.jbFastFwr.setEnabled(true);
		String nodePercent = (String) OptionStore.getOption("PrefsNodePercent");
		if (nodePercent != null && nodePercent.equals(EnumNodePercent.YES.toString())) {
			firstGrid.updateNodeValueCounts();
		}
		this.updateComponentList(this.jccbSBML.getSelectedItems());
		// Re-Paint
		this.repaint();
	}

	private void simulationStepBack() {
		if (this.iCurrSimIter == 0) {
			return;
		}
		EpitheliumGrid prevGrid = this.simulation.getGridAt(--this.iCurrSimIter);
		this.jlStep.setText("" + this.iCurrSimIter);
		this.visualGridSimulation.setEpitheliumGrid(prevGrid);
		setGridGUIStable(false);
		this.setGUITerminalCycle(this.simulation.getTerminalCycleLen());
		if (this.iCurrSimIter == 0) {
			this.jbRewind.setEnabled(false);
			this.jbBack.setEnabled(false);
		}
		this.jbForward.setEnabled(true);
		this.jbFastFwr.setEnabled(true);
		String nodePercent = (String) OptionStore.getOption("PrefsNodePercent");
		if (nodePercent != null && nodePercent.equals(EnumNodePercent.YES.toString())) {
			prevGrid.updateNodeValueCounts();
		}
		this.updateComponentList(this.jccbSBML.getSelectedItems());
		// Re-Paint
		this.repaint();
	}
	
	private void setGridGUIStable(boolean stable) {
		if (stable) {
			this.jlAttractor.setText(Txt.get("s_TAB_SIM_STABLE"));
			this.jbForward.setEnabled(false);
			this.jbFastFwr.setEnabled(false);
		} else {
			this.jlAttractor.setText("           ");
		}
	}

	private void setGUITerminalCycle(int len) {
		if (len > 0) {
			this.jlAttractor.setText(Txt.get("s_TAB_SIM_CYCLE") + " (len=" + len + ")");
		} else {
			this.jlAttractor.setText("           ");
		}
	}

	/**
	 * Updates the list of components to present. If a positional input is now an
	 * integration input then it is no longer set to show in the components panel.
	 * On the other hand if an integration input is change to a positional input it
	 * will appear in the components list.
	 * 
	 * @param node
	 *            node to the removed/added in String format.
	 * @param b
	 *            if b is true, then it should be added to the list, otherwise
	 *            removed.
	 */
	public void updatelPresentComps(String node, boolean b) {
		if (b)
			this.lModelVisibleComps.add(node);
		else
			this.lModelVisibleComps.remove(node);
	}

	@Override
	public String getName() {
		return EpiTab.TOOL_SIMULATION;
	}

	@Override
	public boolean canClose() {
		return true;
	}

	private boolean hasChangedEpithelium() {
		return !this.simulation.getEpithelium().equals(this.epithelium);
	}
	
	@Override
	public void applyChange() {
//		System.out.println("EpiTabSimulation.applyChange()");
		if (this.hasChangedEpithelium()) {
//			System.out.println("applyChange().changedEpi");			
			JTextPane jtp = new JTextPane();
			jtp.setContentType("text/html");
			String color = ColorUtils.getColorCode(this.south.getBackground());
			color = "#000000";
			jtp.setText("<html><body style=\"background-color:" + color + "\">" + "<font color=\"#FFDEAD\">"
					+ "New Epithelium definitions detected!!<br/>"
					+ "Continue current simulation with old definitions, or press <b>Restart</b> to apply the new ones." + "</font></body></html>");
			jtp.setBorder(javax.swing.BorderFactory.createEmptyBorder());
			jtp.setHighlighter(null);
			this.jbRestart.setBackground(Color.RED);
			this.south.add(jtp, BorderLayout.NORTH);
		} else {
			for (int i = 0; i < this.south.getComponentCount(); i++) {
				Component c = this.south.getComponent(i);
				if (c instanceof JTextPane) {
					this.south.remove(i);
					break;
				}
			}
				}
			
//		this.updateComponentList(this.jccbSBML.getSelectedItems());
		this.south.repaint();
		this.repaint();
		this.revalidate();
	}


}
