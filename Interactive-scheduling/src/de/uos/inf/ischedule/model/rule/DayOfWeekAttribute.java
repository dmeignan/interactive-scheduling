/**
 * Copyright 2013-2014, Universitaet Osnabrueck
 * Author: David Meignan
 */
package de.uos.inf.ischedule.model.rule;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

import de.uos.inf.ischedule.model.ShiftSchedulingProblem;
import de.uos.inf.ischedule.model.Solution;

/**
 * Attribute for days of the week.
 * 
 * @author David Meignan
 */
public class DayOfWeekAttribute extends Attribute<String> {

	/**
	 * List of shift label.
	 */
	private ArrayList<String> values;

	/**
	 * Key of the attribute.
	 */
	private String key = "day-of-week";
	
	/**
	 * Creates the attribute for a given problem.
	 * 
	 * @param problem the problem.
	 */
	public DayOfWeekAttribute(ShiftSchedulingProblem problem) {
		// Creates the values
		values = new ArrayList<String>();
		values.add("Monday");
		values.add("Tuesday");
		values.add("Wednesday");
		values.add("Thursday");
		values.add("Friday");
		values.add("Saturday");
		values.add("Sunday");
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Iterable#iterator()
	 */
	@Override
	public Iterator<String> iterator() {
		return Collections.unmodifiableList(values).iterator();
	}

	/* (non-Javadoc)
	 * @see de.uos.inf.ischedule.model.rule.Attribute#getKey()
	 */
	@Override
	public String getKey() {
		return key;
	}

	/* (non-Javadoc)
	 * @see de.uos.inf.ischedule.model.rule.Attribute#getType()
	 */
	@Override
	public ATTRIBUTE_TYPE getType() {
		return ATTRIBUTE_TYPE.Nominal;
	}

	/* (non-Javadoc)
	 * @see de.uos.inf.ischedule.model.rule.Attribute#getValue(int, int, de.uos.inf.ischedule.model.Solution, boolean, boolean)
	 */
	@Override
	public String getValue(int dayIndex, int employeeIndex, Solution solution,
			boolean isPreferred, boolean isUnwanted) {
		return values.get(solution.problem.getSchedulingPeriod().getDayOfWeek(dayIndex)-1);
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
		DayOfWeekAttribute other = (DayOfWeekAttribute) obj;
		if (key == null) {
			if (other.key != null)
				return false;
		} else if (!key.equals(other.key))
			return false;
		return true;
	}

}
