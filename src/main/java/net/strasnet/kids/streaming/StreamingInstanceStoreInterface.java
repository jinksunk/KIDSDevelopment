/**
 * The Streaming Instance Store will store instances identified as potentially event related by streaming detectors. It will be responsible
 * for generating events when the set of signals for a possible event have been seen.
 */
package net.strasnet.kids.streaming;

import java.util.Set;

import org.semanticweb.owlapi.model.IRI;

import net.strasnet.kids.measurement.DataInstance;

/**
 * @author Chris Strasburg
 *
 */
public interface StreamingInstanceStoreInterface {
	
	/**
	 * A method which will allow instances to be added:
	 * @param toAdd - The new data instance to be added, along with the signals it triggered.
	 */
	public void addStreamingInstance(DataInstance toAdd, Set<IRI> signalsMatched);
	
	/** 
	 * An 'add listener' method, which will allow new detected events to be alerted
	 * @param h - An implementation of the StreamingAlertHandler class which implements the
	 *            expected callback for when an event is detected.
	 */
	public void registerStreamingAlertHandler(StreamingAlertHandler h);
	
	/**
	 * 
	 * @return - The number of data instances currently held in the pool.
	 */
	public int size();
}
