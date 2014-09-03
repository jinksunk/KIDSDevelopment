/**
 * 
 */
package net.strasnet.kids.bro;

import java.util.HashMap;
import java.util.Map;

/**
 * @author cstras
 * Returns a null value for any argument.
 */
public class BroNullValueParser implements BroValueParserInterface {

	/* (non-Javadoc)
	 * @see net.strasnet.kids.bro.BroValueParserInterface#getParsedValues(java.lang.String)
	 */
	@Override
	public Map<String, String> getParsedValues(String valToParse) {
		return null;
	}

}
