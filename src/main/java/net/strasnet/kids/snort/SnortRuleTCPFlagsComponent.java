/**
 * 
 */
package net.strasnet.kids.snort;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;

import net.strasnet.kids.KIDSOracle;
import net.strasnet.kids.detectorsyntaxproducers.KIDSIncompatibleSyntaxException;
import net.strasnet.kids.gui.KIDSAddEventOracle;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

/**
 * @author chrisstrasburg
 * 
 * The canonical description of a set of TCP flags is a mapping from bit position to value.
 * 
 * In Snort, only the following types of flag specifications can be represented:
 *  - Specified bits on, the rest must be off. (e.g. "flags: SF")
 *  - Specified bits on, the rest can be on or off. (e.g. "flags: +SF")
 *  - At least one specified bit on, the rest must be off (e.g. "flags: *SF")
 *  - Specified bits off, the rest can be on or off. (e.g. flags: !SF")
 *  
 *  Specifically, the following *cannot* be represented:
 *  - One specified bit on, a second off, and rest can be on or off.
 *  - Two specified bits are on, while at least one of three other bits are on.
 *  
 *  Thus, as long as only 2 of the three possible bit mask values are specified (0 for must be off,
 *  1 for must be on, -n for one of the flags on, and * for either on or off), Snort can represent it.  The following approach is used to produce
 *  a valid Snort rule:
 *   - If the match consists of 0 or more '0' and the rest '1' values, the option is produced with no modifiers.
 *   - If the match consists only of (at least one) '*' and '1' values, the option is produced with the '+' modifier.
 *   - If the match consists only of (at least one) '0' and '*' values, the option is produced with the '!' modifier.
 *   - If the match consists only of (at least one) '*' and '-n' values with only one value for 'n', the option is produced with the '*' modifier.
 *   - If the match is entirely '0' values, the option uses the Snort '0' flag specifier (e.g. flags: 0).
 *   - If the match is entirely '*' values, then there is no signal.
 *   - Otherwise, Snort cannot represent this signal in a single rule.
 *
 */
public class SnortRuleTCPFlagsComponent extends AbstractSnortRuleComponent {

	public static final int NOMOD = 0;
	public static final int PLUSMOD = 1;
	public static final int NOTMOD = 2;
	public static final int STARMOD = 3;
	public static String[] flagChars = {"C","E","U","A","P","R","S","F"};
	public static String[] modChars = {"","+","!","*"};
	
	String value = "";
	//String fName = "#TCPFlags";
	public static final String sClass = "http://solomon.cs.iastate.edu/ontologies/KIDS.owl#TCPFlags_SnortFlagsExpressionMatch";
	public static final String svClass = "http://solomon.cs.iastate.edu/ontologies/KIDS.owl#SnortTCPFlagsValue";
	
	public SnortRuleTCPFlagsComponent(KIDSAddEventOracle ko, Set<IRI> signalSet) throws KIDSIncompatibleSyntaxException {
		super(ko, signalSet);
		
		//TODO: First, check the KB for a specification; set 'value' accordingly
		
		// Query the knowledge base for signals related to this event which are applied to the given feature:
		// Need the intersection of the protocol number class and the class of signals having this event as
		// the filler for the isProducedBy role:
		Set<IRI> signals = ko.getIndividualsFromSetInClass(
				signalSet, IRI.create(sClass)
			);
		
		Iterator<IRI> i = signals.iterator();
		// For now, we're going to assume we get at most one:
		//TODO: Update to use datatype property "canonical signal value"
		while (i.hasNext()){
			OWLNamedIndividual signal = ko.getOwlDataFactory().getOWLNamedIndividual(i.next());
			OWLNamedIndividual sigValue = ko.getCompatibleSignalValue(IRI.create(svClass));
			if (sigValue == null){
				throw new KIDSIncompatibleSyntaxException("Could not get compatible signal values for " + svClass);
			}
			OWLDataProperty signalValue = myF.getOWLDataProperty(KIDSOracle.signalValueDataProp);
			Set<OWLLiteral> ow = ko.getReasoner().getDataPropertyValues(sigValue, signalValue);
			Iterator<OWLLiteral> anI = ow.iterator();
			while (anI.hasNext()){
				String anval = anI.next().getLiteral();
				setSnortRepresentation(anval);
			}
		}
		
	}

	/**
	 * The canonical representation of a TCP flags signal is an ordered list of flag values.
	 * Values are specified in the order: CEUAPRSF and are comma delimited, e.g.: *,*,-1,-1,*,*,*,*. 
	 * @param val
	 */
	private void setSnortRepresentation(String val){
		// Process each range pair:
		String listDelim = ",";
		HashMap<String,Integer> counts = new HashMap<String,Integer>();
		int modifier = NOMOD;
		
		// First, split the values out and store them in a list of strings
		String[] values = val.split(listDelim);
		
		// Determine which of the five possible forms of flag specification this represents; given an error
		// if it cannot be represented in Snort.
		for (int i = 0; i < values.length; i++){
			int update = 1;
			if (counts.containsKey(values[i])){
				update += counts.get(values[i]);
			}
			counts.put(values[i], update);
		}
		
		// Check for too many modifiers:
		if (counts.keySet().size() > 2){
			System.err.println("Error: bad TCP Flags Modifier: ( " + val + " ) skipping...");
			return;
		}
		
		// Check for plusmod:
		if (counts.containsKey("*") && counts.containsKey("1")){
			modifier = PLUSMOD;
		} else if (counts.containsKey("*") && counts.containsKey("0")){
			modifier = NOTMOD;
		} else if (counts.containsKey("*")){
			// This is either all '*' or '*' with -n:
			if (counts.keySet().size() == 1){
				// This is all '*'; no signal
				return;
			} else {
				modifier = STARMOD;
			}
		} else if (counts.containsKey("1")){
			// This is either all '1' or a mix of '1' and '0'
			if (counts.containsKey("0") || counts.keySet().size() == 1){
				modifier = NOMOD;
			} else {
				System.err.println("Error: bad TCP Flags Modifier: ( " + val + " ) skipping...");
			}
		} else if (counts.containsKey("0") && counts.keySet().size() == 1){
			// All '0', so just set the '0' flag:
			value += '0';
			return;
		} else {
			// Snort cannot represent this pattern:
			System.err.println("Error: bad TCP Flags Modifier: ( " + val + " ) skipping...");
			return;
		}
		
		// Once we know the form, we can build the flags field:
		value = modChars[modifier];
		
		for (int i = 0; i < values.length; i++){
			switch (modifier){
			case NOMOD: // Values are either '0' or '1':
				if (values[i].equals("1")){
					value += flagChars[i];
				}
				break;
			case PLUSMOD: // Values are either '*' or '1':
				if (values[i].equals("1")){
					value += flagChars[i];
				}
				break;
			case STARMOD: // Values are either '-n' or '*':
				if (!values[i].equals("*")){
					value += flagChars[i];
				}
				break;
			case NOTMOD: // Values are either '0' or '*':
				if (values[i].equals("0")){
					value += flagChars[i];
				}
				break;
			default:
				System.err.println("Incorrect modifier value: " + modifier);
				break;
			}

		}
		
	}
	
	public String toString(){
		if (value != ""){
			return "flags:" + value + ";";
		} else {
			return "";
		}
	}
	
}
