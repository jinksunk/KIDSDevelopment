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

public class KIDSSnortDataInstance extends AbstractDataInstance implements DataInstance {
	
	static List<IRI> myIDs = new LinkedList<IRI>();

	static {
		myIDs.add(IRI.create(featureIRI + "PacketID"));
		//identifyingFeatures.add(IRI.create(featureIRI + "instanceTimestamp"));
		myIDs.add(IRI.create(featureIRI + "IPv4SourceAddressSignalDomain"));
		myIDs.add(IRI.create(featureIRI + "IPv4DestinationAddressSignalDomain"));
	};
	
	public KIDSSnortDataInstance (HashMap<IRI, String> resourceValues) throws UnimplementedIdentifyingFeatureException{
		super(resourceValues, myIDs);
	}

	public boolean equals(Object o){
		return super.equals(o);
	}
	
	public static void main(String[] args) throws UnimplementedIdentifyingFeatureException{
		// Test use in hashmap:
		HashSet<KIDSSnortDataInstance>m = new HashSet<KIDSSnortDataInstance>();
		HashMap<IRI, String> h1 = new HashMap<IRI, String>();
		HashMap<IRI, String> h2 = new HashMap<IRI, String>();
		h1.put(IRI.create("http://www.semantiknit.com/#f1"),"1");
		h2.put(IRI.create("http://www.semantiknit.com/#f1"),"1");
		KIDSSnortDataInstance di1 = new KIDSSnortDataInstance(h1);
		KIDSSnortDataInstance di2 = new KIDSSnortDataInstance(h2);
		
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
