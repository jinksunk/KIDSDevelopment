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
import net.strasnet.kids.measurement.test.KIDSTestSingleSignal;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
public class KIDSGrepDetector extends KIDSAbstractDetector implements KIDSDetector {
	//private static String featureIRI = "http://solomon.cs.iastate.edu/ontologies/KIDS.owl#";
	private static final Logger logme = LogManager.getLogger(KIDSGrepDetector.class.getName());
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
		supportedFeatures.put(featureIRI + "NTEventLogRecordID",true);
		supportedFeatures.put(featureIRI + "instanceTimestamp", true);
		};
		
	private Pattern rexp = null;
	private Pattern rexpIgnore = null;
	private KIDSDetectorSyntax ourSyn = null; 
	
	/* (non-Javadoc)
	 * @see net.strasnet.kids.detectors.KIDSDetector#getMatchingInstances(java.util.Set, net.strasnet.kids.measurement.datasetviews.DatasetView)
	 */
	@Override
	public Set<DataInstance> getMatchingInstances(Set<IRI> signals,
			DatasetView v) throws IOException,
			KIDSOntologyObjectValuesException,
			KIDSOntologyDatatypeValuesException,
			KIDSIncompatibleSyntaxException, KIDSUnEvaluableSignalException, UnimplementedIdentifyingFeatureException {

		// First, check to see if we have already run the detector on this set of signals - if so, no need to run it again:
		Set<DataInstance> toReturn = super.getMatchingInstances(signals, v);
		if (toReturn != null){
			return toReturn;
		} else {
			toReturn = new HashSet<DataInstance>();
		}
		
		boolean firstSignal = true;
		
		for (IRI signal : signals){
			Set<DataInstance> results = null;
			if (this.sigMap.containsKey(signal)){
				results = this.sigMap.get(signal);
			} else {
				results = getMatchingInstances(signal, v);
				logme.debug(String.format("Adding cache entry of size %d for %s",results.size(), signal));
				this.sigMap.put(signal, results);
				StringBuilder lm = new StringBuilder();
				lm.append("\t(Signal cache now consists of:");
				for (IRI cMapEntry : this.sigMap.keySet()){
					lm.append(String.format("\t%s ;",cMapEntry));
				}
				logme.debug(String.format("%s\n)",lm));

			}
			if (firstSignal){
				toReturn.addAll(results);
				firstSignal = false;
			} else {
				toReturn.retainAll(results);
			}
		}
		return toReturn;
	}
		
	private Set<DataInstance> getMatchingInstances(IRI signal, DatasetView v) throws
			IOException,
			KIDSOntologyObjectValuesException,
			KIDSOntologyDatatypeValuesException,
			UnimplementedIdentifyingFeatureException,
			KIDSIncompatibleSyntaxException, KIDSUnEvaluableSignalException {
		// Run the command with the detector specification
		//String toExec = executionCommand + " -E " +  "'" + ourSyn.getDetectorSyntax(signals) + "' " + v.getViewLocation();
		//String[] toExec = {" -E ",  ourSyn.getDetectorSyntax(signals), v.getViewLocation()};
		Set<IRI> signals = new HashSet<IRI>();
		signals.add(signal);
		String[] toExec = {executionCommand, "-E",  ourSyn.getDetectorSyntax(signals), v.getViewLocation()};
		//String[] toExec2 = {executionCommand, "-E",  ourSyn.getDetectorSyntax(signals), "/Users/cstras/Box Sync/Academic/research/papers/2013-MeasurementPaper/experiments/CodeRedEvent-Dataset1/test/test2.log"};
		Process genPcap = Runtime.getRuntime().exec(toExec);
		BufferedReader rd = new BufferedReader( new InputStreamReader( genPcap.getInputStream() ) );
		String Line;
		Set<DataInstance> toReturn = new HashSet<DataInstance>();
		int count = 0;
		int cvaluesUsed = 0;

		try {
			// Get the log entry ID for each line, and create the data instance object for it
			// Extract out at least the ID, Source IP (if present), and HTTPGetRequestResource (if present)
			while ((Line = rd.readLine()) != null){
					count++;
					Map<IRI, String> rMap = extractResources(Line);
			    	KIDSNTEventLogDataInstance di = new KIDSNTEventLogDataInstance(rMap);
			    	DataInstance sdi = KIDSAbstractDetector.getDataInstance(di);
			    	if (sdi != di){
			    		cvaluesUsed++;
			    	}
			    	toReturn.add(sdi);
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
			if (genPcap.waitFor() != 0){
				logme.warn("Non-0 exit code from KIDSGrepDetector: '");
				StringBuilder lm = new StringBuilder();
				for (int i = 0; i < toExec.length; i++){
					lm.append(toExec[i] + " ");
				}

				BufferedReader errd = new BufferedReader (new InputStreamReader(genPcap.getErrorStream()));
				String errout;
				while ((errout = errd.readLine()) != null){
				    lm.append(String.format("%s\n",errout));
				}
				logme.warn(lm);
			}
			this.sigMap.put(signal, toReturn);
			logme.info(String.format("Used %d/%d cached values.",cvaluesUsed,count));
			return toReturn;
		} catch (InterruptedException e) {
			String em = String.format("Command interrupted: %s", this.executionCommand); 
			logme.warn(em);
			throw new IOException();
		}
		
	}

	/* (non-Javadoc)
	 * @see net.strasnet.kids.detectors.KIDSDetector#init(java.lang.String, org.semanticweb.owlapi.model.IRI, net.strasnet.kids.measurement.KIDSMeasurementOracle)
	 */
	@Override
	public void init(String toExecute, IRI detectorIRI, KIDSMeasurementOracle o)
			throws KIDSOntologyObjectValuesException,
			KIDSOntologyDatatypeValuesException, InstantiationException,
			IllegalAccessException, ClassNotFoundException {
		super.init(toExecute, detectorIRI, o);

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
	 * @throws IOException 
	 */
	private Map<IRI, String> extractResources(String dataLine) throws IOException{
		Map<IRI,String> returnValue = new HashMap<IRI,String>();
		Matcher rexm = rexp.matcher(dataLine);
		if (rexm.matches()){
		   	String logLineID = rexm.group(1);
			// Add the identifying features for the instance
		   	logme.debug(String.format("Read value for NTEventLogRecordID = %s",logLineID));
		   	returnValue.put(IRI.create(featureIRI + "NTEventLogRecordID"), logLineID);

	//			} else if (rexi.matches()){
	//				// Ignore other lines
	//				continue;
		} else {
			String em = String.format("Could not extract Log ID from line: %s", dataLine);
			logme.warn(em);
			throw new IOException(em);
		}
		
		// Look for each other resource in the line:
		String[] commaFields = dataLine.split(",",5);
		returnValue.put(IRI.create(KIDSAbstractDetector.featureIRI + "#instanceTimestamp"), commaFields[1]);
	   	logme.debug(String.format("Read value for timestamp = %s",commaFields[1]));

		//TODO: Extract LogLine ID from the line:
    	//TODO: At some point, need to add resources other than ID features
		String[] kvFields = commaFields[4].split(",");

		for (String kvField : kvFields){
			// Extract known keys from the kvpair part of the log entry
			String[] kvPair = kvField.split("=");
			if (kvPair[0].equals("SRC")){
				returnValue.put(IRI.create(KIDSAbstractDetector.featureIRI + "#IPv4SourceAddressSignalDomain"), kvPair[1]);
				logme.debug(String.format("Read value for IPv4SourceAddressSignalDomain = %s",commaFields[1]));
			} else if (kvPair[0].equals("HTTPGetParameter")){
				returnValue.put(IRI.create(KIDSAbstractDetector.featureIRI + "#HTTPGetParameterSignalDomain"), kvPair[1]);
				logme.debug(String.format("Read value for HTTPGetParameter = %s",commaFields[1]));
			}
		}
		
		return returnValue;
	}

	@Override
	public IRI getIRI() {
		return ourIRI;
	}
	
}
