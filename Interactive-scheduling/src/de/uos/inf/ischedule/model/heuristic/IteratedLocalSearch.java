/**
 * Copyright 2013, Universitaet Osnabrueck
 * Author: David Meignan
 */
package de.uos.inf.ischedule.model.heuristic;

import java.util.Random;

import de.uos.inf.ischedule.model.ShiftSchedulingProblem;
import de.uos.inf.ischedule.model.Solution;

/**
 * Iterated local search procedure. Initial solution is generated using
 * <code>FastGreedyConstruction</code>, local search is performed using
 * <code>SwapVND</code>, and perturbation is performed by <code>ExchangePerturbation</code>.
 * 
 * @author David Meignan
 */
public class IteratedLocalSearch {
	
	/**
	 * Maximum number of iterations.
	 */
	private int maxIterations;
	
	/**
	 * Perturbation strength.
	 */
	private double perturbationStrength;
	
	/**
	 * Acceptance rate.
	 */
	private double worseSolutionAcceptanceRate;
	
	/**
	 * Block sizes.
	 */
	private int[] blockSizes;
	
	/**
	 * Random number generator.
	 */
	private Random rng;
	
	/**
	 * Neighborhood selection policy.
	 */
	private NeighborSelectionPolicy selectionPolicy;
	
	/**
	 * Current solution
	 */
	private Solution currentSolution;
	
	/**
	 * Best found solutions
	 */
	private Solution overallBestFoundSolution;
	private Solution restartBestFoundSolution;
	private int restartBestFoundIteration;
	
	/**
	 * Number of iterations without improvement for restarting from
	 * a new generated solution
	 * (0 for restarting at each iteration)
	 */
	private int restartIterations;
	
	/**
	 * Solution accepted for the current iteration
	 */
	private Solution lastAcceptedSolution;
	
	/**
	 * Current iteration
	 */
	private int currentIteration;
	
	/**
	 * Variable neighborhood search procedure
	 */
	private VariableNeighborhoodDescent localSearchProcedure;
	
	/**
	 * Perturbation procedure
	 */
	private ExchangePerturbation perturbationProcedure; // TODO
//	private GuidedSwapPerturbation perturbationProcedure;
	
	/**
	 * Generation procedure
	 */
	private Operator generationProcedure;
	
	/**
	 * Reconstruction procedure
	 */
	private Operator reconstructionProcedure;
	
	/**
	 * Creates an iterated local search procedure.
	 * 
	 * @param problem the shift scheduling problem on which the procedure will be applied.
	 * @param maxIterations the maximum number of iterations.
	 * @param perturbationStrength the perturbation strength.
	 * @param worseSolutionAcceptanceRate the acceptance rate.
	 * @param blockSizes the block sizes of swap-moves for the variable neighborhood
	 * descent procedure.
	 * @param rng the random number generator.
	 * @param selectionPolicy the selection policy for the variable neighborhood
	 * descent procedure.
	 * @param restartIterations Number of iterations without improvement for restarting from
	 * a new generated solution (0 for restarting at each iteration).
	 * @param nbGenerations the number of generations for the initial solution.
	 * 
	 * @throws IllegalArgumentException if the problem is <code>null</code>, or
	 * the selection policy is <code>null</code>, or the random number generator
	 * is <code>null</code> or the maximum number of iterations is lower than <code>1</code>,
	 * or the perturbation strength is negative, or the acceptance rate is negative,
	 * or the maximum block size is lower than <code>1</code>, or the number of iterations
	 * without improvement for restarting is negative.
	 */
	public IteratedLocalSearch(ShiftSchedulingProblem problem, 
			int maxIterations, double perturbationStrength,
			double worseSolutionAcceptanceRate, int[] blockSizes,
			Random rng, NeighborSelectionPolicy selectionPolicy,
			int restartIterations) {
		// Check parameters
		if (problem == null || selectionPolicy == null || rng == null)
			throw new IllegalArgumentException();
		if (maxIterations < 1 || perturbationStrength < 0 ||
				worseSolutionAcceptanceRate < 0 || blockSizes.length == 0 ||
				restartIterations < 0)
			throw new IllegalArgumentException();
		// Copy and set parameters
		this.maxIterations = maxIterations;
		this.perturbationStrength = perturbationStrength;
		this.worseSolutionAcceptanceRate = worseSolutionAcceptanceRate;
		this.blockSizes = blockSizes;
		this.rng = rng;
		this.selectionPolicy = selectionPolicy;
		this.restartIterations = restartIterations;
		// Init.
		this.currentSolution = null;
		this.overallBestFoundSolution = null;
		this.restartBestFoundSolution = null;
		this.restartBestFoundIteration = 0;
		this.lastAcceptedSolution = null;
		this.currentIteration = 0;
		
		this.localSearchProcedure = new VariableNeighborhoodDescent(
				problem, this.blockSizes, this.selectionPolicy, this.rng);
		
		// TODO
		this.perturbationProcedure = new ExchangePerturbation(
				problem, this.blockSizes, this.perturbationStrength, this.rng);
		
//		this.perturbationProcedure = new GuidedSwapPerturbation(
//				problem, this.blockSizes, this.perturbationStrength, this.rng);
		
		this.generationProcedure = new FastBlockConstruction(
				problem, this.rng);
		
		this.reconstructionProcedure = new GreedyConstruction(
				problem, this.rng);
		
	}
	
	/**
	 * Initializes the procedure. An initial solution can be passed to the
	 * procedure. This procedure also reinitialize the best found solution
	 * and the counter of iterations.
	 * 
	 * @param initialSolution the initial solution or <code>null</code> for generating the 
	 * solution using the <code>FastGreedyConstruction</code> procedure.
	 */
	public void init(Solution initialSolution) {
		currentSolution = initialSolution;
		overallBestFoundSolution = null;
		restartBestFoundSolution = null;
		restartBestFoundIteration = 0;
		currentIteration = 0;
	}
	
	/**
	 * Performs the next step of the ILS procedure. Returns <code>true</code>
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
		if (currentIteration == 0) {
			currentIteration = 1;
			// Generate or reconstruct the initial solution
			if (currentSolution == null) {
				generationProcedure.init();
				while(generationProcedure.nextStep());
				currentSolution = generationProcedure.getResult();
			} else {
				reconstructionProcedure.init(currentSolution);
				while(reconstructionProcedure.nextStep());
				currentSolution = reconstructionProcedure.getResult();
			}
			// Update best found solution
			updateBestFoundSolution(currentSolution);
			// Store last accepted solution
			lastAcceptedSolution = new Solution(currentSolution, true);
			// Initialize local-search
			localSearchProcedure.init(currentSolution);
		} else if (currentIteration > maxIterations) {
			// End of the ILS procedure
			return false;
		} else {
			if (localSearchProcedure.isDone()) {
				// Apply acceptance criterion and restart
				if ( (currentIteration-restartBestFoundIteration) > restartIterations) {
					// Restart
					generationProcedure.init();
					while(generationProcedure.nextStep());
					restartBestFoundSolution = generationProcedure.getResult();
					restartBestFoundIteration = currentIteration;
					currentSolution = new Solution(restartBestFoundSolution, true);
					// Accepted solution is the generated solution
					lastAcceptedSolution = new Solution(currentSolution, true);
				} else if (currentSolution == restartBestFoundSolution) {
					// Current is the best found
					currentSolution = new Solution(restartBestFoundSolution, true);
					// Accepted solution is the best found
					lastAcceptedSolution = new Solution(currentSolution, true);
					// Perturbation
					perturbationProcedure.init(currentSolution);
					while(perturbationProcedure.nextStep());
				} else {
					// Check if current is worse than best
					if (currentSolution.getEvaluation()
						.compareTo(restartBestFoundSolution.getEvaluation()) > 0) {
						// Apply acceptance rate
						if (rng.nextDouble() > this.worseSolutionAcceptanceRate) {
							// Return to best found
							currentSolution = new Solution(restartBestFoundSolution, true);
							// Accepted solution is the best found
							lastAcceptedSolution = new Solution(currentSolution, true);
						} else {
							// Accepted solution is the current solution (worse than the best)
							lastAcceptedSolution = new Solution(currentSolution, true);
						}
					}
					// Perturbation
					perturbationProcedure.init(currentSolution);
					while(perturbationProcedure.nextStep());
				}
				// Initialize local-search
				localSearchProcedure.init(currentSolution);
			} else {
				// Local-search
				localSearchProcedure.nextStep();
				// Update best found
				updateBestFoundSolution(currentSolution);
				// Update iteration number
				if (localSearchProcedure.isDone()) {
					currentIteration++;
				}
			}
		}
		return true;
	}
	
	/**
	 * Returns <code>true</code> if the procedure is completed.
	 * 
	 * @return <code>true</code> if the procedure is completed.
	 * 
	 * @throws IllegalStateException if the <code>init</code> method has not
	 * been called beforehand.
	 */
	public boolean isDone() {
		return (currentIteration > maxIterations);
	}
	
	/**
	 * Returns the best found solution so far.
	 * 
	 * @return the best found solution so far. Returns <code>null</code> if the
	 * initial solution has not been yet generated.
	 */
	public Solution getBestFoundSolution() {
		return overallBestFoundSolution;
	}
	
	/**
	 * Returns the last accepted solution i.e. initial solution of the iteration.
	 * 
	 * @return the last accepted solution.
	 */
	public Solution getLastAcceptedSolution() {
		return lastAcceptedSolution;
	}
	
	/**
	 * Returns the current solution.
	 * 
	 * @return the current solution.
	 */
	public Solution getCurrentSolution() {
		return currentSolution;
	}
	
	/**
	 * Returns the current iteration number.
	 * 
	 * @return the current iteration number.
	 */
	public int getCurrentIteration() {
		return currentIteration;
	}
	
	/**
	 * Updates the best found solutions (overall and restart) with the one
	 * given in parameter.
	 * Only improving solution is taken into account. The new best found
	 * solution will be used in the next iteration of the iterated local
	 * search procedure, depending on the acceptance criterion.
	 * 
	 * @param solution the solution to be considered for updating the best
	 * found solution of the iterated local search procedure.
	 * @return <code>true</code> if the best found solution have been modified,
	 * <code>false</code> otherwise.
	 */
	public boolean updateBestFoundSolution(Solution solution) {
		if (solution == null) {
			return false;
		}
		boolean update = false;
		// Update overall best found
		if (overallBestFoundSolution == null) {
			overallBestFoundSolution = solution;
			update = true;
		} else if (overallBestFoundSolution.getEvaluation()
				.compareTo(solution.getEvaluation()) > 0) {
			overallBestFoundSolution = solution;
		}
		// Update restart best found
		if (restartBestFoundSolution == null) {
			restartBestFoundSolution = solution;
			restartBestFoundIteration = currentIteration;
			update = true;
		} else if (restartBestFoundSolution.getEvaluation()
				.compareTo(solution.getEvaluation()) > 0) {
			restartBestFoundSolution = solution;
			restartBestFoundIteration = currentIteration;
			update = true;
		}
		return update;
	}
	
}
