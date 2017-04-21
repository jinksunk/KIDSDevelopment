/**
 * 
 */
package net.strasnet.kids.ui.gui;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.apache.log4j.LogManager;
import org.semanticweb.owlapi.model.IRI;

/**
 * @author cstras
 * 
 * Base class for KIDS data-gathering dialog classes.
 *
 */
public abstract class KIDSAddDataJDialog extends JDialog {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -7583670328100419398L;
	public static final org.apache.log4j.Logger logme = LogManager.getLogger(KIDSAddDataJDialog.class.getName());
	protected IRI ourAboxIRI = null;
	protected IRI ourIndIRI = null;
	protected IRI ourDataPropIRI = null;
	protected ABOXBuilderController controller = null;
	protected final JPanel contentPanel = new JPanel();
	protected JTextField dataValueTextField;

	
	public KIDSAddDataJDialog(JFrame parent, IRI aboxiri, IRI subjectiri, 
			IRI datapropiri, ABOXBuilderController controller){
		super(parent);
		ourAboxIRI = aboxiri;
		ourIndIRI = subjectiri;
		ourDataPropIRI = datapropiri;
		this.controller = controller;
		
	}
	
	public String getAddedData(){
		return dataValueTextField.getText();
	}

}
