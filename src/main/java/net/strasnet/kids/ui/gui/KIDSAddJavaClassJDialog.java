/**
 * 
 */
package net.strasnet.kids.ui.gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import org.apache.log4j.LogManager;
import org.semanticweb.owlapi.model.IRI;

/**
 * @author cstras
 *
 */
public class KIDSAddJavaClassJDialog extends KIDSAddStringDataJDialog {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 5851375089243455872L;
	public static final org.apache.log4j.Logger logme = LogManager.getLogger(KIDSAddJavaClassJDialog.class.getName());

	/**
	 * 
	 * @param parent - The container parent to attach the dialog to
	 * @param aboxiri - the IRI of the ABOX to associate this individual with
	 */
	public KIDSAddJavaClassJDialog(JFrame parent, 
			IRI aboxiri, 
			IRI subjectiri,
			IRI datapropiri,
			ABOXBuilderController controller) {
		super(parent, aboxiri, subjectiri, datapropiri, controller);
	}
	
	/**
	 * Subclasses should override this method to check string values.
	 * 
	 * @return A mouse adapter which includes defined checks for submitted values.
	 */
	protected MouseAdapter getMouseAdapter(){
		return new MouseAdapter() {
					@Override
					public void mouseClicked(MouseEvent e) {
						String dv = dataValueTextField.getText();
						logme.debug(String.format("JAVA CLASS dialog: checking for valid class:"));
						// Nothing to check for String values
						try {
							Class c = Class.forName(dv);
							logme.debug(String.format("Returned value %s is a valid class.", dv));
							dispose();
						} catch (ClassNotFoundException e1) {
							logme.debug(String.format("Specified value %s is NOT a valid class.", dv));
							JOptionPane.showMessageDialog(KIDSAddJavaClassJDialog.this, 
									String.format("Entered value %s is not a known Java class; please"
											+ " verify the Java classpath and path specification.", 
											dv));
						}
					}
					
				};
	}

}
