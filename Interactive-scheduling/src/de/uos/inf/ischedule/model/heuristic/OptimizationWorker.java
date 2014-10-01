/**
 * Copyright 2013-2014, Universitaet Osnabrueck
 * Author: David Meignan
 */
package de.uos.inf.ischedule.model.heuristic;

import javax.swing.SwingWorker;

import de.uos.inf.ischedule.model.Solution;
import de.uos.inf.ischedule.model.SolutionEvaluation;

/**
 * Abstract worker for optimization processes.
 * 
 * @author David Meignan
 */
public abstract class OptimizationWorker extends SwingWorker<Solution, Solution> {

	/**
	 * Returns a copy of the best found solution if it costs less than the given limit.
	 * 
	 * @param limit the cost limit for which the best found solution is returned. 
	 * This parameter avoid to copy the solution unnecessarily.
	 * 
	 * @return a copy of the best found solution if its cost is less than the given limit, or 
	 * <code>null</code> if the best found solution evaluation is greater than or equal
	 * to the limit or no solution has been yet generated.
	 */
	public abstract Solution getBestFound(SolutionEvaluation limit);
	
}
