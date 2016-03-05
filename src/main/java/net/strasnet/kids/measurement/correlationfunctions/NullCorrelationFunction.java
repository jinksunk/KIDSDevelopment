package net.strasnet.kids.measurement.correlationfunctions;

import net.strasnet.kids.measurement.CorrelationDataInstance;
import net.strasnet.kids.measurement.CorrelationFunction;
import net.strasnet.kids.measurement.DataInstance;
import net.strasnet.kids.measurement.Dataset;
import net.strasnet.kids.measurement.test.KIDSTestSingleSignal;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;
import org.semanticweb.owlapi.model.IRI;

public class NullCorrelationFunction implements CorrelationFunction {
	/**
	 * This class implements a null correlation function, that is none of the instances are correlated with
	 * each other.
	 */
	public static final String kidsTbox = "http://solomon.cs.iastate.edu/ontologies/KIDS.owl";
	/*
	public static final IRI relatedResource = IRI.create(kidsTbox + "#IPv4SourceAddressSignalDomain"); // What should we set this to?  Do we need it?
	*/
	public static final org.apache.logging.log4j.Logger logme = LogManager.getLogger(NullCorrelationFunction.class.getName());
	
	/**
	 * 
	 * @param i1
	 * @param i2
	 * @return false
	 * @throws IncompatibleCorrelationValueException
	 */
	public boolean isCorrelated (DataInstance i1, DataInstance i2) throws IncompatibleCorrelationValueException{
		return false;
	}

	@Override
	public Set<CorrelationDataInstance> generateCorrelatedDataSet(
			Set<DataInstance> rawInstances) throws IncompatibleCorrelationValueException {
		Set<CorrelationDataInstance> toReturn = new HashSet<CorrelationDataInstance>();
		
		// Iterate over the set of data instances until all are correlated.
		// Sort the list, then just take it in chunks
		HashMap <String, Set<DataInstance>> sipMap = new HashMap<String,Set<DataInstance>>();

		for (DataInstance i : rawInstances){
		
			Set<DataInstance> correlatedInstances = new HashSet<DataInstance>();
			correlatedInstances.add(i);

			CorrelationDataInstance newGuy = new CorrelationDataInstance(correlatedInstances, null, null);
			if (newGuy.getInstances().size() == 1){
			  logme.info("\n[Null Correlation Function]: resulted in CDI " + newGuy);
			} else {
				logme.warn("\n[Null Correlation Function]: we should not have had more than a single instance, but we did in: " + newGuy);
			}
			toReturn.add(newGuy);
		}
		
	    logme.info("Final CDI count: " + toReturn.size());
		assert(toReturn.size() == rawInstances.size());
		return toReturn;
	}
}
