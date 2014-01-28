package net.strasnet.kids.gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import java.awt.GridBagLayout;
import javax.swing.JList;
import java.awt.GridBagConstraints;
import java.awt.Insets;

import javax.swing.AbstractListModel;
import javax.swing.JFrame;
import javax.swing.JTextArea;
import javax.swing.JScrollPane;
import javax.swing.JLabel;
import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.Action;

import net.strasnet.kids.KIDSCanonicalRepresentation;
import net.strasnet.kids.KIDSOntologyDatatypeValuesException;
import net.strasnet.kids.KIDSOntologyObjectValuesException;
import net.strasnet.kids.signalRepresentations.KIDSRepresentationIncompatibleValueException;
import net.strasnet.kids.signalRepresentations.KIDSRepresentationInvalidRepresentationValueException;

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import javax.swing.ListSelectionModel;

public class addSignalDialog extends JDialog {

	private final JPanel contentPanel = new JPanel();
	private final Action action = new addSignalAction(this);
	private final JTextArea textArea = new JTextArea();
	private final JList signalSubclassList = new JList();
	private final JList signalRepresentationList = new JList();
	private final JList signalDomainList = new JList();
	private static final String representationClassDataProperty = "#signalRepresentationLibrary";
	private final addSignalDialog me = this;

	private KIDSAddEventOracle ko;
	private addEventPanel p;
	private final Action action_1 = new addSigDomainAct();
	
	/**
	 * Create the dialog.
	 */
	public addSignalDialog(KIDSAddEventOracle myko) {
		ko = myko;
		
		setBounds(100, 100, 647, 360);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		GridBagLayout gbl_contentPanel = new GridBagLayout();
		gbl_contentPanel.columnWidths = new int[]{0, 0, 0, 0};
		gbl_contentPanel.rowHeights = new int[]{0, 0, 0, 0, 0};
		gbl_contentPanel.columnWeights = new double[]{1.0, 1.0, 1.0, Double.MIN_VALUE};
		gbl_contentPanel.rowWeights = new double[]{0.0, 1.0, 0.0, 1.0, Double.MIN_VALUE};
		contentPanel.setLayout(gbl_contentPanel);
		{
			JLabel lblSignalRepresentation = new JLabel("Signal Representation");
			GridBagConstraints gbc_lblSignalRepresentation = new GridBagConstraints();
			gbc_lblSignalRepresentation.insets = new Insets(0, 0, 5, 5);
			gbc_lblSignalRepresentation.gridx = 0;
			gbc_lblSignalRepresentation.gridy = 0;
			contentPanel.add(lblSignalRepresentation, gbc_lblSignalRepresentation);
		}
		{
			JLabel label = new JLabel("Signal Domain");
			GridBagConstraints gbc_label = new GridBagConstraints();
			gbc_label.insets = new Insets(0, 0, 5, 5);
			gbc_label.gridx = 1;
			gbc_label.gridy = 0;
			contentPanel.add(label, gbc_label);
		}
		{
			JLabel lblKnownSignalSubclasses = new JLabel("Known Signal Subclasses");
			GridBagConstraints gbc_lblKnownSignalSubclasses = new GridBagConstraints();
			gbc_lblKnownSignalSubclasses.insets = new Insets(0, 0, 5, 0);
			gbc_lblKnownSignalSubclasses.gridx = 2;
			gbc_lblKnownSignalSubclasses.gridy = 0;
			contentPanel.add(lblKnownSignalSubclasses, gbc_lblKnownSignalSubclasses);
		}
		{
			JScrollPane scrollPane = new JScrollPane();
		GridBagConstraints gbc_signalSubclassList = new GridBagConstraints();
		gbc_signalSubclassList.fill = GridBagConstraints.BOTH;
		gbc_signalSubclassList.insets = new Insets(0, 0, 5, 0);
		gbc_signalSubclassList.gridx = 2;
		gbc_signalSubclassList.gridy = 1;
		contentPanel.add(scrollPane, gbc_signalSubclassList);
		{
			signalSubclassList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			scrollPane.setViewportView(signalSubclassList);
		}
		}
		{
			JScrollPane scrollPane = new JScrollPane();
			GridBagConstraints gbc_scrollPane = new GridBagConstraints();
			gbc_scrollPane.fill = GridBagConstraints.BOTH;
			gbc_scrollPane.insets = new Insets(0, 0, 5, 5);
			gbc_scrollPane.gridx = 0;
			gbc_scrollPane.gridy = 1;
			contentPanel.add(scrollPane, gbc_scrollPane);
			{
				scrollPane.setViewportView(signalRepresentationList);
			}
		}
		{
			JScrollPane scrollPane = new JScrollPane();
			GridBagConstraints gbc_scrollPane = new GridBagConstraints();
			gbc_scrollPane.fill = GridBagConstraints.BOTH;
			gbc_scrollPane.insets = new Insets(0, 0, 5, 5);
			gbc_scrollPane.gridx = 1;
			gbc_scrollPane.gridy = 1;
			contentPanel.add(scrollPane, gbc_scrollPane);
			{
				scrollPane.setViewportView(signalDomainList);
			}
		}
		{
			JLabel lblCanonicalsignaldescription = new JLabel("CanonicalSignalDescription");
			GridBagConstraints gbc_lblCanonicalsignaldescription = new GridBagConstraints();
			gbc_lblCanonicalsignaldescription.insets = new Insets(0, 0, 5, 5);
			gbc_lblCanonicalsignaldescription.gridx = 0;
			gbc_lblCanonicalsignaldescription.gridy = 2;
			contentPanel.add(lblCanonicalsignaldescription, gbc_lblCanonicalsignaldescription);
		}
		{
			JButton btnAddsignaldomain = new JButton("AddSignalDomain");
			btnAddsignaldomain.setAction(action_1);
			GridBagConstraints gbc_btnAddsignaldomain = new GridBagConstraints();
			gbc_btnAddsignaldomain.insets = new Insets(0, 0, 5, 5);
			gbc_btnAddsignaldomain.gridx = 1;
			gbc_btnAddsignaldomain.gridy = 2;
			contentPanel.add(btnAddsignaldomain, gbc_btnAddsignaldomain);
		}
		{
			JScrollPane scrollPane = new JScrollPane();
			GridBagConstraints gbc_scrollPane = new GridBagConstraints();
			gbc_scrollPane.fill = GridBagConstraints.BOTH;
			gbc_scrollPane.gridwidth = 2;
			gbc_scrollPane.insets = new Insets(0, 0, 0, 5);
			gbc_scrollPane.gridx = 0;
			gbc_scrollPane.gridy = 3;
			contentPanel.add(scrollPane, gbc_scrollPane);
			{
				scrollPane.setViewportView(textArea);
			}
		}
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton okButton = new JButton("OK");
				okButton.setAction(action);
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
			}
			{
				JButton cancelButton = new JButton("Cancel");
				cancelButton.setActionCommand("Cancel");
				buttonPane.add(cancelButton);
			}
		}
		rebuildSignalClassList();
		rebuildSignalRepresentationClassList();
		rebuildSignalDomainList();
	}

	private class addSignalAction extends AbstractAction {
		addSignalDialog pa;
		public addSignalAction(addSignalDialog parent) {
			putValue(NAME, "addSignalAction");
			putValue(SHORT_DESCRIPTION, "Add the described signal to the knowledge base.");
			pa = parent;
		}
		public void actionPerformed(ActionEvent e) {
			// First, get the signal class and signal representation:
			String canonicalDescription = textArea.getText();
			OWLNamedIndividual ourRep = ko.getOWLNamedIndividualFromShortname((String) signalRepresentationList.getSelectedValue());
			OWLNamedIndividual sd = ko.getOWLNamedIndividualFromShortname((String) signalDomainList.getSelectedValue());
			
			//Validate the canonical representation:
			// We need a library or something here... 
			KIDSCanonicalRepresentation scr = null;
			try {
				scr = ko.getCanonicalRepresentation(ourRep, null);
				scr.setValue(canonicalDescription);
			} catch (KIDSOntologyObjectValuesException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (KIDSRepresentationInvalidRepresentationValueException e2) {
				// Thrown if the given input is not compatible with the representation.
				// TODO Auto-generated catch block
				e2.printStackTrace();
			} catch (KIDSOntologyDatatypeValuesException e3) {
				// TODO Auto-generated catch block
				e3.printStackTrace();
			}
			
			// Add the signal:
			ko.addSignalToEvent(ourRep, sd, scr);
			pa.processWindowEvent(new WindowEvent(me, WindowEvent.WINDOW_CLOSING));
		}
	}
	
	/**
	 * Set a new list model including all known Signal subclassess in the KB
	 *
	private void rebuildSignalClassList () {
		// Create a new model:
		final java.util.List<OWLClass> signalClasses = ko.getSignalSubClasses();
	}
	*/
	
	/**
	 * Set a new list model including all known Signal subclassess in the KB
	 */
	private void rebuildSignalClassList () {
		// Create a new model:
		final java.util.List<OWLClass> signalSubclasses = ko.getSignalSubClasses();
		
		// For each signal, update the signal box list with the IRI:
		signalSubclassList.setModel(new AbstractListModel() {
			java.util.List<OWLClass> values = signalSubclasses;
			public int getSize() {
				return values.size();
			}
			public Object getElementAt(int index) {
				return ((OWLClass)values.get(index)).getIRI().getFragment();
			}
		});
	}
	
	/**
	 * Set a new list model including all known Signal canonical representations in the KB
	 */
	private void rebuildSignalRepresentationClassList () {
		// Create a new model:
		final java.util.List<OWLNamedIndividual> signalRepresentations = ko.getSignalRepresentations();
		
		// For each signal, update the signal box list with the IRI:
		signalRepresentationList.setModel(new AbstractListModel() {
			java.util.List<OWLNamedIndividual> values = signalRepresentations;
			public int getSize() {
				return values.size();
			}
			public Object getElementAt(int index) {
				return ((OWLNamedIndividual)values.get(index)).getIRI().getFragment();
			}
		});
	}
	
	
	/**
	 * Set a new list model including all known SignalDomain subclassess in the KB
	 */
	private void rebuildSignalDomainList () {
		// Create a new model:
		final java.util.List<OWLNamedIndividual> signalDomains = ko.getSignalDomains();
		signalDomainList.setModel(new AbstractListModel() {
			java.util.List<OWLNamedIndividual> values = signalDomains;
			public int getSize() {
				return values.size();
			}
			public Object getElementAt(int index) {
				return ((OWLNamedIndividual)values.get(index)).getIRI().getFragment();
			}
		}
		);
	}
	
	private class addSigDomainAct extends AbstractAction {
		public addSigDomainAct() {
			putValue(NAME, "addSignalDomain...");
			putValue(SHORT_DESCRIPTION, "Some short description");
		}
		public void actionPerformed(ActionEvent e) {
			try {
				final addFeatureDialog dialog = new addFeatureDialog(ko);
				dialog.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
				dialog.addWindowListener(new WindowAdapter() {
					public void windowClosing(WindowEvent we){
						rebuildSignalDomainList();
						dialog.dispose();
					}
				});				
				dialog.setVisible(true);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}
}
