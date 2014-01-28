/**
 * 
 */
package net.strasnet.kids.gui;

import org.semanticweb.owlapi.model.IRI;

/**
 * @author chrisstrasburg
 * This class provides middleware for interfaces to interact with knowledge bases
 * for the purposes of describing events and generating syntactic forms of events.
 * 
 * Functionality includes:
 *  - Open knowledge base (local or remote)
 *  - Save knowledge base
 *  - Add event
 *  - List events
 *  - Add IDS
 *  - List IDSes
 *  - Add feature
 *  - List Features
 *  - Add signal
 *  - List signals
 *  - List signal languages
 *
 */
public class eventDescription {
	IRI currentKB;
	
	public void openKB (IRI target){
		currentKB = target;
	}
}
