/**
 * 
 */
package net.strasnet.kids.test;

import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import net.strasnet.kids.KIDSOntologyDatatypeValuesException;
import net.strasnet.kids.KIDSOntologyObjectValuesException;
import net.strasnet.kids.KIDSOracle;
import net.strasnet.kids.detectorsyntaxproducers.AbstractGrepSyntax;
import net.strasnet.kids.detectorsyntaxproducers.AbstractGrepSyntax.IntegerRangeSetComponent;
import net.strasnet.kids.detectorsyntaxproducers.KIDSDetectorSyntax;
import net.strasnet.kids.detectorsyntaxproducers.KIDSIncompatibleSyntaxException;
import net.strasnet.kids.gui.KIDSAddEventOracle;
import net.strasnet.kids.measurement.KIDSMeasurementOracle;
import net.strasnet.kids.measurement.KIDSUnEvaluableSignalException;
import net.strasnet.kids.snort.SnortRuleProtocolNumberComponent;

import org.junit.Test;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

/**
 * @author chrisstrasburg
 *
 */
public class testKIDSIntegerRangeSetComponent {
	
	public class TestSyntaxComponent extends AbstractGrepSyntax {

		@Override
		public String getDetectorSyntax(Set<IRI> sigSet) throws KIDSIncompatibleSyntaxException,
				KIDSOntologyObjectValuesException, KIDSOntologyDatatypeValuesException, KIDSUnEvaluableSignalException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void init(KIDSMeasurementOracle o) {
			// TODO Auto-generated method stub
			
		}
		
	};

	@Test
	public void testIntegerRangeSetComponent() throws Exception{
		// Setup inputs:
		Map<String,String> testData = new HashMap<String,String>();

		// Create component to test:
		TestSyntaxComponent tsc = new TestSyntaxComponent();
		IntegerRangeSetComponent irs = tsc.new IntegerRangeSetComponent();
		
		//testData.put("[10:299]", "1[0-9]|[2-8][0-9]|9[0-9]|29[0-9]|2[0-8][0-9]|[1-1][0-9][0-9]");
		//testData.put("[101:299]", "10[1-9]|1[1-9][0-9]|2[0-9][0-9]");
		//testData.put("[101:299012]", "10[1-9]|1[1-9][0-9]|2[0-9][0-9]");
		testData.put("[7:100]", "10[1-9]|1[1-9][0-9]|2[0-9][0-9]");
		//testData.put("[1000007:1000008]", "10[1-9]|1[1-9][0-9]|2[0-9][0-9]");

		// Run the test:
		for (String input : testData.keySet()){
			String output = irs.getSyntaxForm(input, null);
			System.out.println(String.format("Test: %s => %s -- returned %s",input, testData.get(input), output));
			assert(output.equals(testData.get(input)));
		}
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		System.out.println("Running...");
		org.junit.runner.JUnitCore.main("net.strasnet.kids.test.testKIDSIntegerRangeSetComponent");

	}

}
