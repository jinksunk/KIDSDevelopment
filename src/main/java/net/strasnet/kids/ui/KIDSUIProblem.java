/**
 * 
 */
package net.strasnet.kids.ui;

import java.util.ArrayList;
import java.util.List;

/**
 * @author cstras
 * This class represents a 'problem' with a KIDSUI component. It includes a String message, a problem
 * level (Requirement, Warning), and 
 */
public class KIDSUIProblem {

	public enum ProblemType {
		REQUIRED, WARNING
	}
	
	public enum ProblemClass {
		MissingRelation,
		MissingSubclassSpecification,
		UnspecifiedClass
	}

	private String msg;
	protected ProblemType type;
	protected ProblemClass ourClass;
	
	public KIDSUIProblem (String msg, ProblemType p){
		this.msg = msg;
		type = p;
		ourClass = ProblemClass.UnspecifiedClass;
	}
	
	public ProblemType getType(){
		return type;
	}
	
	public ProblemClass getClassOfProblem(){
		return ourClass;
	}
	
	public String toString(){
		return msg;
	}
	
	public List<KIDSUIPossibleFix> getPossibleFixes(){
		List<KIDSUIPossibleFix> toReturn = new ArrayList<KIDSUIPossibleFix>();
		
		toReturn.add(new KIDSUIPossibleFix("No fixes available.", this));
		
		return toReturn;
	}
	
}
