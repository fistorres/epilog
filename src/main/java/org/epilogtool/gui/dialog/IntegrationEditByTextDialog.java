package org.epilogtool.gui.dialog;

import java.io.IOException;
import java.util.Set;

import org.colomoto.biolqm.LogicalModel;
import org.colomoto.biolqm.NodeInfo;
import org.epilogtool.common.Txt;
import org.epilogtool.core.Epithelium;
import org.epilogtool.io.Parser;
import org.epilogtool.project.Project;

public class IntegrationEditByTextDialog extends DialogEditByText {

	public IntegrationEditByTextDialog(Epithelium epi) {
		super(epi);
		
	}

	
	@Override
	public String getHelpText() {
		
		String varOder = Txt.get("s_VAR_INPUT_HELP_PARSING");
		
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
				if (var.isInput()) {
					vars += var.getNodeID();
					vars += ",";
				}
			}
			vars = vars.substring(0, vars.length() - 1);
			vars += "<br>";
		}
		
		String syntax = "IT  [MODEL NAME]" +
		 "[NODE NAME]  [NODE LEVEL]  [Integration function] <br><br> " +
		 "<b>See the documentation for detail on the Integration function syntax: </b> <br> " +
		 "<b> Examples: </b> <br>" + 
		 "IF Z 1 {X} & {Y:2}       -> “input Z is 1 if there isat least 1 neighbouring cell with X "
		 + "at 1 and (possibly another cell) with Y at 2” <br> " +
		 "IF Z 1 {X,max=2}      -> “input Z is 1 if there are at most2 neighbouring cells with X at 1 <br> " +
		 "IF Z 2:{X[1 : 2],min=6 } | {Y:2} -> “input Z is 2 if there are at least 6 cells at distance 1 or 2" +
		  " with X at 1 OR at least one neighbouring cell with Y at 2”";
		 
		return "<html><div style='text-align: left;'> <b>" + varOder + "</b> <br>" + vars + "<br><b>" + 
				header + "</b><br>" + syntax + "</div></html>";
	}
	
//	@Override
//	public String getHelpText() {
//		String helpText = "IT  [MODEL NAME]" +
//		 "[NODE NAME]  [NODE LEVEL]  [Integration function] <br><br> " +
//		 "<b>IT function: </b> <br> CC = {signaling term, min #cell, max #cell} <br>" + 
//		 "<b>Signaling term: </b><br> component : level[min distance: max distance] <br><br>" +
//		 "<b> Example:</b> ({g1} & {g2 : 2, max = 4}) | {g1 : 2[2 :], min = 4} <br> " +
//		 "<b> Meaning:</b> At-least-1 cell at distance 1 with g1 at minimum level 1 and atmost-4 <br> " +
//		 "cells at distance 1 with g2 at minimum level 2, or at-least-4 cells at distance at <br> " +
//		 "least 2 with g1 at minimum level 2.";
//		 
//		return helpText;
//	}

	@Override
	public void getParsing() {
		this.def.setText(Parser.getTextFormatInputDef(this.epi));
	}

	@Override
	public boolean parse(String textarea, boolean save) throws NumberFormatException, IOException {
		return Parser.parseInputDef(epi, textarea, save);
	}

	@Override
	public String getTabName() {
		return TAB_INTEGRATION;
	}


}
