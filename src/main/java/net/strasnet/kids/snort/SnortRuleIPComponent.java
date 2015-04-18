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

import net.strasnet.kids.KIDSOntologyDatatypeValuesException;
import net.strasnet.kids.KIDSOracle;
import net.strasnet.kids.datasources.KIDSSnortIPAddressRange;
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
public class SnortRuleIPComponent extends AbstractSnortRuleComponent {

//	public static final String fName = "#IPPacketSourceAddress"; 
	String sClass = null; // The feature this signal is applied to
	private String ipSet = "any";
	private List<String> values = null;

	
	public SnortRuleIPComponent(KIDSAddEventOracle ko, String sc2, Set<IRI> currentSignalSet) throws KIDSOntologyDatatypeValuesException {
		super(ko, currentSignalSet);
		sClass = sc2;
		values = new LinkedList<String>();
		
		Set<IRI> signals = ko.getIndividualsFromSetInClass(
				currentSignalSet, IRI.create(sClass)
			);
		
		Iterator<IRI> i = signals.iterator();
		// For now, we're going to assume we get at most one:
		//TODO: Update to use datatype property "canonical signal value"
		while (i.hasNext()){
			OWLNamedIndividual signal = ko.getOwlDataFactory().getOWLNamedIndividual(i.next());
			String sigVal = ko.getSignalValue(signal);
			setSnortRepresentation(sigVal);
			/*
			OWLDataProperty signalValue = myF.getOWLDataProperty(KIDSOracle.signalValueDataProp);
			//Set<OWLLiteral> ow = ko.getReasoner().getDataPropertyValues(signal, signalValue);
			Iterator<OWLLiteral> anI = ow.iterator();
			while (anI.hasNext()){
				String anval = anI.next().getLiteral();
				setSnortRepresentation(anval);
			}*/
		}
	}

	/**
	 * Given an IP specification in the form ip/nm or ip/bits, will return a normalized (ip/bits) 
	 * form for a Snort rule.
	 * @param val
	 */
	private void setSnortRepresentation(String val){
		// Get individual ip/netmask sets from the range set:
		String[] cValues;

		cValues = val.substring(1,val.length() - 1).split(",");
		
		for (int i = 0; i < cValues.length; i++){
			// Parse the range into a start and end InetAddress:
			int start1 = 0;
			int end1 = cValues[i].indexOf('/');

			InetAddress start = null;
			InetAddress end = null;

			try {
				start = InetAddress.getByName(cValues[i].substring(start1,end1));
				String s = start.toString().substring(1);
				int bits = -1;
				try {
					bits = Integer.valueOf(cValues[i].substring(end1+1));
					s += String.format("/%d",bits);
				} catch (NumberFormatException ef){
					end = InetAddress.getByName(cValues[i].substring(end1+1));
					s += "/" + KIDSSnortIPAddressRange.netmaskAsCidr(end.toString());
				}
				values.add(s);
			} catch (UnknownHostException e) {
				e.printStackTrace();
				System.exit(1);
			}
		}
	}
	
	/**
	 * We need to override toString() to check the value of the list:
	 */
	public String toString(){
		String returnValue = ipSet;
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
