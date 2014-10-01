/**
 * Copyright 2013-2014, Universitaet Osnabrueck
 * Author: David Meignan
 */
package de.uos.inf.ischedule.model.rule;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

/**
 * Boolean attribute. The values of the attributes are <code>true</code> and
 * <code>false</code>.
 * 
 * @author David Meignan
 */
public abstract class BooleanAttribute extends Attribute<Boolean> {

	/**
	 * List of values.
	 */
	protected ArrayList<Boolean> booleanValues;
	
	/**
	 * Constructs the attribute with the list of values.
	 */
	public BooleanAttribute() {
		booleanValues = new ArrayList<Boolean>();
		booleanValues.add(true);
		booleanValues.add(false);
	}

	/* (non-Javadoc)
	 * @see java.lang.Iterable#iterator()
	 */
	@Override
	public Iterator<Boolean> iterator() {
		return Collections.unmodifiableList(booleanValues).iterator();
	}

	/* (non-Javadoc)
	 * @see de.uos.inf.ischedule.model.rule.Attribute#getType()
	 */
	@Override
	public ATTRIBUTE_TYPE getType() {
		return ATTRIBUTE_TYPE.Boolean;
	}

}
