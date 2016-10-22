/**
 * 
 */
package net.strasnet.kids.ui.gui;

import javax.swing.JDialog;
import javax.swing.JFrame;

import org.apache.logging.log4j.LogManager;
import org.semanticweb.owlapi.model.IRI;

/**
 * @author cstras
 *
 */
public class KIDSAddIndividualJDialog extends JDialog {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 5851375089243455872L;
	public static final org.apache.logging.log4j.Logger logme = LogManager.getLogger(KIDSAddIndividualJDialog.class.getName());

	protected String addedElementIRI = null;
	protected IRI ourAboxIRI = null;
	protected IRI addedElementProcessedIRI = null;
	protected ABOXBuilderController controller = null;

	/**
	 * 
	 * @param parent - The container parent to attach the dialog to
	 * @param aboxiri - the IRI of the ABOX to associate this individual with
	 */
	public KIDSAddIndividualJDialog(JFrame parent, IRI aboxiri, ABOXBuilderController controller) {
		super(parent);
		ourAboxIRI = aboxiri;
		this.controller = controller;
	}
	
	public IRI getAddedElementIRI(){
		if (addedElementProcessedIRI == null){
			addedElementProcessedIRI = controller.getAbsoluteIRI(ourAboxIRI, IRI.create(addedElementIRI));
		}
		return addedElementProcessedIRI;
	}

}
