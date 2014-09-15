/**
 * 
 */
package net.strasnet.kids.measurement.datasetinstances;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.semanticweb.owlapi.model.IRI;

import net.strasnet.kids.detectors.UnimplementedIdentifyingFeatureException;
import net.strasnet.kids.measurement.DataInstance;
import net.strasnet.kids.measurement.Label;

/**
 * @author cstras
 * 
 * TODO: Implement equals, comparison, and hash value methods here
 *
 */
public class AbstractDataInstance implements DataInstance {
	protected static String featureIRI = "http://solomon.cs.iastate.edu/ontologies/KIDS.owl#";
	protected Label myLabel = null;
	private List<IRI> identifiers = new LinkedList<IRI>();
	protected String myID;
	protected Map<IRI,String> myResources = null;
	
	public AbstractDataInstance(Map<IRI, String> rMap, List<IRI> myIDs) throws UnimplementedIdentifyingFeatureException{
		identifiers = myIDs;
		myResources = rMap;
		this.setID();
	}

	/* (non-Javadoc)
	 * @see net.strasnet.kids.measurement.DataInstance#getLabel()
	 */
	@Override
	public Label getLabel() {
		return myLabel;
	}

	/* (non-Javadoc)
	 * @see net.strasnet.kids.measurement.DataInstance#setLabel(net.strasnet.kids.measurement.Label)
	 */
	@Override
	public void setLabel(Label label) {
		myLabel = label;
	}

	/* (non-Javadoc)
	 * @see net.strasnet.kids.measurement.DataInstance#setID(java.util.HashMap)
	 */
	@Override
	public void setID() throws UnimplementedIdentifyingFeatureException {
		StringBuilder idbuild = new StringBuilder();
		for (IRI fVal : this.identifiers){
			if(! this.myResources.containsKey(fVal)){
				throw new UnimplementedIdentifyingFeatureException("Missing identifying feature " + fVal);
			}
			idbuild.append(fVal + "=" + this.myResources.get(fVal));
		}
		if (myID != null){
			System.err.println("[W] Previous ID was: " + myID + " now " + idbuild.toString());
		}
		myID = idbuild.toString();
	}

	/* (non-Javadoc)
	 * @see net.strasnet.kids.measurement.DataInstance#getID()
	 */
	@Override
	public String getID() {
		return myID;
	}

	/* (non-Javadoc)
	 * @see net.strasnet.kids.measurement.DataInstance#getResources()
	 */
	@Override
	public Map<IRI, String> getResources() {
		return myResources;
	}

	@Override
	/**
	 * 
	 */
	public int hashCode(){
		return this.getID().hashCode();
//		return Integer.parseInt(this.myID);
	}
	
	@Override
	/**
	 * TODO: Ensure that none of the features used here are identifying features.
	 */
	public void addResources(Map<IRI, String> extractResources) {
		if (this.myResources == null){
			this.myResources = new HashMap<IRI,String>();
		}
		//TODO: Add a safety check here to ensure that no identifying resources are overwritten
		for (IRI k : extractResources.keySet()){
			if (this.identifiers.contains(k)){
				throw new UnsupportedOperationException("Cannot modify existing identifier " + k);
			}
			this.myResources.put(k, extractResources.get(k));
		}
	}
	
	@Override
	public boolean equals(Object obj){
		if (!(obj instanceof DataInstance)){
			return false;
		}
		if (obj == this){
			return true;
		}
		
		DataInstance rhs = (DataInstance)obj;
		//System.err.println("Comparing " + this.getID() + " == " + rhs.getID() + ": " + this.getID().equals(rhs.getID()));
		return this.getID().equals(rhs.getID());
	}

}
