package net.strasnet.kids.measurement.datasetviews;

import intervalTree.IntervalTree;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLNamedIndividual;

import net.strasnet.kids.KIDSCanonicalRepresentation;
import net.strasnet.kids.KIDSOntologyDatatypeValuesException;
import net.strasnet.kids.KIDSOntologyObjectValuesException;
import net.strasnet.kids.KIDSOracle;
import net.strasnet.kids.KIDSSignal;
import net.strasnet.kids.datasources.KIDSSnortIPAddressRange;
import net.strasnet.kids.detectorsyntaxproducers.KIDSIncompatibleSyntaxException;
import net.strasnet.kids.measurement.DataInstance;
import net.strasnet.kids.measurement.Dataset;
import net.strasnet.kids.measurement.EventOccurrence;
import net.strasnet.kids.measurement.KIDSDatasetFactory;
import net.strasnet.kids.measurement.KIDSMeasurementIncompatibleContextException;
import net.strasnet.kids.measurement.KIDSMeasurementInstanceUnsupportedFeatureException;
import net.strasnet.kids.measurement.KIDSMeasurementOracle;
import net.strasnet.kids.measurement.KIDSUnEvaluableSignalException;
import net.strasnet.kids.measurement.Label;
import net.strasnet.kids.measurement.datasetlabels.DatasetLabel;
import net.strasnet.kids.measurement.datasetlabels.KIDSTruthOracle;
import net.strasnet.kids.measurement.datasetviews.KIDSLibpcapDataset.KIDSLibpcapTruthFile.TruthFileParseException;
import net.strasnet.kids.signalRepresentations.KIDSRepresentationIncompatibleValueException;
import net.strasnet.kids.signalRepresentations.KIDSRepresentationInvalidRepresentationValueException;
import jpcap.JpcapCaptor;
import jpcap.packet.ICMPPacket;
import jpcap.packet.IPPacket;
import jpcap.packet.Packet;
/**
 * Represents an overall dataset, suitable for use in measuring characteristics of IDS/IRS.
 * To support measurement, the data must be accompanied by a "truth file".  In this case (libpcap),
 * each positive instance (where a packet represents the event) is identified by:
 * EventID:<StartEpochTimestamp,EndEpochTimestamp,SIP,DIP,ProtocolID>
 * 
 * Thus, if a packet matches one of these truth entries, it is assumed positive, otherwise it is 
 * assumed negative.
 * 
 * @author chrisstrasburg
 *
 */
public class KIDSLibpcapDataset implements Dataset, java.io.Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2243873069953554553L;
	private List<DataInstance> packets;    		  // All of the packets in the dataset
	private List<DataInstance> positivePackets;   // All of the packets associated with some event occurrence
	private KIDSMeasurementOracle myOracle;			      // The KIDSOracle used to interact with the KB
	private IRI eIRI = null;					  // The IRI of the event (class) under consideration
	private KIDSLibpcapTruthFile oracle;		  // The instance of the truth oracle for this dataset
	private IntervalTree<Integer> k;			  // Interval tree used to index events by time stamp
	private IRI dataIRI = null;					  // The IRI of the dataset object in the KB
	private IRI labelIRI = null;				  // The IRI of the 
	private IRI ourIRI = null;					  // The base IRI of 
	
												  // Map of event to another map of data instance
												  // (since an event will generally have many instances)
	private TreeMap<EventOccurrence,Boolean> eventList = null;
	private TreeMap<EventOccurrence,TreeMap<DataInstance,Boolean>> instancesByEvent = null;
	private TreeMap<OWLNamedIndividual,TreeMap<DataInstance,Boolean>> instancesBySignalMatch = null;

	
	/**
	 */
	public KIDSLibpcapDataset () {
		packets = new LinkedList<DataInstance>();
		positivePackets = new LinkedList<DataInstance>();
		instancesByEvent = new TreeMap<EventOccurrence,TreeMap<DataInstance,Boolean>>();
		instancesBySignalMatch = new TreeMap<OWLNamedIndividual,TreeMap<DataInstance,Boolean>>();
		eventList = new TreeMap<EventOccurrence,Boolean>();
	}
	
	/**
	 * Return the number of instances we have successfully read in:
	 */
	@Override
	public int numInstances() {
		return packets.size();
	}

	/**
	 * @return An iterator over the DataInstances
	 */
	@Override
	public Iterator<DataInstance> getIterator() {
		return packets.iterator();
	}

	/**
	 * @return the number of data instances which are associated with an event.
	 */
	@Override
	public int[] numPositiveInstances() {
		int numEvents = this.numEventOccurrences();
		int[] returnVal = new int [numEvents];
		int i = numEvents - 1;
		Iterator<EventOccurrence> es = eventList.descendingKeySet().iterator();
		while (es.hasNext()){
			EventOccurrence eo = es.next();
			if (instancesByEvent.containsKey(eo)){
				returnVal[i--] = instancesByEvent.get(eo).keySet().size();
			} else {
				returnVal[i--] = 0;
			}
		}
		return returnVal;
	}

	@Override
	public Iterator<DataInstance> getPositiveIterator() {
		return this.positivePackets.iterator();
	}
	
	@Override
	public int numEventOccurrences() {
		return eventList.size();
	}
	

	@Override
	public IRI getIRI() {
		return ourIRI;
	}

	/**
	 * Sets the data source IRI accordingly.
	 * @param dataIRI - An IRI to the data source.
	 */
	@Override
	public void setDataIRI(String dataIRI) throws KIDSUnsupportedSchemeException {
		// Check for supported schemes, e.g. file, mysql, etc...:
		IRI i = IRI.create(dataIRI);
		if (i.getScheme().equals("file")){
			// Set the IRI:
			this.dataIRI = i;
		} else {
			throw new KIDSUnsupportedSchemeException();
		}
	}

	@Override
	public void setLabelIRI(String labelIRI)  throws KIDSUnsupportedSchemeException {
		// Check for supported schemes, e.g. file, mysql, etc...:
		IRI i = IRI.create(labelIRI);
		if (i.getScheme().equals("file")){
			// Set the IRI:
			this.labelIRI = i;
		} else {
			throw new KIDSUnsupportedSchemeException();
		}		
	}
	
	@Override
	public void setDatasetIRI(IRI dsIRI){
		this.ourIRI  = dsIRI;
	}

	@Override
	public void setEventIRI(IRI evIRI){
		this.eIRI  = evIRI;
	}

	@Override
	/**
	 * Given a set of (labeled) DataInstances, initialize
	 * this object.
	 * @param eventIRI
	 * @param iSet
	 * @param evtList - The list of events given by the parent data set
	 */
	public void init(IRI eventIRI, Set<DataInstance> iSet, TreeMap<EventOccurrence,Boolean> eList){
		setEventIRI(eventIRI);
		//int numInstances = 0;
		//int statusProgressAt = 5;
		//String statusCharacter = ".";
		Packet tp = null;
		List<OWLNamedIndividual> applicableSignals = this.getKnownApplicableSignals();
		this.eventList = eList;
		
	    Iterator<DataInstance>i = iSet.iterator();
	    while (i.hasNext()){
	    	KIDSLibpcapDataInstance tmp = (KIDSLibpcapDataInstance) i.next();
	    	packets.add(tmp);
	    	Label l = tmp.getLabel();
	    	if (l.isEvent()){
				positivePackets.add(tmp);
				EventOccurrence TE = l.getEventOccurrence();
				// Index instance by event label; A collection of data instances indexed by
				// the EventOccurrence.
				TreeMap<DataInstance,Boolean> evtList = this.instancesByEvent.get(TE);
				if (evtList == null){
					evtList = new TreeMap<DataInstance,Boolean>();
					this.instancesByEvent.put(TE, evtList);
				}
				evtList.put(tmp, true);
				//statusCharacter = "+";
	    	} else {
				//statusCharacter = ".";
			}
	    	this.evaluateInstanceSignalMatches(applicableSignals, tmp);
			//if (numInstances % statusProgressAt == 0){
				//System.out.print(statusCharacter);
				//if (numInstances % (statusProgressAt * 80) == 0 ){
					//System.out.println("");
				//}
			//}
			//numInstances++;
	    }
	    //System.out.println();
	    
	}
	
	@Override
	/**
	 * Initialize the dataset from a pcap file and corresponding truth file.
	 * These files are given in the ontology used during instantiation.
	 */
	public void init(IRI eventIRI) throws IOException, TruthFileParseException {
		setEventIRI(eventIRI);
		oracle = new KIDSLibpcapTruthFile(labelIRI);
		int numInstances = 1;
		int statusProgressAt = 5;
		String statusCharacter = ".";
		Packet tp = null;
		
		String filter = "proto 1";
		
		JpcapCaptor myp = JpcapCaptor.openFile(dataIRI.toURI().getSchemeSpecificPart());
		// myp.setFilter(filter, true);
		
		tp = myp.getPacket();
		
		List<OWLNamedIndividual> applicableSignals = this.getKnownApplicableSignals();
		
		while (tp != Packet.EOF){
			KIDSLibpcapDataInstance temp = new KIDSLibpcapDataInstance(myOracle, tp);
			packets.add(temp);
			
			if (oracle.getEventOccurrence(temp) != null){
				// This instance is associated with an event
				positivePackets.add(temp);
				EventOccurrence TE = oracle.getEventOccurrence(temp);
				temp.setLabel(new Label(TE, true));
				// Index instance by event label; A collection of data instances indexed by
				// the EventOccurrence.
				eventList.put(TE, true);
				TreeMap<DataInstance,Boolean> evtList = this.instancesByEvent.get(TE);
				if (evtList == null){
					evtList = new TreeMap<DataInstance,Boolean>();
					this.instancesByEvent.put(TE, evtList);
				}
				evtList.put(temp, true);
				statusCharacter = "+";
			} else { 
				//System.out.println("[D]: Non-attack packet: " + temp.getData());
				statusCharacter = ".";
				temp.setLabel(new Label(null, false));
				
			}
			evaluateInstanceSignalMatches(applicableSignals, temp);
			
			tp = myp.getPacket();
			if (numInstances % statusProgressAt == 0){
				System.out.print(statusCharacter);
				if (numInstances % (statusProgressAt * 80) == 0 ){
					System.out.println("");
				}
			}
			numInstances++;
		} 
		System.out.println("");
		myp.close();		
	}
	
	private void evaluateInstanceSignalMatches(List<OWLNamedIndividual> signals, 
												KIDSLibpcapDataInstance temp){
			Iterator<OWLNamedIndividual> sigs = signals.iterator();
			while (sigs.hasNext()){
				OWLNamedIndividual stemp = sigs.next();
				try {
					if (temp.matchesSignal(stemp)){
						this.addSignalInstanceMapEntry(stemp, temp);
					}
				} catch (KIDSOntologyObjectValuesException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (KIDSMeasurementInstanceUnsupportedFeatureException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (KIDSMeasurementIncompatibleContextException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (KIDSOntologyDatatypeValuesException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (KIDSRepresentationInvalidRepresentationValueException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
		
	}
	private void addSignalInstanceMapEntry(OWLNamedIndividual signal, DataInstance instance){
		TreeMap<DataInstance,Boolean> sigList = this.instancesBySignalMatch.get(signal);
		if (sigList == null){
				sigList = new TreeMap<DataInstance,Boolean>();
				this.instancesBySignalMatch.put(signal, sigList);
		}
		sigList.put(instance, true);
	}

	@Override
	public void setOracle(KIDSMeasurementOracle kidsOracle) {
		this.myOracle = kidsOracle;
	}

	@Override
	/**
	 * Given the IRI of a signal, will return a dataset object which contains only those
	 * instances matching the signal.
	 * @param signal - the signal used to identify matching instances
	 */
	public Dataset getMatchingDataset(OWLNamedIndividual signal) {
		Dataset childDS;
		try {
			childDS = KIDSDatasetFactory.createDataset(this.getIRI(), this.getClass().getName(), 
							this.dataIRI.toString(), this.labelIRI.toString(), this.myOracle);
		} catch (KIDSUnsupportedSchemeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		
		// Create a new dataset object
		Set<DataInstance> s = new TreeSet<DataInstance>();
		if (this.instancesBySignalMatch.get(signal) != null){
			s.addAll(this.instancesBySignalMatch.get(signal).keySet());
		} 
		
		// Populate it with instances matching the given signal
		childDS.init(this.eIRI, s, this.eventList);
		
		return childDS;
	}

	/**
	 * getKnownApplicableSignals
	 * @return The list of known signals which can be applied to instances of
	 * this dataset.
	 */
	public List<OWLNamedIndividual> getKnownApplicableSignals(){
		return this.myOracle.getSignalsForDataset(this.ourIRI);
	}
	
	/**
	 * 
	 * @return The KIDSOracle object associated with this data set
	 */
	@Override
	public KIDSMeasurementOracle getKIDSOracle() {
		return myOracle;
	}
	
	/**
	 * Will index current instances against the given signal:
	 * @param signal
	 */
	@Override
	public void indexInstancesBySignal(OWLNamedIndividual signal){
		// Get / re-initialize the instance map for this signal:
		TreeMap<DataInstance,Boolean> index = new TreeMap<DataInstance,Boolean>();
		
		// For each instance in this dataset, build the tree for it:
		Iterator<DataInstance> i = this.getIterator();
		while (i.hasNext()){
			KIDSLibpcapDataInstance t = (KIDSLibpcapDataInstance)i.next();
			try {
				if (t.matchesSignal(signal)){
					index.put(t, true);
				}
			} catch (KIDSOntologyObjectValuesException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (KIDSMeasurementInstanceUnsupportedFeatureException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (KIDSMeasurementIncompatibleContextException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (KIDSOntologyDatatypeValuesException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (KIDSRepresentationInvalidRepresentationValueException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		this.instancesBySignalMatch.put(signal,index);
		
	}
	
	/*******************************************************************************************************
	 * Nested classes below:
	 *******************************************************************************************************/
	
	/**
	 * This class encapsulates each instance.
	 */
	public class KIDSLibpcapDataInstance implements net.strasnet.kids.measurement.DataInstance,Comparable<KIDSLibpcapDataInstance> {
		/**
		 * Public constructor.  A DataInstance needs to know about the ontology it is using so that it knows
		 * how to answer certain queries.
		 */
		private KIDSOracle myOracle;
		private static final String featureContextRelation = "#hasSignalDomainContext";
		private Packet raw;
		private static final String srcIPIRI = "http://solomon.cs.iastate.edu/ontologies/KIDS.owl#IPSourceAddress";
		private static final String dstIPIRI = "http://solomon.cs.iastate.edu/ontologies/KIDS.owl#IPDestinationAddress";
		private static final String ipProtoIRI = "http://solomon.cs.iastate.edu/ontologies/KIDS.owl#IPProtocolNumber";
		private Label l;

		
		public KIDSLibpcapDataInstance (KIDSOracle o, Packet p){
			myOracle = o;
			raw = p;
		}
		
		/**
		 * Uses relation hasSignalDomainContext to determine if any of the contexts include the feature.
		 * 
		 * @param testFeature - the IRI of the feature we want to check for
		 * @return true iff one of the contexts represented by this instance contains the feature asked for.
		 */
		public boolean hasSupportedFeature(IRI testFeature){
			// Build this feature individual
			
			// Query the ontology for all contexts
			return false;
		}
		
		@Override
		/**
		 * To evaluate a signal for an instance of this dataset, we need to:
		 *  - Extract the signal representation and instantiate it
		 *  - Represent the defined signal
		 *  - Extract the feature the signal is defined over
		 *  - Represent the feature value in this packet
		 *  - Compare the canonical value with the value extracted from this instance
		 *  - Return true if the comparison returns true
		 *  
		 *  For reasonable performance, we need to cache the representation.
		 *  
		 * @param signalInd - The signal individual we want to check.
		 */
		public boolean matchesSignal(OWLNamedIndividual signalInd) throws KIDSOntologyObjectValuesException, KIDSMeasurementInstanceUnsupportedFeatureException, KIDSMeasurementIncompatibleContextException, KIDSOntologyDatatypeValuesException, KIDSRepresentationInvalidRepresentationValueException {
			// Extract the signal representation and instantiate it with the associated signal definition
			KIDSCanonicalRepresentation kiss = this.myOracle.getSignalRepresentation(signalInd);
						
			// Extract the feature this signal is defined over
			OWLNamedIndividual ourSigDomain = this.myOracle.getSignalDomain(signalInd);
			
			// Represent this feature value
			String featureValue = this.getFeatureValue(ourSigDomain);
					
			// Compare this value with the signal
			try {
				return kiss.matches(featureValue);
			} catch (KIDSRepresentationIncompatibleValueException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return false;
			}

		}

		@Override
		/**
		 * @return The string canonical form value of the given feature for this instance.  
		 * @throws KIDSMeasurementInstanceUnsupportedFeatureException - if the feature cannot
		 * be extracted in this implementation.
		 */
		public String getFeatureValue(OWLNamedIndividual ourSigDomain) throws KIDSMeasurementInstanceUnsupportedFeatureException, KIDSMeasurementIncompatibleContextException {
			
			if (ourSigDomain.getIRI().toString().equals(srcIPIRI)){
				if (raw instanceof IPPacket){
				    //System.out.println("Returning: " + ((IPPacket)raw).src_ip.getHostAddress());
				    return ((IPPacket)raw).src_ip.getHostAddress();
				} else {
					throw new KIDSMeasurementIncompatibleContextException();
				}
			} else if (ourSigDomain.getIRI().toString().equals(dstIPIRI)){
				if (raw instanceof IPPacket){
					return ((IPPacket)raw).dst_ip.getHostAddress();
				} else {
					throw new KIDSMeasurementIncompatibleContextException();
				}
			} else if (ourSigDomain.getIRI().toString().equals(ipProtoIRI)){
				if (raw instanceof IPPacket){
					return "" + ((IPPacket)raw).protocol;
				} else {
					throw new KIDSMeasurementIncompatibleContextException();
				}
			} else {
				throw new KIDSMeasurementInstanceUnsupportedFeatureException();
			}
		}

		@Override
		/**
		 * @return the associated label.
		 */
		public Label getLabel() {
			return this.l;
		}

		@Override
		/**
		 * Sets the label to label
		 * @param label
		 */
		public void setLabel(Label label) {
			this.l = label;
		}

		@Override
		public Object getDataElement() {
			return this.raw;
		}
		
		/**
		 * For a packet, the following components are used to compute the hashCode:
		 * <UL>
		 * <LI>Timestamp</LI>
		 * <LI>Source MAC</LI>
		 * <LI>Dest MAC</LI>
		 * <LI>Protocol</LI>
		 * </UL>
		 */
		@Override
		public int hashCode(){
			return 0;
		}
		
		@Override
		public boolean equals(Object o){
			return (compareTo((KIDSLibpcapDataInstance)o) == 0);
		}
		
		/**
		 * The "natural order" of a packet (assumed ethernet) is determined by:
		 * timestamp
		 * src mac
		 * dst mac
		 * md5 of payload
		 * @param o
		 * @return
		 */
		@Override
		public int compareTo(KIDSLibpcapDataInstance o){
			Hashtable<String,String> cval = getPacketComparatorFields();
			Hashtable<String,String> ocval = o.getPacketComparatorFields();
			
			// Start with timestamp
			if (cval.containsKey("timestamp")){
			    if (ocval.containsKey("timestamp")){
			    	Double v1 = Double.parseDouble(cval.get("timestamp"));
			    	Double v2 = Double.parseDouble(ocval.get("timestamp"));
			    	if (v1 < v2){
			    		return -1;
			    	} else if (v1 > v2){
			    		return 1;
			    	} 
			    } else {
			    	return 1;
			    }
			} else if (ocval.containsKey("timestamp")){
				return -1;
			}
			
			if (cval.containsKey("srcmac")){
			    if (ocval.containsKey("srcmac")){
			    	BigInteger v1 = new BigInteger(cval.get("srcmac"),16);
			    	BigInteger v2 = new BigInteger(ocval.get("srcmac"),16);
			    	if (!v1.equals(v2)){
			    		return v1.compareTo(v2);
			    	}
			    } else {
			    	return 1;
			    }
			} else if (ocval.containsKey("srcmac")){
				return -1;
			}
			    
			if (cval.containsKey("dstmac")){
			    if (ocval.containsKey("dstmac")){
			    	BigInteger v1 = new BigInteger(cval.get("dstmac"),16);
			    	BigInteger v2 = new BigInteger(ocval.get("dstmac"),16);
			    	if (!v1.equals(v2)){
			    		return v1.compareTo(v2);
			    	}
			    } else {
			    	return 1;
			    }
			} else if (ocval.containsKey("dstmac")){
				return -1;
			}
			
			if (cval.containsKey("srcIP")){
			    if (ocval.containsKey("srcIP")){
			    	Integer v1 = new Integer(cval.get("srcIP"));
			    	Integer v2 = new Integer(ocval.get("srcIP"));
			    	if (!v1.equals(v2)){
			    		return v1.compareTo(v2);
			    	}
			    } else {
			    	return 1;
			    }
			} else if (ocval.containsKey("srcIP")){
				return -1;
			}
			
			if (cval.containsKey("dstIP")){
			    if (ocval.containsKey("dstIP")){
			    	Integer v1 = new Integer(cval.get("dstIP"));
			    	Integer v2 = new Integer(ocval.get("dstIP"));
			    	if (!v1.equals(v2)){
			    		return v1.compareTo(v2);
			    	}
			    } else {
			    	return 1;
			    }
			} else if (ocval.containsKey("dstIP")){
				return -1;
			}
			
			
			if (cval.containsKey("md5")){
			    if (ocval.containsKey("md5")){
			    	BigInteger v1 = new BigInteger(cval.get("md5"),16);
			    	BigInteger v2 = new BigInteger(ocval.get("md5"),16);
			    	if (!v1.equals(v2)){
			    		return v1.compareTo(v2);
			    	}
			    } else {
			    	return 1;
			    }
			} else if (ocval.containsKey("md5")){
				return -1;
			}
			
			return 0;
			
		}
		
		/**
		 * Efficiently convert a byte to a hex string.
		 * @param byte b
		 * @return A String value
		 */
		private String byteToString(byte b){
			final char[] hChars = {'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};
			int v = b & 0xFF;
			char[] hexDigits = new char[2];
			hexDigits[0] = hChars[v >>> 4];
			hexDigits[1] = hChars[v & 0x0f];
			return new String(hexDigits);
		}
		
		/**
		 * 
		 * @return A hashtable with the structure:
		 * "timestamp" => ts.value,
		 * "srcmac" => srcmac.value,
		 * "dstmac" => dstmac.value,
		 * "md5" => md5(Packet.data)
		 */
		protected Hashtable<String,String> getPacketComparatorFields(){
			Hashtable<String,String> toReturn = new Hashtable<String,String>();
			String tVal;
			
			if (this.raw instanceof Packet){
				tVal = "" + ((Packet)this.raw).sec + "." + ((Packet)this.raw).usec;
				toReturn.put("timestamp", tVal);
				tVal = "";
				
				for (int i = 0; i < 6; i++){
					//System.out.println("Byte value: " + Integer.toString(((Packet)this.raw).header[i]));
					tVal += byteToString(((Packet)this.raw).header[i]);
				}
				toReturn.put("dstmac", tVal);
				tVal = "";
				for (int i = 6; i < 12; i++){
					//System.out.println("Byte value: " + Integer.toString(((Packet)this.raw).header[i]));
					tVal += byteToString(((Packet)this.raw).header[i]);
				}
				//System.out.println("Header length:" + ((Packet)this.raw).header.length);
				//System.out.println("Data length:" + ((Packet)this.raw).data.length);
				toReturn.put("srcmac", tVal);
				MessageDigest d;
				try {
					tVal = "";
					d = MessageDigest.getInstance("MD5");
					byte[] db = d.digest(((Packet)this.raw).data);
					//System.out.println("Digest data bytes:");
					for (int k = 0; k < db.length; k++){
						String t = byteToString(db[k]);
						//System.out.print(t + ":");
						tVal += t;
					}
					toReturn.put("md5", tVal);
				} catch (NoSuchAlgorithmException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				/**
				 * Set IP fields:
				 */
				if (this.raw instanceof IPPacket){
  				    tVal = "" + KIDSSnortIPAddressRange.byte2long(((IPPacket)this.raw).src_ip.getAddress());
				    toReturn.put("srcIP", tVal);
				    
  				    tVal = "" + KIDSSnortIPAddressRange.byte2long(((IPPacket)this.raw).dst_ip.getAddress());
  			    }
			}
			return toReturn;
		}

		@Override
		public void setID(HashMap<IRI, String> idValues) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public String getID() {
			// TODO Auto-generated method stub
			return null;
		}
	}
	

	
	/**
	 * This class encapsulates the truth file for the LibpcapDataset.
	 * Instances are indexed by 6-tuple (timestamp,sip,dip,sp,dp,protocol).  In this case
	 * distinct event occurrences are identified in the truth file generator.
	 * @author chrisstrasburg
	 */
	public class KIDSLibpcapTruthFile implements KIDSTruthOracle {
		/*              stime  ,    etime  ,     sip  ,     dip  ,     proto ,  eventID  */
	    private IntervalTree <ITWrapper> hashedData;
	    private static final String eventDelim = ":";
	    private static final String fieldDelim = ",";
	    private HashMap<String, EventOccurrence> knownEvents;
	    private IntervalTree<Long> timeInterval;
	    
	    /**
	     * 
	     * @param labelIRI - The file of actual event ids and specs.  In the form:
	     * X:<start,end,sip,dip,proto#>
	     * ...
	     * Where:
	     *   - X is the event ID, unique for distinct event occurences
	     *   - start is the epoch start time
	     *   - end is the eopoch end time
	     *   - sip is the source IP address
	     *   - dip is the destination IP address
	     *   - proto# is the protocol number
	     * @throws IOException 
	     * @throws TruthFileParseException 
	     */
	    public KIDSLibpcapTruthFile (IRI labelIRI) throws IOException, TruthFileParseException{
	    	// Open the file populate our hash:
	    	java.io.BufferedReader input = new BufferedReader(new FileReader(labelIRI.toURI().getSchemeSpecificPart()));
	    	String currentLine = input.readLine();
	    	hashedData = new IntervalTree<ITWrapper>();
	    	knownEvents = new HashMap<String, EventOccurrence>();
	    	timeInterval = new IntervalTree<Long>();
	    	
	    	while (currentLine != null){
	    		// Process the line
	    		processLine(currentLine);
	    		currentLine = input.readLine();
	    	}
	    }
	    
		/**
	     * Process the given line, throw an exception if the line cannot be parsed:
	     */
	    private void processLine(String currentLine) throws TruthFileParseException {
	    	String sip;
	    	String dip;
	    	long stime;
	    	long etime;
	    	int protoNum;
	    	String eventID;
	    	String remainder;
	    	
	    	String[] tokens = currentLine.split(eventDelim);
	    	
	    	if (tokens.length != 2){
	    		throw new TruthFileParseException();
	    	}
	    	eventID = tokens[0];
	    	remainder = tokens[1];
	    	
	    	tokens = remainder.split(fieldDelim);
	    	
	    	if (tokens.length != 5){
	    		throw new TruthFileParseException();
	    	}
	    	stime = Long.parseLong(tokens[0]);
	    	etime = Long.parseLong(tokens[1]);
	    	sip = tokens[2];
	    	dip = tokens[3];
	    	protoNum = Integer.parseInt(tokens[4]);
	    	
	    	// Now load data into the hash nest:
	    	loadHashData(stime, etime, sip, dip, protoNum, eventID);
	    }
	    
	    private void loadHashData(long stime, long etime, String sip, String dip, int protoNum, String eventID){
	    	// Check each nested hash:
	    	List<ITWrapper> l =hashedData.get(stime, etime);
	    	if (l.isEmpty()){
	    		hashedData.addInterval(stime, etime, new ITWrapper(sip, dip, protoNum, eventID));
	    	} else {
	    		l.get(0).put(sip, dip, protoNum, eventID);
	    	}
	    }
	    
	    /**
	     * 
	     * @param c - the data instance in question.
	     * @return null if the instance is not associated with an event, the EventOccurence object
	     * otherwise.
	     */
	    public EventOccurrence getEventOccurrence(KIDSLibpcapDataInstance c){
	    	Packet p = (Packet) c.getDataElement();
	    	if (p instanceof IPPacket){
    	    	// Get the required parameters in the correct form:
    	    	long stime = ((IPPacket)p).sec;
    	    	String sip = ((IPPacket)p).src_ip.getHostAddress();
    	    	String dip = ((IPPacket)p).dst_ip.getHostAddress();
    	    	short protoID = ((IPPacket)p).protocol;
	    	    	
	    	    // Iterate over all start times which are greater than c.getData().sec
    	    	List<ITWrapper> l = hashedData.get(stime);
    	    	if (l.isEmpty() || l.get(0).get(sip, dip, (int)protoID) == null){
    	    		return null;
    	    	} else {
    	    		return l.get(0).get(sip, dip, (int)protoID);
    	    	}
	    	}
	    	// TODO: This is a huge invalid assumption!
	    	return null;
	    }
	    
	    public class TruthFileParseException extends Exception{

			/**
			 * 
			 */
			private static final long serialVersionUID = -6737727646357921420L;
	    	
	    }
	    
	    public class ITWrapper {
	    	private HashMap<String, SIPWrapper> kids;
	    	String s;
	    	public ITWrapper(String sip, String dip, int protoNum, String eventID){
	    		s = sip;
	    		kids = new HashMap<String, SIPWrapper>();
	    		kids.put(sip, new SIPWrapper(dip, protoNum, eventID));
	    	}
	    	
	    	public EventOccurrence get(String sip, String dip, Integer proto) {
	    		if (!kids.containsKey(sip)){
	    			return null;
	    		}
				return kids.get(sip).get(dip,proto);
			}

			public void put(String sip, String dip, int protoNum, String eventID) {
	    		if (!kids.containsKey(sip)){
	    			kids.put(sip, new SIPWrapper(dip, protoNum, eventID));
	    		} else {
	    			kids.get(sip).put(dip, protoNum, eventID);
	    		}
			}

			public SIPWrapper get(String key){
	    		return kids.get(key);
	    	}
			
	    	public List<String> getKeys(){
	    		LinkedList<String> retVal = new LinkedList<String>();
	    		Iterator<String> i = kids.keySet().iterator();
	    		while (i.hasNext()){
	    			String ktemp = i.next();
	    			Iterator<String> vtemp = kids.get(ktemp).getKeys().iterator();
	    			while (vtemp.hasNext()){
	    				retVal.add(ktemp.toString() + "," + vtemp.next().toString());
	    			}
	    		}
	    		return retVal;
	    	}
			
	    }
	    public class SIPWrapper {
	    	private HashMap<String, DIPWrapper> kids;
	    	String s;
	    	public SIPWrapper(String dip, int protoNum, String eventID){
	    		s = dip;
	    		kids = new HashMap<String, DIPWrapper>();
	    		kids.put(dip, new DIPWrapper(protoNum, eventID));
	    	}
	    	
	    	public EventOccurrence get(String dip, Integer proto) {
	    		if (!kids.containsKey(dip)){
	    			return null;
	    		}
				return kids.get(dip).get(proto);
			}

			public void put(String dip, int protoNum, String eventID) {
	    		if (!kids.containsKey(dip)){
	    			kids.put(dip, new DIPWrapper(protoNum, eventID));
	    		} else {
	    			kids.get(dip).put(protoNum, eventID);
	    		}
			}

			public void put(String key, DIPWrapper val){
	    		kids.put(key, val);
	    	}
	    	
	    	public DIPWrapper get(String key){
	    		return kids.get(key);
	    	}
	    	
	    	public List<String> getKeys(){
	    		LinkedList<String> retVal = new LinkedList<String>();
	    		Iterator<String> i = kids.keySet().iterator();
	    		while (i.hasNext()){
	    			String ktemp = i.next();
	    			Iterator<String> vtemp = kids.get(ktemp).getKeys().iterator();
	    			while (vtemp.hasNext()){
	    				retVal.add(ktemp.toString() + "," + vtemp.next().toString());
	    			}
	    		}
	    		return retVal;
	    	}
	    }
	    public class DIPWrapper {
	    	private HashMap<Integer, EventOccurrence> kids;
	    	Integer p;
	    	public DIPWrapper(Integer proto, String eventID){
	    		p = proto;
	    		kids = new HashMap<Integer, EventOccurrence>();
	    		if (!knownEvents.containsKey(eventID)){
	    			knownEvents.put(eventID, new EventOccurrence(eIRI));
	    		}
	    		EventOccurrence eo = knownEvents.get(eventID);
	    		kids.put(p, eo);
	    	}
	    	
	    	public void put(Integer key, String eventID){
	    		if (!knownEvents.containsKey(eventID)){
	    			knownEvents.put(eventID, new EventOccurrence(eIRI));
	    		}
	    		EventOccurrence eo = knownEvents.get(eventID);
	    		kids.put(key, eo);
	    	}
	    	
	    	public EventOccurrence get(Integer key){
	    		if (!kids.containsKey(key)){
	    			return null;
	    		}
	    		return kids.get(key);
	    	}
	    	
	    	public List<String> getKeys(){
	    		LinkedList<String> retVal = new LinkedList<String>();
	    		Iterator<Integer> i = kids.keySet().iterator();
	    		while (i.hasNext()){
	    			retVal.add(i.next().toString());
	    		}
	    		return retVal;
	    	}
	    }
		@Override
		public int numEventOccurrences() {
			// TODO Auto-generated method stub
			return 0;
		}
	}



	@Override
	public Set<DataInstance> getMatchingInstances(Set<IRI> applicableSignals)
			throws KIDSOntologyObjectValuesException,
			KIDSOntologyDatatypeValuesException, IOException,
			KIDSIncompatibleSyntaxException, KIDSUnEvaluableSignalException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Dataset getDataSubset(Set<DataInstance> dataInstanceSet)
			throws KIDSOntologyDatatypeValuesException,
			KIDSOntologyObjectValuesException, InstantiationException,
			IllegalAccessException, ClassNotFoundException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void init(DatasetView dv, DatasetLabel dl, KIDSMeasurementOracle o,
			IRI eventIRI) throws KIDSOntologyDatatypeValuesException,
			KIDSOntologyObjectValuesException, InstantiationException,
			IllegalAccessException, ClassNotFoundException {
		// TODO Auto-generated method stub
		
	}

}
