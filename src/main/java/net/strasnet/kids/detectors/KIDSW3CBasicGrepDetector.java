/**
 * 
 */
package net.strasnet.kids.detectors;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
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
import net.strasnet.kids.measurement.datasetinstances.KIDSW3CBasicDataInstance;
import net.strasnet.kids.measurement.datasetviews.DatasetView;
import net.strasnet.kids.measurement.test.KIDSTestSingleSignal;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.semanticweb.owlapi.model.IRI;

/**
 * @author cstras
 * 
 * This will utilize grep to extract resources from log lines produced in the W3C default form:
 * 
 * Date Time SourceIP ClientUsername Destip DestPort RequestType RequestResrouce GetQueryString ServerCode ClientUserAgent
 * 1999-03-04 07:03:20 172.16.112.100 - 207.46.130.14 80 GET /kids/reativewriter/ - 200 -
 * 
 * Resources extracted include:
 * 
 *  * TimeStamp in Unix EPOCH format (converted from date-time)
 *  * IPv4SourceAddressSignalDomain
 *  * HTTPClientUsernameResource
 *  * IPv4DestinationAddressSignalDomain
 *  * TCPDestinationPortSignalDomain
 *  * HTTPClientRequestTypeResource
 *  * HTTPGetParameterResource
 *  * HTTPGetQueryParameterResource
 *  * HTTPServerResponseCodeResource
 *  * HTTPClientUserAgentResource
 *  
 *  Dashes indicate that the data is not available.
 *  
 *  Key identifying resources: 
 *    * TimeStamp (seconds) ; 
 *    * IPv4SourceAddress ; 
 *    * IPv4DestinationResource ; 
 *    * TCPDestinationPort ;
 *    * OrderID [generated]; 
 */
public class KIDSW3CBasicGrepDetector extends KIDSAbstractDetector implements KIDSDetector {
	//private static String featureIRI = "http://solomon.cs.iastate.edu/ontologies/KIDS.owl#";
	private static final Logger logme = LogManager.getLogger(KIDSW3CBasicGrepDetector.class.getName());

	// Map known keys in the kvPairs to the feature domains they represent
	private static final Map<String, IRI> knownKeyMap = new HashMap<String, IRI>();
	private static final IRI timestampIRI = IRI.create(featureIRI + "instanceTimestamp");

	static {
		knownKeyMap.put("SRC", IRI.create(featureIRI + "IPv4SourceAddressSignalDomain"));
		knownKeyMap.put("HTTPGetParameter", IRI.create(featureIRI + "HTTPGetParameter"));
		};

	// Map resources we know how to extract:
	private static final Map<String, Boolean> supportedFeatures = new HashMap <String, Boolean>();
	static {
		supportedFeatures.put(featureIRI + "IPv4SourceAddressSignalDomain",true);
		supportedFeatures.put(featureIRI + "HTTPGetParameter",true);
		supportedFeatures.put(featureIRI + "NTEventLogRecordID",true);
		supportedFeatures.put(timestampIRI.toString(), true);
		};
		
	private static final Map<IRI, Integer> IRIFieldMap = new HashMap <IRI, Integer>();
	static {
		IRIFieldMap.put(IRI.create(featureIRI + "IPv4SourceAddressSignalDomain"), 2);
		IRIFieldMap.put(IRI.create(featureIRI + "HTTPClientUsernameResource"), 3);
		IRIFieldMap.put(IRI.create(featureIRI + "IPv4DestinationAddressSignalDomain"), 4);
		IRIFieldMap.put(IRI.create(featureIRI + "TCPDestinationPort"), 5);
		IRIFieldMap.put(IRI.create(featureIRI + "HTTPClientRequestMethodResource"), 6);
		IRIFieldMap.put(IRI.create(featureIRI + "HTTPGetParameterReource"), 7);
		IRIFieldMap.put(IRI.create(featureIRI + "HTTPGetQueryStringResource"), 8);
		IRIFieldMap.put(IRI.create(featureIRI + "HTTPServerResponseCodeResource"), 9);
		IRIFieldMap.put(IRI.create(featureIRI + "HTTPClientUserAgentResource"), 10);
	};
	
	private static final DateFormat mydf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		
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
			    	KIDSW3CBasicDataInstance di = new KIDSW3CBasicDataInstance(rMap);
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
				logme.warn("Non-0 exit code from KIDSW3CBasicGrepDetector: '");
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

		ourSyn = o.getDetectorSyntax(ourIRI);

	}
	
	/**
	 * 
	 * @param dataLine A raw line of text data
	 * @return A Map of feature IRI -> value for the resources associated with this line
	 * 
	 * Example is:
	 * ID,Timestamp,EventID,EventCode,KVPairs
	 * 1999-03-04 07:03:20 172.16.112.100 - 207.46.130.14 80 GET /kids/reativewriter/ - 200 -
 	 * 
 	 * Resources extracted include:
 	 * 
 	 *  * TimeStamp in Unix EPOCH format (converted from date-time)
 	 *  * IPv4SourceAddressSignalDomain
 	 *  * HTTPClientUsernameResource
 	 *  * IPv4DestinationAddressSignalDomain
 	 *  * TCPDestinationPortSignalDomain
 	 *  * HTTPClientRequestTypeResource
 	 *  * HTTPGetParameterResource
 	 *  * HTTPGetQueryParameterResource
 	 *  * HTTPServerResponseCodeResource
 	 *  * HTTPClientUserAgentResource
	 * @throws IOException 
	 */
	private Map<IRI, String> extractResources(String dataLine) throws IOException{
		Map<IRI,String> returnValue = new HashMap<IRI,String>();
		
		Map<IRI, String> toReturn = new HashMap<IRI, String>();
		
		// First split the string according to spaces. There should be exactly 11 fields when finished.
		String[] commaFields = dataLine.split(" ",11);
		
		// Construct a datestamp from the date and time fields:
		try {
			Date t = mydf.parse(commaFields[0] + " " + commaFields[1]);
			toReturn.put(timestampIRI, null);
		} catch (ParseException e) {
			logme.error(String.format("Could not parse date format: %s %s: %s", commaFields[0], commaFields[1], e.getMessage()),e);
			e.printStackTrace();
		}
		
		// Return the map of field IRIs to values; if a value is '-', replace it with a null.
		for (IRI k : IRIFieldMap.keySet()){
			String val = commaFields[IRIFieldMap.get(k)];
			toReturn.put(k, val == "-" ? null : val);
		}
		
		return toReturn;

	}

	@Override
	public IRI getIRI() {
		return ourIRI;
	}
	
}
