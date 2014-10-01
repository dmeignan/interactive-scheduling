/**
 * Copyright 2013, Universitaet Osnabrueck
 * Author: David Meignan
 */
package de.uos.inf.ischedule.model.heuristic;

import de.uos.inf.ischedule.model.SolutionEvaluation;

/**
 * Record for tracing iterations of the ILS process.
 * 
 * @author David Meignan
 */
public class IteratedLocalSearchTraceRecord {

	/**
	 * Iteration recorded.
	 */
	private int iteration;
	
	/**
	 * Evaluation of the best found solution.
	 */
	private SolutionEvaluation bestFound;
	
	/**
	 * Evaluation of the last solution accepted, i.e. initial solution
	 * of the iteration (before perturbation and local-search).
	 */
	private SolutionEvaluation lastAccepted;
	
	/**
	 * Evaluation of the current solution, i.e. solution obtained at the end of
	 * the iteration after perturbation and local-search.
	 */
	private SolutionEvaluation current;
	
	/**
	 * Distance between the last accepted solution and the current solution.
	 */
	private int distanceLastToCurrent;
	
	/**
	 * Creates a record.
	 * 
	 * @param iteration the iteration number.
	 * @param bestFound evaluation of the best found solution.
	 * @param lastAccepted evaluation of the last solution accepted, i.e. initial solution
	 * of the iteration (before perturbation and local-search).
	 * @param current evaluation of the current solution, i.e. solution obtained at the end of
	 * the iteration after perturbation and local-search.
	 * @param distanceLastToCurrent Distance between the last accepted solution 
	 * and the current solution.
	 * @throws IllegalArgumentException if one of the evaluations is <code>null</code>.
	 */
	public IteratedLocalSearchTraceRecord(
			int iteration, SolutionEvaluation bestFound,
			SolutionEvaluation lastAccepted, SolutionEvaluation current,
			int distanceLastToCurrent) {
		if (bestFound == null || lastAccepted == null || current == null)
			throw new IllegalArgumentException();
		this.iteration = iteration;
		this.bestFound = bestFound;
		this.lastAccepted = lastAccepted;
		this.current = current;
		this.distanceLastToCurrent = distanceLastToCurrent;
	}
	
	/**
	 * Returns the iteration number of the record.
	 * 
	 * @return the iteration number of the record.
	 */
	public int getIteration() {
		return iteration;
	}

	/**
	 * Return the evaluation of the best found solution.
	 * 
	 * @return the evaluation of the best found solution.
	 */
	public SolutionEvaluation getBestFound() {
		return bestFound;
	}

	/**
	 * Returns the evaluation of the last accepted solution.
	 * 
	 * @return the evaluation of the last accepted solution.
	 */
	public SolutionEvaluation getLastAccepted() {
		return lastAccepted;
	}

	/**
	 * Returns the evaluation of the current solution.
	 * 
	 * @return the evaluation of the current solution.
	 */
	public SolutionEvaluation getCurrent() {
		return current;
	}

	/**
	 * Returns the distance between the last accepted and the current solution.
	 * 
	 * @return the distance between the last accepted and the current solution.
	 */
	public int getDistanceLastToCurrent() {
		return distanceLastToCurrent;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("IteratedLocalSearchTraceRecord [iteration=");
		builder.append(iteration);
		builder.append(", bestFound=");
		builder.append(bestFound);
		builder.append(", lastAccepted=");
		builder.append(lastAccepted);
		builder.append(", current=");
		builder.append(current);
		builder.append(", distanceLastToCurrent=");
		builder.append(distanceLastToCurrent);
		builder.append("]");
		return builder.toString();
	}
	
}
