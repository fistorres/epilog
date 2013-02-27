package pt.gulbenkian.igc.nmd;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.File;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.colomoto.logicalmodel.LogicalModel;
import org.colomoto.logicalmodel.io.sbml.SBMLFormat;

import pt.gulbenkian.igc.nmd.Epithelium;
import pt.gulbenkian.igc.nmd.MainPanelDescription;
import pt.gulbenkian.igc.nmd.SphericalEpithelium;
import pt.gulbenkian.igc.nmd.teste;

public class StartPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static int DEFAULT_WIDTH = 6;
	public static int DEFAULT_HEIGHT = 6;
	private JButton restartButton;
	private JButton modelButton;
	private static JTextField userDefinedWidth = new JTextField();
	private static JTextField userDefinedHeight = new JTextField();
	private JLabel selectedFilenameLabel;
	private JFileChooser fc = new JFileChooser();
	private Epithelium epithelium = new SphericalEpithelium(DEFAULT_WIDTH,
			DEFAULT_HEIGHT);
	public static LogicalModel model = null;

	public StartPanel() {
		init();
		
	}

	private JPanel init() {

		restartButton = new JButton("Restart");
		modelButton = new JButton("Model");

		selectedFilenameLabel = new JLabel();

		FlowLayout layout = new FlowLayout();
		layout.setAlignment(FlowLayout.LEFT);
		
		userDefinedWidth.setHorizontalAlignment(JTextField.CENTER);
		userDefinedHeight.setHorizontalAlignment(JTextField.CENTER);
		userDefinedWidth.setText("" + DEFAULT_WIDTH);
		userDefinedHeight.setText("" + DEFAULT_HEIGHT);

		userDefinedWidth.addFocusListener(new FocusListener() {

			@Override
			public void focusGained(FocusEvent arg0) {
			}

			@Override
			public void focusLost(FocusEvent arg0) {
				sanityCheckDimension(userDefinedWidth);
			}
		});

		userDefinedHeight.addFocusListener(new FocusListener() {

			@Override
			public void focusGained(FocusEvent arg0) {
			}

			@Override
			public void focusLost(FocusEvent arg0) {
				sanityCheckDimension(userDefinedHeight);
			}
		});
		
		
		
		
		
		System.out.print("restasdrt");
		restartButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.out.print("restart");
				//repaint();

//				if (MainPanelDescription.RollOverPanel != null)
//					MainPanelDescription.cleanRollOverPanel();
//				selectedFilenameLabel.setText("");
			}
		});
		
		modelButton.setBounds(230, 13, 100, 30);

		selectedFilenameLabel.setBounds(335, 13, 100, 30);
		
		modelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				askModel();
				//teste.exp();

			}

		});

	

		setLayout(new FlowLayout());
		add(userDefinedWidth);
		add(userDefinedHeight);
		add(restartButton);
		add(modelButton);
		add(selectedFilenameLabel);
		return this;
	}
	
	public static int getGridWidth() {
		// sanityCheckDimension(userDefinedWidth);
		return Integer.parseInt(userDefinedWidth.getText());
	}

	public static int getGridHeight() {
		// sanityCheckDimension(userDefinedHeight);
		return Integer.parseInt(userDefinedHeight.getText());
	}

	private void sanityCheckDimension(JTextField userDefined) {
		String dimString = userDefined.getText();
		int w = Integer.parseInt(dimString);
		w = (w % 2 == 0) ? w : w + 1;
		userDefined.setText("" + w);
	}
	
	private void askModel() {
		fc.setDialogTitle("Choose file");

		if (fc.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
			selectedFilenameLabel.setText(fc.getSelectedFile().getName());
			loadModel();
			//contentPanel.removeAll();
			//setupMainPanel();
			//teste.exp();

		}
	}
	private void loadModel() {

		File file = fc.getSelectedFile();
		SBMLFormat sbmlFormat = new SBMLFormat();
		LogicalModel logicalModel = null;

		try {
			logicalModel = sbmlFormat.importFile(file);
		} catch (IOException e) {
			System.err.println("Cannot import file " + file.getAbsolutePath()
					+ ": " + e.getMessage());
		}

		if (logicalModel == null)
			return;

		epithelium.setUnitaryModel(logicalModel);
		//getButtonPanel();

	}
	
	public void setmodel(LogicalModel chosenmodel) {
		model = chosenmodel;
	}

	public static LogicalModel getmodel() {
		return model;
	}
}

