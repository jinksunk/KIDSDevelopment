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

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.util.SimpleIRIMapper;

/**
 * @author cstras
 *
 * Generates extended grep syntax strings for the KIDSNTEventLogDetector
 * @see net.strasnet.kids.detectors.KIDSGrepDetector
 * 
 */
public class KIDSGrepSyntax implements KIDSDetectorSyntax {

	public interface ExtendedGrepSyntaxComponent {
		public String getSyntaxForm (String sVal, IRI signalConstraint);
	}
	
	public class HTTPGetParameterSyntaxComponent implements ExtendedGrepSyntaxComponent {
		
		private String template = "HTTPGetParameter=%s";
		@Override
		public String getSyntaxForm(String sVal, IRI signalConsraint){
			return String.format(template,sVal);
		}
	}
	
	public class SRCIPSyntaxComponent implements ExtendedGrepSyntaxComponent {

		private String template = "SRC=%s";
		private Pattern p = Pattern.compile("\\[(?<IPRanges>(([\\d\\.\\/]+),)*([\\d\\.\\/]+))\\]");

		@Override
		public String getSyntaxForm(String sVal, IRI signalConsraint){
			Set<String> octetValues = new HashSet<String>();
			// Parse out the range set thing.  The canonical form is:
			// [aaa.bbb.ccc.ddd/www.xxx.yyy.zzz,...]  So, first split on comma, then determine how many class 'a', 'b', 'c', 'd', etc... there are, and 
			// build the string from that.  It might be a little complicated...
			Matcher m = p.matcher(sVal);
			if (m.matches()){
				String rangeSet = m.group("IPRanges");
				String[] ranges = rangeSet.split(",");

				for (String range : ranges){
					// Determine how many 'classes' are included in the netmask.  If it is all '1's, just include the full IP.  If it is 
					// an exact class 'B', just include the first two octets.  Otherwise, list the octets at the appropriate level.
					String[] components = range.split("/");
					String ip = components[0];
					String nm = components[1];
					// How many bits are in the netmask?
					int[] componentList = IPv4Address.toArray(nm);
					int componentIndex = 0;
					for (int i = 3; i >= 0; i--){
						if (componentList[i] != 0){
							componentIndex = i;
							i = -1;
						}
					}
					// Component index is = the last non-0 octet in the netmask
					
					// If the component index is 255, we just have one thing to list. Otherwise, if it is 'n', we have 255 - n things to list.
					int[] ipOctetList  = IPv4Address.toArray(ip);
					StringBuilder retExpression = new StringBuilder();

					// All the 255 octet values in the netmask can be copied over as-is from the IP address.
					int i = 0;
					for (i = 0; i <= componentIndex && componentList[i] == 255; i++){
						retExpression.append(ipOctetList[i] + ".");
					}
					
					// If there is a non-0 and non-255 value in the netmask, that octet must be enumerated into discreet values:
					if (i < componentList.length && componentList[i] != 0){
						String ipPrefix = retExpression.toString();
						for (int j = 0; j <= (255 - componentList[i]); j++){
						    octetValues.add(ipPrefix + String.valueOf(ipOctetList[i] + j));
						}
					} else {
						retExpression.deleteCharAt(retExpression.lastIndexOf("."));
						octetValues.add(retExpression.toString());
					}
				}
			}
			
			StringBuilder sValBuilder = new StringBuilder();
			sValBuilder.append("(");
			for (String ipVal : octetValues){
				sValBuilder.append(ipVal + "|");
			}
			sValBuilder.deleteCharAt(sValBuilder.lastIndexOf("|"));
			sValBuilder.append(")");
			return String.format(template, sValBuilder);
		}
		
	}
	
	// Map of constraint feature -> type -> syntax
	public static final Map <IRI, Map<IRI, ExtendedGrepSyntaxComponent>> validSignalClassMap = new HashMap<IRI, Map<IRI, ExtendedGrepSyntaxComponent>>();
	private static String sigClassIRI = "http://solomon.cs.iastate.edu/ontologies/KIDS.owl#";
	private static String SRCIPDomain = sigClassIRI + "IPv4SourceAddressSignalDomain";
	private static String GetParamDomain = sigClassIRI + "HTTPGetParameter";
	
	/** Add the valid feature->type->syntax components we are able to represent. */
	HashMap<IRI, ExtendedGrepSyntaxComponent> tempMap = new HashMap<IRI, ExtendedGrepSyntaxComponent>();
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
		
		// In this case, we end up with an extended regular expression, so we need to first check for the SRC=<IP> and then for the HTTPGetRequest=<string>
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
		if (valueMap.containsKey(IRI.create(KIDSGrepSyntax.SRCIPDomain))){
			sb.append(valueMap.get(IRI.create(KIDSGrepSyntax.SRCIPDomain)) + ".*");
		}
		if (valueMap.containsKey(IRI.create(KIDSGrepSyntax.GetParamDomain))){
			sb.append(valueMap.get(IRI.create(KIDSGrepSyntax.GetParamDomain)));
		}
		return sb.toString();
	}

	/* (non-Javadoc)
	 * @see net.strasnet.kids.detectorsyntaxproducers.KIDSDetectorSyntax#init(net.strasnet.kids.measurement.KIDSMeasurementOracle)
	 */
	@Override
	public void init(KIDSMeasurementOracle o) {
		myGuy = o;

		// HTTPGetParameter Feature:
		tempMap.put(IRI.create(sigClassIRI + "stringMatch"), new HTTPGetParameterSyntaxComponent());
		validSignalClassMap.put(IRI.create(GetParamDomain), tempMap);
		
		// SourceIPAddress Feature:
		tempMap = new HashMap<IRI, ExtendedGrepSyntaxComponent>();
		tempMap.put(IRI.create(sigClassIRI + "IPv4AddressRangeSetSignalConstraint"),new SRCIPSyntaxComponent());
		validSignalClassMap.put(IRI.create(SRCIPDomain), tempMap);

	}

}
