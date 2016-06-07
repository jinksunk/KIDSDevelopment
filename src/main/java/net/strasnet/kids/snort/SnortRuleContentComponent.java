package net.strasnet.kids.snort;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import net.strasnet.kids.KIDSOntologyObjectValuesException;
import net.strasnet.kids.KIDSOracle;
import net.strasnet.kids.detectorsyntaxproducers.KIDSIncompatibleSyntaxException;
import net.strasnet.kids.gui.KIDSAddEventOracle;
import net.strasnet.nfa.GNFA;
import net.strasnet.nfa.ThompsonNFA;

import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

/**
 * The content class is perhaps the most complicated in the Snort rule armada.  There
 * are many modifiers, but they all primarily reflect the feature that is being
 * considered.  
 * 
 * The canonical representation of a content match depends on the constraint language
 * of the signal:
 *  + RegularExpression: CR is a textual representation of * the NFA to be matched.  This initial version of the code will attempt to do two
 *  things with this signal:
 *  1) If the nfa is of the form .{1,n}string or .{n}.*string, the code will produce a 
 *  snort "content" component with appropriate "depth" and "within" tags.
 *  2) Otherwise, a pcre component will be produced.
 *  + StringMatch: CR is a textual representation of the string
 * 
 * Depending on the domain of the signal, different modifiers may be added to the content rule.  For instance, 
 * the domain HTTPGetRequest would add the modifier 'http_uri'
 * 
 * Currently this module supports: 
 *  - #IPPacketData_StringMatch
 *  - #IPPacketData_ByteSubsequenceMath
 *  - #PCREGrammar
 * 
 * 
 * 
 * @author chrisstrasburg
 *
 */

public class SnortRuleContentComponent extends AbstractSnortRuleComponent {

	String sClass = null;
	String value = "";
	public static String tboxPrefix = "http://solomon.cs.iastate.edu/ontologies/KIDS.owl";
	
	public static final HashMap<IRI, String> snortContentModifierMap = new HashMap<IRI, String>();
	static {
		snortContentModifierMap.put(IRI.create(tboxPrefix + "#HTTPGetRequestResource"), "http_uri");
	}
	
	public final HashMap<IRI, SnortContentSpecification> contentMap = new HashMap<IRI, SnortContentSpecification>();
	{
		contentMap.put(IRI.create(tboxPrefix + "#PCREGrammar"), new pcreContentSpecification());
		contentMap.put(IRI.create(tboxPrefix + "#IPPacketData_StringMatch"), new substringMatchContentSpecification());
		contentMap.put(IRI.create(tboxPrefix + "#IPPacketData_ByteSubsequenceMatch"), new subsequenceMatchContentSpecification());
	}
	
	/**
	 * 
	 * @author chrisstrasburg
	 * Set the snort content specification according to the constraint type
	 */
	interface SnortContentSpecification {
		public IRI getCompatibleValueClass();
		public String getSnortRepresentation(String an);
	}
	
	class substringMatchContentSpecification implements SnortContentSpecification {
		String compatibleValueClass = "";
		public IRI getCompatibleValueClass() {
				return IRI.create(tboxPrefix + "#StringSignalValue");
		}
		public String getSnortRepresentation(String an) {
				return "content:\"" + an + "\";";
		}
	}
	
	class pcreContentSpecification implements SnortContentSpecification {
		public IRI getCompatibleValueClass() {
				return IRI.create(tboxPrefix + "#ThompsonNFASpecificationValue");
		}
		public String getSnortRepresentation(String an) {
			ThompsonNFA newMe;
			an = an.replaceAll("\\\\n", "\n");
			// Assume we are doing a canonical form rebuild:
			// Load our Lexer:
			parseCanonicalREStringLexer lexer;
			lexer = new parseCanonicalREStringLexer(new ANTLRStringStream(an));
		
			// Load our parser:
			CommonTokenStream rulesStream = new CommonTokenStream(lexer);
			parseCanonicalREStringParser parser = new parseCanonicalREStringParser(rulesStream);
			try {
				newMe = parser.nfa();
//				System.out.println("newMe:\n" + newMe);
				GNFA backToRegex = new GNFA(newMe);
//				System.out.println("GNFAMe:\n" + backToRegex);
				return "pcre:\"" + backToRegex.generateRegex() + "\";";
			} catch (RecognitionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.exit(1);
			}
			return "";
		}
	}
	
	class subsequenceMatchContentSpecification implements SnortContentSpecification {
		public String getSnortRepresentation(String an) {
			return "content:\"" + an + "\";";
		}
		@Override
		public IRI getCompatibleValueClass() {
			return IRI.create(tboxPrefix + "#ByteHexDelimitedValue");
		}
	}
	
	public SnortRuleContentComponent(KIDSAddEventOracle ko, Set<IRI> currentSignalSet, String sc2) throws KIDSIncompatibleSyntaxException, KIDSOntologyObjectValuesException {
		super(ko, currentSignalSet);
		sClass = sc2;
		
		Set<IRI> signals = ko.getIndividualsFromSetInClass(
				currentSignalSet, IRI.create(sClass)
			);
		
		Iterator<IRI> i = signals.iterator();
		// For now, we're going to assume we get at most one:
		//TODO: Update to use datatype property "canonical signal value"
		while (i.hasNext()){
			OWLNamedIndividual signal = ko.getOwlDataFactory().getOWLNamedIndividual(i.next());
			List<IRI> sigList = ko.getSignalClasses(signal.getIRI()); // Get the most specific class we have an entry for
			IRI signalClassToUse = null;
			for (IRI sigClass : sigList){
				if (contentMap.containsKey(sigClass)){
					signalClassToUse = sigClass;
				}
			}
			
			if (signalClassToUse == null){
				throw new KIDSIncompatibleSyntaxException("Could not find a compatible signal class for " + signal.getIRI().toString());
			}
			SnortContentSpecification sigRep = contentMap.get(signalClassToUse);
			OWLNamedIndividual sigValue = ko.getCompatibleSignalValue(sigRep.getCompatibleValueClass());
			if (sigValue == null){
				throw new KIDSIncompatibleSyntaxException("Could not get compatible signal values for " + sigRep.getCompatibleValueClass().toString());
			}
			OWLDataProperty signalValue = myF.getOWLDataProperty(KIDSOracle.signalValueDataProp);
			Set<OWLLiteral> ow = ko.getReasoner().getDataPropertyValues(sigValue, signalValue);
			Iterator<OWLLiteral> anI = ow.iterator();
			while (anI.hasNext()){
				String anval = anI.next().getLiteral();
				value = contentMap.get(signalClassToUse).getSnortRepresentation(anval);
			}
		}
	}

	public String toString() {
		return value;
	}
}
