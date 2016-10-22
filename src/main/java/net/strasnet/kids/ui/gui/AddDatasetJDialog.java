package net.strasnet.kids.ui.gui;

import javax.swing.JFrame;

import org.semanticweb.owlapi.model.IRI;

public class AddDatasetJDialog extends KIDSAddIndividualJDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = 42461733020060477L;

	public AddDatasetJDialog(JFrame parent, IRI aboxiri, ABOXBuilderController c){
		super(parent, aboxiri, c);
	}


}
