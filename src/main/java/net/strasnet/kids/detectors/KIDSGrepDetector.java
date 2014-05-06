/**
 * 
 */
package net.strasnet.kids.detectors;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.strasnet.kids.KIDSOntologyDatatypeValuesException;
import net.strasnet.kids.KIDSOntologyObjectValuesException;
import net.strasnet.kids.detectorsyntaxproducers.KIDSDetectorSyntax;
import net.strasnet.kids.detectorsyntaxproducers.KIDSIncompatibleSyntaxException;
import net.strasnet.kids.measurement.DataInstance;
import net.strasnet.kids.measurement.KIDSMeasurementOracle;
import net.strasnet.kids.measurement.KIDSUnEvaluableSignalException;
import net.strasnet.kids.measurement.datasetinstances.KIDSNTEventLogDataInstance;
import net.strasnet.kids.measurement.datasetinstances.KIDSNativeLibpcapDataInstance;
import net.strasnet.kids.measurement.datasetviews.DatasetView;

import org.semanticweb.owlapi.model.IRI;

/**
 * @author cstras
 * 
 * This will utilize grep to identify log lines matching specific signals.  It expects log lines in the form:
 * LogEntryID,EpochTimestamp,EventID,EventCode,KVPair1[,...,KVPairN]
 * 
 * Each KVPair has the form <KEY>=<VALUE>.  For example:
 * 
 * 1,920593652,40000006,0004,SRC=151.117.166.116
 * 
 * Current feature values include: 
 *  * NTLogEntryID
 *  * TimeStamp in Unix EPOCH format
 *  * NTLogEventID
 *  * NTLogEventCode
 *  * IPv4SourceAddressSignalDomain - KEY 'SRC'
 *  * HTTPGetParameter - KEY 'HTTPGetParameter'
 */
public class KIDSGrepDetector implements KIDSDetector {
	private IRI ourIRI = null;
	private static String featureIRI = "http://solomon.cs.iastate.edu/ontologies/KIDS.owl#";
	private static String executionCommand;
	private static String regexPattern = "(\\d+),.*";

	// Map known keys in the kvPairs to the feature domains they represent
	private static final Map<String, IRI> knownKeyMap = new HashMap<String, IRI>();
	static {
		knownKeyMap.put("SRC", IRI.create(featureIRI + "IPv4SourceAddressSignalDomain"));
		knownKeyMap.put("HTTPGetParameter", IRI.create(featureIRI + "HTTPGetParameter"));
		};

	// Map features we know how to extract:
	private static final Map<String, Boolean> supportedFeatures = new HashMap <String, Boolean>();
	static {
		supportedFeatures.put(featureIRI + "IPv4SourceAddressSignalDomain",true);
		supportedFeatures.put(featureIRI + "HTTPGetParameter",true);
		};
		
	private Pattern rexp = null;
	private Pattern rexpIgnore = null;
	private KIDSMeasurementOracle myGuy = null;
	private KIDSDetectorSyntax ourSyn = null; 
	
	/* (non-Javadoc)
	 * @see net.strasnet.kids.detectors.KIDSDetector#getMatchingInstances(java.util.Set, net.strasnet.kids.measurement.datasetviews.DatasetView)
	 */
	@Override
	public Set<DataInstance> getMatchingInstances(Set<IRI> signals,
			DatasetView v) throws IOException,
			KIDSOntologyObjectValuesException,
			KIDSOntologyDatatypeValuesException,
			KIDSIncompatibleSyntaxException, KIDSUnEvaluableSignalException {

		Set<DataInstance> toReturn = new HashSet<DataInstance>();
		
		// Run the command with the detector specification
		//String toExec = executionCommand + " -E " +  "'" + ourSyn.getDetectorSyntax(signals) + "' " + v.getViewLocation();
		//String[] toExec = {" -E ",  ourSyn.getDetectorSyntax(signals), v.getViewLocation()};
		String[] toExec = {executionCommand, "-E",  ourSyn.getDetectorSyntax(signals), v.getViewLocation()};
		Process genPcap = Runtime.getRuntime().exec(toExec);

		try {
			if (genPcap.waitFor() != 0){
				System.err.print("Non-0 exit code from KIDSGrepDetector: '");
				for (int i = 0; i < toExec.length; i++){
					System.err.print(toExec[i] + " ");
				}
				System.err.println();

				BufferedReader errd = new BufferedReader (new InputStreamReader(genPcap.getErrorStream()));
				String errout;
				while ((errout = errd.readLine()) != null){
				    System.err.println(errout);
				}
			}
		} catch (InterruptedException e) {
			throw new IOException("Command interrupted: " + this.executionCommand);
		}
		BufferedReader rd = new BufferedReader( new InputStreamReader( genPcap.getInputStream() ) );
		String Line;
		
		// Get the log entry ID for each line, and create the data instance object for it
		// Extract out at least the ID, Source IP (if present), and HTTPGetRequestResource (if present)
		while ((Line = rd.readLine()) != null){
			Matcher rexm = rexp.matcher(Line);
			//Matcher rexi = rexpIgnore.matcher(Line);
			if (rexm.matches()){
			    String logLineID = rexm.group(1);
			    //TODO: At some point, need to add resources other than ID features
			    // Maps of feature IRIs to values
			    HashMap<IRI, String> idmap = new HashMap<IRI, String>();
			    // Add the identifying features for the instance
			    idmap.put(v.getIdentifyingFeatures().get(0), logLineID);
			    KIDSNTEventLogDataInstance newGuy = new KIDSNTEventLogDataInstance(idmap);
			    // Now add the extracted resources
			    newGuy.addResources(extractResources(Line));
			    toReturn.add(newGuy);
//			} else if (rexi.matches()){
//				// Ignore other lines
//				continue;
			} else {
				throw new IOException("Could not extract Log ID from line: " + Line);
			}
		}
		
		/**
		Iterator<DataInstance> i = toReturn.iterator();
		while (i.hasNext()){
			DataInstance iNext = i.next();
			Map<IRI,String> iResources = iNext.getResources();
			for (IRI k : iResources.keySet()){
				System.out.println(" -" + k + " :=: " + iResources.get(k));
			}
		}
		*/
		return toReturn;
	}

	/* (non-Javadoc)
	 * @see net.strasnet.kids.detectors.KIDSDetector#init(java.lang.String, org.semanticweb.owlapi.model.IRI, net.strasnet.kids.measurement.KIDSMeasurementOracle)
	 */
	@Override
	public void init(String toExecute, IRI detectorIRI, KIDSMeasurementOracle o)
			throws KIDSOntologyObjectValuesException,
			KIDSOntologyDatatypeValuesException, InstantiationException,
			IllegalAccessException, ClassNotFoundException {
		myGuy = o;
		ourIRI = detectorIRI;
	
		executionCommand = toExecute;
		rexp = Pattern.compile(regexPattern);
		//rexpIgnore = Pattern.compile(regexPatternIgnore);
		ourSyn = o.getDetectorSyntax(ourIRI);

	}
	
	/**
	 * 
	 * @param dataLine A raw line of text data
	 * @return A Map of feature IRI -> value for the resources associated with this line
	 * 
	 * Example is:
	 * ID,Timestamp,EventID,EventCode,KVPairs
     * 1,920593652,40000006,0004,SRC=151.117.166.116
	 */
	private Map<IRI, String> extractResources(String dataLine){
		Map<IRI,String> returnValue = new HashMap<IRI,String>();
		
		// Look for each resource in the line:
		String[] commaFields = dataLine.split(",",5);
		returnValue.put(IRI.create("http://solomon.cs.iastate.edu/ontologies/KIDS.owl#instanceTimestamp"), commaFields[1]);

		String[] kvFields = commaFields[4].split(",");

		for (String kvField : kvFields){
			// Extract known keys from the kvpair part of the log entry
			String[] kvPair = kvField.split("=");
			if (kvPair[0].equals("SRC")){
				returnValue.put(IRI.create("http://solomon.cs.iastate.edu/ontologies/KIDS.owl#IPv4SourceAddressSignalDomain"), kvPair[1]);
			} else if (kvPair[0].equals("HTTPGetParameter")){
				returnValue.put(IRI.create("http://solomon.cs.iastate.edu/ontologies/KIDS.owl#instanceTimestamp"), kvPair[1]);
			}
		}
		
		return returnValue;
	}
	
}
