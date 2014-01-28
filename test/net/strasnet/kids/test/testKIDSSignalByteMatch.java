/**
 * 
 */
package net.strasnet.kids.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import net.strasnet.kids.signalRepresentations.KIDSSignalByteMatchRepresentation;

import org.junit.Test;

/**
 * @author chrisstrasburg
 *
 */
public class testKIDSSignalByteMatch {

	/**
	 * Test the recognition of hexidecimal values in the KIDSSignalByteMatchRepresentation class:
	 */
	@Test
	public void testHexadecimalRecognition(){
		String[] positiveCases = {"05","10","A8","9F","FF"};
		String[] negativeCases = {"9","100","0", "GG", "aa"};
		
		KIDSSignalByteMatchRepresentation tt = new KIDSSignalByteMatchRepresentation();
		
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
		org.junit.runner.JUnitCore.main("net.strasnet.kids.test.testKIDSSignalByteMatch");
	}

}
