package net.strasnet.kids.detectorsyntaxproducers;

import java.util.HashMap;
import java.net.InetAddress;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.strasnet.kids.KIDSOntologyDatatypeValuesException;
import net.strasnet.kids.KIDSOntologyObjectValuesException;
import net.strasnet.kids.constraint.IntegerRange;
import net.strasnet.kids.constraint.IntegerRangeSet;
import net.strasnet.kids.lib.IPv4Address;
import net.strasnet.kids.measurement.KIDSMeasurementOracle;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;

public class KIDSTcpDumpDetectorSyntax implements KIDSDetectorSyntax {
	
	public interface TcpDumpSyntaxComponent {
		public String syntaxForm (String sVal);
	}

	public enum FormatStringTcpDumpSyntaxComponent implements TcpDumpSyntaxComponent {
		PACKETID_EQUAL ("ip[4:2] = %s"),
		TCPFLAGS_BITMASK_SET ("tcp[13] & %s != 0"),
		PACKETSIZE_GREATERTHAN ("ip[2:2] > %s");
		
		private String template = null;
		
		private static HashMap<String,Integer> flagMap = new HashMap<String, Integer>();
		static {
			flagMap.put("0x01", 1);  // FIN
			flagMap.put("0x02", 2);  // SIN
			flagMap.put("0x04", 4);  // RST
			flagMap.put("0x08", 8);  // PSH
			flagMap.put("0x10", 16); // ACK
			flagMap.put("0x20", 32); // URG
		}
		
		private FormatStringTcpDumpSyntaxComponent (String sFormTemplate){
			this.template = sFormTemplate;
		}
		
		public String syntaxForm (String sVal){
			return String.format(template, sVal);
		}
	}
	
	public enum IPRangeSetTcpDumpSyntaxComponent implements TcpDumpSyntaxComponent {
		SOURCE_IP_ADDRESS_RANGESET ("src"),
		DEST_IP_ADDRESS_RANGESET ("dst");
		
		private String template = null;
		private Pattern p = Pattern.compile("\\[(?<IPRanges>(([\\d\\.\\/]+),)*([\\d\\.\\/]+))\\]");

		private IPRangeSetTcpDumpSyntaxComponent (String sFormTemplate){
			this.template = sFormTemplate;
		}
		
		public String syntaxForm (String sVal){
			// Parse out the range set thing.  The canonical form is:
			// [aaa.bbb.ccc.ddd/www.xxx.yyy.zzz,...]  So, first split on comma, then determine how many class 'a', 'b', 'c', 'd', etc... there are, and 
			// build the string from that.  It might be a little complicated...
			Matcher m = p.matcher(sVal);
			StringBuilder sValBuilder = new StringBuilder();
			sValBuilder.append("(");
			if (m.matches()){
				String rangeSet = m.group("IPRanges");
				String[] ranges = rangeSet.split(",");
				String connector = "";

				for (String range : ranges){

					// Determine how many 'classes' are included in the netmask.  If it is all '1's, just include the full IP.  If it is 
					// an exact class 'B', just include the first two octets.  Otherwise, list the octets at the appropriate level.
					String[] components = range.split("/");
					String ip = components[0];
					String nm = components[1];
					// How many bits are in the netmask?
					int[] componentList = IPv4Address.toArray(nm);
					int componentIndex = 0;
					int maskLength = 0;
					int i = 0;
					for (i = 0; i <= 3 && componentList[i] == 255; i++){
						maskLength += 8;
					}
					// Count the number of 1's
					int j = 0;
					while (j < 8 && i <= 3 && componentList[i] != 0){
						if ((componentList[i] & (1 << j++)) != 0){
						//componentList[i] = componentList[i] >> 1;
						    maskLength++;
						}
					}
					sValBuilder.append(connector + this.template);
					if (maskLength > 0){
						sValBuilder.append(" net " + ip + "/" + maskLength);
					} else {
						sValBuilder.append(" host " + ip);
					}
					connector = " or ";
				}
			}
			sValBuilder.append(")");
			return sValBuilder.toString();
		}
	}

	public enum TCPPortRangeSetTcpDumpSyntaxComponent implements TcpDumpSyntaxComponent {
		SOURCE_TCP_PORT_RANGESET ("src"),
		DEST_TCP_PORT_RANGESET ("dst"),
		TCP_PORT_RANGESET ("");
		
		private String template = null;

		private TCPPortRangeSetTcpDumpSyntaxComponent (String sFormTemplate){
			this.template = sFormTemplate;
		}
		
		public String syntaxForm (String sVal){
			// Parse out the range set thing.  The canonical form is:
			// [i1:i2,i3:i4...]  So, first split on comma, then determine how many there are, and 
			// build the string from that.
			IntegerRangeSet irs = new IntegerRangeSet(sVal);
			StringBuilder sValBuilder = new StringBuilder();
			String connector = "";
			
			for (IntegerRange range : irs){

				int start = range.getStartValue();
				int end = range.getEndValue();

				sValBuilder.append(connector + template + " portrange " + start + "-" + end);

				connector = " or ";
			}
			return sValBuilder.toString();
		}
	}
	
	public enum PacketDataLengthSyntaxComponent implements TcpDumpSyntaxComponent {
		PacketLengthGreaterThanOrEqual(">="),
		PacketLengthLessThanOrEqual("<=");

		String template = "len";
		String operator = "";
		
		private PacketDataLengthSyntaxComponent(String op){
			operator = op;
		}
		
		public String syntaxForm (String sVal){
			IntegerRangeSet irs = new IntegerRangeSet(sVal);
			StringBuilder sValBuilder = new StringBuilder();
			String connector = "";
			
			for (IntegerRange range : irs){

				int start = range.getStartValue();
				int end = range.getEndValue();

				sValBuilder.append(connector + template + " "+ operator + " " + start);

				connector = " or ";
			}
			return sValBuilder.toString();
			
		}
		
	}
	
	
	// Map of constraint feature -> type -> syntax
	public static final Map <IRI, Map<IRI, TcpDumpSyntaxComponent>> validSignalClassMap = new HashMap<IRI, Map<IRI, TcpDumpSyntaxComponent>>();
	private static String sigClassIRI = "http://solomon.cs.iastate.edu/ontologies/KIDS.owl#";
	
	static {
		HashMap<IRI, TcpDumpSyntaxComponent> tempMap = new HashMap<IRI, TcpDumpSyntaxComponent>();
		
		// IPPacketID Feature:
		tempMap.put(IRI.create(sigClassIRI + "IntegerEquality"), FormatStringTcpDumpSyntaxComponent.PACKETID_EQUAL);
		validSignalClassMap.put(IRI.create(sigClassIRI + "IPPacketID"), tempMap);
		
		// Size Feature:
		tempMap = new HashMap<IRI, TcpDumpSyntaxComponent>();
		tempMap.put(IRI.create(sigClassIRI + "integerGreaterThan"), FormatStringTcpDumpSyntaxComponent.PACKETSIZE_GREATERTHAN);
		validSignalClassMap.put(IRI.create(sigClassIRI + "IPPacketTotalLength"), tempMap);
		
		// TCPFlags Feature:
		tempMap = new HashMap<IRI, TcpDumpSyntaxComponent>();
		tempMap.put(IRI.create(sigClassIRI + "bitmaskMatchNonZero"), FormatStringTcpDumpSyntaxComponent.TCPFLAGS_BITMASK_SET);
		tempMap.put(IRI.create(sigClassIRI + "singleBitmaskMatchNonZero"), FormatStringTcpDumpSyntaxComponent.TCPFLAGS_BITMASK_SET);
		validSignalClassMap.put(IRI.create(sigClassIRI + "TCPFlags"), tempMap);
		
		// Source IP Address Feature:
		// IPv4SourceAddressSignalDomain
		tempMap = new HashMap<IRI, TcpDumpSyntaxComponent>();
		tempMap.put(IRI.create(sigClassIRI + "IPv4AddressRangeSetSignalConstraint"), IPRangeSetTcpDumpSyntaxComponent.SOURCE_IP_ADDRESS_RANGESET);
		validSignalClassMap.put(IRI.create(sigClassIRI + "IPv4SourceAddressSignalDomain"), tempMap);
		
		// Source TCP Port Feature
		tempMap = new HashMap<IRI, TcpDumpSyntaxComponent>();
		tempMap.put(IRI.create(sigClassIRI + "integerRangeSet"), TCPPortRangeSetTcpDumpSyntaxComponent.SOURCE_TCP_PORT_RANGESET);
		validSignalClassMap.put(IRI.create(sigClassIRI + "TCPSourcePort"), tempMap);
		
		// Destination TCP Port Feature
		tempMap = new HashMap<IRI, TcpDumpSyntaxComponent>();
		tempMap.put(IRI.create(sigClassIRI + "integerRangeSet"), TCPPortRangeSetTcpDumpSyntaxComponent.DEST_TCP_PORT_RANGESET);
		validSignalClassMap.put(IRI.create(sigClassIRI + "TCPDestinationPort"), tempMap);
		
		// Packet length feature
		tempMap = new HashMap<IRI, TcpDumpSyntaxComponent>();
		tempMap.put(IRI.create(sigClassIRI + "positiveIntegerEquality"), PacketDataLengthSyntaxComponent.PacketLengthGreaterThanOrEqual);
		validSignalClassMap.put(IRI.create(sigClassIRI + "PacketLengthSignalDomain"), tempMap);

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
			if (!validSignalClassMap.containsKey(sigDomain)){
				System.err.println("[W]: Detector implementation " + this.getClass().getName() + " cannot extract signal domain " + sigDomain + " returning empty match set!");
				throw new KIDSIncompatibleSyntaxException("Detector cannot extract signal domain " + sigDomain);
			}
			Map<IRI, TcpDumpSyntaxComponent> tempMap = validSignalClassMap.get(sigDomain);
			if (!tempMap.containsKey(sigConstraint)){
				System.err.println("[W]: Detector implementation " + this.getClass().getName() + " cannot represent signal constraint " + sigConstraint + " returning empty match set!");
				throw new KIDSIncompatibleSyntaxException("Detector cannot represent signal constraint " + sigConstraint);
			}
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
