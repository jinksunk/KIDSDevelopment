/**
 * 
 */
package net.strasnet.kids.snort;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import net.strasnet.kids.KIDSOracle;
import net.strasnet.kids.gui.KIDSAddEventOracle;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationValue;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectOneOf;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLSameIndividualAxiom;
import org.semanticweb.owlapi.reasoner.Node;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

/**
 * @author chrisstrasburg
 *
 */
public class SnortRuleICMPTypeComponent extends AbstractSnortRuleComponent {

	public static final String sClass = "#ICMPType_ByteValueMatch";
	private String value;
	
	public SnortRuleICMPTypeComponent(KIDSAddEventOracle ko, Set<IRI> currentSignalSet) {
		super(ko, currentSignalSet);
		value = null;
				
		Set<IRI> signals = ko.getIndividualsFromSetInClass(
				currentSignalSet, IRI.create(myOIri + sClass)
			);
		
		Iterator<IRI> i = signals.iterator();
		// For now, we're going to assume we get at most one:
		//TODO: Update to use datatype property "canonical signal value"
		
		while (i.hasNext()){
			OWLNamedIndividual signal = ko.getOwlDataFactory().getOWLNamedIndividual(i.next());
			OWLDataProperty canonicalRep = myF.getOWLDataProperty(KIDSOracle.signalValueDataProp);
			Set<OWLLiteral> ow = ko.getReasoner().getDataPropertyValues(signal, canonicalRep);
			Iterator<OWLLiteral> anI = ow.iterator();
			while (anI.hasNext()){
				String anval = anI.next().getLiteral();
				Byte val = Byte.parseByte(anval);
				setSnortRepresentationFromByte(val);
			}
		}
	}
	
	/**
	 * Given a byte representing a protocol value, will set this.protocol to the snort text for a rule.  If the byte does not
	 * correspond to a valid Snort rule protocol, will leave the value of this.protocol unchanged. 
	 * @param val
	 */
	private void setSnortRepresentationFromByte(Byte val){
		value = "itype: " + val + ";";
	}
	
	public String toString() {
		return value;
	}

}
