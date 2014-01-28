/**
 * 
 */
package net.strasnet.kids.snort;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import net.strasnet.kids.KIDSOracle;
import net.strasnet.kids.datasources.KIDSSnortIPAddressRange;
import net.strasnet.kids.detectorsyntaxproducers.KIDSIncompatibleSyntaxException;
import net.strasnet.kids.gui.KIDSAddEventOracle;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationValue;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectOneOf;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

/**
 * @author chrisstrasburg
 *
 */
public class SnortRulePortNumberComponent extends AbstractSnortRuleComponent {

//	public static final String fName = "#IPPacketSourceAddress"; 
	private String portSet = "any";
	private List<String> values;
	private String sClass = null;
	private String svClass = "http://solomon.cs.iastate.edu/ontologies/KIDS.owl#IntegerRangeSetValue";
	
	/**
	 * 
	 * @param o - The ontology object representing our knowledge base (kb)
	 * @param oIri - The IRI of our ontology
	 * @param f - An OWLDataFactory to create OWL objects
	 * @param r - A reasoner for performing inference on our KB; initialized and consistency checked
	 * @param evt - The event we are considering for this signal
	 * @param sc2 - The feature on which this signal is defined
	 * @throws KIDSIncompatibleSyntaxException 
	 */
	public SnortRulePortNumberComponent(KIDSAddEventOracle ko, Set<IRI> currentSignalSet, String sc2) throws KIDSIncompatibleSyntaxException {
		super(ko, currentSignalSet);
		sClass = sc2;
		values = new LinkedList<String>();
		
		Set<IRI> signals = ko.getIndividualsFromSetInClass(
				currentSignalSet, IRI.create(myOIri + sClass)
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
	 * The snort representation of a port number signal is a list of value ranges, e.g.: (23:50),(80:80). 
	 * @param val
	 */
	private void setSnortRepresentation(String val){
		// Process each range pair:
		char rangeDelim = ':';
		char listDelim = ',';
		String rest = val.substring(1, val.length()-1);
		
		while (!rest.isEmpty()){
			int cutPoint = rest.indexOf(listDelim);
			String range;
			if (cutPoint > -1){
				range = rest.substring(0, cutPoint);
				rest = rest.substring(cutPoint + 1);
			} else {
				range = rest;
				rest = "";
			}
			int rVal1 = Integer.parseInt(range.substring(0, range.indexOf(rangeDelim)));
			int rVal2 = Integer.parseInt(range.substring(range.indexOf(rangeDelim) + 1));
			values.add(rVal1 + ":" + rVal2);
		}
	}
	
	/**
	 * We need to override toString() to check the value of the list:
	 */
	public String toString(){
		String returnValue = portSet;
		if (values.size() == 1){
			returnValue = values.get(0);
		}
		if (values.size() > 1){
			returnValue = "[";
			String addVal = "";
			
			// Multiple values, need to construct a bracketed set
			Iterator<String> i = values.iterator();
			while (i.hasNext()){
				returnValue += addVal;
				returnValue += i.next();
				addVal = ",";
			}
			returnValue += "]";
		}
		return returnValue;
	}
}
