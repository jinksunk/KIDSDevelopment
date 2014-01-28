package net.strasnet.kids.detectorsyntaxproducers;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import net.strasnet.kids.KIDSOntologyDatatypeValuesException;
import net.strasnet.kids.KIDSOntologyObjectValuesException;
import net.strasnet.kids.gui.KIDSAddEventOracle;
import net.strasnet.kids.measurement.KIDSMeasurementOracle;
import net.strasnet.kids.measurement.KIDSUnEvaluableSignalException;
import net.strasnet.kids.snort.KIDSSnortRuleGenerator;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.util.SimpleIRIMapper;

public class KIDSSnortDetectorSyntax implements KIDSDetectorSyntax {
	/**
	 * This class represents the valid syntax expressions available in the Snort rules
	 * files.
	 * @author chrisstrasburg
	 *
	 */
	
	KIDSSnortRuleGenerator sGen  = new KIDSSnortRuleGenerator();

	@Override
	public void init(KIDSMeasurementOracle o) {
		KIDSAddEventOracle k = new KIDSAddEventOracle();
		try {
			k.loadKIDS(o.getOurIRI(), o.getIRIMapperList());
			sGen.setOracle(k);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@Override
	public String getDetectorSyntax(Set<IRI> sigSet)
			throws KIDSIncompatibleSyntaxException,
			KIDSOntologyObjectValuesException,
			KIDSOntologyDatatypeValuesException, KIDSUnEvaluableSignalException {
		sGen.setCurrentSignalSet(sigSet);
		// Write to a temporary rule file, then return the file location:
		try {
			File rFile = File.createTempFile("snort", ".rule");
			BufferedWriter bw = new BufferedWriter(new FileWriter(rFile));
			String ourRule = sGen.getSyntacticForm();
			if (ourRule.isEmpty()){
				StringBuilder sb = new StringBuilder();
				for (IRI ks : sigSet){
					sb.append(ks.toString());
				}
				throw new KIDSUnEvaluableSignalException("Could not get Snort Syntax for signal set {" + sb.toString() + "}");
			}
			bw.write(ourRule);
			bw.close();
			return rFile.getAbsolutePath();
		} catch (IOException e) {
			// Cannot create the temporary file for some reason.  What should we do?
			e.printStackTrace();
			return null;
		} 
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// Test this puppy :)
		String testABOX = "http://www.semantiknit.com/ontologies/2013/9/15/TestEventExperiment2.owl";
		String testABOXFile = "file:///Users/chrisstrasburg/Documents/academic-research/papers/2013-MeasurementPaper/experiments/TestEvent-Test2-Snort/TestEventExperiment2.owl";
		String testTBOX = "http://solomon.cs.iastate.edu/ontologies/KIDS.owl";
		String testTBOXFile = "http://solomon.cs.iastate.edu/ontologies/KIDS.owl";
		//String testTBOXFile = "file:///Users/chrisstrasburg/Documents/academic-research/papers/2013-MeasurementPaper/experiments/TestEvent-Test1/kids.owl";
		
		
		// Initialize the syntax generator and print out the form:
		KIDSMeasurementOracle kmo = new KIDSMeasurementOracle();
		
        List<SimpleIRIMapper> m = new LinkedList<SimpleIRIMapper>();
        m.add(new SimpleIRIMapper(IRI.create(testABOX), IRI.create(testABOXFile)));
        m.add(new SimpleIRIMapper(IRI.create(testTBOX), IRI.create(testTBOXFile)));
		try {
			kmo.loadKIDS(IRI.create(testABOX), m);
			
			KIDSSnortDetectorSyntax ksds = new KIDSSnortDetectorSyntax();
			ksds.init(kmo);
			
			Set<IRI> testSigSet = new HashSet<IRI>();
			testSigSet.add(IRI.create(testABOX + "#TCPFINFlagSet"));
			testSigSet.add(IRI.create(testABOX + "#TCPPacketSizeSignal200"));
			testSigSet.add(IRI.create(testABOX + "#tcpProtocolSignal"));
			
			System.out.println("Returned: " + ksds.getDetectorSyntax(testSigSet));
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
