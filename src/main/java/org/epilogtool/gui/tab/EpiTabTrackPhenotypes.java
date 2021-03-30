package org.epilogtool.gui.tab;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.DateFormat.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.EventObject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractCellEditor;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToggleButton;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import org.colomoto.biolqm.LogicalModel;
import org.colomoto.biolqm.NodeInfo;
import org.colomoto.biolqm.widgets.PriorityClassPanel;
import org.epilogtool.common.Txt;
import org.epilogtool.core.Epithelium;
import org.epilogtool.core.EpitheliumPhenotypes;
import org.epilogtool.core.EpitheliumPhenotypes.Phenotype;
import org.epilogtool.core.EpitheliumUpdateSchemeIntra;
import org.epilogtool.gui.EpiGUI.TabChangeNotifyProj;
import org.epilogtool.gui.tab.EpiTabDefinitions.TabProbablyChanged;
import org.epilogtool.gui.widgets.JComboCheckBox;
import org.epilogtool.gui.widgets.JComboWideBox;
import org.epilogtool.io.ButtonFactory;
import org.epilogtool.project.Project;

public class EpiTabTrackPhenotypes extends EpiTabDefinitions {

	private phenoTable phenoTable;
	private Map<LogicalModel, DefaultTableModel> model2Table;
	private LogicalModel selModel;
	
	private TabProbablyChanged tpc;

	private JButton jbSelectAll;
	private JButton jbDeselectAll;
	private JButton jbUp;
	private JButton jbClone;
	private JButton jbRemove;
	

	private JPanel jpNorth;
	private JPanel jpNorthLeft;
	private JPanel buttonsPanel;

	private JPanel tableControl;
	private JComboCheckBox jccbSBML;
	
	private int minCellSize;

	private EpitheliumPhenotypes userPhenotypes;
	
	public EpiTabTrackPhenotypes(Epithelium e, TreePath path, TabChangeNotifyProj tabChanged) {
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
		this.model2Table = new HashMap<LogicalModel, DefaultTableModel>();
		this.userPhenotypes = this.epithelium.getPhenosToTrack().clone();

		this.isInitialized = true;
		
		this.phenoTable = new phenoTable(modelList.get(0)) ;
		
		this.buttonsPanel = new JPanel(new FlowLayout());
		JButton jbSelectAll = new JButton("Select All");
		jbSelectAll.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				phenoTable.toggleSelect(true);
			}
		});
		this.buttonsPanel.add(jbSelectAll);
		JButton jbDeselectAll = new JButton("Deselect All");
		jbDeselectAll.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				phenoTable.toggleSelect(false);
			}
		});
		this.buttonsPanel.add(jbDeselectAll);
		JButton jbUp = new JButton("↑");
		this.buttonsPanel.add(jbUp);
		JButton jbDown = new JButton("↓");
		this.buttonsPanel.add(jbDown);
		JButton jbClone = new JButton("Clone");
		this.buttonsPanel.add(jbClone);
		JButton jbRemove = new JButton("Remove");
		this.buttonsPanel.add(jbRemove);
		
		
		this.tableControl = new JPanel(new BorderLayout());
		this.tableControl.add(this.buttonsPanel, BorderLayout.NORTH);
		this.tableControl.add(new JScrollPane(this.phenoTable.getTable()), BorderLayout.CENTER);
		this.tableControl.add(new JPanel(), BorderLayout.EAST);
		this.tableControl.add(new JPanel(), BorderLayout.WEST);
		
		this.center.add(this.tableControl, BorderLayout.CENTER);
		
//		updateComponentList(this.jccbSBML.getSelectedItems());
		this.isInitialized = true;
	}
	
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
//				updatePriorityPanel();
			}
		});
		this.selModel = Project.getInstance().getProjectFeatures().getModel((String) jcb.getItemAt(0));
		return jcb;
		
	}
	

	@Override
	public void buttonReset() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void buttonAccept() {
		// TODO Auto-generated method stub

	}

	@Override
	protected boolean isChanged() {
		// TODO Auto-generated method stub
		return false;
	}


	@Override
	public void applyChange() {
		// TODO Auto-generated method stub

	}
	
	private class phenoTable {
		
		private LogicalModel model;
		private String[] colnames;
		private JTable modelTable;
		private ModelTable modelT;
		private ArrayList<ArrayList<Object>> data;

		
		phenoTable(LogicalModel model) {
			this.model = model;
			this.colnames = new String[this.model.getComponents().size() + 3];
			this.colnames[0] = "Name";
			this.colnames[1] = "Track";
			this.colnames[2] = "Color";
			int i = 3;
			for (NodeInfo node : this.model.getComponents()) {
				this.colnames[i] = node.getNodeID();
				i ++;
			}
			
			this.data = new ArrayList<ArrayList<Object>>();
			
			Object[] tempA = new Object[this.colnames.length];
			for (int ia = 0; ia < this.colnames.length; ia++) {
				if (ia == 1) {
			        tempA[ia] = false;
				} else if (ia == 2) {
					tempA[ia] = Color.black;
				} else {
					tempA[ia] = "";
				}
			}
			
			this.modelT = new ModelTable(this.colnames);
			modelT.addRow(tempA);
			this.modelTable = new JTable(modelT);
			this.modelTable.addMouseListener(new MouseAdapter()
			{
			    public void mousePressed(MouseEvent e)
			    {
			        JTable source = (JTable)e.getSource();
			        int row = source.rowAtPoint( e.getPoint() );

			        if (row == modelTable.getRowCount() - 1) {
			        	Object[] tempA = new Object[colnames.length];
						for (int ia = 0; ia < colnames.length; ia++) {
							if (ia == 1) {
						        tempA[ia] = false;
							} else if (ia == 2) {
								tempA[ia] = Color.black;
							} else {
								tempA[ia] = "";
							}
						}
			        	modelT.addRow(tempA);
			        }
			        	
			   }});
			
			TableColumn colorCol = this.modelTable.getColumnModel().getColumn(2);
			colorCol.setCellEditor(new ColorEditor());
			colorCol.setCellRenderer(new ColorRenderer());
			
			int varC = 3;
			for (NodeInfo var : this.model.getComponents()) {
				JComboBox<String> jcombob = new JComboBox<String>();
				jcombob.addItem("*");
				for (byte vl = 0; vl <= var.getMax(); vl++)
					jcombob.addItem("" + vl);
				TableColumn varCol = this.modelTable.getColumnModel().getColumn(varC);
				varCol.setCellEditor(new DefaultCellEditor(jcombob));
				varC ++;
			}
			
			}
		
			private JTable getTable() {
				return this.modelTable;
			}
			
			public void toggleSelect(boolean select) {
				for (int i = 0; i < this.modelTable.getRowCount(); i++) 
					this.modelTable.setValueAt(select, i, 1);
			}
			
	
		
//			private Object[][] convertData() {
//				Object[][] dataArray = new Object
//						[this.data.size()][this.colnames.length];
//			
//				int rowN = 0;
//				for (List<Object> row : this.data) {
//					int colN = 0;
//					for (Object value : row) {
//						dataArray[rowN][colN] = value;
//						colN ++;
//					}
//					rowN ++;
//				}
//				return dataArray;
//			}
		
			public void addNewRow() {
				
			}
		
			public JComboBox getNodeValues() {
				return null;
		
			}
	
			private class ModelTable extends DefaultTableModel {
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
					case 1:
						clazz = Boolean.class;
	        			break;
					case 2:
						clazz = Color.class;
	        			break;
					default:
						clazz = JComboBox.class;
						break;
					}
					return clazz;
				}
				
			}
			public class ColorEditor extends AbstractCellEditor 
				implements TableCellEditor,ActionListener {

				Color currentColor;
				JButton button;
				JColorChooser colorChooser;
				JDialog dialog;
				protected static final String EDIT = "edit";

				public ColorEditor() {
					setOpaque(true); //MUST do this for background to show up.
					button = new JButton();
					button.setActionCommand(EDIT);
					button.addActionListener(this);
					button.setBorderPainted(false);

					//Set up the dialog that the button brings up.
					colorChooser = new JColorChooser();
					dialog = JColorChooser.createDialog(button,"Pick a Color",
                           true,  //modal
                           colorChooser,
                           this,  //OK button handler
                           null); //no CANCEL button handler
					}

				public void actionPerformed(ActionEvent e) {
					if (EDIT.equals(e.getActionCommand())) {
						//The user has clicked the cell, so
						//bring up the dialog.
						button.setBackground((Color)  currentColor);
						colorChooser.setColor((Color) currentColor);
						dialog.setVisible(true);

						fireEditingStopped(); //Make the renderer reappear.

					} else { //User pressed dialog's "OK" button.
						currentColor = (Color) colorChooser.getColor();
					}
				}

				//Implement the one CellEditor method that AbstractCellEditor doesn't.
				public Object getCellEditorValue() {
					return currentColor;
				}

				//Implement the one method defined by TableCellEditor.
				public Component getTableCellEditorComponent(JTable table,
                                    Object value,
                                    boolean isSelected,
                                    int row,
                                    int column) {
					
					currentColor = (Color) value;
					return button;
				}
			}
			
			public class ColorRenderer extends JLabel
            		implements TableCellRenderer {
				
				public ColorRenderer() {
					setOpaque(true); 
					}

				public Component getTableCellRendererComponent(
					JTable table, Object color,
					boolean isSelected, boolean hasFocus,
					int row, int column) {
					
					Color newColor = (Color) color;
					setBackground(newColor);
					return this;
				}
			}
		
		}
}
