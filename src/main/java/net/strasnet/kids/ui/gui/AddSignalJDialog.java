package net.strasnet.kids.ui.gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import org.semanticweb.owlapi.model.IRI;

import javax.swing.JLabel;
import javax.swing.JTextField;

public class AddSignalJDialog extends KIDSAddIndividualJDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3908074278016058956L;
	private final JPanel contentPanel = new JPanel();
	private JTextField signalIRIJTextField;

	/**
	 * Create the dialog.
	 */
	public AddSignalJDialog(JFrame frame, IRI aboxiri, ABOXBuilderController c) {
		super(frame, aboxiri, c);
		setBounds(100, 100, 450, 300);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setLayout(new FlowLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		{
			JLabel lblSignalIri = new JLabel(String.format("Signal IRI: %s",ourAboxIRI.toString()));
			contentPanel.add(lblSignalIri);
		}
		{
			signalIRIJTextField = new JTextField();
			contentPanel.add(signalIRIJTextField);
			signalIRIJTextField.setColumns(10);
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
						addedElementIRI = signalIRIJTextField.getText();
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

}
