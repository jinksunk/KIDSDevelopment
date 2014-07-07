package net.strasnet.kids.detectors;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

import org.semanticweb.owlapi.model.IRI;

import net.strasnet.kids.KIDSOntologyDatatypeValuesException;
import net.strasnet.kids.KIDSOntologyObjectValuesException;
import net.strasnet.kids.detectorsyntaxproducers.KIDSIncompatibleSyntaxException;
import net.strasnet.kids.measurement.DataInstance;
import net.strasnet.kids.measurement.KIDSMeasurementOracle;
import net.strasnet.kids.measurement.KIDSUnEvaluableSignalException;
import net.strasnet.kids.measurement.datasetviews.DatasetView;
import net.strasnet.kids.measurement.datasetviews.NativeLibPCAPView;

public interface KIDSDetector {

	/**
	 * 
	 * @param signals - A set of signals to evaluate.
	 * @param v
	 * @return - A set of resource maps, one for each matching individual.  It is up to the DatasetView using the detector to instantiate instances from this.
	 * @throws IOException
	 * @throws KIDSIncompatibleSyntaxException 
	 * @throws KIDSOntologyDatatypeValuesException 
	 * @throws KIDSOntologyObjectValuesException 
	 * @throws KIDSUnEvaluableSignalException 
	 */
	Set<Map<IRI, String>> getMatchingInstances(Set<IRI> signals,
			DatasetView v) throws IOException, KIDSOntologyObjectValuesException, KIDSOntologyDatatypeValuesException, KIDSIncompatibleSyntaxException, KIDSUnEvaluableSignalException;

	/**
	 * Initialize the detector with the given values.
	 * @param toExecute
	 * @param detectorIRI
	 * @param o
	 * @throws KIDSOntologyObjectValuesException
	 * @throws KIDSOntologyDatatypeValuesException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws ClassNotFoundException
	 */
	void init(String toExecute, IRI detectorIRI, KIDSMeasurementOracle o)
			throws KIDSOntologyObjectValuesException,
			KIDSOntologyDatatypeValuesException, InstantiationException,
			IllegalAccessException, ClassNotFoundException;

	/**
	 * 
	 * @return The IRI of the detector 
	 */
	IRI getIRI();
}
