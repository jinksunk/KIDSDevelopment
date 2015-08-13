package net.strasnet.kids.detectors;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.semanticweb.owlapi.model.IRI;

import net.strasnet.kids.KIDSOntologyDatatypeValuesException;
import net.strasnet.kids.KIDSOntologyObjectValuesException;
import net.strasnet.kids.detectorsyntaxproducers.KIDSDetectorSyntax;
import net.strasnet.kids.detectorsyntaxproducers.KIDSIncompatibleSyntaxException;
import net.strasnet.kids.measurement.DataInstance;
import net.strasnet.kids.measurement.KIDSMeasurementOracle;
import net.strasnet.kids.measurement.KIDSUnEvaluableSignalException;
import net.strasnet.kids.measurement.datasetinstances.KIDSNativeLibpcapDataInstance;
import net.strasnet.kids.measurement.datasetviews.DatasetView;
import net.strasnet.kids.measurement.datasetviews.NativeLibPCAPView;

public class KIDSTcpDumpDetector extends KIDSAbstractDetector implements KIDSDetector {

	/**
	 * Represents a detector utilizing the TCPDump command-line tool.  Associated with the syntax "bpf" - berkeley packet filter.
	 * TODO: Separate out regular expressions for different packet types
	 */
	
	//public Set<Map<IRI, String>> getMatchingInstances (Set<IRI> signals, NativeLibPCAPView v) throws IOException, KIDSOntologyObjectValuesException, KIDSOntologyDatatypeValuesException, KIDSUnEvaluableSignalException{
	//private static String featureIRI = "http://solomon.cs.iastate.edu/ontologies/KIDS.owl#";
	// 920592993.000000 IP (tos 0x0, ttl 64, id 4, offset 0, flags [none], proto TCP (6), length 429)
	private static final Logger logme = LogManager.getLogger(KIDSTcpDumpDetector.class.getName());
	private static String regexPatternLine1 = "(?<TimeStamp>[\\d\\.]+)\\sIP\\s\\((([^,]+),){2}\\sid\\s(?<PID>\\d+),.*proto[^\\(]+\\((?<PROTO>\\d+)\\), length\\s(?<DATALEN>\\d+)\\).*";
	private static String regexPattern = "\\s*(?<SIP>\\d+\\.\\d+\\.\\d+\\.\\d+)\\.?(?<SPORT>\\d+)?\\s+>\\s+(?<DIP>\\d+\\.\\d+\\.\\d+\\.\\d+)\\.?(?<DPORT>\\d+)?.*";
	
	// 130.130.130.130.20 > 131.131.131.131.80: Flags [S], cksum 0x4e3f (correct), seq 0:389, win 8192, length 389
	//private static String regexPatternIgnore = "\\s+[\\d\\.]+\\s>\\s[\\d\\.]+:\\sFlags\\s.*";
	private static String regexPatternIgnore = ".*";
	
	private static final Map<String, Boolean> supportedFeatures = new HashMap <String, Boolean>();
	static {
		supportedFeatures.put(featureIRI + "TCPPacketPayload",true);
		supportedFeatures.put(featureIRI + "PacketID",true);
		supportedFeatures.put(featureIRI + "IPv4SourceAddressSignalDomain",true);
		supportedFeatures.put(featureIRI + "IPv4DestinationAddressSignalDomain",true);
		supportedFeatures.put(featureIRI + "TCPDestinationPort",true);
		supportedFeatures.put(featureIRI + "instanceTimestamp",true);
		supportedFeatures.put(featureIRI + "PacketLengthSignalDomain",true);
		supportedFeatures.put(featureIRI + "TCPFlags", true);
		};
		
	private Pattern rexpL1 = null;
	private Pattern rexp = null;
	private Pattern rexpIgnore = null;
	
	public enum ConnectionEndpoint {

		DEST(1),
		SOURCE(0);
		
		private final String epValue;
		
		ConnectionEndpoint(int whichone){
			if (whichone == 0){
				epValue = "Source";
			} else if (whichone == 1){
				epValue = "Destination";
			} else {
				epValue = "Invalid";
			}
		}
		
		public String getEndpointString(){
			return epValue;
		}
	}
	
	public enum PortResource {
		TCP("TCP"),
		UDP("UDP"),
		UNKNOWN("UNKNOWN");
		
		private final String proto;
		
		PortResource(String protocol){
			proto = protocol;
		}
		
		/**
		 * @param endpoint - One of 'Destination' or 'Source'
		 */
		public String getPortResource(ConnectionEndpoint ce){
			return proto + ce.getEndpointString() + "Port";
		}
	};
	
	public KIDSTcpDumpDetector(){
		myGuy = null;
		ourIRI = null;
	}
	
	@Override
	public void init(String toExecute, IRI detectorIRI, KIDSMeasurementOracle o) throws KIDSOntologyObjectValuesException, KIDSOntologyDatatypeValuesException, InstantiationException, IllegalAccessException, ClassNotFoundException{
		super.init(toExecute, detectorIRI, o);
		rexpL1 = Pattern.compile(regexPatternLine1);
		rexp = Pattern.compile(regexPattern);
		rexpIgnore = Pattern.compile(regexPatternIgnore);
		ourSyn = o.getDetectorSyntax(ourIRI);
	}
	
	/**
	 * 
		// 08-24-2013 - I'm thinking that the detector should know it's syntax already, 
		// so we *should* be able to just pass in a set of signals.  The KB can be used to map signals to
		// syntaxes in the case of multiple.  
	 * @param detectorSpec
	 * @param v
	 * @return
	 * @throws IOException
	 * @throws KIDSIncompatibleSyntaxException 
	 * @throws KIDSOntologyDatatypeValuesException 
	 * @throws KIDSOntologyObjectValuesException 
	 * @throws KIDSUnEvaluableSignalException 
	 * @throws UnimplementedIdentifyingFeatureException 
	 */
	public Set<DataInstance> getMatchingInstances (Set<IRI> signals, NativeLibPCAPView v) throws IOException, KIDSOntologyObjectValuesException, KIDSOntologyDatatypeValuesException, KIDSUnEvaluableSignalException, KIDSIncompatibleSyntaxException, UnimplementedIdentifyingFeatureException{
		// First, check to see if we have already run the detector on this set of signals - if so, no need to run it again:
		logme.debug(String.format("TCPDumpDetector checking cache..."));
		Set<DataInstance> toReturn = super.getMatchingInstances(signals, v);
		if (toReturn != null){
			logme.info(String.format("Returning %d values cached for signal set in abstract detector.",toReturn.size()));
			return toReturn;
		} else {
			toReturn = new HashSet<DataInstance>();
		}
		boolean firstSignal = true;
		
		for (IRI signal : signals){
			Set<DataInstance> results = null;
			if (this.sigMap.containsKey(signal)){
				logme.info(String.format("TcpDumpDetector using cache entry (size = %d) for %s",this.sigMap.get(signal).size(),signal));
				results = this.sigMap.get(signal);
			} else {
				logme.info(String.format("TcpDumpDetector executing detector to gather instances. "));
				results = getMatchingInstances(signal, v);
				logme.info(String.format("[D] TcpDumpDetector - Added cache entry (size = %d) for %s",results.size(), signal));
				this.sigMap.put(signal, results);
				logme.debug("\t(Signal cache now consists of:");
				for (IRI cMapEntry : this.sigMap.keySet()){
					logme.debug(String.format("\t%s : %d ;",cMapEntry, this.sigMap.get(cMapEntry).size()));
				}
				logme.debug("\t)");
			}
			if (firstSignal){
				toReturn.addAll(results);
				firstSignal = false;
			} else {
				toReturn.retainAll(results);
			}
		}
		logme.info(String.format("Returning %d values.",toReturn.size()));
		return toReturn;
	}
		
	private Set<DataInstance> getMatchingInstances (IRI signal, NativeLibPCAPView v) throws IOException, KIDSOntologyObjectValuesException, KIDSOntologyDatatypeValuesException, KIDSUnEvaluableSignalException, UnimplementedIdentifyingFeatureException{
		
		logme.info(String.format("Determining matching instances for %s",signal));
		Set<DataInstance> toReturn = new HashSet<DataInstance>();
		Set<IRI> signals = new HashSet<IRI>();
		signals.add(signal);

		// Run the command with the detector specification
		String command = null;
		Process genPcap = null;
		try {
			//command = executionCommand + " -v -tt -n -n -r " + v.getViewLocation() + " " + ourSyn.getDetectorSyntax(signals) + " ";
			String[] toExec = {executionCommand,"-v","-tt","-n","-n","-r",v.getViewLocation(),ourSyn.getDetectorSyntax(signals)};
			logme.info("Executing command [" + StringUtils.join(toExec," ") + "] ...");
			genPcap = Runtime.getRuntime().exec(toExec);
		    BufferedReader rd = new BufferedReader( new InputStreamReader( genPcap.getInputStream() ) );
		    String pcapLine;
		    super.resetOrderMap();

		    // Get the packet ID for each packet, and create the data instance object for it
		    int lcount = 0;
		    int icount = 0;
		    int cvaluesUsed = 0;
		    int statusAt = 100000;
		    int dvaluesInSet = 0;
		    int evaluesInSet = 0;
		    long t0 = System.currentTimeMillis();
		    long t1 = 0;
		    while ((pcapLine = rd.readLine()) != null){
			    lcount++;
			    if (lcount % statusAt == 0){
				    t1 = System.currentTimeMillis();
				    double lps = statusAt / (((double)t1 - (double)t0) / 1000);
				    //double pps = icount / (((double)t1 - (double)t0) / 1000);
				    logme.debug(" .. Processed " + lcount + " lines (" + lps + " lps) and " + icount + " instances " );
				    		//+ "(" + pps + " ips)");
				    t0 = t1;
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
				    	if (sdi != di){
				    		cvaluesUsed++;
				    	}

					    if (!toReturn.add(sdi)){
					    	// The return set already contained this element -- this shouldn't happen:
					    	dvaluesInSet++;
					    	if (dvaluesInSet == 1){
					    	    logme.error("TCPDumpDetector -- Duplicate data instance added to return set.  E.g.:");
					    	    logme.error("\t " + sdi.getID());
					    	}
					    }
				    	if (sdi.getLabel() != null && sdi.getLabel().isEvent()){
				    		evaluesInSet++;
				    	}
				    } else {
					    logme.error("Only first line matched (second is listed here): " + pcapLine);
					    throw new IOException("Only first line matched (second is listed here): " + pcapLine);
				    }
			    } else if (rexi.matches()){
				    // Ignore other lines
				    continue;
			    } else {
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
			this.sigMap.put(signal, toReturn);
			logme.debug(String.format(" - Used %d / %d cached values (DataInstance pool size now: %d)",cvaluesUsed,icount, KIDSAbstractDetector.getDataInstancePoolSize()));
			logme.debug(String.format(" - %d duplicate instances found",dvaluesInSet));
			logme.debug(String.format(" - %d event-related instances found",evaluesInSet));
			logme.debug(String.format(" - %d total instances found",toReturn.size()));
			logme.info(String.format("Returning %d instances.",toReturn.size()));
		    return toReturn;
		} catch (InterruptedException e) {
			throw new IOException("Command interrupted: " + command);
		} catch (KIDSIncompatibleSyntaxException e) {
			logme.warn("Could not represent all signals in signal set; returning empty matching instances.");
			logme.warn("    - Detector: " + this.getClass().getName() + " with signals: ");
			for (IRI s : signals){
				logme.warn("    - " + s.toString());
			}
			this.sigMap.put(signal, new HashSet<DataInstance>());
			return new HashSet<DataInstance>();
		}
	}

	@Override
	public Set<DataInstance> getMatchingInstances(Set<IRI> signals,
			DatasetView v) throws IOException,
			KIDSOntologyObjectValuesException,
			KIDSOntologyDatatypeValuesException,
			KIDSIncompatibleSyntaxException, KIDSUnEvaluableSignalException, UnimplementedIdentifyingFeatureException {
		return this.getMatchingInstances(signals, (NativeLibPCAPView)v);
	}

	@Override
	public IRI getIRI() {
		return this.ourIRI;
	}

	/**
	 * Test the KIDSTcpDumpDetector
	 * @param args - Command line arguments
	 * 
	 */
    public static int main (String[] args){
	    return 0;
    }
}

