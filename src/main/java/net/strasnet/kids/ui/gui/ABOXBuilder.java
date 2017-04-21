package net.strasnet.kids.ui.gui;

import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;

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
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.DefaultMutableTreeNode;

import net.strasnet.kids.KIDSOracle;
import net.strasnet.kids.ui.components.KIDSUIComponent;
import net.strasnet.kids.ui.components.KIDSUIDataRelationComponent;
import net.strasnet.kids.ui.components.KIDSUIObjectRelationComponent;
import net.strasnet.kids.ui.components.KIDSUIRelation;
import net.strasnet.kids.ui.components.KIDSUIRelation.RelationType;
import net.strasnet.kids.ui.gui.ABOXBuilderModel.ABOXBuilderGUIState;
import net.strasnet.kids.ui.gui.alerts.KIDSGUIAlert;
import net.strasnet.kids.ui.gui.alerts.KIDSGUIAlertError;
import net.strasnet.kids.ui.gui.alerts.KIDSGUIAlertInfo;
import net.strasnet.kids.ui.problemfixes.KIDSUIAddDatatypePropertyPossibleFix;
import net.strasnet.kids.ui.problemfixes.KIDSUIAddRelationPossibleFix;
import net.strasnet.kids.ui.problemfixes.KIDSUIAddToSubclassPossibleFix;
import net.strasnet.kids.ui.problemfixes.KIDSUIPossibleFix;
import net.strasnet.kids.ui.problemfixes.KIDSUIPossibleFix.KIDSUIPossibleFixType;
import net.strasnet.kids.ui.problems.KIDSMissingRelationUIProblem;
import net.strasnet.kids.ui.problems.KIDSUIProblem;
import net.strasnet.kids.ui.problems.KIDSUIProblem.ProblemClass;

import org.apache.log4j.LogManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.ActionListener;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
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

	public static final org.apache.log4j.Logger logme = LogManager.getLogger(ABOXBuilder.class.getName());

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
					logme.error("Could not load ontology: ", e1);
				} catch (OWLOntologyStorageException e1) {
					controller.logappendError("Uh-oh - an exception occurred when writing the ontology: " + e1.getMessage());
					logme.error("Could not write ontology: ", e1);
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
					KIDSAddIndividualJDialog newEvent = new KIDSAddIndividualJDialog(frame, 
							controller.getABOXPrefix(), 
							KIDSOracle.eventClass, 
							controller);
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
		
		JMenuItem mntmEvalEvent = new JMenuItem("Evaluate...");
		mntmEvalEvent.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				if (model.getState() != ABOXBuilderGUIState.UNINITIALIZED){
					// Execute the measurement code and return results.
					KIDSRunMeasurementEvalJDialog getEventInd = new KIDSRunMeasurementEvalJDialog(frame, controller);
					logme.debug(String.format("KIDSRunMeasurementEvalJDialog loaded..."));
					getEventInd.setModal(true);
					logme.debug(String.format("KIDSRunMeasurementEvalJDialog set modal..."));
					getEventInd.setVisible(true);
					logme.debug(String.format("KIDSRunMeasurementEvalJDialog visible..."));
				} else {
					controller.logappendInfo("ABOX uninitialized; create new or open existing.");
				}

				processMessageQueue();
			}
		});
		mnModelActions.add(mntmEvalEvent);
		
		JMenuItem mntmEvalDatasets = new JMenuItem("Evaluate datasets...");
		mntmEvalDatasets.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				if (model.getState() != ABOXBuilderGUIState.UNINITIALIZED){
					// Execute the measurement code and return results.
					KIDSEvalDatasetViewsJDialog getDatasetViewD = new KIDSEvalDatasetViewsJDialog(frame, controller);
					logme.debug(String.format("KIDSEvalDatasetViewsJDialog loaded..."));
					getDatasetViewD.setModal(true);
					logme.debug(String.format("KIDSEvalDatasetViewsJDialog set modal..."));
					getDatasetViewD.setVisible(true);
					logme.debug(String.format("KIDSEvalDatasetViewsJDialog visible..."));
				} else {
					controller.logappendInfo("ABOX uninitialized; create new or open existing.");
				}

				processMessageQueue();
			}
		});
		mnModelActions.add(mntmEvalDatasets);
		
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
		tabbedPane.setPreferredSize(new Dimension(1000,500));
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
		
		JPanel DetectorSyntaxPanel = new KIDSIndividualProblemsJPanel(ABOXBuilderController.DETECTORSYNTAXCLASSIRI);
		tabbedPane.addTab("DetectorSyntax", null, DetectorSyntaxPanel, null);
		
		JPanel SignalConstraintPanel = new KIDSIndividualProblemsJPanel(ABOXBuilderController.SIGNALCONSTRAINTCLASSIRI);
		tabbedPane.addTab("SignalConstraints", null, SignalConstraintPanel, null);
		
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
	
	class KIDSComponentDetailsJScrollPane extends JScrollPane{
		/**
		 * 
		 */
		private static final long serialVersionUID = 3804024214398440319L;

		/**
		 * This scroll pane is on the far right of the GUI, and displays the tree of component details for the
		 * selected individual on the far left.
		 */
		
		public KIDSComponentDetailsJScrollPane (final JList<KIDSUIComponent> individualsJList){
			final DefaultMutableTreeNode root = new DefaultMutableTreeNode("Details:");
			final JTree detailJTree = new JTree(root);
			
			detailJTree.addTreeExpansionListener(new TreeExpansionListener() {

				@Override
				public void treeCollapsed(TreeExpansionEvent arg0) {
					// No need to process.
				}

				@Override
				public void treeExpanded(TreeExpansionEvent arg0) {
					// Repaint parent window:
					KIDSComponentDetailsJScrollPane.this.repaint();
				}

			}
			);

			
			individualsJList.addMouseListener(new MouseAdapter() {
				@Override
				public void mousePressed(MouseEvent e){
					logme.debug("Processing mouse pressed event for details tree...");
					root.removeAllChildren();
					if (!individualsJList.isSelectionEmpty()){
						KIDSUIComponent selected = individualsJList.getSelectedValue();
						selected.getComponentDetails(root);
					}
				}
				
			});
			this.setViewportView(detailJTree);
		}
		
		
	}
	
	class KIDSProblemsJScrollPane extends JScrollPane{

		/**
		 *  This scroll pane is on the right side of the GUI, and contains the problems identified for the
		 *  selected individual on the left side of the split pane.
		 */
		private static final long serialVersionUID = -5930357945561781477L;

		public KIDSProblemsJScrollPane(final JList<KIDSUIComponent> individualsJList){

			final DefaultListModel<KIDSUIProblem> problemJListModel = new DefaultListModel<KIDSUIProblem>();
			final JList<KIDSUIProblem> ProblemsJList = new JList<KIDSUIProblem>(problemJListModel);
			ProblemsJList.addMouseListener(new MouseAdapter() {
				@Override
				public void mousePressed(MouseEvent e) {

					logme.debug(String.format("Mouse button %d clicked in problemsJList.", e.getButton()));
				

					// Only respond to right-clicks
					if (e.getButton() == MouseEvent.BUTTON3){
					
						// First, get the JList item that was clicked, if any:
						final KIDSUIComponent problemSourceIndividual = individualsJList.getSelectedValue();
						logme.debug(String.format("Loading solutions for individual %s", problemSourceIndividual));

						int index = ProblemsJList.locationToIndex(e.getPoint());
						logme.debug(String.format("Mouse clicked on menu item index %d.", index));
					
						if (index > -1){
							// We clicked on a list item; populate the popup menu and display it
							final KIDSUIProblem ourProblem = problemJListModel.getElementAt(index);
							logme.debug(String.format("Building solutions for problem class %s, type %s",
									ourProblem.getClassOfProblem(),
									ourProblem.getType()
									));
							JPopupMenu eventProblemPopupMenu = new JPopupMenu();
						
							// If it is a missing relation problem, we can assume that 'quick fixes' involve
							// adding the relation:
							//if (ourProblem.getClassOfProblem() == ProblemClass.MissingRelation){
							//	logme.debug(String.format("Problem is an instance of missing relation"));
						    //	final KIDSMissingRelationUIProblem MRProb = (KIDSMissingRelationUIProblem)ourProblem;

						    	// Add an item for the problem
						    	List<KIDSUIPossibleFix> fixes = ourProblem.getPossibleFixes();
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
										   	    	IRI ourClass = 
										   	    			((KIDSMissingRelationUIProblem)ourProblem).getMissingObjectClass();
										   	    	KIDSAddIndividualJDialog ourDialog;
													try {
														ourDialog = controller.getAddInstanceDialogForClass(ourClass, frame);
														if (ourDialog == null){
															controller.logappendError(
																	String.format(
																	"Could not load dialog to add individual of class %s.",ourClass));
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
											    		problemSourceIndividual.getIRI(),
											    		predicate,
												   		object);
											    controller.logappend(
												new KIDSGUIAlertInfo(String.format("Added relation (%s, %s, %s)", 
													problemSourceIndividual.getIRI(),
													predicate,
													object))
												);
											    model.setState(ABOXBuilderGUIState.ABOXModified);
									   		} else if (chosenFix.getType() == KIDSUIPossibleFixType.DEFINESUBCLASSFORINDIVIDUAL){
									   			// All we need to do is provide the class to map to:
										    	KIDSUIAddToSubclassPossibleFix typedFix =((KIDSUIAddToSubclassPossibleFix)chosenFix); 
									   			logme.debug(String.format("Creating entry for fix type %s (map %s -> %s)", 
									   					chosenFix.getType(),
									   					typedFix.getIndividualIRI(),
									   					typedFix.getSubclassIRI()
									   					));
										    	controller.addIndividual(typedFix.getIndividualIRI(), typedFix.getSubclassIRI());
										    	controller.logappendInfo(String.format("Added %s to class %s", typedFix.getIndividualIRI(),
										    			typedFix.getSubclassIRI()));
									   		} else if (chosenFix.getType() == KIDSUIPossibleFixType.ADDDATATYPEVALUE){
									   			logme.debug(String.format("Creating dialog for fix type %s", chosenFix.getType()));
										    	KIDSUIAddDatatypePropertyPossibleFix typedFix =
										    			((KIDSUIAddDatatypePropertyPossibleFix)chosenFix); 
												try {
													KIDSAddDataJDialog ourDialog = controller.getAddDataValueDialogForClass(
															typedFix.getDatatypeClass(), frame,
															typedFix.getSubjectIRI(), typedFix.getRelation());
													if (ourDialog == null){
														controller.logappendError(
																String.format(
																"Could not load dialog to add data of class %s.",typedFix.getDatatypeClass()));
														return;
													}

									   	    		// Open a JDialog which prompts for TBOX location, IRI, and desired ABOX IRI; initiate a new ABOX
									   	    		ourDialog.setModal(true);
									   	    		ourDialog.setVisible(true);
			
									   	    		// We need the data back from the dialog:
									   	    		String dv = ourDialog.getAddedData();

									   	    		// Add via the controller:
									   	    		controller.addDataValueForIndividual(
									   	    				typedFix.getRelation(), 
									   	    				typedFix.getSubjectIRI(),
									   	    				dv);
												} catch (InstantiationException | IllegalAccessException e1) {
													controller.logappend(new KIDSGUIAlertError(
															String.format("Could not load dialog listed for %s",typedFix.getDatatypeClass())
															));
													logme.error(e1.getMessage());
													e1.printStackTrace();
													return;
												}
									   		} else {
									   			logme.error(String.format("Unknown fix type %s.", chosenFix.getType()));
									   		}
								       	}
							    	});
							    	eventProblemPopupMenu.add(thisFix);
						    	}
								processMessageQueue();
							eventProblemPopupMenu.show(e.getComponent(), e.getX(), e.getY());
						}
					}
					processMessageQueue();
				}
			});

			// When an individual is selected in the individuals list, populate the problems list:
			individualsJList.addListSelectionListener(new ListSelectionListener(){
				@Override
				public void valueChanged(ListSelectionEvent e) {
					if (!e.getValueIsAdjusting()){
						problemJListModel.removeAllElements();
						KIDSUIComponent individual = ((JList<KIDSUIComponent>)e.getSource()).getSelectedValue();
						if (individual != null){
						    logme.debug(String.format("New item selected in JList (%s)", 
						    		individual.getIRI().getShortForm()));
							Set<KIDSUIProblem> thisIndividualsProblems = individual.getComponentProblems();
							for (KIDSUIProblem kup : thisIndividualsProblems){
								problemJListModel.addElement(kup);
							}
						}
						logme.debug("Repacking frame due to list selection event...");
						((JFrame) SwingUtilities.getWindowAncestor(ProblemsJList)).pack();
					}
					processMessageQueue();
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
		
		private void repopulateList(DefaultListModel<KIDSUIComponent> target){
			Set<KIDSUIComponent> knownIndividuals = controller.getKnownIndividuals(ourClass);
			logme.debug(String.format("Listing %d individuals of class %s", knownIndividuals.size(), ourClass));
			target.removeAllElements();
			for (KIDSUIComponent ind : knownIndividuals){
				target.addElement(ind);
			}
		}

		/**
		 * This panel will display the individuals known to the KB that are members of ourClass (or a subclass).
		 * 
		 * @param ourClass - The class of individuals shown in this panel.
		 */
		public KIDSIndividualProblemsJPanel(final IRI ourClass){
			this.ourClass = ourClass;
			this.setLayout(new FlowLayout());
			//JSplitPane KIDSSplitPane = new JSplitPane();
			//this.add(KIDSSplitPane);
		
			JScrollPane IndividualScrollPane = new JScrollPane();
			IndividualScrollPane.setPreferredSize(new Dimension(300,300));
			IndividualScrollPane.setMinimumSize(new Dimension(300,300));
			this.add(IndividualScrollPane);
			//KIDSSplitPane.setLeftComponent(IndividualScrollPane);
		
			final DefaultListModel<KIDSUIComponent> individualJListModel = new DefaultListModel<KIDSUIComponent>();
			final JList<KIDSUIComponent> IndividualJList = new JList<KIDSUIComponent>(individualJListModel);
			IndividualJList.addMouseListener(new MouseAdapter() {
				@Override
				public void mousePressed(MouseEvent e) {

					logme.debug(String.format("Mouse button %d clicked in IndividualJList.", e.getButton()));

					// Only respond to right-clicks
					if (e.getButton() == MouseEvent.BUTTON3){
					
						// First, get the JList item that was clicked, if any:
						int index = IndividualJList.locationToIndex(e.getPoint());
						logme.debug(String.format("Mouse clicked on menu item index %d.", index));
						if (index > -1 && IndividualJList.getCellBounds(index, index).contains(e.getPoint())){
							final KIDSUIComponent ourComponent = individualJListModel.getElementAt(index);
							logme.debug(String.format("Loading menu for individual %s", ourComponent));
							JPopupMenu getIndividualInfoPopupMenu = new JPopupMenu();
							JMenuItem getInfo = new JMenuItem("Get details...");
							getIndividualInfoPopupMenu.add(getInfo);

							getInfo.addActionListener(new ActionListener(){
								@Override
								public void actionPerformed(ActionEvent e) {
									// Popup a dialog displaying the details of the individual
									logme.debug(String.format("Generating details for component %s", ourComponent));
									
									// Get class membership

									// Get object properties

									// Get data properties
									
									// Get problems

									processMessageQueue();
								}
							});

							JMenu addRelation = new JMenu("Add relation ...");
							getIndividualInfoPopupMenu.add(addRelation);
							
							List <KIDSUIRelation> itemRelations = ourComponent.getRelations();
							
							for (final KIDSUIRelation kur : itemRelations){
								JMenu ouritem = new JMenu(String.format("Add %s ...", kur.getRelationIRI().getShortForm()));
								if (kur.getType() == RelationType.Data){
									final KIDSUIDataRelationComponent kdr = (KIDSUIDataRelationComponent)kur;
									JMenuItem dataitem = new JMenuItem(String.format(
											"New %s data", kdr.getDatatypeClass()));
								
								    dataitem.addActionListener(new ActionListener(){
								    	@Override
									   	public void actionPerformed(ActionEvent e){
										   	// Load the component:
										   	try {
										   	    KIDSAddDataJDialog ourDialog = 
												controller.getAddDataValueDialogForClass(
												    kdr.getDatatypeClass(), frame, 
													kdr.getSubjectIRI(), kdr.getRelationIRI());
												if (ourDialog == null){
													controller.logappendError(
																String.format(
																"Could not load dialog to add individual of class %s.",ourClass));
												} else {
										   	    	// Open a JDialog which prompts for TBOX location, IRI, and desired ABOX IRI; initiate a new ABOX
										   	    	ourDialog.setModal(true);
										   	    	ourDialog.setVisible(true);
				
										   	    	// We need the IRI back from the dialog:
										   	    	String data = ourDialog.getAddedData();
										   	        controller.addDataValueForIndividual(kdr.getRelationIRI(), 
										   	    		kdr.getSubjectIRI(), data);
										   	        controller.logappendInfo(String.format(
										   	    		    "Added relation (%s, %s, %s)", 
										   	    		    kdr.getSubjectIRI(),
										   	    		    kdr.getRelationIRI(),
										   	    		    data));
												}
										   	} catch (InstantiationException | IllegalAccessException e1) {
											   	controller.logappend(new KIDSGUIAlertError(
												   	String.format("Could not load dialog for %s",
														kur.getRelationIRI())));
											   	logme.error(e1.getMessage());
											   	e1.printStackTrace();
										   	} finally {
											   	processMessageQueue();
										   	}
								    	}
								    });
								} else if (kur.getType() == RelationType.Object){
									final KIDSUIObjectRelationComponent kdr = (KIDSUIObjectRelationComponent) kur;
									
									Set<KIDSUIComponent> candidates = controller.getKnownIndividuals(kdr.getObjectClass());
									
									for (final KIDSUIComponent c : candidates){
										JMenuItem dataitem = new JMenuItem(String.format(
											"%s", c.getIRI()));
										dataitem.addActionListener(new ActionListener(){
											public void actionPerformed(ActionEvent e){
												controller.addRelation(kdr.getSubjectIRI(), 
														kdr.getRelationIRI(), 
														c.getIRI());
												controller.logappendInfo(String.format(
														"Added relation (%s, %s, %s)", 
														kdr.getSubjectIRI(),
														kdr.getRelationIRI(),
														c.getIRI()));
											}
										});
										ouritem.add(dataitem);
									}

									JMenuItem dataitem = new JMenuItem(String.format(
											"New %s instance...", kdr.getObjectClass().getShortForm()));
								
								    dataitem.addActionListener(new ActionListener(){
								    	
								    	public void actionPerformed(ActionEvent e){

								    		try {
										   		KIDSAddIndividualJDialog ourDialog = 
											   		controller.getAddInstanceDialogForClass(
												   		kdr.getObjectClass(),
												   		frame);
										   		if (ourDialog == null){
											   		controller.logappendError(
												   		String.format(
												   		"Could not load dialog to add individual of class %s.",ourClass));
										   		} else {
											   		// Open a JDialog which prompts for TBOX location, IRI, and desired ABOX IRI; initiate a new ABOX
											   		ourDialog.setModal(true);
											   		ourDialog.setVisible(true);
					   		
											   		// We need the IRI back from the dialog:
											   		IRI object = ourDialog.getAddedElementIRI();
											   		controller.addRelation(kdr.getSubjectIRI(), 
										    	   		kdr.getRelationIRI(), object);
											   		controller.logappendInfo(String.format(
										   		   		"Added relation (%s, %s, %s)", 
										   		   		kdr.getSubjectIRI(),
										   		   		kdr.getRelationIRI(),
										   		   		object));
										   		}
									   		} catch (InstantiationException | IllegalAccessException e1) {
										   		controller.logappend(new KIDSGUIAlertError(
												   		String.format("Could not load dialog for %s",
														   		kur.getRelationIRI())));
										   		logme.error(e1.getMessage());
										   		e1.printStackTrace();
									   		} finally {
										   		processMessageQueue();
									   		}
								    	}
								   	});
								addRelation.add(ouritem);
								}
							}

							getIndividualInfoPopupMenu.show(e.getComponent(), e.getX(), e.getY());
						} else {
							// Clicked on an empty area; build a popup menu to add a new Individual:
							JPopupMenu newIndividualPopupMenu = new JPopupMenu();
							JMenuItem newIndividual = new JMenuItem("Add new...");
							newIndividualPopupMenu.add(newIndividual);
							logme.debug(String.format("Loading menu to add a new %s",ourClass));
							newIndividual.addActionListener(new ActionListener(){
								
								@Override
								public void actionPerformed(ActionEvent e){
									// Trigger the appropriate new Individual dialog:
									try {
										KIDSAddIndividualJDialog addOne = controller.getAddInstanceDialogForClass(ourClass, frame);

										if (addOne == null){
											controller.logappendError(
												String.format(
												"Could not load dialog to add individual of class %s.",ourClass));
											return;
										}

									   	// Open a JDialog which prompts for TBOX location, IRI, and desired ABOX IRI; initiate a new ABOX
									   	addOne.setModal(true);
									   	addOne.setVisible(true);
			
									   	// We need the data back from the dialog:
									   	IRI newIndividual = addOne.getAddedElementIRI();

									   	// Add via the controller:
									   	controller.addIndividual(newIndividual, ourClass);
									   	controller.logappendInfo(String.format("Added individual %s to class %s", 
									   			newIndividual,
									   			ourClass));
									} catch (InstantiationException e1) {
										controller.logappend(new KIDSGUIAlertError(
											String.format("Could not load dialog listed for %s",ourClass)
											));
										logme.error(e1.getMessage());
										e1.printStackTrace();
										return;
									} catch (IllegalAccessException e1) {
										controller.logappend(new KIDSGUIAlertError(
											String.format("Could not access dialog class for %s",ourClass)
											));
										logme.error(e1.getMessage());
										e1.printStackTrace();
										return;
									} finally {
										processMessageQueue();
									}
								}

							});
							newIndividualPopupMenu.show(e.getComponent(), e.getX(), e.getY());
						}
					}
				}
			});

			controller.addIndividualAddedListener(new AddEventListener(){
				@Override
				public void newEventReceived(IRI ind) {
					logme.debug(String.format("Received individual added event; repopulating list for class %s", ourClass));
					repopulateList(individualJListModel);
				}
			});
			controller.addOntologyModifiedListener(new OntologyModifiedListener(){
				@Override
				public void ontologyModified(KIDSGUIOracle o){
					// Get list of known events from ontology and populate our model:
					logme.debug(String.format("Received ontology modified event; repopulating list for class %s", ourClass));
					repopulateList(individualJListModel);
				}
			});

			IndividualScrollPane.setViewportView(IndividualJList);
		
			KIDSProblemsJScrollPane ProblemScrollPane = 
				new KIDSProblemsJScrollPane(IndividualJList);
			ProblemScrollPane.setPreferredSize(new Dimension(300,300));
			ProblemScrollPane.setMinimumSize(new Dimension(300,300));
			this.add(ProblemScrollPane);
			//KIDSSplitPane.setRightComponent(ProblemScrollPane);
			
			KIDSComponentDetailsJScrollPane DetailsScrollPane = 
					new KIDSComponentDetailsJScrollPane(IndividualJList);
			DetailsScrollPane.setPreferredSize(new Dimension(300,300));
			DetailsScrollPane.setMinimumSize(new Dimension(300,300));
			this.add(DetailsScrollPane);
			//KIDSSplitPane.add(DetailsScrollPane);
		
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
