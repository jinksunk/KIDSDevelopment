/**
 * 
 */
package net.strasnet.kids.ui.gui.alerts;

import java.awt.Color;

/**
 * @author Chris Strasburg
 *
 */
public class KIDSGUIAlertInfo extends KIDSGUIAlertAbstract {

	public KIDSGUIAlertInfo(String m){
		setText(m);
		setColor(Color.green);
	}

}
