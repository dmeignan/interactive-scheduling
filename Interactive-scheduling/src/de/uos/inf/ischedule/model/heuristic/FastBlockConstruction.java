/**
 * Copyright 2013, Universitaet Osnabrueck
 * Author: David Meignan
 */
package de.uos.inf.ischedule.model.heuristic;

import java.util.Random;

import de.uos.inf.ischedule.model.ShiftSchedulingProblem;
import de.uos.inf.ischedule.model.Solution;

/**
 * Generates a solution by assigning random blocks of assignments.
 * 
 * @author David Meignan
 */
public class FastBlockConstruction implements Operator {

	/**
	 * The shift scheduling problem.
	 */
	private ShiftSchedulingProblem problem;
	
	/**
	 * The random number generator.
	 */
	private Random rng;
	
	/**
	 * The current solution.
	 */
	private Solution currentSolution = null;
	
	/**
	 * The maximum block size.
	 */
	private static final int MAX_BLOCK_SIZE = 7; 
	
	/**
	 * State of the procedure:
	 */
	private boolean done = false;
	
	/**
	 * Constructs a fast greedy procedure for a shift scheduling problem. Note that
	 * modifications in the problem are not reflected on the construction procedure.
	 * If the problem changes after instantiating a fast greedy procedure, it may 
	 * result in inconsistencies and unpredictable behavior.
	 * 
	 * @param problem the shift scheduling problem.
	 * @param rnd the random number generator to be used in the generation procedure.
	 * @throws IllegalArgumentException if the problem or the random number generator
	 * is <code>null</code>.
	 */
	public FastBlockConstruction(ShiftSchedulingProblem problem, Random rnd) {
		if (problem == null || rnd == null)
			throw new IllegalArgumentException();
		this.problem = problem;
		this.rng = rnd;
	}
	
	/**
	 * Generates a solution with random assignments.
	 */
	private void generate() {
		// Initial solution
		if (currentSolution == null)
			currentSolution = new Solution(problem);
		currentSolution.invalidateEvaluation();
		
		// Draw a day index with unassigned demand 
		int unassignedSlotDayIndex = getRandomUnassignedSlotDayIndex();
		while(unassignedSlotDayIndex != -1) {
			
			// Define a block size
			int blockSize = rng.nextInt(MAX_BLOCK_SIZE)+1;
			
			// Draw an employee unassigned at the given day index
			int employeeIndex = getRandomUnassignedEmployeeIndex(unassignedSlotDayIndex);
			if (employeeIndex == -1)
				throw new IllegalStateException("No valid solution can be generated. " +
						"The demand cannot be " +
						"fullfilled with the given resources.");
			
			// Assign a block of shifts
			for (int b=0; b<blockSize; b++) {
				int dayIndex = (unassignedSlotDayIndex+b)%
						currentSolution.assignments.size();
				if (!currentSolution.unassignedSlots.get(dayIndex).isEmpty() &&
						currentSolution.assignments.get(dayIndex).get(employeeIndex) == null) {
					currentSolution.assignments.get(dayIndex).set(
							employeeIndex,
							currentSolution.unassignedSlots.get(dayIndex).remove(0));
				}
			}
			
			// Next random unassigned slot day index
			unassignedSlotDayIndex = getRandomUnassignedSlotDayIndex();
		}
	}

	/**
	 * Returns a random day index for which there remains unassigned demand in
	 * the current solution. Returns <code>-1</code> if no unassigned slot
	 * remains.
	 * 
	 * @return a random day index for which there remains unassigned demand in
	 * the current solution. Returns <code>-1</code> if no unassigned slot
	 * remains.
	 */
	private int getRandomUnassignedSlotDayIndex() {
		int dayIndex = rng.nextInt(currentSolution.unassignedSlots.size());
		if (!currentSolution.unassignedSlots.get(dayIndex).isEmpty()) {
			return dayIndex;
		}
		// Adjust to the first day for which there is unassigned demand
		int adjustedDayIndex = (dayIndex+1)%currentSolution.unassignedSlots.size();
		while(currentSolution.unassignedSlots.get(adjustedDayIndex).isEmpty()) {
			adjustedDayIndex = (adjustedDayIndex+1)%currentSolution.unassignedSlots.size();
			if (adjustedDayIndex == dayIndex)
				return -1;
		}
		return adjustedDayIndex;
	}

	/**
	 * Returns a random employee unassigned at the day index.
	 * Returns <code>-1</code> if there is no unassigned employee at the given
	 * day index.
	 * 
	 * @param dayIndex the day index.
	 * @return  a random employee unassigned at the day index.
	 * Returns <code>-1</code> if there is no unassigned employee at the given
	 * day index.
	 */
	private int getRandomUnassignedEmployeeIndex(int dayIndex) {
		int employeeIndex = rng.nextInt(currentSolution.employees.size());
		if (currentSolution.assignments.get(dayIndex).get(employeeIndex) == null) {
			return employeeIndex;
		}
		// Adjust to the first unassigned employee
		int adjustedEmployeeIndex = (employeeIndex+1)%currentSolution.employees.size();
		while(currentSolution.assignments.get(dayIndex).get(adjustedEmployeeIndex) != null) {
			adjustedEmployeeIndex = (adjustedEmployeeIndex+1)%currentSolution.employees.size();
			if (adjustedEmployeeIndex == employeeIndex)
				return -1;
		}
		return adjustedEmployeeIndex;
	}

	/* (non-Javadoc)
	 * @see de.uos.inf.ischedule.model.heuristic.Operator#init(de.uos.inf.ischedule.model.Solution[])
	 */
	@Override
	public void init(Solution... initialSolutions) {
		if (initialSolutions.length > 1)
			System.err.println("The procedure does not take into account " +
					"more than one initial solutions.");
		if (initialSolutions.length > 0)
			currentSolution = initialSolutions[0];
		else
			currentSolution = null;
		done = false;
	}

	/* (non-Javadoc)
	 * @see de.uos.inf.ischedule.model.heuristic.Operator#nextStep()
	 */
	@Override
	public boolean nextStep() {
		// Generate solution with random assignments
		generate();
		done = true;
		return false;
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
		return currentSolution;
	}

	/* (non-Javadoc)
	 * @see de.uos.inf.ischedule.model.heuristic.Operator#getResults()
	 */
	@Override
	public Solution[] getResults() {
		if (!done)
			return new Solution[]{};
		return new Solution[]{currentSolution};
	}

}
