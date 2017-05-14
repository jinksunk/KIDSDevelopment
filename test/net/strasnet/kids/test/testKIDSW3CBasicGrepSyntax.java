/**
 * 
 */
package net.strasnet.kids.test;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

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
import net.strasnet.kids.test.testKIDSOntologySetup;

import org.junit.Test;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

/**
 * @author chrisstrasburg
 * 
 * This class will test the grep syntax construction based on the signals 
 *
 */
public class testKIDSW3CBasicGrepSyntax {
	
	@Test
	public void testKIDSW3CBasicGrepSyntax() throws Exception{
		fail("Not yet implemented.");
	}

}
