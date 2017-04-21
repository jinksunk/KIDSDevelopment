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

import org.apache.log4j.LogManager;
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
	public static final org.apache.log4j.Logger logme = LogManager.getLogger(KIDSAddIndividualJDialog.class.getName());

	protected String addedElementIRI = null;
	protected IRI ourAboxIRI = null;
	protected IRI ourClassIRI = null;
	protected IRI addedElementProcessedIRI = null;
	protected ABOXBuilderController controller = null;
	private final JPanel contentPanel = new JPanel();
	private JTextField individualIRIJTextField;


	/**
	 * 
	 * @param parent - The container parent to attach the dialog to
	 * @param aboxiri - the IRI of the ABOX to associate this individual with
	 */
	public KIDSAddIndividualJDialog(JFrame parent, 
			IRI aboxiri, 
			IRI classiri, 
			ABOXBuilderController controller) {
		super(parent);
		ourAboxIRI = aboxiri;
		ourClassIRI = classiri;
		this.controller = controller;

		setBounds(100, 100, 450, 300);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setLayout(new FlowLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		{
			JLabel lblSignalIri = new JLabel(String.format("%s IRI: %s",ourClassIRI.getShortForm(), ourAboxIRI.toString()));
			contentPanel.add(lblSignalIri);
		}
		{
			individualIRIJTextField = new JTextField();
			contentPanel.add(individualIRIJTextField);
			individualIRIJTextField.setColumns(10);
		}
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton okButton = new JButton("OK");
				okButton.setActionCommand("OK");
				okButton.addMouseListener(new MouseAdapter() {
					@Override
					public void mouseClicked(MouseEvent e) {
						// TODO Auto-generated method stub
						addedElementIRI = individualIRIJTextField.getText();
						dispose();
					}
					
				});
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
	
	public IRI getAddedElementIRI(){
		if (addedElementProcessedIRI == null){
			addedElementProcessedIRI = controller.getAbsoluteIRI(ourAboxIRI, IRI.create(addedElementIRI));
		}
		logme.debug(String.format("Returning added element IRI %s", addedElementProcessedIRI));
		return addedElementProcessedIRI;
	}
	
}
