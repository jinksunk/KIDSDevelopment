/**
 * 
 */
package net.strasnet.kids.measurement;

import net.strasnet.kids.measurement.correlationfunctions.IncompatibleCorrelationValueException;

/**
 * @author chrisstrasburg
 *
 * This class represents a correlation function implementation.  Implementers provide the functionality to define and return the 
 * correlation resource, and perform the indicated operation on that resource.
 */
public interface CorrelationFunction {

	/**
	 * 
	 * @param r1 - The first resource to check correlation against
	 * @param r2 - The second resource to check correlation against
	 * @return True if the two resource values are correlated under this function, false otherwise.
	 * @throws IncompatibleCorrelationValueException 
	 */
	public boolean isCorrelated(String r1, String r2);
}
