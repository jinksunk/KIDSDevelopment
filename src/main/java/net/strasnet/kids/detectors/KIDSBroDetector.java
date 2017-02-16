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
import java.util.TreeMap;
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
import net.strasnet.kids.measurement.datasetinstances.KIDSBroDataInstance;
import net.strasnet.kids.measurement.datasetinstances.KIDSNativeLibpcapDataInstance;
import net.strasnet.kids.measurement.datasetviews.DatasetView;
import net.strasnet.kids.measurement.datasetviews.NativeLibPCAPView;
import net.strasnet.kids.measurement.test.KIDSSignalSelectionInterface;
import net.strasnet.kids.measurement.test.KIDSTestSingleSignal;
import net.strasnet.kids.measurement.test.TestOracleFactory;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
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
	
	
	/**
	 * Bro rule output sample from this detector:
	 */
	private static final Logger logme = LogManager.getLogger(KIDSBroDetector.class.getName());
	//private static String regexPattern = "[\\d/-:\\.]+\\s+\\[\\*\\*\\].*\\s+ID:(?<PID>\\d+)\\s.*";
//	private static String regexPattern = "(?<TIMESTAMP>[\\d\\/\\-:\\.]+)\\s+\\[\\*\\*\\]\\s+.*(<?SIP>\\d+\\.\\d+\\.\\d+\\.\\d+)[^-]+\\s->\\s(<?DIP>\\d+\\.\\d+\\.\\d+\\.\\d+).*ID:(<?ID>\\d+)\\s+";
	private static String regexPattern = "(?<TIMESTAMP>[\\d\\.]+),(?<PID>\\d+),(?<SIP>\\d+\\.\\d+\\.\\d+\\.\\d+),(?<DIP>\\d+\\.\\d+\\.\\d+\\.\\d+)\\s*";
	
	private static final Map<String, Boolean> supportedFeatures = new HashMap <String, Boolean>();
	static {
		supportedFeatures.put(featureIRI + "TCPPayloadSizeDomain",true);
		};

	/** Configuration file values required for testing */
	private static final HashMap<String,String> configFileTestValues = new HashMap<String,String>();
	static {
		configFileTestValues.put("ABoxFile", "/dev/null");
		configFileTestValues.put("ABoxIRI", "/dev/null");
		configFileTestValues.put("TBoxFile", "/dev/null");
		configFileTestValues.put("TBoxIRI", "/dev/null");
		configFileTestValues.put("EventIRI", "/dev/null");
		//configFileValues.put("DatasetIRI", "/dev/null");
		configFileTestValues.put("DetectorIRI", "/dev/null");
		configFileTestValues.put("TestSignalIRI", "/dev/null");
	}
		
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
	    super.resetOrderMap();
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
				results = this.sigMap.get(signal);
			} else {
				HashSet<DataInstance> tResults = new HashSet<DataInstance>();
				results = getMatchingInstances(signal, v);
				/**
				int count = 0;
				for (DataInstance di : results){
					tResults.add(KIDSAbstractDetector.getDataInstance(di));
					count++;
					if (count % 100000 == 0){
						System.err.println(String.format("[Bro] - Processed %d records...",count));
					}
				}
				results = tResults;
				*/
				logme.debug(String.format("Adding cache entry (size = %d) for %s",results.size(), signal));
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
		
		Set<DataInstance> toReturn = new HashSet<DataInstance>();
		Set<IRI> signals = new HashSet<IRI>();
		signals.add(signal);

		String[] runBroString = {executionCommand, "-r",v.getViewLocation(),ourSyn.getDetectorSyntax(signals)}; 

		logme.info("Executing: " + StringUtils.join(runBroString," "));
		Process runBro = Runtime.getRuntime().exec(runBroString); 
	    super.resetOrderMap();
		BroStreamGobbler eGobble = new BroStreamGobbler(runBro.getErrorStream(), "ERROR");
		BroStreamGobbler iGobble = new BroStreamGobbler(runBro.getInputStream(), "OUTPUT", v);
		eGobble.start();
		iGobble.start();
		
		try {
			if (runBro.waitFor() != 0){
				eGobble.join();
				logme.error(eGobble.getOutput());
			}
			iGobble.join();
		} catch (InterruptedException e){
			throw new IOException("Command interrupted: " + this.executionCommand);
		}
		toReturn = iGobble.getReturnSet();
		logme.info(String.format("- Used %d / %d cache values (pool size now: %d).", iGobble.cvaluesUsed, iGobble.count, KIDSAbstractDetector.getDataInstancePoolSize()));
		logme.info(String.format("- %d duplicate instances found.", iGobble.dvaluesInSet));
		logme.info(String.format("- %d total instances found.", iGobble.icount));
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
		// Load from a config file:
		String usage = "Usage: KIDSBroDetector <pathToConfigFile>";
		if (args.length != 1){
			logme.error(usage);
			java.lang.System.exit(1);
		}
		HashMap<String,String> cVals = KIDSSignalSelectionInterface.loadPropertiesFromFile(args[0], KIDSBroDetector.configFileTestValues.keySet());
		if (cVals == null){
			System.exit(1);
		}
		
		KIDSBroDetector.runTests(cVals);
	}

	private static void runTests(HashMap<String, String> cVals) throws Exception{
		String ABOXLocation = cVals.get("ABoxFile");
		String ABOXIRI = cVals.get("ABoxIRI");
		String TBOXLocation = cVals.get("TBoxFile");
		String TBOXIRI = cVals.get("TBoxIRI");
		IRI DetectorIRI = IRI.create(cVals.get("DetectorIRI"));
		IRI TestSignalIRI = IRI.create(cVals.get("TestSignalIRI"));
		IRI EventIRI = IRI.create(cVals.get("EventIRI"));

		KIDSMeasurementOracle localO = TestOracleFactory.getKIDSMeasurementOracle(TBOXIRI, TBOXLocation, ABOXIRI, ABOXLocation);

		KIDSBroDetector beds = new KIDSBroDetector();
		beds.init(localO.getDetectorExecutionString(DetectorIRI), DetectorIRI, localO);

		Set<IRI> sigSet = new HashSet<IRI>();
		sigSet.add(TestSignalIRI);
		
		Set<OWLNamedIndividual> dSets = localO.getDatasetsForEvent(EventIRI);
		
		// In this case, we want to ensure we get the libpcap dataset:
		Map<IRI, List<OWLNamedIndividual>> dsetViewMap = new HashMap<IRI, List<OWLNamedIndividual>>();
		for (OWLNamedIndividual dSet : dSets){
			List<OWLNamedIndividual> tViews = localO.getAvailableViews(dSet.getIRI(), EventIRI);
			dsetViewMap.put(dSet.getIRI(), tViews);

			// Pull out the libpcap view:
			NativeLibPCAPView nlpv = new NativeLibPCAPView();
			OWLNamedIndividual ourView = dsetViewMap.get(dSet.getIRI()).iterator().next();
			nlpv.setIRI(ourView.getIRI());
		//localO.getLabelForViewAndEvent(ourView, EventIRI);
		//List<IRI> lList = new LinkedList<IRI>();
		//OWLNamedIndividual kl = localO.getLabelImplementation(localO.getLabelForViewAndEvent(ourView, EventIRI));
			ViewLabelDataset v = KIDSDatasetFactory.getViewLabelDataset(dSet.getIRI(), EventIRI, localO);
		// TODO: not lList, we need the identifying features here:
		//nlpv.generateView(localO.getDatasetLocation(chosenDset), localO, lList);
		
		//ViewLabelDataset v = new ViewLabelDataset();
		//v.init(IRI.create(EventIRI));

			v.getMatchingInstances(sigSet);
		}

	}

	class BroStreamGobbler extends StreamGobbler {

		int cvaluesUsed = 0; // Number of cached values seen by this detector
		int nvaluesUsed = 0; // Number of new values seen by this detector
		int count = 0;
		int icount = 0;
		int dvaluesInSet = 0;

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
							logme.info(String.format("Processed %d packets",count));
						}
						if (rexr.find()){
							icount = icount + 1;
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
								} else if (identFeature.toString().equals(featureIRI + "ObservationOrder")){
									continue; // This will be added later, after the key string is built
								} else {
									// We need a feature according to the view that we don't support
									// in this detector:
									throw new UnimplementedIdentifyingFeatureException("Identifying Feature not currently supported by KIDSBroDetector",identFeature.toString());
								}
							}

							LinkedList<IRI> orderKey = new LinkedList<IRI>();
							orderKey.add(IRI.create(featureIRI + "PacketID"));
							orderKey.add(IRI.create(featureIRI + "IPv4SourceAddressSignalDomain"));
							orderKey.add(IRI.create(featureIRI + "IPv4DestinationAddressSignalDomain"));
							addOrderKeyToIDMap(orderKey, idmap);


						  	DataInstance di = new KIDSBroDataInstance(idmap);
							DataInstance sdi = KIDSAbstractDetector.getDataInstance(di);
							if (sdi != di){
								cvaluesUsed++;
							} else {
                                nvaluesUsed++;
                                if (nvaluesUsed == 1){
								  logme.debug(String.format("No existing entry found for e.g.: %s; from line %s", sdi.getID(), line));
                                }
							}
							if (!toReturn.add(sdi)){
								dvaluesInSet++;
						    	if (dvaluesInSet < 5){
						    	    logme.error(String.format("Detector -- Duplicate data instance added to return set e.g.: %s (from line %s)",sdi.getID(), line));
						    	}
							}
						}
				}
			} catch (IOException | UnimplementedIdentifyingFeatureException ioe) {
			  logme.error(ioe);
              ioe.printStackTrace();  
            }
		}
	}
		
		
}
