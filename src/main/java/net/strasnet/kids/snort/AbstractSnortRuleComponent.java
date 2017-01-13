/**
 * 
 */
package net.strasnet.kids.snort;

import java.util.LinkedList;
import java.util.Set;

import net.strasnet.kids.KIDSOracle;
import net.strasnet.kids.gui.KIDSAddEventOracle;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

/**
 * @author chrisstrasburg
 *
 * This class contains the functionality common to all Snort rule components.  The following methods are included:
 * - constructor(OWLOntology o, OWLDataFactory f, OWLReasoner r)
 * 
 * In general, each component should map to a feature + representation.  So, in order to identify the applicable 
 * KB individuals, they should belong to the intersection of the feature and representation.  This requires a 
 * naming convention for features.
 * 
 * In general, we will use the convention <Datatype><Operation>Representation.  So, for instance, matching a specific byte value
 * would have a canonical representation of ByteMatchRepresentation.  Matching a range of byte values would have 
 * a canonical representation of ByteRangeRepresentation.  Match a set of ranges of byte values, ByteRangeSetRepresentation.
 * 
 * It is difficult to determine a convention for feature names, however, one application of an ontology is to represent many
 * names for the same concept.  Therefore, each component should attempt to use the most "standard" feature name (e.g. DestinationIPAddress).
 * If subsequent synonyms are found, these can be addressed by asserting equivalence between the individuals.  Thus,
 * each component should check to see if the specified name, <b>or any equivalent individual<\b> is matched.
 *
 */
public abstract class AbstractSnortRuleComponent {
	OWLOntology myO = null;
	OWLDataFactory myF = null;
	OWLReasoner myR = null;
	Set<IRI> mySS = null;
	String signalToEventRelation = "#isProducedBy";
	public static final String signalToRepresentationRelation = "#isRepresentationLanguageOf";
	public static final String signalToSigDomainRelation = "#isValueInSignalDomain";
	public static final String SigDomainToSignalRelation = "#isDomainOfSignal";
	public static final String RepresentationToSignalRelation = "#hasCanonicalRepresentation";
	public static final String canonicalRepDataProperty = "#canonicalSignalValue";
	IRI myOIri = null;
	
	public AbstractSnortRuleComponent(KIDSAddEventOracle ko, Set<IRI> currentSigSet){
		myO = ko.getOntology();
		myF = ko.getOwlDataFactory();
		myR = ko.getReasoner();
		mySS = currentSigSet;
		myOIri = ko.getABOXIRI();
	}
	
	/**
	 * Constructs a class expression representing the intersection of the given class, and the class of things 
	 * produced by this event.  
	 * 
	 * @param c
	 * @return
	 */
	/**
	public OWLClassExpression getEventClassIntersection(OWLClassExpression c){
		OWLClassExpression s2e = myF.getOWLObjectHasValue(
				myF.getOWLObjectProperty(KIDSOracle.signalEventRelation), 
				myF.getOWLNamedIndividual(
					IRI.create(
							myOIri.toString() + myE.getIRI()
						)
					)
				);
		/* LinkedList<OWLClassExpression> l = new LinkedList<OWLClassExpression>();
		l.add(c);
		l.add(s2e);*/
		
	/**
		OWLClassExpression toReturn = myF.getOWLObjectIntersectionOf(c, s2e);
		
		return toReturn;
	} */
	

	/**
	 * This is a 2-argument version of the above; will include both classes in the intersection:
	 * @param owlClass
	 * @param owlClass2
	 * @return
	 */
	/**
	public OWLClassExpression getEventClassIntersection(OWLClassExpression owlClass,
			OWLClassExpression owlClass2) {

		OWLClassExpression toReturn = myF.getOWLObjectIntersectionOf(getEventClassIntersection(owlClass), owlClass2);
		
		return toReturn;
	}
	*/
}
