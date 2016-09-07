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

import net.strasnet.kids.measurement.CorrelationDataInstance;
import net.strasnet.kids.measurement.DataInstance;
import net.strasnet.kids.measurement.datasetinstances.KIDSNativeLibpcapDataInstance;
import net.strasnet.kids.streaming.StreamingAlertHandlerLogMessageImplementation;
import net.strasnet.kids.streaming.StreamingEvent;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.semanticweb.owlapi.model.IRI;

/**
 * @author Chris Strasburg
 *
 */
//TODO: Capture and test the log output
public class TestStreamingEvent {
	
	public Set<IRI> sigSet;
	public static Set<DataInstance> instances;
	public IRI eventIRI;
	public static String IRIPrefix = "http://solomon.cs.iastate.edu/ontologies/KIDS.owl#";
	public static List<IRI> ResourceKeys;
	
	public static int numInstances = 10;
	
	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		instances = new HashSet<DataInstance>();
		
		ResourceKeys = new LinkedList<IRI>();
	    ResourceKeys.add(IRI.create(IRIPrefix + "PacketID"));
	    ResourceKeys.add(IRI.create(IRIPrefix + "IPv4SourceAddressSignalDomain"));
	    ResourceKeys.add(IRI.create(IRIPrefix + "IPv4DestinationAddressSignalDomain"));
	    ResourceKeys.add(IRI.create(IRIPrefix + "ObservationOrder"));
		
		for (int i = 0; i < numInstances; i++){
			HashMap<IRI, String> tempRMap = new HashMap<IRI, String>();
			for (IRI key : ResourceKeys){
				tempRMap.put(key, String.format("%s-%d",key.getFragment(),i));
			}
			DataInstance temp = (DataInstance) new KIDSNativeLibpcapDataInstance(tempRMap);
			instances.add(temp);
		}

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
	    sigSet = new HashSet<IRI>();
	    sigSet.add(IRI.create(IRIPrefix + "#Signal1")); 
	    sigSet.add(IRI.create(IRIPrefix + "#Signal2")); 
	    
		eventIRI = IRI.create(IRIPrefix + "#badThing1");
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
	    //               (IRI eventIRI, Set<IRI> sigSet, String desc, Set<DataInstance> instances){
		StreamingEvent e = new StreamingEvent(
				eventIRI, 
				sigSet, 
				"Test Event - Bad Thing #1", 
				instances);
		// TODO - test warning output on null sigset and instance set
		// TODO - test getters for event IRI and description
		assertTrue(e.getEventIRI().equals(eventIRI));
		
	}
	
	/**
	 * Run the class from the command line:
	 * @param argv
	 */
	public static void main(String[] argv){
		JUnitCore.runClasses(TestStreamingEvent.class);
	}

}
