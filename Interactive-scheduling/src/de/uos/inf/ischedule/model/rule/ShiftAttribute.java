/**
 * Copyright 2013-2014, Universitaet Osnabrueck
 * Author: David Meignan
 */
package de.uos.inf.ischedule.model.rule;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;

import de.uos.inf.ischedule.model.Shift;
import de.uos.inf.ischedule.model.ShiftSchedulingProblem;
import de.uos.inf.ischedule.model.Solution;

/**
 * Attribute for shifts.
 * 
 * @author David Meignan
 */
public class ShiftAttribute extends Attribute<String> {

	/**
	 * List of shift label.
	 */
	private HashMap<Shift, String> shiftValues;
	private ArrayList<String> allValues;
	
	/**
	 * Key of the attribute.
	 */
	private String key = "shift";
	
	/**
	 * Specific value for no assignment.
	 */
	public static String NO_ASSIGNMENT_SHIFT_VALUE = "No assignment";
	
	/**
	 * Specific value for out-of-bounds assignment.
	 */
	public static String OUT_OF_BOUNDS_ASSIGNMENT_SHIFT_VALUE = "Out of bounds";
	
	/**
	 * Creates the attribute for a given problem.
	 * 
	 * @param problem the problem.
	 */
	public ShiftAttribute(ShiftSchedulingProblem problem) {
		// Creates the values
		shiftValues = new HashMap<Shift, String>();
		allValues = new ArrayList<String>();
		allValues.add(NO_ASSIGNMENT_SHIFT_VALUE);
		for (Shift shift: problem.shifts()) {
			String v = shift.getLabel()+" (ID: "+shift.getId()+")";
			shiftValues.put(shift, v);
			allValues.add(v);
		}
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Iterable#iterator()
	 */
	@Override
	public Iterator<String> iterator() {
		return Collections.unmodifiableList(allValues).iterator();
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
		Shift assignment = solution.assignments.get(dayIndex).get(employeeIndex);
		if (assignment == null)
			return NO_ASSIGNMENT_SHIFT_VALUE;
		return shiftValues.get(assignment);
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
		ShiftAttribute other = (ShiftAttribute) obj;
		if (key == null) {
			if (other.key != null)
				return false;
		} else if (!key.equals(other.key))
			return false;
		return true;
	}

}
