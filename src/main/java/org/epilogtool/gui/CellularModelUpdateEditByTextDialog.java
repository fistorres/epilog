package org.epilogtool.gui;

import java.io.IOException;

import org.epilogtool.common.Txt;
import org.epilogtool.core.Epithelium;
import org.epilogtool.io.Parser;

public class CellularModelUpdateEditByTextDialog extends DialogEditByText {
	
	CellularModelUpdateEditByTextDialog(Epithelium epi) {
		super(epi);
	}
	
	@Override
	public String getHelpText() {
		return Txt.get("s_PRIORITIES_HELP_PARSING");
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
