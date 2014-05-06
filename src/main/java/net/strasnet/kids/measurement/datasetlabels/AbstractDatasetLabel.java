/**
 * 
 */
package net.strasnet.kids.measurement.datasetlabels;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import net.strasnet.kids.measurement.DataInstance;
import net.strasnet.kids.measurement.EventOccurrence;
import net.strasnet.kids.measurement.Label;

import org.semanticweb.owlapi.model.IRI;

/**
 * @author cstras
 *
 */
public abstract class AbstractDatasetLabel implements DatasetLabel {

	protected HashMap<Integer, Label> labelKey;
	protected HashMap<Integer, EventOccurrence> seenEvents;
    protected IRI ourEventIRI;
	protected static String featureIRI = "http://solomon.cs.iastate.edu/ontologies/KIDS.owl#";
	protected List<IRI> identifyingFeatures = new LinkedList<IRI>();
    

	/* (non-Javadoc)
	 * @see net.strasnet.kids.measurement.datasetlabels.DatasetLabel#getNumEvents()
	 */
	@Override
	public int getNumEvents() {
		return EventOccurrence.numberOfEvents;
	}

	/* (non-Javadoc)
	 * @see net.strasnet.kids.measurement.datasetlabels.DatasetLabel#getEventIRI()
	 */
	@Override
	public IRI getEventIRI() {
		return this.ourEventIRI;
	}

	/* (non-Javadoc)
	 * @see net.strasnet.kids.measurement.datasetlabels.DatasetLabel#getIdentifyingFeatures()
	 */
	@Override
	public List<IRI> getIdentifyingFeatures() {
		return identifyingFeatures;
	}

	/* (non-Javadoc)
	 * @see net.strasnet.kids.measurement.datasetlabels.DatasetLabel#getEventList()
	 */
	@Override
	public List<EventOccurrence> getEventList() {
		List<EventOccurrence> toReturn = new LinkedList<EventOccurrence>();
		Iterator<EventOccurrence> eventSet = seenEvents.values().iterator();
		while (eventSet.hasNext()){
			toReturn.add(eventSet.next());
		}
		return toReturn;
	}
}
