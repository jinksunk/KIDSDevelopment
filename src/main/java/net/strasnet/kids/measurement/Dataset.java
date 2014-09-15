/**
 * 
 */
package net.strasnet.kids.measurement;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLNamedIndividual;

import net.strasnet.kids.KIDSOntologyDatatypeValuesException;
import net.strasnet.kids.KIDSOntologyObjectValuesException;
import net.strasnet.kids.detectors.UnimplementedIdentifyingFeatureException;
import net.strasnet.kids.detectorsyntaxproducers.KIDSIncompatibleSyntaxException;
import net.strasnet.kids.measurement.datasetlabels.DatasetLabel;
import net.strasnet.kids.measurement.datasetlabels.TruthFileParseException;
import net.strasnet.kids.measurement.datasetviews.DatasetView;
import net.strasnet.kids.measurement.datasetviews.KIDSUnsupportedSchemeException;

/**
 * @author chrisstrasburg
 *
 * This interface represents a KIDS dataset.  Datasets are used to evaluate the effectiveness of a signal
 * in identifying specific events.  A dataset has the following characteristics:
 * - A set of labeled instances; any given dataset has two labels: event, non-event.  Each label is 
 *   applied to each instance of the dataset.
 * - 
 */
public interface Dataset {
	/**
	 * 
	 * @return The number of instances in the dataset.
	 * @throws KIDSUnEvaluableSignalException 
	 * @throws UnimplementedIdentifyingFeatureException 
	 */
	public int numInstances() throws KIDSUnEvaluableSignalException, UnimplementedIdentifyingFeatureException;
	
	/**
	 * 
	 * @return An iterator over all data instances.
	 * @throws IOException 
	 * @throws KIDSUnEvaluableSignalException 
	 * @throws KIDSIncompatibleSyntaxException 
	 * @throws KIDSOntologyDatatypeValuesException 
	 * @throws KIDSOntologyObjectValuesException 
	 * @throws UnimplementedIdentifyingFeatureException 
	 */
	public Iterator<DataInstance> getIterator() throws IOException, KIDSUnEvaluableSignalException, KIDSOntologyObjectValuesException, KIDSOntologyDatatypeValuesException, KIDSIncompatibleSyntaxException, UnimplementedIdentifyingFeatureException;
	
	/**
	 * 
	 * @return An iterator over all positive data instances.
	 * @throws ClassNotFoundException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 * @throws KIDSOntologyObjectValuesException 
	 * @throws KIDSOntologyDatatypeValuesException 
	 * @throws IOException 
	 * @throws KIDSUnEvaluableSignalException 
	 * @throws KIDSIncompatibleSyntaxException 
	 * @throws UnimplementedIdentifyingFeatureException 
	 */
	public Iterator<DataInstance> getPositiveIterator() throws KIDSOntologyDatatypeValuesException, KIDSOntologyObjectValuesException, InstantiationException, IllegalAccessException, ClassNotFoundException, IOException, KIDSUnEvaluableSignalException, KIDSIncompatibleSyntaxException, UnimplementedIdentifyingFeatureException;
	
	/**
	 * 
	 * @return The number of distinct events represented in the data set.
	 */
	public int numEventOccurrences();
	
	/**
	 * 
	 * @return The number of positive data instances associated with each distinct
	 * instance of the Event in the data set
	 * @throws IOException 
	 * @throws ClassNotFoundException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 * @throws KIDSOntologyObjectValuesException 
	 * @throws KIDSOntologyDatatypeValuesException 
	 * @throws KIDSUnEvaluableSignalException 
	 * @throws KIDSIncompatibleSyntaxException 
	 * @throws UnimplementedIdentifyingFeatureException 
	 */
	public int[] numPositiveInstances() throws KIDSOntologyDatatypeValuesException, KIDSOntologyObjectValuesException, InstantiationException, IllegalAccessException, ClassNotFoundException, IOException, KIDSUnEvaluableSignalException, KIDSIncompatibleSyntaxException, UnimplementedIdentifyingFeatureException;
	
	/**
	 * 
	 * @param dataURI A valid URI to the resource containing the data elements.
	 * @throws KIDSUnsupportedSchemeException 
	 */
	public void setDataIRI(String dataURI) throws KIDSUnsupportedSchemeException;

	/**
	 * 
	 * @param labelURI A valid URI to the resource containing data element labels.
	 */
	public void setLabelIRI(String labelURI) throws KIDSUnsupportedSchemeException;

	/**
	 * Initialize the dataset object (into a state where it can answer queries)
	 * @param eventIRI - The IRI to initialize the dataset with respect to.  This is the
	 * IRI which will be used to interpret the truth file.
	 * @throws TruthFileParseException 
	 */
	public void init(IRI eventIRI) throws IOException, TruthFileParseException;
	
	/**
	 * Initialize the dataset object (into a state where it can answer queries)
	 * @param eventIRI - The IRI to initialize the dataset with respect to.  This is the
	 * IRI which will be used to interpret the truth file.
	 * @param instanceSet - A set of (labelled) DataInstance objects to populate the dataset
	 * with.
	 */
	public void init(IRI eventIRI, Set<DataInstance> iSet, TreeMap<EventOccurrence, Boolean> eList);

	/**
	 * Set the dataset's own IRI.
	 * @param dsIRI
	 */
	public void setDatasetIRI(IRI dsIRI);

	/**
	 * @return the IRI of the dataset itself.
	 */
	public IRI getIRI();

	/**
	 * Set the oracle used by the dataset to kidsOracle
	 * @param kidsOracle
	 */
	public void setOracle(KIDSMeasurementOracle kidsOracle);

	/**
	 * Set the IRI of the event of interest.
	 * @param evIRI
	 */
	void setEventIRI(IRI evIRI);

	/**
	 * Return a List of signals which can be applied to this data set.
	 */
	public Set<IRI> getKnownApplicableSignals();
	
	/**
	 * @return The measurement oracle object used by this dataset.
	 */
	public KIDSMeasurementOracle getKIDSOracle();

	/**
	 * (Re-)index the dataset by the given signal
	 * @param signal
	 */
	public void indexInstancesBySignal(OWLNamedIndividual signal);
	
	/**
	 * @return The IRI of the datasetView this dataset is initialized with
	 */
	public IRI getViewIRI();

	/**
	 * Return the set of DataInstances matching the provided set of signals
	 * @param applicableSignals
	 * @return
	 * @throws KIDSIncompatibleSyntaxException 
	 * @throws IOException 
	 * @throws KIDSOntologyDatatypeValuesException 
	 * @throws KIDSOntologyObjectValuesException 
	 * @throws KIDSUnEvaluableSignalException 
	 * @throws UnimplementedIdentifyingFeatureException 
	 */
	Set<DataInstance> getMatchingInstances(Set<IRI> applicableSignals) throws KIDSOntologyObjectValuesException, KIDSOntologyDatatypeValuesException, IOException, KIDSIncompatibleSyntaxException, KIDSUnEvaluableSignalException, UnimplementedIdentifyingFeatureException;

	/**
	 * 
	 * @param dataInstanceSet - The set of data instances to include in the subset
	 * @return A Dataset of the same type, with the same label function, but a modified (potentially) set of instaces.
	 * @throws ClassNotFoundException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 * @throws KIDSOntologyObjectValuesException 
	 * @throws KIDSOntologyDatatypeValuesException 
	 * @throws KIDSUnEvaluableSignalException 
	 * @throws IOException 
	 * @throws KIDSIncompatibleSyntaxException 
	 * @throws UnimplementedIdentifyingFeatureException 
	 */
	Dataset getDataSubset(Set<DataInstance> dataInstanceSet) throws KIDSOntologyDatatypeValuesException, KIDSOntologyObjectValuesException, InstantiationException, IllegalAccessException, ClassNotFoundException, IOException, KIDSUnEvaluableSignalException, KIDSIncompatibleSyntaxException, UnimplementedIdentifyingFeatureException;

	/**
	 * Perform any/all required dataset initialization:
	 * @param dv
	 * @param dl
	 * @param o
	 * @param eventIRI
	 * @throws ClassNotFoundException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 * @throws KIDSOntologyObjectValuesException 
	 * @throws KIDSOntologyDatatypeValuesException 
	 * @throws KIDSUnEvaluableSignalException 
	 * @throws IOException 
	 * @throws KIDSIncompatibleSyntaxException 
	 * @throws UnimplementedIdentifyingFeatureException 
	 */
	void init(DatasetView dv, DatasetLabel dl, KIDSMeasurementOracle o,
			IRI eventIRI) throws KIDSOntologyDatatypeValuesException, KIDSOntologyObjectValuesException, InstantiationException, IllegalAccessException, ClassNotFoundException, IOException, KIDSUnEvaluableSignalException, KIDSIncompatibleSyntaxException, UnimplementedIdentifyingFeatureException;
}
