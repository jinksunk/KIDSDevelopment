/**
 * 
 */
package net.strasnet.kids.measurement;

import java.util.Set;

import net.strasnet.kids.measurement.correlationfunctions.IncompatibleCorrelationValueException;

/**
 * @author chrisstrasburg
 *
 * This class represents a correlation function implementation.  Implementers provide the functionality to define and return the 
 * correlation resource, and perform the indicated operation on that resource.
 * 
 * The idea is that a correlation function can be used to identify which instances are related to other instances.
 * Should the correlation function itself take, as input, a set of instances, and return a set of correlation instances?
 * Or should it simply operate on pairs of instances?
 * 
 * When a correlated data instance is created, it should start from a 'seed' base instance, which is then compared, via
 * the correlation function, with all other data instances.  If we assume that a base instance will belong to only one
 * correlated instance, then we are assuming an equivalence relation.  Otherwise, we will still have at most 'n' correlated instances,
 * where 'n' is the total number of base instances.
 * 
 * So, given this intended use case, the correlation function should return 'true' or 'false' when comparing base instances.  To do this,
 * it needs to know:
 * 1) What resource(s) are required to evaluate the correlation function on an instance
 *    - Therefore, the data instance needs to supply the resource via getResources() (Map<IRI, String>)
 *    
 * We can at least assume transitivity?
 */
public interface CorrelationFunction {

	/**
	 * 
	 * @param i1 - The first resource to check correlation against
	 * @param i2 - The second resource to check correlation against
	 * @return True if the two resource values are correlated under this function, false otherwise.
	 * @throws IncompatibleCorrelationValueException  - if the two data instances cannot be compared via this
	 * correlation function.
	 */
	//public boolean isCorrelated(DataInstance i1, DataInstance i2) throws IncompatibleCorrelationValueException;
	
	/**
	 * Given a set of data instances, return a set of correlated data instances based on this correlation function.
	 * @return The set of corrleated data instances based on this correlation function.
	 * @throws IncompatibleCorrelationValueException 
	 */
	public Set<CorrelationDataInstance> generateCorrelatedDataSet(Set<DataInstance> rawInstances) throws IncompatibleCorrelationValueException;
}
