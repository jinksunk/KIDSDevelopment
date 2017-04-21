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

public class OpenABOXJDialog extends NewABOXJDialog {

	/**
	 * 
	 */
	public static final org.apache.log4j.Logger logme = LogManager.getLogger(OpenABOXJDialog.class.getName());
	private static final long serialVersionUID = -7218598300316325074L;
	private static final String st8ID = "OpenABOXConfig"; /* Used to identify the ABOX state section */

	public OpenABOXJDialog(JFrame frame, ABOXBuilderState myst8) {
		super(frame, myst8);
	}

}
