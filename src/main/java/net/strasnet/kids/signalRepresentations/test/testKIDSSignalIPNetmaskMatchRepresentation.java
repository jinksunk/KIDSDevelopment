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
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

/**
 * @author chrisstrasburg
 *
 */
public class testKIDSSignalIPNetmaskMatchRepresentation {

	private static final IRI testOccurenceIRI = IRI.create("#testEvent");
	private static final String testip1 = "147.155.1.1";
	private static final String testip2 = "129.186.1.200";
	private static final String testnm1 = "255.255.0.0";
	private static final String testnm2 = "255.255.0.0";
	
	private static final String nonIP1 = "355.128.1.1";
	
	public testKIDSSignalIPNetmaskMatchRepresentation() {
	}
	
	private KIDSSignalIPNetmaskMatchRepresentation testSetup(String Ltestip1, String Ltestnm1) throws KIDSRepresentationInvalidRepresentationValueException{
		return new KIDSSignalIPNetmaskMatchRepresentation("[" + Ltestip1 + "/" + Ltestnm1 + "]");
	}
	/**
	 * Test the constructor of the representation:
	 */
	@Test
	public void testConstructor(){
		try {
			KIDSSignalIPNetmaskMatchRepresentation kimr = testSetup(testip1, testnm1);
			assertTrue(kimr.getCanonicalForm().equals("[" + testip1 + "/" + testnm1 +"]"));
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
		String testip = "147.155.10.10";
		String testip2 = "129.186.1.1";
		String badip1 = "256.256.1.1";
		boolean okflag = false;
		try {
			KIDSSignalIPNetmaskMatchRepresentation kimr = testSetup(testip1, testnm1);
			assertTrue(kimr.matches(testip));
			assertFalse(kimr.matches(testip2));
			okflag = true;
			assertTrue(kimr.matches(badip1));
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
		org.junit.runner.JUnitCore.main("net.strasnet.kids.signalRepresentations.test.testKIDSSignalIPNetmaskMatchRepresentation");
	}

}
