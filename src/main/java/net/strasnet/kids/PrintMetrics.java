/**
 * 
 */
package net.strasnet.kids;

import java.net.URI;
import java.util.Iterator;
import java.util.Set;

//import org.mindswap.pellet.owlapi.PelletReasonerFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.PrefixManager;
import org.semanticweb.owlapi.reasoner.Node;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.util.DefaultPrefixManager;
import org.semanticweb.owlapi.model.*;

import com.clarkparsia.pellet.owlapiv3.PelletReasonerFactory;
/**
 * @author chrisstrasburg
 * This class generates and prints the metrics for detectability from a given KIDS ontology.
 * The metrics printed are:
 * - Signal coverage for each distinct signal (|i -> s| / |i|)
 * - Coverage for entire set of signals (should usually be "1")
 */
public class PrintMetrics {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// Open the provided ontology file:
		PrefixManager kids = new DefaultPrefixManager("https://solomon.cs.iastate.edu/ontologies/KIDS.owl#");
		IRI ontology = IRI.create(args[0]);
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLDataFactory odf = manager.getOWLDataFactory();
		try {
			OWLOntology o = manager.loadOntology(ontology);

			// Initiate a reasoner:
			OWLReasonerFactory rf = new PelletReasonerFactory();
			OWLReasoner r = rf.createReasoner(o);
			
			// For each signal, compute and print the metric value:
			OWLClass Signal = odf.getOWLClass(":Signal",kids);
			NodeSet <OWLNamedIndividual> signals = r.getInstances(Signal,false);
			Iterator <Node<OWLNamedIndividual>> i = signals.iterator();
						
			// For each Node
			while (i.hasNext()){
			
				Node <OWLNamedIndividual> curN = i.next();

				Iterator <OWLNamedIndividual> iInd = curN.iterator();
				// For each Node element
				while (iInd.hasNext()){
					OWLNamedIndividual cur = iInd.next();
					System.out.println("Processing Signal " + cur);
					System.out.println("Value is: " + computeSingleSignalCoverage(cur,r,odf,kids));
				}
			}
			System.out.println("Done!");
		} catch (Exception e){
			System.out.println("Failed to load ontology: " + e);
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	/**
	 * @desc evaluateFingerprint: This method determines from the ontology and provided sample data,
	 * the confidence level of each defined fingerprint.  
	 * 
	 * <ul>
	 * <li>Should we use Weka methods?</li>
	 * </ul>
	 */
	
	/**
	 * 
	 * @param c - The Signal under consideration
	 * @param o - The ontology
	 * @return |i -> s| / |i|
	 */
	public static double computeSingleSignalCoverage(OWLNamedIndividual c, OWLReasoner r,
													OWLDataFactory odf, PrefixManager kids){
		int totalIntrusions = 0;
		int signalIndicatorIntrusions = 0;
		
		// Check to ensure the passed class is a Signal
		
		
		// For each intrusion in the ontology, see if it indicates the given signal: 
		// - Get all Event NamedInstances
		// - Check to see if it indicates the given signal
		OWLClass Event = odf.getOWLClass(":Event",kids);
		NodeSet <OWLNamedIndividual> events = r.getInstances(Event,false);
		Iterator <Node<OWLNamedIndividual>> i = events.iterator();
				
		// For each Node
		while (i.hasNext()){
		
			Node <OWLNamedIndividual> curN = i.next();
			System.out.println("Evaluating node...");
			
			Iterator <OWLNamedIndividual> iInd = curN.iterator();
			// For each Node element
			while (iInd.hasNext()){
				totalIntrusions++;
				OWLNamedIndividual cur = iInd.next();
				System.out.println("\tProcessing Event " + cur);
				// If it indicates the signal...
				// Specifically, if produces(c) includes cur
				OWLObjectHasValue v = odf.getOWLObjectHasValue(odf.getOWLObjectProperty(":isProducerOf",kids), c);
				NodeSet <OWLNamedIndividual> viSet = r.getInstances(v, false);
				Iterator <OWLNamedIndividual> j = viSet.getFlattened().iterator();
				while (j.hasNext()){
					OWLNamedIndividual jInd = j.next();
					System.out.println("\t\tChecking " + jInd.getIRI() + " == " + cur.getIRI());
					if (jInd.equals(cur)){
						signalIndicatorIntrusions++;
					}
				}
			}
		}
		
		// Return the result:
		if (totalIntrusions == 0){
			return 1;
		} else {
			return (double)signalIndicatorIntrusions / (double) totalIntrusions;
		}
	}

}
