/**
 * Test driver for the net.strasnet.kids.measurement.KIDSEIDMeasure
 * class.
 * 
 * For a given dataset and event, compute the EID value.
 * 
 * Determine the CID value for the signal in the data set.
 * 
 * TODO: Probably needs to be re-written / replaced with something that uses the correlation datasets.
 * 
 */
package net.strasnet.kids.measurement.test;

import java.io.File;

import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.util.SimpleIRIMapper;

import net.strasnet.kids.KIDSOntologyDatatypeValuesException;
import net.strasnet.kids.KIDSOntologyObjectValuesException;
import net.strasnet.kids.detectorsyntaxproducers.KIDSIncompatibleSyntaxException;
import net.strasnet.kids.measurement.DataInstance;
import net.strasnet.kids.measurement.Dataset;
import net.strasnet.kids.measurement.EventOccurrence;
import net.strasnet.kids.measurement.KIDSEIDMeasure;
import net.strasnet.kids.measurement.KIDSMeasurementIncompatibleContextException;
import net.strasnet.kids.measurement.KIDSMeasurementInstanceUnsupportedFeatureException;
import net.strasnet.kids.measurement.KIDSMeasurementOracle;
import net.strasnet.kids.measurement.KIDSUnEvaluableSignalException;
import net.strasnet.kids.measurement.ViewLabelDataset;
import net.strasnet.kids.signalRepresentations.KIDSRepresentationInvalidRepresentationValueException;
import net.strasnet.kids.signalRepresentations.KIDSSignalByteMatchRepresentation;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

/**
 * @author chrisstrasburg
 *
 */
public class testKIDSEIDMeasure {

	private static final String testKB = "file:///Users/chrisstrasburg/Documents/academic-research/projects/SignalBasedOntologies/ontologies/testOntologies/testKIDSEIDMeasure-1.owl";
	private static final String OntologyLocation = "http://solomon.cs.iastate.edu/ontologies/KIDS.owl";
	private static final IRI testEventIRI = IRI.create("http://solomon.cs.iastate.edu/ontologies/KIDS.owl#smurfAttackEvent");
	private ViewLabelDataset testSet1 = null;
	private List<OWLNamedIndividual> testSignals;

	private KIDSMeasurementOracle myGuy = null;
	
	@Before
	public void setup() {
		/*
		testSignals = new LinkedList<OWLNamedIndividual>();
		
		if (testSet1 == null){
			try {
				myGuy = new KIDSMeasurementOracle();
	            SimpleIRIMapper m = new SimpleIRIMapper(IRI.create(OntologyLocation), IRI.create(testKB));
	            List<SimpleIRIMapper> ml = new LinkedList<SimpleIRIMapper>();
	            ml.add(m);
				myGuy.loadKIDS(IRI.create(OntologyLocation), ml);

				// First, get the sample repository of contexts from the dataset
				Set<OWLNamedIndividual> datasets = myGuy.getDatasetsForEvent(testEventIRI);
				testSet1 = (KIDSLibpcapDataset) datasets.iterator().next();
				
				// Next, get the sets of signals which can be applied to each dataset
				List<OWLNamedIndividual> applicableSignals = myGuy.getSignalsForDataset(testSet1.getIRI());
				if (applicableSignals == null || applicableSignals.size() == 0){
					System.err.println("No signals associated with:\nDataset: \t " + testSet1.getIRI() + "\nEvent: \t" + testEventIRI);
					System.exit(1);
				} else {
					testSignals = applicableSignals;
				}
				
			} catch (IOException e) {
				e.printStackTrace();
				System.exit(1);
			} catch (Exception e){
				System.err.println(e);
				e.printStackTrace();
				System.exit(1);
			}
			//testSet1 = new KIDSLibpcapDataset();
		} */
	}
	
	/**
	 * Test the CID computation:
	 * @throws KIDSUnEvaluableSignalException 
	 */
	@Test
	public void testGetEID() throws KIDSUnEvaluableSignalException{
		
		// Get the entropy of the dataSet:
		// p(c=0)
		/*
		System.out.println("EID \t CID \t Signal");
		for (int i = 0; i < testSignals.size(); i++){
			IRI testSignal1 = testSignals.get(i).getIRI();
			Set<IRI> sigSet = new HashSet<IRI>();
			sigSet.add(testSignal1);
			double kidsEID;
			try {
				kidsEID = KIDSEIDMeasure.getKIDSEIDMeasureValue(testSet1, sigSet);
				double kidsCID = KIDSEIDMeasure.getKIDSCIDMeasureValue(testSet1, sigSet);
				System.out.printf("%.2f\t%.2f\t%s\n", kidsEID, kidsCID, testSignal1.getFragment());
				assertTrue((kidsEID >= 0) && (kidsEID <= 1) && (kidsEID >= kidsCID));
			} catch (KIDSOntologyDatatypeValuesException
					| KIDSOntologyObjectValuesException
					| InstantiationException | IllegalAccessException
					| ClassNotFoundException | IOException
					| KIDSIncompatibleSyntaxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		*/
	}
	
	/**
	 * @param args - one arg is the libpcap file to parse
	 * run the tests
	 */
	public static void main(String[] args) {
		org.junit.runner.JUnitCore.main("net.strasnet.kids.measurement.test.testCIDSignalEvaluation");
	}

}
