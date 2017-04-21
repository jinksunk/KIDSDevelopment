/**
 * 
 */
package net.strasnet.kids.ui.gui;

import java.util.concurrent.ArrayBlockingQueue;

import net.strasnet.kids.ui.gui.alerts.KIDSGUIAlert;

/**
 * @author cstras
 * Represents the model of our ABOXBuilderGUI; tracks the state it is in, etc...
 */
public class ABOXBuilderModel {
	
	public enum ABOXBuilderGUIState {
		UNINITIALIZED,
		ABOXLoaded,
		ABOXComplete,
		ABOXIncomplete,
		ABOXModified,
		ABOXSAVED
	}
	
	private ABOXBuilderGUIState currentState = ABOXBuilderGUIState.UNINITIALIZED;
	private ArrayBlockingQueue<KIDSGUIAlert> guilog;
	private ABOXBuilderController controller;
	
	
	public ABOXBuilderModel(ArrayBlockingQueue<KIDSGUIAlert> logMessages, ABOXBuilderController controller) {
		guilog = logMessages;
		this.controller = controller;
	}

	public ABOXBuilderGUIState getState(){
		return currentState;
	}
	
	public void setState(ABOXBuilderGUIState newSt8){
		currentState = newSt8;
	}

}
