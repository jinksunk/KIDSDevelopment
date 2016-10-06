/**
 * 
 */
package net.strasnet.kids.ui.gui.alerts;

import java.awt.Color;

/**
 * @author Chris Strasburg
 *
 */
public class KIDSGUIAlertWarning extends KIDSGUIAlertAbstract {

	public KIDSGUIAlertWarning(String m){
		this.myMessage = m;
		this.myColor = Color.yellow;
	}

}
