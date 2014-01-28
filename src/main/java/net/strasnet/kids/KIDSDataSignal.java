package net.strasnet.kids;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import net.strasnet.nfa.ThompsonNFA;

import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

/**
 * 
 * @author chrisstrasburg
 *
 * This class represents a snort content definition.  Each content match component and modifier
 * is stored as a distinct value so that the entire context definition can be constructed.  The general "content" 
 * keyword or "pcre" keyword content definitions can only be applied at the IP packet context or a direct sub-protocol context
 * (TCP, UDP, or ICMP).  Other content definition tags (e.g. uricontent) are applied at the packet context of the
 * feature they are associated with (e.g. HTTPPacket).  
 * 
 * After all the content definition components have been parsed, the defined components are combined into a regular
 * expression and either appended to the previous regular expression, or added as a new definition.
 */
public class KIDSDataSignal extends KIDSAbstractSignal implements KIDSSignal{

	HashMap<String,String> contentComponents;
	List<String> rexes;
	List<ThompsonNFA> nfas;
	
	/** The various components we recognize as part of a data signal definition: */
	public final static String DataComponent = "Data";
	public final static String WithinComponent = "Within";
	public final static String OffsetComponent = "Offset";
	public final static String DepthComponent = "Depth";
	public final static String DistanceComponent = "Distance";
	public final static String NocaseComponent = "NoCase";
	public final static String RawbytesComponent = "Rawbytes";

	public KIDSDataSignal (OWLOntology o, IRI oIRI, OWLDataFactory f, OWLReasoner r){
		super(o,oIRI,f,r);
		contentComponents = new HashMap<String,String>();
		rexes = new LinkedList<String>();
		nfas = new LinkedList<ThompsonNFA>();
		
		name="#PacketDataSignal";
	}
	
	/**
	 * Generate the content definition for the defined components.
	 * @return String - a string version of the content definition.
	 */
	public String toString() {
		// Iterate over each String in the rexes list:
		String returnVal = "";
		String lineEnd = "";
		
		for (int i = 0; i < rexes.size(); i++){
			returnVal += lineEnd + nfas.get(i);
			lineEnd = "\n";
		}
		
		return returnVal;
	}
	
	/**
	 * Generate a hashCode from the string form of our NFA:
	 */
	public int hashCode(){
		Iterator<String> i = rexes.iterator();
		String re;
		String rs = "";
		// Construct a parse tree for each regex, concatenating string outputs:
		while (i.hasNext()){
			re = i.next();
			ThompsonNFA p = new ThompsonNFA(re);
			rs += p.toString() + "\n";
		}
		return rs.hashCode();
	}
	
	/**
	 * "Finalize" this content definition:
	 *  1) Create a RE fragment from the current HashMap contents
	 *  2) Append to any existing RE definition, or define as new
	 *  
	 *  There are several combinations of content tags which must be considered:
	 *  - content: a simple string to be matched literally; add one character at a time
	 *  - pcre: a pcre expression; use ParseTree + NFA to generate
	 *  - offset n: Prepend 'n' mandatory empty states; begin separate NFA 
	 *  - depth n: Prepend 'n' empty states; a failed match on the 'x'th input can only return to 'n' - 'x'th empty state; if 'n' - 'x' < 0, reject; begin separate NFA.
	 *  - distance n: Prepend 'n' mandatory empty states; attach to end of previous NFA, if any
	 *  - within n: Prepend 'n' empty states; a failed match on the 'x'th input can only return to 'n' - 'x'th empty state; if 'n' - 'x' < 0, reject; attach to end of previous NFA, if any
	 *  - nocase: Each 'letter' state is actually an 'or' of two states, one for each case.
	 *  - rawbytes: How to deal with raw bytes? no character conversion.
	 */
	public void finalizeRE(){
		// Just go through the possible modifiers one at a time:
		String re = "";
		int min = 0;
		int max = -1;
		boolean rexOpen = true;
		
		if (contentComponents.containsKey(DataComponent)){
			String con = contentComponents.get(DataComponent);
			re += con.substring(1,con.length() - 1);
		}
		
		if (contentComponents.containsKey(OffsetComponent)){
			min = Integer.parseInt(contentComponents.get(OffsetComponent));
		}
		
		if (contentComponents.containsKey(DepthComponent)){
			max = Integer.parseInt(contentComponents.get(DepthComponent));
		}
		
		if (contentComponents.containsKey(DistanceComponent)){
			min = Integer.parseInt(contentComponents.get(DistanceComponent));
			rexOpen = false;
		}
		
		if (contentComponents.containsKey(WithinComponent)){
			max = Integer.parseInt(contentComponents.get(WithinComponent));
			rexOpen = false;
		}
		
		if (min > 0){
			if (max != -1){
				re = ".{" + min + "," + max + "}" + re;
			} else {
				re = ".{" + min + ",}" + re;
			}
		} else if (max != -1) {
			re = ".{" + min + "," + max + "}" + re;
		} else {
			re = ".*" + re + ".*";
			rexOpen = false;
		}
		if (rexOpen && rexes.size() > 0){
			// Append to the previous expression
			rexes.set(rexes.size() - 1, rexes.get(rexes.size() - 1) + re);
		} else {
			// Start a new expression
			rexes.add(re);
		}
		
		// Now, construct the NFA:
		ThompsonNFA t = new ThompsonNFA("/" + re + "/");
		System.out.println(t);
		nfas.add(t);
	}

	/**
	 * @param signalDefinition - a 2-element List<String> specifying the component 
	 *         name and component value for the definition.
	 */
	public void addDefinition(Object signalDefinition) {
		if (signalDefinition instanceof List){
			List<String> def = (List<String>)signalDefinition;
			contentComponents.put(def.get(0), def.get(1));
		} else {
			System.err.println("[KIDSIPDataSignal] Messed-up content definition!");
		}
	}

/**
 *  Generate the set of add axioms for this signal.  For data content, the add axioms include:
 *   - An IP Packet Data Signal individual with a hash value based on the canonical representation (NFA toString()).
 *   - An annotation giving the aggregated regex
 *   - A link to the 'NFA' language?
 */
	public Collection<AddAxiom> getAddAxioms() {
		LinkedList <AddAxiom> l = new LinkedList<AddAxiom>();
		
		// This signal class is a subclass of things which areValuesInSignalDomain given feature 
		l.add(featureValuesAssociationRules());
		
		// Add the instance as a member of this class
		l.add(classMembershipAxiom());
				
		// Add the property to contain the string form of the regex
		l.add(canonicalRepresentationAxiom());
		
		// TODO: Link to the language the signal implements
		
		return l;
	}
}
