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
import java.io.FileNotFoundException;
import java.io.FileReader;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.junit.Before;
import org.junit.Test;
import org.mindswap.pellet.utils.FileUtils;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.util.SimpleIRIMapper;

import net.strasnet.kids.KIDSOntologyDatatypeValuesException;
import net.strasnet.kids.KIDSOntologyObjectValuesException;
import net.strasnet.kids.detectorsyntaxproducers.KIDSIncompatibleSyntaxException;
import net.strasnet.kids.detectorsyntaxproducers.KIDSSnortDetectorSyntax;
import net.strasnet.kids.measurement.CorrelatedViewLabelDataset;
import net.strasnet.kids.measurement.DataInstance;
import net.strasnet.kids.measurement.Dataset;
import net.strasnet.kids.measurement.EventOccurrence;
import net.strasnet.kids.measurement.KIDSDatasetFactory;
import net.strasnet.kids.measurement.KIDSEIDMeasure;
import net.strasnet.kids.measurement.KIDSMeasurementIncompatibleContextException;
import net.strasnet.kids.measurement.KIDSMeasurementInstanceUnsupportedFeatureException;
import net.strasnet.kids.measurement.KIDSMeasurementOracle;
import net.strasnet.kids.measurement.KIDSUnEvaluableSignalException;
import net.strasnet.kids.measurement.correlationfunctions.IncompatibleCorrelationValueException;
import net.strasnet.kids.measurement.datasetlabels.DatasetLabel;
import net.strasnet.kids.measurement.datasetviews.DatasetView;
import net.strasnet.kids.measurement.datasetviews.KIDSLibpcapDataset;
import net.strasnet.kids.signalRepresentations.KIDSRepresentationInvalidRepresentationValueException;
import net.strasnet.kids.signalRepresentations.KIDSSignalByteMatchRepresentation;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

/**
 * @author chrisstrasburg
 *
 */
public class KIDSSignalSelectionInterface {

//	private static final String testABOX = "http://www.semantiknit.com/ontologies/2013/6/24/TestEventExperiment1.owl";
//	private static final String testABOXFile = "file:///Users/chrisstrasburg/Documents/academic-research/papers/2013-MeasurementPaper/experiments/TestEvent-Test1/TestEventExperiment1.owl";
//	private static final String testKBFile = "file:///Users/chrisstrasburg/Documents/academic-research/papers/2013-MeasurementPaper/experiments/TestEvent-Test1/kids.owl";
//	private static final String OntologyLocation = "http://solomon.cs.iastate.edu/ontologies/KIDS.owl";
	private Set<IRI> signals = null;
//	private static final IRI testDatasetIRI = IRI.create(testABOX + "#TestEvent1LIBPCAPDataset1");
//	private static final IRI testEventIRI = IRI.create(testABOX + "#TestEvent1");
	
	private static final HashMap<String,String> configFileValues = new HashMap<String,String>();
	static {
		configFileValues.put("ABoxFile", "/dev/null");
		configFileValues.put("ABoxIRI", "/dev/null");
		configFileValues.put("TBoxFile", "/dev/null");
		configFileValues.put("TBoxIRI", "/dev/null");
		configFileValues.put("EventIRI", "/dev/null");
		//configFileValues.put("DatasetIRI", "/dev/null");
		configFileValues.put("TimePeriodIRI", "/dev/null");
	}
	
	public KIDSSignalSelectionInterface(){
		signals = new HashSet<IRI>();
	}

	/**
	 * Represents pairs of <Signal,DataSet> to track which signals can be tested in which datasets.
	 */
	private class DSVector{
		
	}
	/**
	 * Represents the result of a signalSet evaluation - records the signal set evaluted, the dataset it was evaluted on, and the eidValue obtained.
	 * @author chrisstrasburg
	 *
	 */
	private class RecursiveResult {
		private Set<IRI> ourSigs;
		private double ourEID;
		private CorrelatedViewLabelDataset ourDataset;
		
		protected RecursiveResult (Set<IRI> signals, double eidValue, CorrelatedViewLabelDataset dApplied){
			ourSigs = signals;
			ourEID = eidValue;
			ourDataset = dApplied;
		}
		
		protected Set<IRI> getSignals(){
			return ourSigs;
		}
		
		protected double getEID(){
			return ourEID;
		}
		
		protected CorrelatedViewLabelDataset getDataset(){
			return ourDataset;
		}
		
		public String toString(){
			StringBuilder sb = new StringBuilder();
			sb.append("EID value " + getEID() + " for signal set:");
			for (IRI signal : getSignals()){
				sb.append("\t" + signal.getFragment() + "\n");
			}
			return sb.toString();
		}
		
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
	 * @throws KIDSUnEvaluableSignalException 
	 */
	private RecursiveResult testSignalSet_iter(KIDSMeasurementOracle kmo, Map<Set<IRI>, RecursiveResult> triedValues, 
			Set<IRI> sigsToEval, CorrelatedViewLabelDataset cvd) throws KIDSOntologyDatatypeValuesException, KIDSOntologyObjectValuesException, InstantiationException, IllegalAccessException, ClassNotFoundException, IOException, KIDSIncompatibleSyntaxException {
		RecursiveResult toReturn = null;
		Set<IRI> key = null;
		//Set<IRI> sigsToEval = sigEvalMap.keySet();
		
		System.out.print("Testing signal set: ");
		for (IRI myI : sigsToEval){
			System.out.print(myI.getFragment() + ",");
		}
		System.out.println(" ");
		
		// If the list contains only one element, evaluate it and return the result.
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
					eidVal =  KIDSEIDMeasure.getKIDSEIDMeasureValue(kmo, sigsToEval, cvd);
					if (eidVal > maxValue){
						maxValue = eidVal;
					}
				} catch (KIDSUnEvaluableSignalException e) {
					System.err.println(e.getMessage());
					e.printStackTrace();
					return null;
				}
			toReturn = new RecursiveResult(sigsToEval,maxValue,cvd);
			triedValues.put(key, toReturn);
			//System.out.println(toReturn);
		} else {
			// If we have more than one element, remove each in turn and evaluate the rest of them, keeping track of the maximum value
			double maxEID = 0;
			try {
				if (triedValues.containsKey(sigsToEval)){
					maxEID = triedValues.get(sigsToEval).getEID(); 
				} else {
					// Try with each feasible dataset, and use the maximum:
					double maxValue = 0.0;
					double eidVal = 0;
					//TODO: Create Correlated View Dataset - matching given signals
					eidVal = KIDSEIDMeasure.getKIDSEIDMeasureValue(kmo, sigsToEval, cvd);
					if (eidVal > maxEID){
						maxEID = eidVal;
					}
					//maxEID = KIDSEIDMeasure.getKIDSEIDMeasureValue(kmo, sigsToEval, cvd);
				}
			} catch (KIDSUnEvaluableSignalException e){
				// Couldn't evaluate:
				maxEID = -1;
				System.err.println("Warning: " + e.getMessage());
			}
			toReturn = new RecursiveResult(sigsToEval, maxEID, cvd);
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
	 */
	public void testSignalSet(String ABOXFile, 
							  String ABOXIRI,
							  String TBOXFile,
							  String TBOXIRI,
							  String EventIRI,
							  String DatasetIRI,
							  String TimePeriodIRI){
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
		
				// Assess all subsets of available signals, recording the EID values for each:
				Map<Set<IRI>, RecursiveResult> triedValues = new HashMap<Set<IRI>, RecursiveResult>();
				RecursiveResult rr = this.testSignalSet_iter(myGuy, triedValues, signals,ourDS);
			
				System.out.println("Maximum signal set (EID = " + rr.getEID() + "):");
					for (IRI signalC : rr.ourSigs){
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
	 * @param args - one arg is the libpcap file to parse
	 * run the tests
	 */
	public static void main(String[] args) {
		String usage = "Usage: KIDSSignalSelection <pathToConfigFile>";
		if (args.length != 1){
			System.err.println(usage);
			java.lang.System.exit(1);
		}
		
		boolean cerr = false;
		Properties p = new Properties ();
		try {
			p.load(new FileReader(new File(args[0])));
			HashMap<String,String> cVals = new HashMap<String,String>();
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
				cVals.put((String)kstring, (String)p.getProperty((String)kstring));
			}
			
			if (cerr){
				System.exit(1);
			}
			
			KIDSSignalSelectionInterface kss = new KIDSSignalSelectionInterface();
			//TODO: Populate ABOX File
			kss.testSignalSet(cVals.get("ABoxFile"), 
							  cVals.get("ABoxIRI"), 
							  cVals.get("TBoxFile"), 
							  cVals.get("TBoxIRI"), 
							  cVals.get("EventIRI"), 
							  cVals.get("DatasetIRI"),
							  cVals.get("TimePeriodIRI"));
			
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
	}

}
