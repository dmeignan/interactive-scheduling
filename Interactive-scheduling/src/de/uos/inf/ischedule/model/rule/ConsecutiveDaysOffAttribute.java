/**
 * Copyright 2013-2014, Universitaet Osnabrueck
 * Author: David Meignan
 */
package de.uos.inf.ischedule.model.rule;

import de.uos.inf.ischedule.model.Solution;

/**
 * Attribute for the number of consecutive days off.
 * 
 * @author David Meignan
 */
public class ConsecutiveDaysOffAttribute extends IntegerAttribute {

	/**
	 * Key of the attribute.
	 */
	private String key = "nb-consecutive-days-off";
	
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
		int consecutive = 0;
		// Assignment described
		if (solution.assignments.get(dayIndex).get(employeeIndex) != null)
			return new Integer(consecutive);
		consecutive++;
		// Previous assignments
		for (int i=dayIndex-1; i>=0; i--) {
			if (solution.assignments.get(i).get(employeeIndex) != null)
				break;
			consecutive++;
		}
		// Next assignments
		for (int i=dayIndex+1; i<solution.assignments.size(); i++) {
			if (solution.assignments.get(i).get(employeeIndex) != null)
				break;
			consecutive++;
		}
		return new Integer(consecutive);
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
		ConsecutiveDaysOffAttribute other = (ConsecutiveDaysOffAttribute) obj;
		if (key == null) {
			if (other.key != null)
				return false;
		} else if (!key.equals(other.key))
			return false;
		return true;
	}

}
