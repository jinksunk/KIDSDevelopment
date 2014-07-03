package net.strasnet.kids.measurement.correlationfunctions;

import net.strasnet.kids.measurement.CorrelationDataInstance;
import net.strasnet.kids.measurement.CorrelationFunction;
import net.strasnet.kids.measurement.DataInstance;
import net.strasnet.kids.measurement.Dataset;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.semanticweb.owlapi.model.IRI;

public class SourceIPAddressCorrelationFunction implements CorrelationFunction {
	/**
	 * Since this class represents an equivalence relation, we can take some shortcuts when constructing the correlated data set.
	 * * We only need to test any instance once; once it is a part of a correlation function, we can remove it from further consideration.
	 */
	public static final String kidsTbox = "http://solomon.cs.iastate.edu/ontologies/KIDS.owl";
	public static final IRI relatedResource = IRI.create(kidsTbox + "#IPv4SourceAddressSignalDomain");
	
	public static boolean DEBUG = true;
	public void debugPrint(String msg){
		if (DEBUG){
			System.err.println(msg);
		}
	}
	
	/**
	 * 
	 * @param i1
	 * @param i2
	 * @return true if they share the required resource(s) for correlation, false otherwise.
	 * @throws IncompatibleCorrelationValueException
	 */
	public boolean isCorrelated (DataInstance i1, DataInstance i2) throws IncompatibleCorrelationValueException{
		Map<IRI, String> rMap1 = i1.getResources();
		Map<IRI, String> rMap2 = i2.getResources();
		
		// If either does not have the resource, they cannot be correlated:
		if (!rMap1.containsKey(relatedResource) ||
			!rMap2.containsKey(relatedResource)){
			return false;
			//throw new IncompatibleCorrelationValueException("Data instances do not share resource " + relatedResource);
		}
		InetAddress ip1 = null;
		InetAddress ip2 = null;
		try {
			ip1 = InetAddress.getByName(rMap1.get(relatedResource));
		} catch (UnknownHostException e) {
			throw new IncompatibleCorrelationValueException("Value " + rMap1.get(relatedResource) + " is not a valid IPv4 Address.");
		}
		try {
			ip2 = InetAddress.getByName(rMap2.get(relatedResource));
		} catch (UnknownHostException e) {
			throw new IncompatibleCorrelationValueException("Value " + rMap2.get(relatedResource) + " is not a valid IPv4 Address.");
		}
		return ip1.equals(ip2);
	}


	@Override
	public Set<CorrelationDataInstance> generateCorrelatedDataSet(
			Set<DataInstance> rawInstances) throws IncompatibleCorrelationValueException {
		Set<CorrelationDataInstance> toReturn = new HashSet<CorrelationDataInstance>();
	//	for (DataInstance i : rawInstances){
	//		System.err.println("[DI] " + i.getID());
	//	}
		
		// Iterate over the set of data instances until all are correlated.
		// Sort the list, then just take it in chunks
		HashMap <String, Set<DataInstance>> sipMap = new HashMap<String,Set<DataInstance>>();

		for (DataInstance i : rawInstances){
			Map<IRI, String> rMap2 = i.getResources();
			String sip = rMap2.get(SourceIPAddressCorrelationFunction.relatedResource);
			if (!sipMap.containsKey(sip)){
				sipMap.put(sip, new HashSet<DataInstance>());
			}
			sipMap.get(sip).add(i);
		}
		
		// Now, the sets are exactly those sets of correlated data instances!
		for (String ciSIP : sipMap.keySet()){
		
			Set<DataInstance> correlatedInstances = sipMap.get(ciSIP);
			CorrelationDataInstance newGuy = new CorrelationDataInstance(sipMap.get(ciSIP));
			if (correlatedInstances.size() > 1){
			  debugPrint("\n[SIP Correlation Functions]: Source IP from instance " + ciSIP + " resulted in CDI with: " + correlatedInstances.size() + " instances, " + newGuy.getEventInstances().keySet().size() + " events, and " + newGuy.getResourceSets().keySet().size() + " resources." );
			}
			toReturn.add(newGuy);
		}
		
	    debugPrint ("Final CDI count: " + toReturn.size());
		return toReturn;
	}
}
