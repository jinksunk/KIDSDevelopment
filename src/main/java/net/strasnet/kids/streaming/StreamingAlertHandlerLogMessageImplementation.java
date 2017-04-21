/**
 * This class implements a simple handler that generates an info log message when an event is seen. 
 */
package net.strasnet.kids.streaming;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

/**
 * @author Chris Strasburg
 *
 */
public class StreamingAlertHandlerLogMessageImplementation implements
		StreamingAlertHandler {
	
	private static final Logger logme = LogManager.getLogger(StreamingAlertHandlerLogMessageImplementation.class.getName());
	
	/**
	 * Default constructor: all the work is done in handleEvent...
	 */
	public StreamingAlertHandlerLogMessageImplementation (){
		super();
	}

	/* (non-Javadoc)
	 * @see net.strasnet.kids.streaming.StreamingAlertHandler#handleEvent(net.strasnet.kids.streaming.StreamingEvent)
	 */
	@Override
	/**
	 * Generates an informational log message about the event.
	 */
	public void handleEvent(StreamingEvent e) {
		logme.info("Handling event: " + e.getEventIRI().getFragment().toString());
	}

}
