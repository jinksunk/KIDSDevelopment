/**
 * 
 */
package net.strasnet.kids.ui.gui.alerts;

import java.awt.Color;

import net.strasnet.kids.KIDSOracle;

/**
 * @author Chris Strasburg
 * 
 * Defines the methods required for each KIDSGUIAlert. These include:
 * - getColor
 * - setColor
 * - toString
 * - setText
 * - checkCondition(OntologyOracle o)
 *
 */
public interface KIDSGUIAlert {
	
	/**
	 * 
	 * @return - The color of this alert message.
	 */
	public Color getColor();
	
	/**
	 * Sets the text color for alerts. In general red is for fatal errors, yellow is for warnings, and green is for information messages.
	 * @param c
	 */
	public void setColor(Color c);
	
	/**
	 * A string representation of this alert. Should generally just return the text.
	 * @return The text of the alert, e.g. as supplied via setText.
	 */
	public String toString();
	
	/**
	 * Sets the text of this alert to the string 'm'
	 * @param m - the string to set the text of this alert to.
	 */
	public void setText(String m);

}
