/**
 * Copyright 2013, Universitaet Osnabrueck
 * Author: David Meignan
 */
package de.uos.inf.ischedule.model.heuristic;

import java.util.LinkedList;
import java.util.Random;

import de.uos.inf.ischedule.model.Constraint;
import de.uos.inf.ischedule.model.Solution;

/**
 * Swap-based neighborhood structure. This class provides a mean
 * to explore the swap-neighborhood of a solution (the origin) according
 * to the change in the number of constraints satisfied and unsatisfied.
 * 
 * @author David Meignan
 */
public class SwapConstraintSatisfactionNeighborhood {
	
	/**
	 * The solution at the origin of the neighborhood.
	 */
	private Solution origin;
	
	/**
	 * The block size for swap moves.
	 */
	private int blockSize;
	
	/**
	 * The random number generator used to select next neighbors.
	 */
	private Random rng;
	
	/**
	 * Filter for moves to be considered in the neighborhood.
	 */
	private SwapMoveFilter moveFilter;
	
	/**
	 * The current neighbor evaluation.
	 */
	private int[] currentNeighborEvaluation = null;
	
	/**
	 * The move of the current neighbor.
	 */
	private SwapMove currentNeighborMove = null;
	
	/**
	 * The list of remaining move to explore in the neighborhood of
	 * the origin solution.
	 * A empty list corresponds to the end of neighbors.
	 */
	private LinkedList<SwapMove> remainingMoves = null;
	private LinkedList<Integer> remainingStartDayIndexes = null;
	
	/**
	 * Constructs a swap-based neighborhood from an initial solution.
	 * 
	 * @param origin the origin of the neighborhood.
	 * @param blockSize the block size for swap-moves.
	 * @param rng the random number generator used to randomly select next neighbors
	 * in the neighborhood. If no random number generator is provided, neighbors are
	 * returned in order.
	 * @throws NullPointerException if the solution is <code>null</code>.
	 * @throws IllegalArgumentException if the block size is lower than <code>1</code>.
	 */
	public SwapConstraintSatisfactionNeighborhood(Solution origin, int blockSize,
			Random rng, SwapMoveFilter moveFilter) {
		if (origin == null)
			throw new NullPointerException();
		if (blockSize < 1)
			throw new IllegalArgumentException();
		this.origin = origin;
		this.blockSize = blockSize;
		this.rng = rng;
		this.moveFilter = moveFilter;
		// Initialize the list of remaining neighbors
		resetExploration();
	}

	/**
	 * Reset the exploration of the neighborhood.
	 */
	public void resetExploration() {
		currentNeighborEvaluation = null;
		currentNeighborMove = null;
		remainingMoves = null;
		remainingStartDayIndexes = null;
		// Initialize the list of moves and days
		remainingMoves = new LinkedList<SwapMove>();
		remainingStartDayIndexes = new LinkedList<Integer>();
		for (int dayIndex=0; dayIndex+blockSize<=origin.assignments.size();
				dayIndex++) {
			remainingStartDayIndexes.add(dayIndex);
		}
	}
	
	/**
	 * Completes the list of moves.
	 */
	private void completeListOfMoves() {
		if (remainingMoves.isEmpty() && !remainingStartDayIndexes.isEmpty()) {
			// Generate list of moves for the next day
			int startDayIndex = -1;
			if (rng == null)
				startDayIndex = remainingStartDayIndexes.removeFirst();
			else
				startDayIndex = remainingStartDayIndexes.remove(
						rng.nextInt(remainingStartDayIndexes.size()));
			for (int employee1=0; 
					employee1<origin.employees.size(); employee1++) {
				for (int employee2=employee1+1; 
						employee2<origin.employees.size(); employee2++) {
					remainingMoves.add(new SwapMove(
							employee1,
							employee2,
							startDayIndex,
							blockSize
							));
				}
			}
		}
	}

	/**
	 * Returns the next neighbor's evaluation in the neighborhood. Returns
	 * <code>null</code> if the neighborhood has no more neighbors.
	 * 
	 * @return the next neighbor's evaluation in the neighborhood.
	 */
	public int[] nextNeighborEvaluation() {
		currentNeighborMove = null;
		while (currentNeighborMove == null ||
				!moveFilter.isSatisfied(currentNeighborMove)) {
			if (remainingMoves.isEmpty() && remainingStartDayIndexes.isEmpty()) {
				currentNeighborEvaluation = null;
				return null;
			}
			if (remainingMoves.isEmpty()) {
				// Complete list of moves
				completeListOfMoves();
			}
			// Select next move
			if (rng != null)
				currentNeighborMove = remainingMoves.remove(
						rng.nextInt(remainingMoves.size()));
			else
				currentNeighborMove = remainingMoves.removeFirst();
		}
		// Evaluate neighbor
		if (!currentNeighborMove.modifyAssignment(origin))
			currentNeighborEvaluation = new int[]{0, 0};
		else
			currentNeighborEvaluation = getConstraintSatisfactionDifference(
					currentNeighborMove);
		// Return evaluation
		return currentNeighborEvaluation;
	}
	
	/**
	 * Returns the difference in terms of constraints satisfaction the move
	 * induce.
	 * 
	 * @param move the move evaluated.
	 * @return the difference in terms of constraints satisfaction the move
	 * induce.
	 */
	private int[] getConstraintSatisfactionDifference(SwapMove move) {
		int sumSatDiff[] = new int[]{0, 0};
		int maxRank = origin.problem.getMaxConstraintsRankIndex();
		for (int rankIndex=0; rankIndex<=maxRank; rankIndex++) {
			for (Constraint constraint: origin.problem.constraints(rankIndex)) {
				int[] satDiff =
						constraint.getEvaluator(origin.problem)
						.getConstraintSatisfactionDifference(origin, move);
				sumSatDiff[0] += satDiff[0];
				sumSatDiff[1] += satDiff[1];
			}
		}
		return sumSatDiff;
	}
	
	/**
	 * Returns the last move evaluated.
	 * 
	 * @return the last move evaluated.
	 */
	public SwapMove getLastEvaluatedMove() {
		return currentNeighborMove;
	}
	
	/**
	 * Returns the solution at the origin of the neighborhood.
	 * 
	 * @return the solution at the origin of the neighborhood.
	 */
	public Solution getOrigin() {
		return origin;
	}

	/**
	 * Return the block size used for swap-moves.
	 * 
	 * @return the block size used for swap-moves.
	 */
	public int getBlockSize() {
		return blockSize;
	}
	
}
