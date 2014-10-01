/**
 * Copyright 2013, Universitaet Osnabrueck
 * Author: David Meignan
 */
package de.uos.inf.ischedule.model.heuristic;

import java.util.Random;

import de.uos.inf.ischedule.model.ShiftSchedulingProblem;
import de.uos.inf.ischedule.model.Solution;

/**
 * Multi-start procedure for generating an initial solution. The procedure
 * generates greedily multiple solutions, then returns the best one.
 * 
 * @author David Meignan
 */
public class MultistartGreedyConstruction implements Operator {

	/**
	 * The number of generated solutions.
	 */
	private int nbGenerations;
	
	/**
	 * The number of remaining solutions to generate.
	 */
	private int remainingGenerations;
	
	/**
	 * The best found solution.
	 */
	private Solution bestFound = null;
	
	/**
	 * The generation procedure.
	 */
	private GreedyConstruction generationProcedure;
	
	/**
	 * Creates a multi-start procedure.
	 * 
	 * @param problem the shift scheduling problem.
	 * @param rng the random number generator.
	 * @param nbGenerations the number of generated solutions.
	 * 
	 * @throws IllegalArgumentException if the number of generation is negative or null.
	 */
	public MultistartGreedyConstruction(ShiftSchedulingProblem problem, Random rng,
			int nbGenerations) {
		if (nbGenerations <= 0)
			throw new IllegalArgumentException("The number of generated solution must " +
					"be positive.");
		this.nbGenerations = nbGenerations;
		this.remainingGenerations = nbGenerations;
		generationProcedure = new GreedyConstruction(problem, rng);
	}
	
	/* (non-Javadoc)
	 * @see de.uos.inf.ischedule.model.heuristic.Operator#init(de.uos.inf.ischedule.model.Solution[])
	 */
	@Override
	public void init(Solution... initialSolutions) {
		if (initialSolutions.length > 0)
			System.err.println("The procedure does not take into account " +
					"initial solutions.");
		remainingGenerations = nbGenerations;
		bestFound = null;
		generationProcedure.init();
	}

	/* (non-Javadoc)
	 * @see de.uos.inf.ischedule.model.heuristic.Operator#nextStep()
	 */
	@Override
	public boolean nextStep() {
		if (isDone())
			return false;
		if (generationProcedure.isDone()) {
			// next generation
			generationProcedure.init();
		}
		generationProcedure.nextStep();
		// Get best found
		if (generationProcedure.isDone()) {
			remainingGenerations--;
			Solution generated = generationProcedure.getResult();
			generated.getEvaluation();
			if (bestFound == null ||
					generated.getEvaluation().compareTo(bestFound.getEvaluation()) < 0)
				bestFound = generated;
		}
		return !isDone();
	}

	/* (non-Javadoc)
	 * @see de.uos.inf.ischedule.model.heuristic.Operator#isDone()
	 */
	@Override
	public boolean isDone() {
		return (remainingGenerations == 0 && generationProcedure.isDone());
	}

	/* (non-Javadoc)
	 * @see de.uos.inf.ischedule.model.heuristic.Operator#getResult()
	 */
	@Override
	public Solution getResult() {
		return bestFound;
	}
	
	/* (non-Javadoc)
	 * @see de.uos.inf.ischedule.model.heuristic.Operator#getResults()
	 */
	@Override
	public Solution[] getResults() {
		return new Solution[]{bestFound};
	}

}
