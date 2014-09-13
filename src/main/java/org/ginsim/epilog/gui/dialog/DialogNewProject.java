package org.ginsim.epilog.gui.dialog;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.ginsim.epilog.gui.color.ColorUtils;
import org.ginsim.epilog.services.TopologyService;

public class DialogNewProject extends JPanel {
	private static final long serialVersionUID = 1877338344309723137L;

	private final String DEFAULT_WIDTH = "20";
	private final String DEFAULT_HEIGHT = "20";

	private JTextField jtfWidth;
	private JTextField jtfHeight;
	private JComboBox<String> jcbLayout;
	private JList<String> listSBMLs;
	private JButton buttonRemove;
	private JButton buttonOK;

	private List<File> fileList;
	private int width;
	private int height;
	private boolean bIsOK;

	public DialogNewProject() {
		setLayout(new BorderLayout());
		this.fileList = new ArrayList<File>();

		// PAGE_START begin
		JPanel top = new JPanel(new FlowLayout());
		top.add(new JLabel("Width:"));
		jtfWidth = new JTextField(DEFAULT_WIDTH, 3);
		jtfWidth.addKeyListener(new KeyListener() {
			@Override
			public void keyReleased(KeyEvent e) {
				validateTextField(e);
			}

			@Override
			public void keyTyped(KeyEvent e) {
			}

			@Override
			public void keyPressed(KeyEvent e) {
			}
		});
		jtfWidth.addFocusListener(new FocusListener() {
			@Override
			public void focusLost(FocusEvent e) {
				validateEvenDimension(e);
			}

			@Override
			public void focusGained(FocusEvent e) {
			}
		});
		top.add(jtfWidth);
		top.add(new JLabel("Height:"));
		jtfHeight = new JTextField(DEFAULT_HEIGHT, 3);
		jtfHeight.addKeyListener(new KeyListener() {
			@Override
			public void keyReleased(KeyEvent e) {
				validateTextField(e);
			}

			@Override
			public void keyTyped(KeyEvent e) {
			}

			@Override
			public void keyPressed(KeyEvent e) {
			}
		});
		jtfHeight.addFocusListener(new FocusListener() {
			@Override
			public void focusLost(FocusEvent e) {
				validateEvenDimension(e);
			}

			@Override
			public void focusGained(FocusEvent e) {
			}
		});
		top.add(jtfHeight);
		top.add(new JLabel("Topology:"));
		DefaultComboBoxModel<String> cbModel = new DefaultComboBoxModel<String>();
		// Dynamically loads Topologies in the classpath
		for (String name : TopologyService.getManager().getTopologyNames()) {
			cbModel.addElement(name);
		}
		jcbLayout = new JComboBox<String>(cbModel);
		top.add(jcbLayout);
		this.add(top, BorderLayout.PAGE_START);

		// CENTER begin
		JPanel center = new JPanel(new BorderLayout());
		DefaultListModel<String> dlmModels = new DefaultListModel<String>();
		this.listSBMLs = new JList<String>(dlmModels);
		JScrollPane scroll = new JScrollPane(this.listSBMLs);
		scroll.setMinimumSize(new Dimension(scroll.getSize().width, 70));
		center.add(scroll, BorderLayout.CENTER);
		this.add(center, BorderLayout.CENTER);

		// PAGE_END begin
		JPanel bottom = new JPanel(new FlowLayout());
		JButton buttonAdd = new JButton("Add SBML");
		buttonAdd.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				addSBMLFile();
			}
		});
		bottom.add(buttonAdd);
		buttonRemove = new JButton("Remove SBML");
		buttonRemove.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					removeSBMLFile();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});
		bottom.add(buttonRemove);
		JSeparator sep = new JSeparator(JSeparator.VERTICAL);
		bottom.add(sep);
		JButton buttonCancel = new JButton("Cancel");
		buttonCancel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				buttonAction(false);
			}
		});
		bottom.add(buttonCancel);
		buttonOK = new JButton("OK");
		buttonOK.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				buttonAction(true);
			}
		});
		bottom.add(buttonOK);
		this.add(bottom, BorderLayout.PAGE_END);
		this.validateDialog();
	}

	private void validateTextField(KeyEvent e) {
		JTextField jtf = (JTextField) e.getSource();
		boolean valid = false;
		try {
			int x = Integer.parseInt(jtf.getText());
			if (x >= 1) {
				valid = true;
			}
		} catch (NumberFormatException nfe) {
		}
		if (!valid) {
			jtf.setBackground(ColorUtils.LIGHT_RED);
		} else {
			jtf.setBackground(Color.WHITE);
		}
		this.validateDialog();
	}

	private void validateEvenDimension(FocusEvent e) {
		JTextField jtf = (JTextField) e.getSource();
		try {
			int x = Integer.parseInt(jtf.getText());
			if (x % 2 == 1) {
				jtf.setText("" + (x + 1));
			}
		} catch (NumberFormatException nfe) {
		}
	}

	private void buttonAction(boolean bIsOK) {
		this.bIsOK = bIsOK;
		if (bIsOK && !this.validateDialog()) {
			return;
		}
		Window win = SwingUtilities.getWindowAncestor(this);
		if (win != null) {
			win.dispose();
		}
	}

	public int getProjWidth() {
		return this.width;
	}

	public int getProjHeight() {
		return this.height;
	}

	public String getTopologyLayout() {
		return (String) this.jcbLayout.getSelectedItem();
	}

	public List<File> getFileList() {
		return this.fileList;
	}

	public boolean isDefined() {
		return this.bIsOK;
	}

	private void addSBMLFile() {
		FileNameExtensionFilter xmlfilter = new FileNameExtensionFilter(
				"sbml files (*.sbml)", "sbml");
		JFileChooser fc = new JFileChooser();
		fc.setFileFilter(xmlfilter);
		fc.setDialogTitle("Open file");
		if (fc.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
			for (File f : this.fileList) {
				if (fc.getSelectedFile().getName().equals(f.getName())) {
					JOptionPane.showMessageDialog(this,
							"A model with the same name '"
									+ fc.getSelectedFile().getName()
									+ "' already exists!", "Warning",
							JOptionPane.WARNING_MESSAGE);
					return;
				}
			}
			this.fileList.add(fc.getSelectedFile());
			((DefaultListModel<String>) this.listSBMLs.getModel())
					.addElement(fc.getSelectedFile().getAbsolutePath());
		}
		this.validateDialog();
	}

	private void removeSBMLFile() throws IOException {
		DefaultListModel<String> dataModel = (DefaultListModel<String>) this.listSBMLs
				.getModel();
		int selIndex = this.listSBMLs.getSelectedIndex();
		if (selIndex != -1) {
			String name = this.listSBMLs.getSelectedValue();
			dataModel.remove(selIndex);
			for (File f : this.fileList) {
				if (f.getCanonicalPath().equals(name)) {
					this.fileList.remove(f);
					break;
				}
			}
			this.validateDialog();
		}
	}

	private boolean validateDialog() {
		boolean isValid = false;
		if (this.listSBMLs.getModel().getSize() > 0) {
			isValid = true;
		}
		this.buttonRemove.setEnabled(isValid);

		try {
			this.width = Integer.parseInt(this.jtfWidth.getText());
			this.height = Integer.parseInt(this.jtfHeight.getText());
		} catch (NumberFormatException nfe) {
			isValid = false;
		}
		this.buttonOK.setEnabled(isValid);
		return isValid;
	}
}
