/**
 * 
 */
package net.strasnet.kids.ui.gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Set;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;

import org.apache.log4j.LogManager;
import org.semanticweb.owlapi.model.IRI;

import net.strasnet.kids.measurement.test.KIDSSignalSelectionInterface;
import net.strasnet.kids.ui.components.KIDSUIComponent;
import net.strasnet.kids.ui.components.KIDSUIDatasetComponent;
import net.strasnet.kids.ui.components.KIDSUIDetectorComponent;
import net.strasnet.kids.ui.components.KIDSUIEventComponent;
import net.strasnet.kids.ui.components.KIDSUISignalComponent;
import net.strasnet.kids.ui.components.KIDSUITimePeriodComponent;

import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import javax.swing.JScrollPane;
import javax.swing.JList;
import javax.swing.JTree;

/**
 * @author cstras
 *
 */
public class KIDSRunMeasurementEvalJDialog extends JDialog {
	
	/**
	 *  This JDialog will display a simple selection interface populated by ABox individuals from the
	 *  given class. The method 'getSelectedElementIRI()' 
	 */
	private static final long serialVersionUID = 5851375789243455872L;
	public static final org.apache.log4j.Logger logme = LogManager.getLogger(KIDSRunMeasurementEvalJDialog.class.getName());

	protected String selectedElementIRI = null;
	protected IRI ourAboxIRI = null;
	protected IRI addedElementProcessedIRI = null;
	protected ABOXBuilderController controller = null;
	private final JPanel contentPanel = new JPanel();
	private JTextField individualIRIJTextField;
	private ABOXBuilderController ourC = null;
	private KIDSUIEventComponent selectedEvent = null;
	private KIDSUITimePeriodComponent selectedTimePeriod = null;

	/**
	 * 
	 * @param parent - The container parent to attach the dialog to
	 * @param aboxiri - the IRI of the ABOX to associate this individual with
	 */
	public KIDSRunMeasurementEvalJDialog(JFrame parent, 
			ABOXBuilderController controller) {
		super(parent);
		setAlwaysOnTop(true);
		ourC = controller;
		
		ourAboxIRI = controller.getABOXPrefix();
		this.controller = controller;
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0, 0, 0, 0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0, 0, 0, 0};
		gridBagLayout.columnWeights = new double[]{1.0, 0.0, 1.0, 0.0, 1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{0.0, 2.0, 0.0, 0.0, Double.MIN_VALUE};
		getContentPane().setLayout(gridBagLayout);
		
		JLabel lblEventList = new JLabel("Event List");
		GridBagConstraints gbc_lblEventList = new GridBagConstraints();
		gbc_lblEventList.insets = new Insets(0, 0, 5, 5);
		gbc_lblEventList.gridx = 0;
		gbc_lblEventList.gridy = 0;
		getContentPane().add(lblEventList, gbc_lblEventList);
		
		JLabel lblTimePeriodList = new JLabel("Time Period List");
		GridBagConstraints gbc_lblTimePeriodList = new GridBagConstraints();
		gbc_lblTimePeriodList.insets = new Insets(0, 0, 5, 5);
		gbc_lblTimePeriodList.gridx = 2;
		gbc_lblTimePeriodList.gridy = 0;
		getContentPane().add(lblTimePeriodList, gbc_lblTimePeriodList);
		
		JLabel lblIncludedParameters = new JLabel("Included Parameters");
		GridBagConstraints gbc_lblIncludedParameters = new GridBagConstraints();
		gbc_lblIncludedParameters.insets = new Insets(0, 0, 5, 5);
		gbc_lblIncludedParameters.gridx = 4;
		gbc_lblIncludedParameters.gridy = 0;
		getContentPane().add(lblIncludedParameters, gbc_lblIncludedParameters);
		
		JScrollPane EventScrollPane = new JScrollPane();
		GridBagConstraints gbc_EventScrollPane = new GridBagConstraints();
		gbc_EventScrollPane.insets = new Insets(0, 0, 5, 5);
		gbc_EventScrollPane.fill = GridBagConstraints.BOTH;
		gbc_EventScrollPane.gridx = 0;
		gbc_EventScrollPane.gridy = 1;
		getContentPane().add(EventScrollPane, gbc_EventScrollPane);

		JScrollPane ParameterViewScrollPane = new JScrollPane();
		GridBagConstraints gbc_ParameterViewScrollPane = new GridBagConstraints();
		gbc_ParameterViewScrollPane.insets = new Insets(0, 0, 5, 5);
		gbc_ParameterViewScrollPane.fill = GridBagConstraints.BOTH;
		gbc_ParameterViewScrollPane.gridx = 4;
		gbc_ParameterViewScrollPane.gridy = 1;
		getContentPane().add(ParameterViewScrollPane, gbc_ParameterViewScrollPane);
		
		//JTreeModel<KIDSUIComponent> parameterTreeModel = new JTreeModel();
		final DefaultMutableTreeNode top = new DefaultMutableTreeNode("Experiment Parameters:");
		final JTree ParameterTree = new JTree(top);

		ParameterViewScrollPane.setViewportView(ParameterTree);
		
		final DefaultListModel<KIDSUITimePeriodComponent> timePeriodJListModel = new DefaultListModel<KIDSUITimePeriodComponent>();
		final JList<KIDSUITimePeriodComponent> timePeriodJList = new JList<KIDSUITimePeriodComponent>(timePeriodJListModel);

		JScrollPane TimePeriodScrollPane = new JScrollPane();
		GridBagConstraints gbc_TimePeriodScrollPane = new GridBagConstraints();
		gbc_TimePeriodScrollPane.insets = new Insets(0, 0, 5, 5);
		gbc_TimePeriodScrollPane.fill = GridBagConstraints.BOTH;
		gbc_TimePeriodScrollPane.gridx = 2;
		gbc_TimePeriodScrollPane.gridy = 1;
		getContentPane().add(TimePeriodScrollPane, gbc_TimePeriodScrollPane);
		
		TimePeriodScrollPane.setViewportView(timePeriodJList);

		final DefaultListModel<KIDSUIEventComponent> eventJListModel = new DefaultListModel<KIDSUIEventComponent>();
		Set<KIDSUIComponent> eventSet = controller.getKnownIndividuals(controller.EVENTCLASSIRI);
		for (KIDSUIComponent k : eventSet){
			eventJListModel.addElement((KIDSUIEventComponent) k);
		}
		final JList<KIDSUIEventComponent> eventJList = new JList<KIDSUIEventComponent>(eventJListModel);
		EventScrollPane.setViewportView(eventJList);

		eventJList.addListSelectionListener(new ListSelectionListener(){

			@Override
			public void valueChanged(ListSelectionEvent arg0) {
				// Populate TimePeriodList with those time periods an event can be evaluated over.
				// From isEvaluableDuringTimePeriod property
				if (!arg0.getValueIsAdjusting()){
					timePeriodJListModel.clear();
					KIDSUIEventComponent selected = eventJList.getSelectedValue();
					
					Set<KIDSUITimePeriodComponent> tplist = selected.getAvailableTimePeriods();

					// Clear TimePeriodList selection (if any)
					timePeriodJList.clearSelection();
					
					for (KIDSUITimePeriodComponent t : tplist){
						timePeriodJListModel.addElement(t);
					}
					selectedEvent = selected;
				}
			}
		});

		timePeriodJList.addListSelectionListener(new ListSelectionListener(){

			@Override
			public void valueChanged(ListSelectionEvent arg0) {
				// Populate the parameterTreeModel with the Signals, Datasets, and Detectors that will be
				// included in the evaluation.
				if (!arg0.getValueIsAdjusting()){
					KIDSUITimePeriodComponent selected = timePeriodJList.getSelectedValue();
					populateTree(top, eventJList.getSelectedValue(), selected);
					ParameterTree.repaint();
					selectedTimePeriod = selected;
				}
			}
			
		});
		
		
		JButton runButton = new JButton("Run");
		GridBagConstraints gbc_runButton = new GridBagConstraints();
		gbc_runButton.insets = new Insets(0, 0, 0, 5);
		gbc_runButton.gridx = 0;
		gbc_runButton.gridy = 2;
		runButton.addActionListener(new ActionListener(){
			// Construct the required settings
			
			// Call evaluation of 

			@Override
			public void actionPerformed(ActionEvent arg0) {
				// When the button is pressed, close the window.
				KIDSSignalSelectionInterface KiSSI = new KIDSSignalSelectionInterface();
				KiSSI.testSignalSet(
						ourC.o, 
						selectedEvent.getIRI(), 
						selectedTimePeriod.getIRI());
			}
			
		});
		
		getContentPane().add(runButton, gbc_runButton);
		
		JButton resSaveButton = new JButton("Save Results...");
		GridBagConstraints gbc_resSaveButton = new GridBagConstraints();
		gbc_resSaveButton.insets = new Insets(0, 0, 0, 5);
		gbc_resSaveButton.gridx = 2;
		gbc_resSaveButton.gridy = 2;
		getContentPane().add(resSaveButton, gbc_resSaveButton);
				
		JButton closeButton = new JButton("Close");
		GridBagConstraints gbc_closeButton = new GridBagConstraints();
		gbc_closeButton.insets = new Insets(0, 0, 0, 5);
		gbc_closeButton.gridx = 4;
		gbc_closeButton.gridy = 2;
		
		closeButton.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent arg0) {
				// When the button is pressed, close the window.
				KIDSRunMeasurementEvalJDialog.this.dispose();
			}
			
		});
		
		getContentPane().add(closeButton, gbc_closeButton);

		this.pack();
		logme.debug("Packed.");
	}
	
	private void populateTree(DefaultMutableTreeNode top, KIDSUIEventComponent eventUIC, KIDSUITimePeriodComponent timeUIC){
		// Build list of compatible signals -- from intersection of isProducerOf and 
		Set<KIDSUISignalComponent> sigset = controller.getEvaluableSignals(eventUIC, timeUIC);

		DefaultMutableTreeNode signals = new DefaultMutableTreeNode("Signals");
		
		for (KIDSUISignalComponent sig : sigset){
			DefaultMutableTreeNode sigNode = new DefaultMutableTreeNode(sig.getIRI().getShortForm());
			sigNode.setUserObject(sig);
			signals.add(sigNode);
		}
		top.add(signals);
		
		// Build list of compatible datasets -- from isEvaluatedBy
		Set<KIDSUIDatasetComponent> datset = controller.getEvaluableDatasets(eventUIC, timeUIC);
		DefaultMutableTreeNode datasets = new DefaultMutableTreeNode("Datasets");
		
		for (KIDSUIDatasetComponent dat : datset){
			DefaultMutableTreeNode datNode = new DefaultMutableTreeNode(dat.getIRI().getShortForm());
			datNode.setUserObject(dat);
			datasets.add(datNode);
		}
		top.add(datasets);
		
		// Build list of compatible detectors -- from isIdentifiedByDetector
		Set<KIDSUIDetectorComponent> detset = controller.getEvaluableDetectors(eventUIC, timeUIC);
		DefaultMutableTreeNode detectors = new DefaultMutableTreeNode("Detectors");
		
		for (KIDSUIDetectorComponent det : detset){
			DefaultMutableTreeNode detNode = new DefaultMutableTreeNode(det.getIRI().getShortForm());
			detNode.setUserObject(det);
			detectors.add(detNode);
		}
		top.add(detectors);
	}
	
}
