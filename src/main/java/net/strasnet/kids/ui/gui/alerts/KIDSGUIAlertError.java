/**
 * 
 */
package net.strasnet.kids.ui.gui.alerts;

import java.awt.Color;

/**
 * @author Chris Strasburg
 *
 */
public class KIDSGUIAlertError extends KIDSGUIAlertAbstract {

	public KIDSGUIAlertError(String m){
		setText(m);
		setColor(Color.red);
	}

}
