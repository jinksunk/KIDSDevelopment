/**
 * 
 */
package net.strasnet.kids.ui;

import java.util.Set;

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

}
