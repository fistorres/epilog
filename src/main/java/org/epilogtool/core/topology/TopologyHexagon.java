package org.epilogtool.core.topology;

import java.util.HashSet;
import java.util.Set;

import org.epilogtool.common.Tuple2D;

public abstract class TopologyHexagon extends Topology {

	protected final double SQRT3 = Math.sqrt(3);
	protected final double SQRT3_2 = SQRT3 / 2;

	@Override
	public Set<Tuple2D<Integer>> getPositionNeighbours(int x, int y, Set<Tuple2D<Integer>> setRelativeNeighbours) {

		Set < Tuple2D<Integer>> setNeighbours = new HashSet<Tuple2D<Integer>>();
		
		for (Tuple2D<Integer> tuple : setRelativeNeighbours){
			Tuple2D<Integer> posTuple = this.relativeToAbsolutePosition(tuple, x, y);
			if (this.includesNeighbour(posTuple)){
				this.includeNeighbour(posTuple);
				setNeighbours.add(posTuple);
			}
		}
		return setNeighbours;
	}
	
	
	protected Tuple2D<Integer> relativeToAbsolutePosition(Tuple2D<Integer> tuple, int x, int y){
		int newX = tuple.getX() + x;
		int newY = tuple.getY() + y;
		return new Tuple2D<Integer>(newX, newY);
	}

	protected boolean includesNeighbour(Tuple2D<Integer> posTuple) {
		int x = posTuple.getX();
		int y = posTuple.getY();
		
		if (this.rollover != RollOver.VERTICAL & (y < 0 || y >= this.maxY))
			return false;
		if (this.rollover != RollOver.HORIZONTAL & (x < 0 || x >= this.maxX))
			return false;
		return true;
	}
	
	protected void includeNeighbour(Tuple2D<Integer> posTuple) {
		int x = posTuple.getX();
		int y = posTuple.getY();

		if (this.rollover == RollOver.VERTICAL) {
			if (y < 0)
				y = (this.maxY + y);
			else if (y >= this.maxY)
				y = y - this.maxY;
		} else if (this.rollover == RollOver.HORIZONTAL) {
			if (x < 0)
				x = (this.maxX + x);
			else if (x >= this.maxX)
				x = x - this.maxX;
		}

		posTuple.setX(x);
		posTuple.setY(y);
	}
	
	public Set<Tuple2D<Integer>> getRelativeNeighbours(boolean even, int minDist, int maxDist) {
		
		int epiMaxDiagonal = (int) Math.ceil(Math.sqrt(Math.pow(this.maxX/2, 2) + 
				   Math.pow(this.maxY/2, 2)));

		maxDist = Math.min(maxDist, epiMaxDiagonal);
		
		Set<Tuple2D<Integer>> setRelativeNeighbours = new HashSet<Tuple2D<Integer>>();
		
		if (even) { 
			for (int i = minDist; i <= maxDist; i ++) {
				setRelativeNeighbours.addAll(this.evenRelativeNeighboursAt(i));
			}
		}
		else {
			for (int i = minDist; i<= maxDist; i++) {
				setRelativeNeighbours.addAll(this.oddRelativeNeighboursAt(i));
			}
		}
		return setRelativeNeighbours;
	}
	
	public abstract Set<Tuple2D<Integer>> evenRelativeNeighboursAt(int distance);
	
	public abstract Set<Tuple2D<Integer>> oddRelativeNeighboursAt(int distance);
	
	public abstract boolean isEven(int x, int y);
	
}

