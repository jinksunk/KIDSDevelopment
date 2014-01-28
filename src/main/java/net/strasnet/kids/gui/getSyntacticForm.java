package net.strasnet.kids.gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.AbstractAction;
import javax.swing.AbstractListModel;
import javax.swing.Action;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.AncestorListener;

import java.awt.GridBagLayout;
import javax.swing.JLabel;
import java.awt.GridBagConstraints;
import javax.swing.JComboBox;
import java.awt.Insets;
import javax.swing.JTextArea;

import org.semanticweb.owlapi.model.OWLNamedIndividual;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

public class getSyntacticForm extends JDialog implements ActionListener {

	private final JPanel contentPanel = new JPanel();
	private final JComboBox comboBox = new JComboBox();
	private final JTextArea textArea = new JTextArea();
	private final KIDSAddEventOracle ko;

	/**
	 * Create the dialog.
	 */
	public getSyntacticForm(KIDSAddEventOracle k) {
		ko = k;
		
		setBounds(100, 100, 450, 300);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		GridBagLayout gbl_contentPanel = new GridBagLayout();
		gbl_contentPanel.columnWidths = new int[]{40, 0, 0};
		gbl_contentPanel.rowHeights = new int[]{0, 0, 0};
		gbl_contentPanel.columnWeights = new double[]{1.0, 1.0, Double.MIN_VALUE};
		gbl_contentPanel.rowWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		contentPanel.setLayout(gbl_contentPanel);
		{
			JLabel lblSelectIds = new JLabel("Select IDS");
			lblSelectIds.setMinimumSize(new Dimension(40, 16));
			GridBagConstraints gbc_lblSelectIds = new GridBagConstraints();
			gbc_lblSelectIds.insets = new Insets(0, 0, 5, 5);
			gbc_lblSelectIds.anchor = GridBagConstraints.EAST;
			gbc_lblSelectIds.gridx = 0;
			gbc_lblSelectIds.gridy = 0;
			contentPanel.add(lblSelectIds, gbc_lblSelectIds);
		}
		{
			GridBagConstraints gbc_comboBox = new GridBagConstraints();
			gbc_comboBox.insets = new Insets(0, 0, 5, 0);
			gbc_comboBox.fill = GridBagConstraints.HORIZONTAL;
			gbc_comboBox.gridx = 1;
			gbc_comboBox.gridy = 0;
			contentPanel.add(comboBox, gbc_comboBox);
			comboBox.addActionListener(this);
		}
		{
			GridBagConstraints gbc_textArea = new GridBagConstraints();
			gbc_textArea.gridwidth = 2;
			gbc_textArea.insets = new Insets(0, 0, 0, 5);
			gbc_textArea.fill = GridBagConstraints.BOTH;
			gbc_textArea.gridx = 0;
			gbc_textArea.gridy = 1;
			contentPanel.add(textArea, gbc_textArea);
		}
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton okButton = new JButton("OK");
				okButton.setActionCommand("OK");
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
			}
			{
				JButton cancelButton = new JButton("Cancel");
				cancelButton.setActionCommand("Cancel");
				buttonPane.add(cancelButton);
			}
		}
		rebuildIDSComboBox();
	}
	
	/**
	 * Get the list of IDSes in the KB and populated the combo box with them:
	 */
	private void rebuildIDSComboBox(){
		final List<OWLNamedIndividual> IDSes = ko.getIDSes();
		
		comboBox.setModel(new DefaultComboBoxModel() {
			@Override
			public Object getElementAt(int index){
				return IDSes.get(index);
			}
			
			@Override
			public int getSize(){
				return IDSes.size();
			}
		});
		
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// Get the IDS selected:
		OWLNamedIndividual cf = (OWLNamedIndividual)comboBox.getSelectedItem();
		OWLNamedIndividual sForm;
		
		// If the IDS has more than one associated syntactic form, ask the user to select the form.
		// Otherwise, just take the default.
		List<OWLNamedIndividual> synforms = ko.getSynforms(cf);
		if (synforms.size() > 1){
			// Prompt for specific detector
			sForm = null;
		} else {
			sForm = synforms.get(0);
		}
		
		// Ask the KO for the syntactic form:
		String sf = ko.getSyntacticForm(sForm);
		
		// Update the text box with the correct form:
		textArea.setText(sf);
	}

}
