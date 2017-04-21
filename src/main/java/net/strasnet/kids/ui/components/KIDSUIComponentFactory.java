/**
 * 
 */
package net.strasnet.kids.ui.components;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.LogManager;
import org.semanticweb.owlapi.model.IRI;

import net.strasnet.kids.ui.gui.KIDSGUIOracle;

/**
 * @author cstras
 *
 */
public class KIDSUIComponentFactory {
	/**
	 * This factory class handles creating, caching, and referencing UIComponents.
	 */

	public static final org.apache.log4j.Logger logme = LogManager.getLogger(KIDSUIComponentFactory.class.getName());
	private static Map<IRI, KIDSUIComponent> cmap = new HashMap<IRI, KIDSUIComponent>();
	
	/**
	 * If the component already exists, will reuse; otherwise will create a new one.
	 * @return
	 * @throws InstantiationException 
	 */
	public static KIDSUIComponent getUIComponent(IRI cIRI, Class<? extends KIDSUIComponent> targetClass, KIDSGUIOracle o) throws InstantiationException{
		KIDSUIComponent toReturn = null;
		if (cmap.containsKey(cIRI)){
			logme.debug(String.format("Found key for IRI %s", cIRI.toString()));
			toReturn = cmap.get(cIRI);
			if (toReturn.getClass() != targetClass){
				throw new InstantiationException(String.format("Requested class %s does not match existing class %s for component %s",
						toReturn.getClass().getName(),
						targetClass.getName(),
						cIRI.toString()));
			}
			return toReturn;
		} else {
			logme.debug(String.format("No existing component found  for IRI %s; creating new class %s", cIRI.toString(), targetClass.getName()));
			
			try {
				toReturn = (KIDSUIComponent)targetClass.getConstructor(IRI.class, KIDSGUIOracle.class).newInstance(cIRI, o);
				cmap.put(cIRI, toReturn);
				return toReturn;
			} catch (InstantiationException | IllegalAccessException | IllegalArgumentException |
					InvocationTargetException | NoSuchMethodException | SecurityException e) {
				logme.error(String.format("Exception creating %s of class %s: %s", cIRI.toString(), targetClass.getName(), e));
				e.printStackTrace();
			}
			
		}
		return null;
	}

}
