/**
 * Test driver for the computation of CID values from a dataset / signal / event combination.
 * 
 * Given:
 * - OWLNamedIndividual DataSetIND
 * - OWLNamedIndividual SignalIND
 * - OWLNamedIndividual EventIND (optional)
 * 
 * Determine the CID value for the signal in the data set.
 * 
 */
package net.strasnet.kids.measurement.test;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.util.SimpleIRIMapper;

import net.strasnet.kids.KIDSOntologyDatatypeValuesException;
import net.strasnet.kids.KIDSOntologyObjectValuesException;
import net.strasnet.kids.detectors.UnimplementedIdentifyingFeatureException;
import net.strasnet.kids.detectorsyntaxproducers.KIDSIncompatibleSyntaxException;
import net.strasnet.kids.measurement.CorrelatedViewLabelDataset;
import net.strasnet.kids.measurement.KIDSDatasetFactory;
import net.strasnet.kids.measurement.KIDSEIDMeasure;
import net.strasnet.kids.measurement.KIDSMeasurementOracle;
import net.strasnet.kids.measurement.KIDSUnEvaluableSignalException;
import net.strasnet.kids.measurement.RecursiveResult;
import net.strasnet.kids.measurement.correlationfunctions.IncompatibleCorrelationValueException;

/**
 * @author chrisstrasburg
 * The input interface is a series of configuration file values that specify:
 *   - The TBOX location
 *   - The TBOX IRI
 *   - The ABOX location
 *   - The ABOX IRI
 *   
 *   Once read in, the user can select an event, or set of events, to evaluate, along with a time period.
 *   
 *   -- If a time period is selected, it should be used to evaluate the data sets according to a dynamic evaluation of the datasets.
 */
public class KIDSSignalSelectionInterface {

	private Set<IRI> signals = null;
	private static final HashMap<String,String> configFileValues = new HashMap<String,String>();
	static {
		configFileValues.put("ABoxFile", "/dev/null");
		configFileValues.put("ABoxIRI", "/dev/null");
		configFileValues.put("TBoxFile", "/dev/null");
		configFileValues.put("TBoxIRI", "/dev/null");

		// TODO: Read the set of events from the ontology
		configFileValues.put("EventIRI", "/dev/null");
		//configFileValues.put("DatasetIRI", "/dev/null");

		// TODO: Read the available time periods from the ontology
		configFileValues.put("TimePeriodIRI", "/dev/null");
	}
	
	public KIDSSignalSelectionInterface(){
		signals = new HashSet<IRI>();
	}

	
	/**
	 * Recursively evaluate the signal set, letting the maximum EID "bubble-up" to the top.
	 * 
	 * Changes to Support Correlation:
	 * 
	 * @param triedValues 
	 * @throws KIDSIncompatibleSyntaxException 
	 * @throws IOException 
	 * @throws ClassNotFoundException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 * @throws KIDSOntologyObjectValuesException 
	 * @throws KIDSOntologyDatatypeValuesException 
	 * @throws UnimplementedIdentifyingFeatureException 
	 * @throws KIDSUnEvaluableSignalException 
	 */
	private RecursiveResult testSignalSet_iter(KIDSMeasurementOracle kmo, Map<Set<IRI>, RecursiveResult> triedValues, 
			Set<IRI> sigsToEval, CorrelatedViewLabelDataset cvd) throws KIDSOntologyDatatypeValuesException, KIDSOntologyObjectValuesException, InstantiationException, IllegalAccessException, ClassNotFoundException, IOException, KIDSIncompatibleSyntaxException, UnimplementedIdentifyingFeatureException {
		RecursiveResult toReturn = null;
		Set<IRI> key = null;
		//Set<IRI> sigsToEval = sigEvalMap.keySet();
		
		System.out.print("Testing signal set: ");
		for (IRI myI : sigsToEval){
			System.out.print(myI.getFragment() + ",");
		}
		System.out.println(" ");
		
		// If the list contains only one element, evaluate it and return the result, caching the members (?)
		if (sigsToEval.size() == 1){
			IRI sig = sigsToEval.iterator().next();
			key = new HashSet<IRI>();
			key.add(sig);
			if (triedValues.containsKey(key)){
				return triedValues.get(key);
		    }
			// Try with the correlated dataset
			// Try with each feasible dataset, and use the maximum:
			double maxValue = 0.0;
			// We'll work with a correlated data set, and only a single one
			double eidVal = 0;
			try {
					toReturn =  KIDSEIDMeasure.getKIDSEIDMeasureValue(kmo, sigsToEval, cvd);
					eidVal = toReturn.getEID();
					if (eidVal > maxValue){
						maxValue = eidVal;
					}
				} catch (KIDSUnEvaluableSignalException e) {
					System.err.println(e.getMessage());
					e.printStackTrace();
					return null;
				}
			triedValues.put(key, toReturn);
			System.out.println(toReturn);
		} else {
			// If we have more than one element, remove each in turn and evaluate the rest of them, keeping track of the maximum value
			double maxEID = 0;
			try {
				if (triedValues.containsKey(sigsToEval)){
					maxEID = triedValues.get(sigsToEval).getEID(); 
				} else {
					// Try with each feasible dataset, and use the maximum:
					double eidVal = 0;
					toReturn = KIDSEIDMeasure.getKIDSEIDMeasureValue(kmo, sigsToEval, cvd);
					eidVal = toReturn.getEID();
					if (eidVal > maxEID){
						maxEID = eidVal;
					}
				}
			} catch (KIDSUnEvaluableSignalException e){
				// Couldn't evaluate:
				maxEID = -1;
				System.err.println("Warning: " + e.getMessage());
			} 
			if (toReturn != null){
				System.out.println(toReturn);
				triedValues.put(sigsToEval, toReturn);
			
				List<IRI> curSigs = new LinkedList<IRI>(sigsToEval);
			
				for (IRI curSig : curSigs){
					Set<IRI> cSigsToEval = new HashSet<IRI>(sigsToEval);
					cSigsToEval.remove(curSig);
					if (!triedValues.containsKey(cSigsToEval)){
						RecursiveResult candidateResult = testSignalSet_iter(kmo, triedValues, cSigsToEval, cvd);
						triedValues.put(cSigsToEval, candidateResult);
						if (candidateResult.getEID() >= toReturn.getEID() && 
								candidateResult.getSignals().size() <= toReturn.getSignals().size()){
							toReturn = candidateResult;
						}
					}
				}
		}
		}
			
		// Return the one with the maximum value.
		return toReturn;
	}
		
	/**
	 * Test the signal set returned from the ontology.
	 * Changes to support correlation -- Done
	 * @throws UnimplementedIdentifyingFeatureException 
	 */
	public void testSignalSet(String ABOXFile, 
							  String ABOXIRI,
							  String TBOXFile,
							  String TBOXIRI,
							  String EventIRI,
							  String DatasetIRI,
							  String TimePeriodIRI) throws UnimplementedIdentifyingFeatureException{
			KIDSMeasurementOracle myGuy = null;
			try {
				myGuy = new KIDSMeasurementOracle();
	            List<SimpleIRIMapper> m = new LinkedList<SimpleIRIMapper>();
	            m.add(new SimpleIRIMapper(IRI.create(ABOXIRI), IRI.create(ABOXFile)));
	            m.add(new SimpleIRIMapper(IRI.create(TBOXIRI), IRI.create(TBOXFile)));
				myGuy.loadKIDS(IRI.create(ABOXIRI), m);
			} catch (IOException e) {
				e.printStackTrace();
				System.exit(1);
			} catch (Exception e){
				System.err.println(e);
				e.printStackTrace();
				System.exit(1);
			}
			
		try {
			// If we have a time-period, get datasets from the time period.  Otherwise (for backward compatibility), load
			// the specified dataset.
			Set<String> ourDSIRIList = new HashSet<String>();
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
			
			//      Create the datasets first, then iterate over the dataset objects rather than the IRIs
			//      When returning the datasets, return a <Dataset,Set<SignalIRI>> map, where the signal set is
			//      the set of signals which can actually be evaluated over the data set.  Should be the union of
			//      individual signal sets for individual datasets.
			List<CorrelatedViewLabelDataset> DSOBJList = KIDSDatasetFactory.getCorrelatedDatasets(ourDSIRIList, IRI.create(EventIRI), myGuy);

			for (CorrelatedViewLabelDataset ourDS: DSOBJList){
				// For each of these signals, we need to map to a dataset and detector
				//Dataset ourDS = KIDSDatasetFactory.getViewLabelDataset(IRI.create(DatasetIRI), IRI.create(EventIRI), myGuy);
				assert(ourDS != null);
				System.out.println("Evaluating CDI with the following properties:");
				System.out.println("Instances:\t" + ourDS.numInstances());
				System.out.println("cInstances:\t" + ourDS.numCorrelatedInstances());
				System.out.println("Events:\t" + ourDS.numEventOccurrences());
				System.out.println("EvInstances:\t" + ourDS.numPositiveInstances().length);
				System.out.println("cEvInstances:\t" + (ourDS.numPositiveCorrelatedInstances().length - 1));
		
				// Assess all subsets of available signals, recording the EID values for each:
				Map<Set<IRI>, RecursiveResult> triedValues = new HashMap<Set<IRI>, RecursiveResult>();
				RecursiveResult rr = this.testSignalSet_iter(myGuy, triedValues, signals,ourDS);
			
				System.out.println("Maximum signal set (EID = " + rr.getEID() + "):");
					for (IRI signalC : rr.getSignals()){
						System.out.println("\t" + signalC.toString());
					}
				
				System.out.println("All results:");
				for (Set<IRI> curKey : triedValues.keySet()){
					StringBuilder keyString = new StringBuilder();
					for (IRI k : curKey){
						keyString.append(k.getFragment().toString() + " ");
					}
					System.out.println("\t" + triedValues.get(curKey).getEID() + "\t{" + keyString + "}");
				}
			}
			
			//KIDSSnortDetectorSyntax kds = new KIDSSnortDetectorSyntax();
			//kds.init(myGuy);
			//System.out.println("Optimal Snort rule: \n" + FileUtils.readFile(kds.getDetectorSyntax(rr.ourSigs)));
		} catch (KIDSOntologyDatatypeValuesException e) {
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
		} catch (KIDSOntologyObjectValuesException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (KIDSIncompatibleSyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (KIDSUnEvaluableSignalException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IncompatibleCorrelationValueException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	/** 
	 * UTILITY METHODS
	 */
	
	
	/**
	 * 
	 * @param sourceFile - A string giving the path of a configuration file to load values from.
	 * @param requiredValueSet - A set of keys which *must* be present in the properties file for it to be considered
	 * a complete configuration.  All values from the properties file will be loaded regardless, however, if any of these
	 * required values are missing it will produce error messages and the method will return a null value.
	 * @return A hash map from property keys to property values, as defined in the file
	 */
	public static HashMap<String,String> loadPropertiesFromFile(String sourceFile, Set<String> requiredValueSet){
		boolean cerr = false;
		Properties p = new Properties ();
		HashMap<String,String> cVals = new HashMap<String,String>();
		
		try {
			p.load(new FileReader(new File(sourceFile)));
			for (String cstring : requiredValueSet){
				if (! p.containsKey(cstring)){
					System.err.println("Config file does not define property " + (String)cstring);
					cerr = true;
				}
			}
			for (Object kstring : p.keySet()){
				if (!requiredValueSet.contains((String)kstring)){
					System.err.println("Config file contains unknown property " + (String)kstring);
					cerr = true;
				}
				cVals.put((String)kstring, (String)p.getProperty((String)kstring));
			}
			
			if (cerr){
				return null;
			}
			
		} catch (IOException e1) {
			e1.printStackTrace();
			return null;
		}
		return cVals;
	}
				
	/**
	 * @param args - one arg is the libpcap file to parse
	 * run the tests
	 * @throws UnimplementedIdentifyingFeatureException 
	 */
	public static void main(String[] args) throws UnimplementedIdentifyingFeatureException {
		String usage = "Usage: KIDSSignalSelection <pathToConfigFile>";
		if (args.length != 1){
			System.err.println(usage);
			java.lang.System.exit(1);
		}
		
			HashMap<String,String> cVals = KIDSSignalSelectionInterface.loadPropertiesFromFile(args[0], KIDSSignalSelectionInterface.configFileValues.keySet());
		
			KIDSSignalSelectionInterface kss = new KIDSSignalSelectionInterface();
			//TODO: Populate ABOX File
			kss.testSignalSet(cVals.get("ABoxFile"), 
							  cVals.get("ABoxIRI"), 
							  cVals.get("TBoxFile"), 
							  cVals.get("TBoxIRI"), 
							  cVals.get("EventIRI"), 
							  cVals.get("DatasetIRI"),
							  cVals.get("TimePeriodIRI"));
			
		
	}

}
