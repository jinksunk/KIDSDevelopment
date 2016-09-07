/**
 * 
 */
package net.strasnet.kids.test.streaming;

import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import net.strasnet.kids.detectors.UnimplementedIdentifyingFeatureException;
import net.strasnet.kids.measurement.DataInstance;
import net.strasnet.kids.measurement.datasetinstances.KIDSNativeLibpcapDataInstance;
import net.strasnet.kids.streaming.KIDSStreamingOracle;
import net.strasnet.kids.streaming.StreamingAlertHandler;
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
import org.semanticweb.owlapi.util.SimpleIRIMapper;

/**
 * @author Chris Strasburg
 *
 */
//TODO: Capture and test the log output
public class TestStreamingInstanceStoreInstanceLimited {
	
	public static int limit = 10;
	public static Set<DataInstance> DataInstancePool;
	public static String IRIPrefix = "http://solomon.cs.iastate.edu/ontologies/KIDS.owl#";
	public static List<IRI> ResourceKeys;
	public static StreamingEvent e;
	
	public static IRI KIDSABOXLOCIRI = null;
	public static IRI KIDSTBOXLOCIRI = null;
	public static IRI KIDSABOXIRI = null;
	public static IRI KIDSTBOXIRI = null;
	public static String KIDSBASEFILELOC = "/Users/cstras/Box%20Sync/Academic/research/projects/2010-PhD-SignalBasedSemanticIDRSMeasurement/KIDS";
	public static String KIDSABOXFileLoc = "file://" + KIDSBASEFILELOC + "/resources/ontologies/testontologies/testKIDSStreamingTCPDetector.owl";
	public static String KIDSTBOXFileLoc = "file://" + KIDSBASEFILELOC + "/resources/ontologies/KIDS-TBOX.owl";
	public static String KIDSIndividualIRIPrefix = "http://www.semantiknit.com/ontologies/2015/03/28/KIDS-CodeRedTest.owl";

	public KIDSStreamingOracle o = null;
	public StreamingInstanceStoreInstanceLimited toTest;
	
	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	    KIDSABOXIRI = IRI.create("http://www.semantiknit.com/ontologies/2016/08/04/testKIDSStreamingTCPDetector.owl");
	    KIDSTBOXIRI = IRI.create("http://www.semantiknit.com/ontologies/KIDS-TBOX.owl");
	    KIDSABOXLOCIRI = IRI.create(KIDSABOXFileLoc);
	    KIDSTBOXLOCIRI = IRI.create(KIDSTBOXFileLoc);
	    
		DataInstancePool = new HashSet<DataInstance>();
		
		ResourceKeys = new LinkedList<IRI>();
	    ResourceKeys.add(IRI.create(IRIPrefix + "PacketID"));
	    ResourceKeys.add(IRI.create(IRIPrefix + "IPv4SourceAddressSignalDomain"));
	    ResourceKeys.add(IRI.create(IRIPrefix + "IPv4DestinationAddressSignalDomain"));
	    ResourceKeys.add(IRI.create(IRIPrefix + "ObservationOrder"));
		
		for (int i = 0; i < limit; i++){
			HashMap<IRI, String> tempRMap = new HashMap<IRI, String>();
			for (IRI key : ResourceKeys){
				tempRMap.put(key, String.format("%s-%d",key.getFragment(),i));
			}
			DataInstance temp = (DataInstance) new KIDSNativeLibpcapDataInstance(tempRMap);
			DataInstancePool.add(temp);
		}

		e = new StreamingEvent(
				IRI.create(IRIPrefix + "#badThing1"), 
				null, 
				"Test Event - Bad Thing #1", 
				null);
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
        toTest = new StreamingInstanceStoreInstanceLimited(limit, this.o);
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	/**
	 * Test method for {@link net.strasnet.kids.streaming.StreamingAlertHandlerLogMessageImplementation#handleEvent(net.strasnet.kids.streaming.StreamingEvent)}.
	 */
	@Test
	public final void testConstructor() {
		DataInstance firstInstance = null;

		// Test that we can add 'limit' instances;
		for (DataInstance di : DataInstancePool){
			if (firstInstance == null){
				firstInstance = di;
			}
			toTest.addStreamingInstance(di, new HashSet<IRI>());
		}
		
		// Test that we still only have 10 instances, even when we add an 11th:
		HashMap<IRI, String> tempRMap = new HashMap<IRI, String>();
		for (IRI key : ResourceKeys){
			tempRMap.put(key, String.format("%s-%d",key.getFragment(),this.limit + 1));
		}
		DataInstance newDI;
		try {
			newDI = (DataInstance) new KIDSNativeLibpcapDataInstance(tempRMap);
		    toTest.addStreamingInstance(newDI, new HashSet<IRI>());
		    assertTrue("DataInstancePool not equal to limit after overflow!", toTest.size() == TestStreamingInstanceStoreInstanceLimited.limit);
		} catch (UnimplementedIdentifyingFeatureException e) {
			e.printStackTrace();
			assertTrue("Unexpectedly could not create data instance.", false);
		}

	}

	@Test
	public final void testFireAlert() {
		class TestStreamingAlertHandler implements StreamingAlertHandler {
			
			StreamingEvent ourE = null;

			@Override
			public void handleEvent(StreamingEvent e) {
				ourE = e;
			}
			
			public StreamingEvent getEvent(){
				return ourE;
			}
			
		};
		TestStreamingAlertHandler h = new TestStreamingAlertHandler();
		toTest.fireAlert(e);
		assertTrue(h.getEvent() == null);
		toTest.registerStreamingAlertHandler(h);
		toTest.fireAlert(e);
		assertTrue("Event handling isn't calling callbacks...", e.equals(h.getEvent()));
	}
	
	/**
	 * Run the class from the command line:
	 * @param argv
	 */
	public static void main(String[] argv){
		JUnitCore.runClasses(TestStreamingInstanceStoreInstanceLimited.class);
	}

}
