/**
 * 
 */
package net.strasnet.kids.measurement;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.semanticweb.owlapi.model.IRI;

/**
 * @author cstras
 *
 * Represents the result of a signalSet evaluation - records the signal set evaluted, the dataset it was evaluted on, and the eidValue obtained.
 * Also includes the event IDs matched / not matched, the false positive rate, and the false negative rate.
 * @author chrisstrasburg
 *
 */
public class RecursiveResult {
		private Set<IRI> ourSigs;
		private double ourEID;
		private double ourFPR;
		private double ourFNR;
		private Map<Integer,Boolean> ourEVStatus;
		private CorrelatedViewLabelDataset ourDataset;
		private int ourBINum;
		private int ourNBINum;
		private int ourSBINum;
		private int ourSINum;
		
		public RecursiveResult(){
			this.ourEVStatus = new HashMap<Integer, Boolean>();
			this.ourSigs = new HashSet<IRI>();
			this.ourEID = 0;
			this.ourFPR = 0;
			this.ourFNR = 0;
			this.ourDataset = null;
		}
		
		/**
		 * 
		 * @param signals - The set of signal IRIs evaluated
		 * @param eidValue - The eidValue produced for this signal set
		 * @param dApplied - The correlated dataset the signals were evaluated on
		 * @param falsePositiveRate - The false positive rate the signals produced
		 * @param falseNegativeRate - The false negative rate the signals produced
		 * @param eventDetectionStatus - The List of event ID -> true/false for the events this signals set detected / missed.
		 */
		public RecursiveResult (Set<IRI> signals, 
				double eidValue, 
				CorrelatedViewLabelDataset dApplied,
				double falsePositiveRate,
				double falseNegativeRate,
				Map<Integer,Boolean> eventDetectionStatus){

			//TODO: Make this its own standalone class

			ourSigs = signals;
			ourEID = eidValue;
			ourDataset = dApplied;
			ourEVStatus = eventDetectionStatus;
			ourFPR = falsePositiveRate;
			ourFNR = falseNegativeRate;
		}
		
		public Set<IRI> getSignals(){
			return ourSigs;
		}
		
		public void setSignals(Set<IRI> sigs){
			this.ourSigs = sigs;
		}
		
		public double getEID(){
			return ourEID;
		}
		
		public void setEID(double eid){
			this.ourEID = eid;
		}
		
		public double getFPR(){
			return ourFPR;
		}
		
		public void setFPR(double fpr){
			this.ourFPR = fpr;
		}

		public double getFNR(){
			return ourFNR;
		}
		
		public void setFNR(double fnr){
			this.ourFNR = fnr;
		}

		public Map<Integer,Boolean> getEventStatus(){
			return ourEVStatus;
		}
		
		public void setEventStatus(Map<Integer,Boolean> m){
			this.ourEVStatus = m;
		}
		
		public CorrelatedViewLabelDataset getDataset(){
			return ourDataset;
		}
		
		public void setDataset(CorrelatedViewLabelDataset cvld){
			this.ourDataset = cvld;
		}
		
		public String toString(){
			StringBuilder sb = new StringBuilder();
			sb.append("Result for signal set:");
			for (IRI signal : getSignals()){
				sb.append("\t" + signal.getFragment() + "\n");
			}
			sb.append(String.format("\tBI: %d\n", this.ourBINum));
			sb.append(String.format("\tSI: %d\n", this.ourSINum));
			sb.append(String.format("\tSBI: %d\n", this.ourSBINum));
			sb.append(String.format("\tNBI: %d\n", this.ourNBINum));
			sb.append(String.format("\tFPR: %f\n", this.ourFPR));
			sb.append(String.format("\tFNR: %f\n", this.ourFNR));
			sb.append(String.format("\tEID: %f\n", this.ourEID));
			sb.append("\tEventList:\n");
			for (Integer i : this.ourEVStatus.keySet()) {
				sb.append(String.format("\t\t%d:\t%s\n",i,ourEVStatus.get(i)));
			}
			return sb.toString();
		}

		public void setBINum(int bInum) {
			this.ourBINum = bInum;
		}

		public void setSINum(int sInum) {
			this.ourSINum = sInum;
		}

		public void setSBINum(int sBInum) {
			this.ourSBINum = sBInum;
		}

		public void setNBINum(int nBInum) {
			this.ourNBINum = nBInum;
		}
		
	}
