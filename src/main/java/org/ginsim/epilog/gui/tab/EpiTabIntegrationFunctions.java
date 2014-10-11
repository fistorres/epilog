package org.ginsim.epilog.gui.tab;

import java.awt.BorderLayout;
import java.awt.Color;
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
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.tree.TreePath;

import org.colomoto.logicalmodel.LogicalModel;
import org.colomoto.logicalmodel.NodeInfo;
import org.ginsim.epilog.project.ProjectModelFeatures;
import org.ginsim.epilog.core.ComponentIntegrationFunctions;
import org.ginsim.epilog.core.Epithelium;
import org.ginsim.epilog.core.EpitheliumIntegrationFunctions;
import org.ginsim.epilog.gui.color.ColorUtils;

public class EpiTabIntegrationFunctions extends EpiTabDefinitions {
	private static final long serialVersionUID = -2124909766318378839L;

	private EpitheliumIntegrationFunctions userIntegrationFunctions;
	private String activeNodeID;

	private Map<String, JRadioButton> mNode2RadioButton;
	private JPanel jpNLBottom;
	private JPanel jpNRTop;
	private JPanel jpNRBottom;

	public EpiTabIntegrationFunctions(Epithelium e, TreePath path,
			ProjectModelFeatures modelFeatures) {
		super(e, path, modelFeatures);
		this.mNode2RadioButton = new HashMap<String, JRadioButton>();
	}

	public void initialize() {
		this.center.setLayout(new BorderLayout());

		this.userIntegrationFunctions = this.epithelium
				.getIntegrationFunctions().clone();
		this.activeNodeID = null;

		// North Panel
		JPanel jpNorth = new JPanel(new BorderLayout());
		JPanel jpNLeft = new JPanel(new BorderLayout());
		jpNorth.add(jpNLeft, BorderLayout.LINE_START);

		// Model selection list
		List<LogicalModel> modelList = new ArrayList<LogicalModel>(
				this.epithelium.getEpitheliumGrid().getModelSet());
		String[] saSBML = new String[modelList.size()];
		for (int i = 0; i < modelList.size(); i++) {
			saSBML[i] = this.modelFeatures.getName(modelList.get(i));
		}
		JComboBox<String> jcbSBML = new JComboBox<String>(saSBML);
		jcbSBML.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				@SuppressWarnings("unchecked")
				JComboBox<String> jcb = (JComboBox<String>) e.getSource();
				updateComponentList((String) jcb.getSelectedItem());
				// Re-Paint
				getParent().repaint();
			}
		});
		JPanel jpNLTop = new JPanel();
		jpNLTop.add(jcbSBML);
		jpNLeft.add(jpNLTop, BorderLayout.NORTH);
		ButtonGroup group = new ButtonGroup();
		for (LogicalModel m : modelList) {
			for (NodeInfo node : m.getNodeOrder()) {
				if (!node.isInput())
					continue;
				String nodeID = node.getNodeID();
				JRadioButton jrb = new JRadioButton(nodeID);
				jrb.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						JRadioButton jrb = (JRadioButton) e.getSource();
						activeNodeID = jrb.getText();
						updateNodeID(jrb.getText());
						// Re-Paint
						getParent().repaint();
					}
				});
				this.mNode2RadioButton.put(nodeID, jrb);
				group.add(jrb);
			}
		}

		// Component selection list
		this.jpNLBottom = new JPanel(new GridBagLayout());
		this.jpNLBottom.setBorder(BorderFactory
				.createTitledBorder("Input components"));
		jpNLeft.add(this.jpNLBottom, BorderLayout.CENTER);

		JPanel jpNRight = new JPanel(new BorderLayout());
		jpNorth.add(jpNRight, BorderLayout.CENTER);
		this.center.add(jpNorth, BorderLayout.NORTH);

		this.jpNRTop = new JPanel(new FlowLayout());
		jpNRight.add(this.jpNRTop, BorderLayout.NORTH);
		this.jpNRBottom = new JPanel(new GridBagLayout());
		jpNRight.add(this.jpNRBottom, BorderLayout.CENTER);

		// South Panel
		JPanel jpSouth = new JPanel(new BorderLayout());
		jpSouth.add(new JLabel("Explanation missing"));
		this.center.add(jpSouth, BorderLayout.SOUTH);
		this.updateComponentList((String) jcbSBML.getSelectedItem());
	}

	private void updateNodeID(String nodeID) {
		this.jpNRTop.removeAll();
		ButtonGroup group = new ButtonGroup();
		this.jpNRTop.add(new JLabel(nodeID + ": "));
		JRadioButton jrEnv = new JRadioButton("Environment");
		jrEnv.setToolTipText(nodeID);
		jrEnv.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JRadioButton jrb = (JRadioButton) e.getSource();
				paintEnvironmentPanel(jrb.getToolTipText());
				// Re-Paint
				getParent().repaint();
			}
		});
		group.add(jrEnv);
		this.jpNRTop.add(jrEnv);
		JRadioButton jrInt = new JRadioButton("Integration");
		jrInt.setToolTipText(nodeID);
		jrInt.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JRadioButton jrb = (JRadioButton) e.getSource();
				paintIntegrationPanel(jrb.getToolTipText());
				// Re-Paint
				getParent().repaint();
			}
		});
		group.add(jrInt);
		this.jpNRTop.add(jrInt);
		if (this.userIntegrationFunctions.containsKey(nodeID)) {
			jrInt.setSelected(true);
			paintIntegrationPanel(nodeID);
		} else {
			jrEnv.setSelected(true);
			paintEnvironmentPanel(nodeID);
		}
	}

	private void paintIntegrationPanel(String nodeID) {
		// GUI
		this.jpNRBottom.removeAll();

		if (!this.userIntegrationFunctions.containsKey(nodeID)) {
			NodeInfo node = this.epithelium.getComponentFeatures().getNodeInfo(
					nodeID);
			this.userIntegrationFunctions.addComponent(node);
		}
		ComponentIntegrationFunctions cfi = this.userIntegrationFunctions
				.getComponentIntegrationFunctions(nodeID);

		List<String> functions = cfi.getFunctions();
		GridBagConstraints gbc = new GridBagConstraints();
		for (int i = 0; i < functions.size(); i++) {
			gbc.gridy = i;
			gbc.gridx = 0;
			gbc.anchor = GridBagConstraints.WEST;
			this.jpNRBottom.add(new JLabel("Level " + (i + 1)), gbc);
			gbc.gridx = 1;
			JTextField jtf = new JTextField(functions.get(i));
			jtf.setToolTipText("" + (i + 1));
			jtf.addKeyListener(new KeyListener() {
				@Override
				public void keyTyped(KeyEvent e) {
				}

				@Override
				public void keyReleased(KeyEvent e) {
					JTextField jtf = (JTextField) e.getSource();
					byte value = Byte.parseByte(jtf.getToolTipText());

					setIntegrationFunction(activeNodeID, value, jtf.getText());
					validateIntegrationFunction(jtf);
				}

				@Override
				public void keyPressed(KeyEvent e) {
				}
			});
			validateIntegrationFunction(jtf);
			jtf.setColumns(20);
			this.jpNRBottom.add(jtf, gbc);
		}
	}

	private void setIntegrationFunction(String nodeID, byte level,
			String function) {

		ComponentIntegrationFunctions cif = this.userIntegrationFunctions
				.getComponentIntegrationFunctions(nodeID);
		cif.setFunctionAtLevel(level, function);
	}

	private void validateIntegrationFunction(JTextField jtf) {
		ComponentIntegrationFunctions cif = this.userIntegrationFunctions
				.getComponentIntegrationFunctions(this.activeNodeID);
		byte value = Byte.parseByte(jtf.getToolTipText());
		if (jtf.getText().trim().isEmpty() || cif.isValidAtLevel(value)) {
			jtf.setBackground(Color.WHITE);
		} else {
			jtf.setBackground(ColorUtils.LIGHT_RED);
		}
	}

	private void paintEnvironmentPanel(String nodeID) {
		this.userIntegrationFunctions.removeComponent(nodeID);
		this.jpNRBottom.removeAll();
	}

	private void updateComponentList(String sModel) {
		this.jpNLBottom.removeAll();
		LogicalModel m = this.modelFeatures.getModel(sModel);

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(1, 5, 1, 0);
		Set<String> sInputs = this.epithelium.getComponentFeatures()
				.getModelComponents(m, true);
		List<String> lInputs = new ArrayList<String>(sInputs);
		Collections.sort(lInputs, new Comparator<String>() {
			public int compare(String s1, String s2) {
				return s1.compareToIgnoreCase(s2);
			}
		});
		int y = 0;
		for (String nodeID : lInputs) {
			if (y == 0) {
				this.activeNodeID = nodeID;
				updateNodeID(nodeID);
				this.mNode2RadioButton.get(nodeID).setSelected(true);
			}
			gbc.gridy = y++;
			gbc.gridx = 0;
			gbc.anchor = GridBagConstraints.WEST;
			this.jpNLBottom.add(this.mNode2RadioButton.get(nodeID), gbc);
		}
	}

	@Override
	protected void buttonReset() {
		this.userIntegrationFunctions = this.epithelium
				.getIntegrationFunctions().clone();
		this.updateNodeID(this.activeNodeID);
		// Repaint
		this.getParent().repaint();
	}

	@Override
	protected void buttonAccept() {
		for (String nodeID : mNode2RadioButton.keySet()) {
			ComponentIntegrationFunctions cifClone = this.userIntegrationFunctions
					.getComponentIntegrationFunctions(nodeID);
			EpitheliumIntegrationFunctions eifOrig = this.epithelium
					.getIntegrationFunctions();
			if (cifClone == null) {
				eifOrig.removeComponent(nodeID);
			} else {
				NodeInfo node = this.epithelium.getComponentFeatures()
						.getNodeInfo(nodeID);
				eifOrig.addComponent(node);
				for (byte i = 1; i <= node.getMax(); i++) {
					eifOrig.getComponentIntegrationFunctions(nodeID)
							.setFunctionAtLevel(i,
									cifClone.getFunctions().get(i - 1));
				}
			}
		}
	}

	@Override
	protected boolean isChanged() {
		for (String nodeID : mNode2RadioButton.keySet()) {
			ComponentIntegrationFunctions cifClone = this.userIntegrationFunctions
					.getComponentIntegrationFunctions(nodeID);
			ComponentIntegrationFunctions cifOrig = this.epithelium
					.getIntegrationFunctions()
					.getComponentIntegrationFunctions(nodeID);
			System.out.println(nodeID);
			if (cifClone == null && cifOrig == null)
				continue;
			if (cifClone == null && cifOrig != null || cifClone != null
					&& cifOrig == null)
				return true;
			if (!cifOrig.equals(cifClone))
				return true;
		}
		return false;
	}
}