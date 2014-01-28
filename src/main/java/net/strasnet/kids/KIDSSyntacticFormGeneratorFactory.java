/**
 * 
 */
package net.strasnet.kids;

/**
 * @author chrisstrasburg
 * 
 * Produce an instance of a subclass of KIDSCanonicalRepresentation given certain parameters.
 *
 */
public class KIDSSyntacticFormGeneratorFactory {

	
	public static KIDSSyntacticFormGenerator createGenerator (String className){
		// Check to make sure the class exists
		try {
			String strippedName = className;
			if (strippedName.startsWith("\"")){
				strippedName = strippedName.substring(1);
			}
			if (strippedName.endsWith("\"")){
				strippedName = strippedName.substring(0,strippedName.length() - 1);
			}
			Class<?> newClass = Class.forName(strippedName);
			Object instance = newClass.newInstance();
			return (KIDSSyntacticFormGenerator) instance;
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			System.out.println("Class " + className + " found, but not instantiated.");
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			System.out.println("Class " + className + " could not be found.");
			e.printStackTrace();
		}
		return null;
	}
	
	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		//Try loading some classes:
		Class newClass;
		try {
			System.out.print("Loading class ...");
			newClass = Class.forName("net.strasnet.kids.snort.KIDSSnortRuleGenerator");
			System.out.print("Success!\n Instantiating class...");
			Object instance = newClass.newInstance();
			System.out.println("Success!");
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
