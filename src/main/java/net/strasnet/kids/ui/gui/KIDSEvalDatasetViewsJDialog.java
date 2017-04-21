/**
 * 
 */
package net.strasnet.kids.ui.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.List;
import java.util.Set;

import javax.swing.DefaultListModel;
import javax.swing.DropMode;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;

import org.apache.log4j.LogManager;
import org.semanticweb.owlapi.model.IRI;

import net.strasnet.kids.KIDSOntologyDatatypeValuesException;
import net.strasnet.kids.KIDSOntologyObjectValuesException;
import net.strasnet.kids.detectors.UnimplementedIdentifyingFeatureException;
import net.strasnet.kids.detectorsyntaxproducers.KIDSIncompatibleSyntaxException;
import net.strasnet.kids.measurement.KIDSDatasetFactory;
import net.strasnet.kids.measurement.KIDSUnEvaluableSignalException;
import net.strasnet.kids.measurement.ViewLabelDataset;
import net.strasnet.kids.measurement.test.KIDSSignalSelectionInterface;
import net.strasnet.kids.ui.components.KIDSUIComponent;
import net.strasnet.kids.ui.components.KIDSUIDatasetComponent;
import net.strasnet.kids.ui.components.KIDSUIDatasetViewComponent;
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
import javax.swing.ListSelectionModel;

/**
 * @author cstras
 *
 */
public class KIDSEvalDatasetViewsJDialog extends JDialog {
	
	/**
	 *  This JDialog will display a simple selection interface populated by ABox individuals from the
	 *  given class. The method 'getSelectedElementIRI()' 
	 */
	private static final long serialVersionUID = 5851375789243455372L;
	public static final org.apache.log4j.Logger logme = LogManager.getLogger(KIDSEvalDatasetViewsJDialog.class.getName());

	protected String selectedElementIRI = null;
	protected IRI ourAboxIRI = null;
	protected IRI addedElementProcessedIRI = null;
	protected ABOXBuilderController controller = null;
	private final JPanel contentPanel = new JPanel();
	private final JTextPane resultJTextPane = new JTextPane();
	private JTextField individualIRIJTextField;
	private ABOXBuilderController ourC = null;
	private KIDSUIEventComponent selectedEvent = null;
	private KIDSUITimePeriodComponent selectedTimePeriod = null;

	/**
	 * 
	 * @param parent - The container parent to attach the dialog to
	 * @param aboxiri - the IRI of the ABOX to associate this individual with
	 */
	public KIDSEvalDatasetViewsJDialog(JFrame parent, 
			ABOXBuilderController controller) {
		super(parent);
		setAlwaysOnTop(true);
		ourC = controller;
		
		ourAboxIRI = controller.getABOXPrefix();
		this.controller = controller;

		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0};
		gridBagLayout.columnWeights = new double[]{1.0, 1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		getContentPane().setLayout(gridBagLayout);
		
		JLabel lblDatasetViews = new JLabel("Dataset View List");
		GridBagConstraints gbc_lblDatasetViews = new GridBagConstraints();
		gbc_lblDatasetViews.insets = new Insets(0, 0, 5, 5);
		gbc_lblDatasetViews.gridx = 1;
		gbc_lblDatasetViews.gridy = 0;
		getContentPane().add(lblDatasetViews, gbc_lblDatasetViews);
		
		JLabel lblEventList = new JLabel("Events");
		GridBagConstraints gbc_lblEventList = new GridBagConstraints();
		gbc_lblEventList.insets = new Insets(0, 0, 5, 5);
		gbc_lblEventList.gridx = 0;
		gbc_lblEventList.gridy = 0;
		getContentPane().add(lblEventList, gbc_lblEventList);
		
		JScrollPane DatasetViewScrollPane = new JScrollPane();
		GridBagConstraints gbc_DatasetViewScrollPane  = new GridBagConstraints();
		gbc_DatasetViewScrollPane.insets = new Insets(0, 0, 5, 5);
		gbc_DatasetViewScrollPane.fill = GridBagConstraints.BOTH;
		gbc_DatasetViewScrollPane.gridx = 1;
		gbc_DatasetViewScrollPane.gridy = 1;
		gbc_DatasetViewScrollPane.gridheight = 4;
		getContentPane().add(DatasetViewScrollPane , gbc_DatasetViewScrollPane);

		final DefaultListModel<KIDSUIDatasetViewComponent> datasetViewJListModel = new DefaultListModel<KIDSUIDatasetViewComponent>();
		final JList<KIDSUIDatasetViewComponent> datasetViewJList = new JList<KIDSUIDatasetViewComponent>(datasetViewJListModel);
		DatasetViewScrollPane.setViewportView(datasetViewJList);
		datasetViewJList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

		JScrollPane EventScrollPane = new JScrollPane();
		GridBagConstraints gbc_EventScrollPane = new GridBagConstraints();
		gbc_EventScrollPane.insets = new Insets(0, 0, 5, 5);
		gbc_EventScrollPane.fill = GridBagConstraints.BOTH;
		gbc_EventScrollPane.gridx = 0;
		gbc_EventScrollPane.gridy = 1;
		gbc_EventScrollPane.gridheight = 4;
		getContentPane().add(EventScrollPane, gbc_EventScrollPane);
		
		final DefaultListModel<KIDSUIEventComponent> eventJListModel = new DefaultListModel<KIDSUIEventComponent>();
		Set<KIDSUIComponent> eventSet = controller.getKnownIndividuals(controller.EVENTCLASSIRI);
		for (KIDSUIComponent k : eventSet){
			eventJListModel.addElement((KIDSUIEventComponent) k);
		}
		final JList<KIDSUIEventComponent> eventJList = new JList<KIDSUIEventComponent>(eventJListModel);
		EventScrollPane.setViewportView(eventJList);
		
		// Next, build the dataset view list based on the selected event:
		eventJList.addListSelectionListener(new ListSelectionListener(){

			@Override
			public void valueChanged(ListSelectionEvent arg0) {
				// Populate TimePeriodList with those time periods an event can be evaluated over.
				// From isEvaluableDuringTimePeriod property
				if (!arg0.getValueIsAdjusting()){
					datasetViewJListModel.clear();
					KIDSUIEventComponent selected = eventJList.getSelectedValue();
					
					Set<KIDSUIDatasetViewComponent> dvlist = selected.getAvailableDatasetViews();

					for (KIDSUIDatasetViewComponent t : dvlist){
						datasetViewJListModel.addElement(t);
					}
					selectedEvent = selected;
				}
			}
		});
		
		// --------------- Buttons ---------------------//
		
		JButton runButton = new JButton("Run");
		GridBagConstraints gbc_runButton = new GridBagConstraints();
		gbc_runButton.insets = new Insets(0, 0, 0, 5);
		gbc_runButton.gridx = 2;
		gbc_runButton.gridy = 1;
		runButton.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent arg0) {
				StringBuilder newResult = new StringBuilder("New result:");
				// When the button is pressed, do the magic:
				
				// Make sure we have valid selections:
				if (!datasetViewJList.isSelectionEmpty() && !eventJList.isSelectionEmpty()){
					List<KIDSUIDatasetViewComponent> dvlist = datasetViewJList.getSelectedValuesList();
					KIDSUIEventComponent ev = eventJList.getSelectedValue();

					for (KIDSUIDatasetViewComponent dv : dvlist){
						try {
							// 1. Create the dataset:
							ViewLabelDataset vld;
							vld = KIDSDatasetFactory.getViewLabelDatasetFromViewEvent(dv.getIRI(), ev.getIRI(), KIDSEvalDatasetViewsJDialog.this.controller.o);

							// 2. Compute the entropy of the dataset
							double entres = vld.getEntropy();
							
							logme.debug(String.format("Got entropy %f from dataset...", entres));
				
							// Once finished, add results to results window.
							newResult.append(String.format("Entropy for (%s, %s): %f", dv.getIRI().getShortForm(), ev.getIRI().getShortForm(), entres));
						} catch (KIDSOntologyDatatypeValuesException | NumberFormatException
								| KIDSOntologyObjectValuesException | InstantiationException | IllegalAccessException
								| ClassNotFoundException | IOException | UnimplementedIdentifyingFeatureException
								| KIDSUnEvaluableSignalException | KIDSIncompatibleSyntaxException e) {
							logme.error("Something horrible happened: ", e);
							e.printStackTrace();
						}
				
					}
					addResults(newResult.toString());
				}
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
		gbc_closeButton.gridx = 2;
		gbc_closeButton.gridy = 3;
		
		closeButton.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent arg0) {
				// When the button is pressed, close the window.
				KIDSEvalDatasetViewsJDialog.this.dispose();
			}
			
		});
		
		getContentPane().add(closeButton, gbc_closeButton);

		// -------------------- Results ------------------//
		JLabel lblResults = new JLabel("Results:");
		GridBagConstraints gbc_lblResults = new GridBagConstraints();
		gbc_lblResults.insets = new Insets(0, 0, 5, 5);
		gbc_lblResults.gridx = 0;
		gbc_lblResults.gridy = 5;
		getContentPane().add(lblResults, gbc_lblResults);
		
		JScrollPane ResultsScrollPane = new JScrollPane();
		GridBagConstraints gbc_ResultsScrollPane = new GridBagConstraints();
		gbc_ResultsScrollPane.insets = new Insets(0, 0, 5, 5);
		gbc_ResultsScrollPane.fill = GridBagConstraints.BOTH;
		gbc_ResultsScrollPane.gridx = 0;
		gbc_ResultsScrollPane.gridy = 6;
		gbc_ResultsScrollPane.gridwidth = 3;
		getContentPane().add(ResultsScrollPane, gbc_ResultsScrollPane);
		
		ResultsScrollPane.setViewportView(resultJTextPane);
		resultJTextPane.setDropMode(DropMode.INSERT);
		resultJTextPane.setBackground(Color.GRAY);
		resultJTextPane.setForeground(new Color(0, 255, 0));
		resultJTextPane.setEditable(false);
		
		this.pack();
		logme.debug("Packed.");
	}
	
	private void addResults(String newRes){
		StringBuilder sb = new StringBuilder(resultJTextPane.getText());
		sb.append(newRes);
		resultJTextPane.setText(sb.toString());
	}
}
