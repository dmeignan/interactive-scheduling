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
 * Ruin and recreate procedure that applies a perturbation on a given solution.
 * Randomly selected assignments are removed and then re-introduced using a 
 * greedy procedure.
 * 
 * @author David Meignan
 */
public class RuinAndRecreateProcedure {

	/**
	 * The maximum block size for exchange moves
	 */
	private int maxBlockSize;
	
	/**
	 * The minimum number of slots to exchange in one iteration
	 */
	private int minSlotRemovals;
	
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
	
	/**
	 * The reconstruction procedure.
	 */
	private GreedyConstruction reconstructionProcedure;
	
	/**
	 * Creates a ruin-and-recreate procedure to be applied on solutions of the given problem.
	 * 
	 * @param problem the problem instance for which solutions have to be perturbed.
	 * @param maxBlockSize the maximum block size for assignment removals.
	 * TODO change maxBlockSize to a perturbationStrength (ratio with total number of slots).
	 * @param strength the strength of the perturbation.
	 * @param rng the random number generator used to select random assignments.
	 * 
	 * @throws IllegalArgumentException if the maximum block size is lower than <code>1</code>
	 * or the strength is lower than <code>0</code>.
	 * @throws IllegalArgumentException if the number of employee is lower than <code>1<code>. 
	 * @throws NullPointerException if the problem, or the random number
	 * generator is <code>null</code>.
	 */
	public RuinAndRecreateProcedure(ShiftSchedulingProblem problem, int maxBlockSize,
			double strength, Random rng) {
		if (problem == null || rng == null)
			throw new NullPointerException();
		if (problem.employees().size() < 1)
			throw new IllegalArgumentException();
		if (maxBlockSize < 1)
			throw new IllegalArgumentException();
		if (strength < 0.)
			throw new IllegalArgumentException();
		this.maxBlockSize = maxBlockSize;
		this.minSlotRemovals = ((int) (strength*(
				problem.employees().size()*problem.getSchedulingPeriod().size())))+1;
		this.solution = null;
		this.rng = rng;
		reconstructionProcedure = new GreedyConstruction(problem, rng);
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
		
		solution.invalidateEvaluation();
		int nbRemovals = 0;
		while (nbRemovals < minSlotRemovals) {
			int employeeIndex;
			int blockSize;
			int startIndex;
			// Choose block size
			blockSize = rng.nextInt(maxBlockSize)+1;
			// Choose start index
			startIndex = rng.nextInt(solution.assignments.size());
			// Choose employee index
			employeeIndex = rng.nextInt(solution.employees.size());
			// Adjust to start from a null-null assignment
			if (solution.assignments.get(startIndex).get(employeeIndex) == null) {
				int adjustedStartIndex = startIndex+1;
				int adjustedEmployeeIndex = employeeIndex;
				boolean adjusted = false;
				while(!adjusted) {
					// Adjust indexes of next assignment
					if (adjustedStartIndex == solution.assignments.size()) {
						adjustedStartIndex = 0;
						adjustedEmployeeIndex++;
						if (adjustedEmployeeIndex == solution.employees.size()) {
							adjustedEmployeeIndex = 0;
						}
					}
					if (adjustedStartIndex == startIndex &&
							adjustedEmployeeIndex == employeeIndex) {
						// No more assignments in solution
						nbRemovals = minSlotRemovals;
						break;
					}
					// Check if non-null assignment
					if (solution.assignments.get(adjustedStartIndex)
							.get(adjustedEmployeeIndex) != null) {
						startIndex = adjustedStartIndex;
						employeeIndex = adjustedEmployeeIndex;
						adjusted = true;
					}
					// Next assignment
					adjustedStartIndex++;
				}
			}
			// Adjust start index and block size
			startIndex = startIndex-(blockSize/2);
			if (startIndex < 0)
				startIndex = 0;
			if (startIndex+blockSize>solution.assignments.size()) {
				blockSize = solution.assignments.size()-startIndex;
			}
			// Remove assignments
			int endIndex = startIndex+blockSize-1;
			for (int dayIndex=startIndex; dayIndex<=endIndex; dayIndex++) {
				Shift assigned = solution.assignments.get(dayIndex).get(employeeIndex);
				if (assigned != null) {
					solution.assignments.get(dayIndex).set(employeeIndex, null);
					solution.unassignedSlots.get(dayIndex).add(assigned);
					nbRemovals++;
				}
			}
		}
		// Reconstruct solution
		reconstructionProcedure.init(solution);
		while(reconstructionProcedure.nextStep())
		done = true;
		return false;
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

}
