package net.strasnet.kids.detectorsyntaxproducers;

import java.util.HashMap;
import java.util.Set;

import net.strasnet.kids.KIDSOntologyDatatypeValuesException;
import net.strasnet.kids.KIDSOntologyObjectValuesException;
import net.strasnet.kids.measurement.KIDSMeasurementOracle;
import net.strasnet.kids.measurement.KIDSUnEvaluableSignalException;

import org.semanticweb.owlapi.model.IRI;

/**
 * 
 * @author chrisstrasburg
 *
 */
public interface KIDSDetectorSyntax {
	/**
	 * 
	 * @param sigSet - The map of representations (features + constraints) -> values for the syntax to include
	 * tests for.
	 * @return - A string representation of the syntactic form for the associated detector.
	 * @throws KIDSIncompatibleSyntaxException - If the set of representations cannot be combined into the detector
	 * syntax.
	 * @throws KIDSOntologyObjectValuesException 
	 * @throws KIDSOntologyDatatypeValuesException 
	 * @throws KIDSUnEvaluableSignalException 
	 */
	public String getDetectorSyntax(Set<IRI> sigSet) throws KIDSIncompatibleSyntaxException, KIDSOntologyObjectValuesException, KIDSOntologyDatatypeValuesException, KIDSUnEvaluableSignalException;

	/**
	 * Just set the oracle to use here.
	 * @param o
	 */
	void init(KIDSMeasurementOracle o);
}
