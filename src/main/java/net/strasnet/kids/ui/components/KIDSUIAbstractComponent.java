/**
 * 
 */
package net.strasnet.kids.ui.components;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.tree.DefaultMutableTreeNode;

import org.apache.log4j.LogManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLDataFactory;

import net.strasnet.kids.ui.KIDSUIInferredProperty;
import net.strasnet.kids.ui.KIDSUIRequiredDataProperty;
import net.strasnet.kids.ui.KIDSUIRequiredProperty;
import net.strasnet.kids.ui.gui.KIDSGUIOracle;
import net.strasnet.kids.ui.problems.KIDSMissingDataPropertyUIProblem;
import net.strasnet.kids.ui.problems.KIDSMissingRelationUIProblem;
import net.strasnet.kids.ui.problems.KIDSSubclassRequiredUIProblem;
import net.strasnet.kids.ui.problems.KIDSUIProblem;

/**
 * @author cstras
 *
 * Implementation of common methods for implementers of KIDSUIComponent
 */
public abstract class KIDSUIAbstractComponent implements KIDSUIComponent {

	public static final org.apache.log4j.Logger logme = LogManager.getLogger(KIDSUIAbstractComponent.class.getName());
	
	public enum KIDSDatatypeClass {
		STRING,
		JAVA, 
		FILEPATH
	};
	
	public enum KIDSComponentDefinition {
		TBOX,
		ABOX
	};

	protected IRI myIRI = null;
	protected Set<KIDSUIInferredProperty> myInfProps;
	protected Set<KIDSUIRequiredProperty> myReqProps;
	protected Set<KIDSUIRequiredDataProperty> myDataProps;
	protected String ABOXIRI;
	protected IRI TBOXIRI;
	protected KIDSGUIOracle o;
	protected OWLDataFactory owldf;
	protected IRI requiredSubclassOf;
	protected KIDSComponentDefinition deflocation;

	public KIDSUIAbstractComponent(IRI myID, KIDSGUIOracle o){
		myIRI = myID;
		TBOXIRI = o.getTBOXIRI();
		ABOXIRI = myIRI.getNamespace();
		this.o = o;
		this.owldf = o.getOwlDataFactory();
		this.requiredSubclassOf = null;
		
		myReqProps = new HashSet<KIDSUIRequiredProperty>();
		myInfProps = new HashSet<KIDSUIInferredProperty>();
		myDataProps = new HashSet<KIDSUIRequiredDataProperty>();
		
		// By default, set defining component location to the TBOX:
		this.deflocation = KIDSComponentDefinition.TBOX;
		
	}
	
	@Override
	public Set<KIDSUIProblem> getComponentProblems() {
		HashSet<KIDSUIProblem> toReturn = new HashSet<KIDSUIProblem>();

		// Check that each required object property has a target; if not, add to problems.
		for (KIDSUIRequiredProperty rprop : myReqProps){

			Set<IRI> propvals = o.getPropertyIndividualsOfClass(myIRI, rprop.getProperty(), rprop.getObjectClass());
			logme.debug(String.format("Evaluating required property (%s, %s, %s)",
								myIRI, 
								rprop.getProperty(), 
								rprop.getObjectClass())); 

			if (propvals.size() == 0){
				// Well, we have a problem:
				toReturn.add(new KIDSMissingRelationUIProblem(
						String.format("Required property not satisfied: (%s, %s, %s)", 
								o.getShortIRIString(myIRI), 
								o.getShortIRIString(rprop.getProperty()), 
								o.getShortIRIString(rprop.getObjectClass())), 
						KIDSUIProblem.ProblemType.REQUIRED,
						rprop.getProperty(),
						rprop.getObjectClass(),
						o
						)
				);
			} else {
				logme.debug(String.format("Property requirement satisfied by (%s, %s, %s)",
						o.getShortIRIString(myIRI),
						o.getShortIRIString(rprop.getProperty()),
						o.getShortIRIString(propvals.iterator().next())));
			}
		}

		// Check that each inferred object property has a target; if not, add to problems.
		for (KIDSUIInferredProperty rprop : myInfProps){

			Set<IRI> propvals = o.getPropertyIndividualsOfClass(myIRI, 
					rprop.getProperty(), rprop.getObjectClass());

			if (propvals.size() == 0){
				// Well, we have a problem:
				toReturn.add(new KIDSUIProblem(
						String.format("Inferred property not defined: (%s, %s, %s)", 
								o.getShortIRIString(myIRI), 
								o.getShortIRIString(rprop.getProperty()), 
								o.getShortIRIString(rprop.getObjectClass())), 
						KIDSUIProblem.ProblemType.REQUIRED
						)
				);
			}
		}

		// Check that each specified data property has a target; if not, add to problems.
		for (KIDSUIRequiredDataProperty rprop : myDataProps){
			
			Set<String> values = o.getDataPropertyValues(myIRI, rprop.getProperty());
			
			if (values.size() == 0){

				// Well, we have a problem:
				toReturn.add(new KIDSMissingDataPropertyUIProblem(
					String.format("Data property not defined: (%s, %s, %s)", 
							o.getShortIRIString(myIRI),
							o.getShortIRIString(rprop.getProperty()), 
							rprop.getObjectClass()), 
					KIDSUIProblem.ProblemType.REQUIRED,
					myIRI,
					rprop.getObjectClass(),
					rprop.getProperty(),
					o
					)
				);
			}
		}
		
		// If subclass membership is required, ensure that we are a member of a strict subclass:
		if (requiredSubclassOf != null){
			logme.debug(String.format("Checking that individual %s is a member of a subclass of %s...",getIRI().getShortForm(), requiredSubclassOf.getShortForm()));
			if (!o.isMemberOfStrictSubclass(requiredSubclassOf, myIRI)){
				logme.debug(String.format("Individual %s is not a member of a subclass of %s; adding as a problem...",getIRI().getShortForm(), requiredSubclassOf.getShortForm()));
				toReturn.add(new KIDSSubclassRequiredUIProblem(
						String.format("%s must be a member of a subclass of %s", 
								o.getShortIRIString(myIRI), 
								o.getShortIRIString(requiredSubclassOf)),
						KIDSUIProblem.ProblemType.REQUIRED,
						requiredSubclassOf,
						myIRI,
						o
						)
				);
			}
			
		}

		logme.debug(String.format("Found %d problems for %s.", toReturn.size(), myIRI.getFragment()));
		return toReturn;
	}
	
	/**
	 * @return - The IRI as defined for the individual represented by this class.
	 */
	@Override
	public IRI getIRI(){
		return this.myIRI;
	}
	
	/**
	 * @return - A short-form version of the IRI for a component.
	 */
	@Override
	public String toString(){
		return this.o.getShortIRIString(this.myIRI);
	}
	
	@Override
	public List<KIDSUIRelation> getRelations(){
		List <KIDSUIRelation> toReturn = new LinkedList<KIDSUIRelation>();
		
		// Add object relations
		for (KIDSUIRequiredProperty p : this.myReqProps){
			toReturn.add(new KIDSUIObjectRelationComponent(this.getIRI(),
					p.getProperty(),
					p.getObjectClass()));
		}
		
		// Add data relations
		for (KIDSUIRequiredDataProperty p : this.myDataProps){
			toReturn.add(new KIDSUIDataRelationComponent(this.getIRI(), 
					p.getProperty(), 
					p.getObjectClass()));
		}
		
		return toReturn;
	}
	
	@Override
	public KIDSComponentDefinition getDefiningLocation(){
		if (this.deflocation == null){
			logme.error(String.format("Component %s does not provide a defining location.", this.getIRI()));
		}
		return this.deflocation;
	}
	
	@Override
	/**
	 * 	 * @param root - The root of the tree to populate with details.
	 * 
	 * @return - A tree node hierarchy of values that provide details about the individual. All components will provide at least:
	 *            + Object Property Values 
	 *              + Property IRI: 
	 *                + individual IRI 1
	 *                ...
	 *            + Data Property Values   
	 *              + Property IRI: 
	 *                + string value 1
	 *                ...
	 *            + Class Memberships:
	 *              + Class IRI
	 */
	public DefaultMutableTreeNode getComponentDetails(DefaultMutableTreeNode root){
		
		// First, get all of the object properties associated with this individual:
		DefaultMutableTreeNode objectProperties = new DefaultMutableTreeNode("Object Properties:");
		Map<IRI, List<IRI>> objProps = o.getObjectPropertyValues(myIRI);
		for (IRI oprop : objProps.keySet()){
			DefaultMutableTreeNode opropNode = new DefaultMutableTreeNode(oprop.getShortForm());
			for (IRI val : objProps.get(oprop)){
				DefaultMutableTreeNode opropValNode = new DefaultMutableTreeNode(val.getShortForm());
				opropNode.add(opropValNode);
			}
			objectProperties.add(opropNode);
		}
		root.add(objectProperties);
		
		// Next, get all of the data property values:
		DefaultMutableTreeNode dataProperties = new DefaultMutableTreeNode("Data Properties:");
		Map<IRI, List<String>> datProps = o.getDataPropertyValues(myIRI);
		for (IRI dprop : datProps.keySet()){
			DefaultMutableTreeNode dpropNode = new DefaultMutableTreeNode(dprop.getShortForm());
			for (String val : datProps.get(dprop)){
				DefaultMutableTreeNode dpropValNode = new DefaultMutableTreeNode(val);
				dpropNode.add(dpropValNode);
			}
			dataProperties.add(dpropNode);
		}
		root.add(dataProperties);
		
		// Next, get all of the subclasses:
		DefaultMutableTreeNode subclassMemberships = new DefaultMutableTreeNode("Subclass Memberships:");
		List<IRI> subclasses = o.getClassMemberships(myIRI);
		
		for (IRI sub : subclasses){
			DefaultMutableTreeNode subNode = new DefaultMutableTreeNode(sub.getShortForm());
			subclassMemberships.add(subNode);
		}
		root.add(subclassMemberships);
		
		return root;

	}


}
