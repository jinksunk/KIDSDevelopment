/**
 * The streaming instance is an encapsulating class which exposes streaming-specific information about
 * normal instances. 
 * 
 * One characteristic of a streaming instance is that we need to keep track of the signals this instance matched when it was
 * seen (and possibly the detector at some point).
 */

package net.strasnet.kids.streaming;

import java.util.List;
import java.util.Map;
import java.util.Set;

import net.strasnet.kids.detectors.UnimplementedIdentifyingFeatureException;
import net.strasnet.kids.measurement.datasetinstances.AbstractDataInstance;

import org.semanticweb.owlapi.model.IRI;

/**
 * 
 * @author Chris Strasburg
 *
 */

public class StreamingInstance extends AbstractDataInstance {
	
	private Set<IRI> ourSignals = null;
	
	public StreamingInstance(Map<IRI, String> rMap, List<IRI> myIDs, Set<IRI> associatedSignals) throws UnimplementedIdentifyingFeatureException{
		super(rMap, myIDs);
		ourSignals = associatedSignals;
	}
	
	/**
	 * 
	 * @return - The set of signals this instance matched when it was processed by the detector.
	 */
	public Set<IRI> getSignalSet(){
		return ourSignals;
	}

}
