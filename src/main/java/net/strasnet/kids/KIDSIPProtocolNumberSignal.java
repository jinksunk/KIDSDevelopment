package net.strasnet.kids;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.reasoner.OWLReasoner;


/**
 * 
 * @author chrisstrasburg
 *
 * This class represents an IP protocol number.  This signal can include a restriction on the IP protocol field, and has
 * a canonical representation of a set of permitted integer ranges.
 * 
 * After the signal components are all added, the defined components are combined into a set of
 * 32-bit unsigned Integer ranges (the canonical form).
 */
public class KIDSIPProtocolNumberSignal extends KIDSAbstractSignal implements KIDSSignal{

	List<Integer> protocolComponents;
	
	public KIDSIPProtocolNumberSignal (OWLOntology o, IRI oIRI, OWLDataFactory f,
			OWLReasoner r){
		super(o,oIRI,f,r);
		protocolComponents = new LinkedList<Integer>();
		name="#KIDSIPProtocolNumberSignal";
		representation="#ByteMatchRepresentation";
	}
	
	/**
	 * Generate the content definition for the defined components.
	 * @return String - a string version of the content definition.
	 */
	public String toString() {
		// Iterate over each String in the protocol numbers list:
		Iterator<Integer> i = protocolComponents.iterator();
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
		return toString().hashCode();
	}
	
	/**
	 * @param signalDefinition - a 2-element List<String> specifying the component 
	 *         name and component value for the definition.
	 */
	public void addDefinition(Object signalDefinition) {
		if (signalDefinition.getClass().equals(Integer.class)){
			protocolComponents.add((Integer)signalDefinition);
		} else {
			System.err.println("[KIDSIPProtocolSignal] Messed-up content definition!");
		}
	}

	@Override
	public Collection<AddAxiom> getAddAxioms() {
		LinkedList <AddAxiom> l = new LinkedList<AddAxiom>();
		
		// This signal class is a subclass of things which areValuesInSignalDomain given feature 
		l.add(featureValuesAssociationRules());
		
		// Add the instance as a member of this class
		l.add(classMembershipAxiom());
				
		// Add the property to contain the string form of the regex
		l.add(canonicalRepresentationAxiom());
		
		// Associate this signal with its representation:
		l.add(representationTypeAxiom());

		/*
		// Get an instance of the language this signal implements (a Byte match)
		OWLNamedIndividual lang = myF.getOWLNamedIndividual(IRI.create("#byteMatchLanguage"));
		
		// Add the axiom that this individual is a SignalCanonicalLanguage:
		l.add(new AddAxiom (myO, myF.getOWLClassAssertionAxiom(myF.getOWLClass(IRI.create("#SignalCanonicalLanguage")), lang))
				);
		
		// Link to the language the signal implements
		l.add(new AddAxiom(myO,
						myF.getOWLObjectPropertyAssertionAxiom(myF.getOWLObjectProperty(IRI.create("#hasSignalLanguage")), myF.getOWLNamedIndividual(getInstanceIRI()), lang)
				)
			);
		*/
		
		return l;
	}

}
