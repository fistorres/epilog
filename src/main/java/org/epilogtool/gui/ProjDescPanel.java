package org.epilogtool.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.epilogtool.gui.menu.SBMLPopupMenu;

/**
 * Container with all the SBML. It is here that it is defined if the options in the menu bar are enabled,
 * the dimensions of the panel with the list of SBML, visual operations of loading, deleting and renaming SBML.
 * 
 *
 */
public class ProjDescPanel extends JPanel {
	private static final long serialVersionUID = -8691538114476162311L;

	private static final String LABEL = "Intra-cellular models: ";
	private JList<String> listSBMLs;
	private JMenu menu;
	private SBMLPopupMenu popupmenu;

	public ProjDescPanel(JMenu sbmlMenu) {
		this.menu = sbmlMenu;
		this.popupmenu = new SBMLPopupMenu();
		this.setLayout(new BorderLayout());

		// PAGE_START
		this.add(EpilogGUIFactory.getJLabelBold(LABEL), BorderLayout.PAGE_START);

		// CENTER
		ListModel<String> listModel = new DefaultListModel<String>();
		this.listSBMLs = new JList<String>(listModel);
		this.listSBMLs.addMouseMotionListener(new MouseMotionListener() {
			@Override
			public void mouseMoved(MouseEvent e) {
				@SuppressWarnings("rawtypes")
				JList l = (JList) e.getSource();
				int index = l.locationToIndex(e.getPoint());
				if (index > -1) {
					l.setToolTipText(l.getModel().getElementAt(index)
							.toString());
				}
			}

			@Override
			public void mouseDragged(MouseEvent e) {
			}
		});
		this.listSBMLs.addMouseListener(new MouseListener() {
			@Override
			public void mouseReleased(MouseEvent e) {
			}

			@Override
			public void mousePressed(MouseEvent e) {
				if (e.isPopupTrigger()) {
					popupmenu.updateMenuItems(listSBMLs.getSelectedValue() != null);
					popupmenu.show(e.getComponent(), e.getX(), e.getY());
				}
			}

			@Override
			public void mouseExited(MouseEvent e) {
			}

			@Override
			public void mouseEntered(MouseEvent e) {
			}

			@Override
			public void mouseClicked(MouseEvent e) {
			}
		});
		this.listSBMLs.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		ListSelectionModel lsModel = this.listSBMLs.getSelectionModel();
		lsModel.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				updateSBMLMenuItems();
			}
		});
		JScrollPane scroll = new JScrollPane(this.listSBMLs);
		scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		scroll.setMinimumSize(new Dimension(0, 100));
		this.add(scroll, BorderLayout.CENTER);
	}

	/**Updates the SBML Menu in the tool bar. 
	 * At any time a new SBML can be loaded (always enabled); 
	 * Operations remove, rename or export are only enabled there is at least a SBML model loaded
	 * The user can one replace a model if there are at-least 2 SBML models loaded.
	 */
	public void updateSBMLMenuItems() {
		menu.getItem(0).setEnabled(true);
		menu.getItem(1).setEnabled(
				this.listSBMLs.getSelectionModel().getMinSelectionIndex() >= 0);
		menu.getItem(2).setEnabled(
				this.listSBMLs.getSelectionModel().getMinSelectionIndex() >= 0);
		menu.getItem(3).setEnabled(
				this.listSBMLs.getSelectionModel().getMinSelectionIndex() >= 0);
		menu.getItem(4).setEnabled(
				this.listSBMLs.getSelectionModel().getMinSelectionIndex() >= 1);
	}

	public void loadModel(String model) {
		if (model.isEmpty() || this.hasModel(model))
			return;
		((DefaultListModel<String>) this.listSBMLs.getModel())
				.addElement(model);
	}

	public void removeModel(String model) {
		((DefaultListModel<String>) this.listSBMLs.getModel())
				.removeElement(model);
	}
	
	public void renameModel(String model) {
	 int index = this.listSBMLs.getSelectedIndex();
	 ((DefaultListModel<String>) this.listSBMLs.add(model, index);
	}

	public boolean hasModel(String model) {
		return ((DefaultListModel<String>) this.listSBMLs.getModel())
				.contains(model);
	}

	public int countModels() {
		return this.listSBMLs.getModel().getSize();
	}

	public String getSelected() {
		return this.listSBMLs.getSelectedValue();
	}

	public void clean() {
		while (this.countModels() > 0) {
			String model = this.listSBMLs.getModel().getElementAt(0);
			this.removeModel(model);
		}
	}

}
