/**
 * 
 */
package net.strasnet.kids.ui.problems;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.semanticweb.owlapi.model.IRI;

import net.strasnet.kids.ui.gui.ABOXBuilderController;
import net.strasnet.kids.ui.gui.KIDSGUIOracle;
import net.strasnet.kids.ui.problemfixes.KIDSUIAddRelationPossibleFix;
import net.strasnet.kids.ui.problemfixes.KIDSUIPossibleFix;

/**
 * @author cstras
 *
 */
public class KIDSMissingInferredRelationUIProblem extends KIDSUIProblem {

	public static final org.apache.logging.log4j.Logger logme = LogManager.getLogger(KIDSMissingInferredRelationUIProblem.class.getName());
	
	IRI missingProperty;
	IRI missingObjectClass;
	KIDSGUIOracle o;
	
	KIDSMissingInferredRelationUIProblem(String message, ProblemType p, IRI missingProperty, IRI missingClass, KIDSGUIOracle kgo){
		super(message, p);
		ourClass = ProblemClass.MissingRelation;
		this.missingProperty = missingProperty;
		this.missingObjectClass = missingClass;
		o = kgo;
	}
	
	public IRI getMissingProperty(){
		return missingProperty;
	}

	public IRI getMissingObjectClass(){
		return missingObjectClass;
	}
	
	public List<KIDSUIPossibleFix> getPossibleFixes(){
		List<KIDSUIPossibleFix> toReturn = new ArrayList<KIDSUIPossibleFix>();
		
		// Look up the available individuals in our problem's class; add a new fix for each.
		Set<IRI> possibleTargets = o.getIRISetFromNamedIndividualSet(o.getIndividualSet(o.getOwlDataFactory().getOWLClass(getMissingObjectClass())));
		
		for (IRI possibleTarget : possibleTargets){
			KIDSUIAddRelationPossibleFix fix = new KIDSUIAddRelationPossibleFix(
					String.format("Add to %s", possibleTarget.getFragment()),
					this,
					getMissingProperty(),
					possibleTarget
					);
			logme.debug(String.format("Adding fix to map %s %s(%s)", 
					getMissingProperty(),
					possibleTarget,
					getMissingObjectClass()));
			toReturn.add(fix);
		}
		
		// Finally, add an 'add new...' fix:
		KIDSUIAddRelationPossibleFix fix = new KIDSUIAddRelationPossibleFix(
					String.format("Add new %s...", getMissingObjectClass().getFragment()),
					this,
					getMissingProperty(),
					null
					);
		logme.debug(String.format("Adding fix to add new %s (%s)", 
					getMissingProperty(),
					getMissingObjectClass()));
		toReturn.add(fix);

		return toReturn;
	}

}
