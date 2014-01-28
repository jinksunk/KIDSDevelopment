/**
 * Test driver for the Label class.  Each Label provides the following:
 * - boolean isEvent()
 * - EventOccurence getEventOccurance()
 */
package net.strasnet.kids.signalRepresentations.test;

import java.io.File;
import java.io.IOException;

import org.junit.Test;
import org.semanticweb.owlapi.model.IRI;

import net.strasnet.kids.measurement.EventOccurrence;
import net.strasnet.kids.measurement.Label;
import net.strasnet.kids.signalRepresentations.KIDSRepresentationIncompatibleValueException;
import net.strasnet.kids.signalRepresentations.KIDSRepresentationInvalidRepresentationValueException;
import net.strasnet.kids.signalRepresentations.KIDSSignalIPNetmaskMatchRepresentation;
import net.strasnet.kids.signalRepresentations.KIDSSignalUniqueSourceIPAddressCountThresholdRepresentation;
import net.strasnet.kids.signalRepresentations.KIDSSignalUnsignedShortRangeSetRepresentation;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

/**
 * @author chrisstrasburg
 *
 */
public class testKIDSSignalUniqueSourceIPAddressCountThresholdRepresentation {

	private static final IRI testOccurenceIRI = IRI.create("#testEvent");
	private static final String tests1 = "10";
	private static final String testc1 = "100";
	private static final String tests2 = "5";
	private static final String testc2 = "50";
	
	private static final String nonIP1 = "355.128.1.1";
	
	public testKIDSSignalUniqueSourceIPAddressCountThresholdRepresentation() {
	}
	
	private KIDSSignalUniqueSourceIPAddressCountThresholdRepresentation testSetup(String Lstart, String Lend) throws KIDSRepresentationInvalidRepresentationValueException{
		return new KIDSSignalUniqueSourceIPAddressCountThresholdRepresentation("[" + Lstart + "," + Lend + "]");
	}
	/**
	 * Test the constructor of the representation:
	 */
	@Test
	public void testConstructor(){
		try {
			KIDSSignalUniqueSourceIPAddressCountThresholdRepresentation kimr = testSetup(tests1, testc1);
			assertTrue(kimr.getCanonicalForm().equals("[" + tests1 + "," + testc1 +"]"));
			assertTrue(kimr.isInCanonicalForm(kimr.getCanonicalForm()));
		} catch (KIDSRepresentationInvalidRepresentationValueException e) {
			e.printStackTrace();
			assertTrue(false);
		}
		
	}
	
	/**
	 * Test the signal membership / comparison functions:
	 */
	@Test
	public void testComparisons(){
		String testS1 = "3";
		String testC1 = "75";
		String badS = "-34";
		boolean okflag = false;
		try {
			KIDSSignalUniqueSourceIPAddressCountThresholdRepresentation kimr = testSetup(tests1, testc1);			
			KIDSSignalUniqueSourceIPAddressCountThresholdRepresentation kimr2 = testSetup(tests2, testc2);

			assertTrue(kimr2.matches("[" + testS1 + "," + testC1 + "]"));
			assertFalse(kimr.matches("[" + testS1 + "," + testC1 + "]"));
			okflag = true;
			assertTrue(kimr.matches("[" + badS + "," + testC1 + "]"));
		} catch (KIDSRepresentationInvalidRepresentationValueException e) {
			e.printStackTrace();
			assertTrue(false);
		} catch (KIDSRepresentationIncompatibleValueException e) {
			// TODO Auto-generated catch block
			if (!okflag){
				e.printStackTrace();
				assertTrue(false);
			}
			return;
		}
	}

	/**
	 * @param args - one arg is the libpcap file to parse
	 * run the tests
	 */
	public static void main(String[] args) {
		org.junit.runner.JUnitCore.main("net.strasnet.kids.signalRepresentations.test.testKIDSSignalUniqueSourceIPAddressCountThresholdRepresentation");
	}

}
