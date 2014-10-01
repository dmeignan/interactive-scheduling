/**
 * Copyright 2013, Universitaet Osnabrueck
 * Author: David Meignan
 */
package de.uos.inf.ischedule.model.heuristic;

import java.util.Random;

import de.uos.inf.ischedule.model.Shift;
import de.uos.inf.ischedule.model.ShiftSchedulingProblem;
import de.uos.inf.ischedule.model.Solution;

/**
 * Procedure for applying a local-perturbation on a solution. The perturbation
 * is based on swap moves located on unsatisfied constraints. 
 * 
 * @author David Meignan
 */
public class GuidedSwapPerturbation {
	
	/**
	 * The block sizes for swap moves
	 */
	private int[] blockSizes;
	
	/**
	 * The minimum number of slots to exchange in one iteration
	 */
	private int minSlotExchanges;
	
	/**
	 * A copy of the initial solution
	 */
	private Solution origin;
	
	/**
	 * The solution on which moves have to be applied.
	 */
	private Solution solution;
	
	/**
	 * Indicate if the perturbation has been applied.
	 */
	private boolean done;
	
	/**
	 * The random number generator
	 */
	private Random rng;
	
	private int moveSelectionPolicy = SELECT_BEST_POLICY;
	private static int SELECT_FIRST_POLICY = 0;
	private static int SELECT_BEST_POLICY = 1;
	
	/**
	 * Creates an swap perturbation to be applied on solutions of the given problem.
	 * 
	 * @param problem the problem instance for which solutions have to be perturbed.
	 * @param blockSizes the block sizes for exchange moves.
	 * TODO change maxBlockSize to a perturbationStrength (ratio with total number of slots).
	 * @param strength the strength of the perturbation.
	 * @param rng the random number generator used to generate random moves.
	 * 
	 * @throws IllegalArgumentException if the strength is lower than <code>0</code>.
	 * @throws IllegalArgumentException if the number of employee is lower than <code>3<code>. 
	 * @throws NullPointerException if the problem, or the random number
	 * generator is <code>null</code>.
	 */
	public GuidedSwapPerturbation(ShiftSchedulingProblem problem, int[] blockSizes,
			double strength, Random rng) {
		if (problem == null || rng == null)
			throw new NullPointerException();
		if (problem.employees().size() < 3)
			throw new IllegalArgumentException();
		if (strength < 0.)
			throw new IllegalArgumentException();
		this.blockSizes = blockSizes;
		this.minSlotExchanges = ((int) (strength*(
				problem.employees().size()*problem.getSchedulingPeriod().size())))+1;
		this.solution = null;
		this.rng = rng;
	}
	
	/**
	 * Initializes the procedure for the given solution.
	 * 
	 * @param solution the solution on which moves have to be applied.
	 * 
	 * @throws NullPointerException if the solution is <code>null</code>.
	 */
	public void init(Solution solution) {
		if (solution == null)
			throw new NullPointerException();
		this.solution = solution;
		this.origin = new Solution(solution, true);
		done = false;
	}
	
	/**
	 * Performs the next step of the perturbation procedure. Returns <code>true</code>
	 * if the procedure has additional steps, and <code>false</code> if the procedure
	 * is finished. Further calls of this method, when it returned <code>false</code>,
	 * perturb further the solution.
	 * 
	 * @return <code>true</code> if the procedure has additional steps, and 
	 * <code>false</code> if the procedure is finished.
	 * 
	 * @throws IllegalStateException if the <code>init</code> method has not
	 * been called beforehand.
	 */
	public boolean nextStep() {
		if (solution == null)
			throw new IllegalStateException();
		
//		// TODO start debug
//		System.out.println("*** Start perturbation ***");
//		// TODO end debug
		
		int nbExchanges = 0;
		solution.invalidateEvaluation();
		while (nbExchanges < minSlotExchanges) {
			int blockSize;
			// Choose block size
			blockSize = blockSizes[rng.nextInt(blockSizes.length)];
			// Select perturbation move
			SwapMove perturbationMove = findPerturbationMove(blockSize);
			if (perturbationMove == null) {
				System.err.println("Unable to find a perturbation move.");
				done = true;
				return false;
			}
			nbExchanges += perturbationMove.getResultingDistance(solution);
			// Apply move
			perturbationMove.applyTo(solution);
		}
		
//		// TODO start debug
//		System.out.println("Nb. changes: "+nbExchanges);
//		System.out.println("*** End perturbation ***");
//		// TODO end debug

		done = true;
		return false;
	}
	
	/**
	 * Find a valid perturbation move.
	 * 
	 * @param blockSize the block size for the perturbation move.
	 * @return a perturbation move.
	 */
	private SwapMove findPerturbationMove(int blockSize) {
		SwapMove alteringMove = null;
		int[] alteringMoveSatDiff = null;
		SwapMove selectedMove = null;
		int[] selectedMoveSatDiff = null;
		
		// Create the neighborhood
		SwapMoveFilter moveFilter = new NoReturnFilter(origin, solution);
		SwapConstraintSatisfactionNeighborhood perturbationNeighborhood = new 
				SwapConstraintSatisfactionNeighborhood(solution,
						blockSize, rng, moveFilter);
		
		if (moveSelectionPolicy == SELECT_FIRST_POLICY) {
			
			// First valid move in the neighborhood
			while(selectedMove == null) {
				int[] satDiff = perturbationNeighborhood.nextNeighborEvaluation();
				if (satDiff == null) {
					// End of the neighborhood without valid move
					selectedMove = alteringMove;
					selectedMoveSatDiff = alteringMoveSatDiff;
					break;
				}
				// Check constraint satisfaction differences
				if (satDiff[0] > 0) {
					selectedMove = perturbationNeighborhood.getLastEvaluatedMove();
					selectedMoveSatDiff = satDiff;
				} else if (alteringMove == null && 
						perturbationNeighborhood.getLastEvaluatedMove()
						.getResultingDistance(solution) > 0) {
					alteringMove = perturbationNeighborhood.getLastEvaluatedMove();
					alteringMoveSatDiff = satDiff;
				}
			}
			
		} else {
			
			// Best move in the neighborhood
			int[] satDiff = perturbationNeighborhood.nextNeighborEvaluation();
			while(satDiff != null) {
				// Check constraint satisfaction differences
				if (satDiff[0] > 0) {
					if (selectedMove == null) {
						selectedMove = perturbationNeighborhood.getLastEvaluatedMove();
						selectedMoveSatDiff = satDiff;
					} else if ( (satDiff[1]-satDiff[0]) < 
							(selectedMoveSatDiff[1]-selectedMoveSatDiff[0])) {
						selectedMove = perturbationNeighborhood.getLastEvaluatedMove();
						selectedMoveSatDiff = satDiff;
					} else if ( (satDiff[1]-satDiff[0]) == 
							(selectedMoveSatDiff[1]-selectedMoveSatDiff[0])) {
						if (satDiff[0] > selectedMoveSatDiff[0]) {
							selectedMove = perturbationNeighborhood.getLastEvaluatedMove();
							selectedMoveSatDiff = satDiff;
						}
					}
				} else if (alteringMove == null && 
						perturbationNeighborhood.getLastEvaluatedMove()
						.getResultingDistance(solution) > 0) {
					alteringMove = perturbationNeighborhood.getLastEvaluatedMove();
					alteringMoveSatDiff = satDiff;
				}
				// Next neighbor
				satDiff = perturbationNeighborhood.nextNeighborEvaluation();
			}
			if (selectedMove == null) {
				selectedMove = alteringMove;
				selectedMoveSatDiff = alteringMoveSatDiff;
			}
			
		}
		
//		// TODO start debug
//		System.out.println("Perturbation, satisfied constraints: "
//				+selectedMoveSatDiff[0]+", new unsat.:"+ selectedMoveSatDiff[1] +" ("
//				+selectedMove.toString()+")");
//		// TODO end debug
		
		return selectedMove;
	}

	/**
	 * Returns <code>true</code> if the procedure is completed for the
	 * solution on which it has been initialized.
	 * 
	 * @return <code>true</code> if the procedure is completed.
	 * 
	 * @throws IllegalStateException if the <code>init</code> method has not
	 * been called beforehand.
	 */
	public boolean isDone() {
		if (solution == null)
			throw new IllegalStateException();
		return done;
	}
	
	/**
	 * Filter that avoid moves that return to the original solution.
	 */
	class NoReturnFilter extends SwapMoveFilter {

		private Solution origin;
		private Solution current;
		
		public NoReturnFilter(Solution origin, Solution current) {
			this.origin = origin;
			this.current = current;
		}
		
		/* (non-Javadoc)
		 * @see de.uos.inf.ischedule.model.heuristic.SwapMoveFilter#isSatisfied(de.uos.inf.ischedule.model.heuristic.SwapMove)
		 */
		@Override
		public boolean isSatisfied(SwapMove move) {
			boolean satisfied = true;
			for (int dayIndex=move.getStartDayIndex(); 
					dayIndex<=move.getEndDayIndex(); dayIndex++) {
				
				Shift originAssignmentEmployee1 = origin.assignments.get(dayIndex)
						.get(move.getEmployee1Index());
				Shift originAssignmentEmployee2 = origin.assignments.get(dayIndex)
						.get(move.getEmployee2Index());
				Shift currentAssignmentEmployee1 = current.assignments.get(dayIndex)
						.get(move.getEmployee1Index());
				Shift currentAssignmentEmployee2 = current.assignments.get(dayIndex)
						.get(move.getEmployee2Index());
				if (originAssignmentEmployee1 != currentAssignmentEmployee1 &&
						originAssignmentEmployee1 == currentAssignmentEmployee2) {
					satisfied = false;
					break;
				}
				if (originAssignmentEmployee2 != currentAssignmentEmployee2 &&
						originAssignmentEmployee2 == currentAssignmentEmployee1) {
					satisfied = false;
					break;
				}
				
			}
			return satisfied;
		}
		
	}

}
