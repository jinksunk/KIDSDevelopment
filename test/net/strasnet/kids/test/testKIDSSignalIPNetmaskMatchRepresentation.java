/**
 * 
 */
package net.strasnet.kids.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import net.strasnet.kids.signalRepresentations.KIDSSignalByteMatchRepresentation;
import net.strasnet.kids.signalRepresentations.KIDSSignalIPNetmaskMatchRepresentation;

import org.junit.Test;

/**
 * @author chrisstrasburg
 *
 */
public class testKIDSSignalIPNetmaskMatchRepresentation {

	/**
	 * Test the recognition of dotted-quad values
	 */
	@Test
	public void testDottedQuadRecognition(){
		String[] positiveCases = {"147.155.1.1"};
		String[] negativeCases = {"129.186.1.1.3","","17"};
		
		KIDSSignalIPNetmaskMatchRepresentation tt = new KIDSSignalIPNetmaskMatchRepresentation();
		
		// First, check positive cases:
		for (int i = 0; i < positiveCases.length; i++){
			assertTrue(tt.isSingleIPForm(positiveCases[i]));
		}
		
		for (int i = 0; i < negativeCases.length; i++){
			assertFalse(tt.isSingleIPForm(negativeCases[i]));
		}
		
	}
	
	/**
	 * Test the recognition of Long values
	 */
	@Test
	public void testLongFormRecognition(){
		String[] positiveCases = {"147","50000","4294967293"};
		String[] negativeCases = {"129.186.1.1.3","","4294967296"};
		
		KIDSSignalIPNetmaskMatchRepresentation tt = new KIDSSignalIPNetmaskMatchRepresentation();
		
		// First, check positive cases:
		for (int i = 0; i < positiveCases.length; i++){
			assertTrue(tt.isLongForm(positiveCases[i]));
		}
		
		for (int i = 0; i < negativeCases.length; i++){
			assertFalse(tt.isLongForm(negativeCases[i]));
		}
		
	}
	
	
	/**
	 * Test the recognition of the canonical form
	 */
	@Test
	public void testCanonicalFormRecognition(){
		String[] positiveCases = {"[147.155.0.0/255.255.0.0]","[192.168.1.1/255.255.255.0,10.10.0.252/255.255.255.253]"};
		String[] negativeCases = {"129.186.1.3","","4294967296"};
		
		KIDSSignalIPNetmaskMatchRepresentation tt = new KIDSSignalIPNetmaskMatchRepresentation();
		
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
		org.junit.runner.JUnitCore.main("net.strasnet.kids.test.testKIDSSignalIPNetmaskMatchRepresentation");
	}

}
