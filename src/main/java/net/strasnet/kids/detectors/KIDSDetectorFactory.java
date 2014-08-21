package net.strasnet.kids.detectors;

import net.strasnet.kids.detectors.KIDSDetector;

public class KIDSDetectorFactory {

	/**
	 * Load the correlation function class given its implementation path
	 * @param A string representing the classpath of the detector to instantiate.
	 * @return
	 */
	public static KIDSDetector getKIDSDetector(String kdImplementation) {
		try {
			String strippedName = kdImplementation;
			if (strippedName.startsWith("\"")){
				strippedName = strippedName.substring(1);
			}
			if (strippedName.endsWith("\"")){
				strippedName = strippedName.substring(0,strippedName.length() - 1);
			}
			Class<?> newClass = Class.forName(strippedName);
			Object instance = newClass.newInstance();
			KIDSDetector toReturn = (KIDSDetector) instance;
			return toReturn;
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			System.out.println("Class " + kdImplementation + " found, but not instantiated.\n" + e);
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			System.out.println("Class " + kdImplementation + " could not be found.\n" + e);
			e.printStackTrace();
		}

		return null;
	}

}
