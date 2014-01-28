package net.strasnet.kids.measurement;

import java.util.HashMap;
import java.util.Map;

import net.strasnet.kids.KIDSOntologyDatatypeValuesException;
import net.strasnet.kids.KIDSOntologyObjectValuesException;
import net.strasnet.kids.signalRepresentations.KIDSRepresentationInvalidRepresentationValueException;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLNamedIndividual;

/**
 * The DataInstance interface abstracts out the general methods and parameters of 
 * @author chrisstrasburg
 *
 */
public interface DataInstance {
	//public boolean matchesSignal(OWLNamedIndividual owlNamedIndividual) throws KIDSOntologyObjectValuesException, KIDSMeasurementInstanceUnsupportedFeatureException, KIDSMeasurementIncompatibleContextException, KIDSOntologyDatatypeValuesException, KIDSRepresentationInvalidRepresentationValueException;
	public Label getLabel();
	
	
	/**
	 * Extract the value of this data instance for the feature represented by ourSigDomain.
	 * @param ourSigDomain - The individual which represents the feature to realize the value of.
	 * @return - The canonical representation of the feature value in String form.
	 * @throws KIDSMeasurementInstanceUnsupportedFeatureException 
	 * @throws KIDSMeasurementIncompatibleContextException 
	String getFeatureValue(OWLNamedIndividual ourSigDomain) throws KIDSMeasurementInstanceUnsupportedFeatureException, KIDSMeasurementIncompatibleContextException;
	 */
	
	/**
	 * Set the Label corresponding to the instance (according to the truth file).
	 * @param label
	 */
	void setLabel(Label label);
	
	/**
	 * 
	 * @param idValues - HashMap of FeatureIRI -> Value for each identifying feature component
	 */
	public void setID(HashMap <IRI,String> idValues);

	/**
	 * 
	 * @return - The ID assigned to this data instance
	 */
	public String getID();
	
	/**
	 * @return - The resource map (<IRI,String>) containing all resources extracted by this instance
	 */
	public Map<IRI,String> getResources();
}
