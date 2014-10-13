package org.ginsim.epilog.core;

import java.awt.Color;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.colomoto.logicalmodel.LogicalModel;
import org.colomoto.logicalmodel.NodeInfo;
import org.colomoto.logicalmodel.perturbation.AbstractPerturbation;
import org.ginsim.epilog.common.Tuple2D;
import org.ginsim.epilog.core.topology.RollOver;

public class Epithelium {
	private String name;
	private EpitheliumGrid grid;
	private EpitheliumComponentFeatures componentFeatures;
	private EpitheliumPriorityClasses priorities;
	private EpitheliumIntegrationFunctions integrationFunctions;
	private EpitheliumPerturbations perturbations;
	private boolean isChanged;

	public Epithelium(int x, int y, String topologyLayout, RollOver rollover,
			LogicalModel m, String name) throws InstantiationException,
			IllegalAccessException, IllegalArgumentException,
			InvocationTargetException, NoSuchMethodException,
			SecurityException, ClassNotFoundException {
		this.name = name;
		this.grid = new EpitheliumGrid(x, y, topologyLayout, rollover, m);
		this.priorities = new EpitheliumPriorityClasses();
		this.priorities.addModel(m);
		this.integrationFunctions = new EpitheliumIntegrationFunctions();
		this.perturbations = new EpitheliumPerturbations();
		this.perturbations.addModel(m);
		this.componentFeatures = new EpitheliumComponentFeatures();
		this.componentFeatures.addModel(m);
		this.isChanged = false;
	}

	private Epithelium(String name, EpitheliumGrid grid,
			EpitheliumIntegrationFunctions eif, EpitheliumPriorityClasses epc,
			EpitheliumPerturbations eap, EpitheliumComponentFeatures ecf) {
		this.name = name;
		this.grid = grid;
		this.priorities = epc;
		this.integrationFunctions = eif;
		this.componentFeatures = ecf;
		this.perturbations = eap;
		this.isChanged = false;
	}

	public boolean hasModel(LogicalModel m) {
		return this.grid.hasModel(m);
	}

	public Epithelium clone() {
		return new Epithelium("CopyOf_" + this.name, this.grid.clone(),
				this.integrationFunctions.clone(), this.priorities.clone(),
				this.perturbations.clone(), this.componentFeatures.clone());
	}
	
	public void update() {
		this.grid.updateModelSet();
		Set<LogicalModel> modelSet = this.grid.getModelSet();
		
		// Add to Epithelium state new models from modelSet
		for (LogicalModel mSet : modelSet) {
			// Priority classes
			if (this.priorities.getModelPriorityClasses(mSet) == null) {
				this.priorities.addModel(mSet);
			}
			// Component features
			this.componentFeatures.addModel(mSet);
			// Perturbations
			if (!this.perturbations.hasModel(mSet))
				this.perturbations.addModel(mSet);
		}
		
		// Remove from Epithelium state absent models from modelSet
		for (LogicalModel mPriorities : new ArrayList<LogicalModel>(this.priorities.getModelSet())) {
			if (!modelSet.contains(mPriorities)) {
				this.priorities.removeModel(mPriorities);
			}
		}
		for (LogicalModel mPerturbation : new ArrayList<LogicalModel>(this.perturbations.getModelSet())) {
			if (!modelSet.contains(mPerturbation)) {
				this.perturbations.removeModel(mPerturbation);
			}
		}
		
		// Create list with all existing Components
		Set<String> sNodeIDs = new HashSet<String>();
		for (LogicalModel m : modelSet) {
			for (NodeInfo node : m.getNodeOrder()) {
				sNodeIDs.add(node.getNodeID());
			}
		}
		// Clean Epithelium components
		for (String oldNodeID : new ArrayList<String>(this.componentFeatures.getComponents())) {
			if (!sNodeIDs.contains(oldNodeID)) {
				this.componentFeatures.removeComponent(oldNodeID);
				if (this.isIntegrationComponent(oldNodeID)) {
					this.integrationFunctions.removeComponent(oldNodeID);
				}
			}
		}
	}

	public EpitheliumComponentFeatures getComponentFeatures() {
		return this.componentFeatures;
	}

	public String toString() {
		return this.name + " ("
				+ this.grid.getTopology().getRollOver().toString() + ")";
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
		this.isChanged = true;
	}

	public LogicalModel getModel(int x, int y) {
		return this.grid.getModel(x, y);
	}

	public void setGridWithModel(LogicalModel m, List<Tuple2D> lTuples) {
		for (Tuple2D tuple : lTuples) {
			this.grid.setModel(tuple.getX(), tuple.getY(), m);
		}
		this.isChanged = true;
	}

	public void setGridWithComponentValue(String nodeID, byte value,
			List<Tuple2D> lTuples) {
		for (Tuple2D tuple : lTuples) {
			this.grid.setCellComponentValue(tuple.getX(), tuple.getY(), nodeID,
					value);
		}
		this.isChanged = true;
	}

	public void setComponentColor(String nodeID, Color color) {
		this.componentFeatures.setNodeColor(nodeID, color);
		this.isChanged = true;
	}

	public void setIntegrationFunction(String nodeID, byte value,
			String function) {
		NodeInfo node = this.componentFeatures.getNodeInfo(nodeID);
		if (!this.integrationFunctions.containsKey(nodeID)) {
			this.integrationFunctions.addComponent(node);
		}
		this.integrationFunctions.setFunctionAtLevel(node, value, function);
		this.isChanged = true;
	}

	public void initPriorityClasses(LogicalModel m) {
		ModelPriorityClasses mpc = new ModelPriorityClasses(m);
		this.priorities.addModelPriorityClasses(mpc);
		// TODO: this.isChanged = true;
	}

	public void initComponentFeatures(LogicalModel m) {
		this.componentFeatures.addModel(m);
		// TODO: this.isChanged = true;
	}

	public void setPriorityClasses(LogicalModel m, String pcs) {
		ModelPriorityClasses mpc = new ModelPriorityClasses(m);
		mpc.setPriorities(pcs);
		this.priorities.addModelPriorityClasses(mpc);
		this.isChanged = true;
	}
	
	public void setPriorityClasses(ModelPriorityClasses mpc) {
		this.priorities.addModelPriorityClasses(mpc);
	}

	public void addPerturbation(LogicalModel m, AbstractPerturbation ap) {
		this.perturbations.addPerturbation(m, ap);
		this.isChanged = true;
	}

	public void delPerturbation(LogicalModel m, AbstractPerturbation ap) {
		this.perturbations.delPerturbation(m, ap);
		this.isChanged = true;
	}

	public void applyPerturbation(LogicalModel m, AbstractPerturbation ap,
			Color c, List<Tuple2D> lTuples) {
		this.perturbations.addPerturbationColor(m, ap, c);
		if (lTuples != null) {
			this.grid.setPerturbation(m, lTuples, ap);
		}
		this.isChanged = true;
	}

	public EpitheliumGrid getEpitheliumGrid() {
		return this.grid;
	}

	public ModelPriorityClasses getPriorityClasses(LogicalModel m) {
		return this.priorities.getModelPriorityClasses(m);
	}

	public ComponentIntegrationFunctions getIntegrationFunctionsForComponent(
			String nodeID) {
		return this.integrationFunctions.getComponentIntegrationFunctions(nodeID);
	}

	public Set<String> getIntegrationFunctionsComponents() {
		return this.integrationFunctions.getComponents();
	}

	public boolean isIntegrationComponent(String nodeID) {
		return this.integrationFunctions.containsKey(nodeID);
	}

	public EpitheliumIntegrationFunctions getIntegrationFunctions() {
		return this.integrationFunctions;
	}

	public ModelPerturbations getModelPerturbations(LogicalModel m) {
		return this.perturbations.getModelPerturbations(m);
	}

	public EpitheliumPerturbations getEpitheliumPerturbations() {
		return this.perturbations;
	}

	public void setModel(int x, int y, LogicalModel m) {
		this.grid.setModel(x, y, m);
		this.isChanged = true;
	}

}
