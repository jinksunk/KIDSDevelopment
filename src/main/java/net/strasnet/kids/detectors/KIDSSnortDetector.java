package net.strasnet.kids.detectors;

import java.io.BufferedReader;

import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.util.SimpleIRIMapper;

import net.strasnet.kids.KIDSOntologyDatatypeValuesException;
import net.strasnet.kids.KIDSOntologyObjectValuesException;
import net.strasnet.kids.detectorsyntaxproducers.KIDSIncompatibleSyntaxException;
import net.strasnet.kids.measurement.DataInstance;
import net.strasnet.kids.measurement.Dataset;
import net.strasnet.kids.measurement.KIDSDatasetFactory;
import net.strasnet.kids.measurement.KIDSMeasurementOracle;
import net.strasnet.kids.measurement.KIDSUnEvaluableSignalException;
import net.strasnet.kids.measurement.datasetinstances.KIDSSnortDataInstance;
import net.strasnet.kids.measurement.datasetviews.DatasetView;

public class KIDSSnortDetector extends KIDSAbstractDetector implements KIDSDetector {

	/**
	 * Represents a detector utilizing Snort.  Associated with the syntax "bpf" - berkeley packet filter syntax.
	 */
	
	//protected static String featureIRI = "http://solomon.cs.iastate.edu/ontologies/KIDS.owl#";
	
	
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
	private static final Logger logme = LogManager.getLogger(KIDSSnortDetector.class.getName());

	//private static String regexPattern = ".*\\[\\*\\*\\].*(?<TIMESTAMP>[\\d\\/\\-:\\.]+)\\s+(?<SIP>\\d+\\.\\d+\\.\\d+\\.\\d+)[^-]*\\s->\\s(?<DIP>\\d+\\.\\d+\\.\\d+\\.\\d+)[^\\*]*ID:(?<ID>\\d+)\\s+IpLen";
	private static String regexPattern = ".*\\[\\*\\*\\].*(?<TIMESTAMP>\\d\\d\\/\\d\\d\\/\\d\\d-[\\d:\\.]+)\\s+(?<SIP>\\d+\\.\\d+\\.\\d+\\.\\d+)[^-]*\\s->\\s(?<DIP>\\d+\\.\\d+\\.\\d+\\.\\d+)[^\\*]*ID:(?<ID>\\d+)\\s+IpLen.*";
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
	//private Path tmpDir;
	
	public KIDSSnortDetector(){
		myGuy = null;
		ourIRI = null;
	}
	
	@Override
	public void init(String toExecute, IRI detectorIRI, KIDSMeasurementOracle o) throws KIDSOntologyObjectValuesException, KIDSOntologyDatatypeValuesException, InstantiationException, IllegalAccessException, ClassNotFoundException{
		super.init(toExecute, detectorIRI, o);
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
		//TODO: Should we pass the string in, or a Syntax object, or just the signal?
		// 08-24-2013 - I'm thinking that the detector should know it's syntax already, 
		// so we *should* be able to just pass in a set of signals.  The KB can be used to map signals to
		// syntaxes in the case of multiple.  
	 * 
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
	@Override
	public Set<DataInstance> getMatchingInstances (Set<IRI> signals, DatasetView v) throws IOException, KIDSOntologyObjectValuesException, KIDSOntologyDatatypeValuesException, KIDSIncompatibleSyntaxException, KIDSUnEvaluableSignalException, UnimplementedIdentifyingFeatureException{
		
		
		logme.info(String.format("Checking cache..."));
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
				logme.debug(String.format("Using cache entry for %s",signal));
				results = this.sigMap.get(signal);
			} else {
				results = getMatchingInstances(signal, v);
				logme.debug(String.format("Adding cache entry for %s",signal));
				this.sigMap.put(signal, results);
				logme.debug("\t(Signal cache now consists of:");
				for (IRI cMapEntry : this.sigMap.keySet()){
					logme.debug(String.format("\t%s ;",cMapEntry));
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
		return toReturn;
	}	
	
	private Set<DataInstance> getMatchingInstances(IRI signal, DatasetView v) throws IOException, KIDSOntologyObjectValuesException, KIDSOntologyDatatypeValuesException, KIDSIncompatibleSyntaxException, KIDSUnEvaluableSignalException{
		Set<IRI> signals = new HashSet<IRI>();
		signals.add(signal);
		Set<DataInstance> toReturn = new HashSet<DataInstance>();
		
		// Run the command with the detector specification
		String ourSynStr = ourSyn.getDetectorSyntax(signals) ;
		String[] toExec = {executionCommand, "-r", v.getViewLocation(), "-c", ourSynStr, "-N", "-k", "none", "-A", "console", "-q", "-v", "-y"};
		logme.info("[Snort] Executing: " + StringUtils.join(toExec, " "));
		Process genPcap = Runtime.getRuntime().exec(toExec);
//		Process genPcap = Runtime.getRuntime().exec(executionCommand + " -r " + v.getViewLocation() + " -c " + ourSynStr + " -N -k none -A console -q -v -y");
	    super.resetOrderMap();
		SnortStreamGobbler eGobble = new SnortStreamGobbler(genPcap.getErrorStream(), "ERROR");
		SnortStreamGobbler iGobble = new SnortStreamGobbler(genPcap.getInputStream(), "OUTPUT", v);
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
		logme.debug(String.format("- Used %d / %d cache values (pool size now: %d).", iGobble.cvaluesUsed, iGobble.icount, KIDSAbstractDetector.getDataInstancePoolSize()));
		logme.debug(String.format("- %d duplicate instances found.", iGobble.dvaluesInSet));
		logme.debug(String.format("- %d total matching instances found.", iGobble.icount));
		logme.debug(String.format("- %d non-matching instances found.", iGobble.bcount));
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
		this.sigMap.put(signal,toReturn);
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
			logme.info("Number of data instances: " + rInstances.size());
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
	
	class SnortStreamGobbler extends StreamGobbler
	{

		int nvaluesUsed = 0;
		int cvaluesUsed = 0;
		int icount = 0;
		int bcount = 0;
		int dvaluesInSet = 0;

		SnortStreamGobbler( InputStream is, String type) {
			super(is, type);
		}

		SnortStreamGobbler(InputStream is, String type, DatasetView v) {
			super(is, type, v);
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
								LinkedList<IRI> orderKey = new LinkedList<IRI>();
								orderKey.add(IRI.create(featureIRI + "PacketID"));
								orderKey.add(IRI.create(featureIRI + "IPv4SourceAddressSignalDomain"));
								orderKey.add(IRI.create(featureIRI + "IPv4DestinationAddressSignalDomain"));
								addOrderKeyToIDMap(orderKey, idmap);

			    	            DataInstance di = new KIDSSnortDataInstance(idmap);
			    	            icount++;
			    	            DataInstance sdi = KIDSAbstractDetector.getDataInstance(di);
			    	            if (di != sdi){
			    	            	cvaluesUsed++;
			    	            } else {
			    	            	nvaluesUsed++;
			    	            	if (nvaluesUsed < 5){
			    	            		logme.debug(String.format("New value found from record: \n++=> %s", sRecord));
			    	            	}
			    	            }
			    	            if (!toReturn.add(sdi)){
			    	            	dvaluesInSet++;
			    	            	if (dvaluesInSet < 5){
						    	             	logme.error(String.format("Detector -- Duplicate data instance added to return set.  E.g. %s (from line %s)",sdi.getID(), line));
			    	            	}
						    	}
			                }  else {
			                	// Getting here implies that the line did not match the regular expression, e.g. did not trigger the snort rule or something along those lines.
			                	bcount++;
			                	logme.debug(String.format("Could not parse record: \n%s",sRecord));
			                }
			                sRecord = new StringBuilder();
			            } else {
				            sRecord.append(line);
			            }
	            	}
	            } catch (IOException | UnimplementedIdentifyingFeatureException ioe)
	              {
	                ioe.printStackTrace();  
	              }
	    }
		
	}

	@Override
	public IRI getIRI() {
		return this.ourIRI;
	}
}
