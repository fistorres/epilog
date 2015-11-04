package org.epilogtool.gui;

import java.awt.BorderLayout;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JMenu;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.ToolTipManager;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.epilogtool.core.Epithelium;
import org.epilogtool.project.ProjectModelFeatures;

public class EpiTreePanel extends JPanel {
	private static final long serialVersionUID = -2143708024027520789L;

	private JScrollPane scrollTree;
	private JMenu menu;
	private JTree epiTree;

	public EpiTreePanel(JMenu epiMenu) {
		this.menu = epiMenu;
		this.epiTree = null;

		this.setLayout(new BorderLayout());
		this.add(EpilogGUIFactory.getJLabelBold("List of Epithelium's:"),
				BorderLayout.PAGE_START);
		this.scrollTree = new JScrollPane(
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		this.add(this.scrollTree, BorderLayout.CENTER);
	}

	public void initEpitheliumJTree(ProjectModelFeatures modelFeatures) {
		DefaultMutableTreeNode root = new DefaultMutableTreeNode(
				"Epithelium list:");
		this.epiTree = new JTree(root);

		ToolTipManager.sharedInstance().registerComponent(this.epiTree);
		TreeCellRenderer renderer = new ToolTipTreeCellRenderer(modelFeatures);
		this.epiTree.setCellRenderer(renderer);

		this.epiTree.getSelectionModel().setSelectionMode(
				TreeSelectionModel.SINGLE_TREE_SELECTION);
		this.scrollTree.setViewportView(this.epiTree);
		this.epiTree.addMouseListener(new MouseListener() {
			@Override
			public void mouseReleased(MouseEvent e) {
			}

			@Override
			public void mousePressed(MouseEvent e) {
				checkDoubleClickEpitheliumJTree(e);
			}

			@Override
			public void mouseExited(MouseEvent e) {
			}

			@Override
			public void mouseEntered(MouseEvent e) {
			}

			@Override
			public void mouseClicked(MouseEvent e) {
			}
		});
		this.epiTree.addTreeSelectionListener(new TreeSelectionListener() {
			@Override
			public void valueChanged(TreeSelectionEvent e) {
				validateTreeNodeSelection();
			}
		});
		this.epiTree.addTreeExpansionListener(new TreeExpansionListener() {
			@Override
			public void treeExpanded(TreeExpansionEvent event) {
			}

			@Override
			public void treeCollapsed(TreeExpansionEvent event) {
				validateJTreeExpansion();
			}
		});
	}

	public Epithelium getSelectedEpithelium() {
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) this.epiTree
				.getLastSelectedPathComponent();
		return (Epithelium) node.getUserObject();
	}

	public void remove(ProjectModelFeatures modelFeatures) {
		// Remove from JTree
		DefaultTreeModel model = (DefaultTreeModel) this.epiTree.getModel();
		DefaultMutableTreeNode root = (DefaultMutableTreeNode) model.getRoot();
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) this.epiTree
				.getLastSelectedPathComponent();
		root.remove(node);
		model.reload();
		if (root.getChildCount() == 0) {
			this.initEpitheliumJTree(modelFeatures);
		}
	}

	public void validateJTreeExpansion() {
		if (this.epiTree != null) {
			for (int i = 0; i < this.epiTree.getRowCount(); i++) {
				this.epiTree.expandRow(i);
			}
			this.epiTree.setRootVisible(false);
		}
	}

	public void updateEpiMenuItems() {
		this.validateJTreeExpansion();
	}

	private void validateTreeNodeSelection() {
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) this.epiTree
				.getLastSelectedPathComponent();
		boolean bActive = node != null && !node.isLeaf() && !node.isRoot();
		menu.getItem(1).setEnabled(bActive);
		menu.getItem(2).setEnabled(bActive);
		menu.getItem(3).setEnabled(bActive);
		for (int i = 0; i < this.epiTree.getRowCount(); i++) {
			this.epiTree.expandRow(i);
		}
	}

	public void addEpi2JTree(Epithelium epi) {
		DefaultMutableTreeNode epiNode = new DefaultMutableTreeNode(epi);
		((DefaultMutableTreeNode) this.epiTree.getModel().getRoot())
				.add(epiNode);

		DefaultMutableTreeNode gm = new DefaultMutableTreeNode("Model Grid");
		epiNode.add(gm);
		DefaultMutableTreeNode it = new DefaultMutableTreeNode(
				"Integration Components");
		epiNode.add(it);
		DefaultMutableTreeNode ic = new DefaultMutableTreeNode(
				"Initial Condition");
		epiNode.add(ic);
		DefaultMutableTreeNode pt = new DefaultMutableTreeNode("Perturbations");
		epiNode.add(pt);
		DefaultMutableTreeNode pr = new DefaultMutableTreeNode(
				"Updating Scheme");
		epiNode.add(pr);
		DefaultMutableTreeNode sim = new DefaultMutableTreeNode("Simulation");
		epiNode.add(sim);

		DefaultTreeModel model = (DefaultTreeModel) this.epiTree.getModel();
		model.reload();

		this.validateJTreeExpansion();
	}

	private void checkDoubleClickEpitheliumJTree(MouseEvent e) {
		if (e.getClickCount() != 2)
			return;
		int selRow = this.epiTree.getRowForLocation(e.getX(), e.getY());
		if (selRow == -1)
			return;

		TreePath selPath = this.epiTree.getPathForLocation(e.getX(), e.getY());
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) selPath
				.getLastPathComponent();
		// Only opens tabs for leafs
		if (!node.isLeaf()) {
			return;
		}
		DefaultMutableTreeNode parent = (DefaultMutableTreeNode) node
				.getParent();

		if (parent != null) {
			EpiGUI.getInstance().openEpiTab(
					(Epithelium) parent.getUserObject(), selPath,
					node.toString());
		}
	}
}