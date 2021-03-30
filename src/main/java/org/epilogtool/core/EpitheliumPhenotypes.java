package org.epilogtool.core;

import java.awt.Color;
import java.awt.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.colomoto.biolqm.LogicalModel;
import org.colomoto.biolqm.tool.simulation.grouping.ModelGrouping;

public class EpitheliumPhenotypes {
	
	public Map<LogicalModel,ArrayList<Phenotype>> phenotypesToTrack;
	
	public EpitheliumPhenotypes() {
		this.phenotypesToTrack = new HashMap<LogicalModel, ArrayList<Phenotype>>();
	}
	
	public void addModel(LogicalModel model) {
		this.phenotypesToTrack.put(model, new ArrayList<Phenotype>());
		
	}

	public void removeModel(LogicalModel m) {
		if (this.phenotypesToTrack.containsKey(m))
			this.phenotypesToTrack.remove(m);
	}

	public ArrayList<Phenotype> getPhenotypes(LogicalModel m) {
		return this.phenotypesToTrack.get(m);
	}

	public void addPhenotype(LogicalModel model, Color color, String name, String pheno) {
		if (!this.phenotypesToTrack.containsKey(model))
			this.addModel(model);
		
		ArrayList<Phenotype> temp = this.phenotypesToTrack.get(model);
		temp.add(new Phenotype(color, name, pheno));

	}
	
	public void addPhenoArray(LogicalModel model, ArrayList<Phenotype> phenos) {
		if (!this.phenotypesToTrack.containsKey(model))
			this.addModel(model);
		ArrayList<Phenotype> temp = this.phenotypesToTrack.get(model);
		temp.addAll(phenos);
	}

	public Set<LogicalModel> getModelSet() {
		return this.phenotypesToTrack.keySet();
	}
	
	public EpitheliumPhenotypes clone() {
		EpitheliumPhenotypes newPhenos = new EpitheliumPhenotypes();
		
		for (LogicalModel m : this.phenotypesToTrack.keySet()) {
			ArrayList<Phenotype> phenosClone = (ArrayList<Phenotype>) newPhenos.getPhenotypes(m).clone();
			newPhenos.addPhenoArray(m, phenosClone);
		}
		return newPhenos;
	}

	public boolean equals(Object o) {
		EpitheliumPhenotypes phenoOut = (EpitheliumPhenotypes) o;
		Set<LogicalModel> sAllModels = new HashSet<LogicalModel>();
		sAllModels.addAll(this.phenotypesToTrack.keySet());
		sAllModels.addAll(phenoOut.phenotypesToTrack.keySet());
		
		for (LogicalModel m : sAllModels) {
			if (!this.phenotypesToTrack.containsKey(m)
					|| !phenoOut.phenotypesToTrack.containsKey(m))
				return false;
			if (!this.phenotypesToTrack.get(m).equals(
				phenoOut.phenotypesToTrack.get(m)))
				return false;
		}
		return true;
	}
	
	
	public class Phenotype {
		
		private Color phenoColor;
		private String name;
		private String pheno; // ^[1,0,.]$		
		
		Phenotype(Color color, String name, String pheno) {
			this.phenoColor = color;
			this.name = name;
			this.pheno = pheno;
		}
		
		public boolean match(byte[] state) {
			String stateS = Arrays.toString(state);
			return Pattern.matches(pheno, stateS);
		}
		
		public Phenotype clone() {
			return new Phenotype(this.phenoColor, this.name, this.pheno);
		}
		
		public boolean equals(Object o) {
			Phenotype outPheno = (Phenotype) o;
			if (outPheno.phenoColor != this.phenoColor)
				return false;
			if (outPheno.name != this.name)
				return false;
			if (outPheno.pheno != this.pheno)
				return false;
			return true;
		}
	}
}
