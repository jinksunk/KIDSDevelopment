package net.strasnet.kids.measurement.test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import net.strasnet.kids.KIDSOntologyDatatypeValuesException;
import net.strasnet.kids.KIDSOntologyObjectValuesException;
import net.strasnet.kids.detectors.KIDSGrepDetector;
import net.strasnet.kids.detectors.UnimplementedIdentifyingFeatureException;
import net.strasnet.kids.detectorsyntaxproducers.KIDSGrepSyntax;
import net.strasnet.kids.detectorsyntaxproducers.KIDSIncompatibleSyntaxException;
import net.strasnet.kids.measurement.CorrelatedViewLabelDataset;
import net.strasnet.kids.measurement.CorrelationFunction;
import net.strasnet.kids.measurement.DataInstance;
import net.strasnet.kids.measurement.Dataset;
import net.strasnet.kids.measurement.KIDSDatasetFactory;
import net.strasnet.kids.measurement.KIDSMeasurementOracle;
import net.strasnet.kids.measurement.KIDSUnEvaluableSignalException;
import net.strasnet.kids.measurement.ViewLabelDataset;
import net.strasnet.kids.measurement.correlationfunctions.IncompatibleCorrelationValueException;
import net.strasnet.kids.measurement.datasetlabels.DatasetLabel;
import net.strasnet.kids.measurement.datasetviews.DatasetView;
import net.strasnet.kids.measurement.datasetviews.EventLogTextFileView;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.util.SimpleIRIMapper;

public class testManyThings {
	/**
	 * A general class to test various aspects of the KIDS framework.  Tests are turned on and off by comments in the main() method.
	 */
	public HashMap<String,String> configFileValues;
	public KIDSMeasurementOracle kmo = new KIDSMeasurementOracle();
	
	/**
	 * 
	 * @param propertiesFile - The file to load the knowledge base locations from
	 * @throws Exception 
	 */
	public testManyThings(String propertiesFile) throws Exception{
		configFileValues = new HashMap<String,String>();
		
		configFileValues.put("ABoxFile", "/dev/null");
		configFileValues.put("ABoxIRI", "/dev/null");
		configFileValues.put("TBoxFile", "/dev/null");
		configFileValues.put("TBoxIRI", "/dev/null");
		configFileValues.put("EventIRI", "/dev/null");
		configFileValues.put("TimePeriodIRI", "/dev/null");
	
		boolean cerr = false;
		Properties p = new Properties ();
			p.load(new FileReader(new File(propertiesFile)));
			for (String cstring : configFileValues.keySet()){
				if (! p.containsKey(cstring)){
					System.err.println("Config file does not define property " + (String)cstring);
					cerr = true;
				}
			}
			for (Object kstring : p.keySet()){
				if (!configFileValues.containsKey((String)kstring)){
					System.err.println("Config file contains unknown property " + (String)kstring);
					cerr = true;
				}
				configFileValues.put((String)kstring, (String)p.getProperty((String)kstring));
			}
			
			if (cerr){
				System.exit(1);
			}
			// Create the oracle
    		List<SimpleIRIMapper> m = new LinkedList<SimpleIRIMapper>();
    		m.add(new SimpleIRIMapper(IRI.create(configFileValues.get("ABoxIRI")), IRI.create(configFileValues.get("ABoxFile"))));
    		m.add(new SimpleIRIMapper(IRI.create(configFileValues.get("TBoxIRI")), IRI.create(configFileValues.get("TBoxFile"))));
			kmo.loadKIDS(IRI.create(configFileValues.get("ABoxIRI")), m);
	}
	
	public void testGrepDetectorSyntax() throws KIDSOntologyObjectValuesException, KIDSOntologyDatatypeValuesException, KIDSIncompatibleSyntaxException, KIDSUnEvaluableSignalException{
			// Test the thing:
			KIDSGrepSyntax kgs = new KIDSGrepSyntax();
			kgs.init(kmo);
			
			Set<IRI> sigSet = new HashSet<IRI>();
			// Add some hard-coded signal IRIs to the set to evaluate:
			String aboxIRI = configFileValues.get("ABoxIRI");
			sigSet.add(IRI.create(aboxIRI + "#MaliciousSourceIPSignal"));
			// TODO: Add the Get Parameter String value too...
			System.out.println(kgs.getDetectorSyntax(sigSet));
	}
	
	public void testGrepDetector() throws KIDSOntologyDatatypeValuesException, KIDSOntologyObjectValuesException, InstantiationException, IllegalAccessException, ClassNotFoundException, NumberFormatException, IOException, KIDSIncompatibleSyntaxException, KIDSUnEvaluableSignalException, UnimplementedIdentifyingFeatureException {
		final IRI detectorIRI = IRI.create("http://www.semantiknit.com/ontologies/2014/03/29/CodeRedExperiment3.owl#IISLogGrepRuleForCodeRed");
		// Test the grep detector - first create one
		KIDSGrepDetector kgd = new KIDSGrepDetector();
		
		// Initialize it
		String toExecute = kmo.getDetectorExecutionString(detectorIRI);
		kgd.init(toExecute, detectorIRI, kmo);
		
		// Look for a few pre-defined signals:
		Set<IRI> sigsToMatch = new HashSet<IRI>();
		sigsToMatch.add(IRI.create("http://www.semantiknit.com/ontologies/2014/03/29/CodeRedExperiment3.owl#MaliciousSourceIPSignal"));
		EventLogTextFileView evtv = new EventLogTextFileView();

		// Setup the dataset
		IRI datasetIRI = IRI.create("http://www.semantiknit.com/ontologies/2014/03/29/CodeRedExperiment3.owl#CodeRedEvalSYSLOGDataset1");
		String datasetLocation = kmo.getDatasetLocation(kmo.getOwlDataFactory().getOWLNamedIndividual(datasetIRI));
		
		// Setup the view - Don't need this?
		List<OWLNamedIndividual> dvs = kmo.getAvailableViews(datasetIRI, IRI.create(configFileValues.get("EventIRI")));
		String ourDVimp = kmo.getViewImplementation(dvs.get(0));
		DatasetView ourDV = KIDSDatasetFactory.getViewGenerator(ourDVimp);
		//evtv.generateView(datasetLocation, kmo, identifyingFeatures);

		// Setup the labeled dataset
		//OWLNamedIndividual edlInd = kmo.getLabelForViewAndEvent(dvs.get(0), IRI.create(configFileValues.get("EventIRI")));
		ViewLabelDataset edl = KIDSDatasetFactory.getViewLabelDataset(datasetIRI, IRI.create(configFileValues.get("EventIRI")), kmo);
		Set<DataInstance> result = edl.getMatchingInstances(sigsToMatch);
		
	}
	
	public void testCorrelatedDataset() throws KIDSOntologyDatatypeValuesException, KIDSOntologyObjectValuesException, NumberFormatException, InstantiationException, IllegalAccessException, ClassNotFoundException, IOException, IncompatibleCorrelationValueException, KIDSUnEvaluableSignalException, KIDSIncompatibleSyntaxException, UnimplementedIdentifyingFeatureException {
		Set<String> ourDSIRIList;
		ourDSIRIList = kmo.getDatasetListForEventAndTimePeriod(IRI.create(configFileValues.get("EventIRI")), 
																	 IRI.create(configFileValues.get("TimePeriodIRI")));

		// Create a ViewLabelDataset for each candidate dataset
		HashMap<Dataset,DatasetLabel> dsets = new HashMap<Dataset,DatasetLabel>();
		for (String dsIRI : ourDSIRIList){
			ViewLabelDataset vld = KIDSDatasetFactory.getViewLabelDataset(IRI.create(dsIRI), 
												   IRI.create(configFileValues.get("EventIRI")), 
												   kmo);
			dsets.put(vld, vld.getDatasetLabel());
		}

		// Get all possible correlation functions between the set of datasets
		Set<CorrelationFunction> ourCFList = kmo.getCompatibleCorrelationFunctions(dsets.keySet());

		// Get a correlated dataset for each correlation function:
		CorrelatedViewLabelDataset cvd = new CorrelatedViewLabelDataset(ourCFList.iterator().next(), dsets);

		// Verify the number of instances resulting in the correlated data set
		int numInstances = cvd.numInstances();

		// Verify the number of events and event instances resulting in the correlated data set
		int numEvents = cvd.numEventOccurrences();
		int[] numEventInstances = cvd.numPositiveInstances();
	}

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		// Load the properties file
		String usage = "Usage: testManyThings <pathToConfigFile>";
		if (args.length != 1){
			System.err.println(usage);
			java.lang.System.exit(1);
		}
		testManyThings tmt = new testManyThings(args[0]);
		//tmt.testGrepDetectorSyntax();
		//tmt.testGrepDetector();
		tmt.testCorrelatedDataset();
	}
}
