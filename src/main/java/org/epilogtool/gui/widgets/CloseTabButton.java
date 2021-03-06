package org.epilogtool.gui.widgets;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import org.epilogtool.gui.tab.EpiTab;
import org.epilogtool.io.ButtonFactory;

public class CloseTabButton extends JPanel {
	private static final long serialVersionUID = -2812970403104281880L;
	
	private JLabel jLabel;

	public CloseTabButton(String title, final JTabbedPane tabPane) {
		this.setLayout(new GridBagLayout());
		// pnlTab.setOpaque(false);
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 1;
		jLabel = new JLabel(title);
		this.add(jLabel, gbc);
		gbc.gridx++;
		gbc.weightx = 0;
		JButton closeButton = ButtonFactory
				.getImageNoBorder("button_close_gray.png");
		closeButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JButton b = (JButton) e.getSource();
				JPanel jp = (JPanel) b.getParent();
				for (int i = 0; i < tabPane.getTabCount(); i++) {
					if (tabPane.getTabComponentAt(i).equals(jp)) {
						EpiTab epi = (EpiTab) tabPane.getComponentAt(i);
						if (epi.canClose()) {
							tabPane.removeTabAt(i);
						}
					}
				}
			}
		});
		this.add(closeButton, gbc);
	}
	
	public void changeTitle(String title) {
		this.jLabel.setText(title);
		this.revalidate();
	}
}
