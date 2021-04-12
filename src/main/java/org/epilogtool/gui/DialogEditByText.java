package org.epilogtool.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.EmptyBorder;

import org.colomoto.biolqm.LogicalModel;
import org.colomoto.biolqm.NodeInfo;
import org.epilogtool.common.EnumRandomSeed;
import org.epilogtool.common.Txt;
import org.epilogtool.core.Epithelium;
import org.epilogtool.core.topology.RollOver;
import org.epilogtool.gui.EpiGUI.TabChangeNotifyProj;
import org.epilogtool.gui.color.ColorUtils;
import org.epilogtool.gui.dialog.EscapableDialog;
import org.epilogtool.project.Project;

public abstract class DialogEditByText extends EscapableDialog {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 2736187748639861895L;
	
	public static final String TAB_INTEGRATION = Txt.get("s_TAB_INTEGRATION");
	public static final String TAB_PRIORITIES = Txt.get("s_TAB_PRIORITIES");
	public static final String TAB_EPIUPDATING = Txt.get("s_TAB_EPIUPDATING");
	public static final String TAB_PHENOTYPES = Txt.get("s_TAB_SELECTPHENOTYPE");
	
	public Epithelium epi;
	
	public JTextArea def;
	public JPanel help;
	
	public JLabel helpText;
	
	public JButton saveAs;
	public JButton loadFrom;
	public JButton applyAndClose;
	public JButton buttonCancel;
	
	public JPanel jpSouth;
	public JPanel jpCenter;

	private boolean parseOK;
	
	// private somethingWithParserObjectInterfase objectToModify;
	
	
	protected DialogEditByText(Epithelium epi) {
		this.epi = epi;
		this.setLayout(new BorderLayout());
		
		this.saveAs = new JButton(Txt.get("s_SAVEAS"));
		this.saveAs.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

			try {
				EpiGUI.getInstance().saveAsTxt(def.getText());
				close();
			} catch (Exception ex) {
				EpiGUI.getInstance().userMessageError(Txt.get("s_MENU_CANNOT_SAVE"), Txt.get("s_MENU_SAVE_AS"));
			}
		}
	});
		
		this.loadFrom = new JButton(Txt.get("s_LOADFROM"));
		this.loadFrom.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

			try {
				EpiGUI.getInstance().loadTxt(getThis());
			} catch (Exception ex) {
				EpiGUI.getInstance().userMessageError(Txt.get("s_MENU_CANNOT_SAVE"), Txt.get("s_MENU_SAVE_AS"));
			}
			}
		});
		
		this.applyAndClose = new JButton(Txt.get("s_APPLYANDCLOSE"));
		this.applyAndClose.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
			try {
				helpParse(def.getText(), true);
//				EpiGUI.getInstance().alertEditByTextChanges(getThis());
				close();
			} catch (NumberFormatException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			}
		});
		
		this.buttonCancel = new JButton(Txt.get("s_CANCEL"));
		this.buttonCancel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				close();
			}
		});

		this.jpSouth = new JPanel();
		this.jpSouth.add(this.saveAs);
		this.jpSouth.add(this.loadFrom);
		this.jpSouth.add(this.applyAndClose);
		this.jpSouth.add(this.buttonCancel);
		
		this.add(jpSouth, BorderLayout.SOUTH);
		
		this.jpCenter = new JPanel();
		this.jpCenter.setLayout(new BoxLayout(this.jpCenter,BoxLayout.PAGE_AXIS));
		this.jpCenter.setBorder(new EmptyBorder(10, 10, 0, 10));


		this.def = new JTextArea();
		this.def.addKeyListener(new KeyListener() {
			@Override
			public void keyReleased(KeyEvent e) {
				try {
					validateTextField();
				} catch (NumberFormatException | IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}

			@Override
			public void keyTyped(KeyEvent e) {
			}

			@Override
			public void keyPressed(KeyEvent e) {
			}
		});

		JScrollPane jScroll = new JScrollPane(this.def);
		jScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		this.jpCenter.add(jScroll);
		
		this.help = new JPanel();
		this.help.setBorder(BorderFactory.createTitledBorder("HELP"));
		
		this.helpText = new JLabel();
		this.help.add(this.helpText);
		
		this.jpCenter.add(this.help);
		this.add(jpCenter, BorderLayout.CENTER);
		
		this.setHelpText();;
		this.getParsing();

	}
	
	public abstract String getTabName();

	public DialogEditByText getThis() {
		return this;
	}
	
	public void setHelpText() {
		String varOder = Txt.get("s_VAR_ORDER_HELP_PARSING");
		String vars = "";
		Set<LogicalModel> modelSet = this.epi.getEpitheliumGrid().getModelSet();
		for (LogicalModel m : modelSet) {
			vars += Project.getInstance().getInstance().getModelName(m);
			vars += ":  ";
			int size = 0;
			for (NodeInfo var: m.getComponents()) {
				vars += var.getNodeID();
				vars += ",";
			}

			vars = vars.substring(0, vars.length() - 1);
			vars += "<br>";
		}
		
		String header = Txt.get("S_HELP_PARSING");
		String unique = this.getHelpText();
		this.helpText.setText(
				"<html>" + varOder + "<br>" + vars + "<br>" + header + "<br>" + unique + "</html>");
	}
	
	public abstract String getHelpText();
	
	public abstract void getParsing();
	
	public void loadFile(String filename) throws IOException {
		this.def.setText("");
		FileInputStream fstream = new FileInputStream(filename);
		DataInputStream in = new DataInputStream(fstream);
		BufferedReader br = new BufferedReader(new InputStreamReader(in));

		String line = null;

		while ((line = br.readLine()) != null) {
			this.def.append(line);
		}
		
		this.validateTextField();
	}
	
	private void validateTextField() throws NumberFormatException, IOException {
		this.parseOK = false;
		if (this.parse(this.def.getText(), false)) 
			this.parseOK = true;
		this.def.setBackground(this.parseOK ? Color.WHITE
				: ColorUtils.LIGHT_RED);
		this.validConfig();
	}
	
	public void helpParse(String textarea, boolean save) throws NumberFormatException, IOException {
		for (String line : textarea.split("\\n"))
			parse(line, save);
	}
	
	
	public abstract boolean parse(String textarea, boolean save) throws NumberFormatException, IOException;
		
	public void close() {
		this.dispose();
	};
	
	public void validConfig() {
		this.applyAndClose.setEnabled(this.parseOK);
	}
	
	@Override
	public void focusComponentOnLoad() {
		// TODO Auto-generated method stub
		
	}
}
