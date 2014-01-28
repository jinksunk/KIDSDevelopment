package net.strasnet.kids;

import java.net.InetAddress;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import net.strasnet.kids.datasources.KIDSSnortIPAddressRange;

import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

/**
 * 
 * @author chrisstrasburg
 *
 * This class represents a snort IP address constraint.  This signal can include any combination of the following:
 * - An IP address / netmask specification
 * - A single IP address
 * - A set of IP addresses
 * - A set of IP address / netmask specifications
 * - A relative IP specification (e.g. a snort variable) TODO: How do we assign semantics to this?
 * 
 * After the signal components are all added, the defined components are combined into a set of
 * 32-bit unsigned Integer ranges (the canonical form).
 */
public class KIDSIPAddressSignal extends KIDSAbstractSignal implements KIDSSignal{

	List<KIDSSnortIPAddressRange> ipComponents; // <IPAddress, NetMask>
	
	public KIDSIPAddressSignal (OWLOntology o, IRI oIRI, OWLDataFactory f, OWLReasoner r){
		super(o,oIRI,f,r);
		ipComponents = new LinkedList<KIDSSnortIPAddressRange>();
		name="#IPAddressSignal";
		representation="#IPNetmaskMatchRepresentation";
	}
	
	/**
	 * Generate the content definition for the defined components.
	 * @return String - a string version of the content definition.
	 */
	public String toString() {
		// Iterate over each String in the rexes list:
		Iterator<KIDSSnortIPAddressRange> i = ipComponents.iterator();
		String returnVal = "";
		String lineEnd = "";
		
		while (i.hasNext()){
			returnVal += lineEnd + i.next();
			lineEnd = "\n";
		}
		
		return returnVal;
	}
	
	/**
	 * Generate a hashCode from the string form of our NFA:
	 */
	public int hashCode(){
		String hashCodeBase = toString();
		hashCodeBase += myFeature.toString();
		return hashCodeBase.hashCode();
	}
	
	/**
	 * @param signalDefinition - a KIDSSnortIPAddressRange object specifying the component 
	 *         name and component value for the definition.
	 */
	public void addDefinition(Object signalDefinition) {
		if (signalDefinition instanceof KIDSSnortIPAddressRange){
			ipComponents.add((KIDSSnortIPAddressRange)signalDefinition);
		} else {
			System.err.println("[KIDSIPAddressSignal] Messed-up content definition!");
		}
	}

	/**
	 * Needs to include the following axioms:
	 * - 
	 */
	@Override
	public Collection<AddAxiom> getAddAxioms() {
		
		LinkedList <AddAxiom> l = new LinkedList<AddAxiom>();
		
		// This signal class is a subclass of things which areValuesInSignalDomain given feature 
		l.add(featureValuesAssociationRules());
		
		// Add the instance as a member of this class
		l.add(classMembershipAxiom());
				
		// Add the property to contain the string form of the regex
		l.add(canonicalRepresentationAxiom());
		
		// Link to the language the signal implements
		l.add(representationTypeAxiom());

		
		return l;
	}

}
