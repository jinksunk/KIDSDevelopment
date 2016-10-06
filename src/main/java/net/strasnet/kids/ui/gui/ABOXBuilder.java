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
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.DefaultMutableTreeNode;

import net.strasnet.kids.ui.gui.alerts.KIDSGUIAlert;

import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.concurrent.ArrayBlockingQueue;
import javax.swing.JLabel;
import java.awt.Insets;
import javax.swing.JTextPane;
import java.awt.Color;
import javax.swing.DropMode;

public class ABOXBuilder {

	private JFrame frame;
	private ABOXBuilderController controller;
	private ArrayBlockingQueue<KIDSGUIAlert> logMessages;

	/**
	 * Launch the application.
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

		frame = new JFrame();
		frame.setBounds(100, 100, 648, 406);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		JMenuBar menuBar = new JMenuBar();
		frame.setJMenuBar(menuBar);
		
		JMenu mnFile = new JMenu("File");
		menuBar.add(mnFile);
		
		JMenuItem mntmNew = new JMenuItem("New");
		mntmNew.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// Open a JDialogue which prompts for TBOX location, IRI, and desired ABOX IRI; initiate a new ABOX
				NewABOXJDialog getStarted = new NewABOXJDialog(frame);
				getStarted.setModal(true);
				getStarted.setVisible(true);
				
				try {
					controller.init(getStarted.getTBOXLocationTextField(), 
								getStarted.getTBOXIRI(), 
								getStarted.getABOXLocationTextField(),
								getStarted.getABOXIRI());
					JOptionPane.showMessageDialog(frame, "Ontology loaded!");
				} catch (OWLOntologyCreationException e1) {
					JOptionPane.showMessageDialog(frame, "Uh-oh - an exception occurred when loading the ontology: " + e1.getMessage());
				}
			}
		});
		mnFile.add(mntmNew);
		
		JMenuItem mntmOpen = new JMenuItem("Open");
		mnFile.add(mntmOpen);
		
		JMenuItem mntmSave = new JMenuItem("Save");
		mnFile.add(mntmSave);
		
		JMenu mnEdit = new JMenu("Edit");
		menuBar.add(mnEdit);
		
		JMenuItem mntmAddEvent = new JMenuItem("Add Event");
		mntmAddEvent.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// Create the AddEventJDialog to create the event
				AddEventJDialog newEvent = new AddEventJDialog(frame);
				newEvent.setModal(true);
				newEvent.setVisible(true);
				
				controller.addEvent(newEvent.getEventIRIString());

				//TODO: Update log panel instead:
				JOptionPane.showMessageDialog(frame, "Event added!");
			}
		});
		mnEdit.add(mntmAddEvent);
		
		JMenuItem mntmAddSignal = new JMenuItem("Add Signal");
		mnEdit.add(mntmAddSignal);
		
		JMenu mnModelActions = new JMenu("Model Actions");
		menuBar.add(mnModelActions);
		
		JMenuItem mntmValidateModel = new JMenuItem("Validate Model");
		mnModelActions.add(mntmValidateModel);
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0};
		gridBagLayout.rowHeights = new int[] {0, 0, 0};
		gridBagLayout.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{1.0, Double.MIN_VALUE, 1.0};
		frame.getContentPane().setLayout(gridBagLayout);
		
		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		GridBagConstraints gbc_tabbedPane = new GridBagConstraints();
		gbc_tabbedPane.insets = new Insets(0, 0, 5, 0);
		gbc_tabbedPane.fill = GridBagConstraints.BOTH;
		gbc_tabbedPane.gridx = 0;
		gbc_tabbedPane.gridy = 0;
		frame.getContentPane().add(tabbedPane, gbc_tabbedPane);
		
		JPanel EventPanel = new JPanel();
		tabbedPane.addTab("Events", null, EventPanel, null);
		
		JSplitPane EventSplitPane = new JSplitPane();
		EventPanel.add(EventSplitPane);
		
		JScrollPane EventScrollPane = new JScrollPane();
		EventSplitPane.setLeftComponent(EventScrollPane);
		
		JList EventJList = new JList();
		EventScrollPane.setViewportView(EventJList);
		
		JScrollPane EventModelScrollPane = new JScrollPane();
		EventSplitPane.setRightComponent(EventModelScrollPane);
		
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
		
		JPanel DatasetPanel = new JPanel();
		tabbedPane.addTab("Datasets", null, DatasetPanel, null);
		
		JSplitPane DatasetSplitPane = new JSplitPane();
		DatasetPanel.add(DatasetSplitPane);
		
		JScrollPane DatasetScrollPane = new JScrollPane();
		DatasetSplitPane.setLeftComponent(DatasetScrollPane);
		
		JList DatasetJList = new JList();
		DatasetScrollPane.setViewportView(DatasetJList);
		
		JScrollPane DatasetModelScrollPane = new JScrollPane();
		DatasetSplitPane.setRightComponent(DatasetModelScrollPane);
		
		JTree DatasetModelTree = new JTree();
		DatasetModelTree.setModel(new DefaultTreeModel(
			new DefaultMutableTreeNode("Dataset") {
				{
					DefaultMutableTreeNode node_1;
					node_1 = new DefaultMutableTreeNode("Required");
						node_1.add(new DefaultMutableTreeNode("Dataset Location"));
						node_1.add(new DefaultMutableTreeNode("Dataset Parser Implementation"));
					add(node_1);
					node_1 = new DefaultMutableTreeNode("Inferred");
						node_1.add(new DefaultMutableTreeNode("Includes Time Period"));
						node_1.add(new DefaultMutableTreeNode("Has Event Included"));
						node_1.add(new DefaultMutableTreeNode("Is Evaluation of Event"));
						node_1.add(new DefaultMutableTreeNode("Is Container of Context"));
						node_1.add(new DefaultMutableTreeNode("Is Compatible Dataset for Signal"));
						node_1.add(new DefaultMutableTreeNode("Is Viewable as Dataset View"));
					add(node_1);
					add(new DefaultMutableTreeNode("Class Membership"));
				}
			}
		));
		DatasetModelScrollPane.setViewportView(DatasetModelTree);
		
		JLabel lblLogs = new JLabel("Logs");
		GridBagConstraints gbc_lblLogs = new GridBagConstraints();
		gbc_lblLogs.insets = new Insets(0, 0, 5, 0);
		gbc_lblLogs.gridx = 0;
		gbc_lblLogs.gridy = 1;
		frame.getContentPane().add(lblLogs, gbc_lblLogs);
		
		JTextPane logLines = new JTextPane();
		logLines.setDropMode(DropMode.INSERT);
		logLines.setBackground(Color.GRAY);
		logLines.setForeground(new Color(0, 255, 0));
		logLines.setEditable(false);
		GridBagConstraints gbc_logLines = new GridBagConstraints();
		gbc_logLines.fill = GridBagConstraints.BOTH;
		gbc_logLines.gridx = 0;
		gbc_logLines.gridy = 2;
		frame.getContentPane().add(logLines, gbc_logLines);
	}

}
