package org.epilogtool.gui.tab;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.EventObject;
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
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
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
	
	private int minCellSize;

	private EpitheliumPhenotypes userPhenotypes;
	
	public EpiTabPhenotypeDefinitions(Epithelium e, TreePath path, TabChangeNotifyProj tabChanged) {
		super(e, path, tabChanged);
	}
	
	@Override
	public void initialize() {
		// ---------------------------------------------------------------------------
		// Model selection jcomboCheckBox * Code copied from EpiTabCellularUpdate
		
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
		
		this.tablePane.revalidate();
		this.tablePane.repaint();

    }
	
	private PhenoTable getTable(LogicalModel m) {
		
		if (!this.model2Table.containsKey(m)) {
			PhenoTable phenoTable = new PhenoTable(m);
			this.model2Table.put(m, phenoTable);
			
			if (this.userPhenotypes.getModelSet().contains(m)) {
				Set<Phenotype> phenos = this.userPhenotypes.getPhenotypes(m);
				for (Phenotype pheno : phenos)
					phenoTable.addFullRow(pheno.getName(), pheno.getPheno());
//				if (phenos.size() == 0)
//					phenoTable.addEmptyRow();
			} else {
//				phenoTable.addEmptyRow();
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
//		this.getTable(selModel).tableModel.fireTableDataChanged();
	}

	@Override
	protected void buttonAccept() {
		EpitheliumPhenotypes clone = this.userPhenotypes.clone();
		this.epithelium.setPhenotypes(clone);
		this.tablePane.revalidate();
		this.center.repaint();
	}

	@Override
	protected boolean isChanged() {
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
	
	private class PhenoTable implements TableModelListener, PropertyChangeListener, Runnable {
		
		private LogicalModel model;
		private String[] colnames;
		private JTable jtable;
		private ModelTable tableModel;		
		
		private int editingRow;
		private int editingCol;
		private Object editingOldValue;
		
		
		PhenoTable(LogicalModel model) {
			this.model = model;
			
			// Set the columns names
			this.colnames = new String[this.model.getComponents().size() + 1];
			this.colnames[0] = "Name";
//			this.colnames[1] = "Track";
//			this.colnames[2] = "Color";

			// Get the components names for the new columns
			int i = 1;
			for (NodeInfo node : this.model.getComponents()) {
				this.colnames[i] = node.getNodeID();
				i++;
			}

			// create new Table
			this.tableModel = new ModelTable(this.colnames);
			this.jtable = new JTable(this.tableModel);	

			this.jtable.addPropertyChangeListener(this);
			
//			TableColumn colorCol = this.jtable.getColumnModel().getColumn(2);
//			colorCol.setCellEditor(new ColorEditor());
//			colorCol.setCellRenderer(new ColorRenderer());
			
			// Set MAX and MIN values for each component and set editor
			this.setNodeValues();
		}
		
		private JTable getTable() {
			return this.jtable;
		}
			
//		public void toggleSelect(boolean select) {
//			for (int i = 0; i < this.jtable.getRowCount(); i++) 
//				this.jtable.setValueAt(select, i, 1);
//		}
			
		public void remove() {
			int[] rows = this.jtable.getSelectedRows();
			if (rows.length > 0 &&  this.jtable.getRowCount() > 0) {
				ArrayUtils.reverse(rows);
				for (int row : rows) {
					// remove from userPhenotypes
					Vector rowData = this.tableModel.getDataVector().elementAt(row);
		        	String phenotype = "";
		        	
		        	for (int i = 1; i < rowData.size(); i++)
		        		phenotype += String.valueOf(rowData.get(i));
			        
					userPhenotypes.removePhenotype(selModel, (String) rowData.get(0), phenotype);
					
					// remove from model
			        int modelIndex = this.jtable.convertRowIndexToModel(row); 
					this.tableModel.removeRow(modelIndex);
//					this.jtable.remove(row);
					
				}
				this.tableModel.fireTableRowsDeleted(rows[rows.length-1], rows[0]);
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
				Vector newClone = (Vector) this.tableModel.getDataVector().elementAt(row).clone();
				String name = (String) newClone.get(0);
					
				int next = 1;
				name += "_2";
				newClone.remove(0);
				newClone.add(0,name);
				
				this.tableModel.insertRow(row + 1, newClone);
				tpc.setChanged();
			}
		}
			
		public void moveRow(int change) {
			int row = this.jtable.getSelectedRow();
			if (row != -1) {
				if ( (change == -1 && row != 0) ||
						(change == 1 && row != this.jtable.getRowCount() -1)) {
					this.tableModel.moveRow(row, row, row + change);
					tpc.setChanged();
				}
			}
		}
			
		public void addFullRow(String name, String pheno) {
				
			Object[] tempA = new Object[this.colnames.length];
			tempA[0] = name;
//			tempA[1] = use;
//			tempA[2] = color;
				
			char[] phenotype = pheno.toCharArray();
			for (int i = 1, e = 0; e < phenotype.length; i++, e++)
				tempA[i] = (char) phenotype[e];
			
			this.tableModel.addRow(tempA);
		}
			
		public void addEmptyRow() {
			Object[] tempA = new Object[this.colnames.length];
			tempA[0] = "Phenotype_1";
//			tempA[1] = false;
//			tempA[2] = Color.black;
			
			String phenotype = "";
			char temp = '*';
			for (int i = 1; i < this.tableModel.getColumnCount(); i++) {
				tempA[i] = temp;
				phenotype += temp;
			}
				
			this.tableModel.addRow(tempA);
			userPhenotypes.addPhenotype(selModel, (String) tempA[0] ,
				        phenotype);
			 
			tpc.setChanged();

		}
		
		public void setNodeValues() {
			int varC = 1;
			for (NodeInfo var : this.model.getComponents()) {
				final JComboBox<String> jcombob = new JComboBox<String>();
				jcombob.addItem("*");
				for (byte vl = 0; vl <= var.getMax(); vl++)
					jcombob.addItem("" + vl);
				TableColumn varCol = this.jtable.getColumnModel().getColumn(varC);
				varCol.setCellEditor(new DefaultCellEditor(jcombob));
//				varCol.setCellEditor(new JComboBoxEditor(jcombob));

				varC ++;
			}
		}
		
		@Override
		public void tableChanged(TableModelEvent e) {
//			int row = e.getFirstRow();
//	        int column = e.getColumn();
//	        
//	        
//	        if (this.tableModel.getRowCount() != 0 && row != -1 && column != -1) {
//	        	Vector rowData = this.tableModel.getDataVector().elementAt(row);
//	        	for (Object par : rowData) {
//	        		if (par  == null) {
//	        			break;
//	        		}
//	        	}
//	        	String phenotype = "";
//	        	for (int i = 3; i < rowData.size(); i++)
//	        		phenotype += String.valueOf(rowData.get(i));
//	        	
//		        userPhenotypes.addPhenotype(selModel, (String) rowData.get(0) ,
//		        		(Boolean) rowData.get(1), (Color) rowData.get(2), phenotype);
//		
//	        }
		}
	
		private class ModelTable extends DefaultTableModel{
			public ModelTable(String[] colnames) {
				super(colnames, 0);
			}

			@Override
			public Class<?> getColumnClass(int columnIndex) {
				Class clazz = String.class;
				switch (columnIndex) {
				case 0:
					clazz = String.class;
					break;
//				case 1:
//					clazz = Boolean.class;
//	        		break;
//				case 2:
//					clazz = Color.class;
//	        		break;
				default:
					// ??
					clazz = JComboBox.class;
					break;
				}
				return clazz;
			}
		}
		
		public PhenoTable getInstance() {
			return this;
		}
		
		public class JComboBoxEditor extends DefaultCellEditor {

			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			public JComboBoxEditor(JComboBox<String> checkBox) {
				super(checkBox);
			}
			
			@Override
			public boolean isCellEditable(EventObject anEvent) {
			     return true;
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
		@Override 
		public void run() {
			this.editingRow = this.jtable.convertRowIndexToModel(this.jtable.getEditingRow());
			this.editingCol = this.jtable.convertColumnIndexToModel(this.jtable.getEditingColumn());
			this.editingOldValue = this.jtable.getModel().getValueAt(this.editingRow,this.editingCol);
			
			Component comp = this.getTable().getEditorComponent();
//			  if (comp instanceof JComboBox) {
//		            System.out.println("kkde");
//		            ((JComboBox<String>) comp).showPopup();
//			  }
			  
			comp.requestFocusInWindow();
		}
		
//
		@Override
		public void propertyChange(PropertyChangeEvent e) {
//		//  A cell has started/stopped editing
			
			if ("tableCellEditor".equals(e.getPropertyName())) {
			
				if (this.jtable.isEditing()) {
					SwingUtilities.invokeLater(this);
				} else {
					Object newValue =  this.tableModel.getValueAt(this.editingRow, this.editingCol);
					if (!Objects.equals(newValue, this.editingOldValue)) {
						tpc.setChanged();

					    Vector rowData = this.tableModel.getDataVector().elementAt(this.editingRow);
					    
					    boolean valid = true;
					    for (Object par : rowData) {
					    	if (par  == null) {
					    		valid = false;
					    		break;
					    	}
					    }
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
