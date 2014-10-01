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
 * is based on random rotation of shift-block between three employees. 
 * 
 * @author David Meignan
 */
public class ExchangePerturbation {
	
	/**
	 * The block sizes for exchange moves
	 */
	private int[] blockSizes;
	
	/**
	 * The minimum number of slots to exchange in one iteration
	 */
	private int minSlotExchanges;
	
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
	 * Creates an exchange perturbation to be applied on solutions of the given problem.
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
	public ExchangePerturbation(ShiftSchedulingProblem problem, int[] blockSizes,
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
		
		int nbExchanges = 0;
		while (nbExchanges < minSlotExchanges) {
			int employee1Index;
			int employee2Index;
			int employee3Index;
			int blockSize;
			int startIndex;
			// Choose block size
			blockSize = blockSizes[rng.nextInt(blockSizes.length)];
			// Choose start index
			startIndex = rng.nextInt(solution.assignments.size());
			// Choose employee index
			employee1Index = rng.nextInt(solution.employees.size());
			// Employee 2
			employee2Index = rng.nextInt(solution.employees.size());
			if (employee2Index == employee1Index)
				employee2Index = (employee2Index+1)%solution.employees.size();
			// Try to find different shift assignments
			int employee2IndexTemp = employee2Index;
			while( solution.assignments.get(startIndex).get(employee1Index) ==
					solution.assignments.get(startIndex).get(employee2IndexTemp)) {
				employee2IndexTemp = (employee2IndexTemp+1)%solution.employees.size();
				if (employee2IndexTemp == employee2Index)
					break;
			}
			employee2Index = employee2IndexTemp;
			// Employee 3
			employee3Index = rng.nextInt(solution.employees.size());
			while(employee3Index == employee1Index ||
					employee3Index == employee2Index) {
				employee3Index = (employee3Index+1)%solution.employees.size();
			}
			// Try to find different shift assignments
			int employee3IndexTemp = employee3Index;
			while( solution.assignments.get(startIndex).get(employee1Index) ==
					solution.assignments.get(startIndex).get(employee3IndexTemp) ||
					solution.assignments.get(startIndex).get(employee2Index) ==
					solution.assignments.get(startIndex).get(employee3IndexTemp)) {
				employee3IndexTemp = (employee3IndexTemp+1)%solution.employees.size();
				if (employee3IndexTemp == employee3Index)
					break;
			}
			employee3Index = employee3IndexTemp;
			// Adjust start index and block size
			startIndex = startIndex-(blockSize/2);
			if (startIndex < 0)
				startIndex = 0;
			if (startIndex+blockSize>solution.assignments.size()) {
				blockSize = solution.assignments.size()-startIndex;
			}
			// Apply rotation
			int endIndex = startIndex+blockSize-1;
			for (int dayIndex=startIndex; dayIndex<=endIndex; dayIndex++) {
				Shift newAssignment1 = solution.assignments.get(dayIndex).get(employee2Index);
				Shift newAssignment2 = solution.assignments.get(dayIndex).get(employee3Index);
				Shift newAssignment3 = solution.assignments.get(dayIndex).get(employee1Index);
				solution.assignments.get(dayIndex).set(employee1Index, newAssignment1);
				solution.assignments.get(dayIndex).set(employee2Index, newAssignment2);
				solution.assignments.get(dayIndex).set(employee3Index, newAssignment3);
				nbExchanges = nbExchanges+3;
			}
		}
		solution.invalidateEvaluation();
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
