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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.strasnet.kids.KIDSOntologyDatatypeValuesException;
import net.strasnet.kids.KIDSOntologyObjectValuesException;
import net.strasnet.kids.lib.IPv4Address;
import net.strasnet.kids.measurement.KIDSMeasurementOracle;
import net.strasnet.kids.measurement.KIDSUnEvaluableSignalException;

import org.apache.commons.lang3.NotImplementedException;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.util.SimpleIRIMapper;

/**
 * @author cstras
 *
 * Generates extended grep syntax strings for the KIDSNTEventLogDetector
 * @see net.strasnet.kids.detectors.KIDSGrepDetector
 * 
 */
public class KIDSWindowsEventLogDetectorSyntax implements KIDSDetectorSyntax {

	// Map of constraint feature -> type -> syntax
	//public static final Map <IRI, Map<IRI, ExtendedGrepSyntaxComponent>> validSignalClassMap = new HashMap<IRI, Map<IRI, ExtendedGrepSyntaxComponent>>();
	private static String sigClassIRI = "http://solomon.cs.iastate.edu/ontologies/KIDS.owl#";
	private static String SRCIPDomain = sigClassIRI + "IPv4SourceAddressSignalDomain";
	private static String GetParamDomain = sigClassIRI + "HTTPGetParameter";
	
	/** Add the valid feature->type->syntax components we are able to represent. */
	//HashMap<IRI, ExtendedGrepSyntaxComponent> tempMap = new HashMap<IRI, ExtendedGrepSyntaxComponent>();
	private KIDSMeasurementOracle myGuy = null;

	/* (non-Javadoc)
	 * @see net.strasnet.kids.detectorsyntaxproducers.KIDSDetectorSyntax#getDetectorSyntax(java.util.Set)
	 */
	@Override
	public String getDetectorSyntax(Set<IRI> sigSet)
			throws KIDSIncompatibleSyntaxException,
			KIDSOntologyObjectValuesException,
			KIDSOntologyDatatypeValuesException, KIDSUnEvaluableSignalException {

		throw new NotImplementedException("KIDSWindowsEventLogDetectorSyntax class is not yet implemented.");
/*
		// For each signal in the set:
		StringBuilder sb = new StringBuilder();
		
		// Keep track of which feature values we've seen:
		Map<IRI, String> valueMap = new HashMap<IRI, String>();
		
		// In this case, we end up with an extended regular expression, so we need to first check for the SRC=<IP> and then for the HTTPGetRequest=<string>
		for (IRI mySignal : sigSet){
			if (mySignal == null){
				continue;
			}
			// Look up the class for each signal in the set
			IRI sigDomain = myGuy.getSignalDomain(mySignal);
			IRI sigConstraint = myGuy.getSignalConstraint(mySignal);
			
			// Class gives us feature and constraint, now get the value of the signal
			String sVal = myGuy.getSignalValue(mySignal);
			
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
		if (valueMap.containsKey(IRI.create(KIDSWindowsEventLogDetectorSyntax.SRCIPDomain))){
			sb.append(valueMap.get(IRI.create(KIDSWindowsEventLogDetectorSyntax.SRCIPDomain)) + ".*");
		}
		if (valueMap.containsKey(IRI.create(KIDSWindowsEventLogDetectorSyntax.GetParamDomain))){
			sb.append(valueMap.get(IRI.create(KIDSWindowsEventLogDetectorSyntax.GetParamDomain)));
		}
		return sb.toString();
		*/
	}

	/* (non-Javadoc)
	 * @see net.strasnet.kids.detectorsyntaxproducers.KIDSDetectorSyntax#init(net.strasnet.kids.measurement.KIDSMeasurementOracle)
	 */
	@Override
	public void init(KIDSMeasurementOracle o) {
		throw new NotImplementedException("KIDSWindowsEventLogDetectorSyntax class is not yet implemented.");
		/*
		myGuy = o;

		// HTTPGetParameter Feature:
		tempMap.put(IRI.create(sigClassIRI + "stringMatch"), new HTTPGetParameterSyntaxComponent());
		validSignalClassMap.put(IRI.create(GetParamDomain), tempMap);
		
		// SourceIPAddress Feature:
		tempMap = new HashMap<IRI, ExtendedGrepSyntaxComponent>();
		tempMap.put(IRI.create(sigClassIRI + "IPv4AddressRangeSetSignalConstraint"),new SRCIPSyntaxComponent());
		validSignalClassMap.put(IRI.create(SRCIPDomain), tempMap);
		*/

	}

}
