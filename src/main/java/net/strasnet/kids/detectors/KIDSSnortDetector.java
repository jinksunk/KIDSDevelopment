package net.strasnet.kids.detectors;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.util.SimpleIRIMapper;

import net.strasnet.kids.KIDSOntologyDatatypeValuesException;
import net.strasnet.kids.KIDSOntologyObjectValuesException;
import net.strasnet.kids.detectorsyntaxproducers.KIDSDetectorSyntax;
import net.strasnet.kids.detectorsyntaxproducers.KIDSIncompatibleSyntaxException;
import net.strasnet.kids.detectorsyntaxproducers.KIDSSnortDetectorSyntax;
import net.strasnet.kids.measurement.DataInstance;
import net.strasnet.kids.measurement.Dataset;
import net.strasnet.kids.measurement.KIDSDatasetFactory;
import net.strasnet.kids.measurement.KIDSMeasurementOracle;
import net.strasnet.kids.measurement.KIDSUnEvaluableSignalException;
import net.strasnet.kids.measurement.datasetinstances.KIDSNativeLibpcapDataInstance;
import net.strasnet.kids.measurement.datasetinstances.KIDSSnortDataInstance;
import net.strasnet.kids.measurement.datasetviews.DatasetView;
import net.strasnet.kids.measurement.datasetviews.NativeLibPCAPView;

public class KIDSSnortDetector implements KIDSDetector {

	/**
	 * Represents a detector utilizing the TCPDump command-line tool.  Associated with the syntax "bpf" - berkeley packet filter.
	 */
	
	private IRI ourIRI = null;
	protected static String featureIRI = "http://solomon.cs.iastate.edu/ontologies/KIDS.owl#";
	private static String executionCommand;
	
	
	/**
	 * Snort rule output from this detector:

09/23-00:08:08.040748  [**] [1:100101:0] Event detected! [**] [Priority: 0] {TCP} 130.130.130.130:20 -> 131.131.131.131:80
09/23-00:08:08.040748 130.130.130.130:20 -> 131.131.131.131:80
TCP TTL:64 TOS:0x0 ID:2 IpLen:20 DgmLen:429
*******F Seq: 0x0  Ack: 0x0  Win: 0x2000  TcpLen: 20
=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+

     - So, we can key on the \[**\] to determine when the following packet was alerted by Snort.
	 */
	// 16:20:59.752086 IP (tos 0x0, ttl 64, id 4, offset 0, flags [none], proto TCP (6), length 429)
	//private static String regexPattern = "[\\d/-:\\.]+\\s+\\[\\*\\*\\].*\\s+ID:(?<PID>\\d+)\\s.*";
//	private static String regexPattern = "(?<TIMESTAMP>[\\d\\/\\-:\\.]+)\\s+\\[\\*\\*\\]\\s+.*(<?SIP>\\d+\\.\\d+\\.\\d+\\.\\d+)[^-]+\\s->\\s(<?DIP>\\d+\\.\\d+\\.\\d+\\.\\d+).*ID:(<?ID>\\d+)\\s+";
	private static String regexPattern = "(?<TIMESTAMP>[\\d\\/\\-:\\.]+)\\s+\\[\\*\\*\\].*\\s+(?<SIP>\\d+\\.\\d+\\.\\d+\\.\\d+)[^-]+\\s->\\s(?<DIP>\\d+\\.\\d+\\.\\d+\\.\\d+).*ID:(?<ID>\\d+)\\s+";
	private static String recordPattern = "(=\\+){37}";
	
	private static final Map<String, Boolean> supportedFeatures = new HashMap <String, Boolean>();
	static {
		supportedFeatures.put(featureIRI + "TCPPacketPayload",true);
		supportedFeatures.put(featureIRI + "PacketID",true);
		supportedFeatures.put(featureIRI + "IPv4SourceAddressSignalDomain",true);
		supportedFeatures.put(featureIRI + "IPv4DestinationAddressSignalDomain",true);
		supportedFeatures.put(featureIRI + "instanceTimestamp",true);
		};
		
	private Pattern rexp = null;
	private Pattern recRexp = null;
	private KIDSMeasurementOracle myGuy = null;
	private KIDSDetectorSyntax ourSyn = null; 
	//private Path tmpDir;
	
	public KIDSSnortDetector(){
		myGuy = null;
		ourIRI = null;
	}
	
	@Override
	public void init(String toExecute, IRI detectorIRI, KIDSMeasurementOracle o) throws KIDSOntologyObjectValuesException, KIDSOntologyDatatypeValuesException, InstantiationException, IllegalAccessException, ClassNotFoundException{
		myGuy = o;
		ourIRI = detectorIRI;
	
		executionCommand = toExecute;
		rexp = Pattern.compile(regexPattern,Pattern.DOTALL);
		recRexp = Pattern.compile(recordPattern);
		ourSyn = o.getDetectorSyntax(ourIRI);
		/**
		try {
			tmpDir = Files.createTempDirectory("KIDSSnortDetector");
		} catch (IOException e) {
			// Worst case, try /tmp
			System.err.println("Could not create dir, using '/tmp' instead (" + e + ")");
			tmpDir = Paths.get("/tmp");
		}
		*/
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
	public Set<Map<IRI,String>> getMatchingInstances (Set<IRI> signals, DatasetView v) throws IOException, KIDSOntologyObjectValuesException, KIDSOntologyDatatypeValuesException, KIDSIncompatibleSyntaxException, KIDSUnEvaluableSignalException{
		//TODO: Should we pass the string in, or a Syntax object, or just the signal?
		// 08-24-2013 - I'm thinking that the detector should know it's syntax already, 
		// so we *should* be able to just pass in a set of signals.  The KB can be used to map signals to
		// syntaxes in the case of multiple.  
		Set<Map<IRI,String>> toReturn;
		
		// Run the command with the detector specification
		Process genPcap = Runtime.getRuntime().exec(executionCommand + " -r " + v.getViewLocation() + " -c " + ourSyn.getDetectorSyntax(signals) + " -N -k none -A console -q -v -y");
		StreamGobbler eGobble = new StreamGobbler(genPcap.getErrorStream(), "ERROR");
		StreamGobbler iGobble = new StreamGobbler(genPcap.getInputStream(), "OUTPUT", v);
		eGobble.start();
		iGobble.start();
		try {
			if (genPcap.waitFor() != 0){
 		            eGobble.join();
				    System.err.println(eGobble.getOutput());
			}
			iGobble.join();
		} catch (InterruptedException e) {
			throw new IOException("Command interrupted: " + this.executionCommand);
		}
		toReturn = iGobble.getReturnSet();
		//BufferedReader rd = new BufferedReader(new StringReader(iGobble.getOutput()));
		//String pcapLine;
		//StringBuilder sRecord = new StringBuilder();
		
		// Get the packet ID for each packet, and create the data instance object for it
		//while ((pcapLine = rd.readLine()) != null){
			//Matcher rexm = recRexp.matcher(pcapLine);
			//if (rexm.matches()){
				// End of record, process:
			    //Matcher rexr = rexp.matcher(sRecord);
			    //if (rexr.find()){
			    	//HashMap<IRI, String> idmap = new HashMap<IRI, String>();
			    	//idmap.put(v.getIdentifyingFeatures().get(0), rexr.group("ID"));
			    	//idmap.put(v.getIdentifyingFeatures().get(1), rexr.group("TIMESTAMP"));
			    	//idmap.put(v.getIdentifyingFeatures().get(2), rexr.group("SIP"));
			    	//idmap.put(v.getIdentifyingFeatures().get(3), rexr.group("DIP"));
			    	//KIDSSnortDataInstance newGuy = new KIDSSnortDataInstance(idmap);
			    	//toReturn.add(newGuy);
			    //} 
			    //sRecord = new StringBuilder();
			//} else {
				//sRecord.append(pcapLine);
			//}
		//}
		
		return toReturn;
	}
	
	public static void main(String[] args) {
		// Test this puppy :)
		String testABOX = "http://www.semantiknit.com/ontologies/2013/9/15/TestEventExperiment2.owl";
		String testABOXFile = "file:///Users/chrisstrasburg/Documents/academic-research/papers/2013-MeasurementPaper/experiments/TestEvent-Test2-Snort/TestEventExperiment2.owl";
		String testTBOX = "http://solomon.cs.iastate.edu/ontologies/KIDS.owl";
		String testTBOXFile = "http://solomon.cs.iastate.edu/ontologies/KIDS.owl";
		IRI snortDetectorIRI = IRI.create("http://www.semantiknit.com/ontologies/2013/9/15/TestEventExperiment2.owl#SnortDetector1");
		//String testTBOXFile = "file:///Users/chrisstrasburg/Documents/academic-research/papers/2013-MeasurementPaper/experiments/TestEvent-Test1/kids.owl";
        List<SimpleIRIMapper> m = new LinkedList<SimpleIRIMapper>();
        m.add(new SimpleIRIMapper(IRI.create(testABOX), IRI.create(testABOXFile)));
        m.add(new SimpleIRIMapper(IRI.create(testTBOX), IRI.create(testTBOXFile)));
		
		
		// Initialize the syntax generator and print out the form:
		KIDSSnortDetector ksd = new KIDSSnortDetector();
		KIDSMeasurementOracle kmo = new KIDSMeasurementOracle();
		Set<IRI> testSigSet = new HashSet<IRI>();
		testSigSet.add(IRI.create(testABOX + "#TCPFINFlagSet"));
		testSigSet.add(IRI.create(testABOX + "#TCPPacketSizeSignal200"));
		testSigSet.add(IRI.create(testABOX + "#tcpProtocolSignal"));
		
		try {
			kmo.loadKIDS(IRI.create(testABOX), m);
			ksd.init(kmo.getDetectorExecutionString(snortDetectorIRI), snortDetectorIRI, kmo);
			IRI testDataset = IRI.create("http://www.semantiknit.com/ontologies/2013/9/15/TestEventExperiment2.owl#TestEvent1LIBPCAPDataset1"); 
			IRI testEvent = IRI.create("http://www.semantiknit.com/ontologies/2013/9/15/TestEventExperiment2.owl#TestEvent2");
			Set<OWLNamedIndividual> eventDSes = kmo.getDatasetsForEvent(testEvent);
			OWLNamedIndividual ourDS = eventDSes.iterator().next();
			Dataset d = KIDSDatasetFactory.getViewLabelDataset(ourDS.getIRI(), testEvent, kmo);
			Set<DataInstance> rInstances = d.getMatchingInstances(testSigSet);
			System.out.println("Number of data instances: " + rInstances.size());
		} catch (KIDSOntologyObjectValuesException
				| KIDSOntologyDatatypeValuesException | InstantiationException
				| IllegalAccessException | ClassNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	class StreamGobbler extends Thread
	{
	    InputStream is;
	    String type;
	    StringBuilder mine;
	    Set<Map<IRI, String>> toReturn;
	    DatasetView v;
	    boolean processStream = false;
	    
	    StreamGobbler(InputStream is, String type)
	    {
	        this.is = is;
	        this.type = type;
	        mine = new StringBuilder();
	        toReturn = new HashSet<Map<IRI,String>>();
	    }
	    
	    StreamGobbler(InputStream is, String type, DatasetView v)
	    {
	        this.is = is;
	        this.type = type;
	        mine = new StringBuilder();
	        toReturn = new HashSet<Map<IRI,String>>();
	        this.v = v;
	        processStream = true;
	    }
	    
	    public String getOutput() {
			return mine.toString();
		}
	    
	    public Set<Map<IRI,String>> getReturnSet(){
	    	return toReturn;
	    }

		public void run()
	    {
			Matcher rexm = null;
	        try
	        {
	            InputStreamReader isr = new InputStreamReader(is);
	            BufferedReader br = new BufferedReader(isr);
	            String line=null;
	            StringBuilder sRecord = new StringBuilder();
	            while ( (line = br.readLine()) != null)
	            	if (! processStream){
	            		mine.append(line);
	            		continue;
	            	} else {
	            	    rexm = recRexp.matcher(line);
	                    if (rexm.matches()){
				        // End of record, process:
			                Matcher rexr = rexp.matcher(sRecord);
			                if (rexr.find()){
			    	            HashMap<IRI, String> idmap = new HashMap<IRI, String>();
			    	            Iterator<IRI> ifs = v.getIdentifyingFeatures().iterator();
			    	            IRI identFeature;
			    	            while (ifs.hasNext()){
			    	            	identFeature = ifs.next();
			    	            	if (identFeature.toString().equals(featureIRI + "PacketID")){
			    	            		idmap.put(identFeature, rexr.group("ID"));
					 	           	} else if (identFeature.toString().equals(featureIRI + "IPv4SourceAddressSignalDomain")){
						 	           	idmap.put(identFeature, rexr.group("SIP"));
					 	           	} else if (identFeature.toString().equals(featureIRI + "IPv4DestinationAddressSignalDomain")){
						 	           	idmap.put(identFeature, rexr.group("DIP"));
					 	           	}
			    	            	
			    	            	/*
									try {
										idmap.put(v.getIdentifyingFeatures().get(1), parseSnortTimestamp(rexr.group("TIMESTAMP")));
									} catch (ParseException e) {
										System.err.println("[W] - Cannot parse timestamp " + (rexr.group("TIMESTAMP")) + " ; leaving null.");
										idmap.put(v.getIdentifyingFeatures().get(1),null);
									}
									*/
			    	            }
								//} catch (ParseException e) {
									// TODO Auto-generated catch block
									//e.printStackTrace();
								//}
			    	            toReturn.add(idmap);
			                } 
			                sRecord = new StringBuilder();
			            } else {
				            sRecord.append(line);
			            }
	            	}
	            } catch (IOException ioe)
	              {
	                ioe.printStackTrace();  
	              }
	    }
		
		/**
		 * Given a timestamp in snort format (), return a unix epoch timestamp instead.
		 * @param snortTS (e.g. 09/23-00:08:08.040748)
		 * @return corresponding unix epoch timestamp
		 * @throws ParseException 
		 */
		public String parseSnortTimestamp(String snortTS) throws ParseException{
			SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yy-HH:mm:ss.SSSSSS");
			return ("" + (sdf.parse("" + snortTS).getTime()/1000));
		}
		
		public long getIPAsInt(String IPDottedQuad) throws UnknownHostException{
			byte[] bytes = InetAddress.getByName(IPDottedQuad).getAddress();
			long iVal = 0;
			for (int i = 0; i < 4; i++){
				iVal <<= 8;
				iVal |= bytes[i] & 0xff;
				//iVal += ((long)(bytes[i] << (24 - (8*i)))) & 0xffffffff;
			}
			return iVal;
			
		}
	}

	@Override
	public IRI getIRI() {
		// TODO Auto-generated method stub
		return this.ourIRI;
	}
}
