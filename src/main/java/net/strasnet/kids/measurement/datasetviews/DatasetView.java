package net.strasnet.kids.measurement.datasetviews;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLNamedIndividual;

import net.strasnet.kids.KIDSOntologyDatatypeValuesException;
import net.strasnet.kids.KIDSOntologyObjectValuesException;
import net.strasnet.kids.detectorsyntaxproducers.KIDSIncompatibleSyntaxException;
import net.strasnet.kids.measurement.DataInstance;
import net.strasnet.kids.measurement.Dataset;
import net.strasnet.kids.measurement.KIDSMeasurementOracle;
import net.strasnet.kids.measurement.KIDSUnEvaluableSignalException;

public interface DatasetView {

	/**
	 * Generate a view of the dataset, d, according to the semantics of this view type.
	 * @throws KIDSOntologyDatatypeValuesException 
	 * @throws ClassNotFoundException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 * @throws KIDSOntologyObjectValuesException 
	 * @throws KIDSUnEvaluableSignalException 
	 */
	void generateView(String datasetLocation, KIDSMeasurementOracle o, List<IRI> identifyingFeatures) throws KIDSOntologyDatatypeValuesException, KIDSOntologyObjectValuesException, InstantiationException, IllegalAccessException, ClassNotFoundException, KIDSUnEvaluableSignalException;

	/**
	 * 
	 * @return The number of instances in this view
	 * @throws KIDSUnEvaluableSignalException 
	 */
	int numInstances() throws KIDSUnEvaluableSignalException;

	/**
	 * 
	 */
	List<IRI> getIdentifyingFeatures();
	
	/**
	 * 
	 */
	String getViewLocation();
	
	/**
	 * 
	 * @return An iterator over the member instances of this data view.
	 * @throws IOException - If data instances cannot be read from the view.
	 * @throws KIDSUnEvaluableSignalException 
	 * @throws KIDSIncompatibleSyntaxException 
	 * @throws KIDSOntologyDatatypeValuesException 
	 * @throws KIDSOntologyObjectValuesException 
	 */
	Iterator<DataInstance> iterator() throws IOException, KIDSUnEvaluableSignalException, KIDSOntologyObjectValuesException, KIDSOntologyDatatypeValuesException, KIDSIncompatibleSyntaxException; 
	
	/**
	 * 
	 * @param signalSet - The set of signals to use (as a conjunction) to filter out instances.
	 * @return The set of DataInstances which match the conjunction of signals passed in (signalSet)
	 * @throws KIDSIncompatibleSyntaxException 
	 * @throws IOException 
	 * @throws KIDSOntologyDatatypeValuesException 
	 * @throws KIDSOntologyObjectValuesException 
	 * @throws KIDSUnEvaluableSignalException 
	 */
	Set<DataInstance> getMatchingInstances(Set<IRI> signalSet) throws KIDSOntologyObjectValuesException, KIDSOntologyDatatypeValuesException, IOException, KIDSIncompatibleSyntaxException, KIDSUnEvaluableSignalException;
	
	/**
	 * 
	 * @param members - The data instances to use to construct the dataset view.
	 * @return A dataset view which is a clone of this view, but only including the provided members.
	 * @throws ClassNotFoundException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 * @throws KIDSOntologyObjectValuesException 
	 * @throws KIDSOntologyDatatypeValuesException 
	 * @throws KIDSUnEvaluableSignalException 
	 */
	DatasetView getSubview(Set<DataInstance> members) throws KIDSOntologyDatatypeValuesException, KIDSOntologyObjectValuesException, InstantiationException, IllegalAccessException, ClassNotFoundException, KIDSUnEvaluableSignalException;

	/**
	 * Set the IRI as specified.
	 * @param iri
	 */
	void setIRI(IRI iri);

	/**
	 * 
	 * @return The IRI of this dataset view
	 */
	IRI getIRI();

	/**
	 * 
	 * @param idMap - The map of identifying features to be used to build the instance
	 * @return A DataInstance of type appropriate to the DatasetView
	 */
	DataInstance buildInstance(HashMap<IRI, String> idMap);
}
