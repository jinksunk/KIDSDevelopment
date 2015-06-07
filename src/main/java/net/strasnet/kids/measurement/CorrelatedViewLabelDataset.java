/**
 * 
 */
package net.strasnet.kids.measurement;

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

import org.apache.logging.log4j.LogManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.util.SimpleIRIMapper;

import net.strasnet.kids.KIDSOntologyDatatypeValuesException;
import net.strasnet.kids.KIDSOntologyObjectValuesException;
import net.strasnet.kids.detectors.UnimplementedIdentifyingFeatureException;
import net.strasnet.kids.detectorsyntaxproducers.KIDSIncompatibleSyntaxException;
import net.strasnet.kids.measurement.correlationfunctions.IncompatibleCorrelationValueException;
import net.strasnet.kids.measurement.datasetlabels.DatasetLabel;

/**
 * @author cstras
 *
 * Represents a set of ViewLabelDatasets correlated by a correlation function.
 * 
 */
public class CorrelatedViewLabelDataset {
	
	public static final org.apache.logging.log4j.Logger logme = LogManager.getLogger(CorrelatedViewLabelDataset.class.getName());
	private Map<Dataset, DatasetLabel> constituentDatasets;
	private CorrelationFunction ourCombiner;
	private Set<CorrelationDataInstance> ourDataInstances;
	private Set<Set<CorrelationDataInstance>> dataSubsetCache;
	private int maxEventID = 0;
	
	/**
	 * Iterates over all base instances of all dataset / dataset label pairs, and creates correlated instances
	 * according to the provided combination function.
	 * 
	 * Sets labels on the individual data instances.
	 * 
	 * @param combinationFunction
	 * @param members
	 * @throws IOException
	 * @throws IncompatibleCorrelationValueException
	 * @throws KIDSUnEvaluableSignalException
	 * @throws KIDSOntologyObjectValuesException
	 * @throws KIDSOntologyDatatypeValuesException
	 * @throws KIDSIncompatibleSyntaxException
	 * @throws UnimplementedIdentifyingFeatureException 
	 */
	public CorrelatedViewLabelDataset(CorrelationFunction combinationFunction, HashMap<Dataset, DatasetLabel> members) throws IOException, IncompatibleCorrelationValueException, KIDSUnEvaluableSignalException, KIDSOntologyObjectValuesException, KIDSOntologyDatatypeValuesException, KIDSIncompatibleSyntaxException, UnimplementedIdentifyingFeatureException{
		constituentDatasets = new HashMap<Dataset, DatasetLabel>();
		dataSubsetCache = new HashSet<Set<CorrelationDataInstance>>();
		ourDataInstances = new HashSet<CorrelationDataInstance>();
		for (Dataset d : members.keySet()){
			logme.debug("Adding dataset " + d.getIRI());
			constituentDatasets.put(d, members.get(d));
		}
		ourCombiner = combinationFunction;
		
		// Build the correlated data instance set.  For each instance in each dataset, compare it with
		// all others in all other datasets to see if it is correlated.  Combine each set of correlated
		// instances into a single CorrelationDataInstance object and store it.
		
		Set<DataInstance> allInstances = new HashSet<DataInstance>();
		for (Dataset d : constituentDatasets.keySet()){
			DatasetLabel myL = constituentDatasets.get(d);
			Iterator<DataInstance> dIter = d.getIterator();
			while (dIter.hasNext()){
				DataInstance di = dIter.next();
				if (di.getLabel() == null){
					di.setLabel(myL.getLabel(di));
				}
				allInstances.add(di);
			}
		}

		ourDataInstances = ourCombiner.generateCorrelatedDataSet(allInstances);
	}
	
	/**
	 * Intended as a copy constructor, this constructor instantiates itself with the provided set of correlation data instances, bypassing the instance
	 * construction process.
	 * @param instanceSet - Set of instances to include in the copy
	 * @param cf - The correlated function used to correlated the given instances
	 * @param dsets - The datasets serving as a base for the given instances
	 */
	public CorrelatedViewLabelDataset(Set<CorrelationDataInstance> instanceSet,
			CorrelationFunction cf, 
			Map<Dataset, DatasetLabel> dsets){
		this.ourCombiner = cf;
		this.ourDataInstances = instanceSet;
		constituentDatasets = new HashMap<Dataset, DatasetLabel>();
		for (Dataset d : dsets.keySet()){
			logme.debug("Adding dataset " + d.getIRI());
			constituentDatasets.put(d, dsets.get(d));
		}
	}
	
	/**
	 * 
	 * @param newSet - The set of correlated data instances to include in the newly created
	 * data set
	 * @return A correlated view label dataset with the same correlation function as this one, but only
	 * the given subset of correlated data instances.
	 */
	public CorrelatedViewLabelDataset getDataSubset(Set<CorrelationDataInstance> newSet){
		// Check to see if a subset has already been created with this instance set.  If so, zero this instance set
		// and just return the existing one.
		// TODO In order to actually cache this, we need correlated data instances that override the equality, comparison, and hash value
		//      methods.
		CorrelatedViewLabelDataset toReturn = new CorrelatedViewLabelDataset(newSet,this.ourCombiner, this.constituentDatasets);
		toReturn.maxEventID = this.maxEventID;
		return toReturn;
	}
	
	/**
	 * NOTE: this works with *base* instances, not the correlated data instances
	 * @return - The number of data instances, minus the number of event-related instances, plus the number of events.
	 */
	public int numInstances(){
		// logme.debug("CorrelatedViewLabelDataset.numInstances:");
		// First, just get a count of all the instances.
		int totalInstances = 0;
		Iterator<CorrelationDataInstance> cii = this.ourDataInstances.iterator();
		while (cii.hasNext()){
			CorrelationDataInstance ci = cii.next();
			//logme.debug(" + CDI " + ci.hashCode() + " with " + ci.getInstances().size() + " instances...");
			Set<DataInstance> instanceSet = ci.getInstances();
			totalInstances += instanceSet.size();
		}
		// We now have a count of all instances

		// Next, remove all the event related instances.
		int[] eventInstanceCounts = numPositiveInstances(); // We need to determine when there is a positive instance here
		//int numEvents = eventInstanceCounts.length - 1; // element '0' is normal -not an event
		int tEvents = 0;
		for (int i = 1; i < eventInstanceCounts.length; i++){   // element '0' is normal instances
			// logme.debug(" - eventInstances " + eventInstanceCounts[i]);
			if (eventInstanceCounts[i] > 0){
				totalInstances -= eventInstanceCounts[i];
				tEvents++;
			}
		}
		// Finally, add back in the number of events.
		// logme.debug(" + eventCounts " + numEvents);
		totalInstances += tEvents; // No - we just add back in the number of non-0 ones!
		return totalInstances;
	}

	/**
	 * @return - An array, indexed by event ID, giving the number of positive instances for each event ID in
	 * this data set.
	 * NOTE: This returns positive *base* instances, not positive correlated data instances.
	 */
	public int[] numPositiveInstances(){
		// Iterate through the dataset, keeping track of both the number of instances per event ID, and the largest eventID seen
		int maxEventID = 0;
		HashMap<Integer,Integer> eid2count = new HashMap<Integer,Integer>();
		
		Iterator<CorrelationDataInstance> cii = this.ourDataInstances.iterator();
		while (cii.hasNext()){
			CorrelationDataInstance ci = cii.next();
			Map<Integer, Set<DataInstance>> eicount = ci.getEventInstances();
			for (Integer eInstance : eicount.keySet()){
				if (!eid2count.containsKey(eInstance)){
					eid2count.put(eInstance, 0);
				}
				eid2count.put(eInstance, eid2count.get(eInstance) + eicount.get(eInstance).size());
				if (eInstance > maxEventID){
					maxEventID = eInstance;
				}
			}
		}
		
		// Now, create the array:
		int[] returnArray = new int[maxEventID+1];
		for (int i = 1; i <= maxEventID; i++){
			returnArray[i] = 0;
		}
		for (Integer eventID : eid2count.keySet()){
			returnArray[eventID] = eid2count.get(eventID);
		}
		return returnArray;
	}

	/**
	 * Given a set of signals, return the subset of CorrelationDataInstances (CDIs) which match that set of signals.
	 * For a CDI to match, the following must be true:
	 * - Each signal in the signal set must match at least one base instances in the CDI
	 * 
	 * @param signalSet - The set of signals to use (as a conjunction) to filter out instances.
	 * @return The set of CorrelationDataInstances which match the conjunction of signals passed in (signalSet)
	 * @throws KIDSIncompatibleSyntaxException 
	 * @throws IOException 
	 * @throws KIDSOntologyDatatypeValuesException 
	 * @throws KIDSOntologyObjectValuesException 
	 * @throws KIDSUnEvaluableSignalException 
	 * @throws UnimplementedIdentifyingFeatureException 
	 */
	Set<CorrelationDataInstance> getMatchingInstances(Set<IRI> signalSet) throws 
		KIDSOntologyObjectValuesException, 
		KIDSOntologyDatatypeValuesException, 
		IOException, 
		KIDSIncompatibleSyntaxException, 
		KIDSUnEvaluableSignalException, UnimplementedIdentifyingFeatureException{
		return this.getMatchingInstances(signalSet, false);
	}

	/**
	 * Given a set of signals, return the subset of CorrelationDataInstances (CDIs) which match that set of signals.
	 * For a CDI to match, the following must be true:
	 * - Each signal in the signal set must match at least one base instances in the CDI
	 * 
	 * @param signalSet - The set of signals used to filter out matching instances
	 * @param debugOutput - Whether to enable debug output for this operation
	 * @return
	 * @throws UnimplementedIdentifyingFeatureException 
	 */
	public Set<CorrelationDataInstance> getMatchingInstances(Set<IRI> signalSet,
			boolean debugOutput) throws
		KIDSOntologyObjectValuesException, 
		KIDSOntologyDatatypeValuesException, 
		IOException, 
		KIDSIncompatibleSyntaxException, 
		KIDSUnEvaluableSignalException, UnimplementedIdentifyingFeatureException {
		Set<CorrelationDataInstance> returnDataSet = new HashSet<CorrelationDataInstance>();

		Set<IRI> usedSignals = new HashSet<IRI>(); // At the end of the method, if we have not used
												   // all the signals, fail.
		
		HashMap<String, Set<IRI>> matchingDISet = new HashMap<String, Set<IRI>>();

		// For each dataset in our constituent datasets, get the set of matching instance IDs 
		// for the subset of the given signals which can be evaluated on that dataset.
		for (Dataset d : this.constituentDatasets.keySet()){
			// Get the subset of signals which can be applied to this dataset
			Set<IRI> appSignals = d.getKnownApplicableSignals();
			
			Set<IRI> applicableSignalSubset = new HashSet<IRI>();
			for (IRI sIRI : appSignals){
				if (signalSet.contains(sIRI)){
					applicableSignalSubset.add(sIRI);
					usedSignals.add(sIRI);
				}
			}
			
			// Get the set of matching instances from dataset d for this set of signals.  If the set of signals is empty, just
			// use the empty set.  Just IDs should be sufficient.
			Set<DataInstance> matchingInstances = d.getMatchingInstances(applicableSignalSubset);

			// Don't include null if it wasn't specified in the original set of signals.
			if (applicableSignalSubset.size() == 1 &&
				applicableSignalSubset.contains(null) &&
				signalSet.size() != 0 &&
				! signalSet.contains(null)){
				matchingInstances = new HashSet<DataInstance>();
			}
			
			// What we really want to know is which signals from the original signal set are covered by a given CDI
			// Key points:
			// 1) Signals are evaluated as a conjunction in a single dataset, so we only need to track the set of signals that is
			//    included in matches from that dataset.
			// 2) We only reject the CDI if there exists a non-matching signal within that CDI
			for (DataInstance mDI : matchingInstances){
			    matchingDISet.put(mDI.getID(), applicableSignalSubset);  // What if we can't match all the signals?
			}
			
		}
		// If we can't process all the signals, then none of the CDIs can match, so we may as well bail:
		if (usedSignals.size() != signalSet.size()){
			System.err.println("[W]: (CorrelatedViewDataset.getMatchingInstances) Not all signals could be applied, returning empty set.");
			System.err.println("   : Only the following signals could be used: ");
			for (IRI t : usedSignals){
				System.err.println("   : - " + t.toString());
			}
			return returnDataSet;
		}

		
		// Iterate over all correlated data instances in this data set
		for (CorrelationDataInstance cdi : this.ourDataInstances){
			// For each CDI, determine if it contains one or more instances which matches each signal in the signal set.
			// If the atomic data instance does not support the signal, treat it as a 'miss'. 
			Set<DataInstance> componentInstances = cdi.getInstances();
			Set<IRI> matchedSignals = new HashSet<IRI>();
			for (DataInstance di : componentInstances){
				// Lookup data instance in hashmap, and count off all the signals it matched from the set we're considering.
				if (matchingDISet.containsKey(di.getID())){
					matchedSignals.addAll(matchingDISet.get(di.getID()));
				}

			}
			// If the CDI is covered, the whole thing matches - add it to the return data set.
			boolean allMatched = true;
			for (IRI signal : signalSet){
				if (!matchedSignals.contains(signal )){
					allMatched = false;
				}
			}
			
			if (allMatched){
				returnDataSet.add(cdi);
				logme.debug("Matched CDI ["+ cdi + "]");
			} else {
				logme.debug("Failed match for CDI containing instance [" + cdi.getInstances().iterator().next().getID() + "]");
			}
		}
		
		return returnDataSet;
	}

	/**
	 * Assumption: all contained datasets share the same view of the number of event occurrences.  This should always be true.
	 * @return - The number of event occurrences represented by this correlated data set.
	 */
	public int numEventOccurrences() {
		if (this.constituentDatasets.size() > 0){
			Dataset d = this.constituentDatasets.keySet().iterator().next();
			return d.numEventOccurrences();
		} else {
			return 0;
		}
	}
	
	public static void main (String[] args) throws KIDSOntologyDatatypeValuesException, KIDSOntologyObjectValuesException, NumberFormatException, InstantiationException, IllegalAccessException, ClassNotFoundException, IOException, IncompatibleCorrelationValueException, KIDSUnEvaluableSignalException{
		//String testPropFile = "file:///Users/cstras/Documents/academic-research/papers/2013-MeasurementPaper/experiments/CorrelatedDataSetTest1/fullExperiment2.prop";
		// Read in the properties file
		boolean cerr = false;
		HashMap<String,String> configFileValues = new HashMap<String,String>();
		
			configFileValues.put("ABoxFile", "/dev/null");
			configFileValues.put("ABoxIRI", "/dev/null");
			configFileValues.put("TBoxFile", "/dev/null");
			configFileValues.put("TBoxIRI", "/dev/null");
			configFileValues.put("EventIRI", "/dev/null");
			configFileValues.put("TimePeriodIRI", "/dev/null");
		
		Properties p = new Properties ();
		HashMap<String,String> cVals = new HashMap<String,String>();
		try {
			p.load(new FileReader(new File(args[0])));
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
			
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		// Get all datasets for the time period and event
		  // Create the oracle
		KIDSMeasurementOracle myGuy = null;
		try {
			myGuy = new KIDSMeasurementOracle();
            List<SimpleIRIMapper> m = new LinkedList<SimpleIRIMapper>();
            m.add(new SimpleIRIMapper(IRI.create(cVals.get("ABoxIRI")), 
            						  IRI.create(cVals.get("ABoxFile"))));
            m.add(new SimpleIRIMapper(IRI.create(cVals.get("TBoxIRI")), 
            						  IRI.create(cVals.get("TBoxFile"))));
			myGuy.loadKIDS(IRI.create(cVals.get("ABoxIRI")), m);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		} catch (Exception e){
			System.err.println(e);
			e.printStackTrace();
			System.exit(1);
		}
		
	}

	/**
	 * 
	 * @return - The number of correlated data instances in this data set
	 */
	public int numCorrelatedInstances() {
		return this.ourDataInstances.size();
	}

	/**
	 * 
	 * @return - An array, one element for each event ID, with the count of correlated
	 *           data instances associated with that event.
	 *           
	 *           TODO: Keep track of the number of events in a different way here - we should
	 *           use the static class variables for this.
	 */
	public int[] numPositiveCorrelatedInstances() {
		// Iterate through the dataset, keeping track of both the number of instances per event ID, and the largest eventID seen
		HashMap<Integer,Integer> eid2count = new HashMap<Integer,Integer>();
		
		Iterator<CorrelationDataInstance> cii = this.ourDataInstances.iterator();
		while (cii.hasNext()){
			CorrelationDataInstance ci = cii.next();
			Map<Integer, Set<DataInstance>> eicount = ci.getEventInstances();
			for (Integer eInstance : eicount.keySet()){
				if (!eid2count.containsKey(eInstance)){
					eid2count.put(eInstance, 0);
				}
				eid2count.put(eInstance, 1);
				if (eInstance > maxEventID){
					maxEventID = eInstance;
				}
			}
		}
		
		// Now, create the array:
		/** TODO: Assuming that the event IDs are a contiguous set of integers. */
		int[] returnArray = new int[maxEventID+1];
		for (int i = 1; i <= maxEventID; i++){
			returnArray[i] = 0;
		}
		for (Integer eventID : eid2count.keySet()){
			returnArray[eventID] = eid2count.get(eventID);
		}
		return returnArray;
	}

	/**
	 * Return a string summarizing the characteristics of this data set.
	 */
	public String toString(){
		StringBuilder sb = new StringBuilder();
		// First, report the number of instances in the dataset:
		sb.append(String.format("Raw Instances: %d",this.numInstances()));
		sb.append(String.format("Correlated Instances %d",this.numCorrelatedInstances())); 
		sb.append(String.format(", Num Event Occurrences: %d",this.numEventOccurrences())); 
		int totalPositives = 0;
		int[] posAry = this.numPositiveInstances();
		for (int i = 0; i < posAry.length; i++){
			totalPositives += posAry[i];
		}
		sb.append(String.format(", numPositiveInstances: %d",totalPositives)); 
		sb.append(String.format(", numPositiveCorrelatedInstances: %d",this.numPositiveCorrelatedInstances()));
		
		return sb.toString();
		
	}

}
