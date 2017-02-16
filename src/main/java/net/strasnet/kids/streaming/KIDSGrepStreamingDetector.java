/**
 * 
 */
package net.strasnet.kids.streaming;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.strasnet.kids.KIDSOntologyDatatypeValuesException;
import net.strasnet.kids.KIDSOntologyObjectValuesException;
import net.strasnet.kids.detectors.KIDSAbstractDetector;
import net.strasnet.kids.detectors.UnimplementedIdentifyingFeatureException;
import net.strasnet.kids.detectorsyntaxproducers.KIDSDetectorSyntax;
import net.strasnet.kids.detectorsyntaxproducers.KIDSIncompatibleSyntaxException;
import net.strasnet.kids.measurement.DataInstance;
import net.strasnet.kids.measurement.KIDSMeasurementOracle;
import net.strasnet.kids.measurement.KIDSUnEvaluableSignalException;
import net.strasnet.kids.measurement.datasetinstances.KIDSNTEventLogDataInstance;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.semanticweb.owlapi.model.IRI;

/**
 * @author Chris Strasburg
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
 *  
 *  In order to consume streaming data in a platform independent way, this class uses a local network socket to received data
 *  on.
 */
public class KIDSGrepStreamingDetector extends StreamingDetectorAbstractClass
		implements StreamingDetectorInterface {

	//private static String featureIRI = "http://solomon.cs.iastate.edu/ontologies/KIDS.owl#";
	private static final Logger logme = LogManager.getLogger(KIDSGrepStreamingDetector.class.getName());
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
	private KIDSDetectorSyntax ourSyn = null; 

	private String monitorhost = null;
	private int monitorport = 0;
	private Set<IRI> signalSet = null;
	private StreamingInstanceStoreInterface myStore = null;
	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	
	/**
	 * 
	 * @param toExecute - The executable path for grep
	 * @param detectorIRI - the IRI of our detector reference in the ontology
	 * @param o - the ontology oracle (interface for queries)
	 * @param monitorPoint - A socket address to listen on, in the form <hostname>:<portnum>
	 * TODO: Consider making this more abstract; a File handle perhaps? 
	 * @param signals - The set of signals to monitor for
	 * @param ourStore - A reference to the data instance store for this detector
	 * 
	 * TODO: Document the exceptions too
	 * @throws KIDSOntologyObjectValuesException
	 * @throws KIDSOntologyDatatypeValuesException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws ClassNotFoundException
	 */
	public KIDSGrepStreamingDetector (String toExecute, IRI detectorIRI, KIDSMeasurementOracle o, String monitorPoint, Set<IRI> signals, StreamingInstanceStoreInterface ourStore) throws KIDSOntologyObjectValuesException, KIDSOntologyDatatypeValuesException, InstantiationException, IllegalAccessException, ClassNotFoundException {
		logme.info("New " + KIDSGrepStreamingDetector.class.getName() + " created, logging with level " + logme.getLevel());
		super.init(toExecute, detectorIRI, o);
		rexp = Pattern.compile(regexPattern);
		ourSyn = o.getDetectorSyntax(ourIRI);
		myStore = ourStore;

		// Extract hostname and port from monitor point:
		String[] hostport = monitorPoint.split(":");
		
		if (hostport.length == 2){
			int portnum = Integer.parseInt(hostport[1]);
			monitorhost = hostport[0];
			monitorport = portnum;
		} else {
			String msg = "Monitoring point" + monitorPoint + " could not be parsed. Ensure the form <hostname>:<port> is used.";
			throw new InstantiationException(msg);
		}
		
		// TODO: Check signal set against the ontology
		signalSet = signals;
		
	}
	
	@Override
	public void run() {
		// TODO: Need to handle a set of signals, not just one:
		IRI signal = signalSet.iterator().next();

		// TODO: Make sure the monitoring point and signal sets are defined.
		logme.info(String.format("Determining matching instances on interface %s:%d for %s",monitorhost, monitorport, signal));

		Set<IRI> signals = new HashSet<IRI>();
		signals.add(signal);

		// Run the command with the detector specification
		String command = null;
		Process genGrep = null;
		int lcount = 0;
		int icount = 0;
		int cvaluesUsed = 0;
		int statusAt = 100;
		int dvaluesInSet = 0;
		int evaluesInSet = 0;
		long t0 = 0;
		long t1 = 0;
		ServerSocket oursock = null;
		
		// What we need to do is:
		// 0. Open the socket and give the output stream  to a class which passes data to a filtered input stream... so -
		//      Create a BufferedOutputStream (PipedOutputStream) from the socket. Create the filtered input stream connected to the
		//      buffered output stream. FilterInputStream(BufferedInputStream(PipeInputStream(out))).
		// 1. Run all the input strings from the socket through grep;
		// 2. For each matching line, process it to extract resources;
		// 3. If the grep process dies, get the error and end the loop
		// So - We can make a filtered output stream -> grep -> if grep matches, write to given input stream
		
		/*
		try {
		    // Setup the socket connection:
		    oursock = new ServerSocket(monitorport);
		    logme.debug(String.format("Server listening on %s:%d", monitorhost, monitorport));
		    Socket client = null;
			client = oursock.accept();
			logme.debug("Client connected...");

            // Setup pipes:
		    String[] toExec = {executionCommand, "-a", "-E",  ourSyn.getDetectorSyntax(signals), "-"};
		    logme.debug("Executing command [" + StringUtils.join(toExec," ") + "] ...");
			BufferedReader in = new BufferedReader(
										new InputStreamReader(
											new KIDSProcessFilterReader(
												client.getInputStream()
											)
										)
									);
		    genGrep = Runtime.getRuntime().exec(toExec);
			
			// Read / process data
            String logline = null;
            

            while ((logline = sockin.readLine()) != null){
            	// Perform line format processing
            	logme.debug(String.format("Read line: %s", logline));
            	String newline = processLine(logline);
            	logme.debug(String.format("Writing line: %s (%d bytes)", newline, newline.getBytes().length));
            	this.target.write(newline.getBytes(), 0, newline.length());
            	this.target.flush();
            }

		    
		    // What this does, is connect TCPSOCKET -> sout -> inrd
		    // TODO: What we want, is connect TCPSOCKET -> grep -> sout -> inrd
		    PipedOutputStream sout = new PipedOutputStream();
		    BufferedReader inrd = new BufferedReader(new InputStreamReader(new PipedInputStream(sout)));
		    KIDSGrepStreamDetectorInputStream in = new KIDSGrepFilterInputStream();

		    String line;
		    t0 = System.currentTimeMillis();

		    // Get the packet ID for each packet, and create the data instance object for it
		    while ((line = inrd.readLine()) != null){
			    lcount++;
		    	logme.debug(String.format("Processing line %d...", lcount));
			    if (lcount % statusAt == 0 || System.currentTimeMillis() - t0 > 1000){
				    t1 = System.currentTimeMillis();
				    double lps = statusAt / (((double)t1 - (double)t0) / 1000);
				    logme.debug(" .. Processed " + lcount + " lines (" + lps + " lps) and " + icount + " instances " );
				    t0 = t1;
				    if (Thread.interrupted()){
				    	// We've been interrupted, bail!
				    	logme.info("Thread interrupted; ending process and exiting."); 
				    	genGrep.destroy();
				    	throw new InterruptedException();
				    }
			    }
		    	// Feed line through grep; see if it matches.
			    String totest = String.format("%s%n",line);
                gout.write(totest, 0, totest.length());
                
                // Check to see if grep matched it:
                if (gin.ready()){
                	logme.debug(String.format("Found matching string: %s",totest));
                	String matchedLine = gin.readLine();

                	try{
                		Map<IRI, String> rMap = extractResources(matchedLine);
                		logme.debug("Extracted resources from line.");
			    	    KIDSNTEventLogDataInstance di = new KIDSNTEventLogDataInstance(rMap);
			    	    DataInstance sdi = KIDSAbstractDetector.getDataInstance(di);
			    	    if (sdi != di){
			    		    cvaluesUsed++;
			    	    }
			    	    myStore.addStreamingInstance(sdi, signals);
			        } catch (IOException e) {
			    	    logme.warn(String.format("Couldn't parse line: %s", matchedLine)); 
			        }
                }
		    }
		
		    inrd.close();
		} catch (InterruptedException e) {
			logme.info("Command interrupted: " + command);
			logme.debug(String.format(" - Used %d / %d cached values (DataInstance pool size now: %d)",cvaluesUsed,icount, KIDSAbstractDetector.getDataInstancePoolSize()));
			logme.debug(String.format(" - %d duplicate instances found",dvaluesInSet));
			logme.debug(String.format(" - %d event-related instances found",evaluesInSet));
		} catch (KIDSIncompatibleSyntaxException e) {
			logme.warn("Could not represent all signals in signal set; returning empty matching instances.");
			logme.warn("    - Detector: " + this.getClass().getName() + " with signals: ");
			for (IRI s : signals){
				logme.warn("    - " + s.toString());
			}
			this.sigMap.put(signal, new HashSet<DataInstance>());
			//return new HashSet<DataInstance>();
		} catch (KIDSUnEvaluableSignalException e){
			logme.error("Cannot evaluate signal " + signal + "; skipping...");
			return;
		} catch (KIDSOntologyObjectValuesException e) {
			logme.error("Object values exception getting syntax for " + signal + "; skipping...");
		} catch (KIDSOntologyDatatypeValuesException e) {
			logme.error("Datatype values exception getting syntax for " + signal + "; skipping...");
		} catch (IOException e) {
			logme.error("Problem getting data from process: " + e);
		    if (genGrep.waitFor() != 0){
			    BufferedReader errd = new BufferedReader (new InputStreamReader(genGrep.getErrorStream()));
			    String errout;
			    while ((errout = errd.readLine()) != null){
			        logme.debug(errout);
			    }
		    }
		} catch (UnimplementedIdentifyingFeatureException e) {
			logme.error("Cannot process data elements, unimplemented identfying feature: " + e.getFeature());
		}*/
		logme.debug("Fell out of the read loop");
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

}
