/**
 * Copyright 2013-2014, Universitaet Osnabrueck
 * Author: David Meignan
 */
package de.uos.inf.ischedule.model.rule;

import java.util.ArrayList;
import java.util.HashMap;

import de.uos.inf.ischedule.model.Contract;
import de.uos.inf.ischedule.model.Employee;
import de.uos.inf.ischedule.model.ShiftSchedulingProblem;
import de.uos.inf.ischedule.model.Solution;
import de.uos.inf.ischedule.model.WeekendType;

/**
 * Attribute for the number of consecutive working weekends.
 * 
 * @author David Meignan
 */
public class ConsecutiveWorkingWeekendsAttribute extends IntegerAttribute {

	/**
	 * Key of the attribute.
	 */
	private String key = "nb-consecutive-working-weekends";
	
	/**
	 * List of starting and ending day index by type of weekend.
	 */
	private HashMap<WeekendType, ArrayList<Integer>> weekendStarts;
	private HashMap<WeekendType, ArrayList<Integer>> weekendEnds;
	
	/**
	 * Creates the attribute for a given problem.
	 * 
	 * @param problem the problem.
	 */
	public ConsecutiveWorkingWeekendsAttribute(ShiftSchedulingProblem problem) {
		// Creates the list of start and end day indexes
		weekendStarts = new HashMap<WeekendType, ArrayList<Integer>>();
		weekendEnds = new HashMap<WeekendType, ArrayList<Integer>>();
		for (WeekendType type: WeekendType.values()) {
			if (weekendTypeUsed(type, problem)) {
				ArrayList<Integer> weekendStartIndexes = new ArrayList<Integer>();
				ArrayList<Integer> weekendEndIndexes = new ArrayList<Integer>();
				int dayOfWeekStart = type.getStartDayOfWeek();
				int dayOfWeekEnd = type.getEndDayOfWeek();
				for (int dayIndex=0; dayIndex<problem.getSchedulingPeriod().size();
						dayIndex++) {
					if (problem.getSchedulingPeriod().getDayOfWeek(dayIndex) 
							== dayOfWeekEnd) {
						// First weekend cut
						weekendStartIndexes.add(0);
						weekendEndIndexes.add(dayIndex);
					} else if (problem.getSchedulingPeriod().getDayOfWeek(dayIndex) 
							== dayOfWeekStart) {
						weekendStartIndexes.add(dayIndex);
						int weekendEndIndex = dayIndex
								+ type.getDuration()-1;
						// Adjust if weekend cut
						if (weekendEndIndex >= problem.getSchedulingPeriod().size())
							weekendEndIndex = problem.getSchedulingPeriod().size()-1;
						weekendEndIndexes.add(weekendEndIndex);
						dayIndex = weekendEndIndex;
					}
				}
				weekendStarts.put(type, weekendStartIndexes);
				weekendEnds.put(type, weekendEndIndexes);
			}
		}
	}
	
	/**
	 * Check if the type of weekend is used in the problem.
	 */
	private boolean weekendTypeUsed(WeekendType type, ShiftSchedulingProblem problem) {
		for (Contract contract: problem.contracts()) {
			if (contract.getWeekendType() == type) {
				for (Employee employee: problem.employees()) {
					if (employee.getContract() == contract)
						return true;
				}
			}
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see de.uos.inf.ischedule.model.rule.Attribute#getKey()
	 */
	@Override
	public String getKey() {
		return key;
	}

	/* (non-Javadoc)
	 * @see de.uos.inf.ischedule.model.rule.Attribute#getValue(int, int, de.uos.inf.ischedule.model.Solution, boolean, boolean)
	 */
	@Override
	public Integer getValue(int dayIndex, int employeeIndex, Solution solution,
			boolean isPreferred, boolean isUnwanted) {
		int consecutiveWorkingWeekends = 0;
		WeekendType weekendType = solution.employees.get(employeeIndex)
				.getContract().getWeekendType();
		if (!weekendType.isOnWeekend(
				solution.problem.getSchedulingPeriod().getDayOfWeek(dayIndex)))
			return new Integer(consecutiveWorkingWeekends);
		// Current weekend
		ArrayList<Integer> weekendStartIndexes = weekendStarts.get(weekendType);
		ArrayList<Integer> weekendEndIndexes = weekendEnds.get(weekendType);
		int currentWeekendIndex = 0;
		while(dayIndex<weekendStartIndexes.get(currentWeekendIndex))
			currentWeekendIndex++;
		if (!isWorkingWeekend(currentWeekendIndex, weekendStartIndexes,
				weekendEndIndexes, solution, employeeIndex))
			return new Integer(consecutiveWorkingWeekends);
		consecutiveWorkingWeekends++;
		// Previous weekends
		for (int weekendIndex=currentWeekendIndex-1; weekendIndex>=0; weekendIndex--) {
			if (!isWorkingWeekend(weekendIndex, weekendStartIndexes,
					weekendEndIndexes, solution, employeeIndex))
				break;
			consecutiveWorkingWeekends++;
		}
		// Next weekends
		for (int weekendIndex=currentWeekendIndex+1; 
				weekendIndex<weekendStartIndexes.size(); weekendIndex++) {
			if (!isWorkingWeekend(weekendIndex, weekendStartIndexes,
					weekendEndIndexes, solution, employeeIndex))
				break;
			consecutiveWorkingWeekends++;
		}
		return new Integer(consecutiveWorkingWeekends);
	}

	/**
	 * Check if the weekend is worked (at least one day).
	 */
	private boolean isWorkingWeekend(int weekendIndex, 
			ArrayList<Integer> weekendStartIndexes,
			ArrayList<Integer> weekendEndIndexes,
			Solution solution, int employeeIndex) {
		int startIndex = weekendStartIndexes.get(weekendIndex);
		int endIndex = weekendEndIndexes.get(weekendIndex);
		for (int dayIndex=startIndex; dayIndex<=endIndex; dayIndex++) {
			if (solution.assignments.get(dayIndex).get(employeeIndex) != null) {
				return true;
			}
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((key == null) ? 0 : key.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		ConsecutiveWorkingWeekendsAttribute other = (ConsecutiveWorkingWeekendsAttribute) obj;
		if (key == null) {
			if (other.key != null)
				return false;
		} else if (!key.equals(other.key))
			return false;
		return true;
	}

}
