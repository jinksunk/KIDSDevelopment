package net.strasnet.kids.measurement.correlationfunctions;

import net.strasnet.kids.measurement.CorrelationFunction;

public class KIDSCorrelationFunctionFactory {

	/**
	 * Load the correlation function class given its implementation path
	 * @param literal
	 * @return
	 */
	public static CorrelationFunction getCorrelationFunction(String cfImplementation) {
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
