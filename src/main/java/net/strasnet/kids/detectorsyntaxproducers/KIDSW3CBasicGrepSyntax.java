/**
 * 
 */
package net.strasnet.kids.detectorsyntaxproducers;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import org.apache.log4j.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.strasnet.kids.KIDSOntologyDatatypeValuesException;
import net.strasnet.kids.KIDSOntologyObjectValuesException;
import net.strasnet.kids.KIDSOracle;
import net.strasnet.kids.lib.IPv4Address;
import net.strasnet.kids.measurement.KIDSMeasurementOracle;
import net.strasnet.kids.measurement.KIDSUnEvaluableSignalException;

import org.apache.log4j.LogManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.util.SimpleIRIMapper;

/**
 * @author cstras
 *
 * Generates extended grep syntax strings for the KIDSNTEventLogDetector
 * @see net.strasnet.kids.detectors.KIDSGrepDetector
 * 
 */
public class KIDSW3CBasicGrepSyntax extends AbstractGrepSyntax implements KIDSDetectorSyntax {
	
	// The components of the view instances are space separated, with positions as:
	// 1 - Date
	// 2 - Time
	// 3 - SourceIP
	// 4 - Client Username
	// 5 - DestIP
	// 6 - DestPort
	// 7 - client request method
	// 8 - client request parameter
	// 9 - request query string
	// 10 - server response code
	// 11 - client user-agent
	
	// The syntax constructor will take signals over each of these fields, and compose an extended grep expression for them.
	private static final org.apache.log4j.Logger logme = LogManager.getLogger(KIDSW3CBasicGrepSyntax.class.getName());
	
	// Map of constraint feature -> type -> syntax
	private static String SRCIPDomain = KIDSOracle.TBOXPrefix + "#IPv4SourceAddressSignalDomain";
	private static String DSTIPDomain = KIDSOracle.TBOXPrefix + "#IPv4DestinationAddressSignalDomain";
	private static String GetParamDomain = KIDSOracle.TBOXPrefix + "#HTTPGetParameter";
	private static String SVRPortDomain = KIDSOracle.TBOXPrefix + "#TCPServerPort";
	
	/** Add the valid feature->type->syntax components we are able to represent. */

	static {

		
	}
	
	private KIDSMeasurementOracle myGuy = null;

	/* (non-Javadoc)
	 * @see net.strasnet.kids.detectorsyntaxproducers.KIDSDetectorSyntax#getDetectorSyntax(java.util.Set)
	 */
	@Override
	public String getDetectorSyntax(Set<IRI> sigSet)
			throws KIDSIncompatibleSyntaxException,
			KIDSOntologyObjectValuesException,
			KIDSOntologyDatatypeValuesException, KIDSUnEvaluableSignalException {

		// For each signal in the set:
		StringBuilder sb = new StringBuilder();
		
		// Keep track of which feature values we've seen:
		Map<IRI, String> valueMap = new HashMap<IRI, String>();
		
		// In this case, we end up with an extended regular expression over the position elements (separated by spaces).
		// Since order matters, we must evaluate the fields in order (or compose them in order anyway.
		for (IRI mySignal : sigSet){
			if (mySignal == null){
				continue;
			}
			// Look up the class for each signal in the set
			IRI sigDomain = myGuy.getSignalDomain(myGuy.getOwlDataFactory().getOWLNamedIndividual(mySignal)).getIRI();
			IRI sigConstraint = myGuy.getSignalConstraint(myGuy.getOwlDataFactory().getOWLNamedIndividual(mySignal)).getIRI();
			
			// Class gives us feature and constraint, now get the value of the signal
			String sVal = myGuy.getSignalValue(myGuy.getOwlDataFactory().getOWLNamedIndividual(mySignal));
			
			// Finally, add the syntax string for the feature / value.
			//Map<IRI, ExtendedGrepSyntaxComponent> tempMap = validSignalClassMap.get(sigDomain);
			if (! validSignalClassMap.containsKey(sigDomain)){
				System.err.println("[W]: Detector implementation " + this.getClass().getName() + " cannot represent signal domain " + sigDomain);
	//			throw new KIDSIncompatibleSyntaxException("Detector cannot represent signal constraint " + sigConstraint);
				continue;
			}
			Map<IRI,ExtendedGrepSyntaxComponent> tempMap = validSignalClassMap.get(sigDomain);
			if (!tempMap.containsKey(sigConstraint)){
				System.err.println("[W]: Detector implementation " + this.getClass().getName() + " cannot represent signal constraint " + sigConstraint);
	//			throw new KIDSIncompatibleSyntaxException("Detector cannot represent signal constraint " + sigConstraint);
				continue;
			}

			valueMap.put(sigDomain, tempMap.get(sigConstraint).getSyntaxForm(sVal, sigConstraint));
		}
		
		// Now, build the final string using those values:
		//  - First, look to see if we have a SRCIP value and add that, then check for the HTTPGet Request value
		if (valueMap.containsKey(IRI.create(KIDSW3CBasicGrepSyntax.SRCIPDomain))){
			sb.append(valueMap.get(IRI.create(KIDSW3CBasicGrepSyntax.SRCIPDomain)) + ".*");
		}
		if (valueMap.containsKey(IRI.create(KIDSW3CBasicGrepSyntax.GetParamDomain))){
			sb.append(valueMap.get(IRI.create(KIDSW3CBasicGrepSyntax.GetParamDomain)));
		}
		return sb.toString();
	}

	/* (non-Javadoc)
	 * @see net.strasnet.kids.detectorsyntaxproducers.KIDSDetectorSyntax#init(net.strasnet.kids.measurement.KIDSMeasurementOracle)
	 */
	@Override
	/**
	 * Map the fields we process to the components that generate the syntax:
	 */
	public void init(KIDSMeasurementOracle o) {
		myGuy = o;

		/**
		 * Populate valid signal map:
		 */
		HashMap<IRI, ExtendedGrepSyntaxComponent> tempMap;
		
		// SourceIPAddress Feature:
		tempMap = new HashMap<IRI, ExtendedGrepSyntaxComponent>();
		tempMap.put(IRI.create(KIDSOracle.TBOXPrefix + "#IPv4AddressRangeSetSignalConstraint"),new IPSyntaxComponent());
		validSignalClassMap.put(IRI.create(SRCIPDomain), tempMap);
		validSignalClassMap.put(IRI.create(DSTIPDomain), tempMap);

		// HTTPGetParameter Feature - we can handle any subclass of RegularGrammar:
		tempMap.put(IRI.create(KIDSOracle.TBOXPrefix + "#RegularGrammar"), new RegularGrammarComponent());
		validSignalClassMap.put(KIDSOracle.HTTPGetRequestClass, tempMap);
		validSignalClassMap.put(KIDSOracle.HTTPGetRequestParameterClass, tempMap);
		validSignalClassMap.put(KIDSOracle.HTTPClientUsernameClass, tempMap);
		validSignalClassMap.put(KIDSOracle.HTTPGetRequestQueryStringClass, tempMap);
		validSignalClassMap.put(KIDSOracle.HTTPClientUserAgentClass, tempMap);

		tempMap.put(IRI.create(KIDSOracle.TBOXPrefix + "#IntegerRangeSet"), new IntegerRangeSetComponent());
		validSignalClassMap.put(KIDSOracle.TCPServerPort, tempMap);
		validSignalClassMap.put(KIDSOracle.HTTPServerResponseCode, tempMap);

	}

}
