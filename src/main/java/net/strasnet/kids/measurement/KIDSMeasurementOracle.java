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

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLNamedObject;
import org.semanticweb.owlapi.model.OWLObjectOneOf;
import org.semanticweb.owlapi.reasoner.NodeSet;

import net.strasnet.kids.KIDSCanonicalRepresentation;
import net.strasnet.kids.KIDSCanonicalRepresentationFactory;
import net.strasnet.kids.KIDSOntologyDatatypeValuesException;
import net.strasnet.kids.KIDSOntologyObjectValuesException;
import net.strasnet.kids.KIDSOracle;
import net.strasnet.kids.detectors.KIDSDetector;
import net.strasnet.kids.detectorsyntaxproducers.KIDSDetectorSyntax;
import net.strasnet.kids.detectorsyntaxproducers.KIDSIncompatibleSyntaxException;
import net.strasnet.kids.measurement.correlationfunctions.KIDSCorrelationFunctionFactory;
import net.strasnet.kids.measurement.datasetlabels.DatasetLabel;
import net.strasnet.kids.measurement.datasetviews.DatasetView;
import net.strasnet.kids.measurement.datasetviews.KIDSUnsupportedSchemeException;

public class KIDSMeasurementOracle extends KIDSOracle {
	public static final String kidsTBOXLocation = "http://solomon.cs.iastate.edu/ontologies/KIDS.owl";
	private static final String contextDomainRelation = kidsTBOXLocation + "#isContextOfSignalDomain";
	private static final String datasetInstanceResourceProp = kidsTBOXLocation + "#datasetLocation";
	private static final String datasetLabelResourceProp = kidsTBOXLocation + "#datasetLabelLocation";
	public static final String datasetParserImplementationProp = kidsTBOXLocation + "#datasetParserImplementation";
	private static final String datasetTimePeriodRelation = kidsTBOXLocation + "#includesTimePeriod";
	private static final String datasetViewCorrelationRelation = kidsTBOXLocation + "#supportCorrelationRelation";
	private static final String datasetViewSignalManifestationRelation = kidsTBOXLocation + "#bringsIntoExistence";
	private static final String correlationRelationDatasetViewRelation = kidsTBOXLocation + "#isSupportedByDatasetView";
	private static final String datasetViewRelation = kidsTBOXLocation + "#isViewableAs";
	private static final String datasetContextRelation = kidsTBOXLocation + "#isContainerOfContext";
	private static final String datasetSignalRelation = kidsTBOXLocation + "#isCompatibleDatasetForSignal";
	private static final String signalDatasetRelation = kidsTBOXLocation + "#isEvaluableWithDataset";
	public static final String eventDatasetRelation = kidsTBOXLocation + "#isRepresentedInDataset";
	private static final String eventLabelRelation = kidsTBOXLocation + "#isIncludedInLabel";
	private static final String eventSignalRelation = kidsTBOXLocation + "#isProducerOf";
	private static final String labelViewRelation = kidsTBOXLocation + "#isLabelerForDatasetView";
	private static final String signalDomainSignalRelation = kidsTBOXLocation + "#isDomainOfSignal";
	private static final String signalConstraintSignalRelation = kidsTBOXLocation + "#hasConstraint";
	private static final String viewLabelRelation = kidsTBOXLocation + "#hasDatasetLabel";
	private static final String signalDetectorRelation = kidsTBOXLocation + "#isAppliedByDetector";
	private static final String detectorSignalRelation = kidsTBOXLocation + "#canApplySignal";
	private static final String signalRepresentationRelation = kidsTBOXLocation + "#isRepresentedBy";
	private static final String signalManifestationRelation = kidsTBOXLocation + "#SignalInManifestation";
	private static final String manifestationSignalRelation = kidsTBOXLocation + "#SignalManifestationIncludesSignal";
	private static final String viewDetectorRelation = kidsTBOXLocation + "#isMonitoredBy";
	private static final String labelClassDataProperty = kidsTBOXLocation + "#hasLabelFunction";
	private static final String labelLocationDataProperty = kidsTBOXLocation + "#hasLabelDataLocation";
	private static final String timePeriodDatasetRelation = kidsTBOXLocation + "#isIncludedInDataset";
	private static final String viewClassDataProperty = kidsTBOXLocation + "#viewProductionImplementation";
	private static final String detectorSyntaxRelation = kidsTBOXLocation + "#hasSyntax";
	private static final String detectorSyntaxImplementationDataProperty = kidsTBOXLocation + "#hasSyntaxProductionImplementation";
	private static final String detectorImplementationDataProperty = kidsTBOXLocation + "#hasImplementationClass";
	private static final String detectorExecutionDataProperty = kidsTBOXLocation + "#detectorExecutionCommand";
	private static final String correlationFunctionImplementationDataProperty = kidsTBOXLocation + "#hasCorrelationRelationImplementation";
	private static final String detectorSyntaxSignalRelation = kidsTBOXLocation + "#canRepresentFeatureWithConstraint";
	private static final String datasetViewIsViewOfDatasetRelation = kidsTBOXLocation + "#providesViewOf";
	
	/**
	 * At this point, we can probably assume we're returning a ViewLabelDataset:
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
		
		/**
		// For each dataset, get the implementing class and load it:
		Iterator<OWLNamedIndividual> i = rList.iterator();
		
		List<IRI> toReturn = new LinkedList<IRI>();
		
		while (i.hasNext()){
			OWLNamedIndividual ind = i.next();
			try{
				toReturn.add(getDatasetImplementation(ind, testeventiri));
				toReturn.get(toReturn.size() - 1).init(eventClass);
			} catch (IOException e) {
				System.err.println("[W]: Could not read a datasource for " + ind);
			} catch (KIDSOntologyDatatypeValuesException e){
				System.err.println("[W]: Problem with datatype value cardinality for " + ind);
			}
		}	
		
		return toReturn;
		*/
	}

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
	 */
	public Dataset getDatasetImplementation(
			OWLNamedIndividual ourDataset,
			IRI eventIRI) throws IOException, KIDSOntologyDatatypeValuesException, KIDSUnsupportedSchemeException, net.strasnet.kids.measurement.datasetlabels.TruthFileParseException, KIDSOntologyObjectValuesException, InstantiationException, IllegalAccessException, ClassNotFoundException, NumberFormatException, KIDSUnEvaluableSignalException, KIDSIncompatibleSyntaxException {
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
	 * Given a dataset IRI, get the signals which are associated with this dataset.  In this method, we return all
	 * signals which apply to this dataset, regardless of current association with any event.
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
				toReturn.addAll(myij.getFlattened()); // Need a chain of dataSetContains -> isContextOfDomain -> isDomainOfSignal
				System.err.println("[D] -- " + r.getReasonerName());
				System.err.println("[D] -- " + r.getReasonerVersion().getBuild() + "." + r.getReasonerVersion().getMajor() + "." + r.getReasonerVersion().getMinor() + "." + r.getReasonerVersion().getPatch());
				OWLClassExpression oce = this.odf.getOWLObjectHasValue(odf.getOWLObjectProperty(IRI.create(signalDatasetRelation)), odf.getOWLNamedIndividual(datasetIRI));
				System.err.println("[D] -- " + oce.toString());
				NodeSet<OWLNamedIndividual> secondSet = r.getInstances(this.odf.getOWLObjectHasValue(odf.getOWLObjectProperty(IRI.create(signalDatasetRelation)), odf.getOWLNamedIndividual(datasetIRI)), false);
		return toReturn;
	}

	/**
	 * Given an event and dataset IRI, get the signals which are associated with this dataset.  More specifically, we would like
	 * the set of signals satisfying: Produced by event e and defined over a feature represented in one of the contexts contained 
	 * within the dataset.
	 * @param datasetIRI - The IRI of the dataset we should get signals for:
	 * @param eventIRI - The IRI of the event individual we are considering
	 * @return A list of signal IRIs which are available in a given dataset.
	 */
	public Set<IRI> getSignalsForDatasetAndEvent(IRI datasetIRI, IRI eventIRI) {
		Set<IRI> toReturn = new HashSet<IRI>();
		
		Set<OWLNamedIndividual> erSet = 	
							this.r.getObjectPropertyValues(
									this.odf.getOWLNamedIndividual(eventIRI),
									this.odf.getOWLObjectProperty(IRI.create(eventSignalRelation))
							).getFlattened();
							
		Set<OWLNamedIndividual> drSet = 
							this.r.getObjectPropertyValues(
								this.odf.getOWLNamedIndividual(datasetIRI),
								this.odf.getOWLObjectProperty(IRI.create( datasetSignalRelation))
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
	 * 
	 * @param testeventiri
	 * @param ourDataset
	 * @return - The truth file IRI for the given dataset
	 * @throws KIDSOntologyDatatypeValuesException
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
	 * Returns a text dump for debugging the current ontology:
	 * @param fromReasoner - True - use reasoner interface; False - only dump explicit axioms
	 */
	public String ontoDump(){
		return this.o.getAxioms().toString();
	}

	/**
	 * Return the list of detectors which are possible evaluators for the event.
	 * @param event
	 * @return A list of detectors.
	 */
	public List<OWLNamedIndividual> getPossibleEvaluatorsFor(
			IRI event) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Return the set of detectable signals both produced by event, and detectable by 'd'
	 * @param event
	 * @param d
	 * @return
	 */
	public Set<OWLNamedIndividual> getDetectableSignals(IRI event,
			OWLNamedIndividual d) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Get the available view list for this dataset
	 * @param testdatasetiri
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
						    this.r.getObjectPropertyValues(
						       this.odf.getOWLNamedIndividual(datasetIRI),
						       this.odf.getOWLObjectProperty(IRI.create(datasetViewRelation))
						    ).getFlattened()
						),
						this.odf.getOWLObjectOneOf(views)
					), 
				false).getFlattened());
		
		return toReturn;
	}

	/**
	 * 
	 * @param signal
	 * @param view
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
	 * 
	 * @param viewIndividual - The view instance we want to instantiate
	 * @return
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
	 * Return the label individual which, applied to datasetView, provides the truth about testeventiri
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
	 * Given a signal individual, return the constraint individual associated with it:
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
	 * 
	 * @param ourIRI - The IRI of the detector to get the syntax for -- for now, assuming each detector has a single
	 * syntax object - may be reasonable since it translates signals into detector definitions...
	 * @return
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
	 * TODO: This assumes that there is only one detector for a given view.  Need to review this assumption.
	 * @param viewIRI - The IRI of the dataset view for which to get a compatible detector
	 * @return An instantiation of a KIDSDetector object.
	 * @throws KIDSOntologyObjectValuesException 
	 * @throws ClassNotFoundException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 * @throws KIDSOntologyDatatypeValuesException 
	 */
	public KIDSDetector getDetectorForView(IRI viewIRI) throws KIDSOntologyObjectValuesException, InstantiationException, IllegalAccessException, ClassNotFoundException, KIDSOntologyDatatypeValuesException {
		// First lookup the detector implementation:
		Set<OWLNamedIndividual> results = r.getObjectPropertyValues(odf.getOWLNamedIndividual(viewIRI), 
				odf.getOWLObjectProperty(IRI.create(KIDSMeasurementOracle.viewDetectorRelation))).getFlattened();
		if (results.size() == 0){
			throw new KIDSOntologyObjectValuesException("No values for property " + viewDetectorRelation + " on individual " + viewIRI);
		} else if (results.size() > 1){
			System.out.println("Warning: " + results.size() + " values for property " + viewDetectorRelation + " on individual " + viewIRI + ": choosing [" + results.iterator().next().getIRI() + "]");
		}
		
		OWLNamedIndividual detector = results.iterator().next();
		
		// Now get the implementation class:
		Set<OWLLiteral> detectorImpl =
				r.getDataPropertyValues(detector, 
						odf.getOWLDataProperty(IRI.create(detectorImplementationDataProperty)));
		
		if (detectorImpl.size() != 1){
			throw new KIDSOntologyDatatypeValuesException("Too many values for property " + detectorImplementationDataProperty + " on individual " + detector);
		}
		
		KIDSDetector d = (KIDSDetector)Class.forName(detectorImpl.iterator().next().getLiteral()).newInstance();
		d.init(this.getDetectorExecutionString(detector.getIRI()), 
				detector.getIRI(), 
				this);
		return d;
	}

	/**
	 * 
	 * @param iri - The IRI of the detector individual we are working with.
	 * @return The string used to execute this detector.
	 * @throws KIDSOntologyDatatypeValuesException 
	 */
	public String getDetectorExecutionString(IRI detector) throws KIDSOntologyDatatypeValuesException {
		Set<OWLLiteral> detectorImpl =
				r.getDataPropertyValues(this.odf.getOWLNamedIndividual(detector), 
						odf.getOWLDataProperty(IRI.create(detectorExecutionDataProperty)));
		
		if (detectorImpl.size() != 1){
			throw new KIDSOntologyDatatypeValuesException("Too many values for property " + detectorExecutionDataProperty + " on individual " + detector);
		}
		return detectorImpl.iterator().next().getLiteral();
	}

	public String getLabelImplementation(OWLNamedIndividual label) throws KIDSOntologyDatatypeValuesException {
		Set<OWLLiteral> results = r.getDataPropertyValues(
					label, 
					odf.getOWLDataProperty(IRI.create(labelClassDataProperty)));
		if (results.size() != 1){
			throw new KIDSOntologyDatatypeValuesException("Too many values for property " + labelClassDataProperty + " on individual " + label);
		}
		return results.iterator().next().getLiteral();
	}

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
	 * Given a detector syntax, return the known signals that it can represent.  Will be the set of signals for which the 
	 * domain is extractable, and the constraint is representable.
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
	 * Given a dataset view IRI, will return the dataset it provides a view of:
	 * @throws KIDSOntologyDatatypeValuesException 
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
	
	/**
	 * 
	 * @param dvIRI
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
	 * 
	 * @param dvIRI - The IRI of the dataset view we want to get detectors for
	 * @return The set of detector individuals which can observe this dataset view
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
	 * 
	 * @param ourMan - The IRI of the signal manifestation we want to get the signal for
	 * @return The signal IRI individuals which is included in this manifestation
	 * @throws KIDSOntologyDatatypeValuesException 
	 */
	public IRI getSignalForManifestation (IRI ourMan) throws KIDSOntologyDatatypeValuesException{
		IRI toReturn = null;
		
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
	 * A signal can be applied by a detector 
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
	 *   @return - A hash map of Signal -> Set of Detectors, listing the detectors which can represent each signal.
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
				System.err.println("[E] -- Ontology Datatype Values Exception");
				e.printStackTrace();
			}

		}
		
		return toReturn;
	}


	/**
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
}
