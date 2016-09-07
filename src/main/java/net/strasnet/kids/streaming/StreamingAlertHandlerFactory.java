/**
 * 
 */
package net.strasnet.kids.streaming;

import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.semanticweb.owlapi.model.IRI;

/**
 * @author Chris Strasburg
 * Using the factory pattern, this class will handle requests for new alert handlers, fielding them from 
 * existing instantiated classes where possible.
 */
public class StreamingAlertHandlerFactory {
	
	private KIDSStreamingOracle o = null;
	private Map<IRI, StreamingAlertHandler> instantiationMap = null;
	
	Logger logme = LogManager.getLogger(StreamingAlertHandlerFactory.class.getName());
	
	/**
	 * 
	 * @param myO - The interface to the oracle.
	 */
	public StreamingAlertHandlerFactory(KIDSStreamingOracle myO) {
		o = myO;
		instantiationMap = new HashMap<IRI, StreamingAlertHandler>();
	}
	
	public StreamingAlertHandler getResponseClass(IRI handlerID) {
		StreamingAlertHandler toReturn = null;
		
		if (!instantiationMap.containsKey(handlerID)){
			// Let's make a new one - time for some reflection:
			String strippedName = "Name not loaded from oracle.";
			try {
				strippedName = o.getAlertHandlerClass(handlerID);
				if (strippedName.startsWith("\"")){
					strippedName = strippedName.substring(1);
				}
				if (strippedName.endsWith("\"")){
					strippedName = strippedName.substring(0,strippedName.length() -1);
				}
				Class<?> newClass = Class.forName(strippedName);
				Object instance = newClass.newInstance();
				instantiationMap.put(handlerID, (StreamingAlertHandler)instance);
			} catch (ClassNotFoundException e){
				logme.error(String.format("Could not find class for response %s -> %s",handlerID, strippedName),e);
				return null;
			} catch (InstantiationException e){
				logme.error(String.format("Could not instantiate class for response %s -> %s",handlerID, strippedName),e);
				return null;
			} catch (IllegalAccessException e){
				logme.error(String.format("Illegal access: %s -> %s",handlerID, strippedName),e);
				return null;
			}
		}
		toReturn = instantiationMap.get(handlerID); 
		
		return toReturn;
	}

}
