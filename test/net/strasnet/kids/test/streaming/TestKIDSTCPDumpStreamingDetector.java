/**
 * 
 */
package net.strasnet.kids.test.streaming;

import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import net.strasnet.kids.KIDSOntologyDatatypeValuesException;
import net.strasnet.kids.KIDSOntologyObjectValuesException;
import net.strasnet.kids.measurement.KIDSMeasurementOracle;
import net.strasnet.kids.streaming.KIDSStreamingOracle;
import net.strasnet.kids.streaming.KIDSTCPDumpStreamingDetector;
import net.strasnet.kids.streaming.StreamingAlertHandlerLogMessageImplementation;
import net.strasnet.kids.streaming.StreamingEvent;
import net.strasnet.kids.streaming.StreamingInstanceStoreInstanceLimited;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.util.SimpleIRIMapper;

/**
 * @author Chris Strasburg
 *
 */
//TODO: Capture and test the log output
public class TestKIDSTCPDumpStreamingDetector {
	
	public KIDSStreamingOracle o = null;
	public StreamingInstanceStoreInstanceLimited store = null;
	public static IRI KIDSABOXLOCIRI = null;
	public static IRI KIDSTBOXLOCIRI = null;
	public static IRI KIDSABOXIRI = null;
	public static IRI KIDSTBOXIRI = null;
	public static String KIDSBASEFILELOC = "/Users/cstras/Box%20Sync/Academic/research/projects/2010-PhD-SignalBasedSemanticIDRSMeasurement/KIDS";
	public static String KIDSABOXFileLoc = "file://" + KIDSBASEFILELOC + "/resources/ontologies/testontologies/testKIDSStreamingTCPDetector.owl";
	public static String KIDSTBOXFileLoc = "file://" + KIDSBASEFILELOC + "/resources/ontologies/KIDS-TBOX.owl";
	public static String KIDSIndividualIRIPrefix = "http://www.semantiknit.com/ontologies/2015/03/28/KIDS-CodeRedTest.owl";

	public static String tcpDumpExecute = "/usr/sbin/tcpdump";
	public static IRI detectorIRI = null;
	public static String monitorPoint = "en0";
	public static Set<IRI> signalsToMonitor = null;
	public static int timeToRun = 10000; // Time in ms to run the detector for.
	
	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	    KIDSABOXIRI = IRI.create("http://www.semantiknit.com/ontologies/2016/08/04/testKIDSStreamingTCPDetector.owl");
	    KIDSTBOXIRI = IRI.create("http://www.semantiknit.com/ontologies/KIDS-TBOX.owl");
	    KIDSABOXLOCIRI = IRI.create(KIDSABOXFileLoc);
	    KIDSTBOXLOCIRI = IRI.create(KIDSTBOXFileLoc);

	    detectorIRI = IRI.create(KIDSIndividualIRIPrefix + "#tcpdumpFilterCommandForCodeRed");
	    
	    signalsToMonitor = new HashSet<IRI>();
	    signalsToMonitor.add(IRI.create(KIDSIndividualIRIPrefix + "#TCPDestinationPort80Signal"));
	    //signalsToMonitor.add(IRI.create(KIDSIndividualIRIPrefix + "#TCPACKFlagSetSignal"));

	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		// Initialize the test Oracle (load the ontology, etc...)
		o = new KIDSStreamingOracle();
		List<SimpleIRIMapper> m = new LinkedList<SimpleIRIMapper>();
		m.add(new SimpleIRIMapper(KIDSABOXIRI, KIDSABOXLOCIRI));
		m.add(new SimpleIRIMapper(KIDSTBOXIRI, KIDSTBOXLOCIRI));
		o.loadKIDS(KIDSABOXIRI, m);
		store = new StreamingInstanceStoreInstanceLimited(1000, o);
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	/**
	 * Test method for {@link net.strasnet.kids.streaming.KIDSTCPDumpStreamingDetector#handleEvent(net.strasnet.kids.streaming.StreamingEvent)}.
	 */
	@Test
	public final void testTCPDumpMonitoring() {

		// Create the TCPDumpStreamingDetector Object:
		try {
			KIDSTCPDumpStreamingDetector t = new KIDSTCPDumpStreamingDetector(tcpDumpExecute, detectorIRI, o, monitorPoint, signalsToMonitor, store);
			Thread thread = (new Thread(t));
			thread.start();
			Thread.sleep(10000);
			thread.interrupt();
			thread.join(10000);
			assertTrue("Detector IRI does not match...", t.getIRI().equals(detectorIRI));
			assertTrue("No instances added to store...", store.size() > 0);
			System.out.println(String.format("Created %d instances from monitored traffic.", store.size()));
		} catch (KIDSOntologyObjectValuesException e) {
			e.printStackTrace();
			assertTrue("Arguments generated a KIDSOntologyObjectValuesException", false);
		} catch (KIDSOntologyDatatypeValuesException e) {
			e.printStackTrace();
			assertTrue("Arguments generated a KIDSOntologyDatatypeValuesException", false);
		} catch (InstantiationException e) {
			e.printStackTrace();
			assertTrue("Arguments generated an InstantiationException", false);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
			assertTrue("Arguments generated an IllegalAccessException", false);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			assertTrue("Arguments generated a ClassNotFoundException", false);
		} catch (InterruptedException e) {
			e.printStackTrace();
			assertTrue("Sleep interrupted...", false);
		}

	}
	
	/**
	 * Run the class from the command line:
	 * @param argv
	 */
	public static void main(String[] argv){
		JUnitCore.runClasses(TestKIDSTCPDumpStreamingDetector.class);
	}

}
