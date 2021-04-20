package org.epilogtool.gui.dialog;

import java.io.IOException;
import java.util.Set;

import org.colomoto.biolqm.LogicalModel;
import org.colomoto.biolqm.NodeInfo;
import org.epilogtool.common.Txt;
import org.epilogtool.core.Epithelium;
import org.epilogtool.io.Parser;
import org.epilogtool.project.Project;

public class EpitheliumUpdateEditByTextDialog extends DialogEditByText {

	public EpitheliumUpdateEditByTextDialog(Epithelium epi) {
		super(epi);
	}

	@Override
	public String getHelpText() {
		
		String header = Txt.get("s_HELP_PARSING");

		String syntax = "AS [FLOAT: ALPHA VALUE] <br> " + 
			"CU [cells to update: \"Only updatable cells\" OR \"All cells\"] <br>" +
			"SD [Seed generator: \"Random\" OR \"Fixed\"] <br> <br>" +
			"<b>Examples:</b> <br>" 	+ 
			"AS 1.0 for a synchronous update (alpha=1.0) <br> "+
			"AS 0.0	for an asynchronous update (alpha=0.0) <br>"+
			"AS 0.5 for an alpha-asynchronous update (alpha=0.5)<br> ";
		
		
		return "<html><div style='text-align: left;'> <b>"  + 
				header + "</b><br>" + syntax + "</div></html>";
		
	}
	

	@Override
	public void getParsing() {
		this.def.setText(Parser.getTextFormatEpitheliumUpdateMode(this.epi));
	}

	@Override
	public boolean parse(String textarea, boolean save) throws NumberFormatException, IOException {
		return Parser.parseEpitheliumUpdateMode(epi, textarea, save);
	}

	@Override
	public String getTabName() {
		return TAB_EPIUPDATING;
	}


}
