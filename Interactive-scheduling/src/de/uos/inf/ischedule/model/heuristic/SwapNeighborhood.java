/**
 * Copyright 2013, Universitaet Osnabrueck
 * Author: David Meignan
 */
package de.uos.inf.ischedule.model.heuristic;

import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Random;

import de.uos.inf.ischedule.model.Shift;
import de.uos.inf.ischedule.model.Solution;
import de.uos.inf.ischedule.model.SolutionEvaluation;

/**
 * Swap-based neighborhood structure. This class provides a mean
 * to explore the swap-neighborhood of a solution (the origin).
 * Exploration is somewhat similar to an Iterator. The <code>nextNeighborEvaluation</code>
 * method returns the next neighbor's evaluation. The  <code>moveToLastEvaluatedNeighbor</code>
 * method move to the last evaluated neighbor, and <code>getLastEvaluatedNeighbor</code>
 * returns the solution of the last evaluated move.
 * 
 * @author David Meignan
 */
public class SwapNeighborhood {
	
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
	 * The best found neighbor evaluation.
	 */
	private SolutionEvaluation bestNeighborEvaluation = null;
	
	/**
	 * The move of the best found evaluation.
	 */
	private SwapMove bestNeighborMove = null;
	
	/**
	 * Indicates if remaining moves have been explored to set
	 * the best neighbor.
	 */
	private boolean bestNeighborChecked = false;
	
	/**
	 * The current neighbor evaluation.
	 */
	private SolutionEvaluation currentNeighborEvaluation = null;
	
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
	 * @param onlyImproving indicates if only improving moves are considered
	 * for the exploration.
	 * @param rng the random number generator used to randomly select next neighbors
	 * in the neighborhood. If no random number generator is provided, neighbors are
	 * returned in order.
	 * @throws NullPointerException if the solution is <code>null</code>.
	 * @throws IllegalArgumentException if the block size is lower than <code>1</code>.
	 */
	public SwapNeighborhood(Solution origin, int blockSize,
			boolean onlyImproving, Random rng) {
		if (origin == null)
			throw new NullPointerException();
		if (blockSize < 1)
			throw new IllegalArgumentException();
		this.origin = origin;
		this.origin.getEvaluation();
		this.blockSize = blockSize;
		this.onlyImproving = onlyImproving;
		this.rng = rng;
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
	 * Returns the evaluation of the best solution in the neighborhood. This method does
	 * not perturb the iterative exploration of neighbors, and does not
	 * impact the origin solution.
	 * Returns <code>null</code> if the neighborhood is empty, or if only
	 * improving neighbors are considered and no neighbor has a better
	 * evaluation than the origin solution.
	 * 
	 * @return the evaluation of the best solution in the neighborhood.
	 */
	public SolutionEvaluation getBestNeighborEvaluation() {
		if (bestNeighborChecked) {
			return bestNeighborEvaluation;
		}
		// Check remaining moves
		for (SwapMove move: remainingMoves) {
			updateBestNeighbor(move, null);
		}
		for (int startDayIndex: remainingStartDayIndexes) {
			for (int employee1=0; 
					employee1<origin.employees.size(); employee1++) {
				for (int employee2=employee1+1; 
						employee2<origin.employees.size(); employee2++) {
					updateBestNeighbor(new SwapMove(
							employee1,
							employee2,
							startDayIndex,
							blockSize
							), null);
				}
			}
		}
		bestNeighborChecked = true;
		return bestNeighborEvaluation;
	}
	
	/**
	 * Update the best neighbor with the given move. If the evaluation of
	 * the move is provided, the move is not evaluated again.
	 * 
	 * @param move the move to compare with the best move.
	 * @param evaluation the evaluation of the move.
	 */
	private void updateBestNeighbor(SwapMove move, SolutionEvaluation evaluation) {
		if (evaluation == null) {
			if (move.modifyAssignment(origin))
				evaluation = move.evaluate(origin);
			else
				evaluation = origin.getEvaluation();
		}
		if (bestNeighborMove == null) {
			 if (!onlyImproving) {
				 bestNeighborMove = move;
				 bestNeighborEvaluation = evaluation;
			 } else if (evaluation.compareTo(origin.evaluation) < 0) {
				 bestNeighborMove = move;
				 bestNeighborEvaluation = evaluation;
			 }
		} else if (evaluation.compareTo(bestNeighborEvaluation) < 0) {
			 bestNeighborMove = move;
			 bestNeighborEvaluation = evaluation;
		}
	}
	
	/**
	 * Returns the best solution in the neighborhood. This method does
	 * not perturb the iterative exploration of neighbors, and does not
	 * impact the origin solution.
	 * Returns <code>null</code> if the neighborhood is empty, or if only
	 * improving neighbors are considered and no neighbor has a better
	 * evaluation than the origin solution.
	 * 
	 * @return the best solution in the neighborhood.
	 */
	public Solution getBestNeighbor() {
		if (getBestNeighborEvaluation() == null)
			return null;
		// Copy the origin solution
		Solution bestMoveResult = new Solution(origin, true);
		// Modify evaluation
		bestMoveResult.evaluation = bestNeighborEvaluation;
		bestMoveResult.evaluated = true;
		bestMoveResult.constraintViolations = null;
		// Apply swap on assignment
		for (int dayIndex=bestNeighborMove.startDayIndex; 
				dayIndex<=bestNeighborMove.getEndDayIndex(); dayIndex++) {
			Shift initialAssignmentEmployee1 = bestMoveResult.assignments.get(dayIndex)
					.get(bestNeighborMove.employee1Index);
			Shift initialAssignmentEmployee2 = bestMoveResult.assignments.get(dayIndex)
					.get(bestNeighborMove.employee2Index);
			bestMoveResult.assignments.get(dayIndex)
					.set(bestNeighborMove.employee1Index, initialAssignmentEmployee2);
			bestMoveResult.assignments.get(dayIndex)
					.set(bestNeighborMove.employee2Index, initialAssignmentEmployee1);
		}
		return bestMoveResult;
	}
	
	/**
	 * Moves the origin of the neighborhood to the best neighbor by 
	 * applying the best swap-move to the origin solution. The exploration
	 * of neighbors is re-initialized with the new origin solution.
	 * 
	 * @return <code>true</code> if the origin solution has changed as a result
	 * of the call. Returns <code>false</code> if the neighborhood is empty, or if only
	 * improving neighbors are considered and no neighbor has a better
	 * evaluation than the origin solution.
	 */
	public boolean moveToBestNeighbor() {
		if (getBestNeighborEvaluation() == null)
			return false;
		// Apply swap on assignment
		for (int dayIndex=bestNeighborMove.startDayIndex; 
				dayIndex<=bestNeighborMove.getEndDayIndex(); dayIndex++) {
			Shift initialAssignmentEmployee1 = origin.assignments.get(dayIndex)
					.get(bestNeighborMove.employee1Index);
			Shift initialAssignmentEmployee2 = origin.assignments.get(dayIndex)
					.get(bestNeighborMove.employee2Index);
			origin.assignments.get(dayIndex)
					.set(bestNeighborMove.employee1Index, initialAssignmentEmployee2);
			origin.assignments.get(dayIndex)
					.set(bestNeighborMove.employee2Index, initialAssignmentEmployee1);
		}
		
		// This must be removed after debugging !!!
//		// TODO start debug
//		// Re-compute evaluation
//		SolutionEvaluation originSolutionEvaluation = origin.getEvaluation();
//		SolutionEvaluation movePartialEvaluation = bestNeighborEvaluation;
//		origin.invalidateEvaluation();
//		SolutionEvaluation moveReEvaluation = origin.getEvaluation();
//		if (movePartialEvaluation.compareTo(moveReEvaluation) != 0) {
//			System.err.println("Error in partial evaluation of a move!");
//			// Reset move
//			for (int dayIndex=bestNeighborMove.startDayIndex; 
//					dayIndex<=bestNeighborMove.getEndDayIndex(); dayIndex++) {
//				Shift initialAssignmentEmployee1 = origin.assignments.get(dayIndex)
//						.get(bestNeighborMove.employee1Index);
//				Shift initialAssignmentEmployee2 = origin.assignments.get(dayIndex)
//						.get(bestNeighborMove.employee2Index);
//				origin.assignments.get(dayIndex)
//						.set(bestNeighborMove.employee1Index, initialAssignmentEmployee2);
//				origin.assignments.get(dayIndex)
//						.set(bestNeighborMove.employee2Index, initialAssignmentEmployee1);
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
//			for (int dayIndex=bestNeighborMove.startDayIndex; 
//					dayIndex<=bestNeighborMove.getEndDayIndex(); dayIndex++) {
//				Shift initialAssignmentEmployee1 = origin.assignments.get(dayIndex)
//						.get(bestNeighborMove.employee1Index);
//				Shift initialAssignmentEmployee2 = origin.assignments.get(dayIndex)
//						.get(bestNeighborMove.employee2Index);
//				origin.assignments.get(dayIndex)
//						.set(bestNeighborMove.employee1Index, initialAssignmentEmployee2);
//				origin.assignments.get(dayIndex)
//						.set(bestNeighborMove.employee2Index, initialAssignmentEmployee1);
//			}
//			// Print move details
//			System.out.println("*** Solution after move ***");
//			System.out.println("Move: "+bestNeighborMove);
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
		
		// Modify evaluation
		origin.evaluation = bestNeighborEvaluation;
		origin.evaluated = true;
		origin.constraintViolations = null;
		// Re-initialize neighborhood exploration
		resetExploration();
		bestNeighborMove = null;
		bestNeighborEvaluation = null;
		bestNeighborChecked = false;
		return true;
	}
	
	/**
	 * Returns the next neighbor's evaluation in the neighborhood. Returns
	 * <code>null</code> if the neighborhood has no more neighbors.
	 * 
	 * @return the next neighbor's evaluation in the neighborhood.
	 */
	public SolutionEvaluation nextNeighborEvaluation() {
		if (remainingMoves.isEmpty() && remainingStartDayIndexes.isEmpty()) {
			currentNeighborMove = null;
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
		// Evaluate neighbor
		if (currentNeighborMove.modifyAssignment(origin))
			currentNeighborEvaluation = currentNeighborMove.evaluate(origin);
		else
			currentNeighborEvaluation = origin.getEvaluation();
		// Only-improving case
		if (onlyImproving) {
			// Search improving neighbor
			while(currentNeighborEvaluation.compareTo(origin.evaluation)>=0) {
				if (remainingMoves.isEmpty() && remainingStartDayIndexes.isEmpty()) {
					currentNeighborMove = null;
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
				// Evaluate neighbor
				if (currentNeighborMove.modifyAssignment(origin))
					currentNeighborEvaluation = currentNeighborMove.evaluate(origin);
				else
					currentNeighborEvaluation = origin.getEvaluation();
			}
		}
		// Update best neighbor
		if (!bestNeighborChecked) {
			updateBestNeighbor(currentNeighborMove, currentNeighborEvaluation);
		}
		// Return evaluation
		return currentNeighborEvaluation;
	}
	
	/**
	 * Returns the solution of the last evaluation returned by 
	 * <code>nextNeighborEvaluation</code>. This method does not
	 * modify the origin solution, nor the exploration of neighbors.
	 * 
	 * @return the solution of the last evaluation returned by 
	 * <code>nextNeighborEvaluation</code>.
	 * @throws NoSuchElementException if the <code>nextNeighborEvaluation</code>
	 * method has not yet been called, or has returned a <code>null</code> value.
	 */
	public Solution getLastEvaluatedNeighbor() {
		if (currentNeighborMove == null)
			throw new NoSuchElementException();
		// Copy the origin solution
		Solution moveResult = new Solution(origin, true);
		// Modify evaluation
		moveResult.evaluation = currentNeighborEvaluation;
		moveResult.evaluated = true;
		moveResult.constraintViolations = null;
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
	 * @throws NoSuchElementException if the <code>nextNeighborEvaluation</code>
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
		
		// Modify evaluation
		origin.evaluation = currentNeighborEvaluation;
		origin.evaluated = true;
		origin.constraintViolations = null;
		// Re-initialize neighborhood exploration
		resetExploration();
		bestNeighborMove = null;
		bestNeighborEvaluation = null;
		bestNeighborChecked = false;
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
