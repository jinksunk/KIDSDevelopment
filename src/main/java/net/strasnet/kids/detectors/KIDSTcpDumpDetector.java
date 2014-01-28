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
import net.strasnet.kids.measurement.datasetviews.NativeLibPCAPView;

public class KIDSTcpDumpDetector implements KIDSDetector {

	/**
	 * Represents a detector utilizing the TCPDump command-line tool.  Associated with the syntax "bpf" - berkeley packet filter.
	 */
	
	private IRI ourIRI = null;
	private static String featureIRI = "http://solomon.cs.iastate.edu/ontologies/KIDS.owl#";
	private static String executionCommand;
	// 16:20:59.752086 IP (tos 0x0, ttl 64, id 4, offset 0, flags [none], proto TCP (6), length 429)
	private static String regexPattern = "[\\d:\\.]+\\sIP\\s\\((([^,]+),){2}\\sid\\s(\\d+),.*";
	
	// 130.130.130.130.20 > 131.131.131.131.80: Flags [S], cksum 0x4e3f (correct), seq 0:389, win 8192, length 389
	private static String regexPatternIgnore = "\\s+[\\d\\.]+\\s+>\\s+[\\d\\.]+.*";
	//private static String regexPatternIgnore = "\\s+[\\d\\.]+\\s>\\s[\\d\\.]+:\\sFlags\\s.*";
	
	private static final Map<String, Boolean> supportedFeatures = new HashMap <String, Boolean>();
	static {
		supportedFeatures.put(featureIRI + "TCPPacketPayload",true);
		supportedFeatures.put(featureIRI + "PacketID",true);
		};
		
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
	@Override
	public Set<DataInstance> getMatchingInstances (Set<IRI> signals, NativeLibPCAPView v) throws IOException, KIDSOntologyObjectValuesException, KIDSOntologyDatatypeValuesException, KIDSIncompatibleSyntaxException, KIDSUnEvaluableSignalException{
		//TODO: Should we pass the string in, or a Syntax object, or just the signal?
		// 08-24-2013 - I'm thinking that the detector should know it's syntax already, 
		// so we *should* be able to just pass in a set of signals.  The KB can be used to map signals to
		// syntaxes in the case of multiple.  
		Set<DataInstance> toReturn = new HashSet<DataInstance>();
		
		// Run the command with the detector specification
		Process genPcap = Runtime.getRuntime().exec(executionCommand + " -v -n -n -r " + v.getViewLocation() + " " + ourSyn.getDetectorSyntax(signals) + " ");
		try {
			if (genPcap.waitFor() != 0){
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
		String pcapLine;
		
		// Get the packet ID for each packet, and create the data instance object for it
		while ((pcapLine = rd.readLine()) != null){
			Matcher rexm = rexp.matcher(pcapLine);
			Matcher rexi = rexpIgnore.matcher(pcapLine);
			if (rexm.matches()){
			    String packetID = rexm.group(3);
			    HashMap<IRI, String> idmap = new HashMap<IRI, String>();
			    idmap.put(v.getIdentifyingFeatures().get(0), packetID);
			    KIDSNativeLibpcapDataInstance newGuy = new KIDSNativeLibpcapDataInstance(idmap);
			    toReturn.add(newGuy);
			} else if (rexi.matches()){
				// Ignore other lines
				continue;
			} else {
				throw new IOException("Could not extract packet ID from line: " + pcapLine);
			}
		}
		
		return toReturn;
	}
}
