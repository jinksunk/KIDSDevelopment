package net.strasnet.kids.measurement.correlationfunctions;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import net.strasnet.kids.detectors.KIDSBroDetector;
import net.strasnet.kids.measurement.CorrelationFunction;

public class KIDSCorrelationFunctionFactory {

	private static final Logger logme = LogManager.getLogger(KIDSCorrelationFunctionFactory.class.getName());

	/**
	 * Load the correlation function class given its implementation path
	 * @param literal
	 * @return
	 */
	public static CorrelationFunction getCorrelationFunction(String cfImplementation) {
		logme.debug(String.format("Loading correlation function class %s", cfImplementation));
		try {
			String strippedName = cfImplementation;
			if (strippedName.startsWith("\"")){
				strippedName = strippedName.substring(1);
			}
			if (strippedName.endsWith("\"")){
				strippedName = strippedName.substring(0,strippedName.length() - 1);
			}
			Class<?> newClass = Class.forName(strippedName);
			Object instance = newClass.newInstance();
			CorrelationFunction toReturn = (CorrelationFunction) instance;
			return toReturn;
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			System.out.println("Class " + cfImplementation + " found, but not instantiated.\n" + e);
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			System.out.println("Class " + cfImplementation + " could not be found.\n" + e);
			e.printStackTrace();
		}

		return null;
	}

}
