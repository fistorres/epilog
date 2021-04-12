package org.epilogtool.core;

import java.awt.Color;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

import org.colomoto.biolqm.LogicalModel;

public class EpitheliumPhenotypes {
	
	public Map<LogicalModel,Set<Phenotype>> phenotypesToTrack;
	
	public EpitheliumPhenotypes() {
		this.phenotypesToTrack = new HashMap<LogicalModel, Set<Phenotype>>();
	}
	
	public void addModel(LogicalModel model) {
		this.phenotypesToTrack.put(model, new HashSet<Phenotype>());
		
	}

	public void removeModel(LogicalModel m) {
		if (this.phenotypesToTrack.containsKey(m))
			this.phenotypesToTrack.remove(m);
	}

	public Set<Phenotype> getPhenotypes(LogicalModel m) {
		return this.phenotypesToTrack.get(m);
	}
	
	public Map<LogicalModel,Set<Phenotype>> getPhenotypes() {
		return this.phenotypesToTrack;
	}

	public void addPhenotype(LogicalModel model, String name, Boolean use, Color color, String pheno) {
		if (!this.phenotypesToTrack.containsKey(model))
			this.addModel(model);
		
		Set<Phenotype> temp = this.phenotypesToTrack.get(model);
		
		boolean valid = true;
		for (Phenotype ph : temp) {
			if (ph.pheno.equals(pheno)) {
				valid = false;
				break;
			}
		}
			if (valid)
				temp.add(new Phenotype(color, name, pheno, use));
	}
	
	public void addPhenotype(LogicalModel model, Phenotype pheno) {
		if (!this.phenotypesToTrack.containsKey(model))
			this.addModel(model);
		
		Set<Phenotype> temp = this.phenotypesToTrack.get(model);
		temp.add(pheno);

	}
	
	public void removePhenotype(LogicalModel model, String name, Boolean use, Color color, String pheno) {
		if (!this.phenotypesToTrack.containsKey(model))
			return;
		
		Set<Phenotype> temp = this.phenotypesToTrack.get(model);
		Phenotype phenotype = new Phenotype(color, name, pheno, use);
		if (temp.contains(phenotype))
			temp.remove(phenotype);
	}
	
	public void addPhenoSet(LogicalModel model, Set<Phenotype> phenos) {
		if (!this.phenotypesToTrack.containsKey(model))
			this.addModel(model);
		Set<Phenotype> temp = this.phenotypesToTrack.get(model);
		temp.addAll(phenos);
	}

	public Set<LogicalModel> getModelSet() {
		return this.phenotypesToTrack.keySet();
	}
	
	public EpitheliumPhenotypes clone() {
		EpitheliumPhenotypes newPhenos = new EpitheliumPhenotypes();
		
		for (LogicalModel m : this.phenotypesToTrack.keySet()) {
			Set<Phenotype> phenosClone = new HashSet<Phenotype>();
			for (Phenotype pheno : this.phenotypesToTrack.get(m))
				phenosClone.add(pheno.clone());
			
			if (phenosClone != null) 
				newPhenos.addPhenoSet(m, phenosClone);
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
	
	
	public class Phenotype implements Comparable<Phenotype> {
		
		private Color phenoColor;
		private Boolean use;
		private String name;
		private String pheno; // ^[1,0,.]$		
		
		Phenotype(Color color, String name, String pheno, Boolean use) {
			this.phenoColor = color;
			this.name = name;
			this.pheno = pheno;
			this.use = use;
		}
		
		public boolean match(byte[] state) {
			
			String[] stateArray = new String[state.length];
			for (int i = 0; i < state.length; i++)
				stateArray[i] = "" + state[i];
			
			String stateS = String.join("", stateArray);
			return Pattern.matches(pheno.replace("*", "."), stateS);
		}
		
		public Color getColor() {
			return this.phenoColor;
		}
		public String getName() {
			return this.name;
		}
		public String getPheno() {
			return this.pheno;
		}
		public Boolean getUse() {
			return this.use;
		}
		
		@Override
		public int compareTo(Phenotype other) {
			return (this.name.compareTo(other.name));
		 }
		
		@Override
		public Phenotype clone() {
			return new Phenotype(this.phenoColor, this.name, this.pheno, this.use);
		}
		
		@Override
		public boolean equals(Object o) {
			Phenotype outPheno = (Phenotype) o;
			if (!outPheno.phenoColor.equals(this.phenoColor))
				return false;
			if (!outPheno.name.equals(this.name))
				return false;
			if (!outPheno.pheno.equals(this.pheno))
				return false;
			if (!outPheno.use.equals(this.use))
				return false;
			return false;
		}
		@Override
		public int hashCode() {
	        return Objects.hashCode(this.phenoColor) + 
	        		Objects.hashCode(this.name) +
	        		Objects.hashCode(this.pheno) +
	        		Objects.hashCode(this.use);
	    }
	}
}
