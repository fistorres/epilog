package org.epilogtool.core;

import java.util.Arrays;
import java.util.List;

import org.antlr.runtime.RecognitionException;
import org.epilogtool.integration.IntegrationFunctionSpecification;
import org.epilogtool.integration.IntegrationFunctionSpecification.IntegrationExpression;

public class ComponentIntegrationFunctions {
	private String[] stringExpr;
	private IntegrationExpression[] computedExpr;

	public ComponentIntegrationFunctions(int maxValue) {
		this.stringExpr = new String[maxValue];
		for (int i = 0; i < maxValue; i++) {
			this.stringExpr[i] = "";
		}
		this.computedExpr = new IntegrationExpression[maxValue];
	}

	public ComponentIntegrationFunctions clone() {
		ComponentIntegrationFunctions newcif = new ComponentIntegrationFunctions(
				this.stringExpr.length);
		for (byte i = 1; i <= this.stringExpr.length; i++) {
			newcif.setFunctionAtLevel(i, this.stringExpr[i - 1]);
		}
		return newcif;
	}

	public List<IntegrationExpression> getComputedExpressions() {
		return Arrays.asList(this.computedExpr);
	}

	private IntegrationExpression string2Expression(String expr) {
		IntegrationFunctionSpecification spec = new IntegrationFunctionSpecification();
		IntegrationExpression expression = null;
		try {
			expression = spec.parse(expr);
		} catch (RecognitionException e) {
			e.printStackTrace();
		}
		return expression;
	}

	public void setFunctionAtLevel(byte value, String function) {
		this.stringExpr[value - 1] = function;
		this.computedExpr[value - 1] = this.string2Expression(function);
	}

	public boolean isValidAtLevel(byte level) {
		// TODO 1: use parser to validate syntax
		// TODO 2: use nodeID to validate semantics (nodeID, values, ...)
		return true;
	}

	public List<String> getFunctions() {
		return Arrays.asList(this.stringExpr);
	}

	public boolean equals(Object o) {
		ComponentIntegrationFunctions cif = (ComponentIntegrationFunctions) o;
		List<String> outStrings = cif.getFunctions();
		for (int i = 0; i < this.stringExpr.length; i++) {
			if (!this.stringExpr[i].equals(outStrings.get(i)))
				return false;
		}
		return true;
	}
}