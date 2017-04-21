/**
 * 
 */
package net.strasnet.kids.snort;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import net.strasnet.kids.KIDSOntologyDatatypeValuesException;
import net.strasnet.kids.KIDSOntologyObjectValuesException;
import net.strasnet.kids.KIDSOracle;
import net.strasnet.kids.KIDSSyntacticFormGenerator;
import net.strasnet.kids.detectorsyntaxproducers.KIDSIncompatibleSyntaxException;
import net.strasnet.kids.gui.KIDSAddEventOracle;
import net.strasnet.kids.measurement.KIDSUnEvaluableSignalException;
import net.strasnet.kids.measurement.test.KIDSSignalSelectionInterface;

import org.apache.log4j.LogManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.reasoner.ConsoleProgressMonitor;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerConfiguration;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.reasoner.SimpleConfiguration;
import org.semanticweb.owlapi.util.SimpleIRIMapper;

import com.clarkparsia.pellet.owlapiv3.PelletReasonerFactory;

/**
 * @author chrisstrasburg
 * 
 * This code generates snort rules from a given knowledge base (Ontology) using the following approach:
 * 1) Query the knowledge base for each event.  For each one:
 * 2) Query the signals that the event produces.
 * 3) Build the rule as follows:
 *    a] Header
 *       i   ] Action <- assume "alert"
 *       ii  ] Protocol <- check for signal; assume "ip" if not present
 *       iii ] SIP <- check for signal; assume "any" if not present
 *       iv  ] SP <- check for signal; assume "any" if not present
 *       v   ] Direction <- check for signal; assume "both" if not present
 *       vi  ] DIP <- check for signal; assume "any" if not present
 *       vii ] DP <- check for signal; assume "any" if not present
 *       viii] Message <- annotation from event, if present
 *       ix  ] Flow <- check for signals
 *       x   ] Content* <- check for signals
 *       xi  ] References <- from annotation on event, if any
 *       xii ] Classtype <- none
 *       xiii] SID <- generate in a special range
 *       xiv ] rev <- always 1
 *       
 */
public class KIDSSnortRuleGenerator implements KIDSSyntacticFormGenerator {

	public static final org.apache.log4j.Logger logme = LogManager.getLogger(KIDSSnortRuleGenerator.class.getName());
	public static final String defaultPrefix = "http://solomon.cs.iastate.edu/ontologies/KIDS.owl";
	public static final IRI ourIdentity = IRI.create("http://solomon.cs.iastate.edu/ontologies/KIDS.owl#snortRuleSyntax3");
	private KIDSAddEventOracle ko;
	private OWLOntology o;
	private OWLDataFactory f;
	private OWLReasoner r;
	private IRI oIri;
	private Set<IRI> currentSigSet;
	
	/**
	 * Default constructor
	 */
	public KIDSSnortRuleGenerator(){
		super();
	}
	
	public void setOracle(KIDSAddEventOracle k){
		ko = k;
		
		// Create the ontology-related objects for the snort rule components:
		o = ko.getOntology();
		f = ko.getOwlDataFactory();
		r = ko.getReasoner();
		oIri = ko.getABOXIRI();	
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// Check arguments: args[0] is the ontology reference / file
		//                  args[1] is the target rules file (or stdout?)

		if (args.length < 2){
			System.err.println("KIDSSnortRuleGenerator requires two arguments:\n\tontology reference / file [RO]\n\t snort rules file[RW].");
		}
		// Check args[0] for readability
		File onto = new File(args[0]);
		//File onto_w = onto;
		if (!onto.canRead()) {
			System.err.println("Cannot read file " + onto);
		}
		KIDSAddEventOracle ko = new KIDSAddEventOracle();
		
		IRI officialPrefix = IRI.create("http://solomon.cs.iastate.edu/ontologies/KIDS.owl");
				
		IRI ontology_r = IRI.create(onto);
		List<SimpleIRIMapper> s = new LinkedList<SimpleIRIMapper>();
		s.add(new SimpleIRIMapper(officialPrefix, ontology_r));
		try {
			ko.loadKIDS(ontology_r, s);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		
		// Check args[1] for readability
		File srules = new File(args[1]);
		if (srules.exists() && !srules.canWrite()) {
			System.err.println("Cannot write file " + srules);
		}
		
		PrintWriter srulesWriter = null;
		try {
			srulesWriter = new PrintWriter(new FileWriter(srules));
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
		
		KIDSSnortRuleGenerator myS = new KIDSSnortRuleGenerator();
		myS.setOracle(ko);
		try {
			srulesWriter.println(myS.getSyntacticForm());
		} catch (KIDSIncompatibleSyntaxException e) {
			e.printStackTrace();
		} catch (KIDSOntologyObjectValuesException e) {
			e.printStackTrace();
		}
		srulesWriter.close();
	}

	/**
	 * Sets the current event to 'e'.
	 * @param e
	 */
	public void setCurrentEvent(OWLNamedIndividual e){
		currentSigSet = ko.getSignalsFromEventAndDetector(e, ourIdentity);
	}
	
	public void setCurrentSignalSet(Set<IRI> sigSet){
		currentSigSet = new HashSet<IRI>();
		currentSigSet.addAll(sigSet);
	}
	
	public Set<IRI> getCurrentSignalSet(){
		return currentSigSet;
	}

	public String getSyntacticForm() throws KIDSIncompatibleSyntaxException, KIDSOntologyObjectValuesException{
						
			// Now, create each snort component and generate the rule:
			String rule = "";
			String ruleT = null; // Check for null returns from component generators
			String sAction = "alert";

			/* Start Header Section */
			// Snort action, no associated feature:
			// Not sure this is the best approach, but for now the default action is 'alert', possibly modified
			// by additional signals below.
			//
			// Thus, action is now applied last, after other signals have been processed
			//rule += new SnortRuleActionComponent(o, oIri, f, r, currentEvent).toString();

			// Protocol specification, associated feature: 
			String protocol = new SnortRuleProtocolNumberComponent(ko,currentSigSet).toString();
			rule += " " + protocol;
			
			// Source IP specification
			try {
				rule += " " + new SnortRuleSourceIPComponent(ko, currentSigSet).toString();
			} catch (KIDSOntologyDatatypeValuesException e) {
				logme.error(String.format("Ontology data values exception on destination IP rule component."));
			}

			// Source Port specification
			if (protocol == "tcp"){
				rule += " " + new SnortRuleTCPSourcePortNumberComponent(ko, currentSigSet).toString();
			} else if (protocol == "udp"){
				rule += " " + new SnortRuleUDPSourcePortNumberComponent(ko, currentSigSet).toString();
			} else {
				rule += " " + "any";
			}
			
			// Direction specification
			rule += " " + new SnortRuleDirectionComponent(ko, currentSigSet).toString();

			// Destination IP
			try {
				rule += " " + new SnortRuleDestinationIPComponent(ko, currentSigSet).toString();
			} catch (KIDSOntologyDatatypeValuesException e) {
				logme.error(String.format("Ontology data values exception on destination IP rule component."));
			}

			// Destination Port
			if (protocol == "tcp"){
				rule += " " + new SnortRuleTCPDestinationPortNumberComponent(ko, currentSigSet).toString();			
			} else if (protocol == "udp"){
				rule += " " + new SnortRuleTCPDestinationPortNumberComponent(ko, currentSigSet).toString();			
			} else {
				rule += " " + "any";
			}
			
			/* Start Content Section */
			rule += " (";
			// Flags
			ruleT = new SnortRuleTCPFlagsComponent(ko, currentSigSet).toString();
			if (protocol == "tcp"){
				rule += " " + ruleT;
			} else if (!ruleT.isEmpty()){
				System.err.println("Warning: Signal for TCP flags set without tcp protocol.");
				//throw new KIDSIncompatibleSyntaxException("Found a tcp flag component without the TCP protocol specified.");
			}
			
			// ICMP type / code
			ruleT = new SnortRuleICMPTypeComponent(ko, currentSigSet).toString();
			if (ruleT != null){
				rule += " " + ruleT;
			}
			
			ruleT = new SnortRuleICMPCodeComponent(ko, currentSigSet).toString();
			if (ruleT != null){
				rule += " " + ruleT;
			}
			
			// Content
			rule += " " + new SnortRuleContentComponent(ko, currentSigSet, defaultPrefix + "#IPPacketData_ByteSubsequenceMatch");
			rule += " " + new SnortRuleContentComponent(ko, currentSigSet, defaultPrefix + "#IPPacketData_StringMatch");
			
			/* Non-payload options */
			// Dsize - payload size
			rule += " " + new SnortRulePayloadSizeComponent(ko, currentSigSet);
			
			// Message (annotation from Event)
			rule += " " + new SnortRuleMessageComponent(ko, currentSigSet).toString();
			
			// references

			// classtype

			// SID -- How to generate this in a non-conflicting way?
			rule += " " + "sid: 12345;";
			// Rev

			rule += ")";
			
			// Check for additional modifiers, e.g. threshold rules:
			ruleT = new SnortRuleUniqueSourceIPAddressCountThreshold(ko, currentSigSet).toString();
			if (ruleT != null){
				rule += "\n" + ruleT;
				sAction = "pass"; // Required to prevent alerts prior to threshold.
			}

			ruleT = new SnortRuleUniqueDestinationIPAddressCountThreshold(ko, currentSigSet).toString();
			if (ruleT != null){
				rule += "\n" + ruleT;
				sAction = "pass"; // Required to prevent alerts prior to threshold.
			}
			
		return sAction + rule;
	}
	
	/**
	 * Check for null 'c'; if so, return empty string
	 * @param c
	 * @return
	 */
	private String getRuleComponent(String c){
		if (c == null){
			return "";
		} else {
			return c;
		}
	}
	
}
