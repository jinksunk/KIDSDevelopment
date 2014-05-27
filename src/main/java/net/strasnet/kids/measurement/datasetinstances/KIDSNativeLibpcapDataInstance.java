package net.strasnet.kids.measurement.datasetinstances;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLNamedIndividual;

import net.strasnet.kids.KIDSOntologyDatatypeValuesException;
import net.strasnet.kids.KIDSOntologyObjectValuesException;
import net.strasnet.kids.measurement.DataInstance;
import net.strasnet.kids.measurement.KIDSMeasurementIncompatibleContextException;
import net.strasnet.kids.measurement.KIDSMeasurementInstanceUnsupportedFeatureException;
import net.strasnet.kids.measurement.Label;
import net.strasnet.kids.signalRepresentations.KIDSRepresentationInvalidRepresentationValueException;

public class KIDSNativeLibpcapDataInstance extends AbstractDataInstance implements DataInstance {
	
	public KIDSNativeLibpcapDataInstance (HashMap<IRI, String> idValues){
		super(idValues);
	}

	@Override
	/**
	 * 
	 */
	public boolean equals(Object o){
		if (o == null){
			return false;
		}
		return ((DataInstance)o).getID().equals(this.getID());
	}
	
	public static void main(String[] args){
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
