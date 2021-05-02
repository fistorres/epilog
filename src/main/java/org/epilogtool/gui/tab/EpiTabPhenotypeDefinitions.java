package org.epilogtool.gui.tab;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.text.JTextComponent;
import javax.swing.tree.TreePath;

import org.apache.commons.lang3.ArrayUtils;
import org.colomoto.biolqm.LogicalModel;
import org.colomoto.biolqm.NodeInfo;
import org.epilogtool.common.Txt;
import org.epilogtool.core.Epithelium;
import org.epilogtool.core.EpitheliumPhenotypes;
import org.epilogtool.core.EpitheliumPhenotypes.Phenotype;
import org.epilogtool.gui.EpiGUI.TabChangeNotifyProj;
import org.epilogtool.gui.widgets.JComboWideBox;
import org.epilogtool.project.Project;

public class EpiTabPhenotypeDefinitions extends EpiTabDefinitions {
	/**
	 * 
	 */
	private static final long serialVersionUID = -8840069272712151873L;
	
	private Map<LogicalModel, PhenoTable> model2Table;
	private LogicalModel selModel;
	
	private TabProbablyChanged tpc;

//	private JButton jbTrackAll;
//	private JButton jbUntrackAll;
	private JButton jbUp;
	private JButton jbDown;
	private JButton jbClone;
	private JButton jbRemove;
	private JButton jbCreate;
	

	private JPanel jpNorth;
	private JPanel jpNorthLeft;
	private JPanel buttonsPanel;
	private JScrollPane tablePane;

	private JPanel tableControl;
	
	private EpitheliumPhenotypes userPhenotypes;
	
	private boolean reordered;
	
	public EpiTabPhenotypeDefinitions(Epithelium e, TreePath path, TabChangeNotifyProj tabChanged) {
		super(e, path, tabChanged);
	}
	
	@Override
	public void initialize() {
		// ---------------------------------------------------------------------------
		// Model selection jcomboCheckBox * Code copied from EpiTabCellularUpdate
		
		this.reordered = false;
		this.center.setLayout(new BorderLayout());

		this.jpNorth = new JPanel(new BorderLayout());
		this.center.add(this.jpNorth, BorderLayout.NORTH);

		// Model selection JPanel
		this.jpNorthLeft = new JPanel(new FlowLayout());
		List<LogicalModel> modelList = new ArrayList<LogicalModel>(this.epithelium.getEpitheliumGrid().getModelSet());
	
		JComboBox<String> jcbSBML = this.newModelCombobox(modelList);
		this.jpNorthLeft.add(jcbSBML);
		this.jpNorthLeft.setBorder(BorderFactory.createTitledBorder(Txt.get("s_MODEL_SELECT")));
		this.jpNorth.add(this.jpNorthLeft, BorderLayout.WEST);

		// ---------------------------------------------------------------------------
	
		this.tpc = new TabProbablyChanged();
		this.model2Table = new HashMap<LogicalModel, PhenoTable>();
		this.userPhenotypes = new EpitheliumPhenotypes();
		for (LogicalModel m : modelList) {
			if (this.epithelium.getPhenotypes().getModelSet().contains(m)) {
				for (Phenotype pheno : this.epithelium.getPhenotypes(m))
					this.userPhenotypes.addPhenotype(m, pheno.clone());
			} else {
				// Adds a new one
				this.userPhenotypes.addModel(m);
			}
		}

		this.isInitialized = true;
				
		this.buttonsPanel = new JPanel(new FlowLayout());
//		this.jbTrackAll = new JButton("Track All");
//		this.jbTrackAll.addActionListener(new ActionListener() {
//			@Override
//			public void actionPerformed(ActionEvent e) {
//				model2Table.get(selModel).toggleSelect(true);
//			}
//		});
//		this.buttonsPanel.add(jbTrackAll);
//		this.jbUntrackAll = new JButton("Untrack All");
//		this.jbUntrackAll.addActionListener(new ActionListener() {
//			@Override
//			public void actionPerformed(ActionEvent e) {
//				model2Table.get(selModel).toggleSelect(false);
//			}
//		});
//		this.buttonsPanel.add(jbUntrackAll);
		this.jbUp = new JButton("↑");
		this.jbUp.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				model2Table.get(selModel).moveRow(-1);
			}
		});
		this.buttonsPanel.add(this.jbUp);
		this.jbDown = new JButton("↓");
		this.jbDown.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				model2Table.get(selModel).moveRow(1);
			}
		});
		this.buttonsPanel.add(this.jbDown);
		this.jbClone = new JButton("Clone");
		this.jbClone.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				model2Table.get(selModel).clonePheno();
			}
		});
		this.buttonsPanel.add(this.jbClone);
		this.jbRemove = new JButton("Remove");
		this.jbRemove.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				model2Table.get(selModel).remove();
			}
		});
		this.buttonsPanel.add(this.jbRemove);
		this.jbCreate = new JButton("Create new");
		this.jbCreate.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				model2Table.get(selModel).addEmptyRow();
			}
		});
		this.buttonsPanel.add(this.jbCreate);
		
		this.tableControl = new JPanel(new BorderLayout());
		this.tableControl.add(this.buttonsPanel, BorderLayout.NORTH);
		
		this.tablePane = new JScrollPane();
		this.tablePane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		this.tablePane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
//		this.tablePane.getViewport().setBackground(Color.BLUE);
//		this.tableControl.setBackground(Color.red);

		this.tableControl.add(this.tablePane, BorderLayout.CENTER);
		this.tableControl.add(new JPanel(), BorderLayout.EAST);
		this.tableControl.add(new JPanel(), BorderLayout.WEST);
		
		this.center.add(this.tableControl, BorderLayout.CENTER);
		
		this.updatePhenoTable();
		this.isInitialized = true;
	}
	
	// code copied from EpiTabCellularModelUpdate
	private JComboBox<String> newModelCombobox(List<LogicalModel> modelList) {
		// Model selection list
		String[] saSBML = new String[modelList.size()];
		for (int i = 0; i < modelList.size() ; i++) {
			saSBML[i] = Project.getInstance().getProjectFeatures().getModelName(modelList.get(i));
		}
		JComboBox<String> jcb = new JComboWideBox<String>(saSBML);
		jcb.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				@SuppressWarnings("unchecked")
				JComboBox<String> jcb = (JComboBox<String>) e.getSource();
				selModel = Project.getInstance().getProjectFeatures().getModel((String) jcb.getSelectedItem());
				updatePhenoTable();
			}
		});
		this.selModel = Project.getInstance().getProjectFeatures().getModel((String) jcb.getItemAt(0));
		return jcb;
		
	}
	
	private void updatePhenoTable() {
		// update the GUI if a new model is selected
		
		BorderLayout centerLayout = (BorderLayout) this.center.getLayout();
		JPanel tmpTableControl = (JPanel) centerLayout.getLayoutComponent(BorderLayout.CENTER);
		BorderLayout tControlLayout = (BorderLayout) tmpTableControl.getLayout();
		JScrollPane tmpTablePane = (JScrollPane) tControlLayout.getLayoutComponent(BorderLayout.CENTER);
		if (tmpTablePane != null) {
			this.center.remove(tmpTableControl);
			this.tableControl.remove(tmpTablePane);
		}
		
		this.tablePane = new JScrollPane(this.getTable(this.selModel).getTable());
		this.tableControl.add(this.tablePane, BorderLayout.CENTER);
		this.center.add(this.tableControl, BorderLayout.CENTER);
		
		this.center.repaint();
		this.center.revalidate();
    }
	
	private PhenoTable getTable(LogicalModel m) {
		// get the JTable correspondent to a logical model
		
		if (!this.model2Table.containsKey(m)) {
			PhenoTable phenoTable = new PhenoTable(m);
			this.model2Table.put(m, phenoTable);
			
			if (this.userPhenotypes.getModelSet().contains(m)) {
				Set<Phenotype> phenos = this.userPhenotypes.getPhenotypes(m);
				for (Phenotype pheno : phenos)
					phenoTable.addFullRow(pheno.getName(), pheno.getPheno());
			}
		}
		return this.model2Table.get(m);
	}

	@Override
	public void buttonReset() {
//		this.model2Table.clear();
		this.userPhenotypes = new EpitheliumPhenotypes();
		for (LogicalModel m : this.epithelium.getEpitheliumGrid().getModelSet()) {
			if (this.epithelium.getPhenotypes().getModelSet().contains(m)) {
				for (Phenotype pheno : this.epithelium.getPhenotypes(m))
					this.userPhenotypes.addPhenotype(m, pheno.clone());
			} else {
				// Adds a new one
				this.userPhenotypes.addModel(m);
			}
		}
		this.model2Table.clear();
		this.updatePhenoTable();
	}

	@Override
	protected void buttonAccept() {
		EpitheliumPhenotypes clone = this.userPhenotypes.clone();
		this.reordered = false;
		this.epithelium.setPhenotypes(clone);
		this.tablePane.revalidate();
		this.center.repaint();
	}

	@Override
	protected boolean isChanged() {
		// se if the phenotypes order changed
		if (this.reordered) {
			for (LogicalModel m : model2Table.keySet()) 
				this.model2Table.get(m).reorder();
			
			return true;
		}
		EpitheliumPhenotypes epiPhenos = this.epithelium.getPhenotypes();
		if (!this.userPhenotypes.equals(epiPhenos))
			return true;
			
		return false;
	}


	@Override
	public void applyChange() {
		List<LogicalModel> modelList = new ArrayList<LogicalModel>(this.epithelium.getEpitheliumGrid().getModelSet());
		EpitheliumPhenotypes newPHs = new EpitheliumPhenotypes();
		for (LogicalModel m : modelList) {
			if (this.userPhenotypes.getModelSet().contains(m)) {
				// Already exists
				newPHs.addPhenoSet(m, this.userPhenotypes.getPhenotypes(m));
			} else {
				// Adds a new one
				newPHs.addModel(m);
			}
		}
		this.userPhenotypes = newPHs;
		this.jpNorthLeft.removeAll();
		this.jpNorthLeft.add(this.newModelCombobox(modelList));
		this.updatePhenoTable();
	}
	
	private class PhenoTable implements PropertyChangeListener, Runnable {
		
		private LogicalModel model;
		private String[] colnames;
		private JTable jtable;
		
		// track which cell is being edited, save pre-editing cell values
		private int editingRow;
		private int editingCol;
		private Object editingOldValue;
		
		private final int minCellSizePheno = 30;
		private final int minCellSizeName = 100;

		
		PhenoTable(LogicalModel model) {
			this.model = model;
			
			// Set the columns names
			this.colnames = new String[this.model.getComponents().size() + 1];
			this.colnames[0] = "Name";

			// Get the components names for the new columns
			int i = 1;
			for (NodeInfo node : this.model.getComponents()) {
				this.colnames[i] = node.getNodeID();
				i++;
			}

			// create new Table
			this.jtable = new JTable(new DefaultTableModel(new Object[0][], this.colnames));
			
			
			// See propertyChange method
			this.jtable.addPropertyChangeListener(this);
			this.jtable.setFillsViewportHeight( true );

			
			// Set MAX and MIN values for each component and set editor
			int varC = 1;
			for (NodeInfo var : this.model.getComponents()) {
				TableColumn varCol = this.jtable.getColumnModel().getColumn(varC);
				varCol.setCellEditor(new NodeEditor(0, var.getMax()));
				varC ++;
			}
			
			TableColumnModel colModel = this.jtable.getColumnModel();
			
			// AUTO RESIZE if the number of columns is not enough to fill table header (w/ min Size)
			// If many columns, use minCellSize
			if (!(this.colnames.length * minCellSizePheno < 
        			this.jtable.getPreferredScrollableViewportSize().getWidth())) {
				
				this.jtable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF); 
				colModel.getColumn(0).setMinWidth(minCellSizeName);
				
			    for (int col = 1; col < this.colnames.length; col++) {
			        if (col < colModel.getColumnCount()) {
			        	colModel.getColumn(col).setMinWidth(minCellSizePheno);
			        }
			    }
			}
			    
		}
		
		private JTable getTable() {
			return this.jtable;
		}
			
		public void remove() {
			
			// remove a phenotype from the table
			int[] rows = this.jtable.getSelectedRows();
			if (rows.length > 0 &&  this.jtable.getRowCount() > 0) {
				ArrayUtils.reverse(rows);
				for (int row : rows) {
					// remove from userPhenotypes
					Vector<?> rowData = ((DefaultTableModel) this.jtable.getModel()).getDataVector().elementAt(row);
		        	String phenotype = "";
		        	
		        	for (int i = 1; i < rowData.size(); i++)
		        		phenotype += String.valueOf(rowData.get(i));
			        
					userPhenotypes.removePhenotype(selModel, (String) rowData.get(0), phenotype);
					
					// remove from model
			        int modelIndex = this.jtable.convertRowIndexToModel(row); 
			        ((DefaultTableModel) this.jtable.getModel()).removeRow(modelIndex);
//					this.jtable.remove(row);
					
				}
				((DefaultTableModel) this.jtable.getModel()).fireTableRowsDeleted(rows[rows.length-1], rows[0]);
				tpc.setChanged();
		    }
		}
		
		public void clonePheno() {
			int row = this.jtable.getSelectedRow();
//				String[] names = new String[this.tableModel.getDataVector().size()];
//				for (int i= 0; i < this.tableModel.getDataVector().size(); i++) 
//					names[i] = (String) this.tableModel.getDataVector().elementAt(i).get(0);
//				
//				Collections.sort(names, new Comparator<String>() {
//					public int compare(String s1, String s2) {
//						String integ1 = s1.rep
//					}
//				});
				
			if (row != -1) {
				Vector<String> newClone = (Vector) ((DefaultTableModel) 
						this.jtable.getModel()).getDataVector().elementAt(row).clone();
				String name = (String) newClone.get(0);
					
				name += "_2";
				newClone.remove(0);
				newClone.add(0,name);
				
				((DefaultTableModel) this.jtable.getModel()).insertRow(row + 1, newClone);
				tpc.setChanged();
			}
		}
			
		public void moveRow(int change) {
		
			int row = this.jtable.getSelectedRow();
			if (row != -1) {
				if ( (change == -1 && row != 0) ||
						(change == 1 && row != this.jtable.getRowCount() -1)) {
					((DefaultTableModel) this.jtable.getModel()).moveRow(row, row, row + change);
					this.jtable.setRowSelectionInterval(row + change, row + change);
					tpc.setChanged();
				}
			}
			reordered = true;
		}
			
		public void addFullRow(String name, String pheno) {
			// function to "load" phenotypes from epithelium
				
			Object[] tempA = new Object[this.colnames.length];
			tempA[0] = name;
//			tempA[1] = use;
//			tempA[2] = color;
				
			char[] phenotype = pheno.toCharArray();
			for (int i = 1, e = 0; e < phenotype.length; i++, e++)
				tempA[i] = (char) phenotype[e];
			
			((DefaultTableModel) this.jtable.getModel()).addRow(tempA);
		}
			
		public void addEmptyRow() {
			Object[] tempA = new Object[this.colnames.length];
			tempA[0] = "Phenotype_1";
			
			// "*" is default node value
			String phenotype = "";
			char temp = '*';
			for (int i = 1; i < this.jtable.getModel().getColumnCount(); i++) {
				tempA[i] = temp;
				phenotype += temp;
			}
				
			((DefaultTableModel) this.jtable.getModel()).addRow(tempA);
			userPhenotypes.addPhenotype(selModel, (String) tempA[0] ,
				        phenotype);
			 
			tpc.setChanged();

		}
		
		public class NodeEditor extends DefaultCellEditor {

			private static final long serialVersionUID = 1L;
			private Integer min;
			private Integer max;

			public NodeEditor(int min, int max) {
	            super(new JTextField());
	            this.max = max;
	            this.min = min;
			}
		
			@Override
			public String getCellEditorValue() {
				 JTextField textField = (JTextField) getComponent();
		         String o = textField.getText();
		         
		         // if String contains numbers, see if the value is valid for the node
		         if (o.matches("-?\\d+")) {
		        	 if ((Integer.parseInt(o)) <= this.min) 
		        		 return this.min.toString();
					 if ((Integer.parseInt(o)) >= this.max) 
					     return this.max.toString();
		         } else {
		        	 return "*";
		         }
		        return "*";
			}
			
		}
		
//		public class ColorEditor extends AbstractCellEditor 
//			implements TableCellEditor,ActionListener {
//
//			Color currentColor;
//			JButton button;
//			JColorChooser colorChooser;
//			JDialog dialog;
//			protected static final String EDIT = "edit";
//
//			public ColorEditor() {
//				setOpaque(true); //MUST do this for background to show up.
//				button = new JButton();
//				button.setActionCommand(EDIT);
//				button.addActionListener(this);
//				button.setBorderPainted(false);
//
//				//Set up the dialog that the button brings up.
//				colorChooser = new JColorChooser();
//				dialog = JColorChooser.createDialog(button,"Pick a Color",
//                        true,  //modal
//                        colorChooser,
//                        this,  //OK button handler
//                        null); //no CANCEL button handler
//				}
//
//			public void actionPerformed(ActionEvent e) {
//				if (EDIT.equals(e.getActionCommand())) {
//					//The user has clicked the cell, so
//					//bring up the dialog.
//					button.setBackground((Color)  currentColor);
//					colorChooser.setColor((Color) currentColor);
//					dialog.setVisible(true);
//
//					fireEditingStopped(); //Make the renderer reappear.
//
//				} else { //User pressed dialog's "OK" button.
//					currentColor = (Color) colorChooser.getColor();
//				}
//			}
//
//			//Implement the one CellEditor method that AbstractCellEditor doesn't.
//			public Object getCellEditorValue() {
//				return currentColor;
//			}
//
//			//Implement the one method defined by TableCellEditor.
//			public Component getTableCellEditorComponent(JTable table,
//                                 Object value,
//                                 boolean isSelected,
//                                 int row,
//                                 int column) {
//				
//				currentColor = (Color) value;
//				return button;
//			}
//		}
//			
//		public class ColorRenderer extends JLabel
//           		implements TableCellRenderer {
//				
//			public ColorRenderer() {
//				setOpaque(true); 
//			}
//
//			public Component getTableCellRendererComponent(
//				JTable table, Object color,
//				boolean isSelected, boolean hasFocus,
//				int row, int column) {
//					
//				Color newColor = (Color) color;
//				setBackground(newColor);
//				return this;
//			}
//		}
		
		public void reorder() {
			// method called when button accept is called. 
			// So the user order is kept
			
			userPhenotypes = new EpitheliumPhenotypes();
	    	for (int r = 0; r < this.jtable.getRowCount(); r++) {
			    Vector<?> rowData = ((DefaultTableModel) this.jtable.getModel()).
			    		getDataVector().elementAt(r);
			    
			    String pheno = "";
			    for (int p = 1; p < this.jtable.getRowCount(); p++) 
					pheno += String.valueOf(rowData.get(p));
				
			    userPhenotypes.addPhenotype(selModel, (String) rowData.get(0), pheno);
			    int i = 0;
			    i ++;
	    	}
		}
		
		
		@Override 
		public void run() {
			
			// save old values, to compare with new
			this.editingRow = this.jtable.convertRowIndexToModel(this.jtable.getEditingRow());
			this.editingCol = this.jtable.convertColumnIndexToModel(this.jtable.getEditingColumn());
			this.editingOldValue = this.jtable.getModel().getValueAt(this.editingRow,this.editingCol);
			
			// select all when double click happens
			JTextComponent textField = ((JTextComponent) this.jtable.getEditorComponent());
			textField.selectAll();
			
		}
		
		@Override
		public void propertyChange(PropertyChangeEvent e) {
//		//  A cell has started/stopped editing

			if ("tableCellEditor".equals(e.getPropertyName())) {
				
				// select all when muse event happens
				JTextComponent textField = ((JTextComponent) this.jtable.getEditorComponent());
				textField.selectAll();
			
				if (this.jtable.isEditing()) {
					SwingUtilities.invokeLater(this);
				} else {
					
					Object newValue =  this.jtable.getModel().getValueAt(this.editingRow, this.editingCol);
					// if the cell value changed
					if (!Objects.equals(newValue, this.editingOldValue)) {
						tpc.setChanged();

					    Vector<?> rowData = ((DefaultTableModel) this.jtable.getModel()).getDataVector().elementAt(this.editingRow);
					    
					    // see if phenotype is defined
					    boolean valid = true;
					    for (Object par : rowData) {
					    	if (par  == null) {
					    		valid = false;
					    		break;
					    	}
					    }
					    
					    // if the phenotype is defined. Save to UserPhenotypes
					    if (valid) {
				    		String newName = (String) rowData.get(0);
				    		String oldName = newName;
				    		String newPhenotype = "";
				    		for (int i = 1; i < rowData.size(); i++) 
								newPhenotype += String.valueOf(rowData.get(i));
				    		
				    		String oldPhenotype = newPhenotype;
				    		
					    	if (this.editingCol == 0) {
					    		oldName = (String) this.editingOldValue;
					    	} else {
						    	oldPhenotype = "";
								for (int i = 1; i < rowData.size(); i++) {
									if (i == this.editingCol)
										oldPhenotype += String.valueOf(this.editingOldValue);
									else
										oldPhenotype += String.valueOf(rowData.get(i));
								}
					    	}
					    	
							userPhenotypes.removePhenotype(selModel, oldName,
									oldPhenotype);
							    
							userPhenotypes.addPhenotype(selModel, (String) rowData.get(0), newPhenotype);
					    }
					}
				}
			}
		}
	}
}
