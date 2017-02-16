/**
 * 
 */
package net.strasnet.kids.ui.problems;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.log4j.LogManager;
import org.semanticweb.owlapi.model.IRI;

import net.strasnet.kids.ui.gui.ABOXBuilderController;
import net.strasnet.kids.ui.gui.KIDSGUIOracle;
import net.strasnet.kids.ui.problemfixes.KIDSUIAddRelationPossibleFix;
import net.strasnet.kids.ui.problemfixes.KIDSUIAddToSubclassPossibleFix;
import net.strasnet.kids.ui.problemfixes.KIDSUIPossibleFix;

/**
 * @author cstras
 *
 */
public class KIDSSubclassRequiredUIProblem extends KIDSUIProblem {

	public static final org.apache.log4j.Logger logme = LogManager.getLogger(KIDSSubclassRequiredUIProblem.class.getName());
	
	IRI parentClass;
	IRI problemIndividual;
	KIDSGUIOracle o;
	
	public KIDSSubclassRequiredUIProblem(String message, ProblemType p, IRI parentClass, IRI problemIndividual, KIDSGUIOracle kgo){
		super(message, p);
		ourClass = ProblemClass.MissingSubclassSpecification;
		this.parentClass = parentClass;
		this.problemIndividual = problemIndividual;
		o = kgo;
	}
	
	public IRI getParentClass(){
		return parentClass;
	}

	public IRI getProblemIndividual(){
		return problemIndividual;
	}
	
	public List<KIDSUIPossibleFix> getPossibleFixes(){
		List<KIDSUIPossibleFix> toReturn = new ArrayList<KIDSUIPossibleFix>();
		
		// Look up the available subclasses in our parent class; add a new fix for each.
		Set<IRI> possibleTargets = o.getStrictSubclasses(parentClass);
		logme.debug(String.format("Found %d subclasses of class %s", 
				possibleTargets.size(),
				parentClass));
		
		for (IRI possibleTarget : possibleTargets){
			KIDSUIAddToSubclassPossibleFix fix = new KIDSUIAddToSubclassPossibleFix(
					String.format("Add to subclass %s", possibleTarget.getFragment()),
					this,
					getProblemIndividual(),
					possibleTarget
					);
			logme.debug(String.format("Adding fix to map: %s", 
					possibleTarget));
			toReturn.add(fix);
		}
		
		return toReturn;
	}

}
