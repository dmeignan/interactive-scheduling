/**
 * Copyright 2013, Universitaet Osnabrueck
 * Author: David Meignan
 */
package de.uos.inf.ischedule.model.heuristic;

import java.util.AbstractList;
import java.util.LinkedList;

/**
 * Trace of a run that contains a single type of trace record.
 * 
 * @author David Meignan
 * @param <V> the type of value of trace records.
 */
public class Trace <V> extends AbstractList<TraceRecord<V>> {

	/**
	 * Label of the trace.
	 */
	private String traceLabel;
	
	/**
	 * Set of trace records.
	 */
	private LinkedList<TraceRecord<V>> records;
	
	/**
	 * Creates a new trace.
	 * 
	 * @param label the label of the trace.
	 */
	public Trace(String label) {
		this.traceLabel = label;
		records = new LinkedList<TraceRecord<V>>();
	}
	
	/**
	 * Returns the label of the trace.
	 * 
	 * @return the label of the trace.
	 */
	public String getTraceLabel() {
		return traceLabel;
	}
	
	/**
	 * Returns the last record added to the trace.
	 * 
	 * @return the last record added to the trace. Return <code>null</code>
	 * if no record have been yet added to the trace.
	 */
	public TraceRecord<V> getLastRecord() {
		if (records.isEmpty())
			return null;
		return records.getLast();
	}
	
	/**
	 * Returns the last record at a given time in seconds.
	 * 
	 * @param time the time in seconds.
	 * @return the last record at a given time in seconds. Returns <code>null</code>
	 * If no record exists before or at the given time.
	 */
	public TraceRecord<V> getLastRecordAt(int time) {
		return getLastRecordAtNano(((long)time)*1000000000L);
	}
	
	/**
	 * Returns the last record at a given time in nanoseconds.
	 * 
	 * @param nanoTime the time in nanoseconds.
	 * @return the last record at a given time in nanoseconds. Returns <code>null</code>
	 * If no record exists before or at the given time.
	 */
	public TraceRecord<V> getLastRecordAtNano(long nanoTime) {
		if (records.isEmpty())
			return null;
		TraceRecord<V> last = null;
		for (TraceRecord<V> r: records) {
			if (r.getElapsedNanoTime() <= nanoTime) {
				if (last == null) {
					last = r;
				} else if (last.getElapsedNanoTime() < r.getElapsedNanoTime()) {
					last = r;
				}
			}
		}
		return last;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("label=");
		builder.append(traceLabel);
		builder.append(", records=");
		builder.append(records);
		return builder.toString();
	}

	/**
	 * Returns the value of the record before or at the given time in seconds.
	 * Return <code>null</code> if no record have been yet added to the trace at
	 * the given time.
	 * 
	 * @param time the time.
	 * @return the value of the record before or at the given time in seconds.
	 * Return <code>null</code> if no record have been yet added to the trace at
	 * the given time.
	 */
	public V getLastValueAt(int time) {
		return getLastValueAtNano(((long)time)*1000000000L);
	}
	
	/**
	 * Returns the value of the record before or at the given time in seconds.
	 * Return <code>null</code> if no record have been yet added to the trace at
	 * the given time.
	 * 
	 * @param time the time.
	 * @return the value of the record before or at the given time in seconds.
	 * Return <code>null</code> if no record have been yet added to the trace at
	 * the given time.
	 */
	public V getLastValueAtNano(long nanoTime) {
		if (records.isEmpty())
			return null;
		TraceRecord<V> last = null;
		for (TraceRecord<V> r: records) {
			if (r.getElapsedNanoTime() <= nanoTime) {
				if (last == null) {
					last = r;
				} else if (last.getElapsedNanoTime() < r.getElapsedNanoTime()) {
					last = r;
				}
			}
		}
		return (last==null)?(null):(last.getValue());
	}

	/* (non-Javadoc)
	 * @see java.util.AbstractCollection#size()
	 */
	@Override
	public int size() {
		return records.size();
	}

	/* (non-Javadoc)
	 * @see java.util.AbstractList#get(int)
	 */
	@Override
	public TraceRecord<V> get(int index) {
		return records.get(index);
	}

	/* (non-Javadoc)
	 * @see java.util.AbstractList#set(int, java.lang.Object)
	 */
	@Override
	public TraceRecord<V> set(int index, TraceRecord<V> element) {
		throw new UnsupportedOperationException();
	}
	

	/* (non-Javadoc)
	 * @see java.util.AbstractList#remove(int)
	 */
	@Override
	public TraceRecord<V> remove(int index) {
		throw new UnsupportedOperationException();
	}

	/* (non-Javadoc)
	 * @see java.util.AbstractList#add(java.lang.Object)
	 */
	@Override
	public boolean add(TraceRecord<V> e) {
		return records.add(e);
	}

	/* (non-Javadoc)
	 * @see java.util.AbstractList#add(int, java.lang.Object)
	 */
	@Override
	public void add(int index, TraceRecord<V> element) {
		records.add(index, element);
	}

}
