package net.strasnet.kids.ui.gui;

import java.awt.Dialog;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import java.awt.GridBagLayout;

import javax.swing.JTabbedPane;

import java.awt.GridBagConstraints;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JList;
import javax.swing.JSplitPane;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.DefaultMutableTreeNode;

import net.strasnet.kids.ui.KIDSUIAddRelationPossibleFix;
import net.strasnet.kids.ui.KIDSUIPossibleFix;
import net.strasnet.kids.ui.KIDSUIPossibleFix.KIDSUIPossibleFixType;
import net.strasnet.kids.ui.KIDSUIProblem;
import net.strasnet.kids.ui.KIDSUIProblem.ProblemClass;
import net.strasnet.kids.ui.KIDSMissingRelationUIProblem;
import net.strasnet.kids.ui.gui.ABOXBuilderModel.ABOXBuilderGUIState;
import net.strasnet.kids.ui.gui.alerts.KIDSGUIAlert;
import net.strasnet.kids.ui.gui.alerts.KIDSGUIAlertError;
import net.strasnet.kids.ui.gui.alerts.KIDSGUIAlertInfo;

import org.apache.logging.log4j.LogManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import javax.swing.JLabel;
import java.awt.Insets;
import javax.swing.JTextPane;
import java.awt.Color;
import java.awt.Component;

import javax.swing.DefaultListModel;
import javax.swing.DropMode;
import javax.swing.JComponent;
import javax.swing.JDialog;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JPopupMenu;

public class ABOXBuilder {

	public static final org.apache.logging.log4j.Logger logme = LogManager.getLogger(ABOXBuilder.class.getName());

	private JFrame frame;
	private JTextPane logLines; // Make this visible to other methods.
	private ABOXBuilderController controller;
	private ABOXBuilderModel model;
	private ArrayBlockingQueue<KIDSGUIAlert> logMessages;
	private ABOXBuilderState myst8 = null;

	/**
	 * Launch the application.
	 * 
	 * TODO: Add arguments to:
	 *  * set the state file 
	 *  * control debugging
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					ABOXBuilder window = new ABOXBuilder();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public ABOXBuilder() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		logMessages = new ArrayBlockingQueue<KIDSGUIAlert>(100);
		controller = new ABOXBuilderController(logMessages);
		model = new ABOXBuilderModel(logMessages, controller);

		frame = new JFrame();
		myst8 = new ABOXBuilderState(frame);
		frame.addWindowListener(new WindowAdapter() {
			// Open a JDialog that prompts whether to save the ABOX or now; also write out configuration file
			// to save state for next session.
			@Override
			public void windowClosing(WindowEvent e) {
				int response = JOptionPane.showConfirmDialog(frame, "Save ABOX to file?", "Warning", JOptionPane.YES_NO_CANCEL_OPTION);
				if (response == JOptionPane.YES_OPTION){
					try {
						controller.save();
					} catch (OWLOntologyStorageException e1) {
						controller.logappend(new KIDSGUIAlertError(String.format("Uh-oh - couldn't save ABOX: %s",e1.getMessage())));
					}
				}
				
				if (response != JOptionPane.CANCEL_OPTION){
					frame.setVisible(false);
					frame.dispose();
					System.exit(0);
				}

			}
		});
		frame.setBounds(100, 100, 648, 406);
		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		
		JMenuBar menuBar = new JMenuBar();
		frame.setJMenuBar(menuBar);
		
		JMenu mnFile = new JMenu("File");
		menuBar.add(mnFile);
		
		JMenuItem mntmNew = new JMenuItem("New");
		mntmNew.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// Open a JDialog which prompts for TBOX location, IRI, and desired ABOX IRI; initiate a new ABOX
				NewABOXJDialog getStarted = new NewABOXJDialog(frame, myst8);
				getStarted.setModal(true);
				getStarted.setVisible(true);
				
				try {
					controller.initNew(getStarted.getTBOXLocationTextField(), 
								getStarted.getTBOXIRI(), 
								getStarted.getABOXLocationTextField(),
								getStarted.getABOXIRI());
					model.setState(ABOXBuilderGUIState.ABOXLoaded);
				} catch (OWLOntologyCreationException e1) {
					controller.logappend(new KIDSGUIAlertError("Uh-oh - an exception occurred when loading the ontology: " + e1.getMessage()));
				} catch (OWLOntologyStorageException e1) {
					controller.logappendError("Uh-oh - an exception occurred when writing the ontology: " + e1.getMessage());
				}
				processMessageQueue();
			}
		});
		mnFile.add(mntmNew);
		
		JMenuItem mntmOpen = new JMenuItem("Open");
		mntmOpen.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// Open a JDialog which prompts for TBOX location, IRI, and desired ABOX IRI; initiate a new ABOX
				OpenABOXJDialog getStarted = new OpenABOXJDialog(frame, myst8);
				getStarted.setModal(true);
				getStarted.setVisible(true);
				
				try {
					controller.initExisting(getStarted.getTBOXLocationTextField(), 
								getStarted.getTBOXIRI(), 
								getStarted.getABOXLocationTextField(),
								getStarted.getABOXIRI());
					model.setState(ABOXBuilderGUIState.ABOXLoaded);
				} catch (OWLOntologyCreationException e1) {
					controller.logappend(new KIDSGUIAlertError("Uh-oh - an exception occurred when loading the ontology: " + e1.getMessage()));
				}
				processMessageQueue();
			}
		});
		mnFile.add(mntmOpen);
		
		JMenuItem mntmSave = new JMenuItem("Save");
		mntmSave.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
					try {
						controller.logappend(new KIDSGUIAlertError(String.format(
								"Saving ontology...")));
						controller.save();
						controller.logappend(new KIDSGUIAlertError(String.format(
								"Done")));
						model.setState(ABOXBuilderGUIState.ABOXSAVED);
					} catch (OWLOntologyStorageException e1) {
						controller.logappend(new KIDSGUIAlertError(String.format("Uh-oh - couldn't save ABOX: %s",e1.getMessage())));
					} finally {
						processMessageQueue();
					}
			}
		});
		mnFile.add(mntmSave);
		
		JMenu mnEdit = new JMenu("Edit");
		menuBar.add(mnEdit);
		
		JMenuItem mntmAddEvent = new JMenuItem("Add Event");
		mntmAddEvent.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				if (model.getState() != ABOXBuilderGUIState.UNINITIALIZED){
					// Create the AddEventJDialog to create the event
					AddEventJDialog newEvent = new AddEventJDialog(frame, controller.getABOXPrefix(), controller);
					newEvent.setModal(true);
					newEvent.setVisible(true);
				
					controller.addEvent(newEvent.getAddedElementIRI());

					controller.logappendInfo("Added event: " + newEvent.getAddedElementIRI());
				} else {
					controller.logappendInfo("ABOX uninitialized; create new or open existing.");
				}

				processMessageQueue();
			}
		});
		mnEdit.add(mntmAddEvent);
		
		JMenuItem mntmAddSignal = new JMenuItem("Add Signal");
		mnEdit.add(mntmAddSignal);
		
		JMenu mnModelActions = new JMenu("Model Actions");
		menuBar.add(mnModelActions);
		
		JMenuItem mntmValidateModel = new JMenuItem("Validate Model");
		mnModelActions.add(mntmValidateModel);
		
		JMenu mnModelInfo = new JMenu("Model Info");
		menuBar.add(mnModelInfo);
		
		JMenuItem mntmGetAllIndividuals = new JMenuItem("Get all individuals");
		mntmGetAllIndividuals.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// Add all IRIs to the log window:
				Set <IRI> individuals = controller.getKnownIndividuals();
				for (IRI item : individuals){
					controller.logappendInfo(item.toString());
				}
			}
		});
		mnModelInfo.add(mntmGetAllIndividuals);
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0};
		gridBagLayout.rowHeights = new int[] {0, 0, 0, 0};
		gridBagLayout.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{1.0, Double.MIN_VALUE, 1.0, 1.0};
		frame.getContentPane().setLayout(gridBagLayout);
		
		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		GridBagConstraints gbc_tabbedPane = new GridBagConstraints();
		gbc_tabbedPane.insets = new Insets(0, 0, 5, 0);
		gbc_tabbedPane.fill = GridBagConstraints.BOTH;
		gbc_tabbedPane.gridx = 0;
		gbc_tabbedPane.gridy = 0;
		frame.getContentPane().add(tabbedPane, gbc_tabbedPane);
		
		JPanel EventPanel = new KIDSIndividualProblemsJPanel(ABOXBuilderController.EVENTCLASSIRI);
		tabbedPane.addTab("Events", null, EventPanel, null);
		
		JPanel DatasetPanel = new KIDSIndividualProblemsJPanel(ABOXBuilderController.DATASETCLASSIRI);
		tabbedPane.addTab("Datasets", null, DatasetPanel, null);

		JPanel SignalPanel = new KIDSIndividualProblemsJPanel(ABOXBuilderController.SIGNALCLASSIRI);
		tabbedPane.addTab("Signals", null, SignalPanel, null);
		
		JPanel TimePeriodPanel = new KIDSIndividualProblemsJPanel(ABOXBuilderController.TIMEPERIODCLASSIRI);
		tabbedPane.addTab("TimePeriods", null, TimePeriodPanel, null);
		
		JPanel DatasetViewPanel = new KIDSIndividualProblemsJPanel(ABOXBuilderController.DATASETVIEWCLASSIRI);
		tabbedPane.addTab("DatasetViews", null, DatasetViewPanel, null);

		JPanel DatasetLabelPanel = new KIDSIndividualProblemsJPanel(ABOXBuilderController.DATASETLABELCLASSIRI);
		tabbedPane.addTab("DatasetLabels", null, DatasetLabelPanel, null);
		
		JPanel DetectorPanel = new KIDSIndividualProblemsJPanel(ABOXBuilderController.DETECTORCLASSIRI);
		tabbedPane.addTab("Detectors", null, DetectorPanel, null);
		
		JPanel SignalManifestationPanel = new KIDSIndividualProblemsJPanel(ABOXBuilderController.SIGNALMANIFESTATIONCLASSIRI);
		tabbedPane.addTab("SignalManifestations", null, SignalManifestationPanel, null);

		JPanel SignalDomainRepresentationPanel = new KIDSIndividualProblemsJPanel(ABOXBuilderController.SIGNALDOMAINREPRESENTATIONCLASSIRI);
		tabbedPane.addTab("SignalDomainReps", null, SignalDomainRepresentationPanel, null);

		JPanel SignalDomainContextPanel = new KIDSIndividualProblemsJPanel(ABOXBuilderController.SIGNALDOMAINCONTEXTCLASSIRI);
		tabbedPane.addTab("SignalDomainContexts", null, SignalDomainContextPanel, null);
		
		JPanel SignalDomainPanel = new KIDSIndividualProblemsJPanel(ABOXBuilderController.SIGNALDOMAINCLASSIRI);
		tabbedPane.addTab("SignalDomains", null, SignalDomainPanel, null);
		
		JPanel SignalValuePanel = new KIDSIndividualProblemsJPanel(ABOXBuilderController.SIGNALVALUECLASSIRI);
		tabbedPane.addTab("SignalValues", null, SignalValuePanel, null);
		
		JPanel ResourcePanel = new KIDSIndividualProblemsJPanel(ABOXBuilderController.RESOURCECLASSIRI);
		tabbedPane.addTab("Resources", null, ResourcePanel, null);
		
		JPanel ResponsePanel = new KIDSIndividualProblemsJPanel(ABOXBuilderController.RESPONSECLASSIRI);
		tabbedPane.addTab("Responses", null, ResponsePanel, null);
		
		JLabel lblLogs = new JLabel("Logs");
		GridBagConstraints gbc_lblLogs = new GridBagConstraints();
		gbc_lblLogs.insets = new Insets(0, 0, 5, 0);
		gbc_lblLogs.gridx = 0;
		gbc_lblLogs.gridy = 1;
		frame.getContentPane().add(lblLogs, gbc_lblLogs);
		
		JScrollPane scrollPane = new JScrollPane();
		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
		gbc_scrollPane.insets = new Insets(0, 0, 5, 0);
		gbc_scrollPane.fill = GridBagConstraints.BOTH;
		gbc_scrollPane.gridx = 0;
		gbc_scrollPane.gridy = 2;
		frame.getContentPane().add(scrollPane, gbc_scrollPane);
		
		logLines = new JTextPane();
		scrollPane.setViewportView(logLines);
		logLines.setDropMode(DropMode.INSERT);
		logLines.setBackground(Color.GRAY);
		logLines.setForeground(new Color(0, 255, 0));
		logLines.setEditable(false);
		
		processMessageQueue();
	}
	
	/*
	 * Update the text box label with messages from the message queue.
	 * Will process entries until the queue is empty.
	 */
	private void processMessageQueue(){
		KIDSGUIAlert m = null;
		StringBuilder newText = new StringBuilder(logLines.getText());
		while ((m = this.logMessages.poll()) != null){
			newText.append(String.format("%s\n", m.toString()));
		}
		logLines.setText(newText.toString());
	}
	
	class KIDSProblemsJScrollPane extends JScrollPane{

		/**
		 * 
		 */
		private static final long serialVersionUID = -5930357945561781477L;

		public KIDSProblemsJScrollPane(final JList<IRI> individualsJList){

			final DefaultListModel<KIDSUIProblem> problemJListModel = new DefaultListModel<KIDSUIProblem>();
			final JList<KIDSUIProblem> ProblemsJList = new JList<KIDSUIProblem>(problemJListModel);
			ProblemsJList.addMouseListener(new MouseAdapter() {
				@Override
				public void mousePressed(MouseEvent e) {

					logme.debug(String.format("Mouse button %d clicked.", e.getButton()));
				

					// Only respond to right-clicks
					if (e.getButton() == MouseEvent.BUTTON3){
					
						// First, get the JList item that was clicked, if any:
						final IRI problemSourceIndividual = individualsJList.getSelectedValue();
						logme.debug(String.format("Loading solutions for individual %s", problemSourceIndividual));

						int index = ProblemsJList.locationToIndex(e.getPoint());
						logme.debug(String.format("Mouse clicked on menu item index %d.", index));
					
						if (index > -1){
							// We clicked on a list item; populate the popup menu and display it
							KIDSUIProblem ourProblem = problemJListModel.getElementAt(index);
							JPopupMenu eventProblemPopupMenu = new JPopupMenu();
						
							// If it is a missing relation problem, we can assume that 'quick fixes' involve
							// adding the relation:
							if (ourProblem.getClassOfProblem() == ProblemClass.MissingRelation){
								logme.debug(String.format("Problem is an instance of missing relation"));
						    	final KIDSMissingRelationUIProblem MRProb = (KIDSMissingRelationUIProblem)ourProblem;

						    	// Add an item for the problem
						    	List<KIDSUIPossibleFix> fixes = MRProb.getPossibleFixes();
						    	final Map<String, KIDSUIPossibleFix> fixmap = new HashMap<String, KIDSUIPossibleFix>();
								logme.debug(String.format("%d solutions available.", fixes.size()));
						    	for (KIDSUIPossibleFix f : fixes){
						    		logme.debug(String.format("Adding fix %s...", f.toString()));
							    	fixmap.put(f.toString(), f);
							    	JMenuItem thisFix = new JMenuItem(f.toString());
							    	thisFix.addActionListener(new ActionListener(){
								    	@Override
								    	public void actionPerformed(ActionEvent e) {
								    		JMenuItem menuItem = (JMenuItem)e.getSource();
								    		if (!fixmap.containsKey(menuItem.getText())){
								    			logme.error(String.format("No key %s in fixmap; cannot load value ",
								    				 menuItem.getText()));
								    			return;
								    		}
								    		logme.debug(String.format("Retrieving solution for menu item %s", menuItem.getText()));
								    		KIDSUIPossibleFix chosenFix = fixmap.get(menuItem.getText());
									   		if (chosenFix.getType() == KIDSUIPossibleFixType.ADDRELATIONTONEW ||
									   	    	chosenFix.getType() == KIDSUIPossibleFixType.ADDRELATIONTOEXISTING){
										    	KIDSUIAddRelationPossibleFix typedFix =((KIDSUIAddRelationPossibleFix)chosenFix); 
										    	IRI predicate = typedFix.getRelation();
										    	IRI object = typedFix.getObject();
									   	    	if (chosenFix.getType() == KIDSUIPossibleFixType.ADDRELATIONTONEW){
										   	    	// Dispatch the correct dialog
										   	    	IRI ourClass = MRProb.getMissingObjectClass();
										   	    	KIDSAddIndividualJDialog ourDialog;
													try {
														ourDialog = controller.getAddInstanceDialogForClass(ourClass, frame);
														if (ourDialog == null){
															controller.logappend(
																	new KIDSGUIAlertError(
																	"Could not load dialog to add relation."));
															return;
														}

										   	    		// Open a JDialog which prompts for TBOX location, IRI, and desired ABOX IRI; initiate a new ABOX
										   	    		ourDialog.setModal(true);
										   	    		ourDialog.setVisible(true);
				
										   	    		// We need the IRI back from the dialog:
										   	    		object = ourDialog.getAddedElementIRI();
													} catch (InstantiationException | IllegalAccessException e1) {
														controller.logappend(new KIDSGUIAlertError(
																String.format("Could not load dialog listed for %s",ourClass)
																));
														logme.error(e1.getMessage());
														e1.printStackTrace();
														return;
													}
									   		}
											// Add a relation
											controller.addRelation(
												problemSourceIndividual,
												predicate,
												object);
											controller.logappend(
												new KIDSGUIAlertInfo(String.format("Added relation (%s, %s, %s)", 
													problemSourceIndividual,
													predicate,
													object))
												);
											model.setState(ABOXBuilderGUIState.ABOXModified);
								       		} else {
								    	   		logme.warn(String.format("Unknown fix type %s.", 
								    	   			chosenFix.getType()));
								       		}
								    	}
							    	});
							    	eventProblemPopupMenu.add(thisFix);
						    	}
								processMessageQueue();
							}
							eventProblemPopupMenu.show(e.getComponent(), e.getX(), e.getY());
						}
					}
				}
			});

			// When an event is selected in the events list, populate the problems list:
			individualsJList.addListSelectionListener(new ListSelectionListener(){
				@Override
				public void valueChanged(ListSelectionEvent e) {
					if (!e.getValueIsAdjusting()){
						problemJListModel.removeAllElements();
						IRI individual = ((JList<IRI>)e.getSource()).getSelectedValue();
						if (individual != null){
						    logme.debug(String.format("New item selected in JList (%s)", individual.getFragment()));
							List<KIDSUIProblem> thisIndividualsProblems = controller.getProblems(individual);
							for (KIDSUIProblem kup : thisIndividualsProblems){
								problemJListModel.addElement(kup);
							}
						}
					}
				}
			});

			this.setViewportView(ProblemsJList);

		}
	}

	class KIDSIndividualProblemsJPanel extends JPanel{
		
		/**
		 * 
		 */
		private static final long serialVersionUID = -7014620571608951624L;
		private final IRI ourClass;
		
		private void repopulateList(DefaultListModel<IRI> target){
			Set<IRI> knownIndividuals = controller.getKnownIndividuals(ourClass);
			logme.debug(String.format("Controller found %d individuals of class %s", knownIndividuals.size(), ourClass));
			target.removeAllElements();
			for (IRI ind : knownIndividuals){
				target.addElement(ind);
			}
		}

		public KIDSIndividualProblemsJPanel(final IRI ourClass){
			this.ourClass = ourClass;
			JSplitPane KIDSSplitPane = new JSplitPane();
			this.add(KIDSSplitPane);
		
			JScrollPane IndividualScrollPane = new JScrollPane();
			KIDSSplitPane.setLeftComponent(IndividualScrollPane);
		
			final DefaultListModel<IRI> individualJListModel = new DefaultListModel<IRI>();
			final JList<IRI> IndividualJList = new JList<IRI>(individualJListModel);
			controller.addIndividualAddedListener(new AddEventListener(){
				@Override
				public void newEventReceived(IRI ind) {
					logme.debug(String.format("Received individual added event; repopulating list for class %s", ourClass));
					repopulateList(individualJListModel);
				}
			});
			controller.addOntologyLoadedListener(new OntologyLoadedListener(){
				@Override
				public void ontologyLoaded(KIDSGUIOracle o){
					// Get list of known events from ontology and populate our model:
					logme.debug(String.format("Received ontology loaded event; repopulating list for class %s", ourClass));
					repopulateList(individualJListModel);
				}
			});

			IndividualScrollPane.setViewportView(IndividualJList);
		
			KIDSProblemsJScrollPane ProblemScrollPane = 
				new KIDSProblemsJScrollPane(IndividualJList);
			KIDSSplitPane.setRightComponent(ProblemScrollPane);
		
			/**
			// Someday we may want to put this back
			JTree EventModelTree = new JTree();
			EventModelTree.setModel(new DefaultTreeModel(
				new DefaultMutableTreeNode("Event") {
					{
					DefaultMutableTreeNode node_1;
					node_1 = new DefaultMutableTreeNode("Required Components");
						node_1.add(new DefaultMutableTreeNode("Signals"));
					add(node_1);
					node_1 = new DefaultMutableTreeNode("Inferred Relations");
						node_1.add(new DefaultMutableTreeNode("Evaluated In Dataset"));
						node_1.add(new DefaultMutableTreeNode("Represented In Dataset"));
						node_1.add(new DefaultMutableTreeNode("Affected By Response"));
						node_1.add(new DefaultMutableTreeNode("Included In Dataset Label"));
					add(node_1);
					add(new DefaultMutableTreeNode("Class Membership"));
					}
				}
			));
			EventModelScrollPane.setViewportView(EventModelTree);
			*/
			
		}

	}

}
