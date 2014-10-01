/**
 * Copyright 2013, Universitaet Osnabrueck
 * Author: David Meignan
 */
package de.uos.inf.ischedule.model.heuristic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Random;

import javax.swing.SwingWorker;

import de.uos.inf.ischedule.model.ShiftSchedulingProblem;
import de.uos.inf.ischedule.model.Solution;
import de.uos.inf.ischedule.model.SolutionEvaluation;

/**
 * A worker for executing a threaded version of a memetic search.
 * 
 * @author David Meignan
 */
public class MemeticAlgorithmWorker extends OptimizationWorker {

	/**
	 * Settings
	 */
	private int maxIterations = Integer.MAX_VALUE;
	private long timeLimitSec = 400;
	private double perturbationStrenght = 0.03;
	private int localSearchBlockSizes[] = new int[]{1,2,3,4,5,6,7};
	private int crossoverBlockSizes[] = new int[]{1,5,7};
	private long rngSeed = 0;
	private int nbThreads = 3;
	private int populationSize = 30;
	
	/**
	 * The best found solution
	 */
	private Solution bestFoundSolution;
	
	/**
	 * The start time
	 */
	private long startNanoTime;
	
	/**
	 * The total number of iterations
	 */
	private int iterationCount;
	
	/**
	 * The set of threads
	 */
	private MAThread[] maThreads;
	
	/**
	 * The population
	 */
	private SolutionPool solutionPool;
	
	
	/**
	 * Constructs a MA worker.
	 * 
	 * @param problem the problem to be solved.
	 * @param initialSolution an optional initial solution.
	 * @param settings settings for ILS.
	 * @throws IllegalArgumentException if the problem is <code>null</code>
	 * or value for settings is <code>null</code>.
	 */
	public MemeticAlgorithmWorker(ShiftSchedulingProblem problem,
			Solution initialSolution) {
		if (problem == null)
			throw new IllegalArgumentException();
		// Initialize parameters and threads
		this.bestFoundSolution = null;
		this.iterationCount = 0;
		this.maThreads = new MAThread[nbThreads];
		this.solutionPool = new SolutionPool(problem);
		this.solutionPool.select(initialSolution);
		for (int th=0; th<maThreads.length; th++) {
			maThreads[th] = new MAThread(problem, th);
		}
	}

	/**
	 * Returns a copy of the best found solution if it costs less than the given limit.
	 * 
	 * @param limit the cost limit for which the best found solution is returned. This parameter
	 * avoid to copy the solution unnecessarily.
	 * 
	 * @return a copy of the best found solution if its cost is less than the given limit, or 
	 * <code>null</code> if the best found solution evaluation is greater than or equal
	 * to the limit or no solution has been yet generated.
	 */
	public synchronized Solution getBestFound(SolutionEvaluation limit) {
		if (bestFoundSolution == null)
			return null;
		if (limit == null) {
			return new Solution(bestFoundSolution, true);
		}
		// Check limit
		if (bestFoundSolution.getEvaluation().compareTo(limit) < 0) {
			return new Solution(bestFoundSolution, true);
		}
		return null;
	}
	
	/**
	 * Returns the evaluation of the best found solution, or <code>null</code> if
	 * no solution has been generated yet.
	 * 
	 * @return the evaluation of the best found solution, or <code>null</code> if
	 * no solution has been generated yet.
	 */
	public synchronized SolutionEvaluation getBestFoundEvaluation() {
		if (bestFoundSolution == null)
			return null;
		return bestFoundSolution.getEvaluation();
	}
	
	/**
	 * Updates the best found solution with the given solution. A copy of the provided solution
	 * is stored if it is better than the current best found solution.
	 * 
	 * @param solution the best found solution candidate.
	 * @return <code>true</code> if the best found solution has been replaced,
	 * <code>false</code> otherwise.
	 */
	public synchronized boolean updateBestFound(Solution solution) {
		if (solution == null)
			return false;
		if (bestFoundSolution == null ||
				bestFoundSolution.getEvaluation().compareTo(solution.getEvaluation()) > 0) {
			// Copy the new best found solution
			bestFoundSolution = new Solution(solution, true);
			// Update progress
			updateProgress();
			return true;
		}
		return false;
	}
	
	/**
	 * Returns <code>true</code> if the stopping condition is met or the task
	 * has been canceled, <code>false</code> otherwise.
	 * 
	 * @return <code>true</code> if the stopping condition is met or the task
	 * has been canceled, <code>false</code> otherwise.
	 */
	public synchronized boolean stopOptimization() {
		// Check task canceled
		if(isDone())
			return true;
		// Check elapsed time
		if ((System.nanoTime()-startNanoTime) > (timeLimitSec*1000000000L))
			return true;
		// Check number of iterations
		if (iterationCount > maxIterations)
			return true;
		return false;
	}
	
	/**
	 * Updates the progress value of the worker based on the number of iterations,
	 * elapsed time, and state of the task.
	 */
	public synchronized void updateProgress() {
		if (isDone()) {
			setProgress(100);
		} else {
			int progress = (int) getExactProgress();
			// Adjust integer value
			if (progress >= 100)
				progress = 99;
			if (progress <= 0)
				progress = 0;
			setProgress(progress);
		}
	}
	
	/**
	 * Returns the progress as a float value.
	 * 
	 * @return the progress as a float value.
	 */
	public synchronized float getExactProgress() {
		if (this.getState() == SwingWorker.StateValue.PENDING)
			return 0.f;
		// Progress according to the number of iterations
		float iterationProgress = (((float)(iterationCount)) / 
				((float)maxIterations)*100.f);
		// Progress according to the time limit
		float timeProgress = (((float) (System.nanoTime()-startNanoTime)) /
				((float)(timeLimitSec*1000000000L))*100.f);
		// Total progress
		float progress = Math.max(iterationProgress, timeProgress);
		if (progress < 0.f)
			progress = 0.f;
		if (progress > 100.f)
			progress = 100.f;
		return progress;
	}
	
	/**
	 * Increment the total number of iteration performed and return its value.
	 */
	public synchronized int incrementTotalIterations() {
		iterationCount++;
		return iterationCount;
	}
	
	/* (non-Javadoc)
	 * @see javax.swing.SwingWorker#doInBackground()
	 */
	@Override
	protected Solution doInBackground() throws Exception {
		// Record starting time
		startNanoTime = System.nanoTime();
		// Run threads
		for (Thread thread: maThreads) {
			thread.start();
		}
		// Run thread
		while(!stopOptimization()) {
			updateProgress();
			try{
				Thread.sleep(500);
			} catch (InterruptedException e) {
				// The task has been canceled
			}
		}
		// Wait thread termination
		for (MAThread thread: maThreads) {
			try {
				if (thread.isAlive())
					thread.join();
			} catch (InterruptedException e) {
				// The task has been cancelled
				// Other threads will end at the next call to "stoppingConditionMet".
			}
		}
		// Update progress
		updateProgress();
		// Return best found
		return getBestFound(null);
	}
	
	/**
	 * Pool of solutions.
	 */
	class SolutionPool {
		
		/**
		 * List of solutions
		 */
		private ArrayList<Solution> solutions;
		
		/**
		 * Random number generator
		 */
		private Random rng;
		
		/**
		 * Crossover operator.
		 */
		private BlockCrossover crossover;
		
		/**
		 * Next insertion index
		 */
		private int insertionIndex = 0;
		
		private int insertionStrategy = REPLACE_IN_WORST_SET;
		private static final int REPLACE_NEXT = 0;
		private static final int REPLACE_ONE_OF_WORST = 1;
		private static final int REPLACE_IN_WORST_SET = 2;
		private double worstSetRatio = 0.1;
		
		/**
		 * Constructs a solution pool
		 */
		public SolutionPool(ShiftSchedulingProblem problem) {
			solutions = new ArrayList<Solution>(populationSize);
			rng = new Random(rngSeed);
			crossover = new BlockCrossover(problem, rng,
					crossoverBlockSizes);
		}
		
		/**
		 * Returns a solution from the offspring of the solution pool.
		 * Returns <code>null</code> if the pool does not contain enough
		 * solutions to generate an offspring.
		 * 
		 * @return a solution from the offspring of the solution pool.
		 * Returns <code>null</code> if the pool does not contain enough
		 * solutions to generate an offspring.
		 */
		public synchronized Solution getOffspringSolution() {
			if (solutions.size() <= 1)
				return null;
			int s1Index = rng.nextInt(solutions.size());
			int s2Index = rng.nextInt(solutions.size());
			while(s1Index == s2Index)
				s2Index = rng.nextInt(solutions.size());
			Solution s1 = solutions.get(s1Index);
			Solution s2 = solutions.get(s2Index);
			// Not necessary to copy the solution for this operator.
			crossover.init(s1, s2);
			while(crossover.nextStep());
			return crossover.getResult();
		}
		
		/**
		 * Introduces the solution in the pool if the solution passes 
		 * the selection criteria.
		 * 
		 * @param s the solution that may be introduced in the pool.
		 */
		public synchronized void select(Solution toInsert) {
			if (toInsert == null)
				return;
			int insertedAt = -1;
			// Empty solution pool
			if (solutions.isEmpty()) {
				// Add the first solution
				solutions.add(toInsert);
				insertedAt = solutions.size()-1;
			}
			
			// Check solution pool
			boolean isBestSolution = true;
			ArrayList<Integer> worstSolutionIndexes = new ArrayList<Integer>();
			SolutionEvaluation worstEval = null;
			for (int inPoolIndex=0; inPoolIndex<solutions.size();
					inPoolIndex++) {
				SolutionEvaluation inPoolEval = solutions.get(inPoolIndex).getEvaluation();
				// Get index of worst solutions
				if (worstEval == null) {
					// First solution
					worstEval = inPoolEval;
					worstSolutionIndexes.add(inPoolIndex);
				} else {
					int worstEvalComparison = worstEval.compareTo(inPoolEval);
					if (worstEvalComparison == 0) {
						// Additional worst
						worstSolutionIndexes.add(inPoolIndex);
					} else if (worstEvalComparison < 0) {
						// New worst
						worstEval = inPoolEval;
						worstSolutionIndexes.clear();
						worstSolutionIndexes.add(inPoolIndex);
					}
				}
				// Compare evaluations
				int inPoolComparison = inPoolEval.compareTo(toInsert.getEvaluation());
				if (inPoolComparison == 0) {
					// Same cost
					isBestSolution = false;
					// Check if the solutions are the same
					if (toInsert.equalAssignments(solutions.get(inPoolIndex))) {
						// Same solution in pool
						// TODO start debug
						System.out.println("Same solution in pool.");
						// TODO end debug
						return;
					}
				} else if (inPoolComparison < 0) {
					// Solution to insert has worse cost
					isBestSolution = false;
				}
			}
			
			// Check pool size
			if (solutions.size() < populationSize) {
				// Complete the population with the solution
				solutions.add(toInsert);
				insertedAt = solutions.size()-1;
			} else {
				if (insertionStrategy == REPLACE_NEXT) {
					// Replace the next solution
					solutions.set(insertionIndex, toInsert);
					insertedAt = insertionIndex;
					insertionIndex = (insertionIndex+1)%solutions.size();
				} else if (insertionStrategy == REPLACE_ONE_OF_WORST) {
					// Replace one of the worst
					int worstIndex = worstSolutionIndexes.get(
							rng.nextInt(worstSolutionIndexes.size()));
					solutions.set(worstIndex, toInsert);
					insertedAt = worstIndex;
					// TODO start debug
					System.out.println("Worst at: "+worstSolutionIndexes.toString());
					// TODO end debug
				} else if (insertionStrategy == REPLACE_IN_WORST_SET) {
					// Replace in the set of worst solutions
					// Order solution pool (worst first)
					Collections.shuffle(solutions); // For same costs
					Collections.sort(solutions, new Comparator<Solution>() {
						public int compare(Solution s1, Solution s2) {
							return s2.getEvaluation().compareTo(s1.getEvaluation());
						}
					});
					// Worst set size
					int worstSetSize = (int) (worstSetRatio*((double)solutions.size()));
					if (worstSetSize == 0)
						worstSetSize = 1;
					// Replace in set
					int worstIndex = rng.nextInt(worstSetSize);
					solutions.set(worstIndex, toInsert);
					insertedAt = worstIndex;
				}
			}
			// TODO start debug
			StringBuilder builder = new StringBuilder();
			builder.append("[");
			for (int i=0;i<solutions.size();i++) {
				builder.append("(");
				builder.append(Integer.toString(solutions.get(i).getEvaluation().getCost(1)));
				builder.append(",");
				builder.append(Integer.toString(solutions.get(i).getEvaluation().getCost(3)));
				builder.append(")");
				if (i==insertedAt) {
					if (isBestSolution)
						builder.append("!");
					builder.append("*");
				} else {
					builder.append(" ");
				}
				if (i<solutions.size()-1)
					builder.append(", ");
			}
			builder.append("]");
			System.out.println(builder.toString());
			// TODO end debug
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("[");
			for (int i=0;i<solutions.size();i++) {
				builder.append(solutions.get(i).getEvaluation().toString());
				if (i<solutions.size()-1)
					builder.append(", ");
			}
			builder.append("]");
			return builder.toString();
		}
		
	}
	
	/**
	 * Thread that encapsulates an ILS procedure.
	 */
	class MAThread extends Thread {

		/**
		 * The generation procedure (for initial solutions)
		 */
		private Operator generationProcedure;
		
		/**
		 * The mutation procedure
		 */
		private ExchangePerturbation mutationProcedure;
		
		/**
		 * The local search procedure
		 */
		private VariableNeighborhoodDescent localSearchProcedure;
		
		/**
		 * The index of the thread
		 */
		private int ilsThreadIndex;
		
		/**
		 * Random number generator
		 */
		private Random rng;
		
		private int currentStep;
		private static final int GET_SOLUTION = 0;
		private static final int MUTATION = 1;
		private static final int LOCAL_SEARCH = 2;
		private Solution currentSolution;
		
		
		/**
		 * Constructs an MA thread.
		 */
		public MAThread(ShiftSchedulingProblem problem, int threadIndex) {
			// Copy parameters
			this.ilsThreadIndex = threadIndex;
			// Set random number generator
			rng = new Random(rngSeed+threadIndex);
			// Creates operators
			generationProcedure = new FastBlockConstruction(problem, rng);
			mutationProcedure = new ExchangePerturbation(problem,
					localSearchBlockSizes, perturbationStrenght, rng);
			localSearchProcedure = new VariableNeighborhoodDescent(problem,
					localSearchBlockSizes, NeighborSelectionPolicy.FirstImproving,
					rng);
			// Set current step
			currentStep = GET_SOLUTION;
			currentSolution = null;
		}
		
		/* (non-Javadoc)
		 * @see java.lang.Thread#run()
		 */
		@Override
		public void run() {
			while(!stopOptimization())  {	// Check finish
				if (currentStep == GET_SOLUTION) {
					currentSolution = solutionPool.getOffspringSolution();
					if (currentSolution == null) {
						// Generate solution
						generationProcedure.init();
						while(generationProcedure.nextStep());
						currentSolution = generationProcedure.getResult();
						// initialize local-search
						localSearchProcedure.init(currentSolution);
						currentStep = LOCAL_SEARCH;
					} else {
						// Initialize mutation
						mutationProcedure.init(currentSolution);
						currentStep = MUTATION;
					}
				} else if (currentStep == MUTATION) {
					if (!mutationProcedure.isDone()) {
						// Next mutation step
						mutationProcedure.nextStep();
					} else {
						// Initialize local-search
						localSearchProcedure.init(currentSolution);
						currentStep = LOCAL_SEARCH;
					}
				} else if (currentStep == LOCAL_SEARCH) {
					if (!localSearchProcedure.isDone()) {
						// Next step of local-search
						localSearchProcedure.nextStep();
					} else {
						// Submit solution for selection
						solutionPool.select(currentSolution);
						updateBestFound(currentSolution);
						incrementTotalIterations();
						currentStep = GET_SOLUTION;
					}
				}
			}
			updateBestFound(currentSolution);
		}
		
		/* (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("ILSThread, index=");
			builder.append(ilsThreadIndex);
			return builder.toString();
		}

	}
	
}
