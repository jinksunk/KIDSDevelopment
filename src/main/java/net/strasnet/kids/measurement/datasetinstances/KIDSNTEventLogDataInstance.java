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
 */
public class KIDSNTEventLogDataInstance extends AbstractDataInstance {
	
	static List<IRI> myIDs = new LinkedList<IRI>();

	static {
		myIDs.add(IRI.create(featureIRI + "NTEventLogRecordID"));
	};
	
	public KIDSNTEventLogDataInstance(Map<IRI, String> rMap) throws UnimplementedIdentifyingFeatureException{
		super(rMap, myIDs);
	}

	public boolean equals(Object o){
		return super.equals(o);
	}
}
