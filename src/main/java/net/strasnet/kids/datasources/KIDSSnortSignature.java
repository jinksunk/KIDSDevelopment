package net.strasnet.kids.datasources;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import net.strasnet.kids.KIDSAbstractAxiom;
import net.strasnet.kids.KIDSAxiom;
import net.strasnet.kids.KIDSContext;
import net.strasnet.kids.KIDSFeature;
import net.strasnet.kids.KIDSSignal;

import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.SWRLAtom;
import org.semanticweb.owlapi.model.SWRLIndividualArgument;
import org.semanticweb.owlapi.model.SWRLObjectPropertyAtom;
import org.semanticweb.owlapi.model.SWRLRule;
import org.semanticweb.owlapi.model.SWRLVariable;
import org.semanticweb.owlapi.reasoner.Node;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.OWLReasoner;


/**
 * Represents a Snort signature.  A signature consists of a projection of signals that identify an event.  
 * In the case of Snort, this projection is onto IP Packet contexts.  Each signature therefore contains 
 * an IP packet context with associated features and signals. 
 *    
 * As the rule is processed:
 *   - Signals are added to the features they are associated with, which are associated with contexts.
 *   - The signature ID is added, and used to generate the signature individual name
 *   - The event (alert text) is added, and used to generate the event individual name
 *   - The reference URL (if present) is added as a comment annotation to the event individual
 *  
 * getAddAxioms() - Returns the collection of axioms associated with this signature.  May include:
 *                   * Signal individuals
 *                   * Associations of signals with features
 *                   * Event individual
 *                   * Associations of signals with event
 *                   * Association of signature with event
 *                   * Association of signature with SnortIDS instance
 *                   
 * getIPPacketContext() - Returns the KIDSIPPacketContext object referenced by this signature.
 * getSUBPacketContext() - Returns the UDP/TCP/ICMPPacketContext object referenced by this signature, or
 *                         null if no such context exists.
 * 
 * @author chrisstrasburg
 *
 */
public class KIDSSnortSignature extends KIDSAbstractAxiom implements KIDSAxiom {

	HashMap <IRI,Collection<AddAxiom>> dedupAx;
	String sid; // The signature ID in snort
	String evt; // The event message
	HashMap<String,List<String>> references;
	List<KIDSSignal> mySigs;
	
	public KIDSSnortSignature(OWLOntology o, IRI oIRI, OWLDataFactory f, OWLReasoner r) {
		super(o, oIRI, f, r);
		
		dedupAx = new HashMap<IRI,Collection<AddAxiom>>();
		references = new HashMap<String,List<String>>();
		sid = null;
		evt = null;		
		mySigs = new LinkedList<KIDSSignal>();
	}
	
	/**
	 * Add the given signal to the list of signals associated with this signature.
	 */
	public void addSignal(KIDSSignal s){
		mySigs.add(s);
	}
	
    /**
     * Create the add axioms for objects in the knowledge base.  These include:
     *  - Our own ID (sid) as an individual
     *  - The Snort IDS and the signature as a member of it
     *  - The event instance described by the set of signals we have identified
     *  - The signal -> feature -> context chain for each signal.
     */
	public Collection <AddAxiom> getAddAxioms() {
		LinkedList<AddAxiom> axList = new LinkedList<AddAxiom>();
		
		if (mySigs.size() > 0){
			OWLNamedIndividual thisSignature = myF.getOWLNamedIndividual(IRI.create(myOIri + "#SnortEventSignature_" + sid));
		
			// Assert the signature itself as a "SnortEventSignature" individual:
			OWLClass Signature = myF.getOWLClass(IRI.create(myOIri + "#SnortEventSignature"));
			axList.add(new AddAxiom(myO, 
				myF.getOWLClassAssertionAxiom(Signature, thisSignature)
			   )
			);
		
			// If the signature doesn't include any signals, don't add anything to the KB
			// If it *does*, then we have some work to do:
			String evtID = "";
			LinkedList<IRI> SignalIRIs = new LinkedList<IRI>();
			Set<SWRLAtom> antecedent = new HashSet<SWRLAtom>();
			
			// First, for each Signal:
			Iterator<KIDSSignal> signals = mySigs.iterator();
			while (signals.hasNext()){
				
				// Add the signal-specific axioms
				KIDSSignal s = signals.next();
				dedupAx.put(s.getIRI(), s.getAddAxioms());
				
				// Combine the string output of each signal for the Event ID:
				evtID += s.toString();
				
				// Keep track of the Signal IRIs for event association, signature association, and rule generation:
				SignalIRIs.add(s.getInstanceIRI());
				
				// Now process the feature axioms, if not done already:
				KIDSFeature f = s.getFeature();
				if (!dedupAx.containsKey(f.getIRI())){
					dedupAx.put(f.getIRI(), f.getAddAxioms());
				}
				
				// Finally, process the context axioms, if not done already:
				KIDSContext c = f.getContext();
				if (!dedupAx.containsKey(c.getIRI())){
					dedupAx.put(c.getIRI(), c.getAddAxioms());
				}
			}
			
			// Now, add the event individual, with appropriate annotations:
			OWLNamedIndividual thisEvent = myF.getOWLNamedIndividual(IRI.create(myOIri + "#Event_" + evtID.hashCode()));
			OWLClass Event = myF.getOWLClass(IRI.create(myOIri + "#Event"));
			axList.add(new AddAxiom(myO, myF.getOWLClassAssertionAxiom(Event, thisEvent)));
			
			// Add the event description:
			OWLAnnotation oan = myF.getOWLAnnotation(myF.getRDFSLabel(), myF.getOWLLiteral(evt));
			OWLAnnotationAssertionAxiom sNote = myF.getOWLAnnotationAssertionAxiom(thisEvent.getIRI(), oan);
			axList.add(new AddAxiom(myO, sNote));
			
			// For each signal IRI:
			// 1) associate the event with it
			// 2) associate the signal with the signature
			// 3) build the antecedent to the SWRL rule
			Iterator<IRI> si = SignalIRIs.descendingIterator();
			OWLObjectProperty relation = myF.getOWLObjectProperty(IRI.create(myOIri + "#isProducerOf"));
			SWRLVariable var = myF.getSWRLVariable(IRI.create(myOIri + "#x"));
			while (si.hasNext()){
				OWLNamedIndividual thisSig = myF.getOWLNamedIndividual(si.next());
				axList.add(new AddAxiom(myO, 
										myF.getOWLObjectPropertyAssertionAxiom(
												relation, 
												thisEvent, 
												thisSig
										)
								)
				);
				// Next the signature itself
				OWLObjectProperty relation2 = myF.getOWLObjectProperty(IRI.create(myOIri + "#hasSignalComponent"));
				axList.add(new AddAxiom(myO, 
										myF.getOWLObjectPropertyAssertionAxiom(
												relation2, 
												thisSignature, 
												thisSig)
										)
				);
				
				// Build the SWRL antecedent
				SWRLObjectPropertyAtom propAtom = myF.getSWRLObjectPropertyAtom(
									relation, 
									var, 
									myF.getSWRLIndividualArgument(thisSig));
				antecedent.add(propAtom);
			}

			// Now, add the SWRL rule for this set of signals
			SWRLObjectPropertyAtom isIdentifiedByProp = myF.getSWRLObjectPropertyAtom(
					myF.getOWLObjectProperty(IRI.create(myOIri + "#isIdentifiedBySignature")), 
					var, 
					myF.getSWRLIndividualArgument(thisSignature));
			SWRLRule eventMatchRule = myF.getSWRLRule(antecedent, Collections.singleton(isIdentifiedByProp));
			axList.add(new AddAxiom(myO, eventMatchRule));

			
			// For each reference type, pull out the strings and add them as axioms for this (event?signature?):
			if (references != null){
				String type;
				String value = "";
				Iterator<String> types = references.keySet().iterator();
				while (types.hasNext()){
					type = types.next();
					Iterator<String> values = references.get(type).iterator();
					value += type + "\n";
					while (values.hasNext()){
						value += "\t" + values.next() + "\n";
					}
				}
				oan = myF.getOWLAnnotation(myF.getRDFSComment(), myF.getOWLLiteral(value));
				sNote = myF.getOWLAnnotationAssertionAxiom(thisEvent.getIRI(), oan);
				axList.add(new AddAxiom(myO, sNote));				
			}
			
			// Finally, run through all the keys in dedupAx and add their values to the axList:
			Iterator<Collection<AddAxiom>> ddAxes = dedupAx.values().iterator();
			while (ddAxes.hasNext()){
				axList.addAll(ddAxes.next());
			}
			
		}
				
		return axList;
	}
	
	/**
	 * Used to set the SID of the signature:
	 */
	public void setSID(String sidValue){
		sid = sidValue;
	}
	
	/**
	 * Used to set the event message this signature detects.
	 */
	public void setEvent(String eventMsg){
		evt = eventMsg;
	}
	
	/**
	 * Used to set the reference URL for the event, if present.
	 */
	public void addRef(String type, String refUrl){
		List<String> l;
		if (!references.containsKey(type)){
			l = new LinkedList<String>();
			references.put(type, l);
		} else {
			l = references.get(type);
		}
		l.add(refUrl);
	}
}
