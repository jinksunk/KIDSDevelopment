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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

public class KIDSTcpDumpDetector implements KIDSDetector {

	/**
	 * Represents a detector utilizing the TCPDump command-line tool.  Associated with the syntax "bpf" - berkeley packet filter.
	 */
	
	private IRI ourIRI = null;
	private static String featureIRI = "http://solomon.cs.iastate.edu/ontologies/KIDS.owl#";
	private static String executionCommand;
	// 920592993.000000 IP (tos 0x0, ttl 64, id 4, offset 0, flags [none], proto TCP (6), length 429)
	private static String regexPatternLine1 = "(?<TimeStamp>[\\d\\.]+)\\sIP\\s\\((([^,]+),){2}\\sid\\s(?<PID>\\d+),.*length\\s\\d+\\).*";
	private static String regexPattern = "\\s*(?<SIP>[\\d\\.]+)\\.\\d+\\s+>\\s+(?<DIP>[\\d\\.]+)\\.\\d+.*";
	
	// 130.130.130.130.20 > 131.131.131.131.80: Flags [S], cksum 0x4e3f (correct), seq 0:389, win 8192, length 389
	//private static String regexPatternIgnore = "\\s+[\\d\\.]+\\s>\\s[\\d\\.]+:\\sFlags\\s.*";
	private static String regexPatternIgnore = ".*";
	
	private static final Map<String, Boolean> supportedFeatures = new HashMap <String, Boolean>();
	static {
		supportedFeatures.put(featureIRI + "TCPPacketPayload",true);
		supportedFeatures.put(featureIRI + "PacketID",true);
		supportedFeatures.put(featureIRI + "IPv4SourceAddressSignalDomain",true);
		supportedFeatures.put(featureIRI + "IPv4DestinationAddressSignalDomain",true);
		supportedFeatures.put(featureIRI + "instanceTimestamp",true);
		};
		
	private Pattern rexpL1 = null;
	private Pattern rexp = null;
	private Pattern rexpIgnore = null;
	private KIDSMeasurementOracle myGuy = null;
	private KIDSDetectorSyntax ourSyn = null; 
	
	public KIDSTcpDumpDetector(){
		myGuy = null;
		ourIRI = null;
	}
	
	@Override
	public void init(String toExecute, IRI detectorIRI, KIDSMeasurementOracle o) throws KIDSOntologyObjectValuesException, KIDSOntologyDatatypeValuesException, InstantiationException, IllegalAccessException, ClassNotFoundException{
		myGuy = o;
		ourIRI = detectorIRI;
	
		executionCommand = toExecute;
		rexpL1 = Pattern.compile(regexPatternLine1);
		rexp = Pattern.compile(regexPattern);
		rexpIgnore = Pattern.compile(regexPatternIgnore);
		ourSyn = o.getDetectorSyntax(ourIRI);
	}
	
	/**
	 * 
	 * @param detectorSpec
	 * @param v
	 * @return
	 * @throws IOException
	 * @throws KIDSIncompatibleSyntaxException 
	 * @throws KIDSOntologyDatatypeValuesException 
	 * @throws KIDSOntologyObjectValuesException 
	 * @throws KIDSUnEvaluableSignalException 
	 */
	public Set<DataInstance> getMatchingInstances (Set<IRI> signals, NativeLibPCAPView v) throws IOException, KIDSOntologyObjectValuesException, KIDSOntologyDatatypeValuesException, KIDSUnEvaluableSignalException{
		//TODO: Should we pass the string in, or a Syntax object, or just the signal?
		// 08-24-2013 - I'm thinking that the detector should know it's syntax already, 
		// so we *should* be able to just pass in a set of signals.  The KB can be used to map signals to
		// syntaxes in the case of multiple.  
		Set<DataInstance> toReturn = new HashSet<DataInstance>();
		
		// Run the command with the detector specification
		String command = null;
		Process genPcap = null;
		try {
			command = executionCommand + " -v -tt -n -n -r " + v.getViewLocation() + " " + ourSyn.getDetectorSyntax(signals) + " ";
			System.err.print("Executing command [" + command + "] ...");
			genPcap = Runtime.getRuntime().exec(command);
		    BufferedReader rd = new BufferedReader( new InputStreamReader( genPcap.getInputStream() ) );
		    String pcapLine;

		    // Get the packet ID for each packet, and create the data instance object for it
		    int count = 0;
		    int statusAt = 100000;
		    long t0 = System.currentTimeMillis();
		    long t1 = 0;
		    while ((pcapLine = rd.readLine()) != null){
			    count++;
			    if (count % statusAt == 0){
				    t1 = System.currentTimeMillis();
				    double pps = statusAt / (((double)t1 - (double)t0) / 1000);
				    System.err.println(" .. Processed " + count + " packets (" + pps + " pps)");
				    t0 = t1;
			    }
			    Matcher rexmpre = rexpL1.matcher(pcapLine);
			    Matcher rexi = rexpIgnore.matcher(pcapLine);
			    if (rexmpre.matches()){
				    // First of the two lines extract resources.
			        String packetID = rexmpre.group("PID");
			        HashMap<IRI, String> idmap = new HashMap<IRI, String>();
			        idmap.put(v.getIdentifyingFeatures().get(0), packetID);
    
			        // Setup resource hashmap:
				    HashMap<IRI,String> resMap = new HashMap<IRI,String>();
				    String tsString = rexmpre.group("TimeStamp");
				    resMap.put(IRI.create(featureIRI + "instanceTimestamp"),tsString.substring(0, tsString.indexOf('.')));
			        
			        // Get the second part
				    pcapLine = rd.readLine();
				    Matcher rexmpost = rexp.matcher(pcapLine);

				    if (rexmpost.matches()){
					    // Extract resources
					    resMap.put(IRI.create(featureIRI + "IPv4SourceAddressSignalDomain"), rexmpost.group("SIP"));
					    idmap.put(v.getIdentifyingFeatures().get(2), rexmpost.group("SIP"));
					    resMap.put(IRI.create(featureIRI + "IPv4DestinationAddressSignalDomain"), rexmpost.group("DIP"));
					    idmap.put(v.getIdentifyingFeatures().get(3), rexmpost.group("DIP"));
					    KIDSNativeLibpcapDataInstance newGuy = new KIDSNativeLibpcapDataInstance(idmap);
					    newGuy.addResources(resMap);
					    toReturn.add(newGuy);
				    } else {
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
			        System.err.println(errout);
			    }
		    }
		    return toReturn;
		} catch (InterruptedException e) {
			throw new IOException("Command interrupted: " + command);
		} catch (KIDSIncompatibleSyntaxException e) {
			System.err.println("[W] - Could not represent all signals in signal set; returning empty matching instances.");
			System.err.println("    - Detector: " + this.getClass().getName() + " with signals: ");
			for (IRI s : signals){
				System.err.println("    - " + s.toString());
			}
			return new HashSet<DataInstance>();
		}
	}

	@Override
	public Set<DataInstance> getMatchingInstances(Set<IRI> signals,
			DatasetView v) throws IOException,
			KIDSOntologyObjectValuesException,
			KIDSOntologyDatatypeValuesException,
			KIDSIncompatibleSyntaxException, KIDSUnEvaluableSignalException {
		return this.getMatchingInstances(signals, (NativeLibPCAPView)v);
	}
}
