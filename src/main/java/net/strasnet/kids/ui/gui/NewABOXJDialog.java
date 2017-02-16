package net.strasnet.kids.ui.gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import java.awt.GridLayout;

import javax.swing.JLabel;
import javax.swing.border.EtchedBorder;

import org.apache.log4j.LogManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import net.strasnet.kids.KIDSOracle;

import javax.swing.JTextField;

import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;

import javax.swing.JFileChooser;
import javax.swing.BoxLayout;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class NewABOXJDialog extends JDialog {

	/**
	 * 
	 */
	public static final org.apache.log4j.Logger logme = LogManager.getLogger(NewABOXJDialog.class.getName());
	protected static final long serialVersionUID = -7218598300316325074L;
	protected static final String st8ID = "NewABOXConfig"; /* Used to identify the ABOX state section */

	protected final JPanel contentPanel = new JPanel();
	protected ABOXBuilderState myst8 = null;
	protected JTextField TBOXIRITextField;
	protected JTextField ABOXIRITextField;
	protected JTextField TBOXLocationTextField;
	protected JTextField ABOXLocationTextField;
	
	protected Map<String, JTextField> configKeyMap;

	/**
	 * Launch the application.
	public static void main(String[] args) {
		try {
			NewABOXJDialog dialog = new NewABOXJDialog();
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			dialog.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	*/
	
	/**
	 * Some getters to get the value of the text fields:
	 */
	public String getTBOXIRI(){
		return TBOXIRITextField.getText();
	}
	public String getABOXIRI(){
		return ABOXIRITextField.getText();
	}
	public String getTBOXLocationTextField(){
		return TBOXLocationTextField.getText();
	}
	public String getABOXLocationTextField(){
		return ABOXLocationTextField.getText();
	}

	/**
	 * Create the dialog.
	 * @param myst8 - The state storage object, for remembering state between runs.
	 * @param frame 
	 */
	public NewABOXJDialog(JFrame frame, ABOXBuilderState myst8){
		super(frame);
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosed(WindowEvent e) {
				storeABOXConfigValues();
			}
		});
		this.myst8 = myst8;
		configKeyMap = new HashMap<String, JTextField>();


		setBounds(100, 100, 513, 191);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		getContentPane().add(contentPanel, BorderLayout.NORTH);
		GridBagLayout gbl_contentPanel = new GridBagLayout();
		gbl_contentPanel.columnWidths = new int[] {0, 0};
		gbl_contentPanel.rowHeights = new int[] {0, 0, 0, 0, 0};
		gbl_contentPanel.columnWeights = new double[]{0.0, 1.0};
		gbl_contentPanel.rowWeights = new double[]{0.0, 1.0, 0.0, 1.0};
		contentPanel.setLayout(gbl_contentPanel);
		{
			JLabel lblTboxIri = new JLabel("TBOX IRI");
			GridBagConstraints gbc_lblTboxIri = new GridBagConstraints();
			gbc_lblTboxIri.anchor = GridBagConstraints.WEST;
			gbc_lblTboxIri.fill = GridBagConstraints.VERTICAL;
			gbc_lblTboxIri.insets = new Insets(0, 0, 5, 5);
			gbc_lblTboxIri.gridx = 0;
			gbc_lblTboxIri.gridy = 0;
			contentPanel.add(lblTboxIri, gbc_lblTboxIri);
		}
		{
			TBOXIRITextField = new JTextField();
			GridBagConstraints gbc_TBOXIRITextField = new GridBagConstraints();
			gbc_TBOXIRITextField.insets = new Insets(0, 0, 5, 0);
			gbc_TBOXIRITextField.fill = GridBagConstraints.BOTH;
			gbc_TBOXIRITextField.gridx = 1;
			gbc_TBOXIRITextField.gridy = 0;
			contentPanel.add(TBOXIRITextField, gbc_TBOXIRITextField);
			TBOXIRITextField.setColumns(10);
			this.configKeyMap.put("TBOXIRI", TBOXIRITextField);
		}
		{
			JLabel lblTboxLocation = new JLabel("TBOX Location");
			GridBagConstraints gbc_lblTboxLocation = new GridBagConstraints();
			gbc_lblTboxLocation.anchor = GridBagConstraints.WEST;
			gbc_lblTboxLocation.fill = GridBagConstraints.VERTICAL;
			gbc_lblTboxLocation.insets = new Insets(0, 0, 5, 5);
			gbc_lblTboxLocation.gridx = 0;
			gbc_lblTboxLocation.gridy = 1;
			contentPanel.add(lblTboxLocation, gbc_lblTboxLocation);
		}
		{
			JPanel panel = new JPanel();
			GridBagConstraints gbc_panel = new GridBagConstraints();
			gbc_panel.insets = new Insets(0, 0, 5, 0);
			gbc_panel.fill = GridBagConstraints.BOTH;
			gbc_panel.gridx = 1;
			gbc_panel.gridy = 1;
			contentPanel.add(panel, gbc_panel);
			panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
			{
				TBOXLocationTextField = new JTextField();
				TBOXLocationTextField.addFocusListener(new FocusAdapter() {
					@Override
					public void focusLost(FocusEvent e) {
						loadIRIFromFile(TBOXLocationTextField, TBOXIRITextField);
					}
				});
				panel.add(TBOXLocationTextField);
				TBOXLocationTextField.setColumns(10);
				this.configKeyMap.put("TBOXLocation", TBOXLocationTextField);
			}
			{
				JButton ChooseTBOXFileLocationButton = new JButton("Choose...");
				ChooseTBOXFileLocationButton.addMouseListener(new MouseAdapter() {
					@Override
					public void mouseClicked(MouseEvent e) {
						// Load the JFileChooser dialog box:
						JFileChooser choose = new JFileChooser();
						initFileChooserFromTextBox(choose, TBOXLocationTextField);
						int returnVal = choose.showOpenDialog(contentPanel);
						if (returnVal == JFileChooser.APPROVE_OPTION){
							TBOXLocationTextField.setText(choose.getSelectedFile().getAbsolutePath());
							loadIRIFromFile(TBOXLocationTextField, TBOXIRITextField);
						}
						
					}

				});
				panel.add(ChooseTBOXFileLocationButton);
			}
		}
		{
			JLabel lblAboxIri = new JLabel("ABOX IRI");
			GridBagConstraints gbc_lblAboxIri = new GridBagConstraints();
			gbc_lblAboxIri.anchor = GridBagConstraints.WEST;
			gbc_lblAboxIri.insets = new Insets(0, 0, 5, 5);
			gbc_lblAboxIri.gridx = 0;
			gbc_lblAboxIri.gridy = 2;
			contentPanel.add(lblAboxIri, gbc_lblAboxIri);
		}
		{
			ABOXIRITextField = new JTextField();
			GridBagConstraints gbc_ABOXIRITextField = new GridBagConstraints();
			gbc_ABOXIRITextField.insets = new Insets(0, 0, 5, 0);
			gbc_ABOXIRITextField.fill = GridBagConstraints.HORIZONTAL;
			gbc_ABOXIRITextField.gridx = 1;
			gbc_ABOXIRITextField.gridy = 2;
			contentPanel.add(ABOXIRITextField, gbc_ABOXIRITextField);
			ABOXIRITextField.setColumns(10);
			this.configKeyMap.put("ABOXIRI", ABOXIRITextField);
		}
		{
			JLabel lblAboxLocation = new JLabel("ABOX Location");
			GridBagConstraints gbc_lblAboxLocation = new GridBagConstraints();
			gbc_lblAboxLocation.insets = new Insets(0, 0, 0, 5);
			gbc_lblAboxLocation.gridx = 0;
			gbc_lblAboxLocation.gridy = 3;
			contentPanel.add(lblAboxLocation, gbc_lblAboxLocation);
		}
		{
			JPanel panel = new JPanel();
			GridBagConstraints gbc_panel = new GridBagConstraints();
			gbc_panel.fill = GridBagConstraints.BOTH;
			gbc_panel.gridx = 1;
			gbc_panel.gridy = 3;
			contentPanel.add(panel, gbc_panel);
			panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
			{
				ABOXLocationTextField = new JTextField();
				ABOXLocationTextField.addFocusListener(new FocusAdapter() {
					@Override
					public void focusLost(FocusEvent e) {
						logme.debug("Lost focus on ABOXLocationTextField.");
						loadIRIFromFile(ABOXLocationTextField, ABOXIRITextField);
					}
				});
				panel.add(ABOXLocationTextField);
				ABOXLocationTextField.setColumns(10);
				this.configKeyMap.put("ABOXLocation", ABOXLocationTextField);
			}
			{
				JButton ChooseABOXFileLocationButton = new JButton("Choose...");
				ChooseABOXFileLocationButton.addMouseListener(new MouseAdapter() {
					@Override
					public void mouseClicked(MouseEvent e) {
						// Load the JFileChooser dialog box:
						JFileChooser choose = new JFileChooser();
						initFileChooserFromTextBox(choose, ABOXLocationTextField);
						int returnVal = choose.showOpenDialog(contentPanel);
						if (returnVal == JFileChooser.APPROVE_OPTION){
							ABOXLocationTextField.setText(choose.getSelectedFile().getAbsolutePath());
							loadIRIFromFile(ABOXLocationTextField, ABOXIRITextField);
						}
					}
				});
				panel.add(ChooseABOXFileLocationButton);
			}
		}
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton okButton = new JButton("OK");
				okButton.addMouseListener(new MouseAdapter() {
					@Override
					public void mouseClicked(MouseEvent e) {
						//TODO: Check input fields here to make sure they make sense
						dispose();
					}
				});
				okButton.setActionCommand("OK");
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
			}
			{
				JButton cancelButton = new JButton("Cancel");
				cancelButton.addMouseListener(new MouseAdapter() {
					@Override
					public void mouseClicked(MouseEvent e) {
						dispose();
					}
				});
				cancelButton.setActionCommand("Cancel");
				buttonPane.add(cancelButton);
			}
		}

		/* Populate the registered fields, if appropriate. */
		loadABOXConfigValues();
	}
	
	void loadIRIFromFile(JTextField FileField, JTextField IRIField){
		// If the file exists, populate the IRI field from it:
		if (!FileField.getText().isEmpty()){
			File tFile = new File(FileField.getText());
			if (tFile.canRead()){
				try {
					if (IRIField.getText().isEmpty())
					    IRIField.setText(KIDSOracle.getOntologyIRI(IRI.create(tFile)).toString());
				} catch (OWLOntologyCreationException e1) {
					IRIField.setText("[UNREADABLE FILE SPECIFIED]");
				}
			}
		}
	}

	/**
	 * The ABOXConfig section needs to be a Map, storing string values.
	 */
	void loadABOXConfigValues(){
		Map<String, String> cvals = this.myst8.getConfigSection(NewABOXJDialog.st8ID);
		
		for (String k : this.configKeyMap.keySet()){
			if (cvals.containsKey(k)){
				JTextField v = this.configKeyMap.get(k);
				v.setText(cvals.get(k));
			}
		}
	}
	
	/**
	 * This should be called when the Dialog closes.
	 */
	void storeABOXConfigValues(){
		logme.debug(String.format("Storing ABOX config values"));
		Map<String, String> cvals = this.myst8.getConfigSection(NewABOXJDialog.st8ID);

		for (String k : this.configKeyMap.keySet()){
			JTextField v = this.configKeyMap.get(k);
			cvals.put(k, v.getText());
		}

		this.myst8.putConfigSection(st8ID, cvals);
	}

	/**
	 * 
	 * @param choose - The chooser we are initializing
	 * @param LocationTextField The text field we are initializing from
	 */
	void initFileChooserFromTextBox(JFileChooser choose, JTextField LocationTextField) {
		if (!LocationTextField.getText().isEmpty()){
			String path = LocationTextField.getText();
			File fpath = new File(path);
			if (fpath.exists()){
				if (fpath.isFile()){
					choose.setSelectedFile(fpath);
				} else if (fpath.isDirectory()){
					choose.setCurrentDirectory(fpath);
				}
			}
		}
	}
}
