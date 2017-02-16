/**
 * This class is responsible for storing (a limited number) of data instances and evaluating them 
 * continuously against the known set of events in the knowledge base. 
 * 
 * An event can also be thought of as a set of signals. 
 */
package net.strasnet.kids.streaming;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.semanticweb.owlapi.model.IRI;

import net.strasnet.kids.measurement.DataInstance;
import net.strasnet.kids.measurement.KIDSMeasurementOracle;

/**
 * @author Chris Strasburg
 *
 */
public class StreamingInstanceStoreInstanceLimited extends
		StreamingInstanceStoreAbstractClass implements
		StreamingInstanceStoreInterface {
	
	//TODO: We should really create an object to wrap these together. We may need more, in fact - what about resources?
	//      To answer, the resources we can get from the Data Instances themselves.
	private LinkedBlockingQueue<DataInstance> DataInstanceQueue = null; // The DI queue
	private LinkedBlockingQueue<Set<IRI>> SignalSetQueue = null; // Keep a synchronized list of signals sets paired with DIs.
	private Map<IRI, List<DataInstance>> SignalToDI = null; // Keep a map of individual signals to the DI(s) that produced them.
	private Map<IRI, Integer> FlattenedSignals = null; // Keep a flattened list of all the signals in the pool, and how many DIs reference them.
	private int sizeLimit = 0; // How many DIs we contain.

	private static final Logger logme = LogManager.getLogger(StreamingInstanceStoreInstanceLimited.class.getName());

	/**
	 * 
	 * @param maxSize - The limit on the number of instances this store will ... store.
	 * @param o - The measurement oracle; needed to determine when an event has been matched.
	 */
	public StreamingInstanceStoreInstanceLimited (int maxSize, KIDSStreamingOracle myO){
		super(myO);
		sizeLimit = maxSize;
		DataInstanceQueue = new LinkedBlockingQueue<DataInstance>(maxSize);
		SignalSetQueue = new LinkedBlockingQueue<Set<IRI>>(maxSize);
		FlattenedSignals = new HashMap<IRI, Integer>();
		SignalToDI = new HashMap<IRI, List<DataInstance>>();
		logme.info(String.format("Created new instances limited data store with max size %d.", maxSize));
	}

	@Override
	public synchronized void addStreamingInstance(DataInstance toAdd, Set<IRI> signalsMatched) {
		if (DataInstanceQueue.size() == sizeLimit){
			logme.debug("Size limit reached, removing oldest instance...");
            removeDataInstance();
		}
		addDataInstance(toAdd, signalsMatched);
		logme.debug(String.format("Instance store now contains %d instances.", this.size()));
		
		// When a new data instance is added, we should also check to see if any new signals have caused an event match:
		checkForMatchedEvents();
	}
	
	/*
	 * Given the current set of signals, check to see if there are any new matched events. A few things to note:
	 * 1) If the matched event is new, it must contain one of the most recently added signals;
	 * TODO: Take advantage of the above
	 * 2) An event *could* contain one of the most recently added signals and still have already been alerted (?)
	 */
	private void checkForMatchedEvents() {
		logme.debug("Checking for newly matched events...");
		Set<IRI> matchedEvents = o.getMatchedEvents(this.FlattenedSignals.keySet());
		if (matchedEvents.size() != 0){
			logme.debug(String.format("Current signal set matches %d events.", matchedEvents.size()));
			// TODO: We've matched events, but are they new or have we already processed them?
			for (IRI event : matchedEvents){
				// Create a streaming event:
				
				// We need to know which data instances are associated with the event:
				//  Event -> SignalSet -> DataInstances associated with those signals:
				Set<DataInstance> relatedInstances = new HashSet<DataInstance>();
				Set<IRI> evSigs = o.getSignalsForEvent(event);
				Set<IRI> relatedSignals = new HashSet<IRI>();

				// We need to know which signals matched the event in question:
				for (IRI evSig : evSigs){
					logme.debug(String.format("Checking SignalToDi for %s...", evSig));
					if (SignalToDI.containsKey(evSig)){
						logme.debug("\t...found, adding instance to related instances.");
						relatedSignals.add(evSig);
						List<DataInstance> iList = SignalToDI.get(evSig);
						// TODO: We're assuming only the most recent data instance is relevant; that's
						// probably not true.
						relatedInstances.add(iList.get(iList.size()-1));
					} else {
						logme.debug("\t...*not* found, in which case, how did we match?");
					}
				}
				
				// Get the description of the event (from the oracle):
				String desc = o.getEventDescription(event);
				StreamingEvent e = new StreamingEvent(event, relatedSignals, desc, relatedInstances);
				logme.debug(String.format("Created a new streaming event with %d related signals, and %d related Instances.", 
						                   relatedSignals.size(), 
						                   relatedInstances.size()));
				super.fireAlert(e);
			}
		}
		
	}

	/*
	 * Do the bookkeeping required to add a data instance.
	 */
	private synchronized void addDataInstance(DataInstance toAdd, Set<IRI> signalsMatched) {
		StringBuilder dbugSigList = new StringBuilder();
		dbugSigList.append("Adding new data instance with signals: (");
		for (IRI i : signalsMatched){
			dbugSigList.append(String.format("%s,",i.getFragment()));
		}
		dbugSigList.append(")");
		logme.debug(dbugSigList);

		DataInstanceQueue.add(toAdd);
		SignalSetQueue.add(signalsMatched);
		for (IRI sig : signalsMatched){
			if (!FlattenedSignals.containsKey(sig)){
				FlattenedSignals.put(sig, 0);
			}
			FlattenedSignals.put(sig, FlattenedSignals.get(sig) + 1);
			if (!SignalToDI.containsKey(sig)){
				SignalToDI.put(sig, new LinkedList<DataInstance>());
			}
			SignalToDI.get(sig).add(toAdd);
		}
		
	}

	/*
	 * Perform clean up when data instance needs to be removed.
	 */
	private synchronized void removeDataInstance() {
			try {
				DataInstance diRemoved = DataInstanceQueue.take();
				Set<IRI> sigsRemoved = SignalSetQueue.take();
				for (IRI sig : sigsRemoved){
					int sigRefCount = FlattenedSignals.get(sig) - 1;
					if (sigRefCount == 0){
						FlattenedSignals.remove(sig);
						this.SignalToDI.remove(sig);
					} else {
					    FlattenedSignals.put(sig, FlattenedSignals.get(sig) - 1);
					    List<DataInstance> possiblyRemove = SignalToDI.get(sig);
					    for (int i = 0; i < possiblyRemove.size(); i++){
					    	if (possiblyRemove.get(i).equals( diRemoved)){
					    		possiblyRemove.remove(i);
					    	}
					    }
					}
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
				logme.warn("Interrupted while processing new streaming instance; returning to known state...");
				//TODO: I should do something about this?
			}
		
	}

	@Override
	public int size() {
		return DataInstanceQueue.size();
	}

}
