/**
 * 
 */
package net.strasnet.kids.eventgenerator.datasetmodifier;

import net.strasnet.kids.eventgenerator.dataelement.KIDSDataElement;
import net.strasnet.kids.exceptions.KIDSTypeMismatchException;

/**
 * @author cstras
 * Defines the methods used to interact with dataset modifiers.
 */
public interface KIDSDatasetModifier {

	/**
	 * 
	 * @param e
	 * @throws KIDSTypeMismatchException - If the KIDSDataElement subclass cannot be used by the implementer.
	 */
	public void addDataElement(KIDSDataElement e) throws KIDSTypeMismatchException;
	
	/**
	 * Signals to the implementer that modifications are finished. For example, if a file is to be written to
	 * the filesystem, this method should trigger a write.
	 */
	public void close();
	
}
