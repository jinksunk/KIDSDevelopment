package net.strasnet.kids.gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.AbstractListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.ListModel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListDataListener;

import java.awt.GridBagLayout;
import javax.swing.JLabel;
import java.awt.GridBagConstraints;
import javax.swing.JTextField;
import java.awt.Insets;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;
import java.util.List;

import javax.swing.Action;
import javax.swing.ListSelectionModel;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;

public class addContextDialog extends JDialog {

	private final JPanel contentPanel = new JPanel();
	private JTextField contextNameTextField;
	private final Action addContextAction = new addContextAct(this);
	private final JList list = new JList();
	private KIDSAddEventOracle ko;
	private final JDialog us;

	public addContextDialog (KIDSAddEventOracle k){
		super();
		ko = k;
		addContextDialog();
		us = this;
	}

	/**
	 * Create the dialog.
	 */
	public void addContextDialog() {
		setBounds(100, 100, 450, 300);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		GridBagLayout gbl_contentPanel = new GridBagLayout();
		gbl_contentPanel.columnWidths = new int[]{0, 0, 0};
		gbl_contentPanel.rowHeights = new int[]{0, 0, 0, 0};
		gbl_contentPanel.columnWeights = new double[]{1.0, 1.0, Double.MIN_VALUE};
		gbl_contentPanel.rowWeights = new double[]{0.0, 0.0, 1.0, Double.MIN_VALUE};
		contentPanel.setLayout(gbl_contentPanel);
		{
			JLabel lblContextName = new JLabel("New Context Name");
			GridBagConstraints gbc_lblContextName = new GridBagConstraints();
			gbc_lblContextName.insets = new Insets(0, 0, 5, 5);
			gbc_lblContextName.anchor = GridBagConstraints.EAST;
			gbc_lblContextName.gridx = 0;
			gbc_lblContextName.gridy = 0;
			contentPanel.add(lblContextName, gbc_lblContextName);
		}
		{
			contextNameTextField = new JTextField();
			GridBagConstraints gbc_contextNameTextField = new GridBagConstraints();
			gbc_contextNameTextField.insets = new Insets(0, 0, 5, 0);
			gbc_contextNameTextField.fill = GridBagConstraints.HORIZONTAL;
			gbc_contextNameTextField.gridx = 1;
			gbc_contextNameTextField.gridy = 0;
			contentPanel.add(contextNameTextField, gbc_contextNameTextField);
			contextNameTextField.setColumns(10);
		}
		{
			JLabel lblContextClass = new JLabel("Context Class");
			GridBagConstraints gbc_lblContextClass = new GridBagConstraints();
			gbc_lblContextClass.insets = new Insets(0, 0, 5, 0);
			gbc_lblContextClass.gridwidth = 2;
			gbc_lblContextClass.gridx = 0;
			gbc_lblContextClass.gridy = 1;
			contentPanel.add(lblContextClass, gbc_lblContextClass);
		}
		{
			JScrollPane scrollPane = new JScrollPane();
			GridBagConstraints gbc_scrollPane = new GridBagConstraints();
			gbc_scrollPane.fill = GridBagConstraints.BOTH;
			gbc_scrollPane.gridwidth = 2;
			gbc_scrollPane.insets = new Insets(0, 0, 0, 5);
			gbc_scrollPane.gridx = 0;
			gbc_scrollPane.gridy = 2;
			contentPanel.add(scrollPane, gbc_scrollPane);
			{
				list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
				scrollPane.setViewportView(list);
			}
		}
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton okButton = new JButton("OK");
				okButton.setAction(addContextAction);
				okButton.setActionCommand("OK");
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
			}
			{
				JButton cancelButton = new JButton("Cancel");
				cancelButton.setActionCommand("Cancel");
				buttonPane.add(cancelButton);
				cancelButton.addActionListener(new AbstractAction(){

					@Override
					public void actionPerformed(ActionEvent arg0) {
						us.dispose();
					}
					
				});
			}
		}
		rebuildContextClasses();
	}

	/**
	 * Refresh the list of signal domain contexts from the knowledge base
	 */
	private void rebuildContextClasses() {
		final List<OWLClass> contextClasses = ko.getSignalDomainContextClasses();
		
		list.setModel(new AbstractListModel(){

			@Override
			public Object getElementAt(int index) {
				return ((OWLClass)contextClasses.get(index)).getIRI().getFragment();
			}

			@Override
			public int getSize() {
				return contextClasses.size();
			}
			
		});
	}

	private class addContextAct extends AbstractAction {
		private final addContextDialog pa;
		public addContextAct(addContextDialog p) {
			putValue(NAME, "addContext");
			putValue(SHORT_DESCRIPTION, "Some short description");
			pa = p;
		}
		public void actionPerformed(ActionEvent e) {
			String selectedContextClass = (String)list.getSelectedValue();
			
			ko.addContextToKB(ko.getOwlDataFactory().getOWLNamedIndividual(IRI.create(ko.getOurIRI().toString() + "#" + contextNameTextField.getText())), 
					          ko.getOwlDataFactory().getOWLClass(IRI.create(ko.getOurIRI().toString() + "#" + selectedContextClass)));
			
			pa.processWindowEvent(new WindowEvent(pa, WindowEvent.WINDOW_CLOSING));

		}
	}
}
