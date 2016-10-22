/**
 * 
 */
package net.strasnet.kids.ui;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLDataFactory;

import net.strasnet.kids.ui.gui.KIDSGUIOracle;

/**
 * @author cstras
 *
 * Implementation of common methods for implementers of KIDSUIComponent
 */
public abstract class KIDSUIAbstractComponent implements KIDSUIComponent {

	public static final org.apache.logging.log4j.Logger logme = LogManager.getLogger(KIDSUIAbstractComponent.class.getName());

	protected IRI myIRI = null;
	protected Set<KIDSUIInferredProperty> myInfProps;
	protected Set<KIDSUIRequiredProperty> myReqProps;
	protected Set<KIDSUIRequiredDataProperty> myDataProps;
	protected String ABOXIRI;
	protected IRI TBOXIRI;
	protected KIDSGUIOracle o;
	protected OWLDataFactory owldf;

	public KIDSUIAbstractComponent(IRI myID, KIDSGUIOracle o){
		myIRI = myID;
		TBOXIRI = o.getTBOXIRI();
		ABOXIRI = myIRI.getStart();
		this.o = o;
		this.owldf = o.getOwlDataFactory();
		
		myReqProps = new HashSet<KIDSUIRequiredProperty>();
		myInfProps = new HashSet<KIDSUIInferredProperty>();
		myDataProps = new HashSet<KIDSUIRequiredDataProperty>();
		
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
								myIRI, 
								rprop.getProperty(), 
								rprop.getObjectClass()), 
						KIDSUIProblem.ProblemType.REQUIRED,
						rprop.getProperty(),
						rprop.getObjectClass(),
						o
						)
				);
			} else {
				logme.debug(String.format("Property requirement satisfied by (%s, %s, %s)",
						myIRI,
						rprop.getProperty(),
						propvals.iterator().next()));
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
								myIRI, 
								rprop.getProperty(), 
								rprop.getObjectClass()), 
						KIDSUIProblem.ProblemType.REQUIRED
						)
				);
			}
		}

		// Check that each specified data property has a target; if not, add to problems.
		for (KIDSUIRequiredDataProperty rprop : myDataProps){

			Set<String> propvals = o.getDataPropertyValues(myIRI, 
					rprop.getProperty());

			if (propvals.size() == 0){
				// Well, we have a problem:
				toReturn.add(new KIDSUIProblem(
						String.format("Data property not defined: (%s, %s, %s)", 
								myIRI, 
								rprop.getProperty(), 
								rprop.getObjectClass()), 
						KIDSUIProblem.ProblemType.REQUIRED
						)
				);
			}
		}

		logme.debug(String.format("Found %d problems for %s.", toReturn.size(), myIRI.getFragment()));
		return toReturn;
	}
}
