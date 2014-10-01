/**
 * Copyright 2013-2014, Universitaet Osnabrueck
 * Author: David Meignan
 */
package de.uos.inf.ischedule.model.rule;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;

import de.uos.inf.ischedule.model.Contract;
import de.uos.inf.ischedule.model.ShiftSchedulingProblem;
import de.uos.inf.ischedule.model.Solution;

/**
 * Attribute for contracts.
 * 
 * @author David Meignan
 */
public class ContractAttribute extends Attribute<String> {

	/**
	 * List of contract label.
	 */
	private HashMap<Contract, String> values;
	
	/**
	 * Key of the attribute.
	 */
	private String key = "contract";
	
	/**
	 * Creates the attribute for a given problem.
	 * 
	 * @param problem the problem.
	 */
	public ContractAttribute(ShiftSchedulingProblem problem) {
		// Creates the values
		values = new HashMap<Contract, String>();
		for (Contract contract: problem.contracts()) {
			values.put(contract, contract.getName()+
					" (ID: "+contract.getID()+")");
		}
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Iterable#iterator()
	 */
	@Override
	public Iterator<String> iterator() {
		return Collections.unmodifiableCollection(values.values()).iterator();
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
		return values.get(solution.employees.get(employeeIndex).getContract());
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
		ContractAttribute other = (ContractAttribute) obj;
		if (key == null) {
			if (other.key != null)
				return false;
		} else if (!key.equals(other.key))
			return false;
		return true;
	}

}
