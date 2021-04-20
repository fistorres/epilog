package org.epilogtool.gui.dialog;

import java.io.IOException;
import java.util.Set;

import org.colomoto.biolqm.LogicalModel;
import org.colomoto.biolqm.NodeInfo;
import org.epilogtool.common.Txt;
import org.epilogtool.core.Epithelium;
import org.epilogtool.io.Parser;
import org.epilogtool.project.Project;

public class CellularModelUpdateEditByTextDialog extends DialogEditByText {
	
	public CellularModelUpdateEditByTextDialog(Epithelium epi) {
		super(epi);
	}
	
	@Override
	public String getHelpText() {
		
		String varOder = Txt.get("s_VAR_ORDER_HELP_PARSING");
		
		String header = Txt.get("s_HELP_PARSING");
		String vars = "";
		Set<LogicalModel> modelSet = this.epi.getEpitheliumGrid().getModelSet();
		for (LogicalModel m : modelSet) {
			vars += "Model: ";
			String modelName = Project.getInstance().getInstance().getModelName(m);
			vars += modelName.substring(0, modelName.length() - 5);
			vars += ":  ";
			int size = 0;
			for (NodeInfo var: m.getComponents()) {
				vars += var.getNodeID();
				vars += ",";
			}
			vars = vars.substring(0, vars.length() - 1);
			vars += "<br>";
		}

		String syntax = " PC [MODEL NAME] [PRIORITIES CLASSES] <br><br>" +
		"<b>Priorities class syntax:</b> <br> VARS: \",\" , CLASS: \":\" (comma), UPDATER: \"$\"  <br>" +
		"<b>Updaters:</b><br>  RN - Random non uniform, RU - Random uniform, S - Synchronous (default) <br><br>" + 
		"<b>Example: </b> <br> PR model.sbml X,Y:Z,W$RN[1.0,1.0,2.0,2.0]"; 
//		"<b>RN is followed by ALL the components rates in the MODEL order, e.g: </b><br> $RN[CR1[-],CR1[+],CR2[-],CR2[+],CR3[-],CR3[+]] <br> <br>" +
//		"<b>Example:</b> <br> C1,C2[+],C3[-]$RN[1.0,1.0,null,3,5,null]:C2[-],C3[+]$RU";
	
		 
		return "<html><div style='text-align: left;'> <b>" + varOder + "</b> <br>" + vars + "<br><b>" + 
				header + "</b><br>" + syntax + "</div></html>";
	
		
	}

	@Override
	public void getParsing() {
		this.def.setText(Parser.getTextFormatCellularUpdateMode(this.epi));
		
	}

	@Override
	public boolean parse(String textarea, boolean save) throws NumberFormatException, IOException {
		return Parser.parseCelullarUpdateMode(epi, textarea, save);
	}

	@Override
	public String getTabName() {
		return TAB_PRIORITIES;
	}

}
