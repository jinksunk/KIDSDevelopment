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

public class KIDSNativeLibpcapDataInstance implements DataInstance {
	
	private Label myLabel = null;
	private List<IRI> identifiers = null;
	private String myID;
	private Map<IRI,String> myResources = null;
	
	public KIDSNativeLibpcapDataInstance (HashMap<IRI, String> idValues){
		this.setID(idValues);
		myResources = new HashMap<IRI,String>();
	}

	@Override
	public Label getLabel() {
		return myLabel;
	}
	
	@Override
	public void setLabel(Label label) {
		myLabel = label;
	}

	/**
	 * 
	 */
	@Override
	public void setID(HashMap<IRI, String> idValues) {
		StringBuilder idbuild = new StringBuilder();
		for (IRI fVal : idValues.keySet()){
			idbuild.append(idValues.get(fVal));
		}
		myID = idbuild.toString();
	}
	
	@Override
	/**
	 * 
	 * @return The unique ID components by which this instance can be identified.
	 */
	public String getID() {
		return myID;
	}
	
	@Override
	/**
	 * 
	 */
	public int hashCode(){
		return Integer.parseInt(this.myID);
	}
	
	@Override
	public Map<IRI,String> getResources(){
		return myResources;
	}
	
	@Override
	/**
	 * 
	 */
	public boolean equals(Object o){
		if (o == null){
			return false;
		}
		return ((KIDSNativeLibpcapDataInstance)o).getID().equals(this.getID());
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
