package org.ginsim.epilog.gui.tab;

import javax.swing.tree.TreePath;

import org.ginsim.epilog.core.Epithelium;
import org.ginsim.epilog.core.EpitheliumCell;

public class EpiTabPerturbations extends EpiTabDefinitions {
	private EpitheliumCell[][] userCellGrid;
	
	public EpiTabPerturbations(Epithelium e, TreePath path) {
		super(e,path);
		this.initializeGUI();
	}
	
	private void initializeGUI() {
		// Define center GUI components
	}
}
