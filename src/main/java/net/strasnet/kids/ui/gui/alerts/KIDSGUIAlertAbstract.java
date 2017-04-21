/**
 * 
 */
package net.strasnet.kids.ui.gui.alerts;

import java.awt.Color;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

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

		// Create an instance of SimpleDateFormat used for formatting 
		// the string representation of date (month/day/year)
		DateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");

		// Get the date today using Calendar object.
		Date today = Calendar.getInstance().getTime();        

		myMessage = String.format(String.format("[%s] - %s", df.format(today), m));
	}
	
	public String toString(){
		return myMessage;
	}

}
