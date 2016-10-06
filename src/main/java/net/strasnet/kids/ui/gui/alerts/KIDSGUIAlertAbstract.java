/**
 * 
 */
package net.strasnet.kids.ui.gui.alerts;

import java.awt.Color;

import net.strasnet.kids.KIDSOracle;

/**
 * @author Chris Strasburg
 *
 */
public abstract class KIDSGUIAlertAbstract implements KIDSGUIAlert {
	protected Color myColor = null;
	protected String myMessage = null;


	/* (non-Javadoc)
	 * @see net.strasnet.kids.ui.gui.alerts.KIDSGUIAlert#getColor()
	 */
	@Override
	public Color getColor() {
		return myColor;
	}

	/* (non-Javadoc)
	 * @see net.strasnet.kids.ui.gui.alerts.KIDSGUIAlert#setColor(java.awt.Color)
	 */
	@Override
	public void setColor(Color c) {
		myColor = c;
	}

	/* (non-Javadoc)
	 * @see net.strasnet.kids.ui.gui.alerts.KIDSGUIAlert#setText(java.lang.String)
	 */
	@Override
	public void setText(String m) {
		myMessage = m;
	}

}
