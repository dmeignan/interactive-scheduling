/**
 * Copyright 2013-2014, Universitaet Osnabrueck
 * Author: David Meignan
 */
package de.uos.inf.ischedule.model.rule;

import java.util.List;
import java.util.TreeMap;

import de.uos.inf.ischedule.model.Solution;
import de.uos.inf.ischedule.model.rule.Attribute.ATTRIBUTE_TYPE;

/**
 * Set of attribute values for a specific assignment and preference.
 * 
 * @author David Meignan
 */
public class AssignmentInstance {

	/**
	 * Values
	 */
	TreeMap<Attribute<?>,Object> values;
	
	/**
	 * Creates an instance that describes an assignment.
	 */
	public AssignmentInstance(
			List<Attribute<?>> attributes,
			int dayIndex, int employeeIndex, Solution solution,
			boolean isPreferred, boolean isUnwanted) {
		values = new TreeMap<Attribute<?>,Object>();
		for (Attribute<?> attribute: attributes) {
			values.put(attribute, attribute.getValue(
					dayIndex, employeeIndex, solution, isPreferred, isUnwanted));
		}
	}
	
	/**
	 * Returns the value of an attribute.
	 * 
	 * @param attribute the attribute for which the value has to be returned.
	 * @return the value of the attribute.
	 */
	public Object getAttributeValue(Attribute<?> attribute) {
		return values.get(attribute);
	}
	
	/**
	 * Returns the description of the instance using the ARFF format.
	 * 
	 * @param attributes the list of attribute to be described.
	 * @return the description of the instance.
	 */
	public String getARFFDescription(List<Attribute<?>> attributes) {
		StringBuffer desc = new StringBuffer();
		// Attributes
		for (int attIdx=0; attIdx<attributes.size(); attIdx++) {
			Object value = getAttributeValue(attributes.get(attIdx));
			if (attributes.get(attIdx).getType() == ATTRIBUTE_TYPE.Numeric) {
				desc.append(value.toString());
			} else {
				desc.append("\"");
				desc.append(value.toString());
				desc.append("\"");
			}
			if (attIdx < attributes.size()-1)
				desc.append(", ");
		}
		return desc.toString();
	}
}
