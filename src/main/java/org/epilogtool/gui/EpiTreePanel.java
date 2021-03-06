package org.epilogtool.gui;

import java.awt.BorderLayout;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JMenu;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
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

import org.epilogtool.common.Txt;
import org.epilogtool.core.Epithelium;
import org.epilogtool.gui.menu.ByTextEpiTreePopupMenu;
import org.epilogtool.gui.menu.EpiTreePopupMenu;
import org.epilogtool.gui.tab.EpiTab;
import org.epilogtool.gui.tab.EpiTabDefinitions;
import org.epilogtool.project.Project;

public class EpiTreePanel extends JPanel {
	private static final long serialVersionUID = -2143708024027520789L;

	private JScrollPane scrollTree;
	private JMenu epiMenu;
	private JMenu toolsMenu;
	private JTree epiTree;
	private EpiTreePopupMenu popupmenu;
	private ByTextEpiTreePopupMenu popupText;

	public EpiTreePanel(JMenu epiMenu, JMenu toolsMenu) {
		this.epiMenu = epiMenu;
		this.toolsMenu = toolsMenu;
		this.epiTree = null;
		this.popupmenu = new EpiTreePopupMenu();
		this.popupText = new ByTextEpiTreePopupMenu();
	
		this.setLayout(new BorderLayout());
		this.add(EpiLogGUIFactory.getJLabelBold(Txt.get("s_EPITREE_PANEL_TITLE")), 
				BorderLayout.PAGE_START);
		this.scrollTree = new JScrollPane(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		this.add(this.scrollTree, BorderLayout.CENTER);
	}

	public void initEpitheliumJTree() {
		DefaultMutableTreeNode root = new DefaultMutableTreeNode("Epithelium list:");
		this.epiTree = new JTree(root);

		ToolTipManager.sharedInstance().registerComponent(this.epiTree);
		ToolTipManager.sharedInstance().setDismissDelay(60000); // 1 min tooltip
		TreeCellRenderer renderer = new ToolTipTreeCellRenderer();
		this.epiTree.setCellRenderer(renderer);

		this.epiTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		this.scrollTree.setViewportView(this.epiTree);
		
		
		this.epiTree.addMouseListener(new MouseListener() {
			@Override
			public void mouseReleased(MouseEvent e) {
				// Windows
				checkPopUp(e);    
			}

			@Override
			public void mousePressed(MouseEvent e) {
				// Linux and Mac
				checkPopUp(e);    
			};

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

	public void selectTabJTreePath(TreePath path) {
		if (this.epiTree.isPathSelected(path)) {
			return;
		}
		this.epiTree.setSelectionPath(path);
	}
	
	public DefaultMutableTreeNode getSelectedNode() {
		return (DefaultMutableTreeNode) this.epiTree.getLastSelectedPathComponent();
	}

	public TreePath getSelectionPath() {
		return this.epiTree.getSelectionPath();
	}

	public TreePath getSelectionEpiPath() {
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) this.epiTree.getLastSelectedPathComponent();
		TreePath path = this.getSelectionPath();
		if (node.isLeaf()) {
			path = path.getParentPath();
		}
		return path;
	}
	
	public Epithelium getSelectedEpithelium() {
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) this.epiTree.getLastSelectedPathComponent();
		if (node == null)
			return null;
		if (node.isLeaf()) {
			// Simulation menu gets an Epithelium even if selection is on a leaf
			node = (DefaultMutableTreeNode) node.getParent();
		}
		return (Epithelium) node.getUserObject();
	}

	public void remove() {
		// Remove from JTree
		DefaultTreeModel model = (DefaultTreeModel) this.epiTree.getModel();
		DefaultMutableTreeNode root = (DefaultMutableTreeNode) model.getRoot();
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) this.epiTree.getLastSelectedPathComponent();
		root.remove(node);
		model.reload();
		if (root.getChildCount() == 0) {
			this.initEpitheliumJTree();
		}
		this.validateJTreeExpansion();
	}

	public void validateJTreeExpansion() {
		if (this.epiTree != null) {
			DefaultTreeModel model = (DefaultTreeModel) this.epiTree.getModel();
			model.reload();
			for (int i = 0; i < this.epiTree.getRowCount(); i++) {
				this.epiTree.expandRow(i);
			}
			this.epiTree.setRootVisible(false);
		}
	}

	public void setSelectionPath(TreePath path) {
		this.epiTree.setSelectionPath(path);
	}

	public void updateEpiMenuItems() {
		this.validateTreeNodeSelection();
	}

	private void validateTreeNodeSelection() {
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) this.epiTree.getLastSelectedPathComponent();
		boolean bActive = node != null && !node.isLeaf() && !node.isRoot();
		//TODO: getitem(0) should only be enabled if there is at least one SBML
		boolean hasmodel = false;
		if (Project.getInstance().getModelNames().size()>0) hasmodel = true;
		this.epiMenu.getItem(0).setEnabled(hasmodel);
		this.epiMenu.getItem(1).setEnabled(bActive);
		this.epiMenu.getItem(2).setEnabled(bActive);
		this.epiMenu.getItem(3).setEnabled(bActive);
		this.popupmenu.notifySelection(this.epiMenu.isEnabled(), bActive);
		
		bActive = node != null && !node.isRoot();
		this.toolsMenu.getItem(0).setEnabled(bActive); // Simulation
	}

	public void addEpi2JTree(Epithelium epi) {
		DefaultMutableTreeNode epiNode = new DefaultMutableTreeNode(epi);
		((DefaultMutableTreeNode) this.epiTree.getModel().getRoot()).add(epiNode);

		DefaultMutableTreeNode gm = new DefaultMutableTreeNode(EpiTab.TAB_MODELGRID);
		epiNode.add(gm);
		DefaultMutableTreeNode it = new DefaultMutableTreeNode(EpiTab.TAB_INTEGRATION);
		epiNode.add(it);
		DefaultMutableTreeNode ph = new DefaultMutableTreeNode(EpiTab.TAB_PHENOTYPES);
		epiNode.add(ph);
		DefaultMutableTreeNode ic = new DefaultMutableTreeNode(EpiTab.TAB_INITCONDITIONS);
		epiNode.add(ic);
		DefaultMutableTreeNode ptc = new DefaultMutableTreeNode(EpiTab.TAB_PERTURBATIONS);
		epiNode.add(ptc);
		DefaultMutableTreeNode mu = new DefaultMutableTreeNode(EpiTab.TAB_PRIORITIES);
		epiNode.add(mu);
		DefaultMutableTreeNode eu = new DefaultMutableTreeNode(EpiTab.TAB_EPIUPDATING);
		epiNode.add(eu);
	
		if (EpiGUI.getInstance().getDeveloperMode()) {
			DefaultMutableTreeNode cd = new DefaultMutableTreeNode(EpiTab.TAB_CELLDIVISION);
			epiNode.add(cd);
		}

		this.epiTree.setRootVisible(false);
		DefaultTreeModel model = (DefaultTreeModel) this.epiTree.getModel();
		model.reload();

		this.validateJTreeExpansion();
	}

	private void checkDoubleClickEpitheliumJTree(MouseEvent e) {
		int selRow = this.epiTree.getClosestRowForLocation(e.getX(), e.getY());
		if (selRow == -1)
			return;

		TreePath selPath = this.epiTree.getClosestPathForLocation(e.getX(), e.getY());
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) selPath.getLastPathComponent();
		// Only opens tabs for leafs
		if (!node.isLeaf()) {
			return;
		}
		DefaultMutableTreeNode parent = (DefaultMutableTreeNode) node.getParent();

		if (parent != null) {
			EpiGUI.getInstance().openEpiTab((Epithelium) parent.getUserObject(),
					selPath, node.toString());
		}
	}
	
	private void getClosestSel(MouseEvent e) {
		 int closestRow = epiTree.getClosestRowForLocation(e.getX(), e.getY());                          
         Rectangle closestRowBounds = epiTree.getRowBounds(closestRow);
         if (closestRowBounds != null) {
        	 if(e.getY() >= closestRowBounds.getY() && 
                     e.getY() < closestRowBounds.getY() + 
                     closestRowBounds.getHeight()) {
             	
             		if(e.getX() > closestRowBounds.getX() && 
                         closestRow < epiTree.getRowCount()){
                 	epiTree.setSelectionRow(closestRow);                                              }
             } else {
             	epiTree.setSelectionRow(-1);
    				return;
    			}
         }
	}
	private void checkPopUp(MouseEvent e) {
		getClosestSel(e);
		  
		if (e.getClickCount() == 2) {
			checkDoubleClickEpitheliumJTree(e);
		} else if(e.isPopupTrigger()) {
			openPopUps(e);
		}
	}
	
	
	
	private void openPopUps(MouseEvent e) {
			// popupmenu.updateMenuItems(listSBMLs.getSelectedValue() !=
			// null);
			// Only opens tabs for leafs
			TreePath selPath = epiTree.getClosestPathForLocation(e.getX(), e.getY());
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) selPath.getLastPathComponent();
			
			if (node.isLeaf()) {
				if (node.toString().equals(EpiTab.TAB_PHENOTYPES) ||
						node.toString().equals(EpiTab.TAB_INTEGRATION) ||
						node.toString().equals(EpiTab.TAB_PRIORITIES) ||
						node.toString().equals(EpiTab.TAB_EPIUPDATING))
					popupText.show(e.getComponent(), e.getX(), e.getY());
			} else {
				popupmenu.show(e.getComponent(), e.getX(), e.getY());
			}				
	}

}