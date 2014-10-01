/**
 * Copyright 2013-2014, Universitaet Osnabrueck
 * Author: David Meignan
 */
package de.uos.inf.ischedule.model.heuristic;

/**
 * Filter for swap moves. This class is the basis for designing
 * basic tabu list.
 * 
 * @author David Meignan
 */
public abstract class SwapMoveFilter {

	/**
	 * Returns <code>true</code> if the move satisfy the filter and should
	 * not be rejected. Returns <code>false</code> otherwise.
	 * 
	 * @param move the move to be evaluated.
	 * @return <code>true</code> if the move satisfy the filter and should
	 * not be rejected. Returns <code>false</code> otherwise.
	 */
	public abstract boolean isSatisfied(SwapMove move);
	
}
