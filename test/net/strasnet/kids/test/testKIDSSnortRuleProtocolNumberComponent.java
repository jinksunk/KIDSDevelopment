/**
 * 
 */
package net.strasnet.kids.test;

import static org.junit.Assert.assertTrue;
import net.strasnet.kids.KIDSOracle;
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
public class testKIDSSnortRuleProtocolNumberComponent {

	@Test
	public void testProtocolNumberComponentProduction() throws Exception{
		IRI ontology_r = IRI.create("file:///Users/chrisstrasburg/copy-master-test.owl");
		KIDSOracle ko = new KIDSOracle();
		ko.loadKIDS(ontology_r, null);
		
		// Initialize some test ontology stuff:
		OWLOntology o = ko.getOntology();
		IRI ontoIRI = ko.getOurIRI();
		OWLDataFactory f = ko.getOwlDataFactory();
		OWLReasoner r = ko.getReasoner();
		OWLNamedIndividual e = f.getOWLNamedIndividual(IRI.create("#testEvent"));
		
		// Run the test:
		SnortRuleProtocolNumberComponent gPig = new SnortRuleProtocolNumberComponent(o, ontoIRI, f, r, e);
		assertTrue(gPig.toString().equals("tcp"));
		
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		System.out.println("Running...");
		org.junit.runner.JUnitCore.main("net.strasnet.kids.test.testKIDSSnortRuleProtocolNumberComponent");

	}

}
