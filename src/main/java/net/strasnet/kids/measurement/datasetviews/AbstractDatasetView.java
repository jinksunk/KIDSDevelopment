/**
 * This class serves as a repository of common code to all dataset views.
 */
package net.strasnet.kids.measurement.datasetviews;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.strasnet.kids.KIDSOntologyDatatypeValuesException;
import net.strasnet.kids.KIDSOntologyObjectValuesException;
import net.strasnet.kids.detectors.KIDSDetector;
import net.strasnet.kids.detectors.KIDSDetectorFactory;
import net.strasnet.kids.detectors.UnimplementedIdentifyingFeatureException;
import net.strasnet.kids.detectorsyntaxproducers.KIDSIncompatibleSyntaxException;
import net.strasnet.kids.measurement.DataInstance;
import net.strasnet.kids.measurement.KIDSMeasurementOracle;
import net.strasnet.kids.measurement.KIDSUnEvaluableSignalException;

import org.semanticweb.owlapi.model.IRI;

/**
 * @author Chris Strasburg
 *
 */
public abstract class AbstractDatasetView implements DatasetView {

	protected KIDSMeasurementOracle myGuy;			      // The KIDSOracle used to interact with the KB
	protected IRI ourIRI = null;					  // The base IRI of 
	protected Map<DataInstance, DataInstance> ourInstances = null; // A cache of data instances

	/* (non-Javadoc)
	 * @see net.strasnet.kids.measurement.datasetviews.DatasetView#getMatchingInstances(java.util.Set)
	 */
	@Override
	public Set<DataInstance> getMatchingInstances(Set<IRI> signalSet)
			throws KIDSOntologyObjectValuesException,
			KIDSOntologyDatatypeValuesException, IOException,
			KIDSIncompatibleSyntaxException, KIDSUnEvaluableSignalException, UnimplementedIdentifyingFeatureException {
		
			if (ourInstances == null){
				ourInstances = new HashMap<DataInstance, DataInstance>();
			}

			HashMap<IRI,Set<IRI>> detectorsToSignals = new HashMap<IRI,Set<IRI>>();
			HashMap<IRI,KIDSDetector> IRIsToDetectors = new HashMap<IRI,KIDSDetector>();
			HashSet<IRI> failedDetector = new HashSet<IRI>();
			
			if (signalSet.isEmpty()){
				signalSet.add(null);
			}
			
			for (IRI mySig : signalSet){
				Set<IRI> signalDetectors;
				if (mySig == null){
					signalDetectors = myGuy.getDetectorsForView(getIRI());
					//signalSet.remove(null);
				} else {
					signalDetectors = myGuy.getDetectorsForSignalAndView(mySig, this.getIRI());
				}
				// If we have not yet loaded this detector, load it:
				for (IRI sigDet : signalDetectors){
					if (failedDetector.contains(sigDet)){
						continue;
					}
					if (!IRIsToDetectors.containsKey(sigDet)){
						KIDSDetector ourD = KIDSDetectorFactory.getKIDSDetector(myGuy.getDetectorImplementation(sigDet));
						try {
							ourD.init(myGuy.getDetectorExecutionString(sigDet), sigDet, myGuy);
						} catch (InstantiationException
								| IllegalAccessException
								| ClassNotFoundException e) {
							// TODO Auto-generated catch block
							System.err.println("[W] - Could not initialize detector " + sigDet + " -- Skipping");
							e.printStackTrace();
							continue;
						}
						IRIsToDetectors.put(sigDet, ourD);
					}
					if (!detectorsToSignals.containsKey(sigDet)){
						detectorsToSignals.put(sigDet,new HashSet<IRI>());
					}
					detectorsToSignals.get(sigDet).add(mySig);
				}
			}
			
			// Once built, get all matching instances.  For now, the policy can be to process all signals processable by the 'first' detector, the next set by
			// the next detector, and so on.
			HashSet<IRI> toBeProcessed = new HashSet<IRI>();
			toBeProcessed.addAll(signalSet);
			Set<DataInstance> allMatching = new HashSet<DataInstance>();
			boolean firstSignal = true;
			
			// For each detector, process the signals, keeping the intersection of what is matched.
			for (IRI kd : detectorsToSignals.keySet()){
				Set<IRI> thisSigSet = detectorsToSignals.get(kd);
				thisSigSet.retainAll(toBeProcessed);
				if (thisSigSet.isEmpty()){
					thisSigSet.add(null);
				}
				if (firstSignal){
					// Run the detector for each individual signal if we have not seen it before, otherwise
					// just return the intersection of data instances.
					allMatching = IRIsToDetectors.get(kd).getMatchingInstances(thisSigSet, this);
					firstSignal = false;
				} else {
					allMatching.retainAll(IRIsToDetectors.get(kd).getMatchingInstances(thisSigSet, this));
				}
				toBeProcessed.removeAll(thisSigSet);
			}
			if (toBeProcessed.size() != 0){
				StringBuilder sigList = new StringBuilder();
				for (IRI sign : toBeProcessed){
					sigList.append(sign + "\n");
				}
				throw new KIDSUnEvaluableSignalException("AbstractDatasetView.getMatchingInstances() -- Could not process all signals; the following were skipped: \n" + sigList.toString());
			}

		return allMatching;
	}

	
	/**
	 * 
	 * @param set - A set of resource -> value maps, one for each instance we need to create and return.  This method will extract
	 * identifying resources according to the defined idMap, and create instances with the given set of resources defined.
	 * @return The resulting set of data instances; throws an exception if not all resources are available.
	 * @throws UnimplementedIdentifyingFeatureException 
	 */
	/*
	protected Set<DataInstance> buildInstances(
			Set<DataInstance> set) {
		
		Set<DataInstance> toReturn = new HashSet<DataInstance>();
		for (Map<IRI,String> resourceSet : set){
			// First, extract the set of identifying features from the matchingInstances
			HashMap<IRI,String> idFeatures = new HashMap<IRI, String>();
			for (IRI idKey : this.getIdentifyingFeatures()){
				if (resourceSet.containsKey(idKey)){
					idFeatures.put(idKey, resourceSet.get(idKey));
				} else {
					System.err.println("[W] - Cannot extract identifying feature " + idKey + " from dataset view instance, skipping...");
					for (IRI rKey : resourceSet.keySet()){
						System.err.println("  - " + rKey.getFragment() + "\t:" + resourceSet.get(rKey));
					}
					idFeatures.put(idKey, null);
				}
			}
			// Next, get the remaining resources and add them to the instance
			DataInstance di = this.buildInstance(idFeatures);
			if (this.ourInstances.containsKey(di)){
				di = ourInstances.get(di);
			}
			di.addResources(resourceSet);
			ourInstances.put(di, di);
			toReturn.add(di);
		}
		return toReturn;
	} */


	/* (non-Javadoc)
	 * @see net.strasnet.kids.measurement.datasetviews.DatasetView#getSubview(java.util.Set)
	 */
	@Override
	public DatasetView getSubview(Set<DataInstance> members)
			throws KIDSOntologyDatatypeValuesException,
			KIDSOntologyObjectValuesException, InstantiationException,
			IllegalAccessException, ClassNotFoundException,
			KIDSUnEvaluableSignalException, UnimplementedIdentifyingFeatureException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see net.strasnet.kids.measurement.datasetviews.DatasetView#setIRI(org.semanticweb.owlapi.model.IRI)
	 */
	@Override
	public void setIRI(IRI iri) {
		this.ourIRI = iri;
	}

	@Override
	public IRI getIRI() {
		return this.ourIRI;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
