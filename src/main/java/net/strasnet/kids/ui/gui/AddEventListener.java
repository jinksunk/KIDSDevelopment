/**
 * 
 */
package net.strasnet.kids.ui.gui;

import org.semanticweb.owlapi.model.IRI;

/**
 * @author cstras
 *
 * Objects wishing to receive events when new Events are added should implement this interface.
 */
public interface AddEventListener {
	
	public void newEventReceived(IRI event);

}
