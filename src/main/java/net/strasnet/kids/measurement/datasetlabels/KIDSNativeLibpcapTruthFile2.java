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

import net.strasnet.kids.datasources.KIDSSnortIPAddressRange;
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
public class KIDSNativeLibpcapTruthFile2 extends AbstractDatasetLabel implements DatasetLabel {
	
	private static String regexPattern = "(?<timestamp>[\\d-]+),(?<pid>[\\d-]+),(?<sip>[\\d-]+),(?<dip>[\\d-]+)\\s+(?<eid>\\d+)";
    private Pattern rexp;
    private IRI ourEventIRI;
    
	public KIDSNativeLibpcapTruthFile2(){
		labelKey = new HashMap<Integer, Label>() ;
		seenEvents= new HashMap<Integer, EventOccurrence>() ;
		identifyingFeatures.add(IRI.create(featureIRI + "PacketID"));
		identifyingFeatures.add(IRI.create(featureIRI + "PacketTimestamp"));
		identifyingFeatures.add(IRI.create(featureIRI + "PacketSourceIP"));
		identifyingFeatures.add(IRI.create(featureIRI + "PacketDestIP"));
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
				// These come in as Long values - convert to dotted quad:
				vals.put(identifyingFeatures.get(2), KIDSSnortIPAddressRange.longIPToString(Long.parseLong(rm.group("sip"))));
				vals.put(identifyingFeatures.get(3), KIDSSnortIPAddressRange.longIPToString(Long.parseLong(rm.group("dip"))));
//				KIDSSnortDataInstance tempGuy = new KIDSSnortDataInstance(vals);
				KIDSNativeLibpcapDataInstance tempGuy = new KIDSNativeLibpcapDataInstance(vals);
				
				Integer eid = Integer.parseInt(rm.group("eid"));
				if (eid == 0){
					labelKey.put(tempGuy.hashCode(), new Label(EventOccurrence.NONEVENT, false));
				} else {
				    if (!seenEvents.containsKey(eid)){
					    seenEvents.put(eid, EventOccurrence.getEventOccurrence(ourEventIRI, eid));
				    }
				    System.err.println("Positive instance: " + tempGuy.getID());
				    labelKey.put(tempGuy.hashCode(), new Label(seenEvents.get(eid), true));
				}
			}
		}
		r.close();
	}

	@Override
	/**
	 * IP ID is: <PID><DIP><SIP>
	 */
	public Label getLabel(DataInstance dve) {
		if (labelKey.containsKey(dve.hashCode())){ //Integer.parseInt(dve.getID()))){
			dve.setLabel(labelKey.get(dve.hashCode()));
			return dve.getLabel();
		}
		// If no label is present, assume benign
		return new Label(EventOccurrence.NONEVENT, false);
	}

}
