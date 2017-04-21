/**
 * 
 */
package net.strasnet.kids.ui.problemfixes;

import org.semanticweb.owlapi.model.IRI;

import net.strasnet.kids.ui.components.KIDSUIAbstractComponent.KIDSDatatypeClass;
import net.strasnet.kids.ui.problemfixes.KIDSUIPossibleFix.KIDSUIPossibleFixType;
import net.strasnet.kids.ui.problems.KIDSMissingDataPropertyUIProblem;

/**
 * @author cstras
 * Represents a possible fix where we are adding a relation to the KB.
 */
public class KIDSUIAddDatatypePropertyPossibleFix extends KIDSUIPossibleFix {
	
	private KIDSMissingDataPropertyUIProblem ourProb;
	private IRI ourSub;
	
	public KIDSUIAddDatatypePropertyPossibleFix(String message, 
			KIDSMissingDataPropertyUIProblem source,
			IRI problemSubject){
		super(message, source);
		ourProb = source;
		type = KIDSUIPossibleFixType.ADDDATATYPEVALUE;
		ourSub = problemSubject;
	}

	public IRI getRelation(){
		return ourProb.getMissingProperty();
	}

	public KIDSDatatypeClass getDatatypeClass(){
		return ourProb.getMissingDatatypeClass();
	}
	
	public IRI getSubjectIRI(){
		return ourSub;
	}

}
