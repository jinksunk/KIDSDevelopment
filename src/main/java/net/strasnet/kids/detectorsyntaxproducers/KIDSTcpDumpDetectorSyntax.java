package net.strasnet.kids.detectorsyntaxproducers;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import net.strasnet.kids.KIDSOntologyDatatypeValuesException;
import net.strasnet.kids.KIDSOntologyObjectValuesException;
import net.strasnet.kids.measurement.KIDSMeasurementOracle;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;

public class KIDSTcpDumpDetectorSyntax implements KIDSDetectorSyntax {
	
	public enum TcpDumpSyntaxComponent {
		PACKETID_EQUAL ("ip[4:2] = %s"),
		TCPFLAGS_BITMASK_SET ("tcp[13] & %s != 0"),
		PACKETSIZE_GREATERTHAN ("ip[2:2] > %s");
		
		private String template = null;
		
		private static HashMap<String,Integer> flagMap = new HashMap<String, Integer>();
		static {
			flagMap.put("F", 1);
			flagMap.put("S", 2);
			flagMap.put("R", 4);
			flagMap.put("P", 8);
			flagMap.put("A", 16);
			flagMap.put("U", 32);
		}
		
		private TcpDumpSyntaxComponent (String sFormTemplate){
			this.template = sFormTemplate;
		}
		
		public String syntaxForm (String sVal){
			return String.format(template, sVal);
		}
	}
	
	// Map of constraint feature -> type -> syntax
	public static final Map <IRI, Map<IRI, TcpDumpSyntaxComponent>> validSignalClassMap = new HashMap<IRI, Map<IRI, TcpDumpSyntaxComponent>>();
	private static String sigClassIRI = "http://solomon.cs.iastate.edu/ontologies/KIDS.owl#";
	
	static {
		HashMap<IRI, TcpDumpSyntaxComponent> tempMap = new HashMap<IRI, TcpDumpSyntaxComponent>();
		
		// IPPacketID Feature:
		tempMap.put(IRI.create(sigClassIRI + "IntegerEquality"), TcpDumpSyntaxComponent.PACKETID_EQUAL);
		validSignalClassMap.put(IRI.create(sigClassIRI + "IPPacketID"), tempMap);
		
		// Size Feature:
		tempMap = new HashMap<IRI, TcpDumpSyntaxComponent>();
		tempMap.put(IRI.create(sigClassIRI + "integerGreaterThan"), TcpDumpSyntaxComponent.PACKETSIZE_GREATERTHAN);
		validSignalClassMap.put(IRI.create(sigClassIRI + "IPPacketTotalLength"), tempMap);
		
		// TCPFlags Feature:
		tempMap = new HashMap<IRI, TcpDumpSyntaxComponent>();
		tempMap.put(IRI.create(sigClassIRI + "bitmaskMatchNonZero"), TcpDumpSyntaxComponent.TCPFLAGS_BITMASK_SET);
		validSignalClassMap.put(IRI.create(sigClassIRI + "TCPFlags"), tempMap);
		
	};
	
	private KIDSMeasurementOracle myGuy = null;
	
	public KIDSTcpDumpDetectorSyntax(){
		
	}
	
	@Override
	public void init(KIDSMeasurementOracle o){
		myGuy = o;
	}

	@Override
	public String getDetectorSyntax(Set<IRI> signalSet)
			throws KIDSIncompatibleSyntaxException, KIDSOntologyObjectValuesException, KIDSOntologyDatatypeValuesException {
		// For each signal in the set:
		StringBuilder sb = new StringBuilder();
		String tString = "";
		for (IRI mySignal : signalSet){
			// Look up the class for each signal in the set
			IRI sigDomain = myGuy.getSignalDomain(myGuy.getOwlDataFactory().getOWLNamedIndividual(mySignal)).getIRI();
			IRI sigConstraint = myGuy.getSignalConstraint(myGuy.getOwlDataFactory().getOWLNamedIndividual(mySignal)).getIRI();
			
			// Class gives us feature and constraint, now get the value of the signal
			String sVal = myGuy.getSignalValue(myGuy.getOwlDataFactory().getOWLNamedIndividual(mySignal));
			
			// Finally, add the syntax string for the feature / value.
			Map<IRI, TcpDumpSyntaxComponent> tempMap = validSignalClassMap.get(sigDomain);
			sb.append(tString + tempMap.get(sigConstraint).syntaxForm(sVal) + " ");
			tString = " and ";
			
		}
		
		return sb.toString();
	}
	
	protected String getSignalForm(String feature, String constraint, String sVal) throws KIDSIncompatibleSyntaxException {
		TcpDumpSyntaxComponent tdsc = null;
		if (validSignalClassMap.containsKey(feature)){ 
			Map <IRI, TcpDumpSyntaxComponent> fmap = validSignalClassMap.get(feature);
			if (fmap.containsKey(constraint)){
				return fmap.get(constraint).syntaxForm(sVal);
			}
		}
		
		throw new KIDSIncompatibleSyntaxException("Did not find a valid signal class for " + feature);
	}
	

}
