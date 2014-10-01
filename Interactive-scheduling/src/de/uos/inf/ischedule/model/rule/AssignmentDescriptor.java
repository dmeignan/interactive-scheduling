/**
 * Copyright 2013-2014, Universitaet Osnabrueck
 * Author: David Meignan
 */
package de.uos.inf.ischedule.model.rule;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import de.uos.inf.ischedule.model.AssignmentPreference;
import de.uos.inf.ischedule.model.AssignmentPreferenceConstraint;
import de.uos.inf.ischedule.model.AssignmentPreferenceConstraint.AssignmentPreferenceConstraintEvaluator;
import de.uos.inf.ischedule.model.Shift;
import de.uos.inf.ischedule.model.ShiftSchedulingProblem;
import de.uos.inf.ischedule.model.Solution;

/**
 * Assignment descriptor.
 * 
 * @author David Meignan
 */
public class AssignmentDescriptor {

	/**
	 * Instances of assignment preferences
	 */
	private ArrayList<AssignmentInstance> instances;
	
	/**
	 * Assignment preferences related to instances (for checking new preferences)
	 */
	private ArrayList<AssignmentPreference> preferences;
	
	/**
	 * List of attributes
	 */
	private ArrayList<Attribute<?>> attributes;
	
	/**
	 * Creates a description for a problem.
	 * 
	 * @param problem the problem.
	 */
	public AssignmentDescriptor(ShiftSchedulingProblem problem) {
		instances = new ArrayList<AssignmentInstance>();
		preferences = new ArrayList<AssignmentPreference>();
		
		// Creates the list of attributes
		attributes = new ArrayList<Attribute<?>>();
		attributes.add(new EmployeeAttribute(problem));
		attributes.add(new ContractAttribute(problem));
		attributes.add(new ShiftAttribute(problem));
		attributes.add(new PreviousShiftAttribute(problem));
		attributes.add(new NextShiftAttribute(problem));
		attributes.add(new DayOfWeekAttribute(problem));
		attributes.add(new RequestedDayOffAttribute());
		attributes.add(new NumberOfAssignmentsAttribute());
		attributes.add(new ConsecutiveWorkingDaysAttribute());
		attributes.add(new ConsecutiveDaysOffAttribute());
		attributes.add(new ConsecutiveWorkingWeekendsAttribute(problem));
		attributes.add(new AssignmentPreferenceAttribute());
	}
	
	/**
	 * Add to the description the preferences given by the constraint.
	 * 
	 * @param assignmentPreferenceConstraint the assignment preferences.
	 * @param problem the problem.
	 * @param solution the solution on which the preferences have been defined.
	 * @param describeNoPreferenceAssignments if <code>true</code> the assignments
	 * with no preferences will be described (! redundancy for the description of
	 * assignments with no preference is not checked. These assignments should be
	 * added only at the very end of the process).
	 */
	public void addPreferenceDescription(
			AssignmentPreferenceConstraint assignmentPreferenceConstraint,
			ShiftSchedulingProblem problem,
			Solution solution,
			boolean describeNoPreferenceAssignments) {
		AssignmentPreferenceConstraintEvaluator constraintEvaluator = 
				(AssignmentPreferenceConstraintEvaluator) 
				assignmentPreferenceConstraint.getEvaluator(problem);
		// Check all assignments
		for (int dayIndex=0; dayIndex<solution.assignments.size(); dayIndex++) {
			for (int employeeIndex=0; employeeIndex<solution.employees.size(); 
					employeeIndex++) {
				Shift assignment = solution.assignments.get(dayIndex).get(employeeIndex);
				// Check special case to add unwanted shift
				if (constraintEvaluator.hasPreferredAssignment(dayIndex, employeeIndex) &&
						!constraintEvaluator.hasUnwantedAssignment(dayIndex, employeeIndex)) {
					if (!constraintEvaluator.isPreferredAssignment
							(dayIndex, employeeIndex, assignment)) {
						AssignmentPreference specialPref =new AssignmentPreference(
								false,
								assignment,
								solution.employees.get(employeeIndex),
								dayIndex);
						if (!preferences.contains(specialPref)) { // TODO check contains
							// Add the preference description
							preferences.add(specialPref);
							instances.add(new AssignmentInstance(
									attributes,
									dayIndex, employeeIndex, solution, 
									specialPref.isPreferred(), 
									!specialPref.isPreferred()
									));
						}
					}
				}
				// Check assignment with no preference
				if (describeNoPreferenceAssignments &&
						!constraintEvaluator.hasPreferredAssignment(dayIndex, employeeIndex) &&
						!constraintEvaluator.hasUnwantedAssignment(dayIndex, employeeIndex)) {
					// Add the description of the assignment
					instances.add(new AssignmentInstance(
							attributes,
							dayIndex, employeeIndex, solution, 
							false, 
							false
							));
				}
			}
		}
		// Check preferences
		for (AssignmentPreference preferred: 
			assignmentPreferenceConstraint.preferredAssignments()) {
			if (!preferences.contains(preferred)) { // TODO check contains
				// Add the preference description
				preferences.add(preferred);
				instances.add(new AssignmentInstance(
						attributes,
						preferred.getDayIndex(),
						solution.employees.indexOf(preferred.getEmployee()),
						solution, 
						preferred.isPreferred(), 
						!preferred.isPreferred()
						));
			}
		}
		for (AssignmentPreference unwanted: 
			assignmentPreferenceConstraint.unwantedAssignments()) {
			if (!preferences.contains(unwanted)) { // TODO check contains
				// Add the preference description
				preferences.add(unwanted);
				instances.add(new AssignmentInstance(
						attributes,
						unwanted.getDayIndex(),
						solution.employees.indexOf(unwanted.getEmployee()),
						solution, 
						unwanted.isPreferred(), 
						!unwanted.isPreferred()
						));
			}
		}
	}

	/**
	 * Returns the entire ARFF description.
	 * 
	 * @return the entire ARFF description.
	 */
	public String getARFFHeader() {
		StringBuffer desc = new StringBuffer();
		// Attributes
		desc.append("%%\n% Description of assignment preferences\n%%\n\n");
		desc.append("@RELATION assignment\n\n");
		desc.append("%%\n% Attributes\n%%\n\n");
		for (Attribute<?> attribute: attributes) {
			desc.append(attribute.getARFFDescription());
			desc.append("\n");
		}
		// Data
		desc.append("\n%%\n% Instances\n%%\n\n@DATA\n\n");
		return desc.toString();
	}
	
	/**
	 * Returns an iterator over the instances.
	 * 
	 * @return an iterator over the instances.
	 */
	public Iterator<AssignmentInstance> instanceIterator() {
		return Collections.unmodifiableList(instances).iterator();
	}
	
	/**
	 * Returns the list of attributes (unmodifiable).
	 * 
	 * @return the list of attributes.
	 */
	public List<Attribute<?>> getAttributes() {
		return Collections.unmodifiableList(attributes);
	}
}
