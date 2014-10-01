/**
 * Copyright 2013, Universitaet Osnabrueck
 * Author: David Meignan
 */
package de.uos.inf.ischedule.model.heuristic;

import java.util.Random;

import de.uos.inf.ischedule.model.ShiftSchedulingProblem;
import de.uos.inf.ischedule.model.Solution;

/**
 * Perturbation procedure that combines different operators.
 * 
 * @author David Meignan
 */
public class MixedPerturbationProcedure {

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
	 * Ruin and recreate procedure.
	 */
	private RuinAndRecreateProcedure ruinRecreateProcedure;
	
	/**
	 * Perturbation with block rotations.
	 */
	private ExchangePerturbation exchangePerturbation;
	
	/**
	 * Creates the operator to be applied on solutions of the given problem.
	 * 
	 * @param problem the problem instance for which solutions have to be perturbed.
	 * @param rng the random number generator used to select random assignments.
	 * 
	 * @throws IllegalArgumentException if the number of employee is lower than <code>1<code>. 
	 * @throws NullPointerException if the problem, or the random number
	 * generator is <code>null</code>.
	 */
	public MixedPerturbationProcedure(ShiftSchedulingProblem problem, Random rng) {
		if (problem == null || rng == null)
			throw new NullPointerException();
		if (problem.employees().size() < 1)
			throw new IllegalArgumentException();
		this.solution = null;
		this.rng = rng;
		ruinRecreateProcedure = new RuinAndRecreateProcedure(
				problem,
				7,			// Max block size
				0.1,		// Perturbation strength
				rng);
		exchangePerturbation = new ExchangePerturbation(
				problem,
				new int[]{1,2,3,4,5,6,7},	// Block sizes
				0.03,						// Perturbation strength
				rng);
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
	 * is finished.
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
		// Choose the perturbation operator
		if (rng.nextBoolean()) {
			// Ruin recreate
			ruinRecreateProcedure.init(solution);
			while(ruinRecreateProcedure.nextStep());
		} else {
			// Exchange
			exchangePerturbation.init(solution);
			while(exchangePerturbation.nextStep());
		}
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
