/**
 * Test driver for the Label class.  Each Label provides the following:
 * - boolean isEvent()
 * - EventOccurence getEventOccurance()
 */
package net.strasnet.kids.measurement.test;

import java.io.File;
import java.io.IOException;

import org.junit.Test;
import org.semanticweb.owlapi.model.IRI;

import net.strasnet.kids.measurement.EventOccurrence;
import net.strasnet.kids.measurement.Label;
import net.strasnet.kids.measurement.datasetviews.KIDSLibpcapDataset;
import net.strasnet.kids.signalRepresentations.KIDSSignalByteMatchRepresentation;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

/**
 * @author chrisstrasburg
 *
 */
public class testLabel {

	private static final IRI testOccurenceIRI = IRI.create("#testEvent");
	
	public testLabel() {
	}
	
	/**
	 * Test the label's true/false values:
	 */
	@Test
	public void testIsEvent(){
		EventOccurrence t1 = new EventOccurrence(testOccurenceIRI);
		Label tl1 = new Label(t1, true);
		assertTrue(tl1.isEvent());
		tl1 = new Label(t1, false);
		assertFalse(tl1.isEvent());
	}

	/**
	 * Test the getEventOccurence method.
	 */
	@Test
	public void testGetEventOccurence(){
		EventOccurrence t1 = new EventOccurrence(testOccurenceIRI);
		Label tl1 = new Label(t1, true);
		assertTrue(tl1.getEventOccurrence().equals(tl1));
	}
	
	/**
	 * @param args - one arg is the libpcap file to parse
	 * run the tests
	 */
	public static void main(String[] args) {
		org.junit.runner.JUnitCore.main("net.strasnet.kids.measurement.test.testEventOccurence");
	}

}
