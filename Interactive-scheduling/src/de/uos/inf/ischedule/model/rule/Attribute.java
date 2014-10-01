/**
 * Copyright 2012, Universitat Osnabrueck
 * Author: David Meignan
 */
package de.uos.inf.ischedule.model.rule;

import java.util.Iterator;

import de.uos.inf.ischedule.model.Solution;

/**
 * The abstract class <code>Attribute</code> is the superclass of attributes
 * that characterize assignments.
 * 
 * @author David Meignan
 */
public abstract class Attribute<E extends Comparable<E>> 
		implements Comparable<Attribute<E>>, Iterable<E> {

	/**
	 * Set of attribute's types.
	 */
	public static enum ATTRIBUTE_TYPE {
		Nominal, // Finite set of values without order
		Ordinal, // Finite set of values with order
		Boolean, // Boolean attribute
		Numeric; // Numeric attribute
	}
	
	/**
	 * Returns the string identifier of the attribute. This string must
	 * be unique for the set of attributes that are used together.
	 * This identifier is used for comparing attributes with
	 * <code>compareTo</code> and <code>equals</code> methods, and for
	 * <code>hashCode</code> method.
	 * 
	 * @return the string that identifies the attribute.
	 */
	public abstract String getKey();
	
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj == null || !(obj instanceof Attribute))
			return false;
		return getKey().equals(((Attribute<?>) obj).getKey());
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return getKey().hashCode();
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(Attribute<E> o) {
		return getKey().compareTo(o.getKey());
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return getKey();
	}

	/**
	 * Returns the type of the attribute.
	 * 
	 * @return the type of the attribute.
	 */
	public abstract ATTRIBUTE_TYPE getType();
	
	/**
	 * Returns the value of the attribute for the specified assignment.
	 * 
	 * @param dayIndex the day index of the assignment.
	 * @param employeeIndex the employee index of the assignment.
	 * @param solution the solution described.
	 * @param isPreferred if the assignment is preferred.
	 * @param isUnwanted if the assignment is unwanted.
	 * @return the value of the attribute for the specified assignment.
	 */
	public abstract E getValue(int dayIndex, int employeeIndex, Solution solution,
			boolean isPreferred, boolean isUnwanted);
	
	/**
	 * Return a description of the attribute in the ARFF format.
	 *  
	 * @return a description of the attribute in the ARFF format.
	 */
	public String getARFFDescription() {
		StringBuffer desc = new StringBuffer();
		desc.append("@ATTRIBUTE ");
		desc.append(getKey());
		if (getType() == ATTRIBUTE_TYPE.Numeric) {
			desc.append(" NUMERIC");
		} else {
			desc.append(" { ");
			Iterator<E> vIt = iterator();
			while(vIt.hasNext()) {
				desc.append("\"");
				desc.append(vIt.next().toString());
				desc.append("\"");
				if (vIt.hasNext())
					desc.append(", ");
			}
			desc.append(" } ");
		}
		return desc.toString();
	}
}
