/**
 * 
 */
package net.strasnet.kids.detectors;

import java.io.BufferedReader;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.strasnet.kids.KIDSOntologyDatatypeValuesException;
import net.strasnet.kids.KIDSOntologyObjectValuesException;
import net.strasnet.kids.detectorsyntaxproducers.BroEventDetectorSyntax;
import net.strasnet.kids.detectorsyntaxproducers.KIDSDetectorSyntax;
import net.strasnet.kids.detectorsyntaxproducers.KIDSIncompatibleSyntaxException;
import net.strasnet.kids.measurement.DataInstance;
import net.strasnet.kids.measurement.KIDSDatasetFactory;
import net.strasnet.kids.measurement.KIDSMeasurementOracle;
import net.strasnet.kids.measurement.KIDSUnEvaluableSignalException;
import net.strasnet.kids.measurement.ViewLabelDataset;
import net.strasnet.kids.measurement.datasetinstances.KIDSNativeLibpcapDataInstance;
import net.strasnet.kids.measurement.datasetviews.DatasetView;
import net.strasnet.kids.measurement.datasetviews.NativeLibPCAPView;
import net.strasnet.kids.measurement.test.TestOracleFactory;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLNamedIndividual;

/**
 * @author cstras
 *
 */
public class KIDSBroDetector extends KIDSAbstractDetector implements KIDSDetector {
	/**
	 * Represents a detector utilizing the Bro command-line tool.  Associated with the bro script syntax.
	 * 
	 * Will parse and data in the format:
	 * EpochTS,PacketID,SourceIP,DestIP 
	 * - EpochTS - The timestamp in Unix epoch
	 * - PacketID - The IP packet ID in the trace file
	 * - SourceIP - The source IP address in dotted-quad form
	 * - DestIP - The destination IP address in dotted-quad form
	 * 
	 * Example:
	 * 920567169.0,38963,136.201.241.147,172.16.112.100
	 */
	
	protected static String featureIRI = "http://solomon.cs.iastate.edu/ontologies/KIDS.owl#";
	
	
	/**
	 * Bro rule output sample from this detector:
	 */
	//private static String regexPattern = "[\\d/-:\\.]+\\s+\\[\\*\\*\\].*\\s+ID:(?<PID>\\d+)\\s.*";
//	private static String regexPattern = "(?<TIMESTAMP>[\\d\\/\\-:\\.]+)\\s+\\[\\*\\*\\]\\s+.*(<?SIP>\\d+\\.\\d+\\.\\d+\\.\\d+)[^-]+\\s->\\s(<?DIP>\\d+\\.\\d+\\.\\d+\\.\\d+).*ID:(<?ID>\\d+)\\s+";
	private static String regexPattern = "(?<TIMESTAMP>[\\d\\.]+),(?<PID>\\d+),(?<SIP>\\d+\\.\\d+\\.\\d+\\.\\d+),(?<DIP>\\d+\\.\\d+\\.\\d+\\.\\d+)\\s*";
	
	private static final Map<String, Boolean> supportedFeatures = new HashMap <String, Boolean>();
	static {
		supportedFeatures.put(featureIRI + "TCPPayloadSizeDomain",true);
		};
		
	private Pattern rexp = null;
	//private Path tmpDir;

	/* (non-Javadoc)
	 * @see net.strasnet.kids.detectors.KIDSDetector#getMatchingInstances(java.util.Set, net.strasnet.kids.measurement.datasetviews.DatasetView)
	 */
	@Override
	public Set<DataInstance> getMatchingInstances(Set<IRI> signals,
			DatasetView v) throws IOException,
			KIDSOntologyObjectValuesException,
			KIDSOntologyDatatypeValuesException,
			KIDSIncompatibleSyntaxException, KIDSUnEvaluableSignalException, UnimplementedIdentifyingFeatureException {

		// First, check cache to see if the signal set has already been evaluated:
		Set<DataInstance> toReturn = super.getMatchingInstances(signals, v);
		if (toReturn != null){
			return toReturn;
		}
		
		boolean firstSignal = true;
		
		for (IRI signal : signals){
			Set<DataInstance> results = null;
			if (this.sigMap.containsKey(signal)){
				results = this.sigMap.get(signal);
			} else {
				HashSet<DataInstance> tResults = new HashSet<DataInstance>();
				results = getMatchingInstances(signal, v);
				int count = 0;
				for (DataInstance di : results){
					tResults.add(KIDSAbstractDetector.getDataInstance(di));
					count++;
					if (count % 100000 == 0){
						System.err.println(String.format("[M] - Processed %d records...",count));
					}
				}
				System.err.println(String.format("[D] BroDetector - Adding cache entry (size = %d) for %s",results.size(), signal));
				this.sigMap.put(signal, results);
				System.err.println("\t(Signal cache now consists of:");
				for (IRI cMapEntry : this.sigMap.keySet()){
					System.err.println(String.format("\t%s ;",cMapEntry));
				}
				System.err.println("\t)");

			}
			if (firstSignal){
				toReturn = results;
				firstSignal = false;
			} else {
				toReturn.retainAll(results);
			}
		}
		return toReturn;

	}
	
	private Set<DataInstance> getMatchingInstances(IRI signal, DatasetView v) throws IOException, KIDSOntologyObjectValuesException, KIDSOntologyDatatypeValuesException, KIDSIncompatibleSyntaxException, KIDSUnEvaluableSignalException{
		
		Set<DataInstance> toReturn = new HashSet<DataInstance>();
		Set<IRI> signals = new HashSet<IRI>();
		signals.add(signal);

		String runBroString = executionCommand + " -r " + v.getViewLocation() + " " + ourSyn.getDetectorSyntax(signals); 
		System.out.println("Executing: " + runBroString);
		Process runBro = Runtime.getRuntime().exec(executionCommand + " -r " + v.getViewLocation() + " " + ourSyn.getDetectorSyntax(signals)); 
		BroStreamGobbler eGobble = new BroStreamGobbler(runBro.getErrorStream(), "ERROR");
		BroStreamGobbler iGobble = new BroStreamGobbler(runBro.getInputStream(), "OUTPUT", v);
		eGobble.start();
		iGobble.start();
		
		try {
			if (runBro.waitFor() != 0){
				eGobble.join();
				System.err.println(eGobble.getOutput());
			}
			iGobble.join();
		} catch (InterruptedException e){
			throw new IOException("Command interrupted: " + this.executionCommand);
		}
		toReturn = iGobble.getReturnSet();
		System.err.println(String.format("[D] KIDSBroDetector - Used %d / %d cache values.", iGobble.cvaluesUsed, iGobble.count));
		this.sigMap.put(signal, toReturn);

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
		super.init(toExecute, detectorIRI, o);
		rexp = Pattern.compile(regexPattern);
	}

	/* (non-Javadoc)
	 * @see net.strasnet.kids.detectors.KIDSDetector#getIRI()
	 */
	@Override
	public IRI getIRI() {
		return ourIRI;
	}

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		// Test driver here
		String ABOXLocation = args[0];
		String ABOXIRI = "http://www.semantiknit.com/ontologies/2014/03/29/CodeRedExperiment3.owl";
		String TBOXLocation = args[1];
		String TBOXIRI = "http://solomon.cs.iastate.edu/ontologies/KIDS.owl";
		IRI DetectorIRI = IRI.create(args[2]);
		IRI TestSignalIRI = IRI.create(args[3]);
		IRI EventIRI = IRI.create(args[4]);

		KIDSMeasurementOracle localO = TestOracleFactory.getKIDSMeasurementOracle(TBOXIRI, TBOXLocation, ABOXIRI, ABOXLocation);

		KIDSBroDetector beds = new KIDSBroDetector();
		beds.init(localO.getDetectorExecutionString(DetectorIRI), DetectorIRI, localO);

		Set<IRI> sigSet = new HashSet<IRI>();
		sigSet.add(TestSignalIRI);
		
		Set<OWLNamedIndividual> dSets = localO.getDatasetsForEvent(EventIRI);
		
		// In this case, we want to ensure we get the libpcap dataset:
		Map<IRI, List<OWLNamedIndividual>> dsetViewMap = new HashMap<IRI, List<OWLNamedIndividual>>();
		OWLNamedIndividual chosenDset = null;
		for (OWLNamedIndividual dSet : dSets){
			if (dSet.getIRI().equals(IRI.create("http://www.semantiknit.com/ontologies/2014/03/29/CodeRedExperiment3.owl#CodeRedEvalPCAPDataset1"))){
				chosenDset = dSet;
			}
			List<OWLNamedIndividual> tViews = localO.getAvailableViews(dSet.getIRI(), EventIRI);
			dsetViewMap.put(dSet.getIRI(), tViews);
		}

		// Pull out the libpcap view:
		NativeLibPCAPView nlpv = new NativeLibPCAPView();
		OWLNamedIndividual ourView = dsetViewMap.get(chosenDset.getIRI()).iterator().next();
		nlpv.setIRI(ourView.getIRI());
		//localO.getLabelForViewAndEvent(ourView, EventIRI);
		//List<IRI> lList = new LinkedList<IRI>();
		//OWLNamedIndividual kl = localO.getLabelImplementation(localO.getLabelForViewAndEvent(ourView, EventIRI));
		ViewLabelDataset v = KIDSDatasetFactory.getViewLabelDataset(chosenDset.getIRI(), EventIRI, localO);
		// TODO: not lList, we need the identifying features here:
		//nlpv.generateView(localO.getDatasetLocation(chosenDset), localO, lList);
		
		//ViewLabelDataset v = new ViewLabelDataset();
		//v.init(IRI.create(EventIRI));

		v.getMatchingInstances(sigSet);

	}

	class BroStreamGobbler extends StreamGobbler {

		int cvaluesUsed = 0;
		int count = 0;

		BroStreamGobbler(InputStream is, String type) {
			super(is, type);
		}

		BroStreamGobbler(InputStream is, String type, DatasetView v) {
			super(is, type, v);
		}
		
		public void run() {
			try {
				InputStreamReader isr = new InputStreamReader(is);
				BufferedReader br = new BufferedReader(isr);
				String line = null;
				while ( (line = br.readLine()) != null) {
						Matcher rexr = rexp.matcher(line);
						count = count+1;
						if (count % 100000 == 0){
							System.err.println(String.format("\t.. [Bro] Processed %d packets",count));
						}
						if (rexr.find()){
							HashMap<IRI, String> idmap = new HashMap<IRI, String>();
							Iterator<IRI> ifs = v.getIdentifyingFeatures().iterator();
							IRI identFeature;
							while (ifs.hasNext()){
								identFeature = ifs.next();
								if (identFeature.toString().equals(featureIRI + "PacketID")){
									idmap.put(identFeature, rexr.group("PID"));
								} else if (identFeature.toString().equals(featureIRI + "IPv4SourceAddressSignalDomain")){
									idmap.put(identFeature, rexr.group("SIP"));
								} else if (identFeature.toString().equals(featureIRI + "IPv4DestinationAddressSignalDomain")){
									idmap.put(identFeature, rexr.group("DIP"));
								} else {
									// We need a feature according to the view that we don't support
									// in this detector:
									throw new UnimplementedIdentifyingFeatureException(String.format("Identifying Feature %s not currently supported by KIDSBroDetector",identFeature.toString()));

								}
							}
							DataInstance di = new KIDSNativeLibpcapDataInstance(idmap);
							DataInstance sdi = KIDSAbstractDetector.getDataInstance(di);
							if (sdi != di){
								cvaluesUsed++;
							}
							toReturn.add(sdi);
						}
				}
			} catch (IOException | UnimplementedIdentifyingFeatureException ioe) {
			  System.err.println(ioe);
              ioe.printStackTrace();  
            }
		}
		
	}
}
