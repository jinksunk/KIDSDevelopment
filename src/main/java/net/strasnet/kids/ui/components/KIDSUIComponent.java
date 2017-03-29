/**
 * 
 */
package net.strasnet.kids.ui.components;

import java.util.List;
import java.util.Set;

import org.semanticweb.owlapi.model.IRI;

import net.strasnet.kids.ui.components.KIDSUIAbstractComponent.KIDSComponentDefinition;
import net.strasnet.kids.ui.problems.KIDSUIProblem;

/**
 * @author cstras
 * Defines the methods that all KIDSUI components must implement. These include, e.g.:
 * <UL>
 *   <LI> getRequiredObjectProperties() </LI>
 *   <LI> getRequiredDataProperties() </LI>
 * </UL>
 */
public interface KIDSUIComponent {

	/**
	 * This method will return a Set of Problem objects for this component.
	 * @return
	 */
	public Set<KIDSUIProblem> getComponentProblems();

	/**
	 * 
	 * @return - The IRI of the individual represented by this component
	 */
	public IRI getIRI();
	
	/**
	 * 
	 * @return - Get a list of relation objects.
	 */
	public List<KIDSUIRelation> getRelations();

	/**
	 * 
	 * @return - The self-identified defining location for this component.
	 */
	KIDSComponentDefinition getDefiningLocation();

}
