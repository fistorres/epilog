package org.epilogtool.core;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.colomoto.logicalmodel.LogicalModel;
import org.colomoto.logicalmodel.perturbation.AbstractPerturbation;
import org.epilogtool.common.Tuple2D;
import org.epilogtool.core.topology.RollOver;
import org.epilogtool.core.topology.Topology;
import org.epilogtool.services.TopologyService;

public class EpitheliumGrid {
	private EpitheliumCell[][] gridEpiCell;
	private Topology topology;
	private Set<LogicalModel> modelSet;

	private EpitheliumGrid(EpitheliumCell[][] gridEpiCell, Topology topology,
			Set<LogicalModel> modelSet) {
		this.gridEpiCell = gridEpiCell;
		this.topology = topology;
		this.modelSet = modelSet;
	}

	public EpitheliumGrid(int gridX, int gridY, String topologyLayout,
			RollOver rollover, LogicalModel m) throws InstantiationException,
			IllegalAccessException, IllegalArgumentException,
			InvocationTargetException, NoSuchMethodException,
			SecurityException, ClassNotFoundException {
		this.topology = TopologyService.getManager().getNewTopology(
				topologyLayout, gridX, gridY, rollover);
		this.gridEpiCell = new EpitheliumCell[gridX][gridY];
		for (int y = 0; y < gridY; y++) {
			for (int x = 0; x < gridX; x++) {
				this.gridEpiCell[x][y] = new EpitheliumCell(m);
			}
		}
		this.modelSet = new HashSet<LogicalModel>();
		this.modelSet.add(m);
	}

	public boolean hasModel(LogicalModel m) {
		return this.modelSet.contains(m);
	}

	public void updateModelSet() {
		this.modelSet = new HashSet<LogicalModel>();
		for (int y = 0; y < this.getY(); y++) {
			for (int x = 0; x < this.getX(); x++) {
				this.modelSet.add(this.gridEpiCell[x][y].getModel());
			}
		}
	}

	public Set<LogicalModel> getModelSet() {
		return Collections.unmodifiableSet(this.modelSet);
	}

	public void setRollOver(RollOver r) {
		this.topology.setRollOver(r);
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
		return new EpitheliumGrid(newGrid, newTop, newModelSet);
	}

	public EpitheliumCell cloneEpitheliumCellAt(int x, int y) {
		return this.gridEpiCell[x][y].clone();
	}

	public AbstractPerturbation getPerturbation(int x, int y) {
		return gridEpiCell[x][y].getPerturbation();
	}

	public void setPerturbation(int x, int y, AbstractPerturbation ap) {
		gridEpiCell[x][y].setPerturbation(ap);
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

	public byte[] getCellState(int x, int y) {
		return gridEpiCell[x][y].getState();
	}

	public void setCellState(int x, int y, byte[] state) {
		gridEpiCell[x][y].setState(state);
	}

	public void setCellComponentValue(int x, int y, String nodeID, byte value) {
		gridEpiCell[x][y].setValue(nodeID, value);
	}

	public LogicalModel getModel(int x, int y) {
		return gridEpiCell[x][y].getModel();
	}

	public void setModel(int x, int y, LogicalModel m) {
		gridEpiCell[x][y].setModel(m);
	}

	public List<EpitheliumCell> getNeighbours(int x, int y, int minDist,
			int maxDist) {
		List<EpitheliumCell> l = new ArrayList<EpitheliumCell>();

		for (Tuple2D<Integer> tuple : this.topology.getNeighbours(x, y,
				minDist, maxDist)) {
			l.add(gridEpiCell[tuple.getX()][tuple.getY()]);
		}
		return l;
	}

	public List<EpitheliumCell> filterNeighboursByComponent(
			List<EpitheliumCell> neighbours, String nodeID) {
		List<EpitheliumCell> filteredCells = new ArrayList<EpitheliumCell>();
		for (EpitheliumCell cell : neighbours) {
			if (cell.getNodeIndex(nodeID) > 0)
				filteredCells.add(cell);
		}
		return filteredCells;
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

	public int getNodeIndex(int x, int y, String nodeID) {
		return this.gridEpiCell[x][y].getNodeIndex(nodeID);
	}
}