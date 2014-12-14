package net.strasnet.kids.measurement.datasetinstances;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLNamedIndividual;

import net.strasnet.kids.KIDSOntologyDatatypeValuesException;
import net.strasnet.kids.KIDSOntologyObjectValuesException;
import net.strasnet.kids.detectors.UnimplementedIdentifyingFeatureException;
import net.strasnet.kids.measurement.DataInstance;
import net.strasnet.kids.measurement.KIDSMeasurementIncompatibleContextException;
import net.strasnet.kids.measurement.KIDSMeasurementInstanceUnsupportedFeatureException;
import net.strasnet.kids.measurement.Label;
import net.strasnet.kids.signalRepresentations.KIDSRepresentationInvalidRepresentationValueException;

/**
 * @author cstras
 * 
 * The class of data instance indicates what features are used to identify unique 
 * data instances.  Multiple views may use a single data instance type, however
 * a view may only use a single data instance type; the data instance used implies
 * the features that the view considers to be identifying.
 * 
 * This relates to the identifying features used by the dataset label classes - those
 * classes indicate the features which the label function uses to differentiate malicious 
 * from benign data instances.  A label is free to use a subset of data instance identifying
 * features, however this may cause multiple actual data instance to match a single label
 * instance.
 * 
 * A label function cannot be used to evaluate a data instance which does not include all
 * of the label function's identifying features.
 *
 */
public class KIDSBroDataInstance extends AbstractDataInstance implements DataInstance {
	
	static List<IRI> myIDs = new LinkedList<IRI>();
	
	static {
		myIDs.add(IRI.create(featureIRI + "PacketID"));
		//myIDs.add(IRI.create(featureIRI + "instanceTimestamp"));
		myIDs.add(IRI.create(featureIRI + "IPv4SourceAddressSignalDomain"));
		myIDs.add(IRI.create(featureIRI + "IPv4DestinationAddressSignalDomain"));
		myIDs.add(IRI.create(featureIRI + "ObservationOrder"));
	};
	
	public KIDSBroDataInstance (HashMap<IRI, String> resourceValues) throws UnimplementedIdentifyingFeatureException{
		super(resourceValues, KIDSBroDataInstance.myIDs);
	}
	
	public boolean equals(Object o){
		return super.equals(o);
	}

	public static void main(String[] args) throws UnimplementedIdentifyingFeatureException{
		// Test use in hashmap:
		HashSet<KIDSBroDataInstance> m = new HashSet<KIDSBroDataInstance>();
		HashMap<IRI, String> h1 = new HashMap<IRI, String>();
		HashMap<IRI, String> h2 = new HashMap<IRI, String>();
		h1.put(IRI.create("http://www.semantiknit.com/#f1"),"1");
		h2.put(IRI.create("http://www.semantiknit.com/#f1"),"1");
		KIDSBroDataInstance di1 = new KIDSBroDataInstance(h1);
		KIDSBroDataInstance di2 = new KIDSBroDataInstance(h2);
		
		if (di1.equals(di2)){
			System.out.println("Instances are equal.");
		} else {
			System.out.println("Instances are not equal.");
		}
		
		m.add(di1);
		
		if (m.contains(di2)){
			System.out.println("Instances are recognized.");
		} else {
			System.out.println("Instances are not recognized.");
		}
		
	}
	
}
