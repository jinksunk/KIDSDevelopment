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
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import org.apache.logging.log4j.LogManager;
import org.semanticweb.owlapi.model.IRI;

/**
 * @author cstras
 *
 */
public class KIDSAddStringDataJDialog extends KIDSAddDataJDialog {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 5851375089243455872L;
	public static final org.apache.logging.log4j.Logger logme = LogManager.getLogger(KIDSAddStringDataJDialog.class.getName());

	/**
	 * 
	 * @param parent - The container parent to attach the dialog to
	 * @param aboxiri - the IRI of the ABOX to associate this individual with
	 */
	public KIDSAddStringDataJDialog(JFrame parent, 
			IRI aboxiri, 
			IRI subjectiri,
			IRI datapropiri,
			ABOXBuilderController controller) {
		super(parent, aboxiri, subjectiri, datapropiri, controller);

		setBounds(100, 100, 450, 300);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setLayout(new FlowLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		{
			JLabel lblSignalIri = new JLabel(String.format("%s %s value: ", ourIndIRI.getFragment(),
					ourDataPropIRI.getFragment()));
			contentPanel.add(lblSignalIri);
		}
		{
			dataValueTextField = new JTextField();
			contentPanel.add(dataValueTextField);
			dataValueTextField.setColumns(10);
		}
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton okButton = new JButton("OK");
				okButton.setActionCommand("OK");
				okButton.addMouseListener(getMouseAdapter());
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
			}
			{
				JButton cancelButton = new JButton("Cancel");
				cancelButton.setActionCommand("Cancel");
				buttonPane.add(cancelButton);
			}
		}
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
						logme.debug(String.format("STRING dialog defines no checks."));
						// Nothing to check for String values
						dispose();
					}
					
				};
	}

}
