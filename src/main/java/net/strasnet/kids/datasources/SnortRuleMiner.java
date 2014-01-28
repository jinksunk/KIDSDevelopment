/**
 * 
 */
package net.strasnet.kids.datasources;

import java.io.BufferedReader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.regex.*;

//import org.mindswap.pellet.owlapi.PelletReasonerFactory;
import net.strasnet.kids.KIDSAxiom;
import net.strasnet.kids.KIDSOracle;
import net.strasnet.kids.datasources.snortRulesLexer;
import net.strasnet.kids.datasources.snortRulesParser;

import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CharStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.TokenStream;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.model.PrefixManager;
import org.semanticweb.owlapi.reasoner.ConsoleProgressMonitor;
import org.semanticweb.owlapi.reasoner.Node;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.OWLReasonerConfiguration;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.SimpleConfiguration;
import org.semanticweb.owlapi.util.DefaultPrefixManager;
import org.semanticweb.owlapi.model.*;

import com.clarkparsia.pellet.owlapiv3.PelletReasonerFactory;

/**
 * @author chrisstrasburg
 * This class parses a Snort rules file and maps the following knowledge base elements:
 *  - IDS -> SnortIDS (Class)
 *  - Rule -> Detector (Instance)
 *  - Message -> Event (Class)
 *  - protocol -> Signal Domain Context (Class)
 *  - source IP -> Signal Domain (Class); contained within IP protocol
 *  - source IP value -> Signal (if constrained, e.g. not "any")
 *  - source port -> Signal Domain (Class); contained within TCP / UDP protocols
 *  - source port value -> Signal (if constrained, e.g. not "any")
 *  - destination IP -> Signal Domain (Class); contained within IP protocol
 *  - destination IP value -> Signal (if constrained, e.g. not "any")
 *  - destination port -> Signal Domain (Class); contained within TCP/UDP protocols
 *  - destination port value -> Signal (if constrained, e.g. not "any")
 *  - Direction -> Signal Domain (Class); contained within IP protocol (?)
 *  - Direction value -> Signal if constrained (e.g. not "<>")
 *  - "content" + distance + within + offset + nocase + depth -> Signal Domain within IP context
 *  - "pcre" + distance + within + offset + nocase + depth -> Signal Domain within IP context
 *  - "uricontent" + ... -> Signal Domain within HTTP context
 *  - content / pcre / uricontent value -> Signal
 *  - flow -> Signal Domain within IP context (?)
 *  - classtype -> Subclass of Event which is the parent of this Event
 *  - threshold -> Signal Domain within ? context ?
 * 
 * These values are extracted using an Antlr tokenizer/parser.
 */
public class SnortRuleMiner {

	public static final String defaultPrefix = "https://solomon.cs.iastate.edu/ontologies/KIDS.owl";
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// Check arguments: args[0] is the ontology reference / file
		//                  args[1] is the Snort rules file (or stdin?)
		//					args[2] is the filename to write out as the modified ontology
		//                          if not set, will write back to arg[0]

		if (args.length < 2){
			System.err.println("SnortRuleMiner requires two arguments:\n\tontology reference / file\n\t snort rules file.");
		}
		// Check args[0] for readability
		File onto = new File(args[0]);
		File onto_w = onto;
		if (!onto.canRead()) {
			System.err.println("Cannot read file " + onto);
		}
		KIDSOracle ko = new KIDSOracle();
		IRI ontology_r = IRI.create("file://" + args[0]);
		try {
			ko.loadKIDS(ontology_r, null);
		} catch (Exception e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		
		// Check args[1] for readability
		File srules = new File(args[1]);
		if (!srules.canRead()) {
			System.err.println("Cannot read file " + srules);
		}
		
		BufferedReader srulesReader = null;
		try {
			srulesReader = new BufferedReader(new FileReader(srules));
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
			System.exit(1);
		}
		
		// Check args[2] for writability:
		try {
  		  if (args.length >= 2){
  		    onto_w = new File(args[2]);
		    if (!(onto_w.createNewFile() || onto_w.canWrite())) {
			  System.err.println("Cannot write file " + onto_w);
			  throw new IOException();
		    }
		  }
		} catch (IOException e1) {
			e1.printStackTrace();
			System.exit(1);
		}
		// Load our Lexer:
		snortRulesLexer lexer = new snortRulesLexer();
				
		// Parse the given Snort rules file:
		String snortRuleString;
		KIDSAxiom tempAxiom;

		// Bookkeeping for statistics:
		int nonEmptyRulesCount = 0;
		int axiomCount = ko.getOntology().getAxiomCount();
		int opaCount = ko.getOntology().getAxiomCount(AxiomType.OBJECT_PROPERTY_ASSERTION);
		int caCount = ko.getOntology().getAxiomCount(AxiomType.CLASS_ASSERTION);
		int anCount = ko.getOntology().getAxiomCount(AxiomType.ANNOTATION_ASSERTION);
		int swrlCount = ko.getOntology().getAxiomCount(AxiomType.SWRL_RULE);
		try {
			/*
			lexer = new snortRulesLexer(new ANTLRStringStream("(flow:to_server,both;)"));
			snortRulesParser parser = new snortRulesParser(new CommonTokenStream(lexer));
			parser.body();
			System.out.println("Body: OK!"); */
			while ((snortRuleString = srulesReader.readLine()) != null){
				lexer = new snortRulesLexer(new ANTLRStringStream(snortRuleString));
				
				// Load our parser:
				CommonTokenStream rulesStream = new CommonTokenStream(lexer);
				snortRulesParser parser = new snortRulesParser(rulesStream);
				KIDSSnortSignature ipc = parser.rule(ko.getOntology(), IRI.create(defaultPrefix), ko.getOwlDataFactory(), ko.getReasoner());
				
				Collection<AddAxiom> toAdd = ipc.getAddAxioms();
				
				// Add the signals, etc...
				Iterator<AddAxiom> toAddI = toAdd.iterator();
				
				// Count the number of rules (with signals) processed:
				nonEmptyRulesCount++;
				while (toAddI.hasNext()){
					    // Count the number of axioms added:
						AddAxiom a = toAddI.next();
						ko.getOntologyManager().addAxiom(ko.getOntology(), a.getAxiom());
						//System.out.println(a);
				}
			}
			ko.getOntologyManager().saveOntology(ko.getOntology(), new FileOutputStream(onto_w));
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		} catch (RecognitionException e) {
			e.printStackTrace();
		} catch (OWLOntologyStorageException e) {
			e.printStackTrace();
		}
		
		long cCheckTime = Calendar.getInstance().getTimeInMillis();
		ConsoleProgressMonitor progressMonitor = new ConsoleProgressMonitor();

		OWLReasonerConfiguration config = new SimpleConfiguration(progressMonitor);
		OWLReasonerFactory rf = new PelletReasonerFactory();
		OWLReasoner reasoner = rf.createReasoner(ko.getOntology(), config);
		System.out.println("Precomputing inferences...");
  	    reasoner.precomputeInferences();
		System.out.println("Checking consistency..." + reasoner.isConsistent());
		cCheckTime = Calendar.getInstance().getTimeInMillis() - cCheckTime;
		
		axiomCount = ko.getOntology().getAxiomCount() - axiomCount;
		
		// Print some summary stats:
		System.out.println("Read from file " + args[1] + ": " + nonEmptyRulesCount + " rules; generated " + axiomCount + " axioms.");
		System.out.println("  Average axioms generated per rule = " + ((double) axiomCount / nonEmptyRulesCount));
		System.out.println("\nAxiom Breakdown:\n");
		System.out.println("\t Object Property Assertions: " + (ko.getOntology().getAxiomCount(AxiomType.OBJECT_PROPERTY_ASSERTION) - opaCount));
		System.out.println("\tClass Assertions: " + (ko.getOntology().getAxiomCount(AxiomType.CLASS_ASSERTION) - caCount));
		System.out.println("\tSWRL Rules: " + (ko.getOntology().getAxiomCount(AxiomType.SWRL_RULE) - swrlCount));
		System.out.println("\tAnnotations: " + (ko.getOntology().getAxiomCount(AxiomType.ANNOTATION_ASSERTION) - anCount));
		
		System.out.println("Time for consistency check:" + ((0.0 + cCheckTime) / 1000.0) + "s");
	}

}
