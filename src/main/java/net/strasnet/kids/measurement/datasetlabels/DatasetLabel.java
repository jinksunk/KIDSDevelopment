package net.strasnet.kids.measurement.datasetlabels;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.semanticweb.owlapi.model.IRI;

import net.strasnet.kids.detectors.UnimplementedIdentifyingFeatureException;
import net.strasnet.kids.measurement.DataInstance;
import net.strasnet.kids.measurement.EventOccurrence;
import net.strasnet.kids.measurement.Label;

/**
 * Represents a dataset label function.  Defines one method: getLabel(DataviewInstance i), which returns a
 * Label object.
 * @author chrisstrasburg
 *
 */
public interface DatasetLabel {
	
	/**
	 * 
	 * @param dve The instance used by this dataset
	 * @return A Label object associating this instance with it's event occurrence (if any).
	 */
	public Label getLabel(DataInstance dve);

	/**
	 * 
	 * @return The number of event occurrences, e.g. the number of distinct event IDs.
	 */
	public int getNumEvents();
	
	/**
	 * 
	 * @return The IRI of the event associated with this label function
	 */
	public IRI getEventIRI();

	/**
	 * 
	 * @return The set of features designated to identify instances for this label function
	 */
	List<IRI> getIdentifyingFeatures();

	/**
	 * 
	 * @param labelLocation - The location of the actual label file
	 * @param event - The IRI of the event this label file deals with.
	 * @throws IOException 
	 * @throws NumberFormatException 
	 * @throws UnimplementedIdentifyingFeatureException 
	 */
	public void init(String labelLocation, IRI event) throws NumberFormatException, IOException, UnimplementedIdentifyingFeatureException;

	/**
	 * 
	 * @return - A list of all of the event instances included in the dataset.
	 */
	public List<EventOccurrence> getEventList();
	
}
