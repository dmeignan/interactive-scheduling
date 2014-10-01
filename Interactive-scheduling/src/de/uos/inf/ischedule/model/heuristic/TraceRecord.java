/**
 * Copyright 2013, Universitaet Osnabrueck
 * Author: David Meignan
 */
package de.uos.inf.ischedule.model.heuristic;

/**
 * A trace record is a value of a trace at a given time.
 * 
 * @author David Meignan
 * @param <V> the type of value of the record.
 */
public class TraceRecord <V> {

	/**
	 * Elapsed time
	 */
	protected long elapsedNanoTime;
	
	/**
	 * Value of the record
	 */
	protected V value;
	
	/**
	 * Creates a record with an elapsed time and a value.
	 * 
	 * @param elapsedNanoTime the elapsed time to the record in nanoseconds.
	 * @param value the value of the record.
	 * @throws IllegalArgumentException if the elapsed time is negative or the
	 * value is <code>null</code>.
	 */
	public TraceRecord(long elapsedNanoTime, V value) {
		if (elapsedNanoTime < 0 || value == null)
			throw new IllegalArgumentException();
		this.elapsedNanoTime = elapsedNanoTime;
		this.value = value;
	}
	
	/**
	 * Creates a record with an elapsed time and a value.
	 * 
	 * @param startTraceNano the start time of the trace in nanoseconds.
	 * @param currentTimeNano the time of the trace in nanoseconds.
	 * @param value the value of the record.
	 * @throws IllegalArgumentException if the start time is greater than the current
	 * time or the value is <code>null</code>.
	 */
	public TraceRecord(long startTraceNano, long currentTimeNano, V value) {
		if (startTraceNano > currentTimeNano || value == null)
			throw new IllegalArgumentException();
		this.elapsedNanoTime = currentTimeNano-startTraceNano;
		this.value = value;
	}
	
	/**
	 * Returns the value of the record.
	 * 
	 * @return the value of the record.
	 */
	public V getValue() {
		return value;
	}
	
	/**
	 * Returns the time of the record in nanoseconds.
	 * 
	 * @return the time of the record in nanoseconds.
	 */
	public long getElapsedNanoTime() {
		return elapsedNanoTime;
	}
	
	/**
	 * Returns the time of the record in seconds.
	 * 
	 * @return the time of the record in seconds.
	 */
	public int getElapsedTime() {
		return (int) (elapsedNanoTime/1000000000L);
	}
}
