/**
 * 
 */
package net.strasnet.kids.ui.problemfixes;

import net.strasnet.kids.ui.problems.KIDSUIProblem;

/**
 * @author cstras
 * Represents an available automated (or UI-driven) fix which can be attempted.
 * 
 * The name string is what will be displayed in the menu (e.g. should be short).
 * The problem is a reference to the source problem.
 */
public class KIDSUIPossibleFix {
	
	public enum KIDSUIPossibleFixType {
		ADDRELATIONTOEXISTING,
		ADDRELATIONTONEW,
		MANUAL, 
		ADDDATATYPEVALUE, 
		DEFINESUBCLASSFORINDIVIDUAL;
	};
	
	private String displayMsg;
	private KIDSUIProblem ourProblem;
	protected KIDSUIPossibleFixType type = KIDSUIPossibleFixType.MANUAL;
	
	public KIDSUIPossibleFix(String display, KIDSUIProblem source){
		ourProblem = source;
		displayMsg = display;
	}
	
	public KIDSUIProblem getSourceProblem(){
		return ourProblem;
	}
	
	public String toString(){
		return displayMsg;
	}
	
	public KIDSUIPossibleFixType getType(){
		return type;
	}

}
