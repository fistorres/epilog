package org.epilogtool.gui.dialog;

import java.io.IOException;
import java.util.Set;

import org.colomoto.biolqm.LogicalModel;
import org.colomoto.biolqm.NodeInfo;
import org.epilogtool.common.Txt;
import org.epilogtool.core.Epithelium;
import org.epilogtool.core.EpitheliumPhenotypes;
import org.epilogtool.io.Parser;
import org.epilogtool.project.Project;

public class PhenotypesEditByTextDialog extends DialogEditByText {

	public PhenotypesEditByTextDialog(Epithelium epi) {
		super(epi);
	}

	@Override
	public String getHelpText() {

		String varOder = Txt.get("s_VAR_ORDER_HELP_PARSING");
		
		String header = Txt.get("s_HELP_PARSING");
		String vars = "";
		Set<LogicalModel> modelSet = this.epi.getEpitheliumGrid().getModelSet();
		for (LogicalModel m : modelSet) {
			String modelName = Project.getInstance().getInstance().getModelName(m);
			vars += modelName.substring(0, modelName.length() - 5);
			vars += " model:  ";
			int size = 0;
			for (NodeInfo var: m.getComponents()) {
				vars += var.getNodeID();
				vars += ",";
			}
			vars = vars.substring(0, vars.length() - 1);
			vars += "<br>";
		}

		String syntax = "PH [MODEL] [PHENO NAME] " +
				"[PHENOTYPE STRING: ACCEPTS \"*\" OR VALID NODE LEVEL]";

		return "<html><div style='text-align: left;'> <b>" + varOder + "</b> <br>" + vars + "<br><b>" + 
		header + "</b><br>" + syntax + "</div></html>";

	}

	@Override
	public String getDefinitionText() {
		return Parser.getTextFormatPhenotypes(this.epi);
	}

	@Override
	public boolean parse(String textarea, boolean save) throws NumberFormatException, IOException {
		if (save)
			epi.setPhenotypes(new EpitheliumPhenotypes());
		for (String line : textarea.split("\\n"))
			if(!Parser.parsePhenotypes(epi, line, save))
				return false;
		
		return true;

	}
	@Override
	public String getTabName() {
		return DialogEditByText.TAB_PHENOTYPES;
	}
	
}
