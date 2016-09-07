/**
 * 
 */
package net.strasnet.kids.streaming;

import java.util.HashSet;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.semanticweb.owlapi.model.IRI;

/**
 * @author Chris Strasburg
 *
 */
public abstract class StreamingInstanceStoreAbstractClass implements
		StreamingInstanceStoreInterface {
	
	private Set<StreamingAlertHandler> registeredHandlers = null;
	private KIDSNaiveResponseSelectionEngine rse = null;
	private static final Logger logme = LogManager.getLogger(StreamingInstanceStoreAbstractClass.class.getName());
	private StreamingAlertHandlerFactory sahf = null;
	KIDSStreamingOracle o = null;
	KIDSNaiveResponseSelectionEngine kre = null;
	
	public StreamingInstanceStoreAbstractClass (KIDSStreamingOracle myO){
		o = myO;
		sahf = new StreamingAlertHandlerFactory(o);
		kre = new KIDSNaiveResponseSelectionEngine(o);
	}


	/* (non-Javadoc)
	 * @see net.strasnet.kids.streaming.StreamingInstanceStoreInterface#registerStreamingAlertHandler(net.strasnet.kids.streaming.StreamingInstance)
	 */
	@Override
	/**
	 * A thread-safe (synchronized) register method for new alert handlers.
	 * @param h - an implementation of the 
	 */
	public synchronized void registerStreamingAlertHandler(StreamingAlertHandler h){
		if (registeredHandlers == null){
			registeredHandlers = new HashSet<StreamingAlertHandler>();
		}
		registeredHandlers.add(h);
	}
	
	/**
	 * This implementaiton will simply pass the fired event on to each registered handler.
	 * @param e - the event that was identified.
	 */
	public synchronized void fireAlert(StreamingEvent e){
		logme.info("Event fired: " + e.getEventIRI().getFragment().toString());
		IRI ourResponse = kre.selectResponse(e);
		StreamingAlertHandler s = sahf.getResponseClass(ourResponse);
		s.handleEvent(e);
		/*
		if (registeredHandlers == null){
			registeredHandlers = new HashSet<StreamingAlertHandler>();
		}
		for (StreamingAlertHandler h : registeredHandlers){
			h.handleEvent(e);
		}
		*/
	}

}
