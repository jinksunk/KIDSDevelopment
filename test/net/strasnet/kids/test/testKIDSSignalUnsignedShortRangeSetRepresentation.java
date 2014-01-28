/**
 * 
 */
package net.strasnet.kids.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import net.strasnet.kids.signalRepresentations.KIDSSignalByteMatchRepresentation;
import net.strasnet.kids.signalRepresentations.KIDSSignalIPNetmaskMatchRepresentation;
import net.strasnet.kids.signalRepresentations.KIDSSignalUnsignedShortRangeSetRepresentation;

import org.junit.Test;

/**
 * @author chrisstrasburg
 *
 */
public class testKIDSSignalUnsignedShortRangeSetRepresentation {

	/**
	 * Test the recognition of dotted-quad values
	 */
	@Test
	public void testSingleValueRecognition(){
		String[] positiveCases = {"0","10000"};
		String[] negativeCases = {"-3","","120583","test"};
		
		KIDSSignalUnsignedShortRangeSetRepresentation tt = new KIDSSignalUnsignedShortRangeSetRepresentation();
		
		// First, check positive cases:
		for (int i = 0; i < positiveCases.length; i++){
			assertTrue(tt.isInSingleValueForm(positiveCases[i]));
		}
		
		for (int i = 0; i < negativeCases.length; i++){
			assertFalse(tt.isInSingleValueForm(negativeCases[i]));
		}
		
	}
	
	/**
	 * Test the recognition of the canonical form
	 */
	@Test
	public void testCanonicalFormRecognition(){
		String[] positiveCases = {"[0-10000,10001-12000]","[1-2]"};
		String[] negativeCases = {"[12000,13-19]","","4294967296"};
		
		KIDSSignalUnsignedShortRangeSetRepresentation tt = new KIDSSignalUnsignedShortRangeSetRepresentation();
		
		// First, check positive cases:
		for (int i = 0; i < positiveCases.length; i++){
			assertTrue(tt.isInCanonicalForm(positiveCases[i]));
		}
		
		for (int i = 0; i < negativeCases.length; i++){
			assertFalse(tt.isInCanonicalForm(negativeCases[i]));
		}
		
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		System.out.println("Running...");
		org.junit.runner.JUnitCore.main("net.strasnet.kids.test.testKIDSSignalUnsignedShortRangeSetRepresentation");
	}

}
