package net.strasnet.kids.streaming;

import java.util.List;
import java.util.Set;

import org.semanticweb.owlapi.model.IRI;

/**
 * This interface defines the methods for use in instantiating streaming mode detectors in KIDS. It provides a method,
 * runDetector which, given a (set of) signals and an interface / handle to monitor, will start a thread to continually
 * evaluate instances.
 *  
 * @author Chris Strasburg
 *
 */
public interface StreamingDetectorInterface {
	
	/**
	 * This interface defines will start a thread running, monitoring the output of a detector for detected signals, and keeping track
	 * of the ones that are seen. When the signal queue matches, within the correlation constraints, the signal profile for a
	 * known event, an 'alert' will be produced.
	 * 
	 * In order to do this, a number of threads equal to the signal set size will need to be spawned, and each will monitor for
	 * a single signal. The signal match store will need to determine which signals match which instances.
	 * 
	 * The constructor for implementing classes should include: 
	 * 
	 * @param monitorPoint - A specification of a file handle, network socket, or other detector specific monitoring
	 * point to monitor instances from.
	 * @param signals - A list of signals to detect.
	 */

}
