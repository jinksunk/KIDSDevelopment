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
public class KIDSNTEventLogDataInstance extends AbstractDataInstance {
	
	public KIDSNTEventLogDataInstance(HashMap<IRI, String> idValues){
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

}
