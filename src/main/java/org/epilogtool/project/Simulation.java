package org.epilogtool.project;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.colomoto.logicalmodel.LogicalModel;
import org.colomoto.logicalmodel.NodeInfo;
import org.colomoto.logicalmodel.perturbation.AbstractPerturbation;
import org.colomoto.logicalmodel.tool.simulation.updater.PriorityClasses;
import org.colomoto.logicalmodel.tool.simulation.updater.PriorityUpdater;
import org.epilogtool.common.RandomFactory;
import org.epilogtool.common.Tuple2D;
import org.epilogtool.core.Epithelium;
import org.epilogtool.core.EpitheliumGrid;
import org.epilogtool.core.EpitheliumUpdateSchemeInter;
import org.epilogtool.core.UpdateOrder;
import org.epilogtool.integration.IntegrationFunctionEvaluation;
import org.epilogtool.integration.IntegrationFunctionSpecification.IntegrationExpression;

/**
 * Initializes and implements the simulation on epilog.
 * 
 * @author Pedro Monteiro and Pedro Varela
 * 
 */
public class Simulation {
	private Epithelium epithelium;
	private List<EpitheliumGrid> gridHistory;
	private List<String> gridHashHistory;

	private boolean allCellsCalledToUpdate;

	private List<Tuple2D<Integer>> schuffledInstances;
	private int indexOrder;

	private boolean stable;
	private boolean hasCycle;
	// Perturbed models cache - avoids repeatedly computing perturbations at
	// each step
	private PriorityUpdater[][] updaterCache;

	/**
	 * Initializes the simulation. It is called after creating and epithelium.
	 * Creates a list of EpitheliumGrids, to allow the user to travel to a
	 * previously calculated (and saved) epitheliumGrid. This list is
	 * initialized with the current EpitheliumGrid (the one defined in the
	 * initialConditions).
	 * 
	 * @param e
	 *            the epithelium the user is working with.
	 * 
	 */
	public Simulation(Epithelium e) {
		this.epithelium = e;
		this.gridHistory = new ArrayList<EpitheliumGrid>();
		EpitheliumGrid firstGrid = this.epithelium.getEpitheliumGrid();
		firstGrid.updateNodeValueCounts();
		this.gridHistory.add(this.restrictGridWithPerturbations(firstGrid));
		this.gridHashHistory = new ArrayList<String>();
		this.gridHashHistory.add(firstGrid.hashGrid());
		this.stable = false;
		this.hasCycle = false;
		this.buildPriorityUpdaterCache();

		this.allCellsCalledToUpdate = true;
	}

	private EpitheliumGrid restrictGridWithPerturbations(EpitheliumGrid grid) {
		for (int y = 0; y < grid.getY(); y++) {
			for (int x = 0; x < grid.getX(); x++) {
				grid.restrictCellWithPerturbation(x, y);
			}
		}
		return grid;
	}

	private void buildPriorityUpdaterCache() {
		this.updaterCache = new PriorityUpdater[this.getCurrentGrid().getX()][this.getCurrentGrid().getY()];
		Map<LogicalModel, Map<AbstractPerturbation, PriorityUpdater>> tmpMap = new HashMap<LogicalModel, Map<AbstractPerturbation, PriorityUpdater>>();
		for (int y = 0; y < this.getCurrentGrid().getY(); y++) {
			for (int x = 0; x < this.getCurrentGrid().getX(); x++) {
				if (this.getCurrentGrid().hasEmptyModel(x, y)) {
					continue;
				}
				LogicalModel m = this.getCurrentGrid().getModel(x, y);
				AbstractPerturbation ap = this.getCurrentGrid().getPerturbation(x, y);
				if (!tmpMap.containsKey(m))
					tmpMap.put(m, new HashMap<AbstractPerturbation, PriorityUpdater>());
				if (!tmpMap.get(m).containsKey(ap)) {
					// Apply model perturbation
					LogicalModel perturb = (ap == null) ? m : ap.apply(m);
					// Get Priority classes
					PriorityClasses pcs = this.epithelium.getPriorityClasses(m).getPriorities();
					PriorityUpdater updater = new PriorityUpdater(perturb, pcs);
					tmpMap.get(m).put(ap, updater);
				}
				this.updaterCache[x][y] = tmpMap.get(m).get(ap);
			}
		}
	}

	/**
	 * This function retrieves the next step in the simulation. The first step
	 * in this
	 */
	public EpitheliumGrid nextStepGrid() {
		EpitheliumGrid currGrid = this.getCurrentGrid();
		if (this.stable) {
			return currGrid;
		}

		EpitheliumGrid currNeighboursGrid = this.getNeighboursGrid();

		EpitheliumGrid nextGrid = currGrid.clone();

		Set<ComponentPair> sIntegComponentPairs = this.epithelium.getIntegrationComponentPairs();

		IntegrationFunctionEvaluation evaluator = new IntegrationFunctionEvaluation(currNeighboursGrid,
				this.epithelium.getProjectFeatures());

		// Gets the set of cells that can be updated
		// And builds the default next grid (= current grid)
		HashMap<Tuple2D<Integer>, byte[]> cells2update = new HashMap<Tuple2D<Integer>, byte[]>();
		List<Tuple2D<Integer>> keys = new ArrayList<Tuple2D<Integer>>();
		List<Tuple2D<Integer>> changedKeys = new ArrayList<Tuple2D<Integer>>();

		for (int y = 0; y < currGrid.getY(); y++) {
			for (int x = 0; x < currGrid.getX(); x++) {
				if (currGrid.hasEmptyModel(x, y)) {
					continue;
				}
				byte[] currState = currGrid.getCellState(x, y);

				// Compute next state
				byte[] nextState = this.nextCellValue(x, y, currGrid, evaluator, sIntegComponentPairs);

				// If the cell state changed then add it to the pool
				Tuple2D<Integer> key = new Tuple2D<Integer>(x, y);
				cells2update.put(key, nextState);
				keys.add(key);
				if (!Arrays.equals(currState, nextState)) {
					changedKeys.add(key);
				}
			}
		}

		if (!this.allCellsCalledToUpdate) {
			keys = changedKeys;
		}

		if (changedKeys.size() == 0 || keys.size() == 0) {
			this.stable = true;
			return currGrid;
		} else {

			// Inter-cellular alpha-asynchronism
			float alphaProb = this.epithelium.getUpdateSchemeInter().getAlpha();
			int numberCellsCalledToUpdate = (int) Math.floor(alphaProb * keys.size());
			if (numberCellsCalledToUpdate == 0) {
				numberCellsCalledToUpdate = 1;
			}
			List<Tuple2D<Integer>> cellsUpdatedThisStep = new ArrayList<Tuple2D<Integer>>();

			// RANDOM_INDEP + Cyclic - BEGIN
			if (this.schuffledInstances == null) {
				// Create the initial shuffled array of cells
				Collections.shuffle(keys, RandomFactory.getInstance().getGenerator());
				this.schuffledInstances = keys;
				this.indexOrder = 0;
			}

			for (int idx = 0; idx < numberCellsCalledToUpdate; idx++, this.indexOrder++) {
				if (this.indexOrder == this.schuffledInstances.size()) {
					if (this.epithelium.getUpdateSchemeInter().getUpdateOrder().equals(UpdateOrder.RANDOM_ORDER)) {
						Collections.shuffle(keys, RandomFactory.getInstance().getGenerator());
						this.schuffledInstances = keys;
					}
					this.indexOrder = 0;
				}
				Tuple2D<Integer> cell = this.schuffledInstances.get(indexOrder);
				while (cellsUpdatedThisStep.contains(cell)) {
					// only valid for RANDOM_ORDER
					int displace = numberCellsCalledToUpdate - idx + this.indexOrder;
					int n = RandomFactory.getInstance().nextInt(this.schuffledInstances.size() - displace);
					Collections.swap(this.schuffledInstances, this.indexOrder, n + displace);
					cell = this.schuffledInstances.get(this.indexOrder);
				}
				cellsUpdatedThisStep.add(cell);
			}

			for (Tuple2D<Integer> key : cellsUpdatedThisStep) {
				nextGrid.setCellState(key.getX(), key.getY(), cells2update.get(key));
			}
		}

		nextGrid.updateNodeValueCounts();

		this.gridHistory.add(nextGrid);
		this.gridHashHistory.add(nextGrid.hashGrid());
		return nextGrid;
	}

	public boolean hasCycle() {
		if (!this.hasCycle) {
			Set<String> sStateHistory = new HashSet<String>(this.gridHashHistory);
			this.hasCycle = (sStateHistory.size() < this.gridHashHistory.size());
		}
		return this.hasCycle;
	}

	private byte[] nextCellValue(int x, int y, EpitheliumGrid currGrid, IntegrationFunctionEvaluation evaluator,
			Set<ComponentPair> sIntegComponentPairs) {
		byte[] currState = currGrid.getCellState(x, y).clone();

		PriorityUpdater updater = this.updaterCache[x][y];
		LogicalModel m = this.epithelium.getEpitheliumGrid().getModel(x, y);

		// 2. Update integration components
		for (NodeInfo node : m.getNodeOrder()) {
			ComponentPair nodeCP = new ComponentPair(m, node);
			if (node.isInput() && sIntegComponentPairs.contains(nodeCP)) {
				List<IntegrationExpression> lExpressions = this.epithelium.getIntegrationFunctionsForComponent(nodeCP)
						.getComputedExpressions();
				byte target = 0;
				for (int i = 0; i < lExpressions.size(); i++) {
					if (evaluator.evaluate(x, y, lExpressions.get(i))) {
						target = (byte) (i + 1);
						break; // The lowest value being satisfied
					}
				}
				currState[m.getNodeOrder().indexOf(node)] = target;
			}
		}

		List<byte[]> succ = updater.getSuccessors(currState);
		if (succ == null) {
			return currState;
		} else if (succ.size() > 1) {
			// FIXME
			// throw new Exception("Argh");
		}
		return succ.get(0);
	}

	public boolean isStableAt(int i) {
		return (i >= this.gridHistory.size() && this.stable);
	}

	public boolean hasCycleAt(int i) {
		if (!(this.epithelium.getUpdateSchemeInter().getAlpha() == 1)) {
			return false;
		}
		List<String> tmpList = new ArrayList<String>(this.gridHashHistory.subList(0, i));
		Set<String> tmpSet = new HashSet<String>(tmpList);
		return !(tmpSet.size() == tmpList.size());
	}

	public EpitheliumGrid getGridAt(int i) {
		if (i < this.gridHistory.size()) {
			return this.gridHistory.get(i);
		}
		return this.nextStepGrid();
	}

	public EpitheliumGrid getCurrentGrid() {
		return gridHistory.get(gridHistory.size() - 1);
	}

	public Epithelium getEpithelium() {
		return this.epithelium;
	}

	private EpitheliumGrid getNeighboursGrid() {
		// Creates an epithelium which is only visited to 'see' neighbours and
		// their states
		EpitheliumGrid neighbourEpi = this.getCurrentGrid().clone();

		Map<ComponentPair, Float> mSigmaAsync = this.epithelium.getUpdateSchemeInter().getCPSigmas();

		Map<LogicalModel, Set<Tuple2D<Integer>>> mapModelPositions = neighbourEpi.getModelPositions();
		if (mSigmaAsync.size() == 0) {
			return neighbourEpi;
		} else {
			EpitheliumGrid delayGrid = this.gridHistory.get(0);
			if (this.gridHistory.size() >= 2) {
				delayGrid = this.gridHistory.get(this.gridHistory.size() - 2);
			}
			for (ComponentPair cp : mSigmaAsync.keySet()) {
				float sigma = mSigmaAsync.get(cp);
				if (sigma != EpitheliumUpdateSchemeInter.DEFAULT_SIGMA) {
					LogicalModel m = cp.getModel();
					String nodeID = cp.getNodeInfo().getNodeID();
					List<NodeInfo> modelNodes = m.getNodeOrder();
					int nodePosition = modelNodes.indexOf(cp.getNodeInfo());
					List<Tuple2D<Integer>> modelPositions = new ArrayList<Tuple2D<Integer>>(mapModelPositions.get(m));
					int selectedCells = (int) Math.ceil((1 - sigma) * modelPositions.size());
					Collections.shuffle(modelPositions, RandomFactory.getInstance().getGenerator());
					List<Tuple2D<Integer>> selectedModelPositions = modelPositions.subList(0, selectedCells);
					for (Tuple2D<Integer> tuple : selectedModelPositions) {
						if (!(delayGrid.getModel(tuple.getX(), tuple.getY()).equals(m))) {
							neighbourEpi.setCellComponentValue(tuple.getX(), tuple.getY(), nodeID, (byte) 0);
						} else {
							byte[] delayState = delayGrid.getCellState(tuple.getX(), tuple.getY());
							neighbourEpi.setCellComponentValue(tuple.getX(), tuple.getY(), nodeID,
									(byte) delayState[nodePosition]);
						}
					}
				}
			}
			return neighbourEpi;
		}
	}

	public List<String> getCell2Percentage() {
		List<String> mesList = new ArrayList<String>();

		int index = 0;
		for (EpitheliumGrid grid : this.gridHistory) {
			for (LogicalModel model : grid.getModelSet()) {
				for (NodeInfo node : model.getNodeOrder()) {
					String mes = index + ": " + node.getNodeID() + " " + grid.getPercentage(node.getNodeID());
					mesList.add(mes);
				}
			}
			index = index + 1;
		}

		return mesList;
	}
}
