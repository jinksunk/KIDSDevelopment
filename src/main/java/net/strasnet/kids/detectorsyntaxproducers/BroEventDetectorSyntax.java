package net.strasnet.kids.detectorsyntaxproducers;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;

import net.strasnet.kids.KIDSOntologyDatatypeValuesException;
import net.strasnet.kids.KIDSOntologyObjectValuesException;
import net.strasnet.kids.bro.BroValueParserFactory;
import net.strasnet.kids.bro.BroValueParserInterface;
import net.strasnet.kids.measurement.KIDSMeasurementOracle;
import net.strasnet.kids.measurement.KIDSUnEvaluableSignalException;
import net.strasnet.kids.measurement.test.TestOracleFactory;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.util.SimpleIRIMapper;

/**
 * 
 * @author cstras
 * Produce code like:
<pre>
@load kids/broIDSMethods

export {
    const stddev_threshold: double = 2 &redef;
}

event kids_constraint_satisfied (c: connection , l: vector of string , msg: string ) 
{
    local d = "";
    local s: int ;
    local b = "" ;
    for (s in l)
    {
        b = b + fmt("%s%s",d,l[s]);
        d = ",";
    }
    print b ;
}
</pre>

 * Notes and assumptions:
 * 1. The domain / constraint must already be available as overridable events in broIDSMethods (or whatever module is used).
 * 2. The domain / constraint / context specify the bro module to load and the event to override.
 */

public class BroEventDetectorSyntax implements KIDSDetectorSyntax {
	
	private KIDSMeasurementOracle o = null;
	
	/* Map of domain, constraint, and context to Bro module, event, and value parser */
	static Map<Map<String,IRI>, Map<String,String>> broModuleSelector = new HashMap<Map<String,IRI>, Map<String,String>>();
	
	/* Map Keys */
	static String SignalDomain = "SignalDomain";
	static String SignalConstraint = "SignalConstraint";
	static String SignalContext = "SignalContext";
	
	static String BroModule = "Module";
	static String BroEvent = "Event";
	static String BroValueParser = "ValueParser";
	
	/* Initialize static map */
	static {
		final Map<String,IRI> kMap = new HashMap<String,IRI>();
		kMap.put(SignalDomain, IRI.create(KIDSMeasurementOracle.TBOXPrefix + "#TCPPayloadSizeDomain"));
		kMap.put(SignalConstraint, IRI.create(KIDSMeasurementOracle.TBOXPrefix + "#STDDEVThresholdExceededConstraint"));
		kMap.put(SignalContext, IRI.create(KIDSMeasurementOracle.TBOXPrefix + "#TCPPacket"));
		
		Map<String,String> vMap = new HashMap<String,String>();
		vMap.put(BroModule,"kids/broIDSMethods");
		vMap.put(BroEvent,"kids_constraint_satisfied");
		vMap.put(BroValueParser,"net.strasnet.kids.bro.BroDoubleValueParser");

		broModuleSelector.put(kMap, vMap);

		vMap = new HashMap<String,String>();
		vMap.put(BroModule,"kids/kidsBroNullAllPackets");
		vMap.put(BroEvent,"all_ip_packets");
		vMap.put(BroValueParser,"net.strasnet.kids.bro.BroNullValueParser");

		broModuleSelector.put(null, vMap);
		
	}

	@Override
	/**
	 * The signal provides a constraint and domain, and the domain provides the context.  The value should be provided as input to 
	 * the bro method.
	 */
	public String getDetectorSyntax(Set<IRI> sigSet)
			throws KIDSIncompatibleSyntaxException,
			KIDSOntologyObjectValuesException,
			KIDSOntologyDatatypeValuesException, KIDSUnEvaluableSignalException {
		/*
		 * For each signal in the set, obtain the domain, constraint, and set of domain contexts, and value. 
		 */
		Map<String,IRI> kMap = new HashMap<String,IRI>();
		Map<String,String> vMap = null;
		String sigValue = null;
		for (IRI mySig : sigSet){
			if (mySig == null){
				// In this case, load the null module:
				if (broModuleSelector.containsKey(null)){
					vMap = broModuleSelector.get(null);
					}
			} else {
				IRI sigDomain = o.getSignalDomain(mySig);
				sigValue = o.getSignalValue(mySig);
				IRI sigConstraint = o.getSignalConstraint(mySig);
				Set<IRI> contexts = o.getSignalContexts(mySig);

				/* When we find the first match, stop */
				for (IRI myContext : contexts){
					kMap.put(BroEventDetectorSyntax.SignalDomain, sigDomain);
					kMap.put(BroEventDetectorSyntax.SignalContext, myContext);
					kMap.put(BroEventDetectorSyntax.SignalConstraint, sigConstraint);
				
					if (broModuleSelector.containsKey(kMap)){
						vMap = broModuleSelector.get(kMap);
						continue;
					}
				}
				if (vMap == null){
					throw new KIDSIncompatibleSyntaxException(String.format(
						"Cannot match bro syntax function to domain %s and constraint %s.",
						sigDomain.getFragment(),
						sigConstraint.getFragment()));
				}
			}
		}
		
		BroValueParserInterface bpi = null;
		/*
		 * Determine which Bro module to load, and event to override, based on the domain and constraint.
		 */
		bpi = BroValueParserFactory.getInterfaceImplementation(vMap.get(BroValueParser));
		/*
		 * Parse the signal according to the selected module and event
		 */
		Map<String,String> sigValues = bpi.getParsedValues(sigValue);

		
		StringBuilder broSynString = new StringBuilder();
		broSynString.append(String.format("@load %s\n", vMap.get(BroEventDetectorSyntax.BroModule)));
		if (sigValues != null){
			/*
		 	* Build the syntax string
		 	*/
			broSynString.append(String.format("export {" +
				"const stddev_threshold: double = %s &redef;\n}\n" +
				"event kids_constraint_satisfied (c: connection , l: vector of string , msg: string ) \n" +
				"{\n" +
				"local d = \"\";\n" +
				"local s: int ;\n" +
				"local b = \"\" ;\n" +
				"for (s in l)\n", sigValues.get("Value1")) +
				"{\n" +
				"b = b + fmt(\"%s%s\",d,l[s]);\n" +
				"d = \",\";\n" +
    			"}\n" +
    			"print b ; \n" +
				"}\n");
		} else {
			/* In this case, we need a bro file which will return all packets */
			broSynString.append("event kids_constraint_satisfied (c: connection , l: vector of string , msg: string ) \n" +
				"{\n" +
				"local d = \"\";\n" +
				"local s: int ;\n" +
				"local b = \"\" ;\n" +
				"for (s in l)\n" +
				"{\n" +
				"b = b + fmt(\"%s%s\",d,l[s]);\n" +
				"d = \",\";\n" +
    			"}\n" +
    			"print b ; \n" +
				"}\n");
		}
		
		// Write the script to a file, and return the file name:
		File tmp;
		try {
			tmp = File.createTempFile("KIDS-", ".bro");
			//tmp.deleteOnExit();
			org.apache.commons.io.FileUtils.writeStringToFile(tmp, broSynString.toString(), Charset.forName("UTF-8"));
			return tmp.getAbsolutePath();
		} catch (IOException e) {
			System.err.println("Could not create file:");
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public void init(KIDSMeasurementOracle o) {
		this.o = o;
	}

	/**
	 * @param args -- Test the module
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		String ABOXLocation = args[0];
		String ABOXIRI = "http://www.semantiknit.com/ontologies/2014/03/29/CodeRedExperiment3.owl";
		String TBOXLocation = args[1];
		String TBOXIRI = "http://www.semantiknit.com/";
		KIDSMeasurementOracle localO = TestOracleFactory.getKIDSMeasurementOracle(TBOXIRI, TBOXLocation, ABOXIRI, ABOXLocation);

		// Test the bro syntax producer
		BroEventDetectorSyntax beds = new BroEventDetectorSyntax();
		beds.init(localO);
		Set<IRI> sigSet = new HashSet<IRI>();
		sigSet.add(IRI.create("http://www.semantiknit.com/ontologies/2014/03/29/CodeRedExperiment3.owl#TCPPacketLengthExceeds3STDEVsSignal"));
		String synString = beds.getDetectorSyntax(sigSet);
		System.out.println(synString);
	}

}
