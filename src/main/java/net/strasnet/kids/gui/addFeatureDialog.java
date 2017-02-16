package net.strasnet.kids.gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.AbstractListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import java.awt.GridBagLayout;
import javax.swing.JLabel;
import java.awt.GridBagConstraints;
import javax.swing.JTextField;
import java.awt.Insets;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.LinkedList;
import java.util.List;

import javax.swing.Action;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLNamedIndividual;

public class addFeatureDialog extends JDialog {

	private final JPanel contentPanel = new JPanel();
	private final Action addFeatureAct = new addFeatureAction(this);
	private final Action addContextAct = new addContextAction();
	private final JTextField txtFeaturenamefield = new JTextField();
	private final JList list = new JList();
	private KIDSAddEventOracle ko;


	/**
	 * Create the dialog.
	 */
	public addFeatureDialog(KIDSAddEventOracle k) {
		ko = k;
		
		setBounds(100, 100, 450, 300);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		GridBagLayout gbl_contentPanel = new GridBagLayout();
		gbl_contentPanel.columnWidths = new int[]{0, 0, 0};
		gbl_contentPanel.rowHeights = new int[]{0, 0, 0, 0, 0};
		gbl_contentPanel.columnWeights = new double[]{1.0, 1.0, Double.MIN_VALUE};
		gbl_contentPanel.rowWeights = new double[]{0.0, 0.0, 1.0, 0.0, Double.MIN_VALUE};
		contentPanel.setLayout(gbl_contentPanel);
		{
			JLabel lblFeatureName = new JLabel("Feature Name:");
			GridBagConstraints gbc_lblFeatureName = new GridBagConstraints();
			gbc_lblFeatureName.insets = new Insets(0, 0, 5, 5);
			gbc_lblFeatureName.anchor = GridBagConstraints.EAST;
			gbc_lblFeatureName.gridx = 0;
			gbc_lblFeatureName.gridy = 0;
			contentPanel.add(lblFeatureName, gbc_lblFeatureName);
		}
		{
			GridBagConstraints gbc_txtFeaturenamefield = new GridBagConstraints();
			gbc_txtFeaturenamefield.insets = new Insets(0, 0, 5, 0);
			gbc_txtFeaturenamefield.fill = GridBagConstraints.HORIZONTAL;
			gbc_txtFeaturenamefield.gridx = 1;
			gbc_txtFeaturenamefield.gridy = 0;
			contentPanel.add(txtFeaturenamefield, gbc_txtFeaturenamefield);
			txtFeaturenamefield.setColumns(10);
		}
		{
			JLabel lblAssociatedContexts = new JLabel("Associated Contexts:");
			GridBagConstraints gbc_lblAssociatedContexts = new GridBagConstraints();
			gbc_lblAssociatedContexts.insets = new Insets(0, 0, 5, 0);
			gbc_lblAssociatedContexts.gridwidth = 2;
			gbc_lblAssociatedContexts.gridx = 0;
			gbc_lblAssociatedContexts.gridy = 1;
			contentPanel.add(lblAssociatedContexts, gbc_lblAssociatedContexts);
		}
		{
			JScrollPane scrollPane = new JScrollPane();
			GridBagConstraints gbc_scrollPane = new GridBagConstraints();
			gbc_scrollPane.fill = GridBagConstraints.BOTH;
			gbc_scrollPane.gridwidth = 2;
			gbc_scrollPane.insets = new Insets(0, 0, 5, 5);
			gbc_scrollPane.gridx = 0;
			gbc_scrollPane.gridy = 2;
			contentPanel.add(scrollPane, gbc_scrollPane);
			{
				scrollPane.setViewportView(list);
			}
		}
		{
			JButton btnAddContext = new JButton("Add Context...");
			btnAddContext.setAction(addContextAct);
			GridBagConstraints gbc_btnAddContext = new GridBagConstraints();
			gbc_btnAddContext.insets = new Insets(0, 0, 0, 5);
			gbc_btnAddContext.gridx = 0;
			gbc_btnAddContext.gridy = 3;
			contentPanel.add(btnAddContext, gbc_btnAddContext);
		}
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton okButton = new JButton("OK");
				okButton.setAction(addFeatureAct);
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
		rebuildSignalDomainContextList();
	}

	private class addFeatureAction extends AbstractAction {
		private final addFeatureDialog pa;
		public addFeatureAction(addFeatureDialog p) {
			putValue(NAME, "AddFeature");
			putValue(SHORT_DESCRIPTION, "Some short description");
			pa = p;
		}
		/**
		 * Add the feature to the knowledge base, associating it with each
		 * selected context.
		 */
		public void actionPerformed(ActionEvent e) {
			List <OWLNamedIndividual> selectedContexts = new LinkedList<OWLNamedIndividual>();
			Object[] cStrings = list.getSelectedValues();
			for (int i = 0; i < cStrings.length; i++){
				selectedContexts.add((OWLNamedIndividual) cStrings[i]);
			}
			
			ko.addFeatureToKB(ko.getOwlDataFactory().getOWLNamedIndividual(IRI.create(ko.getABOXIRI().toString() + "#" + txtFeaturenamefield.getText())), 
					          ko.getOwlDataFactory().getOWLClass(IRI.create(ko.getTBOXIRI().toString() + "#" + "SignalDomain")),
					          selectedContexts);
			
			pa.processWindowEvent(new WindowEvent(pa, WindowEvent.WINDOW_CLOSING));

		}
	}
	
	private class addContextAction extends AbstractAction {
		public addContextAction() {
			putValue(NAME, "addNewContext...");
			putValue(SHORT_DESCRIPTION, "Some short description");
		}
		public void actionPerformed(ActionEvent e) {
			try {
				final addContextDialog dialog = new addContextDialog(ko);
				dialog.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
				dialog.addWindowListener(new WindowAdapter() {
					public void windowClosing(WindowEvent we){
						rebuildSignalDomainContextList();
						dialog.dispose();
					}
				});				
				dialog.setVisible(true);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}
	
	/**
	 * Refresh the list of known contexts in the knowledgebase:
	 */
	private void rebuildSignalDomainContextList(){
		// Create a new list model:
		final List<OWLNamedIndividual> contexts = ko.getSignalDomainContexts();
		
		// For each signal, update the signal domain box list with the IRI:
		list.setModel(new AbstractListModel() {
			java.util.List<OWLNamedIndividual> values = contexts;
			public int getSize() {
				return values.size();
			}
			public Object getElementAt(int index) {
				return values.get(index);
			}
		});
	}
}
