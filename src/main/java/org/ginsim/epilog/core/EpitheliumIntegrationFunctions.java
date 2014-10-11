package org.ginsim.epilog.core;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.colomoto.logicalmodel.NodeInfo;

public class EpitheliumIntegrationFunctions {
	private Map<String, ComponentIntegrationFunctions> functions;

	public EpitheliumIntegrationFunctions() {
		this.functions = new HashMap<String, ComponentIntegrationFunctions>();
	}

	public EpitheliumIntegrationFunctions clone() {
		EpitheliumIntegrationFunctions newEIF = new EpitheliumIntegrationFunctions();
		Map<String, ComponentIntegrationFunctions> newFuncs = new HashMap<String, ComponentIntegrationFunctions>();
		for (String nodeID : this.functions.keySet()) {
			newFuncs.put(nodeID, this.functions.get(nodeID).clone());
		}
		newEIF.setFunctions(newFuncs);
		return newEIF;
	}

	private void setFunctions(Map<String, ComponentIntegrationFunctions> f) {
		this.functions = f;
	}

	public void setFunctionAtLevel(NodeInfo node, byte value,
			String function) {
		this.functions.get(node.getNodeID())
				.setFunctionAtLevel(value, function);
	}

	public void addComponent(NodeInfo node) {
		this.functions.put(node.getNodeID(), new ComponentIntegrationFunctions(
				node.getMax()));
	}

	public void removeComponent(String nodeID) {
		this.functions.remove(nodeID);
	}

	public boolean containsKey(String nodeID) {
		return this.functions.containsKey(nodeID);
	}

	public Set<String> getComponents() {
		return Collections.unmodifiableSet(this.functions.keySet());
	}

	public ComponentIntegrationFunctions getComponentIntegrationFunctions(String nodeID) {
		return this.functions.get(nodeID);
	}
}