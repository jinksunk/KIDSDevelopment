package net.strasnet.kids.measurement;

import java.io.IOException;

import net.strasnet.kids.KIDSOntologyDatatypeValuesException;
import net.strasnet.kids.KIDSOntologyObjectValuesException;
import net.strasnet.kids.detectors.UnimplementedIdentifyingFeatureException;
import net.strasnet.kids.detectorsyntaxproducers.KIDSIncompatibleSyntaxException;
import net.strasnet.kids.measurement.correlationfunctions.IncompatibleCorrelationValueException;
import net.strasnet.kids.measurement.datasetlabels.DatasetLabel;
import net.strasnet.kids.measurement.datasetviews.DatasetView;

import org.semanticweb.owlapi.model.IRI;

/**
 * This interface represents a generic method for evaluating a set of signals
 * on a data set.  
 * 
 * @author chrisstrasburg
 *
 */

public interface KIDSEval {
	
	/**
	 * Note that the signal set may represent different features and constraints.
	 * The only assumption here is that there is some suitable dataset for this signal.
	 * 
	 * If there are no detectors/datasets which can evaluate the signal, will throw an "unEvaluableSignalException".
	 * 
	 * @param signal - the signal individual to evaluate on this dataset
	 * @param d - the IRI of the dataset to be used to evaluate this signal
	 * @param event - the event this measure is being taken with respect to
	 * @return The EID value
	 * @throws ClassNotFoundException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 * @throws KIDSOntologyDatatypeValuesException 
	 * @throws KIDSIncompatibleSyntaxException 
	 * @throws IOException 
	 * @throws NumberFormatException 
	 * @throws KIDSOntologyObjectValuesException 
	 * @throws IncompatibleCorrelationValueException 
	 * @throws UnimplementedIdentifyingFeatureException 
	 */
//	double EvalSignal(IRI signal, DatasetView dv, DatasetLabel dl)
	double EvalSignal(IRI signal, IRI d, IRI event)
			throws KIDSUnEvaluableSignalException, KIDSOntologyDatatypeValuesException, InstantiationException, IllegalAccessException, ClassNotFoundException, KIDSOntologyObjectValuesException, NumberFormatException, IOException, KIDSIncompatibleSyntaxException, IncompatibleCorrelationValueException, UnimplementedIdentifyingFeatureException;
	
	/** 
	 * Given the IRI for an event, E, determine the maximum CID attainable with the given KB
	 * @param event
	 * @return The value of the EID score for the event, given this dataset and signal.
	 */
	public double EvalEvent(IRI event);


}
