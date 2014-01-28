package net.strasnet.kids.detectors;

import java.io.IOException;
import java.util.Set;

import org.semanticweb.owlapi.model.IRI;

import net.strasnet.kids.KIDSOntologyDatatypeValuesException;
import net.strasnet.kids.KIDSOntologyObjectValuesException;
import net.strasnet.kids.detectorsyntaxproducers.KIDSIncompatibleSyntaxException;
import net.strasnet.kids.measurement.DataInstance;
import net.strasnet.kids.measurement.KIDSMeasurementOracle;
import net.strasnet.kids.measurement.KIDSUnEvaluableSignalException;
import net.strasnet.kids.measurement.datasetviews.NativeLibPCAPView;

public interface KIDSDetector {

	/**
	 * 
	 * @param detectorSpec - The syntax with which our detector should filter out instances
	 * @param v
	 * @return - The set of data instances matching the detector specification
	 * @throws IOException
	 * @throws KIDSIncompatibleSyntaxException 
	 * @throws KIDSOntologyDatatypeValuesException 
	 * @throws KIDSOntologyObjectValuesException 
	 * @throws KIDSUnEvaluableSignalException 
	 */
	Set<DataInstance> getMatchingInstances(Set<IRI> signals,
			NativeLibPCAPView v) throws IOException, KIDSOntologyObjectValuesException, KIDSOntologyDatatypeValuesException, KIDSIncompatibleSyntaxException, KIDSUnEvaluableSignalException;

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

}
