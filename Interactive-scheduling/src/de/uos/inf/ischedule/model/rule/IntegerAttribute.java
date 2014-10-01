/**
 * Copyright 2013-2014, Universitaet Osnabrueck
 * Author: David Meignan
 */
package de.uos.inf.ischedule.model.rule;

import java.util.Iterator;


/**
 * Integer attribute. The values of the attribute are integers.
 * 
 * @author David Meignan
 */
public abstract class IntegerAttribute extends Attribute<Integer> {

	/* (non-Javadoc)
	 * @see java.lang.Iterable#iterator()
	 */
	@Override
	public Iterator<Integer> iterator() {
		throw new UnsupportedOperationException("Cannot iterate on numeric type.");
	}

	/* (non-Javadoc)
	 * @see de.uos.inf.ischedule.model.rule.Attribute#getType()
	 */
	@Override
	public ATTRIBUTE_TYPE getType() {
		return ATTRIBUTE_TYPE.Numeric;
	}

}
