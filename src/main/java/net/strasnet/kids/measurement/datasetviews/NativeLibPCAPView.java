package net.strasnet.kids.measurement.datasetviews;

import intervalTree.IntervalTree;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLNamedIndividual;

import net.strasnet.kids.KIDSCanonicalRepresentation;
import net.strasnet.kids.KIDSOntologyDatatypeValuesException;
import net.strasnet.kids.KIDSOntologyObjectValuesException;
import net.strasnet.kids.KIDSOracle;
import net.strasnet.kids.KIDSSignal;
import net.strasnet.kids.datasources.KIDSSnortIPAddressRange;
import net.strasnet.kids.detectors.KIDSDetector;
import net.strasnet.kids.detectorsyntaxproducers.KIDSIncompatibleSyntaxException;
import net.strasnet.kids.measurement.DataInstance;
import net.strasnet.kids.measurement.Dataset;
import net.strasnet.kids.measurement.EventOccurrence;
import net.strasnet.kids.measurement.KIDSDatasetFactory;
import net.strasnet.kids.measurement.KIDSMeasurementIncompatibleContextException;
import net.strasnet.kids.measurement.KIDSMeasurementInstanceUnsupportedFeatureException;
import net.strasnet.kids.measurement.KIDSMeasurementOracle;
import net.strasnet.kids.measurement.KIDSUnEvaluableSignalException;
import net.strasnet.kids.measurement.Label;
import net.strasnet.kids.measurement.datasetinstances.KIDSNativeLibpcapDataInstance;
import net.strasnet.kids.measurement.datasetlabels.KIDSNativeLibpcapTruthFile;
import net.strasnet.kids.measurement.datasetlabels.TruthFileParseException;
import net.strasnet.kids.signalRepresentations.KIDSRepresentationIncompatibleValueException;
import net.strasnet.kids.signalRepresentations.KIDSRepresentationInvalidRepresentationValueException;
/**
 * Represents a LIBPCAP file as read by tcpdump -r <file> -n -n.  Produces lines such as:
 * 
 * 16:20:59.752086 IP (tos 0x0, ttl 64, id 4, offset 0, flags [none], proto TCP (6), length 429)
    130.130.130.130.20 > 131.131.131.131.80: Flags [S], cksum 0x4e3f (correct), seq 0:389, win 8192, length 389
 * 
 * To support measurement, the data must be accompanied by a "truth file", provided at init() time.  
 * 
 * @author chrisstrasburg
 *
 */
public class NativeLibPCAPView implements DatasetView, java.io.Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2243873069953554553L;
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
	 */
	public NativeLibPCAPView () {
		ourInstances = new HashSet<DataInstance>();
		instancesByEvent = new TreeMap<EventOccurrence,TreeMap<DataInstance,Boolean>>();
		instancesBySignalMatch = new TreeMap<OWLNamedIndividual,TreeMap<DataInstance,Boolean>>();
		eventList = new TreeMap<EventOccurrence,Boolean>();
	}
	
	/**
	 * Return the number of instances we have successfully read in:
	 * @throws KIDSUnEvaluableSignalException 
	 */
	@Override
	public int numInstances(){
		return ourInstances.size();
	}

	@Override
	/**
	 * Given a Dataset reference, generate a specific view from it - in this case,
	 * the view is the same file as the dataset (native LibPCAP).
	 * 
	 * @param eventIRI
	 * @param iSet
	 * @param evtList - The list of events given by the parent data set
	 */
	public void generateView(
			String datasetLoc,
			KIDSMeasurementOracle o,
			List<IRI> idFeatures) throws KIDSOntologyDatatypeValuesException, KIDSOntologyObjectValuesException, InstantiationException, IllegalAccessException, ClassNotFoundException, KIDSUnEvaluableSignalException{
		myGuy = o;
		identifyingFeatures = idFeatures;
		datasetLocation = datasetLoc;
		ourDetector = o.getDetectorForView(ourIRI);
			try {
				Set<DataInstance> iSet = this.getMatchingInstances(new HashSet<IRI>());
				ourInstances.addAll(iSet);
			} catch (KIDSOntologyObjectValuesException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (KIDSOntologyDatatypeValuesException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (KIDSIncompatibleSyntaxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
		/*
		File TempDest = File.createTempFile("NativeLibPcapView", "pcap");
		datasetLocation = TempDest.getAbsolutePath();
		if (! TempDest.delete()){
			throw new IOException("Could not delete temporary file " + TempDest.getAbsolutePath());
		}
		ourDetector = o.getDetectorForView(ourIRI);
		*/
		
	}

	@Override
	// Need to accommodate the filter set:
	public Iterator<DataInstance> iterator() throws IOException, KIDSUnEvaluableSignalException, KIDSOntologyObjectValuesException, KIDSOntologyDatatypeValuesException, KIDSIncompatibleSyntaxException {
		if (ourInstances == null || ourInstances.size() == 0){
			try {
				ourInstances = (HashSet<DataInstance>) getMatchingInstances(new HashSet<IRI>());
			} catch (KIDSOntologyObjectValuesException | KIDSOntologyDatatypeValuesException | KIDSIncompatibleSyntaxException e){
				throw new IOException("Could not read data instances from view " + this.datasetLocation + ": " + e);
			}
		}
		return this.getMatchingInstances(new HashSet<IRI>()).iterator();
	}

	@Override
	/**
	 * If we have a filter set, ensure that only filtered results are included.
	 */
	public Set<DataInstance> getMatchingInstances(
			Set<IRI> signalSet) throws KIDSOntologyObjectValuesException, KIDSOntologyDatatypeValuesException, IOException, KIDSIncompatibleSyntaxException, KIDSUnEvaluableSignalException {
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
	/**
	 * Only include those instances included in this view.
	 */
	public DatasetView getSubview(Set<DataInstance> members) throws KIDSOntologyDatatypeValuesException, KIDSOntologyObjectValuesException, InstantiationException, IllegalAccessException, ClassNotFoundException, KIDSUnEvaluableSignalException {
		NativeLibPCAPView ourDV = new NativeLibPCAPView();
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
	public List<IRI> getIdentifyingFeatures() {
		return identifyingFeatures;
	}

	@Override
	public String getViewLocation() {
		return this.datasetLocation;
	}

	@Override
	public void setIRI(IRI iri) {
		ourIRI = iri;
	}

	@Override
	public IRI getIRI() {
		return ourIRI;
	}

}
