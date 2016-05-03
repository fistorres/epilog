package org.epilogtool.core;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.colomoto.logicalmodel.LogicalModel;
import org.colomoto.logicalmodel.perturbation.AbstractPerturbation;
import org.epilogtool.common.Tuple2D;
import org.epilogtool.core.cellDynamics.CellTrigger;
import org.epilogtool.core.topology.RollOver;
import org.epilogtool.core.topology.Topology;
import org.epilogtool.project.ComponentPair;
import org.epilogtool.services.TopologyService;

public class EpitheliumGrid {
	private EpitheliumCell[][] gridEpiCell;
	private Topology topology;
	private Set<LogicalModel> modelSet;
	private Map<LogicalModel, List<Tuple2D<Integer>>> modelPositions;

	private EpitheliumGrid(EpitheliumCell[][] gridEpiCell, Topology topology,
			Set<LogicalModel> modelSet,
			Map<LogicalModel, List<Tuple2D<Integer>>> modelPositions) {
		this.gridEpiCell = gridEpiCell;
		this.topology = topology;
		this.modelSet = modelSet;
		this.modelPositions = modelPositions;
	}

	public EpitheliumGrid(int gridX, int gridY, String topologyLayout,
			RollOver rollover, LogicalModel m) throws InstantiationException,
			IllegalAccessException, IllegalArgumentException,
			InvocationTargetException, NoSuchMethodException,
			SecurityException, ClassNotFoundException {
		this.topology = TopologyService.getManager().getNewTopology(
				topologyLayout, gridX, gridY, rollover);
		this.modelPositions = new HashMap<LogicalModel, List<Tuple2D<Integer>>>();
		this.gridEpiCell = new EpitheliumCell[gridX][gridY];
		for (int y = 0; y < gridY; y++) {
			for (int x = 0; x < gridX; x++) {
				this.gridEpiCell[x][y] = new EpitheliumCell(m);
				if (this.modelPositions.keySet().contains(m)) {
					this.modelPositions.get(m).add(new Tuple2D<Integer>(x, y));
				} else {
					List<Tuple2D<Integer>> tmpList = new ArrayList<Tuple2D<Integer>>();
					tmpList.add(new Tuple2D<Integer>(x, y));
					this.modelPositions.put(m, tmpList);
				}
			}
		}
		this.modelSet = new HashSet<LogicalModel>();
		this.modelSet.add(m);
	}
	
	public int getX() {
		return this.topology.getX();
	}
	
	public int getY() {
		return this.topology.getY();
	}
	
	public Topology getTopology() {
		return this.topology;
	}
	
	public LogicalModel getModel(int x, int y) {
		return gridEpiCell[x][y].getModel();
	}
	
	public boolean hasModel(LogicalModel m) {
		return this.modelSet.contains(m);
	}
	
	public byte[] getCellState(int x, int y) {
		return gridEpiCell[x][y].getState();
	}
	
	public Map<ComponentPair, Byte> getCellEnvironment(int x, int y) {
		return this.gridEpiCell[x][y].getCellEnvironment();
	}
	
	public byte[] getCellInitialState(int x, int y) {
		return gridEpiCell[x][y].getInitialState();
	}

	public AbstractPerturbation getPerturbation(int x, int y) {
		return gridEpiCell[x][y].getPerturbation();
	}
	
	public Map<LogicalModel, List<Tuple2D<Integer>>> getModelPositions() {
		return this.modelPositions;
	}
	
	public int getNodeIndex(int x, int y, String nodeID) {
		return this.gridEpiCell[x][y].getNodeIndex(nodeID);
	}
	
	public byte getCellComponentValue(int x, int y, String nodeID) {
		return this.gridEpiCell[x][y].getNodeValue(nodeID);
	}
	
	public Set<LogicalModel> getModelSet() {
		return Collections.unmodifiableSet(this.modelSet);
	}
	
	public CellTrigger getCellTrigger(int x, int y) {
		return this.gridEpiCell[x][y].getCellTrigger();
	}
	
	public boolean isEmptyCell(int x, int y) {
		return this.gridEpiCell[x][y].isEmptyCell();
	}

	public void updateModelSet() {
		this.modelSet = new HashSet<LogicalModel>();
		this.modelPositions = new HashMap<LogicalModel, List<Tuple2D<Integer>>>();
		for (int y = 0; y < this.getY(); y++) {
			for (int x = 0; x < this.getX(); x++) {
				if (this.isEmptyCell(x, y)) {
					continue;
				}
				LogicalModel m = this.gridEpiCell[x][y].getModel();
				this.modelSet.add(m);
				Tuple2D<Integer> tmpTuple = new Tuple2D<Integer>(x, y);
				if (this.modelPositions.containsKey(m)) {
					this.modelPositions.get(m).add(tmpTuple);
				} else {
					List<Tuple2D<Integer>> tmpList = new ArrayList<Tuple2D<Integer>>();
					tmpList.add(tmpTuple);
					this.modelPositions.put(m, tmpList);
				}
			}
		}
	}
		
	public void setRollOver(RollOver r) {
		this.topology.setRollOver(r);
	}
	
	public void setModel(int x, int y, LogicalModel m) {
		Tuple2D<Integer> tmpTuple = new Tuple2D<Integer>(x, y);
		if (this.gridEpiCell[x][y].getModel() != m) {
			if (!EmptyModel.getInstance().isEmptyModel(this.gridEpiCell[x][y].getModel())){
				if (this.modelPositions.get(this.gridEpiCell[x][y].getModel())
						.contains(tmpTuple)) {
					this.modelPositions.get(this.gridEpiCell[x][y].getModel())
						.remove(tmpTuple);
				}
			}
		}
		gridEpiCell[x][y].setModel(m);
		if (!this.modelPositions.containsKey(m)) {
			List<Tuple2D<Integer>> tmpList = new ArrayList<Tuple2D<Integer>>();
			tmpList.add(tmpTuple);
			this.modelPositions.put(m, tmpList);
		} else {
			this.modelPositions.get(m).add(tmpTuple);
		}
	}
	
	public void setPerturbation(LogicalModel m, List<Tuple2D<Integer>> lTuples,
			AbstractPerturbation ap) {
		for (Tuple2D<Integer> tuple : lTuples) {
			if (this.gridEpiCell[tuple.getX()][tuple.getY()].getModel().equals(
					m)) {
				this.setPerturbation(tuple.getX(), tuple.getY(), ap);
			}
		}
	}
	
	public void setPerturbation(int x, int y, AbstractPerturbation ap) {
		gridEpiCell[x][y].setPerturbation(ap);
	}
	
	public void setCellState(int x, int y, byte[] state) {
		gridEpiCell[x][y].setState(state);
	}
	
	public void setCellInitialState(int x, int y, byte[] state) {
		gridEpiCell[x][y].setInitialState(state);
	}
	
	public void setCell2Naive(int x, int  y) {
		gridEpiCell[x][y].setState(gridEpiCell[x][y].getInitialState().clone());
		gridEpiCell[x][y].setCellTrigger(CellTrigger.DEFAULT);
	}
	
	public void setCellTrigger(int x, int y, CellTrigger trigger) {
		this.gridEpiCell[x][y].setCellTrigger(trigger);
	}
	
	public void setCellComponentValue(int x, int y, String nodeID, byte value) {
		gridEpiCell[x][y].setValue(nodeID, value);
	}
	
	public void setEnvironmentalInput(int x, int y, ComponentPair cp, byte value) {
		this.gridEpiCell[x][y].addEnvironmentalInput(cp, value);
	}
	
	public void setGridEnvironment(ComponentPair cp) {
		for (int x = 0; x < this.getX(); x ++) {
			for (int y = 0; y < this.getY(); y ++) {
				this.gridEpiCell[x][y].addEnvironmentalInput(cp, (byte) 0); 
			}
		}
	}
	
	public void removeGridEnvironment(ComponentPair cp) {
		for (int x = 0; x < this.getX(); x ++) {
			for (int y = 0; y < this.getY(); y ++) {
				this.gridEpiCell[x][y].removeEnvironmentalInput(cp);
			}
		}
	}

	public EpitheliumCell cloneEpitheliumCellAt(int x, int y) {
		return this.gridEpiCell[x][y].clone();
	}
	
	private void cloneCellPosition(Tuple2D<Integer> originalPos, Tuple2D<Integer> clonedPos) {
		LogicalModel originalModel = this.getModel(originalPos.getX(), originalPos.getY());
		LogicalModel clonedModel = this.getModel(clonedPos.getX(), clonedPos.getY());
		if (!(clonedModel.equals(originalModel)) 
				&& !(EmptyModel.getInstance().isEmptyModel(originalModel))){
			this.modelPositions.get(originalModel).add(clonedPos);
			if (!(EmptyModel.getInstance().isEmptyModel(clonedModel))) {
				this.modelPositions.get(clonedModel).remove(clonedPos);
			}
		}
		this.gridEpiCell[clonedPos.getX()][clonedPos.getY()]
				.setLogicalCell(this.gridEpiCell[originalPos.getX()][originalPos.getY()]
						.getLogicalCell().clone());
	}
	
	public void shiftCells(List<Tuple2D<Integer>> path) {
		for (int index = 1; index < path.size(); index ++) {
			this.cloneCellPosition(path.get(index), path.get(index-1));
		}
	}
		
	public int emptyModelNumber(){
		int gridSize = this.getX() * this.getY();
		int cellNumber = 0;
		for (LogicalModel m : this.modelPositions.keySet()) {
			cellNumber += this.modelPositions.get(m).size();
		}
		return gridSize - cellNumber;
	}
	
	public String hashGrid() {
		String hash = "";
		for (int y = 0; y < this.getY(); y++) {
			for (int x = 0; x < this.getX(); x++) {
				hash += this.gridEpiCell[x][y].hashState();
			}
		}
		return hash;
	}

	public String toString() {
		String s = "";
		for (int y = 0; y < this.getY(); y++) {
			s += (y + 1) + "|";
			for (int x = 0; x < this.getX(); x++) {
				byte[] currState = this.getCellState(x, y);
				for (int i = 0; i < currState.length; i++) {
					s += currState[i];
				}
				s += "|";
			}
			s += "\n";
		}
		return s;
	}

	public boolean equals(Object a) {
		if (a == null || !(a instanceof EpitheliumGrid))
			return false;
		if (a == this)
			return true;
		EpitheliumGrid o = (EpitheliumGrid) a;

		if (!this.topology.equals(o.topology))
			return false;
		for (int y = 0; y < this.getY(); y++) {
			for (int x = 0; x < this.getX(); x++) {
				if (!this.gridEpiCell[x][y].equals(o.gridEpiCell[x][y]))
					return false;
			}
		}
		return true;
	}
	
	public EpitheliumGrid clone() {
		EpitheliumCell[][] newGrid = new EpitheliumCell[this.getX()][this
				.getY()];
		for (int y = 0; y < this.getY(); y++) {
			for (int x = 0; x < this.getX(); x++) {
				newGrid[x][y] = this.gridEpiCell[x][y].clone();
			}
		}
		Topology newTop = this.topology.clone();
		Set<LogicalModel> newModelSet = new HashSet<LogicalModel>(this.modelSet);
		Map<LogicalModel, List<Tuple2D<Integer>>> newModelPositions = new HashMap<LogicalModel, List<Tuple2D<Integer>>>();
		for (LogicalModel m : this.modelPositions.keySet()) {
			newModelPositions.put(m, new ArrayList<Tuple2D<Integer>>(this.modelPositions.get(m)));
		}
		return new EpitheliumGrid(newGrid, newTop, newModelSet,
				newModelPositions);
	}
}
