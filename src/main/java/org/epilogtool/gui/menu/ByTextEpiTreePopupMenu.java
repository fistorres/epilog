package org.epilogtool.gui.menu;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import org.epilogtool.common.Txt;
import org.epilogtool.gui.EpiGUI;



public class ByTextEpiTreePopupMenu extends JPopupMenu {

	private static final long serialVersionUID = 3799923018481051985L;

	public ByTextEpiTreePopupMenu() {
		JMenuItem editByText = new JMenuItem(Txt.get("s_MENU_EPITREE_EDIT_BY_TEXT"));
		editByText.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					EpiGUI.getInstance().openEditByTextDialog();
				} catch (Exception e1) {
					// TODO: handle java reflection in the future
					e1.printStackTrace();
				}
			}
		});
		this.add(editByText);
	}
	
}