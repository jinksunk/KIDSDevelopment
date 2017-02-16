/**
 * 
 */
package net.strasnet.kids.streaming;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.semanticweb.owlapi.model.IRI;

/**
 * @author Chris Strasburg
 * This class implements a naive response selection engine, simply returning the first response that 
 * is satisfiable given the set of resources available.
 */
public class KIDSNaiveResponseSelectionEngine {
	
	Logger logme = LogManager.getLogger(KIDSNaiveResponseSelectionEngine.class.getName());
	KIDSStreamingOracle o;
	
	public KIDSNaiveResponseSelectionEngine(KIDSStreamingOracle oracle){
		o = oracle;
	}
	
	/**
	 * 
	 * @param e - The streaming event we need to respond to
	 * @return The IRI of the selected response, or null if no response is possible.
	 */
	public IRI selectResponse(StreamingEvent e){
		logme.debug(String.format("Selecting response for event: ", e.getEventIRI()));
		List<IRI> viableResponses;
		Map<IRI, String> availableResources = e.getAvailableResources();
		logme.debug(String.format("Found %d availableResource from the event", availableResources.size()));
		// Given the resources provided by this event, select from the ontology those known responses
		// for which all required resources are available.
		viableResponses = o.getViableResponseList(availableResources.keySet());
		logme.debug(String.format("Found %d viable responses", viableResponses.size()));
		
		if (viableResponses.size() == 0){
			logme.info("No responses were available.");
			return null;
		}

		IRI chosenResponse = viableResponses.get(0);
		logme.info(String.format("Selected response %s to mitigate event %s", chosenResponse.getFragment(), e.getEventIRI().getFragment()));
		return chosenResponse;
	}

}
