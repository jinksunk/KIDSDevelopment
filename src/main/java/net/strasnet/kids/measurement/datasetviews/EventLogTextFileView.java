package net.strasnet.kids.measurement.datasetviews;

import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

import net.strasnet.kids.KIDSOntologyDatatypeValuesException;
import net.strasnet.kids.KIDSOntologyObjectValuesException;
import net.strasnet.kids.detectors.KIDSDetector;
import net.strasnet.kids.detectorsyntaxproducers.KIDSIncompatibleSyntaxException;
import net.strasnet.kids.measurement.DataInstance;
import net.strasnet.kids.measurement.EventOccurrence;
import net.strasnet.kids.measurement.KIDSMeasurementOracle;
import net.strasnet.kids.measurement.KIDSUnEvaluableSignalException;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLNamedIndividual;

public class EventLogTextFileView implements DatasetView, java.io.Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2002776263250625193L;
	private HashSet<DataInstance> ourInstances;    		  // All of the instances in the dataset
	private KIDSMeasurementOracle myGuy;			      // The KIDSOracle used to interact with the KB
	private IRI ourIRI = null;					  // The base IRI of 
	
												  // Map of event to another map of data instance
												  // (since an event will generally have many instances)
	private TreeMap<EventOccurrence,Boolean> eventList = null;
	private TreeMap<EventOccurrence,TreeMap<DataInstance,Boolean>> instancesByEvent = null;
	private TreeMap<OWLNamedIndividual,TreeMap<DataInstance,Boolean>> instancesBySignalMatch = null;
	private String datasetLocation = null;
	private KIDSDetector ourDetector = null;
	private List<IRI> identifyingFeatures = null;
	private Set<DataInstance> viewFilter = null;

	/**
	 * 
	 */
	public EventLogTextFileView () {
		ourInstances = new HashSet<DataInstance>();
		instancesByEvent = new TreeMap<EventOccurrence,TreeMap<DataInstance,Boolean>>();
		instancesBySignalMatch = new TreeMap<OWLNamedIndividual,TreeMap<DataInstance,Boolean>>();
		eventList = new TreeMap<EventOccurrence,Boolean>();
	}

	@Override
	public void generateView(String datasetLocation, KIDSMeasurementOracle o,
			List<IRI> identifyingFeatures)
			throws KIDSOntologyDatatypeValuesException,
			KIDSOntologyObjectValuesException, InstantiationException,
			IllegalAccessException, ClassNotFoundException {
		this.ourInstances = new HashSet<DataInstance>();
		this.datasetLocation = datasetLocation;
		this.myGuy = o;
		ourDetector = o.getDetectorForView(ourIRI);
		this.identifyingFeatures = new LinkedList<IRI>();
		for (IRI i : identifyingFeatures){
			this.identifyingFeatures.add(i);
		}

	}

	@Override
	public int numInstances() throws KIDSUnEvaluableSignalException {
		if (ourInstances.size() == 0){
			Set<DataInstance> iSet;
			try {
				iSet = this.getMatchingInstances(new HashSet<IRI>());
				ourInstances.addAll(iSet);
			} catch (KIDSOntologyObjectValuesException
					| KIDSOntologyDatatypeValuesException | IOException
					| KIDSIncompatibleSyntaxException e) {
				e.printStackTrace();
			}
		}
		return ourInstances.size();
	}

	@Override
	public List<IRI> getIdentifyingFeatures() {
		return this.identifyingFeatures;
	}

	@Override
	public String getViewLocation() {
		return this.datasetLocation;
	}

	@Override
	public Iterator<DataInstance> iterator() throws IOException,
			KIDSUnEvaluableSignalException, KIDSOntologyObjectValuesException, KIDSOntologyDatatypeValuesException, KIDSIncompatibleSyntaxException {
		if (ourInstances == null || ourInstances.size() == 0){
			try {
				ourInstances = (HashSet<DataInstance>) getMatchingInstances(new HashSet<IRI>());
			} catch (KIDSOntologyObjectValuesException
					| KIDSOntologyDatatypeValuesException
					| KIDSIncompatibleSyntaxException e) {
				e.printStackTrace();
			}
		}
		return this.getMatchingInstances(new HashSet<IRI>()).iterator();
	}

	@Override
	public Set<DataInstance> getMatchingInstances(Set<IRI> signalSet)
			throws KIDSOntologyObjectValuesException,
			KIDSOntologyDatatypeValuesException, IOException,
			KIDSIncompatibleSyntaxException, KIDSUnEvaluableSignalException {
		try {
		    Set<DataInstance> allMatching = ourDetector.getMatchingInstances(signalSet, this);
		    if (viewFilter != null){
			    Iterator<DataInstance> instanceIter = allMatching.iterator();
			    while (instanceIter.hasNext()){
				    DataInstance d = instanceIter.next();
				    if (!viewFilter.contains(d)){
					    instanceIter.remove();
				    }
			    }
		    } 
		    return allMatching;
		} catch (KIDSIncompatibleSyntaxException e){
			StringBuilder sb = new StringBuilder();
			for (IRI sig : signalSet){
				sb.append(sig.toString());
			}
			System.err.println("Warning: incompatible syntax expression from signal set: {" + sb.toString() + "}");
			return new HashSet<DataInstance>();
		}
	}

	@Override
	public DatasetView getSubview(Set<DataInstance> members)
			throws KIDSOntologyDatatypeValuesException,
			KIDSOntologyObjectValuesException, InstantiationException,
			IllegalAccessException, ClassNotFoundException {
		EventLogTextFileView ourDV = new EventLogTextFileView();
		ourDV.setIRI(ourIRI);
		ourDV.generateView(datasetLocation, myGuy, identifyingFeatures);
		ourDV.setViewFilter(members);
		return ourDV;
	}
	
	// Need a copy here:
	protected void setViewFilter(Set<DataInstance> includedMembers){
		viewFilter = new HashSet<DataInstance>();
		viewFilter.addAll(includedMembers);
	}

	@Override
	public void setIRI(IRI iri) {
		this.ourIRI = iri;
	}

	@Override
	public IRI getIRI() {
		return this.ourIRI;
	}

}
