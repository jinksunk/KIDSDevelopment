package net.strasnet.kids.measurement;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.strasnet.kids.KIDSOntologyDatatypeValuesException;
import net.strasnet.kids.KIDSOntologyObjectValuesException;
import net.strasnet.kids.detectors.UnimplementedIdentifyingFeatureException;
import net.strasnet.kids.detectorsyntaxproducers.KIDSIncompatibleSyntaxException;
import net.strasnet.kids.measurement.test.KIDSSignalSelectionInterface;

import org.apache.log4j.LogManager;
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
	 *   - The number of instances associated with each event E_i: Enum[i]
	 *   - The total number of instance bags: Inum = IBnum - sum(Enum) + (Enum.len -1)
	 *   - The total number of benign instance bags: IBnum = Inum - sum(Enum)
	 *   - The number of benign instance bags which are matched by s: SBInum - (any benign instances in event-related CDIs)
	 *   - The number of benign instance bags which are not matched by s: NBInum = Inum - SBInum
	 *   - The adjusted number of event instances matched by the signal: SEnum[i] = all event instances in all matched CDIs for I
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
	 * @throws UnimplementedIdentifyingFeatureException 
	 */
	
	public static final org.apache.log4j.Logger logme = LogManager.getLogger(KIDSEIDMeasure.class.getName());
	
	//TODO: Rather than just return the EID value, modify to return a suite of metrics, including fpr, fnr, and list of events matched / missed
	public static RecursiveResult getKIDSEIDMeasureValue(KIDSMeasurementOracle kmo, Set<IRI> s, CorrelatedViewLabelDataset d) throws 
		KIDSOntologyDatatypeValuesException, 
		KIDSOntologyObjectValuesException, 
		InstantiationException, 
		IllegalAccessException, 
		ClassNotFoundException, 
		IOException, 
		KIDSIncompatibleSyntaxException, 
		KIDSUnEvaluableSignalException, UnimplementedIdentifyingFeatureException {

		double E_ID = 0;
		int Inum = 0;                // The total number of instances (bags) (instances + # events - event related instances)
		int[] EInumAry = null;          // The number of instances associated with each event E_i
		int EInum = 0;               // The total number of event bags (# events)
		int BInum = 0;               // The total number of benign instances
		int[] SEInumAry = null;		 // The count of positive instances, by event, matched by s
		int SEInum = 0;				 // The number of correlated event instances matched by s
		int SBInum = 0;				 // The number of benign instances matched by s
		int NBInum = 0;				 // The number of benign instances *not* matched by s
		int NEInum = 0;				 // The number of event instances not matched by s, but with related instances matched by s - discounted from NInum
		int SInum = 0;				 // The number of instances matched (detected) by s
		int i = 0;					 // Just a counter ;)
		
		double pBI = 0;				 // The probability of an instance being benign
		double pSI = 0;			     // The probability of an instance being detected (or associated with an event
								 // which is detected.
		double pNI = 0;				 // The probability of an instance not being detected
		
		double H0 = 0; 				 // The entropy of the original dataset
		double HS = 0; 			     // The entropy of the dataset matching Signal s
		double HN = 0; 			     // The entropy of the dataset not matching Signal s
		
		double IGS = 0; 			 // The information gain after applying signal s
		
		boolean fpDebug = true;
		boolean fnDebug = true;
		boolean tnDebug = true;
		boolean tpDebug = true;

		logme.debug(String.format("Evaluating EID over %d dataset instances and %d signals.", d.numCorrelatedInstances(), s.size()));
		
		RecursiveResult toReturn = new RecursiveResult();
		toReturn.setSignals(s);
		toReturn.setDataset(d);
		Map<Integer,Boolean> eventMatches = new HashMap<Integer,Boolean>();
		
		// Estimate probabilities:
		// DONE: Do we need to build an abstract "correlated interface" here? - Yes, we do; call from the DatasetFactory?
		// First, build the 'perfectly correlated' dataset.  We will end up with one instance for each event,
		//  and one instance for each uncorrelated instance.
		
		/*** Denominators - perfectly correlated values ***/
		//      In a correlated data set, numInstance returns the sum of the instances in each component
		//      dataset, minus the event related instances, plus one for each event.  This implements 'bagging'.
		// problem (?) - we want to keep the denominator consistent, so we assume that we are only using correlation functions that work over
		//               all datasets under consideration...
		Inum += d.numRawBags(); // Number of bags in the dataset
		EInumAry = d.numPositiveRawInstances();  // Event instances in the dataset; each element of the array is one 'bag'
			
		EInum = d.numEventOccurrences(); // Number of event bags; should be the same as for (i in EInumAry; i> 0){ count++; }
		
		
		for (i = 0; i < EInum; i++){
			eventMatches.put(i, false);
		}
		
		BInum = Inum - EInum; // Benign bags (instances) (non-event related)
		
		logme.debug(String.format("Bagged Instances: %d ; Event bags: %d ; Benign bags: %d", Inum, EInum, BInum));
		
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
		CorrelatedViewLabelDataset dTemp = null;  // These are S+ bags
		
		try {
			dTemp = d.getDataSubset(d.getMatchingCorrelatedInstances(s, false)); // Call without debugging
		} catch (net.strasnet.kids.measurement.KIDSUnEvaluableSignalException e){
			logme.error("Could not evaluate signal set {");
			StringBuilder sb = new StringBuilder();
			for (IRI sigiri : s){
				sb.append(sigiri.toString() + ",");
			}
			logme.error(sb);
			toReturn.setEID(0);
			toReturn.setFPR(1);
			toReturn.setFNR(1);
			toReturn.setEventStatus(eventMatches);
			return toReturn; // Assume no benefit to the signal if we cannot evaluate it
		}
		
//		SInum = dTemp.numCorrelatedInstances(); // # of instances matching s, counting event-related instances once for each event -- I think this needs to change now for bagging
		SInum = dTemp.numRawBags(); // # of instance bags matching s, counting event-related instances once for each event -- STILL NEED TO DISCOUNT FROM S-
		SBInum = 0;					// # of benign bags matching s
		SEInum = 0;					// # of malicious bags matching s

		SEInumAry = dTemp.numPositiveCorrelatedInstances(); // Correlated instances marked as 'positive'

		if ((SEInumAry.length - 1) != dTemp.numEventOccurrences()){
			logme.warn("Positive instance array length (" + SEInumAry.length + ") != numEventOccurrences (" + dTemp.numEventOccurrences() + ")");
		}

		// This is the loop where we handle tp credits and fn discounts
		for (i = 1; i < SEInumAry.length; i++){
			if (SEInumAry[i] == 0){
				logme.debug(String.format("No signals for event %d detected.", i));
				continue;
			}

		    SEInum += 1; // Track the total number of event related bags in the signal selected data subset
			
		    // Don't count the same bag more than once, even if split across multiple CDIs -- we only detect or not-detect an event
		    // After this loop, eventMatches will determine which events were identified by the signal set, and which were not.
			if (SEInumAry[i] > 0){
			    SEInumAry[i] = 0;
			    eventMatches.put(i, true);
			}
			logme.debug(String.format("Detected event %d.", i));

		}

		/**
		 * Suppose we have 10 instances, three event related, and 7 benign.  There is one event.
		 * Our correlation function groups the 2 of the event and three other instances together, and the other 
		 * 4 benign and 1 event related.
		 * 
		 * Our signal catches the first correlation group, and not the second.
		 * 
		 * Bags = 8
		 * Correlated Instances = 2
		 * SInum = 4 (bags)
		 * SEInum = 1 (bag, there is only 1 event)
		 * SBInum = 3 (SInum - SEInum -- this should be 3, number of benign raw instances)
		 * NBInum = 4
		 * NEInum = 0 (The signal gets credit for the event instance)
		 */

		SBInum = SInum - SEInum;  // Number of raw benign bags caught by the signal
		NBInum = BInum - SBInum;  // Number of raw benign bags not caught by the signal
		NEInum = EInum - SEInum;  // Number of raw event bags not caught by the signal
		
		logme.debug(String.format("Signal set identified %d total instances, with %d true positives, %d false positive.", 
				SInum, SEInum, SBInum));
		
		// Compute Entropies:
		if (Inum > 0){
		    pBI = (double)BInum / (double)Inum;  // p(benign) ~= # Benign / #
    		pSI = (double)SInum / (double)Inum; // p(benign|signal) != # Benign | signal / #
		} else {
			pBI = 0;
			pSI = 0;
		}
		pNI = 1 - pSI;
		
		H0 = computeEntropy(BInum, EInum);
		HS = computeEntropy(SBInum, SEInum); // So, SEInum needs to include all instances part of caught events, regardless of
		                                     //     whether their correlated instances were caught or not.
		HN = computeEntropy(NBInum, NEInum); // will include all benign events not caught by the signal, and all events not counted in S+
		
		IGS = H0 - (pSI*HS + pNI*HN);
		
		// Return E_ID:
		if (H0 > 0){
		    E_ID = IGS / H0;
		} else {
			E_ID = 0;
		}
		StringBuilder sb = new StringBuilder();
		for (IRI sigiri : s){
			sb.append(sigiri.toString() + ",");
		}
		logme.info("Values for signal set: " + sb.toString() + "\n\tE_ID:" + E_ID + "\n\t FP:" + SBInum + "\n\t TN:" + NBInum + "\n\t TP:" + SEInum + "\n\t FNInum:" + NEInum + "\n\t SInum:" + SInum + "\n\tBInum:" + BInum + "\n\tInum:" + Inum);
		logme.info(String.format("Starting correlated data set: %s", d));
		logme.info(String.format("Signal filtered correlated data set: %s", dTemp));
		if (SBInum > 0 && fpDebug){
			CorrelationDataInstance FPex = dTemp.getCorrelatedBenignInstance();
			if (FPex != null){
			    logme.info(String.format("Correlated FP Example: %s", FPex.toString()));
			} else {
				DataInstance FPRaw = dTemp.getRawBenignInstance();
				if (FPRaw != null){
					logme.info(String.format("Raw FP Example: %s", FPRaw.toString()));
				} else {
				    logme.error(String.format("Odd condition: SBInum == %d, but no FP Examples found...", SBInum));
				    logme.error(String.format("Dataset: %s", dTemp));
				}
			}
			fpDebug = false;
		}
		if (SEInum > 0 && tpDebug){
			CorrelationDataInstance TPex = dTemp.getEventInstance();
			if (TPex != null){
			    logme.info(String.format("TP Example: %s", TPex.toString()));
			} else {
				logme.error(String.format("Odd condition: SEInum == %d, but no TP Examples found...", SEInum));
				logme.error(String.format("Dataset: %s", dTemp));
			}
			tpDebug = false;
		}
		if (NBInum > 0 && tnDebug){
			CorrelationDataInstance TNex = d.getCorrelatedBenignInstance();
			if (TNex != null){
			    logme.info(String.format("TN Example: %s", TNex.toString()));
			} else {
				DataInstance TNRaw = dTemp.getRawBenignInstance();
				if (TNRaw != null){
					logme.info(String.format("Raw TN Example: %s", TNRaw.toString()));
				} else {
				    logme.error(String.format("Odd condition: NBInum == %d, but no TN Examples found...", NBInum));
				    logme.error(String.format("Dataset: %s", d));
				}
			}
			fnDebug = false;
		}
		if (NEInum > 0 && fnDebug){
			CorrelationDataInstance FNex = d.getEventInstance();
			if (FNex != null){
			    logme.info(String.format("TN Example: ", FNex.toString()));
			} else {
				logme.error(String.format("Odd condition: NEInum == %d, but no FN Examples found...", NEInum));
				logme.error(String.format("Dataset: %s", d));
			}
			tnDebug = false;
		}
		toReturn.setBINum(BInum);
		toReturn.setSINum(SInum);
		toReturn.setSBINum(SBInum);
		toReturn.setNBINum(NBInum);
		toReturn.setFNR(((double)(NEInum)) / EInum);
		toReturn.setFPR(((double)SBInum) / BInum);
		toReturn.setEID(E_ID);
		return toReturn;
	}
	
	/**
	 * Given a 2-class count of instances, return the entropy as a double.  If either class count
	 * is '0', entropy is '0'.
	 * @param c1 - The count of instances of class 1
	 * @param c2 - The count of instances of class 2
	 * @return The entropy H(c) = - (p(c1)*log_2(p(c1)) + p(c2)*log_2(p(c2)))
	 */
	static double computeEntropy(int c1, int c2){
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
	 * @throws UnimplementedIdentifyingFeatureException 
	 */
	public static double getKIDSCIDMeasureValue(Dataset d, Set<IRI> sigSet) throws KIDSOntologyDatatypeValuesException, KIDSOntologyObjectValuesException, InstantiationException, IllegalAccessException, ClassNotFoundException, IOException, KIDSIncompatibleSyntaxException, KIDSUnEvaluableSignalException, UnimplementedIdentifyingFeatureException{
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
