/**
 * 
 */
package net.strasnet.kids.gui;
import javax.swing.*;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.util.SimpleIRIMapper;

import net.strasnet.kids.KIDSOracle;

import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.LinkedList;
import java.awt.Panel;
import java.awt.List;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.beans.PropertyChangeListener;
import java.awt.event.ActionListener;

/**
 * @author chrisstrasburg
 *  Interface for a user to add an event.  The general workflow is:
 *   1) Query the knowledge base for known signal types.
 *   2) Create an "addSignal" interface for each known signal type.
 *   3) For each signal, identify the valid features the signal can be applied to.
 *   4) For each feature, identify the valid contexts the feature can be present in. 
 *   
 *   Initially, the following menu items exist:
 *   File -> OpenKB [IRI or File]
 *   File -> SaveKB [File]
 *   Edit -> AddEvent
 *   Edit -> AddIDS
 *   Tools -> GenerateIDSSignature
 */
public class addEventPanel {

	private final JFrame mainWindow;
	private final Action loadKBAction = new loadKBAct();
	private KIDSAddEventOracle ko;
	private final Action saveKBAction = new saveKBAct();
	private final Action addEventAction = new addEventAct();
	private final Action addSignalAction = new addSignalAct();
	/**
	 * @wbp.nonvisual location=53,191
	 */
	private final Panel panel = new Panel();
	private final JLabel lblCurrentEvent = new JLabel();
	private final JLabel lblKbloadiri;
	private final JLabel lblCurrentkbiri;
	private final JList signalList = new JList();
	private final Action getSyntacticFormAction = new getSFormAct();
	public static final String OntologyLocation = "http://solomon.cs.iastate.edu/ontologies/KIDS.owl";

	public addEventPanel (){
		
		ko = new KIDSAddEventOracle();
		try{
			ko.loadKIDS(IRI.create(OntologyLocation), null);
		} catch (Exception e){
			// If the ontology is not available, just don't load it
			System.err.println("Warning: could not load from " + OntologyLocation);
		}
		mainWindow = new JFrame();
		//mainWindow.setPreferredSize(new Dimension(500, 500));
		mainWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		mainWindow.setTitle("KIDS Event Representation Inferface");
		
		JMenuBar menuBar = new JMenuBar();
		mainWindow.setJMenuBar(menuBar);
		
		JMenu mnFile = new JMenu("File");
		menuBar.add(mnFile);
		
		JMenuItem mntmOpenkb = new JMenuItem("OpenKB...");
		mntmOpenkb.setAction(loadKBAction);
		mnFile.add(mntmOpenkb);
		
		JMenuItem mntmSavekb = new JMenuItem("SaveKB...");
		mntmSavekb.setAction(saveKBAction);
		mnFile.add(mntmSavekb);
		
		JMenu mnEdit = new JMenu("Edit");
		menuBar.add(mnEdit);
		
		JMenuItem mntmAddEvent = new JMenuItem("Add Event...");
		mntmAddEvent.setAction(addEventAction);
		mnEdit.add(mntmAddEvent);
		
		JMenuItem mntmAddIds = new JMenuItem("Add IDS...");
		mnEdit.add(mntmAddIds);
		
		JMenu mnTools = new JMenu("Tools");
		menuBar.add(mnTools);
		
		JMenuItem mntmGenerateIdsSignature = new JMenuItem("Generate IDS Signature...");
		mntmGenerateIdsSignature.setAction(getSyntacticFormAction);
		mnTools.add(mntmGenerateIdsSignature);
		
		mainWindow.getContentPane().add(panel);
		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[]{110, 155, 172,0};
		gbl_panel.rowHeights = new int[]{0, 0, 0, 0, 0, 156, 0, 0};
		gbl_panel.columnWeights = new double[]{0.0, 0.0, 0.0, Double.MIN_VALUE};
		gbl_panel.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		panel.setLayout(gbl_panel);
		
		JLabel lblCurrentKbLoad = new JLabel("Current KB Load Location:");
		GridBagConstraints gbc_lblCurrentKbLoad = new GridBagConstraints();
		gbc_lblCurrentKbLoad.anchor = GridBagConstraints.EAST;
		gbc_lblCurrentKbLoad.insets = new Insets(0, 0, 5, 5);
		gbc_lblCurrentKbLoad.gridx = 0;
		gbc_lblCurrentKbLoad.gridy = 0;
		panel.add(lblCurrentKbLoad, gbc_lblCurrentKbLoad);
		
		if (ko.getOntology() != null){
			lblKbloadiri = new JLabel(ko.getOntologyManager().getOntologyDocumentIRI(ko.getOntology()).toString());
		} else {
			lblKbloadiri = new JLabel("No Ontology Loaded!");
		}
			
		GridBagConstraints gbc_lblKbloadiri = new GridBagConstraints();
		gbc_lblKbloadiri.anchor = GridBagConstraints.WEST;
		gbc_lblKbloadiri.insets = new Insets(0, 0, 5, 5);
		gbc_lblKbloadiri.gridx = 1;
		gbc_lblKbloadiri.gridy = 0;
		panel.add(lblKbloadiri, gbc_lblKbloadiri);
		
		JLabel lblCurrentKbIri = new JLabel("Current KB IRI:");
		GridBagConstraints gbc_lblCurrentKbIri = new GridBagConstraints();
		gbc_lblCurrentKbIri.anchor = GridBagConstraints.EAST;
		gbc_lblCurrentKbIri.insets = new Insets(0, 0, 5, 5);
		gbc_lblCurrentKbIri.gridx = 0;
		gbc_lblCurrentKbIri.gridy = 1;
		panel.add(lblCurrentKbIri, gbc_lblCurrentKbIri);
		
		if (ko.getOntology() != null){
			lblCurrentkbiri = new JLabel(ko.getOntology().toString());
		} else {
			lblCurrentkbiri = new JLabel("No Ontology Loaded!");
		}
		GridBagConstraints gbc_lblCurrentkbiri = new GridBagConstraints();
		gbc_lblCurrentkbiri.anchor = GridBagConstraints.WEST;
		gbc_lblCurrentkbiri.insets = new Insets(0, 0, 5, 5);
		gbc_lblCurrentkbiri.gridx = 1;
		gbc_lblCurrentkbiri.gridy = 1;
		panel.add(lblCurrentkbiri, gbc_lblCurrentkbiri);
		
		JLabel lblEventName = new JLabel("Event Name:");
		GridBagConstraints gbc_lblEventName = new GridBagConstraints();
		gbc_lblEventName.insets = new Insets(0, 0, 5, 5);
		gbc_lblEventName.anchor = GridBagConstraints.EAST;
		gbc_lblEventName.gridx = 0;
		gbc_lblEventName.gridy = 2;
		panel.add(lblEventName, gbc_lblEventName);
		
		GridBagConstraints gbc_lblCurrentEvent = new GridBagConstraints();
		gbc_lblCurrentEvent.anchor = GridBagConstraints.WEST;
		gbc_lblCurrentEvent.insets = new Insets(0, 0, 5, 5);
		gbc_lblCurrentEvent.gridx = 1;
		gbc_lblCurrentEvent.gridy = 2;
		panel.add(lblCurrentEvent, gbc_lblCurrentEvent);
		
		JSeparator separator = new JSeparator();
		GridBagConstraints gbc_separator = new GridBagConstraints();
		gbc_separator.gridwidth = 3;
		gbc_separator.insets = new Insets(0, 0, 5, 0);
		gbc_separator.gridx = 0;
		gbc_separator.gridy = 3;
		panel.add(separator, gbc_separator);
		
		JLabel lblSignals = new JLabel("Signals");
		GridBagConstraints gbc_lblSignals = new GridBagConstraints();
		gbc_lblSignals.insets = new Insets(0, 0, 5, 5);
		gbc_lblSignals.gridx = 1;
		gbc_lblSignals.gridy = 4;
		panel.add(lblSignals, gbc_lblSignals);
		
		JScrollPane scrollPane = new JScrollPane();
		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
		gbc_scrollPane.gridwidth = 3;
		gbc_scrollPane.fill = GridBagConstraints.BOTH;
		gbc_scrollPane.insets = new Insets(0, 0, 5, 0);
		gbc_scrollPane.gridx = 0;
		gbc_scrollPane.gridy = 5;
		panel.add(scrollPane, gbc_scrollPane);
		
		scrollPane.setViewportView(signalList);
		
		JButton btnAddsignal = new JButton("AddSignal");
		btnAddsignal.setAction(addSignalAction);
		btnAddsignal.setText("AddSignal...");
		GridBagConstraints gbc_btnAddsignal = new GridBagConstraints();
		gbc_btnAddsignal.insets = new Insets(0, 0, 0, 5);
		gbc_btnAddsignal.gridx = 1;
		gbc_btnAddsignal.gridy = 6;
		panel.add(btnAddsignal, gbc_btnAddsignal);
	}
	
    private void createAndShowGUI() {
 
        //Display the window.
        mainWindow.pack();
        mainWindow.setVisible(true);
    }
 
    public static void main(String[] args) {
        //Schedule a job for the event dispatch thread:
        //creating and showing this application's GUI.
    	final addEventPanel myC = new addEventPanel();
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                //Turn off metal's use of bold fonts
                UIManager.put("swing.boldMetal", Boolean.FALSE);
                myC.createAndShowGUI();
            }
        });
    }

	private static void addPopup(Component component, final JPopupMenu popup) {
		component.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				if (e.isPopupTrigger()) {
					showMenu(e);
				}
			}
			public void mouseReleased(MouseEvent e) {
				if (e.isPopupTrigger()) {
					showMenu(e);
				}
			}
			private void showMenu(MouseEvent e) {
				popup.show(e.getComponent(), e.getX(), e.getY());
			}
		});
	}
	
	private class loadKBAct extends AbstractAction {
		private static final long serialVersionUID = 1L;
		public loadKBAct() {
			putValue(NAME, "OpenKB");
			putValue(SHORT_DESCRIPTION, "Initializes the specified knowledge base.");
		}
		public void actionPerformed(ActionEvent e) {
			JFileChooser fc = new JFileChooser();
			int returnVal = fc.showOpenDialog(mainWindow);
			 
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File KBFile = fc.getSelectedFile();
                //This is where a real application would open the file.
                SimpleIRIMapper m = new SimpleIRIMapper(IRI.create(OntologyLocation), IRI.create(KBFile));
                LinkedList<SimpleIRIMapper> inList = new LinkedList<SimpleIRIMapper>();
                inList.add(m);
                try {
					ko.loadKIDS(IRI.create(OntologyLocation), inList);
					lblKbloadiri.setText(ko.getOntologyManager().getOntologyDocumentIRI(ko.getOntology()).toString());
					lblCurrentkbiri.setText(ko.getOntology().toString());
	                System.out.println("Loaded KB!");
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
        		createAndShowGUI();
            }		
		}
	}
	private class saveKBAct extends AbstractAction {
		public saveKBAct() {
			putValue(NAME, "SaveKB");
			putValue(SHORT_DESCRIPTION, "Save KB to local file.");
		}
		public void actionPerformed(ActionEvent e) {
			JFileChooser fc = new JFileChooser();
			int returnVal = fc.showSaveDialog(mainWindow);
			 
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File KBFile = fc.getSelectedFile();
                //This is where a real application would open the file.
    			try {
					ko.getOntologyManager().saveOntology(ko.getOntology(), new FileOutputStream(KBFile));
				} catch (OWLOntologyStorageException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (FileNotFoundException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
                System.out.println("Saved KB!");
            }
		}
	}
	private class addEventAct extends AbstractAction {
		public addEventAct() {
			putValue(NAME, "Set Event");
			putValue(SHORT_DESCRIPTION, "Describe an event in the knowledge base.");
		}
		public void actionPerformed(ActionEvent e) {
			// User is requesting to add a new event.  We need to "pop up" a new JDialog:
			final JDialog newEventDialog = new JDialog(mainWindow, "Set the current event",true);
			
			// Add a pane which will get the name of the new event and add it to the current knowledge base:
			Panel eventControls = new Panel();
			newEventDialog.getContentPane().add(eventControls);
			
			JLabel evtNameLabel = new JLabel("Event name (type to create new):");
			eventControls.add(evtNameLabel);
			
			final JComboBox evtList = new JComboBox(ko.getShortNames(ko.getEvents()));
			eventControls.add(evtList);
			evtList.setEditable(true);
			
			JButton createEventButton = new JButton();
			eventControls.add(createEventButton);
			
			createEventButton.setAction( new AbstractAction () {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					// Get the value of the text field and add a new individual to the knowledge base:
					ko.setEventIRI((String)evtList.getSelectedItem());
					lblCurrentEvent.setText((String)evtList.getSelectedItem());
					newEventDialog.dispose();
					
					// Reset the signals, features, and contexts
					updateSignalBox();
					createAndShowGUI();
				}

			});
			createEventButton.setText("Set Current Event");

			
			newEventDialog.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

			newEventDialog.pack();
			newEventDialog.setVisible(true);

		}
	}

	private class addSignalAct extends AbstractAction {
			public void actionPerformed(ActionEvent arg0) {
				
				// Create a dialog box which lists known signal classes, allowing the user to 
				// choose one.
				// User is requesting to add a new event.  We need to "pop up" a new JDialog:
				final JDialog newEventDialog = new addSignalDialog(ko);
				
				newEventDialog.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
				newEventDialog.addWindowListener(new WindowAdapter() {
					public void windowClosing(WindowEvent we){
						updateSignalBox();
						newEventDialog.dispose();
						createAndShowGUI();
					}
				});

				newEventDialog.pack();
				newEventDialog.setVisible(true);
				
				// Rebuild our stuff:
				//updateSignalBox();
			}
	}
	/**
	 * Updates the signal list to reflect what is in the knowledge base for the currently defined Event:
	 */
	protected void updateSignalBox() {
		final java.util.List<OWLNamedIndividual> signals = ko.getEventSignals();
		
		// For each signal, update the signal box list with the IRI:
		signalList.setModel(new AbstractListModel() {
			java.util.List<OWLNamedIndividual> values = signals;
			public int getSize() {
				return values.size();
			}
			public Object getElementAt(int index) {
				return values.get(index);
			}
		});
		
		// When a signal is selected, display the features the signal is associated with
		
		// When a feature is selected, display the contexts the feature is associated with
		
		
	}
	private class getSFormAct extends AbstractAction {
		public getSFormAct() {
			putValue(NAME, "Generate IDS Signature...");
			putValue(SHORT_DESCRIPTION, "Some short description");
		}
		public void actionPerformed(ActionEvent e) {
			final JDialog sFormDialog = new getSyntacticForm(ko);
			
			sFormDialog.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

			sFormDialog.pack();
			sFormDialog.setVisible(true);
			
		}
	}
}
