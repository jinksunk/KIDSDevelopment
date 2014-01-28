package net.strasnet.kids.measurement;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import net.strasnet.kids.KIDSOntologyDatatypeValuesException;
import net.strasnet.kids.KIDSOntologyObjectValuesException;
import net.strasnet.kids.detectorsyntaxproducers.KIDSIncompatibleSyntaxException;
import net.strasnet.kids.measurement.datasetlabels.DatasetLabel;
import net.strasnet.kids.measurement.datasetviews.DatasetView;
import net.strasnet.kids.measurement.datasetviews.KIDSLibpcapDataset;
import net.strasnet.kids.measurement.datasetviews.KIDSUnsupportedSchemeException;
import net.strasnet.kids.measurement.datasetviews.KIDSLibpcapDataset.KIDSLibpcapTruthFile.TruthFileParseException;
import net.strasnet.kids.signalRepresentations.KIDSRepresentationInvalidRepresentationValueException;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLNamedIndividual;

public class ViewLabelDataset implements Dataset {
	
	private DatasetView dv;
	private DatasetView positiveOnlyView;
	private HashMap<IRI, DatasetView> signalToDV;
	private DatasetLabel dl;
	private IRI eventIRI;
	private KIDSMeasurementOracle myGuy;
	private IRI ourIRI;
	private String datasetLocation;
	
	public ViewLabelDataset(){
		dv = null;
		positiveOnlyView = null;
		signalToDV = null;
		dl = null;
		eventIRI = null;
		myGuy = null;
	}
	
	@Override
	/**
	 * Initialize this dataset:
	 * @param dv
	 * @param dl
	 * @param o
	 * @param eventIRI
	 */
	public void init(DatasetView dv, 
			DatasetLabel dl, 
			KIDSMeasurementOracle o,
			IRI eventIRI) throws KIDSOntologyDatatypeValuesException, KIDSOntologyObjectValuesException, InstantiationException, IllegalAccessException, ClassNotFoundException{
		this.datasetLocation = o.getDatasetLocation(o.getOwlDataFactory().getOWLNamedIndividual(this.ourIRI));
		this.dv = dv;
		dv.generateView(o.getDatasetLocation(o.getOwlDataFactory().getOWLNamedIndividual(this.ourIRI)), 
				o, 
				dl.getIdentifyingFeatures());
		this.dl = dl;
		this.myGuy = o;
		this.eventIRI = eventIRI;
		this.signalToDV = new HashMap<IRI, DatasetView>();
	}

	@Override
	/**
	 * Return the number of instances in the dataset view:
	 */
	public int numInstances() throws KIDSUnEvaluableSignalException {
		return dv.numInstances();
	}

	@Override
	/**
	 * Return iterator from underlying data view
	 */
	public Iterator<DataInstance> getIterator() throws IOException, KIDSUnEvaluableSignalException {
		return dv.iterator();
	}

	@Override
	/**
	 * Return iterator from underlying data view, only including those instances which are "positive"
	 */
	public Iterator<DataInstance> getPositiveIterator() throws KIDSOntologyDatatypeValuesException, KIDSOntologyObjectValuesException, InstantiationException, IllegalAccessException, ClassNotFoundException, IOException, KIDSUnEvaluableSignalException {
		
		if (this.positiveOnlyView == null){
			Set<DataInstance> pSet = new HashSet<DataInstance>();
			
  		    // Iterate over instances, adding those which are positive to the result set.
			Iterator<DataInstance> i = this.getIterator();
			while (i.hasNext()){
				DataInstance ti = i.next();
				if (dl.getLabel(ti).isEvent()){
					pSet.add(ti);
				}
			}
		    // Create a new DV based on the result set.
			positiveOnlyView = dv.getSubview(pSet);
		}
		
		return positiveOnlyView.iterator();
	}

	@Override
	/**
	 * From data label - returns the number of events contained within the label - e.g. those potentially detectable
	 * from the parent dataset:
	 */
	public int numEventOccurrences() {
		return dl.getNumEvents();
		/*
		HashMap<EventOccurrence,Integer> hm = new HashMap<EventOccurrence,Integer>();
		Iterator<DataInstance> dveIter = dv.getIterator();
		while (dveIter.hasNext()){
			DataInstance dve = dveIter.next();
			hm.put(dve.getLabel().getEventOccurrence(), 0);
		}
		return hm.keySet().size() - 1; // -1 for the non-event instances
		*/
	}

	@Override
	/**
	 * 
	 */
	public int[] numPositiveInstances() throws KIDSOntologyDatatypeValuesException, KIDSOntologyObjectValuesException, InstantiationException, IllegalAccessException, ClassNotFoundException, IOException, KIDSUnEvaluableSignalException {
		List<EventOccurrence> allEvents = dl.getEventList();
		HashMap<EventOccurrence,Integer> hm = new HashMap<EventOccurrence,Integer>();
		Iterator<DataInstance> dveIter = getPositiveIterator();
		
		for (EventOccurrence ourEvent : allEvents){
			if (!hm.containsKey(ourEvent)){
				hm.put(ourEvent, 0);
			}
		}
        while (dveIter.hasNext()){
			DataInstance dve = dveIter.next();
			EventOccurrence dlo = dl.getLabel(dve).getEventOccurrence();
			hm.put(dlo, hm.get(dlo) + 1);
		}
		EventOccurrence[] eArray = new EventOccurrence[hm.size()];
		asSortedList(hm.keySet()).toArray(eArray);
		int[] retVal = new int[hm.size()];
		for (int i = 0; i < eArray.length; i++){
			retVal[i] = hm.get(eArray[i]).intValue();
		}
		
		return retVal;
	}

	@Override
	public void setDataIRI(String dataURI)
			throws KIDSUnsupportedSchemeException {
		// TODO Auto-generated method stub
	}

	@Override
	public void setLabelIRI(String labelURI)
			throws KIDSUnsupportedSchemeException {
		// TODO Auto-generated method stub

	}

	@Override
	public void init(IRI eventIRI) throws IOException, TruthFileParseException {
		// TODO Auto-generated method stub

	}

	@Override
	public void init(IRI eventIRI, Set<DataInstance> iSet,
			TreeMap<EventOccurrence, Boolean> eList) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setDatasetIRI(IRI dsIRI) {
		this.ourIRI = dsIRI;
	}

	@Override
	public IRI getIRI() {
		return ourIRI;
	}

	@Override
	public void setOracle(KIDSMeasurementOracle kidsOracle) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setEventIRI(IRI evIRI) {
		this.eventIRI = evIRI;
	}

	@Override
	public Set<DataInstance> getMatchingInstances(Set<IRI> signalSet) throws KIDSOntologyObjectValuesException, KIDSOntologyDatatypeValuesException, IOException, KIDSIncompatibleSyntaxException, KIDSUnEvaluableSignalException {
		return dv.getMatchingInstances(signalSet);
	}

	@Override
	public Dataset getDataSubset(Set<DataInstance> dataInstanceSet) throws KIDSOntologyDatatypeValuesException, KIDSOntologyObjectValuesException, InstantiationException, IllegalAccessException, ClassNotFoundException {
		//public ViewLabelDataset(DatasetView dv, DatasetLabel dl, KIDSMeasurementOracle o){
		ViewLabelDataset vld = new ViewLabelDataset();
		vld.setDatasetIRI(ourIRI);
		vld.init(dv.getSubview(dataInstanceSet), dl, myGuy, eventIRI);
		return vld;
	}

	@Override
	public List<OWLNamedIndividual> getKnownApplicableSignals() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public KIDSMeasurementOracle getKIDSOracle() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void indexInstancesBySignal(OWLNamedIndividual signal) {
		// TODO Auto-generated method stub

	}
	
	private static
	<T extends Comparable<? super T>> List<T> asSortedList(Collection<T> c) {
		  List<T> list = new ArrayList<T>(c);
		  java.util.Collections.sort(list);
		  return list;
		}

	@Override
	public IRI getViewIRI() {
		return this.dv.getIRI();
	}

}
