/**
 * Test driver for the EventOccurence class.  Each EventOccurence provides the following:
 * - int getID()
 * - boolean equals(EventOccurence o)
 * - int compareTo (EventOccurence o)
 */
package net.strasnet.kids.measurement.test;

import java.io.File;
import java.io.IOException;

import org.junit.Test;
import org.semanticweb.owlapi.model.IRI;

import net.strasnet.kids.measurement.EventOccurrence;
import net.strasnet.kids.measurement.datasetviews.KIDSLibpcapDataset;
import net.strasnet.kids.signalRepresentations.KIDSSignalByteMatchRepresentation;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

/**
 * @author chrisstrasburg
 *
 */
public class testEventOccurence {

	private KIDSLibpcapDataset testSet1;
	private static final int testSet1InstanceCount = 20;
	private static final IRI testOccurenceIRI = IRI.create("#testEvent");
	
	public testEventOccurence() {
	}
	
	/**
	 * Test the ability to produce the correct number of instances:
	 */
	@Test
	public void testGetID(){
		EventOccurrence t1 = new EventOccurrence(testOccurenceIRI);
		assertTrue(t1.getID() == 0);
		assertTrue(EventOccurrence.currentEventID == 1);
	}

	/**
	 * Test the equality works as expected:
	 */
	@Test
	public void testEquals(){
		EventOccurrence t1 = new EventOccurrence(testOccurenceIRI);
		EventOccurrence t2 = new EventOccurrence(testOccurenceIRI);
		assertFalse(t1.equals(t2));
		assertTrue(t2.equals(t2));
	}
	
	/**
	 * Test compareTo method:
	 */
	@Test
	public void testCompareTo(){
		EventOccurrence t1 = new EventOccurrence(testOccurenceIRI);
		EventOccurrence t2 = new EventOccurrence(testOccurenceIRI);
		assertTrue(t1.compareTo(t2) == -1);
		assertTrue(t1.compareTo(t1) == 0);
		assertTrue(t2.compareTo(t1) == 1);

	}
	
	/**
	 * @param args - one arg is the libpcap file to parse
	 * run the tests
	 */
	public static void main(String[] args) {
		org.junit.runner.JUnitCore.main("net.strasnet.kids.measurement.test.testEventOccurence");
	}

}
