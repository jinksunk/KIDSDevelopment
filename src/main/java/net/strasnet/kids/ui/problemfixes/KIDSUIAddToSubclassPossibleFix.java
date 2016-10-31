/**
 * 
 */
package net.strasnet.kids.ui.problemfixes;

import org.semanticweb.owlapi.model.IRI;

import net.strasnet.kids.ui.problemfixes.KIDSUIPossibleFix.KIDSUIPossibleFixType;
import net.strasnet.kids.ui.problems.KIDSSubclassRequiredUIProblem;
import net.strasnet.kids.ui.problems.KIDSUIProblem;

/**
 * @author cstras
 * Represents a possible fix where we are adding a relation to the KB.
 */
public class KIDSUIAddToSubclassPossibleFix extends KIDSUIPossibleFix {
	
	private IRI ourParent;
	private IRI ourSubclass;
	private IRI individualIRI;
	private KIDSSubclassRequiredUIProblem ourProb;
	
	public KIDSUIAddToSubclassPossibleFix(String message, 
			KIDSSubclassRequiredUIProblem source,
			IRI individualIRI,
			IRI subclassIRI){
		super(message, source);
		ourProb = source;
		type = KIDSUIPossibleFixType.DEFINESUBCLASSFORINDIVIDUAL;
		ourParent = ourProb.getParentClass();
		this.individualIRI = individualIRI;
		this.ourSubclass = subclassIRI;
	}

	public IRI getParentClass(){
		return ourParent;
	}

	public IRI getIndividualIRI() {
		return individualIRI;
	}

	public IRI getSubclassIRI() {
		return ourSubclass;
	}

}
