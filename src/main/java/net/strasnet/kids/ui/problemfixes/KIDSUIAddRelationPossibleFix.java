/**
 * 
 */
package net.strasnet.kids.ui.problemfixes;

import org.semanticweb.owlapi.model.IRI;

import net.strasnet.kids.ui.problemfixes.KIDSUIPossibleFix.KIDSUIPossibleFixType;
import net.strasnet.kids.ui.problems.KIDSUIProblem;

/**
 * @author cstras
 * Represents a possible fix where we are adding a relation to the KB.
 */
public class KIDSUIAddRelationPossibleFix extends KIDSUIPossibleFix {
	
	private IRI ourPredicate;
	private IRI ourObject;
	
	public KIDSUIAddRelationPossibleFix(String message, KIDSUIProblem source, IRI relation, IRI object){
		super(message, source);
		type = KIDSUIPossibleFixType.ADDRELATIONTOEXISTING;
		ourPredicate = relation;
		ourObject = object;
		if (ourObject == null){
			type = KIDSUIPossibleFixType.ADDRELATIONTONEW;
		}
	}

	public IRI getRelation(){
		return ourPredicate;
	}

	public IRI getObject(){
		return ourObject;
	}

}
