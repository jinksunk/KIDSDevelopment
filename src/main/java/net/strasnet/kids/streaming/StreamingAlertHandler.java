/**
 * The streaming alert handler is responsible for dispatching alerts when a new event is seen. The interface defines a method,
 * handleEvent(...) that is called when a new event is detected by a StreamingInstanceStoreInterface.
 * 
 */
package net.strasnet.kids.streaming;

import java.util.Map;

import org.semanticweb.owlapi.model.IRI;

/**
 * @author Chris Strasburg
 *
 */
public interface StreamingAlertHandler {
    public void handleEvent(StreamingEvent e);
}
