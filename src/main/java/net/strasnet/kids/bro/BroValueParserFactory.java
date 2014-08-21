/**
 * 
 */
package net.strasnet.kids.bro;

import net.strasnet.kids.detectors.KIDSDetector;

/**
 * @author cstras
 * Given a class name, will produce an instance of BroValueParserInterface.
 */
public class BroValueParserFactory {
	
	public static BroValueParserInterface getInterfaceImplementation(String classToInstantiate){
		//TODO: Implement This
		try {
			String strippedName = classToInstantiate;
			if (strippedName.startsWith("\"")){
				strippedName = strippedName.substring(1);
			}
			if (strippedName.endsWith("\"")){
				strippedName = strippedName.substring(0,strippedName.length() - 1);
			}
			Class<?> newClass = Class.forName(strippedName);
			Object instance = newClass.newInstance();
			BroValueParserInterface toReturn = (BroValueParserInterface) instance;
			return toReturn;
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			System.out.println("Class " + classToInstantiate + " found, but not instantiated.\n" + e);
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			System.out.println("Class " + classToInstantiate + " could not be found.\n" + e);
			e.printStackTrace();
		}

		return null;
		
	}

}
