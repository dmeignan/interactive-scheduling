/**
 * Copyright 2013, Universitaet Osnabrueck
 * Author: David Meignan
 */
package de.uos.inf.ischedule.model.heuristic;

import java.util.Random;

import javax.swing.SwingWorker;

import de.uos.inf.ischedule.model.ShiftSchedulingProblem;
import de.uos.inf.ischedule.model.Solution;
import de.uos.inf.ischedule.model.SolutionEvaluation;
import de.uos.inf.ischedule.ui.Messages;

/**
 * A worker for executing a threaded version of the iterated local search
 * procedure.
 * 
 * @author David Meignan
 */
public class IteratedLocalSearchWorker extends OptimizationWorker {

	/**
	 * Settings for ILS procedures
	 */
	private IteratedLocalSearchSettings settings;
	
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
	private int totalIterations;
	
	/**
	 * The trace of the run
	 */
	private Trace<SolutionEvaluation> solvingTrace;
	
	/**
	 * The trace of iterations of the ILS procedure
	 */
	private Trace<IteratedLocalSearchTraceRecord> iterationTrace;
	
	/**
	 * The set of ILS threads
	 */
	private ILSThread[] ilsThreads;
	
	
	
	/**
	 * Constructs a ILS worker.
	 * 
	 * @param problem the problem to be solved.
	 * @param initialSolution an optional initial solution.
	 * @param settings settings for ILS.
	 * @throws IllegalArgumentException if the problem is <code>null</code>
	 * or value for settings is <code>null</code>.
	 */
	public IteratedLocalSearchWorker(ShiftSchedulingProblem problem,
			Solution initialSolution, IteratedLocalSearchSettings settings) {
		if (problem == null || settings == null)
			throw new IllegalArgumentException();
		this.settings = settings;
		// Initialize parameters and threads
		this.bestFoundSolution = null;
		this.totalIterations = 0;
		if (settings.recordTrace()) {
			this.solvingTrace = new Trace<SolutionEvaluation>(
					Messages.getString("IteratedLocalSearchWorker.ils")); //$NON-NLS-1$
		}
		if (settings.recordFullIterationTrace()) {
			this.iterationTrace = new Trace<IteratedLocalSearchTraceRecord>(
					Messages.getString("IteratedLocalSearchWorker.ils")); //$NON-NLS-1$
		}
		this.ilsThreads = new ILSThread[this.settings.getNbThreads()];
		for (int th=0; th<ilsThreads.length; th++) {
			ilsThreads[th] = new ILSThread(
					settings, problem, th, initialSolution);
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
		if ((System.nanoTime()-startNanoTime) > settings.getTimeLimitNano())
			return true;
		// Check number of iterations
		if (totalIterations > settings.getMaxIterations())
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
			int progress = (int) getProgressFloat();
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
	private float getProgressFloat() {
		if (this.getState() == SwingWorker.StateValue.PENDING)
			return 0.f;
		// Progress according to the number of iterations
		float iterationProgress = (((float)(totalIterations)) / 
				((float)settings.getMaxIterations())*100.f);
		// Progress according to the time limit
		float timeProgress = (((float) (System.nanoTime()-startNanoTime)) /
				((float)settings.getTimeLimitNano())*100.f);
		// Total progress
		float progress = Math.max(iterationProgress, timeProgress);
		if (progress < 0.f)
			progress = 0.f;
		if (progress > 100.f)
			progress = 100.f;
		return progress;
	}
	
	/**
	 * Returns the progress of the iterated local search as a float value.
	 * 
	 * @return the progress of the iterated local search as a float value.
	 */
	public synchronized float getExactProgress() {
		return getProgressFloat();
	}
	
	/**
	 * Update the trace of the run. A new record will be added only if the update period
	 * is passed, or the record is forced with the parameter, or there is a new best 
	 * found solution.
	 * 
	 * @param forceRecord force the addition of a new record in the trace.
	 */
	public synchronized void updateTraceRecord(boolean forceRecord) {
		if (bestFoundSolution == null || !settings.recordTrace())
			return;
		if (forceRecord || solvingTrace.isEmpty()) {
			// Forced addition
			solvingTrace.add(new TraceRecord<SolutionEvaluation>(
					(System.nanoTime()-startNanoTime),
					bestFoundSolution.getEvaluation()));
		} else if (
				( (System.nanoTime()-startNanoTime)
				  -solvingTrace.getLastRecord().getElapsedNanoTime())
				  > settings.getTraceRecordPeriodNano()){
			// Record period elapsed
			solvingTrace.add(new TraceRecord<SolutionEvaluation>(
					(System.nanoTime()-startNanoTime),
					bestFoundSolution.getEvaluation()));
		} else if (
				solvingTrace.getLastRecord() != null &&
						solvingTrace.getLastRecord().getValue()
						.compareTo(bestFoundSolution.getEvaluation()) != 0
				) {
			// New best found solution
			solvingTrace.add(new TraceRecord<SolutionEvaluation>(
					(System.nanoTime()-startNanoTime),
					bestFoundSolution.getEvaluation()));
		}
	}
	
	/**
	 * Increment the total number of iteration performed and return its value.
	 */
	public synchronized int incrementTotalIterations() {
		totalIterations++;
		return totalIterations;
	}
	
	/**
	 * Adds a record to the trace of iterations.
	 * 
	 * @param iterationRecord the record to be added to the trace.
	 */
	public synchronized void addIterationRecord(
			IteratedLocalSearchTraceRecord record) {
		if (!settings.recordFullIterationTrace() || record == null)
			return;
		iterationTrace.add(new TraceRecord<IteratedLocalSearchTraceRecord>(
				(System.nanoTime()-startNanoTime),
				record));
		// TODO start debug
//		System.out.println(record.toString());
		// TODO end debug
	}
	
	/* (non-Javadoc)
	 * @see javax.swing.SwingWorker#doInBackground()
	 */
	@Override
	protected Solution doInBackground() throws Exception {
		// Record starting time
		startNanoTime = System.nanoTime();
		// Run threads
		for (Thread thread: ilsThreads) {
			thread.start();
		}
		// Run thread for automatic 
		while(!stopOptimization()) {
			updateProgress();
			try{
				Thread.sleep(500);
			} catch (InterruptedException e) {
				// The task has been canceled
			}
		}
		// Wait thread termination
		for (ILSThread thread: ilsThreads) {
			try {
				if (thread.isAlive())
					thread.join();
			} catch (InterruptedException e) {
				// The task has been cancelled
				// Other threads will end at the next call to "stoppingConditionMet".
			}
		}
		// Update of the trace
		updateTraceRecord(true);
		// Update progress
		updateProgress();
		// Return best found
		return getBestFound(null);
	}
	
	/**
	 * Returns the solving trace of this worker. Note that this method is not
	 * synchronized and should be called only when the worker has finished its task. Returns
	 * <code>null</code> if the trace has not been saved.
	 * 
	 * @return the solving trace of this worker.
	 */
	public Trace<SolutionEvaluation> getSolvingTrace() {
		return solvingTrace;
	}
	
	/**
	 * Returns the trace of iterations of the ILS procedure. Note that this method is not
	 * synchronized and should be called only when the worker has finished its task. Returns
	 * <code>null</code> if the trace has not been saved.
	 * 
	 * @return the trace of iterations of the ILS procedure.
	 */
	public Trace<IteratedLocalSearchTraceRecord> getIterationTrace() {
		return iterationTrace;
	}
	
	/**
	 * Thread that encapsulates an ILS procedure.
	 */
	class ILSThread extends Thread {

		/**
		 * The ILS procedure managed by the thread
		 */
		private IteratedLocalSearch ilsProcedure;
		
		/**
		 * Indicates if the thread must share its best found solution
		 */
		private boolean shareBestFound;
		
		/**
		 * The index of the thread
		 */
		private int ilsThreadIndex;
		
		/**
		 * Constructs an ILS thread.
		 */
		public ILSThread(IteratedLocalSearchSettings settings,
				ShiftSchedulingProblem problem, int ilsThreadIndex,
				Solution initialSolution) {
			// Copy parameters
			this.ilsThreadIndex = ilsThreadIndex;
			if (settings.getNbThreads() > 1)
				this.shareBestFound = settings.shareBestFoundSolution();
			else
				this.shareBestFound = false;
			// Creates an ILS procedure
			ilsProcedure = new IteratedLocalSearch(
					problem,
					Integer.MAX_VALUE,
					settings.getPerturbationStrength(),
					settings.getWorseSolutionAcceptanceRate(),
					settings.getBlockSizes(),
					new Random(settings.getRngSeed()+ilsThreadIndex),
					settings.getSelectionPolicy(),
					settings.getRestartIterations()
					);
			// Initialize the procedure
			ilsProcedure.init(
					(initialSolution==null)?(null):(new Solution(initialSolution, true)));
		}
		
		/* (non-Javadoc)
		 * @see java.lang.Thread#run()
		 */
		@Override
		public void run() {
			while(!stopOptimization())  {	// Check finish
				int initialIteration = ilsProcedure.getCurrentIteration();
				// Next step
				ilsProcedure.nextStep();
				// Update iterations
				if (ilsProcedure.getCurrentIteration() > initialIteration) {
					int globalIteration = incrementTotalIterations();
					if (settings.recordFullIterationTrace()) {
						// Record iteration info.
						addIterationRecord(new IteratedLocalSearchTraceRecord(
								globalIteration,
								ilsProcedure.getBestFoundSolution().getEvaluation(),
								ilsProcedure.getLastAcceptedSolution().getEvaluation(),
								ilsProcedure.getCurrentSolution().getEvaluation(),
								ilsProcedure.getCurrentSolution().distanceTo(
										ilsProcedure.getLastAcceptedSolution())
								));
					}
				}
				// Publish best found
				updateBestFound(ilsProcedure.getBestFoundSolution());
				// Check best found
				if (shareBestFound) {
					ilsProcedure.updateBestFoundSolution(
								getBestFound(
										ilsProcedure.getBestFoundSolution().getEvaluation()));
				}
				// Update trace
				updateTraceRecord(false);
			}
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
