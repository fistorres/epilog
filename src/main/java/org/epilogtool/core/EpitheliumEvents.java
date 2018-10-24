package org.epilogtool.core;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.colomoto.biolqm.LogicalModel;
import org.epilogtool.common.Txt;

public class EpitheliumEvents {
	
	public static String DEFAULT_ORDER = Txt.get("s_TAB_EPIUPDATE_ORDER_RANDOM");
	public static String DEFAULT_DIVISIONOPTION= Txt.get("s_TAB_EPIUPDATE_NEWCELLSTATE_NAIVE");
	public static String DEFAULT_DEATHOPTION = Txt.get("s_TAB_EPIUPDATE_CELLDEATH_EMPTY");
	public static int DEFAULT_DEATHRANGE = 1;
	
	public static String DEFAULT_DEATHTRIGGER = Txt.get("s_TAB_EVE_TRIGGER_NONE");
	public static String DEFAULT_DIVISIONTRIGGER = Txt.get("s_TAB_EVE_TRIGGER_NONE");
	public static String DEFAULT_DEATHPATTERN = "";
	public static String DEFAULT_DIVISIONPATTERN = "";
	
	public static int DEFAULT_DIVISIONRANGE = 1;
	public static int DEFAULT_NEIGHBOURSRANGE = 1;
	public static int DEFAULT_COMPRESSIONPARAMETER = 1;
	
	public static float DEFAULT_DIVISIONPROBABILITY = (int) 0.0;
	public static float DEFAULT_DEATHPROBABILITY = (int) 0.0;
	
	public static String DEFAULT_DEATHALGORITHM = Txt.get("s_TAB_EVE_ALGORITHM_RANDOM");
	public static String DEFAULT_DIVISIONALGORITHM = Txt.get("s_TAB_EVE_ALGORITHM_RANDOM");
	
	//GENERAL
	private String eventOrder;
	private String deathOption;
	private String divisionOption;
	private int deathNeighbourRange;
	
	private Map<LogicalModel, ModelCellularEvent> model2MCE;

public EpitheliumEvents(String eventOrder, String deathOption, String divisionOption, int deathNeighbourRange, Map<LogicalModel, ModelCellularEvent> model2MCE, Set<LogicalModel> modelList) {
		
		this.eventOrder = eventOrder;
		this.deathOption = deathOption;
		this.divisionOption = divisionOption;
		this.deathNeighbourRange = deathNeighbourRange;
		this.model2MCE = model2MCE;
		
		for (LogicalModel model: modelList) {
			addModel2MCE(model);
			this.setModel2MCE(model, model2MCE.get(model).clone());
		}
}
		

	public void addModel2MCE(LogicalModel model) {
		if (!model2MCE.containsKey(model)){
			ModelCellularEvent mce = new ModelCellularEvent(DEFAULT_DEATHPROBABILITY,DEFAULT_DIVISIONPROBABILITY,DEFAULT_DEATHTRIGGER, DEFAULT_DIVISIONTRIGGER, DEFAULT_DEATHPATTERN, DEFAULT_DIVISIONPATTERN, new byte[model.getComponents().size()],DEFAULT_DEATHALGORITHM,DEFAULT_DIVISIONALGORITHM,DEFAULT_DIVISIONRANGE,DEFAULT_NEIGHBOURSRANGE, DEFAULT_COMPRESSIONPARAMETER);
			this.setModel2MCE(model, mce);
		}
	}
	
	public void removeModel2MCE(LogicalModel model) {
		if (model2MCE.containsKey(model)){
			this.model2MCE.remove(model);
		}
	}

	public void setModel2MCE (LogicalModel model, ModelCellularEvent mce) {
		this.model2MCE.put(model, mce);
	}

	//Pattern that triggers Death
	public String getDeathPattern(LogicalModel m) {
		return this.model2MCE.get(m).getDeathPattern();
	}
	
	public void setDeathPattern(LogicalModel m, String pattern) {
		this.model2MCE.get(m).setDeathPattern(pattern);
	}
	
	//Pattern that triggers Division
	public String getDivisionPattern(LogicalModel m) {
		return this.model2MCE.get(m).getDivisionPattern();
	}
	
	public void setDivisionPattern(LogicalModel m, String pattern) {
		this.model2MCE.get(m).setDivisionPattern(pattern);
	}
	
	//If cell Division is predefined new cells will have this state (byte array)
	public byte[] getDivisionNewState(LogicalModel m) {
		return this.model2MCE.get(m).getNewCellState();
	}
	
	public void setDivisionNewState(LogicalModel m,byte[] state) {
		this.model2MCE.get(m).setNewCellState(state);
	}
	
	//What type of trigger are we assuming here for Death
	public String getDeathTrigger(LogicalModel m) {
		return this.model2MCE.get(m).getDeathTrigger();
	}
	
	public void setDeathTrigger(LogicalModel m, String str) {
		this.model2MCE.get(m).setDeathTrigger(str);
	}
	
	public String getDivisionTrigger(LogicalModel m) {
		return this.model2MCE.get(m).getDivisionTrigger();
	}
	
	public void setDivisionTrigger(LogicalModel m, String str) {
		this.model2MCE.get(m).setDivisionTrigger(str);
	}
	
	
	public String getEventOrder() {
		return this.eventOrder;
	}
	
	public void setEventOrder(String str) {
		this.eventOrder = str;
	}

	public String getDeathOption() {
		return this.deathOption;
	}
	
	public void setDeathOption(String str) {
		this.deathOption = str;
	}
	
	public String getDivisionOption() {
		return this.divisionOption;
	}
	
	public void setDivisionOption(String option) {
		 this.divisionOption = option;
	}
	
	public float getDivisionValue(LogicalModel m) {
		return this.model2MCE.get(m).getDivisionValue();
	}
	
	public float getDeathValue(LogicalModel m) {
		return this.model2MCE.get(m).getDeathValue();
	}
	
	public void setDivisionValue(LogicalModel m, float value) {
		this.model2MCE.get(m).setDivisionValue(value);
	}
	
	public void setDeathValue(LogicalModel m, float f) {
		this.model2MCE.get(m).setDeathValue(f);
	}

	
	public void setDivisionRange(LogicalModel m, int parseInt) {
		this.model2MCE.get(m).setDivisionRange(parseInt);
		
	}
	
	public void setDivisionAlgorithm(LogicalModel m, String string) {
		this.model2MCE.get(m).setDivisionAlgorithm(string);
		
	}

	public void setDeathAlgorithm(LogicalModel m, String string) {
		this.model2MCE.get(m).setDeathAlgorithm(string);
		
	}
	

	
	
	
	public EpitheliumEvents clone() {
		
		Map<LogicalModel, ModelCellularEvent> model2MCEClone = new HashMap<LogicalModel, ModelCellularEvent>();
		for (LogicalModel m: this.model2MCE.keySet()) {
			model2MCEClone.put(m, this.model2MCE.get(m));
			}
		return new EpitheliumEvents(this.getEventOrder(), this.getDeathOption(), this.getDivisionOption(), this.getDeathNeighbourRange(),model2MCEClone, model2MCEClone.keySet());
	}

	public boolean equals(Object o) {
		
		EpitheliumEvents newEpiEvents = (EpitheliumEvents) o ;
		
		if (!this.eventOrder.equals(newEpiEvents.getEventOrder())) {
			return false;		}
		
		if (!this.deathOption.equals(newEpiEvents.getDeathOption())){
			return false;		}
		
		if (!this.divisionOption.equals(newEpiEvents.getDivisionOption())){
			return false;		}
		if (this.deathNeighbourRange!=(newEpiEvents.getDeathNeighbourRange())){
			return false;		}
		
		for (LogicalModel m: this.model2MCE.keySet()) {
			if (!this.model2MCE.get(m).equals(newEpiEvents.getMCE(m))){
				return false;		}
		}
		return true;
	}
	
	
	public void setDeathNeighbourRange(int range) {
		this.deathNeighbourRange = range;
	}

	public int getDeathNeighbourRange() {
		return this.deathNeighbourRange;
	}


	public ModelCellularEvent getMCE(LogicalModel selModel) {
		return this.model2MCE.get(selModel);
	}


	public Set<LogicalModel> getModels() {
		return this.model2MCE.keySet();
	}

	
}
