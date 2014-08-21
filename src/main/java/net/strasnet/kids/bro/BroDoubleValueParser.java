/**
 * 
 */
package net.strasnet.kids.bro;

import java.util.HashMap;
import java.util.Map;

/**
 * @author cstras
 * Returns a map containing the key "DoubleValue" and the value provided.
 */
public class BroDoubleValueParser implements BroValueParserInterface {

	/* (non-Javadoc)
	 * @see net.strasnet.kids.bro.BroValueParserInterface#getParsedValues(java.lang.String)
	 */
	@Override
	public Map<String, String> getParsedValues(String valToParse) {
		Map<String, String> toReturn = new HashMap<String,String>();
		Double.parseDouble(valToParse);
		toReturn.put("Value1", valToParse);
		return toReturn;
	}

}
