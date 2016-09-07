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
 *TODO: *Deprecated* - this whole thing should probably be removed and re-written
 * 
 */
package net.strasnet.kids.measurement.test;

import java.io.File;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.util.SimpleIRIMapper;

import net.strasnet.kids.KIDSOntologyDatatypeValuesException;
import net.strasnet.kids.KIDSOntologyObjectValuesException;
import net.strasnet.kids.measurement.DataInstance;
import net.strasnet.kids.measurement.Dataset;
import net.strasnet.kids.measurement.EventOccurrence;
import net.strasnet.kids.measurement.KIDSMeasurementIncompatibleContextException;
import net.strasnet.kids.measurement.KIDSMeasurementInstanceUnsupportedFeatureException;
import net.strasnet.kids.measurement.KIDSMeasurementOracle;
import net.strasnet.kids.signalRepresentations.KIDSRepresentationInvalidRepresentationValueException;
import net.strasnet.kids.signalRepresentations.KIDSSignalByteMatchRepresentation;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

/**
 * @author chrisstrasburg
 * 
 * This needs to be completely revamped to use the new dataset structure.
 *
 */
public class testEIDSignalEvaluation {

	private static final String testKB = "file:///Users/chrisstrasburg/Documents/academic-research/projects/SignalBasedOntologies/ontologies/experiment1.owl";
	private static final String OntologyLocation = "http://solomon.cs.iastate.edu/ontologies/KIDS.owl";
	private List<Dataset> datasets = null;
	private static final IRI testSignal1IRI = IRI.create("#IPProtocolNumberSignal_1");
	private static final IRI testEventIRI = IRI.create("#smurfAttackEvent");
	private KIDSLibpcapDataset testSet1 = null;
	private OWLNamedIndividual testSignal1 = null;

	@Before
	public void setup() {
		if (testSet1 == null){
			try {
				KIDSMeasurementOracle myGuy = new KIDSMeasurementOracle();
	            SimpleIRIMapper m = new SimpleIRIMapper(IRI.create(OntologyLocation), IRI.create(testKB));
				myGuy.loadKIDS(IRI.create(OntologyLocation), m);

				// First, get the sample repository of contexts from the dataset
				datasets = myGuy.getDatasetsForEvent(IRI.create(myGuy.getOurIRI() + testEventIRI.toString()));
				testSet1 = (KIDSLibpcapDataset) datasets.get(0);
				
				// Next, get the signal individual
				testSignal1 = myGuy.getOwlDataFactory().getOWLNamedIndividual(
						IRI.create(myGuy.getOurIRI() + testSignal1IRI.toString()));
				
			} catch (IOException e) {
				e.printStackTrace();
				System.exit(1);
			} catch (Exception e){
				System.err.println(e);
				e.printStackTrace();
				System.exit(1);
			}
			//testSet1 = new KIDSLibpcapDataset();
		}
	}
	
	/**
	 * Test the EID computation:
	 */
	@Test
	public void testGetEID(){
		
		// Get the entropy of the dataSet:
		// p(c=0)
		int nTot = testSet1.numInstances();
		int[] nPosAry = testSet1.numPositiveInstances();
		int nPos = 0;
		int nNeg = nTot - nPos;
		
		// Set nPos to the total number of positive instances:
		for (int i : nPosAry){
			nPos += i;
		}
		
		// The entropy of the data set, from an instance perspective
		double HE = computeEntropy(nPos,nNeg);
		
		// Get the matching instances for the signal:
		int nPosPos = 0;
		int nPosNeg = 0;
		int nNegPos = 0;
		int nNegNeg = 0;
		
		Iterator<DataInstance> i = testSet1.getIterator();
		while (i.hasNext()){
			DataInstance cur = i.next();
			try {
				if (cur.matchesSignal(this.testSignal1)){
					if (cur.getLabel().isEvent()){
						nPosPos++;
					} else {
						nPosNeg++;
					}
				} else {
					if (cur.getLabel().isEvent()){
						nNegPos++;
					} else {
						nNegNeg++;
					}
				}
			} catch (KIDSOntologyObjectValuesException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.exit(1);
			} catch (KIDSMeasurementInstanceUnsupportedFeatureException e) {
				// The instance implementation doesn't support this feature (yet); make some noise
				// negative signal match.
				System.err.println("[W]: Measurement needs implementation of unsupported feature: " + e);
				if (cur.getLabel().isEvent()){
					nNegPos++;
				} else {
					nNegNeg++;
				}
			} catch (KIDSMeasurementIncompatibleContextException e) {
				// The data instance doesn't even have a feature to support this signal, so treat it as a 
				// negative signal match.
				if (cur.getLabel().isEvent()){
					nNegPos++;
				} else {
					nNegNeg++;
				}
			} catch (KIDSOntologyDatatypeValuesException e) {
				// Error in the KB - Warn
				System.err.println("[W]: Datatype value cardinality error on signal: " + e);
				e.printStackTrace();
			} catch (KIDSRepresentationInvalidRepresentationValueException e) {
				// Error in the KB - Warn
				System.err.println("[W]: Signal specifies an invalid representation value: " + e);
				e.printStackTrace();
			}
		}
		
		double nPosTot = nPosPos + nPosNeg;
		double nNegTot = nNegPos + nNegNeg;
		
		double HPos = computeEntropy(nPosPos, nPosNeg);
		double HNeg = computeEntropy(nNegPos, nNegNeg);
		
		// Get the IG of this signal:
		double IGs = HE - ((double)nPosTot / nTot * HPos + (double)nNegTot / nTot * HNeg);
		
		// Finally, get the CID:
		double CID = IGs / HE;
		
		System.out.println("CID:\n\tSignal: " + testSignal1IRI.toString() + 
							"\n\tEvent: " + testEventIRI.toString() + 
							"\n\tDataSet: " + testSet1.toString() +
							"\n\tCID = " + CID);
		
		// Get the resultant entropy of the positive and negative signal results:
		assertTrue(CID >= 0 && CID <= 1);
	}
	
	/**
	 * Given a 2-class count of instances, return the entropy as a double.  If either class count
	 * is '0', entropy is '0'.
	 * @param c1 - The count of instances of class 1
	 * @param c2 - The count of instances of class 2
	 * @return The entropy H(c) = - (p(c1)*log_2(p(c1)) + p(c2)*log_2(p(c2)))
	 */
	private double computeEntropy(int c1, int c2){
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
	 * @param args - one arg is the libpcap file to parse
	 * run the tests
	 */
	public static void main(String[] args) {
		org.junit.runner.JUnitCore.main("net.strasnet.kids.measurement.test.testCIDSignalEvaluation");
	}

}
