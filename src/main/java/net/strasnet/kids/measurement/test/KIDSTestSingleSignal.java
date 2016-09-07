package net.strasnet.kids.measurement.test;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import net.strasnet.kids.KIDSOntologyDatatypeValuesException;
import net.strasnet.kids.KIDSOntologyObjectValuesException;
import net.strasnet.kids.detectors.KIDSDetector;
import net.strasnet.kids.detectors.KIDSDetectorFactory;
import net.strasnet.kids.detectors.UnimplementedIdentifyingFeatureException;
import net.strasnet.kids.detectorsyntaxproducers.KIDSIncompatibleSyntaxException;
import net.strasnet.kids.measurement.CorrelatedViewLabelDataset;
import net.strasnet.kids.measurement.CorrelationDataInstance;
import net.strasnet.kids.measurement.CorrelationFunction;
import net.strasnet.kids.measurement.DataInstance;
import net.strasnet.kids.measurement.Dataset;
import net.strasnet.kids.measurement.KIDSDatasetFactory;
import net.strasnet.kids.measurement.KIDSMeasurementOracle;
import net.strasnet.kids.measurement.KIDSUnEvaluableSignalException;
import net.strasnet.kids.measurement.ViewLabelDataset;
import net.strasnet.kids.measurement.correlationfunctions.IncompatibleCorrelationValueException;
import net.strasnet.kids.measurement.correlationfunctions.KIDSCorrelationFunctionFactory;
import net.strasnet.kids.measurement.datasetlabels.DatasetLabel;
import net.strasnet.kids.measurement.datasetviews.DatasetView;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.util.SimpleIRIMapper;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/**
 * This class will read a configuration file which specifies:
 *  - The name of a signal to test
 *  - The location of the ABox and TBox files and ontologies
 * 
 * The file will produce:
 *  - The number of matching raw instances according to the ontology
 *  - The number of matching correlated instances according to the 
 *    ontology
 *  - The number of event related raw instances
 *  - The number of non-event related raw instances
 *  - The number of event related correlated instances
 *  - The number of non-event related correlated instances
 *  
 * @author cstras
 *
 */

public class KIDSTestSingleSignal {

	private static final Logger logme = LogManager.getLogger(KIDSTestSingleSignal.class.getName());
	
	private static final HashMap<String,String> configFileValues = new HashMap<String,String>();
	static {
		configFileValues.put("ABoxFile", "/dev/null");
		configFileValues.put("ABoxIRI", "/dev/null");
		configFileValues.put("TBoxFile", "/dev/null");
		configFileValues.put("TBoxIRI", "/dev/null");
		configFileValues.put("EventIRI", "/dev/null");
		//configFileValues.put("DatasetIRI", "/dev/null");
		configFileValues.put("TimePeriodIRI", "/dev/null");
		configFileValues.put("TestSignalIRI", "/dev/null");
	}
	
	private String TimePeriodIRI;
	private String DatasetIRI;
	private String EventIRI;
	private KIDSMeasurementOracle myGuy;
	private List<CorrelatedViewLabelDataset> DSOBJList;
	private Dataset mOd;
	private CorrelatedViewLabelDataset cvld;
	
	/** Object Methods */
	public KIDSTestSingleSignal(){
		super();
		myGuy = null;
	}
	
	public Set<IRI> getApplicableSignalIRISet(){
		// If we have a time-period, get datasets from the time period.  Otherwise (for backward compatibility), load
		// the specified dataset.
		Set<String> ourDSIRIList = new HashSet<String>();
		Set<IRI> signals = new HashSet<IRI>();
		if (TimePeriodIRI == null){
			ourDSIRIList.add(DatasetIRI);
		} else {
			//      Okay, so the Oracle should just return the list, and the Factory should
			//      have a 'Get All Datasets' method which returns individual + correlated ones.
			//      Hmm, so are we using this to check multiple correlation functions?
			ourDSIRIList = myGuy.getDatasetListForEventAndTimePeriod(IRI.create(EventIRI), IRI.create(TimePeriodIRI));
		}
		
		// For each dataset, get the set of signals evaluable with that dataset and this event:
		for (String dsIRI : ourDSIRIList){
			signals.addAll(myGuy.getSignalsForDatasetAndEvent(IRI.create(dsIRI), IRI.create(EventIRI)));
		}
		
		return signals;
	}

	/**
	 * 
	 * @param myGuy2 - The KIDSMeasurementOracle to use for this model.
	 */
	private void setOracle(KIDSMeasurementOracle myGuy2) {
		this.myGuy = myGuy2;
	}
	
	/**
	 * 
	 * @param EventIRI - The string representation of the IRI to store
	 */
	private void setEventIRI(String EventIRI){
		this.EventIRI = EventIRI;
	}

	private void setCorrelatedDatasets(
			List<CorrelatedViewLabelDataset> correlatedDatasets) {
		this.DSOBJList = correlatedDatasets;
	}
	
	private List<CorrelatedViewLabelDataset> getCorrelatedDatasets(){
		return this.DSOBJList;
	}
	
	private void setDataset(Dataset vld){
		this.mOd = vld;
	}
	
	/**
	 * Given a signal individual, evaluate it on the applicable data sets, and return the number
	 * of raw instances matched, along with the dataset(s) matched.
	 * 
	 * @param Signal
	 */
	public int getSingleSignalRawInstances(IRI Signal){
		Set<IRI> singleSigSet = new HashSet<IRI>();
		singleSigSet.add(Signal);

		// Evaluate the signal - to do this we need:
		// Each dataset to apply the detector to
		Map <IRI, Set<IRI>> signalDatasets = this.myGuy.getDatasetsForSignal(Signal);
		
		// Return the number of raw instances matched
		IRI eIRI = IRI.create(this.EventIRI);
		for (IRI dataset : signalDatasets.keySet()){
			Dataset od;
			try {
				od = KIDSDatasetFactory.getViewLabelDataset(dataset, 
						eIRI, this.myGuy);
				logme.info("[D] - Event: " + eIRI.getFragment() + "\tDataset: " + dataset.getFragment() + "\tView: " + od.getViewIRI().getFragment());
				Set<DataInstance> rs = od.getMatchingInstances(singleSigSet);
				this.setDataset(od.getDataSubset(rs));
				return rs.size();
			} catch (KIDSOntologyDatatypeValuesException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (KIDSOntologyObjectValuesException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NumberFormatException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InstantiationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (KIDSUnEvaluableSignalException e) {
				logme.error("[W] - " + e.getMessage());
			} catch (KIDSIncompatibleSyntaxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (UnimplementedIdentifyingFeatureException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return -1;
	}
	
	public int getSingleSignalEventRawInstances() throws KIDSOntologyDatatypeValuesException, KIDSOntologyObjectValuesException, InstantiationException, IllegalAccessException, ClassNotFoundException, IOException, KIDSUnEvaluableSignalException, KIDSIncompatibleSyntaxException, UnimplementedIdentifyingFeatureException{
		int count = 0;
		Iterator<DataInstance> pi = this.mOd.getPositiveIterator();
		while (pi.hasNext()){
			DataInstance di = pi.next();
			count = count+1;
		}
		return count;
	}
	
	/** Static interface methods */
	public static KIDSTestSingleSignal getSignalModel(String ABoxFile, 
			  										  String ABoxIRI,
			  										  String TBoxFile,
			  										  String TBoxIRI,
			  										  String EventIRI,
			  										  String DatasetIRI,
			  										  String TimePeriodIRI,
			  										  String CFIRI,
			  										  String CDSIRI){
		KIDSTestSingleSignal KTSS = new KIDSTestSingleSignal();
		KIDSMeasurementOracle myGuy = null;
		Set<String> ourDSIRIList = new HashSet<String>();
		try {
			myGuy = new KIDSMeasurementOracle();
            List<SimpleIRIMapper> m = new LinkedList<SimpleIRIMapper>();
            m.add(new SimpleIRIMapper(IRI.create(ABoxIRI), IRI.create(ABoxFile)));
            m.add(new SimpleIRIMapper(IRI.create(TBoxIRI), IRI.create(TBoxFile)));
			myGuy.loadKIDS(IRI.create(ABoxIRI), m);
			// If we have a time-period, get datasets from the time period.  Otherwise (for backward compatibility), load
			// the specified dataset.
			if (TimePeriodIRI == null){
				ourDSIRIList.add(DatasetIRI);
			} else {
				//      Okay, so the Oracle should just return the list, and the Factory should
				//      have a 'Get All Datasets' method which returns individual + correlated ones.
				//      Hmm, so are we using this to check multiple correlation functions?
				ourDSIRIList = myGuy.getDatasetListForEventAndTimePeriod(IRI.create(EventIRI), IRI.create(TimePeriodIRI));
				// Get the view for this dataset
				// Get the label for this Dataset and Event 
			}
			//CorrelationFunction cf = KIDSCorrelationFunctionFactory.getCorrelationFunction(CFIRI);
			//HashMap<Dataset,DatasetLabel> CDSes = new HashMap<Dataset,DatasetLabel>();
			
			//for (String DSiri : ourDSIRIList){
				// Get a label for each dataset
				//List<OWLNamedIndividual> vList = myGuy.getAvailableViews(IRI.create(DSiri), IRI.create(EventIRI));
				//if (vList.size() == 0){
					//System.err.println("[D] - No views available for dataset " + DSiri);
				//} else {
					// Just take the first instance for now:
					//ViewLabelDataset vld = KIDSDatasetFactory.getViewLabelDataset(IRI.create(DSiri), 
																				//IRI.create(EventIRI), myGuy);
					//CDSes.put(vld, vld.getDatasetLabel());
				//}
			//}
			//KTSS.setCorrelatedViewLabelDataset(new CorrelatedViewLabelDataset(cf,CDSes));
		} catch (Exception e){
			//TODO - More granular excpetion handling here!
			logme.error(e);
			e.printStackTrace();
			System.exit(1);
		}
		KTSS.setOracle(myGuy);
		KTSS.setEventIRI(EventIRI);
		try {
			KTSS.setCorrelatedDatasets(KIDSDatasetFactory.getCorrelatedDatasets(ourDSIRIList, IRI.create(EventIRI), myGuy));
		} catch (KIDSOntologyDatatypeValuesException
				| KIDSOntologyObjectValuesException | NumberFormatException
				| InstantiationException | IllegalAccessException
				| ClassNotFoundException | IOException
				| KIDSUnEvaluableSignalException
				| KIDSIncompatibleSyntaxException
				| IncompatibleCorrelationValueException
				| UnimplementedIdentifyingFeatureException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return KTSS;
		
	}

	/** Test interface driver 
	 * @throws UnimplementedIdentifyingFeatureException 
	 * @throws KIDSIncompatibleSyntaxException 
	 * @throws KIDSUnEvaluableSignalException 
	 * @throws IOException 
	 * @throws ClassNotFoundException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 * @throws KIDSOntologyObjectValuesException 
	 * @throws KIDSOntologyDatatypeValuesException */
	public static void main(String[] args) throws KIDSOntologyDatatypeValuesException, KIDSOntologyObjectValuesException, InstantiationException, IllegalAccessException, ClassNotFoundException, IOException, KIDSUnEvaluableSignalException, KIDSIncompatibleSyntaxException, UnimplementedIdentifyingFeatureException {
		logme.error("error");
		logme.warn("warn");
		logme.info("info");
		logme.debug("debug");
		String usage = "Usage: KIDSTestSingleSignal <pathToConfigFile>";
		if (args.length != 1){
			logme.error(usage);
			java.lang.System.exit(1);
		}
		HashMap<String,String> cVals = KIDSSignalSelectionInterface.loadPropertiesFromFile(args[0], KIDSTestSingleSignal.configFileValues.keySet());
		if (cVals == null){
			System.exit(1);
		}

		// Initialize the model:
		KIDSTestSingleSignal ktss = KIDSTestSingleSignal.getSignalModel(cVals.get("ABoxFile"), 
				cVals.get("ABoxIRI"), 
				cVals.get("TBoxFile"), 
				cVals.get("TBoxIRI"), 
				cVals.get("EventIRI"), 
				cVals.get("DatasetIRI"), 
				cVals.get("TimePeriodIRI"),
				cVals.get("CorrelationFunctionIRI"), cVals.get("TestDatasetIRI"));
		
        //*  - The number of matching raw instances according to the ontology
		logme.info("== Begin Test of Signal " + cVals.get("TestSignalIRI") + " ==");
		int numRawInstances = ktss.getSingleSignalRawInstances(IRI.create(cVals.get("TestSignalIRI"))); 
		logme.info("\tRaw Instances: " + numRawInstances);

		//*  - The number of event related raw instances
		int numERInstances = ktss.getSingleSignalEventRawInstances(); 
		logme.info("\tEvent Related: " + numERInstances);

		//*  - The number of non-event related raw instances
		logme.info("\tNon-Event Related: " + (numRawInstances - numERInstances));

		//*  - The number of matching correlated instances according to the 
		//*    ontology
		List<CorrelatedViewLabelDataset> cdsList = ktss.getCorrelatedDatasets();
		int numCIs = cdsList.get(0).numCorrelatedInstances();
		logme.info("\tCorrelated Data Instances: " + numCIs);
		for (CorrelatedViewLabelDataset cd : cdsList){
		    logme.debug(cd);
		    for (CorrelationDataInstance cdi : cd.getMatchingCorrelatedInstances(new HashSet<IRI>(), true)){
		    	logme.debug(cdi);
		    }
		}

		//*  - The number of event related correlated instances
		logme.info("\tCorrelated Event Related Instances: ");
		int[] eventRelatedInstances = cdsList.get(0).numPositiveCorrelatedInstances();
		int totalEVCInstances = 0;
		for (int p = 0 ; p < eventRelatedInstances.length; p++){
			logme.info("\t\t " + p + " : " + eventRelatedInstances[p]);
			totalEVCInstances += eventRelatedInstances[p];
		}

		//*  - The number of non-event related correlated instances
		logme.info("\tCorrelated Normal Instances: " + (numCIs - totalEVCInstances));
		
	}
}
