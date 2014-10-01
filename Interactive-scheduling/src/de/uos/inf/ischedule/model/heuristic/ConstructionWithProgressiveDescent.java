/**
 * Copyright 2013, Universitaet Osnabrueck
 * Author: David Meignan
 */
package de.uos.inf.ischedule.model.heuristic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Random;

import de.uos.inf.ischedule.model.Constraint;
import de.uos.inf.ischedule.model.ShiftCoverageConstraint;
import de.uos.inf.ischedule.model.ShiftSchedulingProblem;
import de.uos.inf.ischedule.model.SingleAssignmentPerDayConstraint;
import de.uos.inf.ischedule.model.Solution;

/**
 * Generates a solution and improve it by progressively introducing the constraints.
 * 
 * @author David Meignan
 */
public class ConstructionWithProgressiveDescent implements Operator {

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
	 * State of the procedure:
	 */
	private boolean done = false;
	
	/**
	 * Set of active constraints.
	 */
	private ArrayList<ArrayList<Constraint>> activeConstraints;
	
	/**
	 * Set of constraint not yet active.
	 */
	private ArrayList<ArrayList<Constraint>> disabledConstraints;
	private boolean remainsDisabledConstraints;
	
	/**
	 * VND procedure that considers only active constraints. 
	 */
	private BiasedVariableNeighborhoodDescent vnd;
	
	/**
	 * Procedure for generating a solution.
	 */
	private Operator constructionProcedure;
	
	private int[] BLOCK_SIZES = {1,2,3,5,7};
	
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
	public ConstructionWithProgressiveDescent(ShiftSchedulingProblem problem, Random rnd) {
		if (problem == null || rnd == null)
			throw new IllegalArgumentException();
		this.problem = problem;
		this.rng = rnd;
	}
	
	/* (non-Javadoc)
	 * @see de.uos.inf.ischedule.model.heuristic.Operator#init(de.uos.inf.ischedule.model.Solution[])
	 */
	@Override
	public void init(Solution... initialSolutions) {
		if (initialSolutions.length > 0)
			System.err.println("The procedure does not take into account " +
					"initial solutions.");
		done = false;
		currentSolution = null;
		vnd = null;
		// Create list of active and disabled constraints
		activeConstraints = new ArrayList<ArrayList<Constraint>>();
		disabledConstraints = new ArrayList<ArrayList<Constraint>>();
		remainsDisabledConstraints = true;
		for (int rankIndex=0; rankIndex<=problem.getMaxConstraintsRankIndex();
				rankIndex++) {
			ArrayList<Constraint> activeSet = new ArrayList<Constraint>();
			ArrayList<Constraint> disableSet = new ArrayList<Constraint>();
			activeConstraints.add(activeSet);
			disabledConstraints.add(disableSet);
			disableSet.addAll(problem.constraints(rankIndex));
			// Activate coverage constraints
			Iterator<Constraint> constraintIt = disableSet.iterator();
			while (constraintIt.hasNext()) {
				Constraint c = constraintIt.next();
				if (c instanceof ShiftCoverageConstraint ||
						c instanceof SingleAssignmentPerDayConstraint) {
					constraintIt.remove();
					activeSet.add(c);
				}
			}
			// Randomize constraints order (of the same rank)
			Collections.shuffle(disableSet, rng);
		}
		// Generation procedure
		constructionProcedure = new FastBlockConstruction(problem, rng);
	}

	/* (non-Javadoc)
	 * @see de.uos.inf.ischedule.model.heuristic.Operator#nextStep()
	 */
	@Override
	public boolean nextStep() {
		if (done)
			return false;
		if (currentSolution == null) {
			// Generate solution with random assignments
			constructionProcedure.init();
			while(constructionProcedure.nextStep());
			currentSolution = constructionProcedure.getResult();
			return true;
		} else if (vnd != null && !vnd.isDone()) {
			// Improvement with VND
			vnd.nextStep();
			return true;
		} else {
			// Check if additional constraint to introduce
			if (!remainsDisabledConstraints) {
				done = true;
				return false;
			}
			// Introduce constraint
			activateNextConstraint();
			// Initialize VND
			vnd = new BiasedVariableNeighborhoodDescent(
					problem, BLOCK_SIZES, rng, activeConstraints);
			vnd.init(currentSolution);
		}
		return true;
	}

	/**
	 * Activates the next constraint and update the variable that
	 * indicates if there are remaining disabled constraints.
	 */
	private void activateNextConstraint() {
		int rankIndexToActivate = 0;
		while(rankIndexToActivate < disabledConstraints.size() &&
				disabledConstraints.get(rankIndexToActivate).isEmpty()) {
			rankIndexToActivate++;
		}
		if (rankIndexToActivate == disabledConstraints.size()) {
			remainsDisabledConstraints = false;
			return;
		}
		activeConstraints.get(rankIndexToActivate).add(
				disabledConstraints.get(rankIndexToActivate).remove(0)
				);
		if (rankIndexToActivate == (disabledConstraints.size()-1) &&
				disabledConstraints.get(rankIndexToActivate).isEmpty())
			remainsDisabledConstraints = false;
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
