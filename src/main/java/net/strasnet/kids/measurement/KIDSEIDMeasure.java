package net.strasnet.kids.measurement;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.strasnet.kids.KIDSOntologyDatatypeValuesException;
import net.strasnet.kids.KIDSOntologyObjectValuesException;
import net.strasnet.kids.detectorsyntaxproducers.KIDSIncompatibleSyntaxException;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLNamedIndividual;

/**
 * Implementation of the Effectiveness of Intrusion Detection (E_ID) measure for the KIDS model of IDS/IRS.
 * This differs from the C_ID model in the way that event-associated instances are counted.  In the C_ID model, each
 * instance is tagged either malicious or benign.  Any malicious instance can produce a False Negative (Type II error)
 * or a True Positive, while any benign instance can produce a False Positive (Type I error) or a True Negative.  
 * 
 * Using the E_ID model, instances are associated with events, or (by default) benign activity.  When computing the 
 * entropy of the data set (or resulting sub-data sets after filtering based on signals), the following are true:
 * - Any benign instance can produce either a False Positive or a True Negative.
 * - An instance associated with an event for which <b>NO</b> other instance is detected can produce either a true
 *   positive or a false negative.
 * - An instance associated with an event for which <b>ANY</b> other instance is detected is always considered a 
 *   true positive, even if the instance itself is not matched by the signal.
 *   
 * The effect of this modification is that a false positive is only counted for an event if the event is not detected
 * at all by the set of signals under consideration.  If any signal matches any data instance associated with the event,
 * the event is considered "detected" and all data instances are "pulled" in as true positives.
 * 
 * @author chrisstrasburg
 *
 */
public class KIDSEIDMeasure {

	/**
	 * The E_ID measure is computed as follows:
	 *   - The total number of instances: Inum
	 *   - The number of instances associated with each event E_i: Enum[i]
	 *   - The total number of benign instances: IBnum = Inum - sum(Enum)
	 *   - The number of benign instances which are matched by s: SBInum
	 *   - The number of benign instances which are not matched by s: NBInum = Inum - SBInum
	 *   - The adjusted number of event instances matched by the signal: SEnum[i] = Ematched[i] ? 
	 *   
	 * From these values we produce estimates of:
	 *   - The probability that a randomly chosen instance is benign: pBI = IBnum / Inum
	 *   - The probability that a randomly chosen instance is associated with an event: pEI = 1 - pBI
	 *   - The probability that a randomly chosen instance is detected by the signal: pDI = (SBInum + sum(Ematched[i] ? Enum[i] : 0)) / Inum
	 *   - The probability that a randomly chosen instance is not detected by the signal: pNI = 1 - pDI
	 * 
	 * Then entropy (H() ), information gain (IG() ) are produced as usual, and E_ID ~= C_ID (is computed identically)
	 *   
	 * @param d - The dataset on which to evaluate the E_ID measure.
	 * TODO: I *think*, with a correlation dataset abstract interface, we no longer need the map for 's'; just the set of
	 * signals to include.
	 * @param s - The signal set (mapped to appropriate datasets) which will be used to filter data.
	 * @return The E_ID value for this dataset.
	 * @throws IOException 
	 * @throws ClassNotFoundException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 * @throws KIDSOntologyObjectValuesException 
	 * @throws KIDSOntologyDatatypeValuesException 
	 * @throws KIDSIncompatibleSyntaxException 
	 * @throws KIDSUnEvaluableSignalException 
	 */
	public static double getKIDSEIDMeasureValue(KIDSMeasurementOracle kmo, Set<IRI> s, Dataset d) throws KIDSOntologyDatatypeValuesException, KIDSOntologyObjectValuesException, InstantiationException, IllegalAccessException, ClassNotFoundException, IOException, KIDSIncompatibleSyntaxException, KIDSUnEvaluableSignalException{
		double E_ID = 0;
		int Inum = 0;                // The total number of instances (bags) (instances + # events - event related instances)
		int[] EInumAry = null;          // The number of instances associated with each event E_i
		int EInum = 0;               // The total number of event bags (# events)
		int BInum = 0;               // The total number of benign instances
		int[] SEInumAry = null;		 // The count of positive instances, by event, matched by s
		int SEInum = 0;				 // The number of event instances matched by s
		int SBInum = 0;				 // The number of benign instances matched by s
		int NBInum = 0;				 // The number of benign instances *not* matched by s
		int SInum = 0;				 // The number of instances matched (detected) by s
		int i = 0;					 // Just a counter ;)
		
		double pBI = 0;				 // The probability of an instance being benign
		double pEI = 0;			     // The probability of an instance being associated with some event
		double pSI = 0;			     // The probability of an instance being detected (or associated with an event
								 // which is detected.
		double pNI = 0;				 // The probability of an instance not being detected
		
		double H0 = 0; 				 // The entropy of the original dataset
		double HS = 0; 			     // The entropy of the dataset matching Signal s
		double HN = 0; 			     // The entropy of the dataset not matching Signal s
		
		double IGS = 0; 			 // The information gain after applying signal s
		
		// First, build a unique set of datasets to consider:
		//HashSet<Dataset> dsets = new HashSet<Dataset>();
		//dsets.addAll(s.values());
		
		// Estimate probabilities:
		// DONE: Do we need to build an abstract "correlated interface" here? - Yes, we do; call from the DatasetFactory?
		// First, build the 'perfectly correlated' dataset.  We will end up with one instance for each event,
		//  and one instance for each uncorrelated instance.
		
		/*** Denominators - perfectly correlated values ***/
		//TODO: Add getComponentDatasets() ; should we just always assume / use a 'correlated' data set, with a null-correlation?  Yes.
		Set<Dataset> dsets = d.getComponentDatasets();

		for (Dataset ds : dsets){
			Inum += ds.numInstances();
			int[] tmpAry = ds.numPositiveInstances();  // Event Instance counts
			
			// Initialize the EInumAry, if necessary, to track the number of instances associated with each event.
			if (EInumAry == null){
				EInumAry = new int[tmpAry.length];
			}
			// Remove instances in the same event-related "bag", keeping track of them in EInumAry, the array of bags
			for (i = 0; i < tmpAry.length; i++){
				Inum -= tmpAry[i];
				EInumAry[i] += tmpAry[i];
			}
			// Add back in the number of events, to represent the bags
			Inum += EInumAry.length;
		
		}
		EInum = EInumAry.length; // Number of event bags
		
		BInum = Inum - EInum; // Benign instances (non-event related)
		
		/*** Numerators - Based on IDS correlated data ***/
		// To get the overall performance of a set of signals, we need to determine:
		// 1) How many total false positives there are;
		// 2) How many of the events have a data instance detected
		//
		// To get 1, we must incorporate the correlation function used to group the alerts/instances from different data sets.
		// The DatasetView class implements a method to obtain related alerts given a DataInstance and correlation function individual.
		// If we either only have one dataset to worry about, or there is no compatible correlation function, we can skip this step.
		//
		// To get 2 we also need to incorporate the correlation function, finding cases where the function would constitute a 
		//  match.  For example, if the function is an 'and', then the bag of instances must match all signals.  If it is an
		//  'or', then the bag of instances only needs to include one signal.  The dataset view will implement this.
		// 
		// Get counts from filtered data set:
		Dataset dTemp = null;
		
		// Determine if correlation is required:
		// Moving to a correlated dataset interface instead.  This block should no longer be needed.
		/* Once working, DELETE
		if (dsets.size() > 1){
			// Is there a correlation function that can be used for this set of datasets?
			Set<CorrelationFunction> cfs = kmo.getCompatibleCorrelationFunctions(dsets);
			if (cfs.size() == 0){
				// Cannot perform correlation; just a huge bag of instances
			}
		}
		*/
		
		try {
			dTemp = d.getDataSubset(d.getMatchingInstances(s));
		} catch (net.strasnet.kids.measurement.KIDSUnEvaluableSignalException e){
			System.err.print("Could not evaluate signal set {");
			StringBuilder sb = new StringBuilder();
			for (IRI sigiri : s){
				System.err.print(s.toString());
			}
			return 0; // Assume no benefit to the signal if we cannot evaluate it
		}
		
		SInum = dTemp.numInstances(); // # of instances matching s, counting event-related instances once for each event
		SBInum = 0;					// # of correlated benign instances matching s
		SEInum = 0;					// # of correlated malicious instances matching s
		SEInumAry = dTemp.numPositiveInstances();
		if (SEInumAry.length != dTemp.numEventOccurrences()){
			System.err.println("Warning: Positive instance array length (" + SEInumAry.length + ") != numEventOccurrences (" + dTemp.numEventOccurrences() + ")");
		}
		for (i = 0; i < SEInumAry.length; i++){
			if (SEInumAry[i] == 0){
				continue;
			
			if (EInumAry[i] > 0){
			    SEInum += 1; // NOTE: This accounts for a single event being detected, regardless of number of instances.
			    EInumAry[i] = 0;
			}

			/*  DELETE - after working
			if (EInumAry[i] > SEInumAry[i]){  
				SInum += (EInumAry[i] - SEInumAry[i]); // To make sure that all accounted-for instances are... accounted for :)
			}
			if (EInumAry[i] == 0){
				// If we've already accounted for this event, remove it from SInum:
				SInum -= SEInumAry[i];
			}
			EInumAry[i] = 0; // Ensure we don't double-count positives
			*/
		}
		SBInum = SInum - SEInum;
		NBInum = BInum - SBInum;
		
		// Compute Entropies:
		if (Inum > 0){
		    pBI = (double)BInum / (double)Inum;  // p(benign) ~= # Benign / #
    		pSI = (double)SInum / (double)Inum; // p(benign|signal) != # Benign | signal / #
		} else {
			pBI = 0;
			pSI = 0;
		}
		pEI = 1 - pBI;
		pNI = 1 - pSI;
		
		H0 = computeEntropy(BInum, EInum);
		HS = computeEntropy(SBInum, SEInum);
		HN = computeEntropy(BInum - SBInum, EInum - SEInum);
		
		IGS = H0 - (pSI*HS + pNI*HN);
		
		// Return E_ID:
		if (H0 > 0){
		    E_ID = IGS / H0;
		} else {
			E_ID = 0;
		}
		return E_ID;
	}
	
	/**
	 * Given a 2-class count of instances, return the entropy as a double.  If either class count
	 * is '0', entropy is '0'.
	 * @param c1 - The count of instances of class 1
	 * @param c2 - The count of instances of class 2
	 * @return The entropy H(c) = - (p(c1)*log_2(p(c1)) + p(c2)*log_2(p(c2)))
	 */
	private static double computeEntropy(int c1, int c2){
		if (c1 == 0 || c2 == 0){
			return 0;
		}
		
		int cTot = c1 + c2;
		double pc1 = (double)c1 / cTot;
		double pc2 = (double)c2 / cTot;
		double Hc = -(pc1 * Math.log(pc1) / Math.log(2) + pc2 * Math.log(pc2) / Math.log(2));
		return Hc;
	}
	
	/**
	 * The C_ID measure is computed as follows:
	 *   - The total number of instances: Inum
	 *   - The number of instances associated with each event E_i: Enum[i]
	 *   - The total number of benign instances: IBnum = Inum - sum(Enum)
	 *   - The number of benign instances which are matched by s: SBInum
	 *   - The number of benign instances which are not matched by s: NBInum = Inum - SBInum
	 *   - The number of event instances matched by the signal: SEnum[i]
	 *   
	 * From these values we produce estimates of:
	 *   - The probability that a randomly chosen instance is benign: pBI = IBnum / Inum
	 *   - The probability that a randomly chosen instance is associated with an event: pEI = 1 - pBI
	 *   - The probability that a randomly chosen instance is detected by the signal: pDI = (SBInum + sum(Ematched[i] ? Enum[i] : 0)) / Inum
	 *   - The probability that a randomly chosen instance is not detected by the signal: pNI = 1 - pDI
	 * 
	 * Then entropy (H() ), information gain (IG() ) are produced as usual, and 
	 * C_ID (is computed identically)
	 *   
	 * @param d - The dataset on which to evaluate the E_ID measure.
	 * @param sigSet2 - The signal which will be used to filter data.
	 * @return The C_ID value for this dataset.
	 * @throws IOException 
	 * @throws ClassNotFoundException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 * @throws KIDSOntologyObjectValuesException 
	 * @throws KIDSOntologyDatatypeValuesException 
	 * @throws KIDSIncompatibleSyntaxException 
	 * @throws KIDSUnEvaluableSignalException 
	 */
	public static double getKIDSCIDMeasureValue(Dataset d, Set<IRI> sigSet) throws KIDSOntologyDatatypeValuesException, KIDSOntologyObjectValuesException, InstantiationException, IllegalAccessException, ClassNotFoundException, IOException, KIDSIncompatibleSyntaxException, KIDSUnEvaluableSignalException{
		double C_ID = 0;
		int Inum;                // The total number of instances
		int[] EInumAry;          // The number of instances associated with each event E_i
		int EInum;               // The total number of event instances
		int BInum;               // The total number of benign instances
		int[] SEInumAry;		 // The count of positive instances, by event, matched by s
		int SEInum;				 // The number of event instances matched by s
		int SBInum;				 // The number of benign instances matched by s
		int NBInum;				 // The number of benign instances *not* matched by s
		int SInum;				 // The number of instances matched (detected) by s
		int i;					 // Just a counter ;)
		
		double pBI;				 // The probability of an instance being benign
		double pEI;			     // The probability of an instance being associated with some event
		double pSI;			     // The probability of an instance being detected (or associated with an event
								 // which is detected.
		double pNI;				 // The probability of an instance not being detected
		
		double H0; 				 // The entropy of the original dataset
		double HS; 			     // The entropy of the dataset matching Signal s
		double HN; 			     // The entropy of the dataset not matching Signal s
		
		double IGS; 			 // The information gain after applying signal s
		
		// Estimate probabilities:
		Inum = d.numInstances();
		EInumAry = d.numPositiveInstances();  // Event Instance counts
		EInum = 0; // Total Event Instances
		for (i = 0; i < d.numEventOccurrences(); i++){
			EInum += EInumAry[i];
		}
		BInum = Inum - EInum; // Benign instances (non-event related)
		
		// Get counts from filtered data set:
		Dataset dTemp = d.getDataSubset(d.getMatchingInstances(sigSet));
		SInum = dTemp.numInstances(); // # of instances matching s
		SBInum = 0;					// # of benign instances matching s
		SEInum = 0;					// # of malicious instances matching s
		SEInumAry = dTemp.numPositiveInstances();
		for (i = 0; i < dTemp.numEventOccurrences(); i++){
			if (SEInumAry[i] == 0){
				continue;
			}
			SEInum += SEInumAry[i]; // NOTE: This is *original* event instances; 
								    // other counts must be adjusted accordingly
		}
		SBInum = SInum - SEInum;
		NBInum = BInum - SBInum;
		
		// Compute Entropies:
		pBI = (double)BInum / (double)Inum;  // p(benign) ~= # Benign / #
		pEI = 1 - pBI;
		pSI = (double)SInum / (double)Inum; // p(benign|signal) != # Benign | signal / #
		pNI = 1 - pSI;
		
		H0 = computeEntropy(BInum, EInum);
		HS = computeEntropy(SBInum, SEInum);
		HN = computeEntropy(BInum - SBInum, EInum - SEInum);
		
		IGS = H0 - (pSI*HS + pNI*HN);
		
		// Return E_ID:
		C_ID = IGS / H0;
		return C_ID;
	}
}
