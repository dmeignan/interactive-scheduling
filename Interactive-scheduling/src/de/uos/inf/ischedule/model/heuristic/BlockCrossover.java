/**
 * Copyright 2013, Universitaet Osnabrueck
 * Author: David Meignan
 */
package de.uos.inf.ischedule.model.heuristic;

import java.util.Arrays;
import java.util.Random;

import de.uos.inf.ischedule.model.ShiftSchedulingProblem;
import de.uos.inf.ischedule.model.Solution;

/**
 * Generates a solution by assigning random blocks of assignments.
 * 
 * @author David Meignan
 */
public class BlockCrossover implements Operator {

	/**
	 * The random number generator.
	 */
	private Random rng;
	
	/**
	 * The current solution.
	 */
	private Solution initialSolution1 = null;
	private Solution initialSolution2 = null;
	private Solution crossSolution = null;
	
	/**
	 * The block sizes.
	 */
	private int[] blockSizes; 
	
	/**
	 * State of the procedure:
	 */
	private boolean done = false;
	
	/**
	 * Constructs a crossover procedure.
	 * 
	 * @param problem the shift scheduling problem.
	 * @param rnd the random number generator to be used in the crossover procedure.
	 * @throws IllegalArgumentException if the problem, the random number generator,
	 * or block sizes are <code>null</code>.
	 */
	public BlockCrossover(ShiftSchedulingProblem problem, Random rnd, 
			int[] blockSizes) {
		if (problem == null || rnd == null || blockSizes == null)
			throw new IllegalArgumentException();
		this.rng = rnd;
		this.blockSizes = Arrays.copyOf(blockSizes, blockSizes.length);
	}
	
	/* (non-Javadoc)
	 * @see de.uos.inf.ischedule.model.heuristic.Operator#init(de.uos.inf.ischedule.model.Solution[])
	 */
	@Override
	public void init(Solution... initialSolutions) {
		if (initialSolutions.length != 2)
			System.err.println("The procedure only accepts two solutions.");
		initialSolution1 = initialSolutions[0];
		initialSolution2 = initialSolutions[1];
		crossSolution = null;
		done = false;
	}

	/* (non-Javadoc)
	 * @see de.uos.inf.ischedule.model.heuristic.Operator#nextStep()
	 */
	@Override
	public boolean nextStep() {
		// Generate cross solution
		crossSolution = cross(initialSolution1, initialSolution2);
		done = true;
		return false;
	}

	/**
	 * Crosses two solutions.
	 */
	private Solution cross(Solution s1, Solution s2) {
		Solution r = new Solution(s1, true);
		r.invalidateEvaluation();
		boolean blockToCopy = false;
		int startDayIndex = 0;
		while(startDayIndex<r.assignments.size()) {
			// Block size
			int blockSize = blockSizes[rng.nextInt(blockSizes.length)];
			// Copy block
			if (blockToCopy) {
				for (int dayIndex = startDayIndex; dayIndex<r.assignments.size() &&
						dayIndex<startDayIndex+blockSize; dayIndex++) {
					for (int employeeIndex=0; employeeIndex<r.employees.size();
							employeeIndex++) {
						r.assignments.get(dayIndex).set(employeeIndex, 
								s2.assignments.get(dayIndex).get(employeeIndex));
					}
				}
			}
			// Next block
			startDayIndex += blockSize;
			blockToCopy = !blockToCopy;
		}
		return r;
	}

	/* (non-Javadoc)
	 * @see de.uos.inf.ischedule.model.heuristic.Operator#isDone()
	 */
	@Override
	public boolean isDone() {
		return done;
	}

	/* (non-Javadoc)
	 * @see de.uos.inf.ischedule.model.heuristic.Operator#getResult()
	 */
	@Override
	public Solution getResult() {
		if (!done)
			return null;
		return crossSolution;
	}

	/* (non-Javadoc)
	 * @see de.uos.inf.ischedule.model.heuristic.Operator#getResults()
	 */
	@Override
	public Solution[] getResults() {
		if (!done)
			return new Solution[]{};
		return new Solution[]{crossSolution};
	}

}
