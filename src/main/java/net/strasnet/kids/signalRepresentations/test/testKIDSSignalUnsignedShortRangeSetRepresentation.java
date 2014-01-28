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
import net.strasnet.kids.signalRepresentations.KIDSSignalUnsignedShortRangeSetRepresentation;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

/**
 * @author chrisstrasburg
 *
 */
public class testKIDSSignalUnsignedShortRangeSetRepresentation {

	private static final IRI testOccurenceIRI = IRI.create("#testEvent");
	private static final String tests1 = "100";
	private static final String teste1 = "200";
	private static final String tests2 = "199";
	private static final String teste2 = "301";
	
	private static final String nonIP1 = "355.128.1.1";
	
	public testKIDSSignalUnsignedShortRangeSetRepresentation() {
	}
	
	private KIDSSignalUnsignedShortRangeSetRepresentation testSetup(String Lstart, String Lend) throws KIDSRepresentationInvalidRepresentationValueException{
		return new KIDSSignalUnsignedShortRangeSetRepresentation("[" + Lstart + ":" + Lend + "]");
	}
	/**
	 * Test the constructor of the representation:
	 */
	@Test
	public void testConstructor(){
		try {
			KIDSSignalUnsignedShortRangeSetRepresentation kimr = testSetup(tests1, teste1);
			assertTrue(kimr.getCanonicalForm().equals("[" + tests1 + ":" + teste1 +"]"));
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
		String testL1 = "104";
		String testL2 = "204";
		String badShort = "1234567";
		boolean okflag = false;
		try {
			KIDSSignalUnsignedShortRangeSetRepresentation kimr = testSetup(tests1, teste1);
			assertTrue(kimr.matches(testL1));
			assertFalse(kimr.matches(testL2));
			okflag = true;
			assertTrue(kimr.matches(badShort));
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
		org.junit.runner.JUnitCore.main("net.strasnet.kids.signalRepresentations.test.testKIDSSignalUnsignedShortRangeSetRepresentation");
	}

}
