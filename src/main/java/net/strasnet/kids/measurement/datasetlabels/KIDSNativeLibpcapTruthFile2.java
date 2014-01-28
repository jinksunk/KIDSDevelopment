package net.strasnet.kids.measurement.datasetlabels;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.strasnet.kids.measurement.DataInstance;
import net.strasnet.kids.measurement.EventOccurrence;
import net.strasnet.kids.measurement.Label;
import net.strasnet.kids.measurement.datasetinstances.KIDSNativeLibpcapDataInstance;
import net.strasnet.kids.measurement.datasetinstances.KIDSSnortDataInstance;

import org.semanticweb.owlapi.model.IRI;

/**
 * 
 * @author chrisstrasburg
 * This class can parse a label file of the form:
 * packetTimestamp,packetID,srcIP,dstIP eventID
 * 
 * An eventID of 0 means "normal"
 * packetTimestamp is a long, while srcIP and destIP are also longs.
 * 
 */
public class KIDSNativeLibpcapTruthFile2 implements DatasetLabel {
	
	private HashMap<Integer, Label> labelKey;
	private HashMap<Integer, EventOccurrence> seenEvents;
	private static String regexPattern = "(?<timestamp>[\\d-]+),(?<pid>[\\d-]+),(?<sip>[\\d-]+),(?<dip>[\\d-]+)\\s+(?<eid>\\d+)";
    private Pattern rexp;
    private IRI ourEventIRI;
	private static String featureIRI = "http://solomon.cs.iastate.edu/ontologies/KIDS.owl#";
    
	private static final List<IRI> identifyingFeatures = new LinkedList<IRI>();
	static {
		identifyingFeatures.add(IRI.create(featureIRI + "PacketID"));
		identifyingFeatures.add(IRI.create(featureIRI + "PacketTimestamp"));
		identifyingFeatures.add(IRI.create(featureIRI + "PacketSourceIP"));
		identifyingFeatures.add(IRI.create(featureIRI + "PacketDestIP"));
	}

	public KIDSNativeLibpcapTruthFile2(){
		labelKey = new HashMap<Integer, Label>() ;
		seenEvents= new HashMap<Integer, EventOccurrence>() ;
	}
	
	/**
	 * 
	 * @param labelIRI - The location of the labels
	 * @param eventIRI - The IRI of the event we are labeling
	 * @throws IOException 
	 * @throws NumberFormatException 
	 */
	@Override
	public void init(IRI labelFileIRI, IRI eventIRI) throws NumberFormatException, IOException {
		ourEventIRI = eventIRI;
		rexp = Pattern.compile(regexPattern);
		// Read in file and build the labelKey
		BufferedReader r = new BufferedReader(new FileReader(new File(labelFileIRI.toURI())));
		
		String line;
		while ((line = r.readLine()) != null){
			Matcher rm = rexp.matcher(line);
			if (rm.matches()){
				HashMap<IRI,String> vals = new HashMap<IRI,String>();
				vals.put(identifyingFeatures.get(0), rm.group("pid"));
				//vals.put(identifyingFeatures.get(1), rm.group("timestamp"));
				vals.put(identifyingFeatures.get(2), rm.group("sip"));
				vals.put(identifyingFeatures.get(3), rm.group("dip"));
				KIDSSnortDataInstance tempGuy = new KIDSSnortDataInstance(vals);
				
				Integer eid = Integer.parseInt(rm.group("eid"));
				if (eid == 0){
					labelKey.put(tempGuy.hashCode(), new Label(null, false));
				} else {
				    if (!seenEvents.containsKey(eid)){
					    seenEvents.put(eid, new EventOccurrence(ourEventIRI));
				    }
				    labelKey.put(tempGuy.hashCode(), new Label(seenEvents.get(eid), true));
				}
			}
		}
	}

	@Override
	public Label getLabel(DataInstance dve) {
		if (labelKey.containsKey(dve.hashCode())){ //Integer.parseInt(dve.getID()))){
			dve.setLabel(labelKey.get(Integer.parseInt(dve.getID())));
			return dve.getLabel();
		}
		// If no label is present, assume benign
		return new Label(EventOccurrence.NONEVENT, false);
	}

	@Override
	public int getNumEvents() {
		return EventOccurrence.currentEventID;
	}

	@Override
	public IRI getEventIRI() {
		return this.ourEventIRI;
	}

	@Override
	public List<IRI> getIdentifyingFeatures() {
		return identifyingFeatures;
	}

	@Override
	public List<EventOccurrence> getEventList() {
		List<EventOccurrence> toReturn = new LinkedList<EventOccurrence>();
		Iterator<EventOccurrence> eventSet = seenEvents.values().iterator();
		while (eventSet.hasNext()){
			toReturn.add(eventSet.next());
		}
		return toReturn;
	}

}
