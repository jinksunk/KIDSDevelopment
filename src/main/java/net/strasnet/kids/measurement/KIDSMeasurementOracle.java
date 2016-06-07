package net.strasnet.kids.measurement;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.NodeSet;

import net.strasnet.kids.KIDSCanonicalRepresentation;
import net.strasnet.kids.KIDSCanonicalRepresentationFactory;
import net.strasnet.kids.KIDSOntologyDatatypeValuesException;
import net.strasnet.kids.KIDSOntologyObjectValuesException;
import net.strasnet.kids.KIDSOracle;
import net.strasnet.kids.detectors.KIDSDetector;
import net.strasnet.kids.detectors.UnimplementedIdentifyingFeatureException;
import net.strasnet.kids.detectorsyntaxproducers.KIDSDetectorSyntax;
import net.strasnet.kids.detectorsyntaxproducers.KIDSIncompatibleSyntaxException;
import net.strasnet.kids.measurement.correlationfunctions.KIDSCorrelationFunctionFactory;
import net.strasnet.kids.measurement.datasetlabels.DatasetLabel;
import net.strasnet.kids.measurement.datasetviews.DatasetView;
import net.strasnet.kids.measurement.datasetviews.KIDSUnsupportedSchemeException;

public class KIDSMeasurementOracle extends KIDSOracle {
	
	/** TODO: Use a logger rather than Stderr. */
	/** TODO: Move these to a properties file. */
	/** Define all the static values for properties etc... These should all probably be in a properties file */
	public static final String kidsTBOXLocation = "http://solomon.cs.iastate.edu/ontologies/KIDS.owl";

	public static final String contextDomainRelation = kidsTBOXLocation + "#isContextOfSignalDomain";
	public static final String correlationFunctionImplementationDataProperty = kidsTBOXLocation + "#hasCorrelationRelationImplementation";
	public static final String correlationRelationDatasetViewRelation = kidsTBOXLocation + "#isSupportedByDatasetView";
	public static final String datasetContextRelation = kidsTBOXLocation + "#isContainerOfContext";
	public static final String datasetInstanceResourceProp = kidsTBOXLocation + "#datasetLocation";
	public static final String datasetLabelResourceProp = kidsTBOXLocation + "#datasetLabelLocation";
	public static final String datasetParserImplementationProp = kidsTBOXLocation + "#datasetParserImplementation";
	public static final String datasetTimePeriodRelation = kidsTBOXLocation + "#includesTimePeriod";
	public static final String datasetSignalRelation = kidsTBOXLocation + "#isCompatibleDatasetForSignal";
	public static final String datasetViewCorrelationRelation = kidsTBOXLocation + "#supportCorrelationRelation";
	public static final String datasetViewIsViewOfDatasetRelation = kidsTBOXLocation + "#providesViewOf";
	public static final String datasetViewRelation = kidsTBOXLocation + "#isViewableAs";
	public static final String datasetViewSignalManifestationRelation = kidsTBOXLocation + "#bringsIntoExistence";
	public static final String detectorImplementationDataProperty = kidsTBOXLocation + "#hasImplementationClass";
	public static final String detectorExecutionDataProperty = kidsTBOXLocation + "#detectorExecutionCommand";
	public static final String detectorSignalRelation = kidsTBOXLocation + "#canApplySignal";
	public static final String detectorSyntaxImplementationDataProperty = kidsTBOXLocation + "#hasSyntaxProductionImplementation";
	public static final String detectorSyntaxRelation = kidsTBOXLocation + "#hasSyntax";
	public static final String detectorSyntaxSignalRelation = kidsTBOXLocation + "#canRepresentFeatureWithConstraint";
	public static final String domainContextRelation = kidsTBOXLocation + "#isInContext";
	public static final String eventDatasetRelation = kidsTBOXLocation + "#isRepresentedInDataset";
	public static final String eventLabelRelation = kidsTBOXLocation + "#isIncludedInLabel";
	public static final String eventSignalRelation = kidsTBOXLocation + "#isProducerOf";
	public static final String labelClassDataProperty = kidsTBOXLocation + "#hasLabelFunction";
	public static final String labelLocationDataProperty = kidsTBOXLocation + "#hasLabelDataLocation";
	public static final String labelViewRelation = kidsTBOXLocation + "#isLabelerForDatasetView";
	public static final String manifestationSignalRelation = kidsTBOXLocation + "#SignalManifestationIncludesSignal";
	public static final String signalConstraintSignalRelation = kidsTBOXLocation + "#hasConstraint";
	public static final String signalDatasetRelation = kidsTBOXLocation + "#isEvaluableWithDataset";
	public static final String signalDomainSignalRelation = kidsTBOXLocation + "#isDomainOfSignal";
	public static final String signalDetectorRelation = kidsTBOXLocation + "#isAppliedByDetector";
	public static final String signalManifestationRelation = kidsTBOXLocation + "#SignalInManifestation";
	public static final String signalRepresentationRelation = kidsTBOXLocation + "#isRepresentedBy";
	public static final String timePeriodDatasetRelation = kidsTBOXLocation + "#isIncludedInDataset";
	public static final String viewClassDataProperty = kidsTBOXLocation + "#viewProductionImplementation";
	public static final String viewDetectorRelation = kidsTBOXLocation + "#isMonitoredBy";
	public static final String viewLabelRelation = kidsTBOXLocation + "#hasDatasetLabel";
	public static final String resourceProviderRelation = kidsTBOXLocation + "#isProviderOfResource";

	/**********************************/
	/** Ontology Interaction Methods **/
	/**********************************/
	
	/**
	 * Returns the (atomic) dataset IRIs known by the ontology to be valid for evaluating the given event.  The intention
	 * is to identify which datasets contain data elements which can be used to evaluate an event.  
	 * 
	 * For the specific semantics, 
	 * @see http://solomon.cs.iastate.edu/ontologies/KIDS.owl#isRepresentedInDataset
	 * 
	 * @param testeventiri
	 * @return
	 * @throws IOException
	 * @throws KIDSOntologyDatatypeValuesException
	 * @throws KIDSUnsupportedSchemeException
	 * @throws TruthFileParseException
	 * @throws ClassNotFoundException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 * @throws KIDSOntologyObjectValuesException 
	 */
	public Set<OWLNamedIndividual> getDatasetsForEvent(IRI testeventiri) throws IOException, KIDSOntologyDatatypeValuesException, KIDSUnsupportedSchemeException, net.strasnet.kids.measurement.datasetlabels.TruthFileParseException, KIDSOntologyObjectValuesException, InstantiationException, IllegalAccessException, ClassNotFoundException {
		Set<OWLNamedIndividual> rList = this.r.getObjectPropertyValues(this.odf.getOWLNamedIndividual(testeventiri), 
				this.odf.getOWLObjectProperty(IRI.create(eventDatasetRelation))).getFlattened();
		return rList;
	}

	/**
	 * Given a dataset IRI, get the signals which are associated with the given dataset.  In this method, we return all
	 * signals which are represented in this dataset, regardless of current association with any event.
	 * 
	 * For specific semantics: 
	 * @see http://solomon.cs.iastate.edu/ontologies/KIDS.owl#isCompatibleDatasetForSignal
	 * 
	 * @param datasetIRI - The IRI of the dataset we should get signals for:
	 * @return A list of signal IRIs which are available in a given dataset.
	 */
	public List<OWLNamedIndividual> getSignalsForDataset(IRI datasetIRI) {
		List<OWLNamedIndividual> toReturn = new LinkedList<OWLNamedIndividual>();
		
		// If Get the intersection of signals produced by e and defined over any feature in any context of the data set.
		NodeSet<OWLNamedIndividual> myij = this.r.getObjectPropertyValues(
								this.odf.getOWLNamedIndividual(datasetIRI),
								this.odf.getOWLObjectProperty(IRI.create( datasetSignalRelation))
				);
				toReturn.addAll(myij.getFlattened()); // TODO: Review in ontology: Need a chain of dataSetContains -> isContextOfDomain -> isDomainOfSignal

				this.dumpReasonerInfo();
				OWLClassExpression oce = this.odf.getOWLObjectHasValue(odf.getOWLObjectProperty(IRI.create(signalDatasetRelation)), odf.getOWLNamedIndividual(datasetIRI));
				System.err.println("[D] -- " + oce.toString());
				NodeSet<OWLNamedIndividual> secondSet = r.getInstances(this.odf.getOWLObjectHasValue(odf.getOWLObjectProperty(IRI.create(signalDatasetRelation)), odf.getOWLNamedIndividual(datasetIRI)), false);
		return toReturn;
	}

	/**
	 * Given event and dataset IRIs, get the signals evaluable on the dataset, which are associated with
	 * the event.  
	 * 
	 * More specifically, we would like
	 * the set of signals satisfying: Produced by event e and defined over a feature represented in 
	 * one of the contexts contained within the dataset.
	 * 
	 * For specific semantics:
	 * @see http://solomon.cs.iastate.edu/ontologies/KIDS.owl#isProducerOf
	 * @see http://solomon.cs.iastate.edu/ontologies/KIDS.owl#isCompatibleDatasetForSignal
	 * 
	 * @param datasetIRI - The IRI of the dataset we should get signals for:
	 * @param eventIRI - The IRI of the event individual we are considering
	 * @return A list of signal IRIs which are available in a given dataset.
	 */
	public Set<IRI> getSignalsForDatasetAndEvent(IRI datasetIRI, IRI eventIRI) {
		Set<IRI> toReturn = new HashSet<IRI>();
		
		// Quick test:
		Set <OWLNamedIndividual> tset = 
							this.r.getObjectPropertyValues(
									this.odf.getOWLNamedIndividual(eventIRI),
									this.odf.getOWLObjectProperty(IRI.create(eventSignalRelation))).getFlattened();
		tset = 
							this.r.getObjectPropertyValues(
								this.odf.getOWLNamedIndividual(datasetIRI),
								this.odf.getOWLObjectProperty(IRI.create(datasetSignalRelation))
							).getFlattened();
		// If Get the intersection of signals produced by e and defined over any feature in any context of the data set.
		Set <OWLNamedIndividual> sset = 
			(this.r.getInstances(
				this.odf.getOWLObjectIntersectionOf(
						this.odf.getOWLObjectOneOf(
							this.r.getObjectPropertyValues(
									this.odf.getOWLNamedIndividual(eventIRI),
									this.odf.getOWLObjectProperty(IRI.create(eventSignalRelation))
							).getFlattened()
						),
						this.odf.getOWLObjectOneOf(
							// Need a chain of dataSetContains -> isContextOfDomain -> isDomainOfSignal
							this.r.getObjectPropertyValues(
								this.odf.getOWLNamedIndividual(datasetIRI),
								this.odf.getOWLObjectProperty(IRI.create(datasetSignalRelation))
							).getFlattened()
						)
					), 
				false).getFlattened());
		for (OWLNamedIndividual i : sset){
			toReturn.add(i.getIRI());
		}
		return toReturn;
	}

	/**
	 * There may be multiple views of a given dataset - this method returns a list of all of
	 * their IRIs.  For detailed semantics:
	 * @see http://solomon.cs.iastate.edu/ontologies/KIDS.owl#isViewableAs
	 * 
	 * @param datasetIRI
	 * @return - All available views of the given datasetIRI
	 */
	public Set<OWLNamedIndividual> getAvailableViews(IRI datasetIRI) {
		
		Set<OWLNamedIndividual> datasetViewSet = this.r.getObjectPropertyValues(
						       this.odf.getOWLNamedIndividual(datasetIRI),
						       this.odf.getOWLObjectProperty(IRI.create(datasetViewRelation))
						    ).getFlattened();
		return datasetViewSet;
	}

	/**
	 * Get the available view list for this dataset *and* event - that is, the view
	 * should both be associated with the given dataset, and have a defined label function
	 * related to the given event.
	 * 
	 * For detailed semantics:
	 * @see http://solomon.cs.iastate.edu/ontologies/KIDS.owl#isIncludedInLabel
	 * @see http://solomon.cs.iastate.edu/ontologies/KIDS.owl#isLabelerForDatasetView
	 * 
	 * @param datasetIRI
	 * @param eventIRI
	 * @return
	 */
	public List<OWLNamedIndividual> getAvailableViews(IRI datasetIRI, IRI eventIRI) {
		List<OWLNamedIndividual> toReturn = new LinkedList<OWLNamedIndividual>();
		
		// Get the values of the datasetViewRelation which have this dataset as a target, and a label function which 
		// includes the event.
		
		// First, get the set of views with a label function that includes this event:
			    Set<OWLNamedIndividual> labels = this.r.getObjectPropertyValues(
			       this.odf.getOWLNamedIndividual(eventIRI),
			       this.odf.getOWLObjectProperty(IRI.create(eventLabelRelation))
			    ).getFlattened();
			
			    Set<OWLNamedIndividual> views = new HashSet<OWLNamedIndividual>();
			for (OWLNamedIndividual labelInd : labels){
					views.addAll( this.r.getObjectPropertyValues(
			    					labelInd, 
			    					odf.getOWLObjectProperty(
			    							IRI.create(labelViewRelation)
			    					)
			    				).getFlattened());
			}
		
		toReturn.addAll(this.r.getInstances(
				this.odf.getOWLObjectIntersectionOf(
						this.odf.getOWLObjectOneOf(
								this.getAvailableViews(datasetIRI)
						),
						this.odf.getOWLObjectOneOf(views)
					), 
				false).getFlattened());
		
		return toReturn;
	}

	/**
	 * Given a signal and a view to track the signal in, will return a set of detector individuals
	 * capable of identifying the signal in the view.
	 * 
	 * For detailed semantics:
	 * 
	 * @see http://solomon.cs.iastate.edu/ontologies/KIDS.owl#isAppliedByDetector
	 * @see http://solomon.cs.iastate.edu/ontologies/KIDS.owl#isMonitoredBy
	 *  
	 * @param signal - The signal to be identified in the view
	 * @param view - The view to identify the signal in
	 * @return - A set of detector individuals which can apply the signal to the view.
	 */
	public Set<IRI> getDetectorsForSignalAndView(
			IRI signal, IRI view) {
		// Get the set of detectors which can monitor this view *and* see the manifestation.
		// Stop at the first one found.
		Set<IRI> toReturn = new HashSet<IRI>();
		Set<OWLNamedIndividual> retTemp = new HashSet<OWLNamedIndividual>();
		
		retTemp.addAll(
				this.r.getInstances(
				this.odf.getOWLObjectIntersectionOf(
						this.odf.getOWLObjectOneOf(
						    this.r.getObjectPropertyValues(
						       odf.getOWLNamedIndividual(signal),
						       this.odf.getOWLObjectProperty(IRI.create(signalDetectorRelation))
						    ).getFlattened()
						),
						this.odf.getOWLObjectOneOf(
							this.r.getObjectPropertyValues(
								odf.getOWLNamedIndividual(view),
								this.odf.getOWLObjectProperty(IRI.create(viewDetectorRelation))
							).getFlattened()
						)
					), 
				false).getFlattened());
		
		for (OWLNamedIndividual oni : retTemp){
			toReturn.add(oni.getIRI());
		}
		
		return toReturn;
	}
		
	/**
	 * For a given signal and dataset view, a set of detectors is defined which can both detect the signal,
	 * as well as process the view.  This method will return one of these detectors, if set size > 0.
	 * 
	 * @param signal
	 * @param view
	 * @return - A single detector individual which can apply the signal to the view.  Assumes there is only one.
	 * @throws KIDSOntologyDatatypeValuesException 
	 */
	public OWLNamedIndividual getDetectorForSignalAndView(
			OWLNamedIndividual signal, OWLNamedIndividual view) throws KIDSOntologyDatatypeValuesException {
		
		// Get the set of detectors which can monitor this view *and* see the manifestation.
		// Stop at the first one found.
		LinkedList<OWLNamedIndividual> toReturn = new LinkedList<OWLNamedIndividual>();
		
		Set<IRI>tRet = getDetectorsForSignalAndView(signal.getIRI(), view.getIRI());
		if (tRet.size() > 1){
			throw new KIDSOntologyDatatypeValuesException("Too many detectors for signal " + signal + " and view " + view);
		} else if (tRet.size() == 0){
			throw new KIDSOntologyDatatypeValuesException("No detectors for signal " + signal + " and view " + view);
		}
		for (IRI dIRI : tRet){
			toReturn.add(odf.getOWLNamedIndividual(dIRI));
		}
		
		return toReturn.get(0);
	}

	/**
	 * Get the implementation information for the given dataset view.  The result is a single class to
	 * instantiate.
	 * 
	 * For specific semantics:
	 * @see http://solomon.cs.iastate.edu/ontologies/KIDS.owl#viewProductionImplementation
	 * 
	 * @param viewIndividual - The view instance we want to instantiate
	 * @return The name of a java class to instantiate
	 * @throws KIDSOntologyDatatypeValuesException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws ClassNotFoundException
	 */
	public String getViewImplementation(OWLNamedIndividual viewIndividual) 
			 throws KIDSOntologyDatatypeValuesException, InstantiationException, IllegalAccessException, ClassNotFoundException {
		Set<OWLLiteral> results = r.getDataPropertyValues(
					viewIndividual, 
					odf.getOWLDataProperty(IRI.create(viewClassDataProperty)));
		if (results.size() > 1){
			throw new KIDSOntologyDatatypeValuesException("Too many values for property " + viewClassDataProperty + " on individual " + viewIndividual);
		} else if (results.size() == 0){
			throw new KIDSOntologyDatatypeValuesException("No values for property " + viewClassDataProperty + " on individual " + viewIndividual);
		}
		return results.iterator().next().getLiteral();
	}

	/**
	 * Return the label individual which is associated with the given datasetView, 
	 * and provides the truth about the given event.  For specific semantics:
	 * 
	 * @see http://solomon.cs.iastate.edu/ontologies/KIDS.owl#hasDatasetLabel
	 * @see http://solomon.cs.iastate.edu/ontologies/KIDS.owl#isIncludedInLabel
	 * 
	 * @param datasetView
	 * @param testeventiri
	 * @return
	 */
	public OWLNamedIndividual getLabelForViewAndEvent(
			OWLNamedIndividual datasetView, IRI testeventiri) {
		// Get the label for the view and event.  The intersection of things that label datasetView and which
		// are labels of testeventiri.
		
		OWLNamedIndividual toReturn;
		
		toReturn = this.r.getInstances(
				this.odf.getOWLObjectIntersectionOf(
						this.odf.getOWLObjectOneOf(
						    this.r.getObjectPropertyValues(
						       datasetView,
						       this.odf.getOWLObjectProperty(IRI.create(viewLabelRelation))
						    ).getFlattened()
						),
						this.odf.getOWLObjectOneOf(
							this.r.getObjectPropertyValues(
								this.odf.getOWLNamedIndividual(testeventiri),
								this.odf.getOWLObjectProperty(IRI.create(eventLabelRelation))
							).getFlattened()
						)
					), 
				false).getFlattened().iterator().next();
		
		return toReturn;
	}

	/**
	 * Given a signal individual, return the constraint individual associated with it. For specific semantics:
	 * 
	 * @see http://solomon.cs.iastate.edu/ontologies/KIDS.owl#hasConstraint
	 * 
	 * @param owlNamedIndividual
	 * @return The constraint individual associated with the signal
	 * @throws KIDSOntologyObjectValuesException When the ontology contains ill-defined values, e.g. more than one constraint for a signal.
	 */
	public OWLNamedIndividual getSignalConstraint(
			OWLNamedIndividual signal) throws KIDSOntologyObjectValuesException {
		Set<OWLNamedIndividual> results = r.getObjectPropertyValues(
					signal, 
					odf.getOWLObjectProperty(IRI.create(KIDSMeasurementOracle.signalConstraintSignalRelation))).getFlattened();
		if (results.size() > 1){
			throw new KIDSOntologyObjectValuesException("Too many values for property " + signalConstraintSignalRelation + " on individual " + signal);
		} else if (results.size() == 0){
			throw new KIDSOntologyObjectValuesException("No values for property " + signalConstraintSignalRelation + " on individual " + signal);
		}
		return results.iterator().next();
	}

	/**
	 * Given the IRI of a detector individual, will return the IRI of the Syntax individual associated with it.
	 * For specific semantics:
	 * 
	 * @see http://solomon.cs.iastate.edu/ontologies/KIDS.owl#hasSyntax
	 * 
	 * @param ourIRI - The IRI of the detector to get the syntax for -- for now, assuming each detector has a single
	 * syntax object - may be reasonable since it translates signals into detector definitions...
	 * @return The IRI of the syntax individual associated with the given detector.
	 * @throws KIDSOntologyObjectValuesException 
	 * @throws KIDSOntologyDatatypeValuesException 
	 * @throws ClassNotFoundException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 */
	public KIDSDetectorSyntax getDetectorSyntax(IRI detectorIRI) throws KIDSOntologyObjectValuesException, KIDSOntologyDatatypeValuesException, InstantiationException, IllegalAccessException, ClassNotFoundException {
		OWLNamedIndividual detector = odf.getOWLNamedIndividual(detectorIRI);
		Set<OWLNamedIndividual> results = r.getObjectPropertyValues(
					detector, 
					odf.getOWLObjectProperty(IRI.create(KIDSMeasurementOracle.detectorSyntaxRelation))).getFlattened();
		if (results.size() > 1){
			throw new KIDSOntologyObjectValuesException("Too many values for property " + detectorSyntaxRelation + " on individual " + detector);
		} else if (results.size() < 1){
			throw new KIDSOntologyObjectValuesException("No values found for property " + detectorSyntaxRelation + " on individual " + detector);
		}
		
		Set<OWLLiteral> Lresults = r.getDataPropertyValues(
					results.iterator().next(), 
					odf.getOWLDataProperty(IRI.create(KIDSMeasurementOracle.detectorSyntaxImplementationDataProperty)));
		if (Lresults.size() != 1){
			throw new KIDSOntologyDatatypeValuesException("Too many values for property " + detectorSyntaxImplementationDataProperty + " on individual " + results.iterator().next());
		}
		String synClassName = Lresults.iterator().next().getLiteral();
		
		this.getClass();
		KIDSDetectorSyntax ourSyn = (KIDSDetectorSyntax)Class.forName(synClassName).newInstance();
		ourSyn.init(this);
		return ourSyn;
	}

	/**
	 * Given a dataset individual, will return the dataset location (physical) for that dataset.
	 * For specific semantics:
	 * 
	 * @see http://solomon.cs.iastate.edu/ontologies/KIDS.owl#datasetLocation
	 * 
	 * @param ourDS - The dataset to get the location of
	 * @return The dataset location for the dataset 'd'
	 * @throws KIDSOntologyObjectValuesException 
	 */
	public String getDatasetLocation(OWLNamedIndividual ourDS) throws KIDSOntologyDatatypeValuesException {
			Set<OWLLiteral> dataLocation =
					r.getDataPropertyValues(
							odf.getOWLNamedIndividual(ourDS.getIRI()),
							odf.getOWLDataProperty(IRI.create( datasetInstanceResourceProp)));
		if (dataLocation.size() != 1){
			throw new KIDSOntologyDatatypeValuesException("Too many values for property " + datasetInstanceResourceProp + " on individual " + dataLocation);
		}
		return dataLocation.iterator().next().getLiteral();
	}

	/**
	 * Returns the set of all detectors which can be applied to the given view.  A detector for a view 
	 * is expected to be able to return all instances within the view.  For specific semantics:
	 * 
	 * @see http://solomon.cs.iastate.edu/ontologies/KIDS.owl#isMonitoredBy
	 * 
	 * @param viewIRI - The IRI of the dataset view for which to get a compatible detector
	 * @return An instantiation of a KIDSDetector object.
	 * @throws KIDSOntologyObjectValuesException 
	 * @throws ClassNotFoundException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 * @throws KIDSOntologyDatatypeValuesException 
	 */
	public Set<IRI> getDetectorsForView(IRI viewIRI) throws KIDSOntologyObjectValuesException, KIDSOntologyDatatypeValuesException {
		// First lookup the detector implementation:
		Set<OWLNamedIndividual> results = r.getObjectPropertyValues(odf.getOWLNamedIndividual(viewIRI), 
				odf.getOWLObjectProperty(IRI.create(KIDSMeasurementOracle.viewDetectorRelation))).getFlattened();
		if (results.size() == 0){
			throw new KIDSOntologyObjectValuesException("No values for property " + viewDetectorRelation + " on individual " + viewIRI);
		}
		
		HashSet<IRI> toReturn = new HashSet<IRI>();
		Iterator<OWLNamedIndividual> i = results.iterator();
		OWLNamedIndividual detector = null;
		
		while (i.hasNext()){
			detector = i.next();
			toReturn.add(detector.getIRI());
		}
		return toReturn;
	}

	/**
	 * Given a detector individual, return the execution string for it (physical).
	 * For specific semantics:
	 * 
	 * @see http://solomon.cs.iastate.edu/ontologies/KIDS.owl#detectorExecutionCommand
	 * 
	 * @param iri - The IRI of the detector individual we are working with.
	 * @return The string used to execute this detector.
	 * @throws KIDSOntologyDatatypeValuesException 
	 */
	public String getDetectorExecutionString(IRI detector) throws KIDSOntologyDatatypeValuesException {
		Set<OWLLiteral> detectorImpl =
				r.getDataPropertyValues(this.odf.getOWLNamedIndividual(detector), 
						odf.getOWLDataProperty(IRI.create(detectorExecutionDataProperty)));
		
		if (detectorImpl.size() > 1){
			throw new KIDSOntologyDatatypeValuesException("Too many values for property " + detectorExecutionDataProperty + " on individual " + detector);
		} else if (detectorImpl.size() == 0){
			throw new KIDSOntologyDatatypeValuesException("No values for property " + detectorExecutionDataProperty + " on individual " + detector);
		}
		return detectorImpl.iterator().next().getLiteral();
	}

	/**
	 * Given a labeler individual, return the class which implements it.
	 * For specific semantics:
	 * 
	 * @see http://solomon.cs.iastate.edu/ontologies/KIDS.owl#hasLabelFunction
	 * 
	 * @param label - The label individual
	 * @return The java class which implements this label function
	 * @throws KIDSOntologyDatatypeValuesException
	 */
	public String getLabelImplementation(OWLNamedIndividual label) throws KIDSOntologyDatatypeValuesException {
		Set<OWLLiteral> results = r.getDataPropertyValues(
					label, 
					odf.getOWLDataProperty(IRI.create(labelClassDataProperty)));
		if (results.size() != 1){
			throw new KIDSOntologyDatatypeValuesException("Too many values for property " + labelClassDataProperty + " on individual " + label);
		}
		return results.iterator().next().getLiteral();
	}

	/**
	 * Given a label individual, return the physical location of the label specification.
	 * For specific semantics:
	 * 
	 * @see http://solomon.cs.iastate.edu/ontologies/KIDS.owl#hasLabelDataLocation
	 * 
	 * @param label
	 * @return The physical location of the label specification
	 * @throws KIDSOntologyDatatypeValuesException
	 */
	public IRI getLabelLocation(OWLNamedIndividual label) throws KIDSOntologyDatatypeValuesException {
		Set<OWLLiteral> results = r.getDataPropertyValues(
					label, 
					odf.getOWLDataProperty(IRI.create(labelLocationDataProperty)));
		if (results.size() != 1){
			throw new KIDSOntologyDatatypeValuesException("Too many values for property " + labelLocationDataProperty + " on individual " + label);
		}
		return IRI.create(results.iterator().next().getLiteral());
	}

	/**
	 * Given a set of datasets, will return the set of defined correlation functions that can be applied
	 * to the entire set of them.
	 * 
	 * For full semantics:
	 * 
	 * @see http://solomon.cs.iastate.edu/ontologies/KIDS.owl#supportCorrelationRelation
	 * @see http://solomon.cs.iastate.edu/ontologies/KIDS.owl#hasCorrelationRelationImplementation
	 * 
	 * @param dsets
	 * @return The set of all correlation functions compatible with all indicated data sets
	 * @throws KIDSOntologyDatatypeValuesException 
	 */
	public Set<CorrelationFunction> getCompatibleCorrelationFunctions(
			Set<Dataset> dsets) throws KIDSOntologyDatatypeValuesException {
		HashSet<CorrelationFunction> cfs = new HashSet<CorrelationFunction>();
		
		// Build an intersection query of all members of the CorrelationFunction class which are also 
		// supported by the given datasets.
		// 
		Set<OWLNamedIndividual> results = null;
		Set<OWLNamedIndividual> operands = null;
		for (Dataset dv : dsets){

			// Get the set of functions supported by these datasets:
			operands = new HashSet<OWLNamedIndividual>();
			operands.addAll(
					r.getObjectPropertyValues(
						odf.getOWLNamedIndividual(dv.getViewIRI()),
						odf.getOWLObjectProperty(IRI.create(datasetViewCorrelationRelation))
						).getFlattened()
						);
			if (results== null){
				results = new HashSet<OWLNamedIndividual>();
				results.addAll(operands);
			} else {
				for (OWLNamedIndividual ourInd : results){
					if (!operands.contains(ourInd)){
						results.remove(ourInd);
					}
				}
				
			}
				
		}

		for (OWLNamedIndividual owi : results){
			Set<OWLLiteral> classInstance = r.getDataPropertyValues(owi, 
					odf.getOWLDataProperty(IRI.create(correlationFunctionImplementationDataProperty)));
			if (classInstance.size() > 1){
				throw new KIDSOntologyDatatypeValuesException("Too many values for property " + correlationFunctionImplementationDataProperty + " on individual " + owi);
			} else if (classInstance.size() == 0){
				throw new KIDSOntologyDatatypeValuesException("No values for property " + correlationFunctionImplementationDataProperty + " on individual " + owi);
			}
			OWLLiteral l = classInstance.iterator().next();
			cfs.add(KIDSCorrelationFunctionFactory.getCorrelationFunction(l.getLiteral()));
		}
					
		return cfs;
	}
	
	/**
	 * Given a dataset view, get the set of all signal manifestations available in it.
	 * 
	 * For specific semantics:
	 * 
	 * @see http://solomon.cs.iastate.edu/ontologies/KIDS.owl#bringsIntoExistence
	 * 
	 * @param dvIRI - The dataset view we want the manifestations from
	 * @return A set of IRIs for the signal manifestations that exist in the given view.
	 */
	public Set<IRI> getSignalManifestationsInDatasetView(IRI dvIRI){
		Set<IRI> toReturn = new HashSet<IRI>();
		
		OWLNamedIndividual ourDV = odf.getOWLNamedIndividual(dvIRI);
		
		Set<OWLNamedIndividual> retTmp = r.getObjectPropertyValues(ourDV, 
				odf.getOWLObjectProperty(
						IRI.create(KIDSMeasurementOracle.datasetViewSignalManifestationRelation))
						).getFlattened();
		
		for (OWLNamedIndividual ourMan : retTmp){
			toReturn.add(ourMan.getIRI());
		}
		return toReturn;
		
	}
	
	/**
	 * Given a dataset view, return the list of detectors which can be applied to it.
	 * 
	 * @see http://solomon.cs.iastate.edu/ontologies/KIDS.owl#isMonitoredBy
	 * 
	 * @param dvIRI - The IRI of the dataset view we want to get detectors for
	 * @return The set of detector individuals which can observe this dataset view
	 * 
	 * TODO: getDetectorsForSignalAndView should use this...
	 */
	public Set<IRI> getDetectorsForDatasetView(IRI dvIRI){
		Set<IRI> toReturn = new HashSet<IRI>();
		
		OWLNamedIndividual ourDV = odf.getOWLNamedIndividual(dvIRI);
		
		Set<OWLNamedIndividual> retTmp = r.getObjectPropertyValues(ourDV, 
				odf.getOWLObjectProperty(
						IRI.create(KIDSMeasurementOracle.viewDetectorRelation))
						).getFlattened();
		
		for (OWLNamedIndividual ourMan : retTmp){
			toReturn.add(ourMan.getIRI());
		}
		return toReturn;
	}

	/**
	 * Given a manifestation, return the signal individual associated with it.  For precise semantics:
	 * 
	 * @see http://solomon.cs.iastate.edu/ontologies/KIDS.owl#SignalManifestationIncludesSignal
	 * 
	 * @param ourMan - The IRI of the signal manifestation we want to get the signal for
	 * @return The signal IRI individuals which is included in this manifestation
	 * @throws KIDSOntologyDatatypeValuesException 
	 */
	public IRI getSignalForManifestation (IRI ourMan) throws KIDSOntologyDatatypeValuesException{
		OWLNamedIndividual oni = odf.getOWLNamedIndividual(ourMan);
		
		Set<OWLNamedIndividual> retTmp = r.getObjectPropertyValues(oni, 
				odf.getOWLObjectProperty(
						IRI.create(KIDSMeasurementOracle.manifestationSignalRelation))
						).getFlattened();
		
		if (retTmp.size() > 1){
			throw new KIDSOntologyDatatypeValuesException("Too many values for property " + KIDSMeasurementOracle.signalManifestationRelation + " on individual " + ourMan);
		} else if (retTmp.size() == 0){
			throw new KIDSOntologyDatatypeValuesException("No values for property " +  KIDSMeasurementOracle.signalManifestationRelation  + " on individual " + ourMan);
		}	
		
		return retTmp.iterator().next().getIRI();
	}
	
	/**
	 * Return the set of signals which can be applied (in theory) by a detector.  Note that no
	 * dataset view is indicated here, so all signals that can potentially be applied will be returned.
	 * 
	 * @see http://solomon.cs.iastate.edu/ontologies/KIDS.owl#canApplySignal
	 * 
	 * @param detector
	 * @return - The set of signal IRIs which can be applied by this detector.
	 */
	public Set<IRI> getSignalsApplicableByDetector(IRI detector){
		Set<IRI> toReturn = new HashSet<IRI>();
		
		OWLNamedIndividual ourDV = odf.getOWLNamedIndividual(detector);
		
		Set<OWLNamedIndividual> retTmp = r.getObjectPropertyValues(ourDV, 
				odf.getOWLObjectProperty(
						IRI.create(KIDSMeasurementOracle.detectorSignalRelation))
						).getFlattened();
		
		for (OWLNamedIndividual ourMan : retTmp){
			toReturn.add(ourMan.getIRI());
		}
		return toReturn;
	}

	/**
	 * 
	 * For a given dataset, we need to know the detectors which can be used to evaluate the signal over that dataset.  So, what we really
	 * want to return is a HashMap<Signal,Set<Detector>> which will indicate, for each signal, which detector(s) can be used to evaluate it.
	 * 
	 * This will be those detectors which:
	 *   1) Can monitor the dataset view which brings the signal manifestation into existence
	 *   2) Can apply the signal associated with that manifestation
	 *   
	 * @param datasetViewIRI - The view individual to search for event instances in
	 * @param eventIRI - The event individual which is being targeted
	 * @return - A hash map of Signal -> Set of Detectors, listing the detectors which can represent each signal.
	 * @throws KIDSOntologyDatatypeValuesException 
	 */
	public Map<IRI, Set<IRI>> getSignalDetectorsForDataset(IRI datasetViewIRI, IRI eventIRI) throws KIDSOntologyDatatypeValuesException{
		Map<IRI, Set<IRI>> toReturn = new HashMap<IRI, Set<IRI>>();
		
		// Get the set of signal manifestations brought into existence by this dataset view
		Set<IRI> manifestations = this.getSignalManifestationsInDatasetView(datasetViewIRI);
		
		// Get the set of detectors which can monitor the dataset view
		Set<IRI> ourDetectors = getDetectorsForDatasetView(datasetViewIRI);
		
		// Get the signals associated with each manifestation
		for (IRI ourMan : manifestations){
			try{
				IRI ourSignal = this.getSignalForManifestation(ourMan);
				toReturn.put(ourSignal, new HashSet<IRI>());
				// Get the detectors which can 'apply' that signal, and which can also monitor the dataset view
				for (IRI ourDet: ourDetectors){
					Set<IRI> appSignals = this.getSignalsApplicableByDetector(ourDet);
					if (appSignals.contains(ourSignal)){
						toReturn.get(ourSignal).add(ourDet);
					}
				}
			} catch (KIDSOntologyDatatypeValuesException e){
				//System.err.println("[E] -- Ontology Datatype Values Exception");
				//e.printStackTrace();
				continue;
			}

		}
		
		return toReturn;
	}


	/**
	 * Will return a list of datasets which both include the given time period and (potentially) contain event
	 * related signals.
	 * 
	 * 
	 * @see http://solomon.cs.iastate.edu/ontologies/KIDS.owl#isRepresentedInDataset
	 * @see http://solomon.cs.iastate.edu/ontologies/KIDS.owl#isIncludedInDataset
	 * 
	 * @param EventIRI
	 * @param TimePeriodIRI
	 * @return A set of IRI values for each dataset that both contains the referenced time period and contains
	 *         the event IRI.
	 */
	public Set<String> getDatasetListForEventAndTimePeriod(IRI EventIRI,
			IRI TimePeriodIRI) {
		Set<String> returnSet = new HashSet<String>();
		// Ask for the intersection of objects which both "Is Evaluation Of Event" EventIRI, and "Includes Time Period" TimePeriodIRI
		Set<OWLNamedIndividual> eventDSes = 
							this.r.getObjectPropertyValues(
									this.odf.getOWLNamedIndividual(EventIRI),
									this.odf.getOWLObjectProperty(IRI.create(eventDatasetRelation))
							).getFlattened();
		Set<OWLNamedIndividual> timePeriodDSes = 
							this.r.getObjectPropertyValues(
								this.odf.getOWLNamedIndividual(TimePeriodIRI),
								this.odf.getOWLObjectProperty(IRI.create(timePeriodDatasetRelation))
							).getFlattened();

		Set<OWLNamedIndividual> s = r.getInstances(
				this.odf.getOWLObjectIntersectionOf(
						this.odf.getOWLObjectOneOf(eventDSes),
						this.odf.getOWLObjectOneOf(timePeriodDSes)
					), false).getFlattened();
		for (OWLNamedIndividual ds : s){
			returnSet.add(ds.getIRI().toString());
		}
		return returnSet;
	}

	/**
	 * The detector implementation for the given detector individual.  This is the actual java class
	 * which implements the detector evaluation on the dataset.
	 * 
	 * @see http://solomon.cs.iastate.edu/ontologies/KIDS.owl#hasImplementationClass
	 * 
	 * @param sigDet - The detector object for which to get the implementation class
	 * @return - A string representing the class to load for this detector implementation
	 */
	public String getDetectorImplementation(IRI sigDet) {
		// get the library string value for the given Class
		// The value is a data property of the individual:
		OWLDataProperty detectorImpl = odf.getOWLDataProperty(IRI.create(detectorImplementationDataProperty.toString()));
		//System.err.println("DEBUG: " + representationImpl);
		Set<OWLLiteral> oaSet = r.getDataPropertyValues(odf.getOWLNamedIndividual(sigDet), detectorImpl);
		if (oaSet.size() > 1){
			// Error: cannot identify class
			System.err.println("Too many values for implementation class of: " + sigDet);
			return null;
		} else if (oaSet.size() == 0) {
			// Cannot find any class for ourRep:
			System.err.println("Cannot find any implementation class for: " + sigDet);
			return null;
		} 

		return oaSet.iterator().next().getLiteral();
	}

	/**
	 * Convenience method to work w/ IRIs rather than Named Individuals.  Given a signal, 
	 * return the signal domain individual.
	 * 
	 * @param mySig
	 * @return the signal domain individual
	 * @throws KIDSOntologyObjectValuesException
	 */
	public IRI getSignalDomain(IRI mySig) throws KIDSOntologyObjectValuesException {
		OWLNamedIndividual s = odf.getOWLNamedIndividual(mySig);
		OWLNamedIndividual d = this.getSignalDomain(s);
		return d.getIRI();
	}

	/**
	 * Convenience method to work w/ IRIs rather than Named Individuals.  Given a signal, 
	 * return the signal value individual.
	 * 
	 * @param mySig
	 * @return the signal value individual
	 * @throws KIDSOntologyObjectValuesException
	 */
	public String getSignalValue(IRI mySig) throws KIDSOntologyDatatypeValuesException {
		OWLNamedIndividual s = odf.getOWLNamedIndividual(mySig);
		String v = this.getSignalValue(s);
		return v;
	}

	/**
	 * Convenience method to work w/ IRIs rather than Named Individuals.  Given a signal, 
	 * return the signal constraint.
	 * 
	 * @param mySig
	 * @return The signal constraint individual
	 * @throws KIDSOntologyObjectValuesException
	 */
	public IRI getSignalConstraint(IRI mySig) throws KIDSOntologyObjectValuesException {
		OWLNamedIndividual c = odf.getOWLNamedIndividual(mySig);
		return this.getSignalConstraint(c).getIRI();
	}

	/**
	 * Given a specification of a signal individual, will return the set of contexts which include
	 * that signal.
	 * 
	 * @see http://solomon.cs.iastate.edu/ontologies/KIDS.owl#isInContext
	 * 
	 * @param mySig - the signal under consideration
	 * @return - A set of contexts (IRIs) associated with this signal
	 * @throws KIDSOntologyObjectValuesException 
	 */
	public Set<IRI> getSignalContexts(IRI mySig) throws KIDSOntologyObjectValuesException {
		// Get the signal domain for this signal:
		IRI sDom = this.getSignalDomain(mySig);
		
		// Now iterate through all the contexts associated with this domain:
		NodeSet<OWLNamedIndividual> consets = this.r.getObjectPropertyValues(
									this.odf.getOWLNamedIndividual(sDom),
									this.odf.getOWLObjectProperty(IRI.create(domainContextRelation))
							);
		Set<OWLNamedIndividual> contexts = consets.getFlattened();
		
		Set<IRI> toReturn = new HashSet<IRI>();
		for (OWLNamedIndividual c : contexts){
			toReturn.add(c.getIRI());
		}
		
		return toReturn;
	}
	
	/**
	 * Given a signal, get the set of Datasets that the signal can be applied to,
	 * along with the detectors which can apply it.
	 * 
	 * @see http://solomon.cs.iastate.edu/ontologies/KIDS.owl#isEvaluableWithDataset
	 * 
	 * @param SignalIRI -- The signal individual under consideration.
	 * @return A hashmap of Dataset -> Set<Detector> IRIs. 
	 */
	public Map<IRI, Set<IRI>> getDatasetsForSignal(IRI SignalIRI){

		Map<IRI,Set<IRI>> returnMap = new HashMap<IRI,Set<IRI>>();
		
		Set<OWLNamedIndividual> dataSetSet = 
				this.r.getObjectPropertyValues(this.odf.getOWLNamedIndividual(SignalIRI), 
						this.odf.getOWLObjectProperty(IRI.create(signalDatasetRelation))).getFlattened();
		
		// For each dataset returned, determine the detectors which can be used to apply the signal
		// to the dataset:
		for (OWLNamedIndividual own : dataSetSet){
			Set<OWLNamedIndividual> viewSet = this.getAvailableViews(own.getIRI());
			Set<IRI> detectorSet = new HashSet<IRI>();

			for (OWLNamedIndividual oView : viewSet){
				detectorSet.addAll(this.getDetectorsForSignalAndView(SignalIRI, oView.getIRI()));
			}
			returnMap.put(own.getIRI(), detectorSet);
		}
		return returnMap;
	}
	
/*********************/
/** Utility Methods **/
/*********************/
	/**
	 * Returns a text dump for debugging the current ontology:
	 * @param fromReasoner - True - use reasoner interface; False - only dump explicit axioms
	 */
	public String ontoDump(boolean fromReasoner){
		if (fromReasoner){
			//TODO: Determine how best to list all inferred axioms
			System.err.println("[D] -- TODO: KIDSMeasurementOracle.OntoDump(boolean)");
			return this.o.getAxioms().toString();
		} else {
			return this.o.getAxioms().toString();
		}
	}

	
	/**
	 * Log (print) the reasoner details we are using:
	 */
	public void dumpReasonerInfo(){
				System.err.println("[D] -- Reasoner: " + r.getReasonerName() + " -- " + 
						r.getReasonerVersion().getBuild() + "." + 
						r.getReasonerVersion().getMajor() + "." + 
						r.getReasonerVersion().getMinor() + "." + 
						r.getReasonerVersion().getPatch());
	}

/****************/
/** DEPRECATED **/
/****************/
	/**
	 * Attempts to instantiate the library associated with the given dataset
	 * @param ourDataset
	 * @return
	 * @throws IOException 
	 * @throws KIDSOntologyDatatypeValuesException 
	 * @throws TruthFileParseException 
	 * @throws KIDSUnsupportedSchemeException 
	 * @throws ClassNotFoundException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 * @throws KIDSOntologyObjectValuesException 
	 * @throws KIDSUnEvaluableSignalException 
	 * @throws NumberFormatException 
	 * @throws KIDSIncompatibleSyntaxException 
	 * @throws UnimplementedIdentifyingFeatureException 
	 * 
	 * @deprecated -- This appears to no longer be used; the dataset factory should be employed instead.
	 */
	public Dataset getDatasetImplementation(
			OWLNamedIndividual ourDataset,
			IRI eventIRI) throws IOException, KIDSOntologyDatatypeValuesException, KIDSUnsupportedSchemeException, net.strasnet.kids.measurement.datasetlabels.TruthFileParseException, KIDSOntologyObjectValuesException, InstantiationException, IllegalAccessException, ClassNotFoundException, NumberFormatException, KIDSUnEvaluableSignalException, KIDSIncompatibleSyntaxException, UnimplementedIdentifyingFeatureException {
		// get the library string value for the given Class
		// The value is a data property of the individual:
		OWLDataProperty datasetImpl = odf.getOWLDataProperty(IRI.create(datasetParserImplementationProp.toString()));
		//System.err.println("DEBUG: " + representationImpl);
		Set<OWLLiteral> oaSet = r.getDataPropertyValues(ourDataset, datasetImpl);
		if (oaSet.size() > 1){
			// Error: cannot identify class
			System.err.println("Too many values for representation library of: " + ourDataset);
			return null;
		} else if (oaSet.size() == 0) {
			// Cannot find any class for ourRep:
			System.err.println("Cannot find any representation library for: " + ourDataset);
			return null;
		} else {
			OWLLiteral oa = oaSet.iterator().next();
			
			// For each data set, get the URI of the instances and the URI of the labels:
			Set<OWLLiteral> dataLocation =
					r.getDataPropertyValues(
							ourDataset,
							odf.getOWLDataProperty(IRI.create( datasetInstanceResourceProp)));
			
			if (dataLocation.size() != 1){
				throw new KIDSOntologyDatatypeValuesException();
			}
			
			// Now try to instantiate the class:
			return KIDSDatasetFactory.getViewLabelDataset(ourDataset.getIRI(), eventIRI, this);
		}
	}

	/**
	 * Each truth file is related to both an event and a dataset.  This method will return the 
	 * truth file given for each.  There should only ever be one.  For specific semantics:
	 * @see http://solomon.cs.iastate.edu/ontologies/KIDS.owl#datasetLabelLocation
	 * 
	 * @param testeventiri
	 * @param ourDataset
	 * @return - The truth file IRI for the given dataset
	 * @throws KIDSOntologyDatatypeValuesException
	 * 
	 * @deprecated - Truth files are no longer associated with datasets, rather dataset views.
	 */
	public IRI getTruthFileForEvent(IRI testeventiri, OWLNamedIndividual ourDataset) throws KIDSOntologyDatatypeValuesException {
			Set<OWLLiteral> labelLocation =
					r.getDataPropertyValues(
							ourDataset,
							odf.getOWLDataProperty(IRI.create( datasetLabelResourceProp)));
			
			if (labelLocation.size() != 1){
				throw new KIDSOntologyDatatypeValuesException();
			}
			return IRI.create(labelLocation.iterator().next().getLiteral());
	}

	/**
	 * Given a detector syntax, return the known signals that it can represent.  Will be the set of signals for which the 
	 * domain is extractable, and the constraint is representable.  For detailed semantics:
	 * 
	 * @see http://solomon.cs.iastate.edu/ontologies/KIDS.owl#canRepresentFeatureWithConstaint
	 * 
	 * @param detectorSyntax - The syntax individual we want the signals for.
	 * 
	 * @deprecated - Not currently in use
	 */
	public Set<IRI> getSignalsRepresentableInSyntax(IRI detectorSyntax){
		HashSet<IRI> returnValue = new HashSet<IRI>();
		OWLNamedIndividual ourSyn = odf.getOWLNamedIndividual(detectorSyntax);
		
		// Get the set of signals with an extractable domain:
		//   -- property: http://solomon.cs.iastate.edu/ontologies/KIDS.owl#canRepresentFeatureWithConstraint
		Set<OWLNamedIndividual> retTemp = r.getObjectPropertyValues(ourSyn, odf.getOWLObjectProperty(IRI.create(KIDSMeasurementOracle.detectorSyntaxSignalRelation))).getFlattened();
		for (OWLNamedIndividual oni: retTemp){
			returnValue.add(oni.getIRI());
		}
		return returnValue;
	}

	/**
	 * Given a dataset view IRI, will return the dataset it provides a view of.  For detailed semantics:
	 * 
	 * @see http://solomon.cs.iastate.edu/ontologies/KIDS.owl#providesViewOf
	 * 
	 * @param datasetView - The view that we want to identify the dataset for
	 * @throws KIDSOntologyDatatypeValuesException 
	 * 
	 * @deprecated - Not currently used by anything
	 */
	public IRI getDatasetForDatasetView(IRI datasetView) throws KIDSOntologyDatatypeValuesException{
		OWLNamedIndividual ourDV = odf.getOWLNamedIndividual(datasetView);
		Set<OWLNamedIndividual> ourDSSet = r.getObjectPropertyValues(ourDV, odf.getOWLObjectProperty(IRI.create(KIDSMeasurementOracle.datasetViewIsViewOfDatasetRelation))).getFlattened();
		if (ourDSSet.size() > 1){
			throw new KIDSOntologyDatatypeValuesException("Too many values for property " + KIDSMeasurementOracle.datasetViewIsViewOfDatasetRelation + " on individual " + datasetView);
		} else if (ourDSSet.size() == 0){
			throw new KIDSOntologyDatatypeValuesException("No values for property " +  KIDSMeasurementOracle.datasetViewIsViewOfDatasetRelation  + " on individual " + datasetView);
		}	
		return ourDSSet.iterator().next().getIRI();
			
	}

	public Set<OWLNamedIndividual> getResourcesProvidedBySignalDomainContext (IRI context){
		OWLNamedIndividual ourCtxt = odf.getOWLNamedIndividual(context);
		return r.getObjectPropertyValues(ourCtxt, odf.getOWLObjectProperty(IRI.create(KIDSMeasurementOracle.resourceProviderRelation))).getFlattened();
	}

	public HashSet<OWLNamedIndividual> getResourcesProvidedBySignal(IRI signal) throws KIDSOntologyObjectValuesException{
		HashSet<OWLNamedIndividual> returnValue = new HashSet<>();
		Set<IRI> contexts = this.getSignalContexts(signal);
		for(IRI context : contexts){
			returnValue.addAll(this.getResourcesProvidedBySignalDomainContext(context));
		}
		return returnValue;
	}


	public boolean isResourcesAvailable(Set<IRI> signals,Set<OWLNamedIndividual> resources) throws KIDSOntologyObjectValuesException {
		HashSet<String> availableResources = new HashSet<>();
		HashSet<String> requiredResources = new HashSet<>();
		for(IRI signal : signals){
			for(OWLNamedIndividual resource : getResourcesProvidedBySignal(signal)){
				availableResources.add(this.getTypeOfIndividual(resource).toString());
			}
		}
		for(OWLNamedIndividual resource : resources){
			requiredResources.add(this.getTypeOfIndividual(resource).toString());
		}
		for(String reqd : requiredResources){
			if(!availableResources.contains(reqd)){
				return false;
			}
		}
		return true;
	}
}
