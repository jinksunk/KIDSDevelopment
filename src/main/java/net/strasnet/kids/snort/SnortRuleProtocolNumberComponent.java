/**
 * 
 */
package net.strasnet.kids.snort;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import net.strasnet.kids.KIDSOracle;
import net.strasnet.kids.detectorsyntaxproducers.KIDSIncompatibleSyntaxException;
import net.strasnet.kids.gui.KIDSAddEventOracle;
import net.strasnet.kids.measurement.KIDSUnEvaluableSignalException;

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
 * This component works on the IP Protocol Number domain, with the 
 */
public class SnortRuleProtocolNumberComponent extends AbstractSnortRuleComponent {

	public static final String sClass = "http://solomon.cs.iastate.edu/ontologies/KIDS.owl#IPProtocolNumber_ByteEquality";
	public static final String svClass = "http://solomon.cs.iastate.edu/ontologies/KIDS.owl#ByteIntegerValue";
//	public static final String fName = "#IPProtocolNumber";
//	public static final String rName = "#ByteMatchRepresentation";
	private String protocol = "ip";
	
	//TODO: Use ip_proto instead of second "protocol" field
	
	// Map of integers to protocol IDs:
    private static final Map<Byte, String> protocolMap = new HashMap<Byte, String>();
    static {
        protocolMap.put((byte)1, "icmp");
    	protocolMap.put((byte)6, "tcp");
        protocolMap.put((byte)17, "udp");
    }
	
	public SnortRuleProtocolNumberComponent(KIDSAddEventOracle ko, Set<IRI> currentSigSet) throws KIDSIncompatibleSyntaxException {
		super(ko, currentSigSet);
				
		// Query the knowledge base for signals related to this event which are related to the defined feature *and* 
		//  have the given representation.
		
		Set<IRI> signals = ko.getIndividualsFromSetInClass(
				currentSigSet, IRI.create(sClass)
			);
		
		
		Iterator<IRI> i = signals.iterator();
		// For now, we're going to assume we get at most one:
		//TODO: Update to use datatype property "canonical signal value"
		while (i.hasNext()){
			OWLNamedIndividual signal = ko.getOwlDataFactory().getOWLNamedIndividual(i.next());
			OWLNamedIndividual sigValue = ko.getCompatibleSignalValue(IRI.create(svClass));
			if (sigValue == null){
				throw new KIDSIncompatibleSyntaxException("Could not get compatible signal value for " + svClass);
			}
			OWLDataProperty signalValue = myF.getOWLDataProperty(KIDSOracle.signalValueDataProp);
			Set<OWLLiteral> ow = ko.getReasoner().getDataPropertyValues(sigValue, signalValue);
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
		if (protocolMap.containsKey(val)){
			protocol = protocolMap.get(val);
		}
	}
	
	public String toString() {
		return protocol;
	}

}
