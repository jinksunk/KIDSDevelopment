/**
 * Test driver for the KIDSLibpcapDataset dataset class.  Each dataset class should provide the following functions:
 * - int numInstances()
 * - Iterator<Instance> getIterator()
 * - numEventOccurances()
 * - numPositiveOccurances()
 * 
 * In addition, this test class also tests KIDSLibpcapDataInstance inner class.
 */
package net.strasnet.kids.measurement.datasetviews.test;

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
import net.strasnet.kids.KIDSOracle;
import net.strasnet.kids.measurement.DataInstance;
import net.strasnet.kids.measurement.Dataset;
import net.strasnet.kids.measurement.EventOccurrence;
import net.strasnet.kids.measurement.KIDSDatasetFactory;
import net.strasnet.kids.measurement.KIDSMeasurementIncompatibleContextException;
import net.strasnet.kids.measurement.KIDSMeasurementInstanceUnsupportedFeatureException;
import net.strasnet.kids.measurement.KIDSMeasurementOracle;
import net.strasnet.kids.measurement.KIDSUnEvaluableSignalException;
import net.strasnet.kids.measurement.Label;
import net.strasnet.kids.measurement.datasetviews.NativeLibPCAPView;
import net.strasnet.kids.measurement.datasetviews.KIDSUnsupportedSchemeException;
import net.strasnet.kids.measurement.datasetviews.KIDSLibpcapDataset.KIDSLibpcapDataInstance;
import net.strasnet.kids.measurement.datasetviews.KIDSLibpcapDataset.KIDSLibpcapTruthFile;
import net.strasnet.kids.measurement.datasetviews.KIDSLibpcapDataset.KIDSLibpcapTruthFile.TruthFileParseException;
import net.strasnet.kids.signalRepresentations.KIDSRepresentationInvalidRepresentationValueException;
import net.strasnet.kids.signalRepresentations.KIDSSignalByteMatchRepresentation;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

/**
 * @author chrisstrasburg
 *
 */
public class testNativeLibPCAPView {

	private Dataset testSet1 = null;
	private static final int numDistinctEvents = 1;
	private static final int testSet1InstanceCount = 99;
	private static final int testSet1NumPositiveInstances = 20;
	
	//private static final int testSet1NumPositiveInstances = 527723;
	//private static final int testSet1InstanceCount = 2202860;
	//private static final int numDistinctEvents = 2;

	//private static final int testSet1InstanceCount = 499;
	
	//private static final String testPCAPFile1 = "/Users/chrisstrasburg/Documents/academic-research/projects/SignalBasedOntologies/datasets/DARPA-1998/Training/w5/fri/tcpdump-onebadhost";
	//private static final String testPCAPFile1 = "/Users/chrisstrasburg/Documents/academic-research/projects/SignalBasedOntologies/experiments/experiment1/test5.pcap";
	//private static final String testPCAPFile1 = "/Users/chrisstrasburg/Documents/academic-research/projects/SignalBasedOntologies/datasets/DARPA-1998/Training/w5/mon/tcpdump";
	//private static final String testTruthFile = "/Users/chrisstrasburg/Documents/academic-research/projects/SignalBasedOntologies/experiments/experiment1/smurfKey-mon.txt";

	private static final String testABOX = "http://www.semantiknit.com/ontologies/2013/6/24/TestEventExperiment1.owl";
	private static final String testABOXFile = "file:///Users/chrisstrasburg/Documents/academic-research/papers/2013-MeasurementPaper/experiments/TestEvent-Test1/TestEventExperiment1.owl";
	private static final String testKBFile = "file:///Users/chrisstrasburg/Documents/academic-research/papers/2013-MeasurementPaper/experiments/TestEvent-Test1/kids.owl";
	private static final String OntologyLocation = "http://solomon.cs.iastate.edu/ontologies/KIDS.owl";
	private static final IRI testEventIRI = IRI.create(testABOX + "#TestEvent1");
	private static final List<IRI> idFeatures = new LinkedList<IRI>();
	static {
		idFeatures.add(IRI.create(testABOX + "#IPPacketID"));
	};
	
	private KIDSMeasurementOracle myGuy = null;
	
	public testNativeLibPCAPView() {
	}
	
	@Before
	public void setup(){
		try {
			myGuy = new KIDSMeasurementOracle();
            List<SimpleIRIMapper> m = new LinkedList<SimpleIRIMapper>();
            m.add(new SimpleIRIMapper(IRI.create(testABOX), IRI.create(testABOXFile)));
            m.add(new SimpleIRIMapper(IRI.create(OntologyLocation), IRI.create(testKBFile)));
			myGuy.loadKIDS(IRI.create(testABOX), m);
		
			// First, get the sample repository of contexts from the dataset
			Set<OWLNamedIndividual> eventDSes = myGuy.getDatasetsForEvent(testEventIRI);
			OWLNamedIndividual ourDS = eventDSes.iterator().next();
			
			testSet1 = KIDSDatasetFactory.getViewLabelDataset(ourDS.getIRI(), testEventIRI, myGuy);
					
			//Next, get the sets of signals which can be applied to the dataset
			//List<OWLNamedIndividual> applicableSignals = myGuy.getSignalsForDatasetAndEvent(testSet1.getIRI(),testEventIRI);
			Set<IRI> applicableSignals = myGuy.getSignalsForDatasetAndEvent(testSet1.getIRI(),testEventIRI);
			if (applicableSignals == null || applicableSignals.size() == 0){
				System.err.println("No signals associated with:\nDataset: \t " + testSet1.getIRI() + "\nEvent: \t" + testEventIRI);
				System.exit(1);
			}
			
			int[] pCount1 = testSet1.numPositiveInstances();
			for (int c : pCount1){
				System.out.println("\t" + c + " positive instances.");
			}
			
			// Then, evaluate each signal over the dataset:
			Dataset signalFiltered = testSet1.getDataSubset(testSet1.getMatchingInstances(applicableSignals));
			pCount1 = signalFiltered.numPositiveInstances();
			for (int c : pCount1){
				System.out.println("\t" + c + " positive instances after filter.");
			}

			int testCount = signalFiltered.numInstances();
			System.out.println("Filtered set has " + testCount + " instances.");
//            testSet1 = new KIDSLibpcapDataset(testPCAPFile1, new File(testTruthFile), testEventIRI, myGuy);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		} catch (Exception e){
			System.err.println(e);
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	/**
	 * Test the ability to produce the correct number of instances:
	 * @throws IOException 
	 * @throws ClassNotFoundException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 * @throws KIDSOntologyObjectValuesException 
	 * @throws KIDSOntologyDatatypeValuesException 
	 * @throws KIDSUnEvaluableSignalException 
	 */
	@Test
	public void testNumInstances() throws IOException, KIDSOntologyDatatypeValuesException, KIDSOntologyObjectValuesException, InstantiationException, IllegalAccessException, ClassNotFoundException, KIDSUnEvaluableSignalException{

		System.out.println("Testing " + testSet1InstanceCount + " == " + testSet1.numInstances());
		assertTrue(testSet1InstanceCount == testSet1.numInstances());
		
		System.out.println("Testing event ID count (" + EventOccurrence.currentEventID + ")");
		assertTrue(EventOccurrence.currentEventID == numDistinctEvents);
		
		System.out.println("Testing iterator over instances...");
		Iterator<DataInstance> i = testSet1.getIterator();
		int count = 0;
		while (i.hasNext()){
			count++;
			i.next();
		}
		assertTrue(count == this.testSet1InstanceCount);
		
		System.out.println("Testing count of positive event data instances");
		int[] pis = testSet1.numPositiveInstances();
		int numPIs = 0;
		for (int j = 0; j < numDistinctEvents; j++){
			numPIs += pis[j];
		}
		assertTrue(numPIs == this.testSet1NumPositiveInstances);
	}
	
	/**
	 * @param args - no args
	 * run the tests
	 */
	public static void main(String[] args) {
		testNativeLibPCAPView tp = new testNativeLibPCAPView();
		tp.setup();
  		//org.junit.runner.JUnitCore.main("net.strasnet.kids.measurement.datasetviews.test.testNativeLibPCAPView");
	}

}
