/**
 * 
 */
package net.strasnet.kids.ui.problems;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.log4j.LogManager;
import org.semanticweb.owlapi.model.IRI;

import net.strasnet.kids.ui.components.KIDSUIAbstractComponent.KIDSDatatypeClass;
import net.strasnet.kids.ui.gui.ABOXBuilderController;
import net.strasnet.kids.ui.gui.KIDSGUIOracle;
import net.strasnet.kids.ui.problemfixes.KIDSUIAddDatatypePropertyPossibleFix;
import net.strasnet.kids.ui.problemfixes.KIDSUIAddRelationPossibleFix;
import net.strasnet.kids.ui.problemfixes.KIDSUIPossibleFix;

/**
 * @author cstras
 *
 */
public class KIDSMissingDataPropertyUIProblem extends KIDSUIProblem {

	public static final org.apache.log4j.Logger logme = LogManager.getLogger(KIDSMissingDataPropertyUIProblem.class.getName());
	
	IRI missingProperty;
	IRI ourInd;
	KIDSDatatypeClass missingDatatypeClass;
	KIDSGUIOracle o;
	
	public KIDSMissingDataPropertyUIProblem(String message, ProblemType p, 
											IRI ourIndividual, KIDSDatatypeClass datatypeClass, 
											IRI missingProperty, KIDSGUIOracle kgo){
		super(message, p);
		ourClass = ProblemClass.MissingDataProperty;
		ourInd = ourIndividual;
		this.missingProperty = missingProperty;
		this.missingDatatypeClass = datatypeClass;
		o = kgo;
	}
	
	public IRI getMissingProperty(){
		return missingProperty;
	}

	public KIDSDatatypeClass getMissingDatatypeClass(){
		return missingDatatypeClass;
	}
	
	public List<KIDSUIPossibleFix> getPossibleFixes(){
		List<KIDSUIPossibleFix> toReturn = new ArrayList<KIDSUIPossibleFix>();
		
		// The only real option here is to add the property with a new value:
		// Add an 'add new...' fix:
		KIDSUIAddDatatypePropertyPossibleFix fix = new KIDSUIAddDatatypePropertyPossibleFix(
					String.format("Add new %s value...", getMissingProperty().getFragment()),
					this, ourInd
					);
		logme.debug(String.format("Adding fix to add new %s value", 
					getMissingProperty()));
		toReturn.add(fix);

		return toReturn;
	}

}
