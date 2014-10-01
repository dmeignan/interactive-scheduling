/**
 * Copyright 2013, Universitaet Osnabrueck
 * Author: David Meignan
 */
package de.uos.inf.ischedule.model.heuristic;

import de.uos.inf.ischedule.model.Shift;

/**
 * A remove-replace move represents a modification of assignments in 
 * a solution. The move consists in removing the shift-slot from its origin
 * (assigned employee or list of unassigned shift-slots) and assign the
 * targeted employee to the shift slot.
 * 
 * @author David Meignan
 *
 */
public class RemoveReplaceMove {

	/**
	 * Index of the origin of the shift-slot. An index of
	 * <code>-1</code> corresponds to the list of unassigned
	 * shift-slots.
	 */
	protected int originEmployeeIndex;
	
	/**
	 * Index of the target of the shift-slot. An index of
	 * <code>-1</code> corresponds to the list of unassigned
	 * shift-slots for removing an assigned shift-slot.
	 */
	protected int targetEmployeeIndex;
	
	/**
	 * The shift-slot involved in the move.
	 */
	protected Shift shiftSlot;
	
	/**
	 * The day-index of the move.
	 */
	protected int dayIndex;

	/**
	 * Constructs a remove-replace move.
	 * 
	 * @param originEmployeeIndex the origin of the shift slot. Value <code>-1</code>
	 * corresponds to the list of unassigned shift-slots.
	 * @param targetEmployeeIndex the target of the shift slot. Value <code>-1</code>
	 * corresponds to the list of unassigned shift-slots.
	 * @param shiftSlot the shift-slot moved.
	 * @param dayIndex the day-index of the move.
	 */
	public RemoveReplaceMove(int originEmployeeIndex, int targetEmployeeIndex,
			Shift shiftSlot, int dayIndex) {
		this.originEmployeeIndex = originEmployeeIndex;
		this.targetEmployeeIndex = targetEmployeeIndex;
		this.shiftSlot = shiftSlot;
		this.dayIndex = dayIndex;
	}

	/**
	 * Returns the origin of the shift slot. Value <code>-1</code>
	 * corresponds to the list of unassigned shift-slots.
	 * 
	 * @return the origin of the shift slot.
	 */
	public int getOriginEmployeeIndex() {
		return originEmployeeIndex;
	}

	/**
	 * Sets the origin of the shift slot. Value <code>-1</code>
	 * corresponds to the list of unassigned shift-slots.
	 * 
	 * @param originEmployeeIndex the origin of the shift slot to set.
	 * @throws IllegalArgumentException if the value is lower than <code>-1</code>.
	 */
	public void setOriginEmployeeIndex(int originEmployeeIndex) {
		if (originEmployeeIndex < -1)
			throw new IllegalArgumentException();
		this.originEmployeeIndex = originEmployeeIndex;
	}

	/**
	 * Returns the target of the shift slot. Value <code>-1</code>
	 * corresponds to the list of unassigned shift-slots.
	 * 
	 * @return the target of the shift slot.
	 */
	public int getTargetEmployeeIndex() {
		return targetEmployeeIndex;
	}

	/**
	 * Sets the target of the shift slot. Value <code>-1</code>
	 * corresponds to the list of unassigned shift-slots.
	 * 
	 * @param targetEmployeeIndex the target of the shift slot to set.
	 * @throws IllegalArgumentException if the value is lower than <code>-1</code>.
	 */
	public void setTargetEmployeeIndex(int targetEmployeeIndex) {
		if (targetEmployeeIndex < -1)
			throw new IllegalArgumentException();
		this.targetEmployeeIndex = targetEmployeeIndex;
	}

	/**
	 * Returns the shift slot moved.
	 * 
	 * @return the shift slot moved.
	 */
	public Shift getShiftSlot() {
		return shiftSlot;
	}

	/**
	 * Set the shift slot moved.
	 * 
	 * @param shiftSlot the shift slot moved.
	 */
	public void setShiftSlot(Shift shiftSlot) {
		this.shiftSlot = shiftSlot;
	}

	/**
	 * Returns the day index of the move.
	 * 
	 * @return the day index of the move.
	 */
	public int getDayIndex() {
		return dayIndex;
	}

	/**
	 * Sets the day index of the move.
	 * 
	 * @param dayIndex the day index of the move.
	 * @throws IllegalArgumentException if the value is lower than <code>0</code>.
	 */
	public void setDayIndex(int dayIndex) {
		if (targetEmployeeIndex < 0)
			throw new IllegalArgumentException();
		this.dayIndex = dayIndex;
	}
	
}
