package org.epilogtool.core;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.colomoto.biolqm.LogicalModel;
import org.colomoto.biolqm.tool.simulation.grouping.ModelGrouping;

public class EpitheliumUpdateSchemeIntra {
	private Map<LogicalModel, ModelGrouping> priorityClassSet;
	private Map<LogicalModel, Rates> ratesSet;
	// true for PC, false for Rates
	private Map<LogicalModel, Boolean> activeSet;
 
	
	public EpitheliumUpdateSchemeIntra() {
		this.priorityClassSet = new HashMap<LogicalModel, ModelGrouping>();
		this.ratesSet = new HashMap<LogicalModel,Rates>();
		this.activeSet = new HashMap<LogicalModel, Boolean>();
	}

	public void addModelPriorities(LogicalModel m) {
		this.priorityClassSet.put(m, new ModelGrouping(m));
	} 
	
	public void addModelRates(LogicalModel m) {
		this.ratesSet.put(m, new Rates(m));
	}


	public void removeModel(LogicalModel m) {
		if (this.activeSet.containsKey(m)) {
			this.activeSet.remove(m);
			if (this.priorityClassSet.containsKey(m))
				this.priorityClassSet.remove(m);
			if (this.ratesSet.containsKey(m))
				this.ratesSet.remove(m);
		}
	}
	
	public void setActive(LogicalModel m, Boolean scheme) {
		this.activeSet.put(m, scheme);
	}
	
	/*
	 * public void setActiveSet(Map<LogicalModel, Boolean> activeSet) {
	 * this.activeSet = activeSet; }
	 */

	public ModelGrouping getModelPriorityClasses(LogicalModel m) {
		if (this.priorityClassSet.get(m) == null) {
			return null;
		}
		return this.priorityClassSet.get(m);
	}
	
	public Rates getModelRates(LogicalModel m) {
		if (this.ratesSet.get(m) == null) {
			return null; 
		}
		return this.ratesSet.get(m);
	}
	
	public Boolean getModelActive(LogicalModel m) {
		return this.activeSet.get(m);
	}

	public void addModelPriorityClasses(ModelGrouping mpc) {
		LogicalModel m = mpc.getModel();
		this.priorityClassSet.put(m, mpc);
	}
	
	public void addModelRates(Rates rates) {
		LogicalModel m = rates.getModel();
		this.ratesSet.put(m, rates);
	} 

	public Set<LogicalModel> getModelSet() {
		return this.priorityClassSet.keySet();
	}

	public EpitheliumUpdateSchemeIntra clone() {
		EpitheliumUpdateSchemeIntra newUSs = new EpitheliumUpdateSchemeIntra();
		for (LogicalModel m : this.priorityClassSet.keySet()) {
			ModelGrouping oldMpc = this.getModelPriorityClasses(m).clone();
		 	Rates oldRates = this.getModelRates(m);
			Boolean oldActive = this.getModelActive(m);


			if (oldRates != null) {
				oldRates = oldRates.clone();
				newUSs.addModelRates(oldRates);
			}
			if (oldActive != null)
				newUSs.setActive(m, oldActive);

			// mpc active
			newUSs.addModelPriorityClasses(oldMpc);
			
		}
		return newUSs;
	}

	public boolean equals(Object o) {
		EpitheliumUpdateSchemeIntra epcOut = (EpitheliumUpdateSchemeIntra) o;
		Set<LogicalModel> sAllPCModels = new HashSet<LogicalModel>();
		Set<LogicalModel> sAllRatesModels = new HashSet<LogicalModel>();

		sAllPCModels.addAll(this.priorityClassSet.keySet());
		sAllPCModels.addAll(epcOut.priorityClassSet.keySet());
		
		sAllRatesModels.addAll(this.ratesSet.keySet());
		sAllRatesModels.addAll(epcOut.ratesSet.keySet());

		for (LogicalModel m : sAllPCModels) {
			if (!this.priorityClassSet.containsKey(m) || !epcOut.priorityClassSet.containsKey(m))
				return false;
			if (!this.priorityClassSet.get(m).equals(epcOut.priorityClassSet.get(m)))
				return false;
		}
		for (LogicalModel m : sAllRatesModels) {
			if (!this.ratesSet.containsKey(m) || !epcOut.ratesSet.containsKey(m))
				return false;
			if (!this.ratesSet.get(m).equals(epcOut.ratesSet.get(m)))
				return false;
		}
		if (!this.activeSet.equals(epcOut.activeSet)) {
			return false;
		}
		return true;
	}
}