package net.strasnet.kids.measurement;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.strasnet.kids.KIDSOntologyDatatypeValuesException;
import net.strasnet.kids.KIDSOntologyObjectValuesException;
import net.strasnet.kids.detectorsyntaxproducers.KIDSIncompatibleSyntaxException;
import net.strasnet.kids.measurement.datasetlabels.DatasetLabel;
import net.strasnet.kids.measurement.datasetviews.DatasetView;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.util.SimpleIRIMapper;

public class KIDSEvalImplementation implements KIDSEval {
	
	private static final String testKB = "file:///Users/chrisstrasburg/Documents/academic-research/papers/2013-MeasurementPaper/ontologies/S-MAIDS.owl";
	private static final String OntologyLocation = "http://solomon.cs.iastate.edu/ontologies/KIDS.owl";
	private List<OWLNamedIndividual> signals = null;
	private static final IRI testDatasetIRI = IRI.create("#IISLogDump1");
	private static final IRI testEventIRI = IRI.create("#CodeRed");
	private KIDSMeasurementOracle myGuy = null;
	
	public KIDSEvalImplementation(KIDSMeasurementOracle o){
			myGuy = o;
	}

	@Override
	public double EvalSignal(IRI signal, IRI d, IRI event)
			//DatasetView dv, DatasetLabel dl)
			throws KIDSUnEvaluableSignalException, KIDSOntologyDatatypeValuesException, InstantiationException, IllegalAccessException, ClassNotFoundException, KIDSOntologyObjectValuesException, NumberFormatException, IOException, KIDSIncompatibleSyntaxException {
		Set<IRI> sigset = new HashSet<IRI>();
		sigset.add(signal);
		
		return KIDSEIDMeasure.getKIDSEIDMeasureValue(
				KIDSDatasetFactory.getViewLabelDataset(d, event, myGuy), 
				sigset);
	}

	@Override
	/**
	 * EvalEvent will:
	 *  - List the known detectors which can detect the event, along with their datasets, signals, and respective signal
	 *    EID values for each dataset signal combination.
	 *  - List out promising combinations of signals
	 *  
	 * Assumptions:
	 *  - Datasets cover overlapping time periods, and label files indicate timestamps for synchronization
	 *  - Label files for datasets have synchronized labels for events
	 */
	public double EvalEvent(IRI event) {
		HashMap<OWLNamedIndividual, OWLNamedIndividual> signalSets = new HashMap<OWLNamedIndividual, OWLNamedIndividual>();
		
		// Get the datasets applicable to this event -- e.g., datasets which have a view (areViewableAs) that have a
		// label file which includes a label for the event. 
		
		// Get the signals which can be evaluated for this event (given the datasets and event itself)
		Set<IRI> signals = myGuy.getSignalsForDatasetAndEvent(
				IRI.create(myGuy.getOurIRI() + testDatasetIRI.toString()),
				IRI.create(myGuy.getOurIRI() + testEventIRI.toString())
			);
		
		// Get the set of detector implementations which can monitor the dataset view, see the signal representation
		// manifestation, and implement a syntax which can represent the signal.
		
		// For each dataset:
		    // Using the detector implementation for each signal, evaluate the EID value of the signal over the dataset
		
		/************************************** vvv old vvv *******************************/
		// First, get the detectors which "canEval" the event
		//List<OWLNamedIndividual> viableDetectors = o.getPossibleEvaluatorsFor(event);
		
		
		// Next, for each viable Detector, get the set of signals both produced by event, and detectable 
		// by the detector.
		//for (OWLNamedIndividual d : viableDetectors){
			//Set<OWLNamedIndividual> detectableSignals = o.getDetectableSignals(event, d); 
		//}
		
		// For each of those signals, compute the CID value.
		//KIDSEIDMeasure m = new KIDSEIDMeasure();
		
		// Need to include the signal, dataset.
		//m.getKIDSCIDMeasureValue(d, s);
		/************************************** ^^^ old ^^^ *******************************/
		
		return 0;
	}

}
