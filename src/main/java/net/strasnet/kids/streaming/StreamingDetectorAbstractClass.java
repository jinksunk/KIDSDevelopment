/**
 * This class implements the common methods for all StreamingDetectors 
 */
package net.strasnet.kids.streaming;

import net.strasnet.kids.detectors.KIDSAbstractDetector;
import net.strasnet.kids.detectorsyntaxproducers.KIDSDetectorSyntax;
import net.strasnet.kids.measurement.KIDSMeasurementOracle;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.semanticweb.owlapi.model.IRI;

/**
 * @author Chris Strasburg
 *
 *
 */
public abstract class StreamingDetectorAbstractClass extends KIDSAbstractDetector implements StreamingDetectorInterface, Runnable {
	private static final Logger logme = LogManager.getLogger(StreamingDetectorAbstractClass.class.getName());
//	protected static String featureIRI = "http://solomon.cs.iastate.edu/ontologies/KIDS.owl#";
	
//	KIDSMeasurementOracle myO = null;
//	String executionCommand = null;
//	KIDSDetectorSyntax ourSyn = null;

	//TODO: Replace this init thing with a constructor.
}
