/**
 * 
 */
package net.strasnet.kids.detectors;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import net.strasnet.kids.KIDSOntologyDatatypeValuesException;
import net.strasnet.kids.KIDSOntologyObjectValuesException;
import net.strasnet.kids.detectorsyntaxproducers.BroEventDetectorSyntax;
import net.strasnet.kids.detectorsyntaxproducers.KIDSDetectorSyntax;
import net.strasnet.kids.detectorsyntaxproducers.KIDSIncompatibleSyntaxException;
import net.strasnet.kids.measurement.KIDSMeasurementOracle;
import net.strasnet.kids.measurement.KIDSUnEvaluableSignalException;
import net.strasnet.kids.measurement.ViewLabelDataset;
import net.strasnet.kids.measurement.datasetviews.DatasetView;
import net.strasnet.kids.measurement.datasetviews.NativeLibPCAPView;
import net.strasnet.kids.measurement.test.TestOracleFactory;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLNamedIndividual;

/**
 * @author cstras
 *
 */
public class KIDSBroDetector implements KIDSDetector {
	/**
	 * Represents a detector utilizing the Bro command-line tool.  Associated with the bro script syntax 
	 */
	
	private IRI ourIRI = null;
	protected static String featureIRI = "http://solomon.cs.iastate.edu/ontologies/KIDS.owl#";
	private static String executionCommand;
	
	
	/**
	 * Bro rule output sample from this detector:
	 */
	//private static String regexPattern = "[\\d/-:\\.]+\\s+\\[\\*\\*\\].*\\s+ID:(?<PID>\\d+)\\s.*";
//	private static String regexPattern = "(?<TIMESTAMP>[\\d\\/\\-:\\.]+)\\s+\\[\\*\\*\\]\\s+.*(<?SIP>\\d+\\.\\d+\\.\\d+\\.\\d+)[^-]+\\s->\\s(<?DIP>\\d+\\.\\d+\\.\\d+\\.\\d+).*ID:(<?ID>\\d+)\\s+";
	private static String regexPattern = "(?<TIMESTAMP>[\\d\\/\\-:\\.]+)\\s+\\[\\*\\*\\].*\\s+(?<SIP>\\d+\\.\\d+\\.\\d+\\.\\d+)[^-]+\\s->\\s(?<DIP>\\d+\\.\\d+\\.\\d+\\.\\d+).*ID:(?<ID>\\d+)\\s+";
	private static String recordPattern = "(=\\+){37}";
	
	private static final Map<String, Boolean> supportedFeatures = new HashMap <String, Boolean>();
	static {
		supportedFeatures.put(featureIRI + "TCPPayloadSizeDomain",true);
		};
		
	private Pattern rexp = null;
	private Pattern recRexp = null;
	private KIDSMeasurementOracle myGuy = null;
	private KIDSDetectorSyntax ourSyn = null; 
	//private Path tmpDir;
	

	/* (non-Javadoc)
	 * @see net.strasnet.kids.detectors.KIDSDetector#getMatchingInstances(java.util.Set, net.strasnet.kids.measurement.datasetviews.DatasetView)
	 */
	@Override
	public Set<Map<IRI, String>> getMatchingInstances(Set<IRI> signals,
			DatasetView v) throws IOException,
			KIDSOntologyObjectValuesException,
			KIDSOntologyDatatypeValuesException,
			KIDSIncompatibleSyntaxException, KIDSUnEvaluableSignalException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see net.strasnet.kids.detectors.KIDSDetector#init(java.lang.String, org.semanticweb.owlapi.model.IRI, net.strasnet.kids.measurement.KIDSMeasurementOracle)
	 */
	@Override
	public void init(String toExecute, IRI detectorIRI, KIDSMeasurementOracle o)
			throws KIDSOntologyObjectValuesException,
			KIDSOntologyDatatypeValuesException, InstantiationException,
			IllegalAccessException, ClassNotFoundException {
		// TODO Auto-generated method stub
	}

	/* (non-Javadoc)
	 * @see net.strasnet.kids.detectors.KIDSDetector#getIRI()
	 */
	@Override
	public IRI getIRI() {
		return null;
	}

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		// Test driver here
		String ABOXLocation = args[0];
		String ABOXIRI = "http://www.semantiknit.com/ontologies/2014/03/29/CodeRedExperiment3.owl";
		String TBOXLocation = args[1];
		String TBOXIRI = "http://solomon.cs.iastate.edu/ontologies/KIDS.owl";
		IRI DetectorIRI = IRI.create(args[2]);
		IRI TestSignalIRI = IRI.create(args[3]);
		IRI EventIRI = IRI.create(args[4]);

		KIDSMeasurementOracle localO = TestOracleFactory.getKIDSMeasurementOracle(TBOXIRI, TBOXLocation, ABOXIRI, ABOXLocation);

		// Test the bro syntax producer
		//KIDSBroDetector beds = new KIDSBroDetector();
		//beds.init(localO.getDetectorExecutionString(DetectorIRI), DetectorIRI, localO);
		Set<IRI> sigSet = new HashSet<IRI>();
		sigSet.add(TestSignalIRI);
		
		Set<OWLNamedIndividual> dSets = localO.getDatasetsForEvent(EventIRI);
		OWLNamedIndividual dSet = dSets.iterator().next();

		List<OWLNamedIndividual> vSets = localO.getAvailableViews(dSet.getIRI(), EventIRI);
		NativeLibPCAPView nlpv = new NativeLibPCAPView();
		nlpv.setIRI(vSets.get(0).getIRI());
		localO.getLabelForViewAndEvent(vSets.get(0), EventIRI);
		List<IRI> lList = new LinkedList<IRI>();
		lList.add(localO.getLabelForViewAndEvent(vSets.get(0), EventIRI).getIRI());
		nlpv.generateView(localO.getDatasetLocation(dSet), localO, lList);
		
		//ViewLabelDataset v = new ViewLabelDataset();
		//v.init(IRI.create(EventIRI));

		nlpv.getMatchingInstances(sigSet);

	}

}
