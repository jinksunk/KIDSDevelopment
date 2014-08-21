package net.strasnet.kids.measurement.test;

import java.util.LinkedList;
import java.util.List;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.util.SimpleIRIMapper;

import net.strasnet.kids.measurement.KIDSMeasurementOracle;

public class TestOracleFactory {
	public static KIDSMeasurementOracle getKIDSMeasurementOracle(String TBOXIRI, String TBOXLocation, String ABOXIRI, String ABOXLocation) throws Exception{
		KIDSMeasurementOracle localO = new KIDSMeasurementOracle();
		SimpleIRIMapper mapper = new SimpleIRIMapper( IRI.create(ABOXIRI),IRI.create(ABOXLocation));
		SimpleIRIMapper TBOXmapper = new SimpleIRIMapper( IRI.create(TBOXIRI),IRI.create(TBOXLocation));
		List<SimpleIRIMapper> mlist = new LinkedList<SimpleIRIMapper>();
		mlist.add(mapper);
		mlist.add(TBOXmapper);
		
		localO.loadKIDS(IRI.create("http://www.semantiknit.com/ontologies/2014/03/29/CodeRedExperiment3.owl"),mlist);
		
		return localO;

	}
}
