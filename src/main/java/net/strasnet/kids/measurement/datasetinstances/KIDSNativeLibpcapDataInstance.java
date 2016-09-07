package net.strasnet.kids.measurement.datasetinstances;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import net.strasnet.kids.detectors.UnimplementedIdentifyingFeatureException;
import net.strasnet.kids.measurement.DataInstance;
import net.strasnet.kids.measurement.Label;
import net.strasnet.kids.measurement.test.KIDSSignalSelectionInterface;

import org.apache.logging.log4j.LogManager;
import org.semanticweb.owlapi.model.IRI;

public class KIDSNativeLibpcapDataInstance extends AbstractDataInstance implements DataInstance {
	
	static List<IRI> myIDs = new LinkedList<IRI>();
	
	static {
		myIDs.add(IRI.create(featureIRI + "PacketID"));
		//myIDs.add(IRI.create(featureIRI + "instanceTimestamp"));
		//TODO: Why is the above commented out?
		myIDs.add(IRI.create(featureIRI + "IPv4SourceAddressSignalDomain"));
		myIDs.add(IRI.create(featureIRI + "IPv4DestinationAddressSignalDomain"));
		myIDs.add(IRI.create(featureIRI + "ObservationOrder"));
	};

	public static final org.apache.logging.log4j.Logger logme = LogManager.getLogger(KIDSSignalSelectionInterface.class.getName());
	
	public KIDSNativeLibpcapDataInstance (HashMap<IRI, String> resourceValues) throws UnimplementedIdentifyingFeatureException{
		super(resourceValues, KIDSNativeLibpcapDataInstance.myIDs);
	}
	
	public boolean equals(Object o){
		return super.equals(o);
	}
	
	@Override
	public Label getLabel() {
		if (myLabel != null){
		    logme.debug(String.format("Returning %s label for instance %s",this.myLabel.isEvent(), this.myID));
		}
		return myLabel;
	}

	public static void main(String[] args) throws UnimplementedIdentifyingFeatureException{
		// Test use in hashmap:
		HashSet<KIDSNativeLibpcapDataInstance>m = new HashSet<KIDSNativeLibpcapDataInstance>();
		HashMap<IRI, String> h1 = new HashMap<IRI, String>();
		HashMap<IRI, String> h2 = new HashMap<IRI, String>();
		h1.put(IRI.create("http://www.semantiknit.com/#f1"),"1");
		h2.put(IRI.create("http://www.semantiknit.com/#f1"),"1");
		KIDSNativeLibpcapDataInstance di1 = new KIDSNativeLibpcapDataInstance(h1);
		KIDSNativeLibpcapDataInstance di2 = new KIDSNativeLibpcapDataInstance(h2);
		
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
