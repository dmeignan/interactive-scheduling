/**
 * Copyright 2013, Universitaet Osnabrueck
 * Author: David Meignan
 */
package de.uos.inf.ischedule.model.heuristic;

import java.util.ArrayList;
import java.util.Random;

import de.uos.inf.ischedule.model.Constraint;
import de.uos.inf.ischedule.model.ShiftSchedulingProblem;
import de.uos.inf.ischedule.model.Solution;

/**
 * Variable neighborhood descent procedure based on biased swap neighborhood.
 * 
 * @author David Meignan
 */
public class BiasedVariableNeighborhoodDescent {

	/**
	 * The neighborhood used by the VND procedure.
	 */
	private BiasedSwapNeighborhood currentNeighborhood;
	
	/**
	 * The block sizes used for neighborhoods.
	 */
	private int[] swapBlockSizes;
	
	/**
	 * The current block size index.
	 */
	private int currentBlockSizeIndex;
	
	/**
	 * The random number generator used for the exploration of neighborhoods.
	 */
	private Random rng;
	
	/**
	 * The set of active constraints used for evaluating neighbors.
	 */
	private ArrayList<ArrayList<Constraint>> activeConstraints;
	
	/**
	 * Creates a new swap VND procedure for improving solution to a given problem.
	 * 
	 * @param problem the problem instance for which solutions have to be improved.
	 * @param swapBlockSizes the block-sizes to be used for swap-neighborhood.
	 * Sizes are taken in order.
	 * @param rng the random number generator for the exploration of
	 * neighborhoods.
	 * @param activeConstraints the set of constraints on which moves are evaluated,
	 * grouped by rank.
	 * 
	 * @throws NullPointerException if the problem, block sizes, or the
	 * active constraints set is <code>null</code>.
	 * @throws IllegalArgumentException if one of the block-size is lower than
	 * 1.
	 */
	public BiasedVariableNeighborhoodDescent(ShiftSchedulingProblem problem, 
			int[] swapBlockSizes,
			Random rng, 
			ArrayList<ArrayList<Constraint>> activeConstraints) {
		if (problem == null || swapBlockSizes == null ||
				activeConstraints == null || swapBlockSizes.length == 0)
			throw new NullPointerException();
		for (int size: swapBlockSizes) {
			if (size < 1)
				throw new IllegalArgumentException();
		}
		this.swapBlockSizes = swapBlockSizes;
		this.activeConstraints = activeConstraints;
		this.rng = rng;
		currentNeighborhood = null;
	}
	
	/**
	 * Initializes the procedure for improving the given solution.
	 * 
	 * @param solution the solution to be improved. Note that the solution
	 * is modified by the VND procedure.
	 * 
	 * @throws NullPointerException if the solution is <code>null</code>.
	 */
	public void init(Solution solution) {
		currentBlockSizeIndex = 0;
		currentNeighborhood = new BiasedSwapNeighborhood(
				solution,
				swapBlockSizes[currentBlockSizeIndex],
				true, rng, activeConstraints);
	}
	
	/**
	 * Performs the next step of the VND. Returns <code>true</code> if the procedure
	 * has additional steps, and <code>false</code> if the VND procedure is finished.
	 * 
	 * @return <code>true</code> if the procedure has additional steps, and 
	 * <code>false</code> if the VND procedure is finished.
	 * 
	 * @throws IllegalStateException if the <code>init</code> method has not
	 * been called beforehand.
	 */
	public boolean nextStep() {
		if (currentNeighborhood == null)
			throw new IllegalStateException();
		// Check if already finish
		if (currentBlockSizeIndex >= swapBlockSizes.length)
			return false;
		// Find improving
		if (currentNeighborhood.nextNeighborPartialDeltaEvaluation() != null) {
			currentNeighborhood.moveToLastEvaluatedNeighbor();
			if (currentBlockSizeIndex != 0) {
				// Return to first block size
				currentBlockSizeIndex = 0;
				currentNeighborhood = new BiasedSwapNeighborhood(
						currentNeighborhood.getOrigin(),
						swapBlockSizes[currentBlockSizeIndex],
						true, rng, activeConstraints);
			}
			return true;
		} else {
			// Next block size
			currentBlockSizeIndex++;
			if (currentBlockSizeIndex<swapBlockSizes.length) {
				currentNeighborhood = new BiasedSwapNeighborhood(
						currentNeighborhood.getOrigin(),
						swapBlockSizes[currentBlockSizeIndex],
						true, rng, activeConstraints);
				return true;
			} else {
				return false;
			}
		}
	}
	
	/**
	 * Returns <code>true</code> if the VND procedure is completed for the
	 * solution on which it has been initialized.
	 * 
	 * @return <code>true</code> if the VND procedure is completed.
	 * 
	 * @throws IllegalStateException if the <code>init</code> method has not
	 * been called beforehand.
	 */
	public boolean isDone() {
		if (currentNeighborhood == null)
			throw new IllegalStateException();
		return (currentBlockSizeIndex >= swapBlockSizes.length);
	}
	
}
