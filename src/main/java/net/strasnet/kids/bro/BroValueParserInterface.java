/**
 * 
 */
package net.strasnet.kids.bro;

import java.util.Map;

/**
 * @author cstras
 * This interface provides a standard API for parsing the values provided by specific signal value classes into
 * the parameters required for a bro detector.
 */
public interface BroValueParserInterface {
	
	public Map<String, String> getParsedValues(String valToParse);

}
