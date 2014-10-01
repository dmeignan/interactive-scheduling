/**
 * Copyright 2013, Universitaet Osnabrueck
 * Author: David Meignan
 */
package de.uos.inf.ischedule.model.heuristic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;
import java.util.TreeMap;

import de.uos.inf.ischedule.model.Constraint;
import de.uos.inf.ischedule.model.Shift;
import de.uos.inf.ischedule.model.ShiftSchedulingProblem;
import de.uos.inf.ischedule.model.Solution;
import de.uos.inf.ischedule.model.SolutionEvaluation;

/**
 * Generates or complete a solution by assigning unallocated shift-slots
 * to employees. Assignments are selected according to an estimation of
 * the cost of assignment.
 * 
 * @author David Meignan
 */
public class GreedyConstruction implements Operator {

	/**
	 * The shift scheduling problem.
	 */
	private ShiftSchedulingProblem problem;
	
	/**
	 * The random number generator.
	 */
	private Random rng;
	
	/**
	 * Set of shift-demand per day.
	 * The first dimension is the set of days.
	 */
	private ArrayList<TreeMap<Shift, Integer>> shiftDemands;
	
	/**
	 * The current solution (initial and final).
	 */
	private Solution currentSolution = null;
	
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
	public GreedyConstruction(ShiftSchedulingProblem problem, Random rnd) {
		if (problem == null || rnd == null)
			throw new IllegalArgumentException();
		this.problem = problem;
		this.rng = rnd;
		// Generates the list of shift slots (for faster reconstruction)
		shiftDemands = new ArrayList<TreeMap<Shift, Integer>>();
		for (int dayIndex=0; dayIndex<problem.getSchedulingPeriod().size();
				dayIndex++) {
			TreeMap<Shift, Integer> dayDemands = new TreeMap<Shift, Integer>();
			shiftDemands.add(dayDemands);
			for (Shift shift: problem.shifts()) {
				int demand = problem.getDemand(shift, dayIndex);
				dayDemands.put(shift, demand);
			}
		}
	}
	
	/**
	 * Generates or reconstructs a solution. If no solution is provided a new
	 * solution is generated from scratch. If a solution is given, it is reconstructed
	 * so all shift slots are assigned with no over-staffing.
	 * 
	 * @param initialSolution an optional initial solution to reconstruct.
	 * @return the solution generated or reconstructed.
	 */
	public Solution generate(Solution initialSolution) {
		// Initial solution
		Solution solution = null;
		if (initialSolution == null) {
			// Empty solution
			solution = new Solution(problem);
		} else {
			boolean complete = completeUnassignedSlots(initialSolution);
			if (complete)
				return initialSolution;
			solution = initialSolution;
		}
		
		// Randomly sorted list of day index
		ArrayList<Integer> dayIndexes = new ArrayList<Integer>();
		for (int di=0; di<problem.getSchedulingPeriod().size(); di++) {
			dayIndexes.add(di);
		}
		Collections.shuffle(dayIndexes, rng);
		
		// Iterate on day-indexes
		for (int dayIndex: dayIndexes) {
			// Randomly sorted list of unassigned shift-slots
			ArrayList<Shift> unassignedSlots = new ArrayList<Shift>(
					solution.unassignedSlots.get(dayIndex));
			Collections.shuffle(unassignedSlots, rng);
			// Iterate on unassigned slots
			for (Shift slot: unassignedSlots) {
				// Find best assignment estimated cost
				int bestEmployeeIndex = -1;
				SolutionEvaluation bestEstimatedCost = null;
				for (int employeeIndex=0; employeeIndex<solution.employees.size();
						employeeIndex++) {
					if (solution.assignments.get(dayIndex).get(employeeIndex) == null) {
						SolutionEvaluation estimatedCost =
								getEstimatedAssignmentEvaluation(
										solution, employeeIndex, slot, dayIndex);
						if (bestEmployeeIndex == -1) {
							bestEmployeeIndex = employeeIndex;
							bestEstimatedCost = estimatedCost;
						} else if (estimatedCost.compareTo(bestEstimatedCost) < 0) {
							bestEmployeeIndex = employeeIndex;
							bestEstimatedCost = estimatedCost;
						}
					}
				}
				if (bestEstimatedCost == null) {
					// Not enough employees!
					System.err.println("Not enough employees for the demand!");
					break;
				}
				// Assign to the best estimated cost
				solution.assignments.get(dayIndex).set(bestEmployeeIndex, slot);
			}
			// Remove unassigned slots
			solution.unassignedSlots.get(dayIndex).clear();
		}
		
		// Invalidate evaluation
		solution.invalidateEvaluation();
		
		return solution;
	}


	/**
	 * Returns the estimated cost of an assignment.
	 * 
	 * @param solution the solution on which the assignment is evaluated
	 * @param employeeIndex the employee index of the assignment
	 * @param slot the shift assigned
	 * @param dayIndex the day-index of the assignment
	 * 
	 * @return the estimated cost of an assignment.
	 */
	private SolutionEvaluation getEstimatedAssignmentEvaluation(
			Solution solution, int employeeIndex, Shift slot, int dayIndex) {
		int[] rValues = new int[problem.getMaxConstraintsRankIndex()+1];
		Arrays.fill(rValues, 0);
		for (int rankIndex=0; rankIndex<rValues.length; rankIndex++) {
			for (Constraint constraint: problem.constraints(rankIndex)) {
				rValues[rankIndex] +=
						constraint.getEvaluator(problem).getEstimatedAssignmentCost(
								solution, employeeIndex, slot, dayIndex);
			}
		}
		return new SolutionEvaluation(rValues);
	}

	/**
	 * Complete the list of unassigned slots. Returns <code>true</code> if the solution
	 * is complete, i.e. all demands are covered, <code>false</code> otherwise.
	 * 
	 * @param solution the solution for which unassigned slots have to be completed.
	 * @return <code>true</code> if the solution is complete, i.e. all demands 
	 * are covered, <code>false</code> otherwise.
	 */
	private boolean completeUnassignedSlots(Solution solution) {
		boolean complete = true;
		// Check if over-staffing
		for (int dayIndex=0; dayIndex<solution.assignments.size();
				dayIndex++) {
			// Clear the list of unassigned slots
			solution.unassignedSlots.get(dayIndex).clear();
			// Check over and under-staffing
			for (Shift shift: problem.shifts()) {
				int shiftDemand = shiftDemands.get(dayIndex).get(shift);
				int shiftAssignmentCount = 0;
				for (int employeeIndex=0; employeeIndex<solution.employees.size();
						employeeIndex++) {
					if (solution.assignments.get(dayIndex)
							.get(employeeIndex) == shift) {
						shiftAssignmentCount++;
					}
				}
				if (shiftDemand < shiftAssignmentCount) {
					// over-staffing
					solution.invalidateEvaluation();
					int nbOverStaffing = shiftAssignmentCount-shiftDemand;
					while(nbOverStaffing > 0) {
						// Remove assignment randomly selected
						int assignmentToRemove = rng.nextInt(shiftDemand+nbOverStaffing);
						for (int employeeIndex=0; employeeIndex<solution.employees.size();
								employeeIndex++) {
							if (solution.assignments.get(dayIndex)
									.get(employeeIndex) == shift) {
								if (assignmentToRemove == 0) {
									// Remove assignment
									solution.assignments.get(dayIndex)
										.set(employeeIndex, null);
									nbOverStaffing--;
									break;
								} else {
									assignmentToRemove--;
								}
							}
						}
					}
				} else if (shiftDemand > shiftAssignmentCount) {
					// under-staffing
					complete = false;
					int nbUnderStaffing = shiftDemand-shiftAssignmentCount;
					// add the demand to the list of unassigned slots
					for (int u=0; u<nbUnderStaffing; u++) {
						solution.unassignedSlots.get(dayIndex).add(shift);
					}
				}
			}
		}
		return complete;
	}

	/* (non-Javadoc)
	 * @see de.uos.inf.ischedule.model.heuristic.Operator#init(de.uos.inf.ischedule.model.Solution[])
	 */
	@Override
	public void init(Solution... initialSolutions) {
		done = false;
		currentSolution = null;
		if (initialSolutions.length == 1 && initialSolutions[0] != null)
			currentSolution = initialSolutions[0];
		else if (initialSolutions.length > 1)
			System.err.println("The procedure take into account " +
					"only one initial solution.");
	}

	/* (non-Javadoc)
	 * @see de.uos.inf.ischedule.model.heuristic.Operator#nextStep()
	 */
	@Override
	public boolean nextStep() {
		currentSolution = generate(currentSolution);
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
