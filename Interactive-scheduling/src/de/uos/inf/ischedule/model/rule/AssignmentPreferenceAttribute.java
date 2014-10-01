/**
 * Copyright 2013-2014, Universitaet Osnabrueck
 * Author: David Meignan
 */
package de.uos.inf.ischedule.model.rule;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

import de.uos.inf.ischedule.model.Solution;

/**
 * Attribute for assignment preferences.
 * 
 * @author David Meignan
 */
public class AssignmentPreferenceAttribute extends Attribute<String> {

	/**
	 * List of employee label.
	 */
	private ArrayList<String> values;
	
	/**
	 * Key of the attribute.
	 */
	private String key = "assignment-preference";
	
	public static String PREFERRED_ASSIGNMENT_VALUE = "Preferred";
	public static String UNSATISFACTORY_ASSIGNMENT_VALUE = "Not satisfactory";
	public static String WITHOUT_PREFERENCE_ASSIGNMENT_VALUE = "Without preference";
	
	/**
	 * Creates the attribute for a given problem.
	 * 
	 * @param problem the problem.
	 */
	public AssignmentPreferenceAttribute() {
		// Creates the values
		values = new ArrayList<String>();
		values.add(PREFERRED_ASSIGNMENT_VALUE);
		values.add(UNSATISFACTORY_ASSIGNMENT_VALUE);
		values.add(WITHOUT_PREFERENCE_ASSIGNMENT_VALUE);
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
		if (isPreferred)
			return PREFERRED_ASSIGNMENT_VALUE;
		if (isUnwanted)
			return UNSATISFACTORY_ASSIGNMENT_VALUE;
		return WITHOUT_PREFERENCE_ASSIGNMENT_VALUE;
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
		AssignmentPreferenceAttribute other = (AssignmentPreferenceAttribute) obj;
		if (key == null) {
			if (other.key != null)
				return false;
		} else if (!key.equals(other.key))
			return false;
		return true;
	}

}
