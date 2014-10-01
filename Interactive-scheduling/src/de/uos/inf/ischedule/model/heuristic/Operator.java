/**
 * Copyright 2013, Universitaet Osnabrueck
 * Author: David Meignan
 */
package de.uos.inf.ischedule.model.heuristic;

import de.uos.inf.ischedule.model.Solution;

/**
 * The Operator interface is the base interface for procedures that explore the search space. 
 * 
 * @author David Meignan
 */
public interface Operator {

	/**
	 * Initializes or reinitializes the procedure with a solution or a set of
	 * solutions. The solutions may be modified by the procedure.
	 * 
	 * @param initialSolution the initial solution.
	 */
	public void init(Solution... initialSolutions);
	
	/**
	 * Performs the next step of the procedure. Returns <code>true</code>
	 * if the procedure has additional steps, and <code>false</code> if the procedure
	 * is finished.
	 * 
	 * @return <code>true</code> if the procedure has additional steps, and 
	 * <code>false</code> if the procedure is finished.
	 * 
	 * @throws IllegalStateException if the <code>init</code> method has not
	 * been called beforehand.
	 */
	public boolean nextStep();
	
	/**
	 * Returns <code>true</code> if the procedure is completed.
	 * 
	 * @return <code>true</code> if the procedure is completed.
	 * 
	 * @throws IllegalStateException if the <code>init</code> method has not
	 * been called beforehand.
	 */
	public boolean isDone();
	
	/**
	 * Returns the first result or possibly intermediate result of the procedure.
	 * Returns <code>null</code> if no result is available.
	 * 
	 * @return the next result or possibly intermediate result of the procedure.
	 * Returns <code>null</code> if no result is available.
	 */
	public Solution getResult();
	
	/**
	 * Returns the results or possibly intermediate results of the procedure.
	 * Returns an empty array if no results are available.
	 * 
	 * @return the results or possibly intermediate results of the procedure.
	 */
	public Solution[] getResults();
	
}
