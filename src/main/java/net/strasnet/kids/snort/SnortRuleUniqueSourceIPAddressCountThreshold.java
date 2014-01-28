package net.strasnet.kids.snort;

import java.util.Iterator;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import net.strasnet.kids.KIDSOracle;
import net.strasnet.kids.detectorsyntaxproducers.KIDSIncompatibleSyntaxException;
import net.strasnet.kids.gui.KIDSAddEventOracle;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectOneOf;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
/**
 * 
 * @author chrisstrasburg
 * 
 */
public class SnortRuleUniqueSourceIPAddressCountThreshold extends AbstractSnortRuleComponent {

	// fName is the name of the SignalDomain individual this signal class is related to
	public static final String sClass = "http://solomon.cs.iastate.edu/ontologies/KIDS.owl#IPSourceAddress_UniqueSourceIPAddressCountThreshold";
	public static final String svClass = "http://solomon.cs.iastate.edu/ontologies/KIDS.owl#CountTimeBracketValue";
	
	// A regex pattern for the canonical form we work with:
	private static final Pattern cformPattern = Pattern.compile("\\[(\\d+),(\\d+)\\]");
	
	// Values:
	private int count = 0;
	private int seconds = 0;
	private String signatureID = "SIGID";
	
	// Actual Snort representation:
	private String returnString;
	
	public SnortRuleUniqueSourceIPAddressCountThreshold(KIDSAddEventOracle ko, Set<IRI> currentSignalSet) throws KIDSIncompatibleSyntaxException {
		super(ko, currentSignalSet);
		
		returnString = null;
		
		Set<IRI> signals = ko.getIndividualsFromSetInClass(
				currentSignalSet, IRI.create(sClass)
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
	 * Set the values of this representation from the given String.
	 * @param cRep
	 */
	private void setSnortRepresentation(String cRep){
		// First check that cRep matches the required representation:
		Matcher m = cformPattern.matcher(cRep);
		boolean found = false;
		while (m.find()){
			// Set our values:
			count = Integer.parseInt(m.group(1));
			seconds = Integer.parseInt(m.group(2));
			returnString = "event_filter gen_id 1, sig_id " + signatureID + ", type both, track by_dst, count 1, seconds " + seconds + "\n";
			returnString += "rate_filter gen_id 1, sig_id " + signatureID + ", track by_rule, count " + (count + 1) + ", seconds " + seconds + ", new_action pass, timeout " + seconds + "\n";
			returnString += "rate_filter gen_id 1, sig_id " + signatureID + ", track by_rule, count " + count + ", seconds " + seconds + ", new_action alert, timeout " + seconds;
			found = true;
		}
		
	}

	/**
	 * Return the snort representation of this signal:
	 */
	public String toString(){
		return returnString;
	}
}
