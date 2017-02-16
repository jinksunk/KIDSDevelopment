package net.strasnet.kids.streaming;

/**
 * @author Chris Strasburg
 */

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

/**
 * TODO: Initialize tcpdump with the given monitoring point;
 * TODO: Produce a tcpdump configuration with the given set of signals
 * 
 */

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.strasnet.kids.KIDSOntologyDatatypeValuesException;
import net.strasnet.kids.KIDSOntologyObjectValuesException;
import net.strasnet.kids.detectors.KIDSAbstractDetector;
import net.strasnet.kids.detectors.KIDSTcpDumpDetector.ConnectionEndpoint;
import net.strasnet.kids.detectors.KIDSTcpDumpDetector.PortResource;
import net.strasnet.kids.detectors.UnimplementedIdentifyingFeatureException;
import net.strasnet.kids.detectorsyntaxproducers.KIDSIncompatibleSyntaxException;
import net.strasnet.kids.measurement.DataInstance;
import net.strasnet.kids.measurement.KIDSMeasurementOracle;
import net.strasnet.kids.measurement.KIDSUnEvaluableSignalException;
import net.strasnet.kids.measurement.datasetinstances.KIDSNativeLibpcapDataInstance;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.semanticweb.owlapi.model.IRI;

public class KIDSTCPDumpStreamingDetector extends StreamingDetectorAbstractClass implements
		StreamingDetectorInterface, Runnable {
	
	private static final Logger logme = LogManager.getLogger(KIDSTCPDumpStreamingDetector.class.getName());
	private static String regexPatternLine1 = "(?<TimeStamp>[\\d\\.]+)\\sIP\\s\\((([^,]+),){2}\\sid\\s(?<PID>\\d+),.*proto[^\\(]+\\((?<PROTO>\\d+)\\), length\\s(?<DATALEN>\\d+)\\).*";
	private static String regexPattern = "\\s*(?<SIP>\\d+\\.\\d+\\.\\d+\\.\\d+)\\.?(?<SPORT>\\d+)?\\s+>\\s+(?<DIP>\\d+\\.\\d+\\.\\d+\\.\\d+)\\.?(?<DPORT>\\d+)?.*";
	
	// 130.130.130.130.20 > 131.131.131.131.80: Flags [S], cksum 0x4e3f (correct), seq 0:389, win 8192, length 389
	//private static String regexPatternIgnore = "\\s+[\\d\\.]+\\s>\\s[\\d\\.]+:\\sFlags\\s.*";
	private static String regexPatternIgnore = ".*";
	
	private Pattern rexpL1 = null;
	private Pattern rexp = null;
	private Pattern rexpIgnore = null;

	private String monitoringPoint = null;
	private Set<IRI> signalSet = null;
	private StreamingInstanceStoreInterface myStore = null;

	/**
	 * Main constructor; will initialize the monitoring point string and set of signals to evaluate.
	 */
	public KIDSTCPDumpStreamingDetector (String toExecute, IRI detectorIRI, KIDSMeasurementOracle o, String monitorPoint, Set<IRI> signals, StreamingInstanceStoreInterface ourStore) throws KIDSOntologyObjectValuesException, KIDSOntologyDatatypeValuesException, InstantiationException, IllegalAccessException, ClassNotFoundException {
		logme.info("New KIDSTCPDumpStreamingDetector created, logging with level " + logme.getLevel());
			super.init(toExecute, detectorIRI, o);
			rexpL1 = Pattern.compile(regexPatternLine1);
			rexp = Pattern.compile(regexPattern);
			rexpIgnore = Pattern.compile(regexPatternIgnore);
			ourSyn = o.getDetectorSyntax(ourIRI);
			myStore = ourStore;

		// TODO: Check monitoring point for validity
		
		monitoringPoint = monitorPoint;
		
		// TODO: Check signal set against the ontology
		
		signalSet = signals;
		
	}

	@Override
	public void run() {
		// TODO: Need to handle a set of signals, not just one:
		IRI signal = signalSet.iterator().next();

		// TODO: Make sure the monitoring point and signal sets are defined.
		logme.info(String.format("Determining matching instances on interface %s for %s",monitoringPoint, signal));

		Set<IRI> signals = new HashSet<IRI>();
		signals.add(signal);

		// Run the command with the detector specification
		String command = null;
		Process genPcap = null;
			//command = executionCommand + " -v -tt -n -n -i " + v.getViewLocation() + " " + ourSyn.getDetectorSyntax(signals) + " ";
		int lcount = 0;
		int icount = 0;
		int cvaluesUsed = 0;
		int statusAt = 1000;
		int dvaluesInSet = 0;
		int evaluesInSet = 0;
		long t0 = 0;
		long t1 = 0;
		try {
			String[] toExec = {executionCommand,"-v","-tt","-n","-n","-i",monitoringPoint,ourSyn.getDetectorSyntax(signals)};

		    logme.debug("Executing command [" + StringUtils.join(toExec," ") + "] ...");
		    genPcap = Runtime.getRuntime().exec(toExec);
		    BufferedReader rd = new BufferedReader( new InputStreamReader( genPcap.getInputStream() ) );
		    String pcapLine;
		    t0 = System.currentTimeMillis();

		    // Get the packet ID for each packet, and create the data instance object for it
		    while ((pcapLine = rd.readLine()) != null){
			    lcount++;
		    	logme.debug(String.format("Processing line %d...", lcount));
			    if (lcount % statusAt == 0 || System.currentTimeMillis() - t0 > 1000){
				    t1 = System.currentTimeMillis();
				    double lps = statusAt / (((double)t1 - (double)t0) / 1000);
				    //double pps = icount / (((double)t1 - (double)t0) / 1000);
				    logme.debug(" .. Processed " + lcount + " lines (" + lps + " lps) and " + icount + " instances " );
				    		//+ "(" + pps + " ips)");
				    t0 = t1;
				    if (Thread.interrupted()){
				    	// We've been interrupted, bail!
				    	logme.info("Thread interrupted; ending process and exiting."); 
				    	genPcap.destroy();
				    	throw new InterruptedException();
				    }
			    }
			    Matcher rexmpre = rexpL1.matcher(pcapLine);
			    Matcher rexi = rexpIgnore.matcher(pcapLine);
			    if (rexmpre.matches()){
				    // First of the two lines extract resources.
			        String packetID = rexmpre.group("PID");
    
			        // Setup resource hashmap:
				    HashMap<IRI,String> resMap = new HashMap<IRI,String>();
				    String tsString = rexmpre.group("TimeStamp");
				    //resMap.put(IRI.create(featureIRI + "instanceTimestamp"),tsString.substring(0, tsString.indexOf('.')));
				    resMap.put(IRI.create(featureIRI + "instanceTimestamp"),tsString);
			        resMap.put(IRI.create(featureIRI + "PacketID"),packetID);
			        
			        // Get the second part
				    pcapLine = rd.readLine();
				    Matcher rexmpost = rexp.matcher(pcapLine);

				    if (rexmpost.matches()){
				    	icount++;
					    // Extract resources
				    	// Start with identifying features:
					    resMap.put(IRI.create(featureIRI + "IPv4SourceAddressSignalDomain"), rexmpost.group("SIP"));
					    //idmap.put(v.getIdentifyingFeatures().get(2), rexmpost.group("SIP"));
					    resMap.put(IRI.create(featureIRI + "IPv4DestinationAddressSignalDomain"), rexmpost.group("DIP"));
					    //idmap.put(v.getIdentifyingFeatures().get(3), rexmpost.group("DIP"));
					    
					    // Next, features common to all packets:
					    resMap.put(IRI.create(featureIRI + "PacketLengthSignalDomain"), rexmpre.group("DATALEN"));

					    // Need to check that this is actually a TCP packet - otherwise should set the DPORT to null (?)
					    int ipProtocol = Integer.parseInt(rexmpre.group("PROTO"));
				    	resMap.put(IRI.create(featureIRI + "IPProtocol"), "" + ipProtocol);
				    	PortResource op;
					    if (ipProtocol == 6){  // TCP
					    	op = PortResource.TCP;
					    } else if (ipProtocol == 17){ // UDP
					    	op = PortResource.UDP;
					    } else {
					    	op = PortResource.UNKNOWN;
					    }
				    	resMap.put(IRI.create(featureIRI + op.getPortResource(ConnectionEndpoint.DEST) + "Port"), rexmpost.group("DPORT"));
				    	resMap.put(IRI.create(featureIRI + op.getPortResource(ConnectionEndpoint.SOURCE) + "Port"), rexmpost.group("SPORT"));

						List<IRI> orderKey = new LinkedList<IRI>();
						orderKey.add(IRI.create(featureIRI + "PacketID"));
						orderKey.add(IRI.create(featureIRI + "IPv4SourceAddressSignalDomain"));
						orderKey.add(IRI.create(featureIRI + "IPv4DestinationAddressSignalDomain"));
						
						super.addOrderKeyToIDMap(orderKey, resMap);

				    	DataInstance di = new KIDSNativeLibpcapDataInstance(resMap);
				    	DataInstance sdi = KIDSAbstractDetector.getDataInstance(di);
				    	if (sdi != di){ // Just check to see if we used a cached instance or not
				    		cvaluesUsed++;
				    	}
				    	myStore.addStreamingInstance(sdi, signals);
				    } else {
					    logme.error("Only first line matched (second is listed here): " + pcapLine);
					    throw new IOException("Only first line matched (second is listed here): " + pcapLine);
				    }
			    } else if (rexi.matches()){
				    // Ignore other lines
			    	// logme.debug("Skipping line " + pcapLine);
				    continue;
			    } else {
			    	logme.error("Packet ID extraction error from " + pcapLine);
				    throw new IOException("Could not extract packet ID from line: " + pcapLine);
			    }
		    }
		
		    if (genPcap.waitFor() != 0){
			    BufferedReader errd = new BufferedReader (new InputStreamReader(genPcap.getErrorStream()));
			    String errout;
			    while ((errout = errd.readLine()) != null){
			        logme.debug(errout);
			    }
		    }
			//logme.debug(String.format(" - Used %d / %d cached values (DataInstance pool size now: %d)",cvaluesUsed,icount, KIDSAbstractDetector.getDataInstancePoolSize()));
			//logme.debug(String.format(" - %d duplicate instances found",dvaluesInSet));
			//logme.debug(String.format(" - %d event-related instances found",evaluesInSet));
			//logme.debug(String.format(" - %d total instances found",toReturn.size()));
			//logme.info(String.format("Returning %d instances.",toReturn.size()));
		    //return toReturn;
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
		} catch (UnimplementedIdentifyingFeatureException e) {
			logme.error("Cannot process data elements, unimplemented identfying feature: " + e.getFeature());
		}
		logme.debug("Fell out of the read loop for tcpdump");
	}
}
