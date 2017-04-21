package net.strasnet.kids.detectors;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import net.strasnet.kids.detectors.KIDSDetector;

public class KIDSDetectorFactory {
	
	private static final Logger logme = LogManager.getLogger(KIDSDetectorFactory.class.getName());
	private static Map<String, KIDSDetector> activeImplMap = new HashMap<String, KIDSDetector>();

	/**
	 * Load the detector class given its implementation path
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
			if (activeImplMap.containsKey(strippedName)){
				logme.debug("Using cached object for " + strippedName);
				return activeImplMap.get(strippedName);
			} else {
				logme.debug("No cached object for " + strippedName + "; creating new implementation.");
			    Class<?> newClass = Class.forName(strippedName);
			    Object instance = newClass.newInstance();
			    KIDSDetector toReturn = (KIDSDetector) instance;
			    activeImplMap.put(strippedName, toReturn);
			    return toReturn;
			}
		} catch (InstantiationException e) {
			logme.error("Class " + kdImplementation + " found, but not instantiated.\n" + e);
			e.printStackTrace();
		} catch (ClassNotFoundException | IllegalAccessException e) {
			logme.error("Class " + kdImplementation + " could not be found.\n" + e);
			e.printStackTrace();
		}

		return null;
	}

}
