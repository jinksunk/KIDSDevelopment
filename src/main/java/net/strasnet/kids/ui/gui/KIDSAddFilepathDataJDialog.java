/**
 * 
 */
package net.strasnet.kids.ui.gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import org.apache.log4j.LogManager;
import org.semanticweb.owlapi.model.IRI;

/**
 * @author cstras
 *
 */
public class KIDSAddFilepathDataJDialog extends KIDSAddDataJDialog {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 5851375089243455872L;
	public static final org.apache.log4j.Logger logme = LogManager.getLogger(KIDSAddFilepathDataJDialog.class.getName());

	/**
	 * 
	 * @param parent - The container parent to attach the dialog to
	 * @param aboxiri - the IRI of the ABOX to associate this individual with
	 */
	public KIDSAddFilepathDataJDialog(JFrame parent, 
			IRI aboxiri, 
			IRI subjectiri,
			IRI datapropiri,
			ABOXBuilderController controller) {
		super(parent, aboxiri, subjectiri, datapropiri, controller);

		setBounds(100, 100, 450, 300);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setLayout(new FlowLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		{
			JLabel lblSignalIri = new JLabel(String.format("%s %s value: ", ourIndIRI.getFragment(),
					ourDataPropIRI.getFragment()));
			contentPanel.add(lblSignalIri);
		}
		{
			dataValueTextField = new JTextField();
			contentPanel.add(dataValueTextField);
			dataValueTextField.setColumns(10);
		}
		{
			JButton ChooseABOXFileLocationButton = new JButton("Choose...");
			ChooseABOXFileLocationButton.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					// Load the JFileChooser dialog box:
					JFileChooser choose = new JFileChooser();
					int returnVal = choose.showOpenDialog(contentPanel);
					if (returnVal == JFileChooser.APPROVE_OPTION){
						dataValueTextField.setText(choose.getSelectedFile().getAbsolutePath());
					}
				}
			});
			contentPanel.add(ChooseABOXFileLocationButton);
			
		}
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton okButton = new JButton("OK");
				okButton.setActionCommand("OK");
				okButton.addMouseListener(getMouseAdapter());
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
			}
			{
				JButton cancelButton = new JButton("Cancel");
				cancelButton.setActionCommand("Cancel");
				buttonPane.add(cancelButton);
			}
		}
	}
	
	/**
	 * Subclasses should override this method to check string values.
	 * 
	 * @return A mouse adapter which includes defined checks for submitted values.
	 */
	protected MouseAdapter getMouseAdapter(){
		return new MouseAdapter() {
					@Override
					public void mouseClicked(MouseEvent e) {
						String dv = dataValueTextField.getText();
						logme.debug(String.format("FILE dialog; checking file exists and is readable."));
						// Nothing to check for String values
						File f = new File(dv);
						if (!f.exists()){
							JOptionPane.showMessageDialog(KIDSAddFilepathDataJDialog.this, 
									String.format("Entered value %s is not an existing file. Please"
											+ " verify the file path specification.", 
											dv));
							return;
						}
						if (!f.isFile()){
							JOptionPane.showMessageDialog(KIDSAddFilepathDataJDialog.this, 
									String.format("Entered value %s does not point to a plain file. Please"
											+ " verify the file path specification.", 
											dv));
							return;
						}
						if (!f.canRead()){
							JOptionPane.showMessageDialog(KIDSAddFilepathDataJDialog.this, 
									String.format("Entered value %s is not readable. Please"
											+ " verify the file path specification.", 
											dv));
							return;
						}
						logme.debug(String.format("%s is a valid file path.", dv));
						dispose();
					}
					
				};
	}

}
