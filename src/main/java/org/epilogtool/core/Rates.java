package org.epilogtool.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.colomoto.biolqm.LogicalModel;
import org.colomoto.biolqm.NodeInfo;
import org.colomoto.biolqm.tool.simulation.grouping.PCRankGroupsVars;


public class Rates {

	protected LogicalModel model;
	// Node and rate
	private Map<NodeInfo, Double> rates;
	Boolean uniformProb; 
	
	/*
	 * Rates(ModelGrouping mpc) { this.model = mpc.getModel(); List<NodeInfo>
	 * components = this.model.getComponents(); for (int i = 0;) mpc.getClass(0) }
	 */

	Rates(LogicalModel model) {
		this.model = model;
		this.rates = new LinkedHashMap<NodeInfo, Double>();
		List<NodeInfo> components = this.model.getComponents();

		this.uniformProb = true;
		// uniform probability.
		for (int i = 0; i < components.size(); i++) {
			NodeInfo node = components.get(i);

			if (node.isInput()) {
				this.rates.put(node, 0.0);
			} else {
				this.rates.put(node, 1.0);
			}
		}
	}
	
	// rates should be something like "[0.2,0.1,...]"
	Rates(LogicalModel model, String rates) {
		this.model = model;
		this.rates = new LinkedHashMap<NodeInfo, Double>();
		List<NodeInfo> components = this.model.getComponents();
		this.uniformProb = false;
		
		// remove "[" and "]"
		rates = rates.substring(1, rates.length() - 1);
		String[] nodeRates = rates.split(",");
		for (int i = 0; i < components.size(); i++) {
			NodeInfo node = components.get(i);
			
			double rate = Double.parseDouble(nodeRates[i]);

			if (node.isInput()) {
				this.rates.put(node, 0.0);
			} else {
				this.rates.put(node, rate);
			}
		}

	}

	public Map<NodeInfo, Double> getRates() {
		return this.rates;
	}
	
	public double[] getAllRates() {
		double[] rates = new double[this.rates.size()];
		int i = 0;
		for (NodeInfo node : this.rates.keySet()) {
			rates[i] = this.rates.get(node);
			i += 1;
		}
		return rates;
	}

	public LogicalModel getModel() {
		return this.model;
	}

	
	 public Double getNodeRate(String nodeName) { 
		 for (NodeInfo node : this.rates.keySet()) {
			 if (node.getNodeID() == nodeName) 
				 return  this.rates.get(node); 
			 }
		 return -1.0;
		 }
	 

	public Double getNodeRate(NodeInfo node) {
		for (NodeInfo nodeX : this.rates.keySet()) {
			if (nodeX.equals(node))
				return this.rates.get(node);
		}
		return -1.0;
	}

	public Boolean isUniform() {
		return this.uniformProb;
	}
	
	public Boolean setUniform(Boolean uniform) {
		return this.uniformProb = uniform;
	}


	
	 public void setNodeRate(String nodeName, Double rate) { 
		 for (NodeInfo node : this.rates.keySet()) { 
			 if (node.getNodeID() == nodeName)
				 this.rates.put(node,rate); 
			 } 
		 this.uniformProb = false; 
		 }
	 

	public void setNodeRate(NodeInfo node, Double rate) {
		for (NodeInfo nodeX : this.rates.keySet()) {
			if (nodeX.equals(node))
				this.rates.put(node, rate);
			this.uniformProb = false;
		}
	}

	public Rates clone() {
		Rates newRates = new Rates(this.model);
		for (NodeInfo node : this.rates.keySet()) {
			newRates.setNodeRate(node, this.getNodeRate(node));
		}
		
		newRates.setUniform(this.uniformProb);
		return newRates;
	}

	public boolean equals(Object o) {
		Rates outRates = (Rates) o;
		Set<NodeInfo> allModels = new HashSet<NodeInfo>();

		allModels.addAll(this.rates.keySet());
		allModels.addAll(outRates.getRates().keySet());

		for (NodeInfo node : allModels) {
			if (!this.rates.containsKey(node) || !outRates.getRates().containsKey(node))
				return false;
			if (!(this.getNodeRate(node) == outRates.getNodeRate(node)))
				;
			return false;
		}
		return true;
	}

}
