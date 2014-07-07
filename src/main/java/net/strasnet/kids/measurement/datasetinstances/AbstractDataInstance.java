/**
 * 
 */
package net.strasnet.kids.measurement.datasetinstances;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.semanticweb.owlapi.model.IRI;

import net.strasnet.kids.measurement.DataInstance;
import net.strasnet.kids.measurement.Label;

/**
 * @author cstras
 *
 */
public class AbstractDataInstance implements DataInstance {
	protected Label myLabel = null;
	protected List<IRI> identifiers = null;
	protected String myID;
	protected Map<IRI,String> myResources = null;
	
	public AbstractDataInstance(HashMap<IRI, String> idValues){
		this.setID(idValues);
		myResources = new HashMap<IRI,String>();
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
	public void setID(HashMap<IRI, String> idValues) {
		StringBuilder idbuild = new StringBuilder();
		for (IRI fVal : idValues.keySet()){
			idbuild.append(idValues.get(fVal));
		}
		if (myID != null){
			System.err.println("Previous ID was: " + myID + " now " + idbuild.toString());
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
	public void addResources(Map<IRI, String> extractResources) {
		if (this.myResources == null){
			this.myResources = new HashMap<IRI,String>();
		}
		for (IRI k : extractResources.keySet()){
			this.myResources.put(k, extractResources.get(k));
		}
	}
	

}
