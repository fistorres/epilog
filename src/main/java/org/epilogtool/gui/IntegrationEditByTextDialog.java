package org.epilogtool.gui;

import java.io.IOException;

import org.epilogtool.common.Txt;
import org.epilogtool.core.Epithelium;
import org.epilogtool.io.Parser;

public class IntegrationEditByTextDialog extends DialogEditByText {

	IntegrationEditByTextDialog(Epithelium epi) {
		super(epi);
		
	}

	@Override
	public void getHelpText() {
		this.helpText.setText(Txt.get("s_INTEGRATION_HELP_PARSING"));

	}

	@Override
	public void getParsing() {
		this.def.setText(Parser.getTextFormatInputDef(this.epi));
	}

	@Override
	public boolean parse(String[] textarea, boolean save) throws NumberFormatException, IOException {
		return Parser.parseInputDef(epi, textarea, save);
	}

	@Override
	public String getTabName() {
		return TAB_INTEGRATION;
	}


}
