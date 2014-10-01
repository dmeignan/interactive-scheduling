/**
 * Copyright 2013, Universitaet Osnabrueck
 * Author: David Meignan
 */
package de.uos.inf.ischedule.model.heuristic;

/**
 * Set of parameter values for executing an iterated local search procedure.
 * 
 * @author David Meignan
 */
public class IteratedLocalSearchSettings {

	/**
	 * Maximum number of iterations.
	 */
	private int maxIterations;
	
	/**
	 * The time limit in seconds.
	 */
	private int timeLimitSec;
	
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
	 * Seed for random number generator.
	 */
	private long rngSeed;

	/**
	 * Neighborhood selection policy.
	 */
	private NeighborSelectionPolicy selectionPolicy;
	
	/**
	 * Number of threads.
	 */
	private int nbThreads;
	
	/**
	 * Share the best found solution between threads.
	 */
	private boolean shareBestFoundSolution;
	
	/**
	 * The period for recording solving trace in seconds.
	 */
	private int traceRecordPeriodSec;

	/**
	 * Record the trace of the run.
	 */
	private boolean recordTrace;
	
	/**
	 * Record the trace of iterations.
	 */
	private boolean recordFullIterationTrace;
	
	/**
	 * Number of iterations without improvement for restarting the search from
	 * a new generated solution (0 for restarting at each iteration)
	 */
	private int restartIterations;
	
	/**
	 * Constructs a setting.
	 * 
	 * @param maxIterations the maximum number of iterations.
	 * @param timeLimitSec the time limit in seconds.
	 * @param perturbationStrength the perturbation strength.
	 * @param worseSolutionAcceptanceRate the acceptance rate for worse solution.
	 * @param blockSizes the block sizes for swap neighborhood.
	 * @param rngSeed the seed value for random number generation.
	 * @param selectionPolicy the selection policy for local search.
	 * @param nbThreads the number of threads.
	 * @param shareBestFoundSolution the policy for sharing the best found solution between
	 * threads.
	 * @param traceRecordPeriodSec the period between records of the trace.
	 * @param recordTrace record the trace of the run (best found value).
	 * @param recordFullIterationTrace record the trace of each iteration.
	 * @param restartIterations Number of iterations without improvement for restarting the search from
	 * a new generated solution (0 for restarting at each iteration).
	 * 
	 * @throws IllegalArgumentException if one of the arguments has an illegal value.
	 */
	public IteratedLocalSearchSettings(int maxIterations, int timeLimitSec,
			double perturbationStrength, double worseSolutionAcceptanceRate,
			int[] blockSizes, long rngSeed,
			NeighborSelectionPolicy selectionPolicy, int nbThreads,
			boolean shareBestFoundSolution, int traceRecordPeriodSec,
			boolean recordTrace, boolean recordFullIterationTrace,
			int restartIterations) {
		this.setMaxIterations(maxIterations);
		this.setTimeLimitSec(timeLimitSec);
		this.setPerturbationStrength(perturbationStrength);
		this.setWorseSolutionAcceptanceRate(worseSolutionAcceptanceRate);
		this.setBlockSizes(blockSizes);
		this.setRngSeed(rngSeed);
		this.setSelectionPolicy(selectionPolicy);
		this.setNbThreads(nbThreads);
		this.setShareBestFoundSolution(shareBestFoundSolution);
		this.setTraceRecordPeriod(traceRecordPeriodSec);
		this.setRecordTrace(recordTrace);
		this.setRecordFullIterationTrace(recordFullIterationTrace);
		this.setRestartIterations(restartIterations);
	}

	/**
	 * Returns the maximum number of iterations.
	 * 
	 * @return the maximum number of iterations.
	 */
	public int getMaxIterations() {
		return maxIterations;
	}

	/**
	 * Sets the maximum number of iterations.
	 * 
	 * @param maxIterations the maximum number of iterations to set.
	 * @throws IllegalArgumentException if the maximum number of iterations is less
	 * than <code>1</code>.
	 */
	public void setMaxIterations(int maxIterations) {
		if (maxIterations < 1)
			throw new IllegalArgumentException();
		this.maxIterations = maxIterations;
	}

	/**
	 * Returns the time limit in seconds.
	 * 
	 * @return the time limit in seconds.
	 */
	public int getTimeLimitSec() {
		return timeLimitSec;
	}

	/**
	 * Returns the time limit in nanoseconds.
	 * 
	 * @return the time limit in nanoseconds.
	 */
	public long getTimeLimitNano() {
		return ((long) this.timeLimitSec)*1000000000L;
	}
	
	/**
	 * Sets the time limit in seconds.
	 * 
	 * @param timeLimitSec the time limit in seconds to set.
	 * @throws IllegalArgumentException if the time limit is less
	 * than <code>1</code>.
	 */
	public void setTimeLimitSec(int timeLimitSec) {
		if (timeLimitSec < 1)
			throw new IllegalArgumentException();
		this.timeLimitSec = timeLimitSec;
	}

	/**
	 * Returns the perturbation strength.
	 * 
	 * @return the perturbation strength.
	 */
	public double getPerturbationStrength() {
		return perturbationStrength;
	}

	/**
	 * Set the perturbation strength.
	 * 
	 * @param the perturbation strength to set.
	 * @throws IllegalArgumentException if the perturbation strength is less
	 * than <code>0</code> or greater than <code>1</code>.
	 */
	public void setPerturbationStrength(double perturbationStrength) {
		if (perturbationStrength < 0 || perturbationStrength > 1)
			throw new IllegalArgumentException();
		this.perturbationStrength = perturbationStrength;
	}

	/**
	 * Returns the acceptance rate for worse solution than the best found solution.
	 * 
	 * @return the acceptance rate.
	 */
	public double getWorseSolutionAcceptanceRate() {
		return worseSolutionAcceptanceRate;
	}

	/**
	 * Sets the acceptance rate for worse solution than the best found solution.
	 * 
	 * @param the acceptance rate to set.
	 * @throws IllegalArgumentException if the acceptance rate is less
	 * than <code>0</code> or greater than <code>1</code>.
	 */
	public void setWorseSolutionAcceptanceRate(double worseSolutionAcceptanceRate) {
		if (worseSolutionAcceptanceRate < 0 || worseSolutionAcceptanceRate > 1)
			throw new IllegalArgumentException();
		this.worseSolutionAcceptanceRate = worseSolutionAcceptanceRate;
	}

	/**
	 * Returns the block sizes for swap-neighborhood.
	 * 
	 * @return the block sizes for swap-neighborhood.
	 */
	public int[] getBlockSizes() {
		return blockSizes;
	}

	/**
	 * Set the block sizes for swap-neighborhood.
	 * 
	 * @param the block sizes to set.
	 * @throws IllegalArgumentException if the block sizes are less
	 * than <code>1</code>.
	 */
	public void setBlockSizes(int[] blockSizes) {
		if (blockSizes == null || blockSizes.length == 0)
			throw new IllegalArgumentException("At least one block size must be" +
					"provided.");
		for (int bs: blockSizes) {
			if (bs < 1)
				throw new IllegalArgumentException("The block size must be" +
						"greater than 0.");
		}
		this.blockSizes = blockSizes;
	}

	/**
	 * Returns the seed for random number generation.
	 * 
	 * @return the seed for random number generation.
	 */
	public long getRngSeed() {
		return rngSeed;
	}

	/**
	 * Sets the seed for random number generation.
	 * 
	 * @param rngSeed the seed for random number generation to set
	 */
	public void setRngSeed(long rngSeed) {
		this.rngSeed = rngSeed;
	}

	/**
	 * Returns the selection policy for local search.
	 * 
	 * @return the selection policy for local search.
	 */
	public NeighborSelectionPolicy getSelectionPolicy() {
		return selectionPolicy;
	}

	/**
	 * Sets the selection policy for local search.
	 * 
	 * @param selectionPolicy the selection policy to set.
	 * @throws IllegalArgumentException if the selection policy is <code>null</code>.
	 */
	public void setSelectionPolicy(NeighborSelectionPolicy selectionPolicy) {
		if (selectionPolicy == null)
			throw new IllegalArgumentException();
		this.selectionPolicy = selectionPolicy;
	}

	/**
	 * Returns the number of thread to be used for the procedure.
	 * 
	 * @return the number of thread.
	 */
	public int getNbThreads() {
		return nbThreads;
	}

	/**
	 * Sets the number of threads to be used.
	 * 
	 * @param nbThreads the number of threads to set.
	 * @throws IllegalArgumentException if the number of threads is less than
	 * <code>1</code>.
	 */
	public void setNbThreads(int nbThreads) {
		if (nbThreads < 1)
			throw new IllegalArgumentException();
		this.nbThreads = nbThreads;
	}

	/**
	 * Return <code>true</code> if the best found solution must be shared between
	 * threads, <code>false</code> otherwise.
	 * 
	 * @return <code>true</code> if the best found solution must be shared between
	 * threads, <code>false</code> otherwise.
	 */
	public boolean shareBestFoundSolution() {
		return shareBestFoundSolution;
	}

	/**
	 * Sets the policy for sharing the best found solution between threads.
	 * 
	 * @param <code>true</code> if the best found solution must be shared between
	 * threads, <code>false</code> otherwise..
	 */
	public void setShareBestFoundSolution(boolean shareBestFoundSolution) {
		this.shareBestFoundSolution = shareBestFoundSolution;
	}

	/**
	 * Returns the period in seconds between records of the trace.
	 * 
	 * @return the period in seconds between records of the trace.
	 */
	public int getTraceRecordPeriod() {
		return traceRecordPeriodSec;
	}

	/**
	 * Returns the period in nanoseconds between records of the trace.
	 * 
	 * @return the period in nanoseconds between records of the trace.
	 */
	public long getTraceRecordPeriodNano() {
		return ((long) this.traceRecordPeriodSec)*1000000000L;
	}

	/**
	 * Sets the period in seconds between records of the trace.
	 * 
	 * @param traceRecordPeriod the period to set.
	 * @throws IllegalArgumentException if the period is less than
	 * <code>1</code>.
	 */
	public void setTraceRecordPeriod(int traceRecordPeriodSec) {
		if (traceRecordPeriodSec < 1)
			throw new IllegalArgumentException();
		this.traceRecordPeriodSec = traceRecordPeriodSec;
	}
	
	/**
	 * Determines if the trace of the best found solution value is recorded.
	 * 
	 * @param recordTrace <code>true</code> to record the trace of the best found solution,
	 * <code>false</code> otherwise.
	 */
	public void setRecordTrace(boolean recordTrace) {
		this.recordTrace = recordTrace;
	}
	
	/**
	 * Returns <code>true</code> if the trace of the best found solution is recorded,
	 * <code>false</code> otherwise.
	 * 
	 * @return <code>true</code> if the trace of the best found solution is recorded,
	 * <code>false</code> otherwise.
	 */
	public boolean recordTrace() {
		return recordTrace;
	}
	
	/**
	 * Determines if the trace of each iteration is recorded.
	 * 
	 * @param recordFullIterationTrace <code>true</code> to record the trace for each iteration,
	 * <code>false</code> otherwise.
	 */
	public void setRecordFullIterationTrace(boolean recordFullIterationTrace) {
		this.recordFullIterationTrace = recordFullIterationTrace;
	}
	
	/**
	 * Returns <code>true</code> if the trace of each iteration is recorded,
	 * <code>false</code> otherwise.
	 * 
	 * @return <code>true</code> if the trace of each iteration is recorded,
	 * <code>false</code> otherwise.
	 */
	public boolean recordFullIterationTrace() {
		return recordFullIterationTrace;
	}
	
	/**
	 * Set the number of iterations without improvement for restarting the search
	 * from a newly generated solution (0 for restarting at each iteration). 
	 * 
	 * @param restartIterations the number of iterations without improvement for restarting the search
	 * from a newly generated solution (0 for restarting at each iteration).
	 * @throws IllegalArgumentException if the argument has a negative value.
	 */
	public void setRestartIterations(int restartIterations) {
		if (restartIterations < 0)
			throw new IllegalArgumentException();
		this.restartIterations = restartIterations;
	}
	
	/**
	 * Returns the number of iterations without improvement for restarting the search
	 * from a newly generated solution (0 for restarting at each iteration). 
	 * 
	 * @return the number of iterations without improvement for restarting the search
	 * from a newly generated solution (0 for restarting at each iteration).
	 */
	public int getRestartIterations() {
		return restartIterations;
	}

}
