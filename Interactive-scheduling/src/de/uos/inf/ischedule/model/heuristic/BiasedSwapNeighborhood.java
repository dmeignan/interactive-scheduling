/**
 * Copyright 2013, Universitaet Osnabrueck
 * Author: David Meignan
 */
package de.uos.inf.ischedule.model.heuristic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Random;

import de.uos.inf.ischedule.model.Constraint;
import de.uos.inf.ischedule.model.Shift;
import de.uos.inf.ischedule.model.Solution;
import de.uos.inf.ischedule.model.SolutionEvaluation;

/**
 * Swap-based neighborhood structure for which only a subset of constraints are
 * taken into account for evaluating the neighbors.
 * 
 * @author David Meignan
 */
public class BiasedSwapNeighborhood {
	
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
	 * Indicates if only improving moves are considered in the
	 * neighborhood.
	 */
	private boolean onlyImproving;
	
	/**
	 * The set of active constraints used for evaluating neighbors.
	 */
	private ArrayList<ArrayList<Constraint>> activeConstraints;
	
	/**
	 * The current neighbor evaluation.
	 */
	private SolutionEvaluation currentNeighborDeltaEvaluation = null;
	
	/**
	 * The move to the current neighbor.
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
	 * A null delta evaluation for comparison with other delta
	 * evaluations.
	 */
	private SolutionEvaluation nullDeltaEvaluation;
	
	/**
	 * Constructs a biased swap-based neighborhood from an initial solution.
	 * 
	 * @param origin the origin of the neighborhood.
	 * @param blockSize the block size for swap-moves.
	 * @param onlyImproving indicates if only improving moves are considered
	 * for the exploration.
	 * @param rng the random number generator used to randomly select next neighbors
	 * in the neighborhood.
	 * @param activeConstraints the set of constraints on which neighbors are evaluated,
	 * grouped by rank.
	 * @throws NullPointerException if the solution is <code>null</code> or the random
	 * number generator is <code>null</code>.
	 * @throws IllegalArgumentException if the block size is lower than <code>1</code>.
	 */
	public BiasedSwapNeighborhood(Solution origin, int blockSize,
			boolean onlyImproving, Random rng, 
			ArrayList<ArrayList<Constraint>> activeConstraints) {
		if (origin == null || rng == null)
			throw new NullPointerException();
		if (blockSize < 1)
			throw new IllegalArgumentException();
		if (activeConstraints == null)
			throw new IllegalArgumentException();
		this.origin = origin;
		this.origin.getEvaluation();
		this.blockSize = blockSize;
		this.onlyImproving = onlyImproving;
		this.rng = rng;
		this.activeConstraints = activeConstraints;
		// Set null delta evaluation
		int[] nullDelta = new int[activeConstraints.size()];
		Arrays.fill(nullDelta, 0);
		nullDeltaEvaluation = new SolutionEvaluation(nullDelta);
		// Initialize the list of remaining neighbors
		resetExploration();
	}

	/**
	 * Reset the exploration of the neighborhood.
	 */
	public void resetExploration() {
		currentNeighborDeltaEvaluation = null;
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
	 * Returns the partial delta evaluation of the next neighbor. Returns
	 * <code>null</code> if the neighborhood has no more neighbors.
	 * 
	 * @return the partial delta evaluation of the next neighbor.
	 */
	public SolutionEvaluation nextNeighborPartialDeltaEvaluation() {
		if (remainingMoves.isEmpty() && remainingStartDayIndexes.isEmpty()) {
			currentNeighborMove = null;
			currentNeighborDeltaEvaluation = null;
			return null;
		}
		if (remainingMoves.isEmpty()) {
			// Complete list of moves
			completeListOfMoves();
		}
		// Select next move
		currentNeighborMove = remainingMoves.remove(
				rng.nextInt(remainingMoves.size()));
		// Evaluate neighbor
		if (currentNeighborMove.modifyAssignment(origin))
			currentNeighborDeltaEvaluation = getBiasedDeltaEvaluation(currentNeighborMove);
		else
			currentNeighborDeltaEvaluation = nullDeltaEvaluation;
		// Only-improving case
		if (onlyImproving) {
			// Search improving neighbor
			while(currentNeighborDeltaEvaluation.compareTo(nullDeltaEvaluation)>=0) {
				if (remainingMoves.isEmpty() && remainingStartDayIndexes.isEmpty()) {
					currentNeighborMove = null;
					currentNeighborDeltaEvaluation = null;
					return null;
				}
				if (remainingMoves.isEmpty()) {
					// Complete list of moves
					completeListOfMoves();
				}
				// Select next move
				currentNeighborMove = remainingMoves.remove(
						rng.nextInt(remainingMoves.size()));
				// Evaluate neighbor
				if (currentNeighborMove.modifyAssignment(origin))
					currentNeighborDeltaEvaluation = 
							getBiasedDeltaEvaluation(currentNeighborMove);
				else
					currentNeighborDeltaEvaluation = nullDeltaEvaluation;
			}
		}
		
		// Return evaluation
		return currentNeighborDeltaEvaluation;
	}
	
	/**
	 * Returns the delta evaluation of a move according to the active
	 * constraints.
	 * 
	 * @param move the swap move.
	 * @return the delta evaluation of a move according to the active
	 * constraints.
	 */
	private SolutionEvaluation getBiasedDeltaEvaluation(
			SwapMove move) {
		int[] rValues = new int[activeConstraints.size()];
		Arrays.fill(rValues, 0);
		for (int rankIndex=0; rankIndex<rValues.length; rankIndex++) {
			for (Constraint constraint: activeConstraints.get(rankIndex)) {
				rValues[rankIndex] +=
						constraint.getEvaluator(origin.problem)
						.getSwapMoveCostDifference(origin, move);
			}
		}
		return new SolutionEvaluation(rValues);
	}

	/**
	 * Returns the solution of the last evaluation returned by 
	 * <code>nextNeighborPartialDeltaEvaluation</code>. This method does not
	 * modify the origin solution, nor the exploration of neighbors.
	 * 
	 * @return the solution of the last evaluation returned by 
	 * <code>nextNeighborPartialDeltaEvaluation</code>.
	 * @throws NoSuchElementException if the <code>nextNeighborPartialDeltaEvaluation</code>
	 * method has not yet been called, or has returned a <code>null</code> value.
	 */
	public Solution getLastEvaluatedNeighbor() {
		if (currentNeighborMove == null)
			throw new NoSuchElementException();
		// Copy the origin solution
		Solution moveResult = new Solution(origin, true);
		// Invalidate evaluation
		moveResult.invalidateEvaluation();
		// Apply swap on assignment
		for (int dayIndex=currentNeighborMove.startDayIndex; 
				dayIndex<=currentNeighborMove.getEndDayIndex(); dayIndex++) {
			Shift initialAssignmentEmployee1 = moveResult.assignments.get(dayIndex)
					.get(currentNeighborMove.employee1Index);
			Shift initialAssignmentEmployee2 = moveResult.assignments.get(dayIndex)
					.get(currentNeighborMove.employee2Index);
			moveResult.assignments.get(dayIndex)
					.set(currentNeighborMove.employee1Index, initialAssignmentEmployee2);
			moveResult.assignments.get(dayIndex)
					.set(currentNeighborMove.employee2Index, initialAssignmentEmployee1);
		}
		return moveResult;
	}
	
	/**
	 * Moves the origin of the neighborhood to the last evaluated neighbor, by 
	 * applying the last swap-move to the origin solution. The exploration
	 * of neighbors is re-initialized with the new origin solution.
	 * 
	 * @throws NoSuchElementException if the <code>nextNeighborPartialDeltaEvaluation</code>
	 * method has not yet been called, or has returned a <code>null</code> value.
	 */
	public void moveToLastEvaluatedNeighbor() {
		if (currentNeighborMove == null)
			throw new NoSuchElementException();
		// Apply swap on assignment
		for (int dayIndex=currentNeighborMove.startDayIndex; 
				dayIndex<=currentNeighborMove.getEndDayIndex(); dayIndex++) {
			Shift initialAssignmentEmployee1 = origin.assignments.get(dayIndex)
					.get(currentNeighborMove.employee1Index);
			Shift initialAssignmentEmployee2 = origin.assignments.get(dayIndex)
					.get(currentNeighborMove.employee2Index);
			origin.assignments.get(dayIndex)
					.set(currentNeighborMove.employee1Index, initialAssignmentEmployee2);
			origin.assignments.get(dayIndex)
					.set(currentNeighborMove.employee2Index, initialAssignmentEmployee1);
		}
		
		// This must be removed after debugging !!!
//		// TODO start debug
//		// Re-compute evaluation
//		SolutionEvaluation originSolutionEvaluation = origin.getEvaluation();
//		SolutionEvaluation movePartialEvaluation = currentNeighborEvaluation;
//		origin.invalidateEvaluation();
//		SolutionEvaluation moveReEvaluation = origin.getEvaluation();
//		if (movePartialEvaluation.compareTo(moveReEvaluation) != 0) {
//			System.err.println("Error in partial evaluation of a move!");
//			// Reset move
//			for (int dayIndex=currentNeighborMove.startDayIndex; 
//					dayIndex<=currentNeighborMove.getEndDayIndex(); dayIndex++) {
//				Shift initialAssignmentEmployee1 = origin.assignments.get(dayIndex)
//						.get(currentNeighborMove.employee1Index);
//				Shift initialAssignmentEmployee2 = origin.assignments.get(dayIndex)
//						.get(currentNeighborMove.employee2Index);
//				origin.assignments.get(dayIndex)
//						.set(currentNeighborMove.employee1Index, initialAssignmentEmployee2);
//				origin.assignments.get(dayIndex)
//						.set(currentNeighborMove.employee2Index, initialAssignmentEmployee1);
//			}
//			// Print initial solution details
//			System.out.println("*** Initial solution ***");
//			System.out.println("Evaluation: "+originSolutionEvaluation);
//			java.util.ArrayList<de.uos.inf.ischedule.model.ConstraintViolation> constraintViolations =
//					origin.getConstraintViolations();
//			for (de.uos.inf.ischedule.model.ConstraintViolation violation: constraintViolations) {
//				System.out.println("\t"+violation.getMessage()+
//						", cost: "+violation.getCost()+
//						", scope: "+violation.getConstraintViolationScopeDescription());
//			}
//			// Re-apply move
//			for (int dayIndex=currentNeighborMove.startDayIndex; 
//					dayIndex<=currentNeighborMove.getEndDayIndex(); dayIndex++) {
//				Shift initialAssignmentEmployee1 = origin.assignments.get(dayIndex)
//						.get(currentNeighborMove.employee1Index);
//				Shift initialAssignmentEmployee2 = origin.assignments.get(dayIndex)
//						.get(currentNeighborMove.employee2Index);
//				origin.assignments.get(dayIndex)
//						.set(currentNeighborMove.employee1Index, initialAssignmentEmployee2);
//				origin.assignments.get(dayIndex)
//						.set(currentNeighborMove.employee2Index, initialAssignmentEmployee1);
//			}
//			// Print move details
//			System.out.println("*** Solution after move ***");
//			System.out.println("Move: "+currentNeighborMove);
//			System.out.println("Partial (wrong) evaluation: "+movePartialEvaluation);
//			System.out.println("Re-evaluation: "+moveReEvaluation);
//			constraintViolations =
//					origin.getConstraintViolations();
//			for (de.uos.inf.ischedule.model.ConstraintViolation violation: constraintViolations) {
//				System.out.println("\t"+violation.getMessage()+
//						", cost: "+violation.getCost()+
//						", scope: "+violation.getConstraintViolationScopeDescription());
//			}
//			throw new IllegalArgumentException();
//		}	
//		// TODO end debug
		
		// Invalidate evaluation
		origin.invalidateEvaluation();
		// Re-initialize neighborhood exploration
		resetExploration();
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
