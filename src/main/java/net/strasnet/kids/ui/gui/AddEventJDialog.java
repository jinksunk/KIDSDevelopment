package net.strasnet.kids.ui.gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import org.semanticweb.owlapi.model.IRI;

import javax.swing.JLabel;
import javax.swing.JTextField;

public class AddEventJDialog extends KIDSAddIndividualJDialog {

	private final JPanel contentPanel = new JPanel();
	private JTextField EventIRIJTextField;

	/**
	 * Launch the application.
	public static void main(String[] args) {
		try {
			AddEventJDialog dialog = new AddEventJDialog();
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			dialog.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	 */
	
	/**
	 * Create the dialog.
	 */
	public AddEventJDialog(IRI aboxiri, ABOXBuilderController c) {
		this(null, aboxiri, c);
	}

	public AddEventJDialog(JFrame parent, IRI aboxiri, ABOXBuilderController c) {
		super(parent, aboxiri, c);
		setBounds(100, 100, 450, 123);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setLayout(new FlowLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		{
			JLabel lblNewEventName = new JLabel(String.format("New Event Name: %s", aboxiri));
			contentPanel.add(lblNewEventName);
		}
		{
			EventIRIJTextField = new JTextField();
			contentPanel.add(EventIRIJTextField);
			EventIRIJTextField.setColumns(10);
		}
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton okButton = new JButton("OK");
				okButton.addActionListener(new ActionListener(){

					@Override
					public void actionPerformed(ActionEvent e) {
						//TODO: Check input for IRIs
						addedElementIRI = EventIRIJTextField.getText();
						dispose();
					}
				});
				okButton.setActionCommand("OK");
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
			}
			{
				JButton cancelButton = new JButton("Cancel");
				cancelButton.addActionListener(new ActionListener(){

					@Override
					public void actionPerformed(ActionEvent e) {
						dispose();
					}
				});
				cancelButton.setActionCommand("Cancel");
				buttonPane.add(cancelButton);
			}
		}
	}

}
