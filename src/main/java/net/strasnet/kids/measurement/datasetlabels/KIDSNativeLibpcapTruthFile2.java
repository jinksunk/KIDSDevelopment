package net.strasnet.kids.measurement.datasetlabels;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.strasnet.kids.datasources.KIDSSnortIPAddressRange;
import net.strasnet.kids.detectors.UnimplementedIdentifyingFeatureException;
import net.strasnet.kids.measurement.DataInstance;
import net.strasnet.kids.measurement.EventOccurrence;
import net.strasnet.kids.measurement.Label;
import net.strasnet.kids.measurement.datasetinstances.KIDSNativeLibpcapDataInstance;
import net.strasnet.kids.measurement.datasetinstances.KIDSSnortDataInstance;

import org.apache.log4j.LogManager;
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
	
	public static final org.apache.log4j.Logger logme = LogManager.getLogger(KIDSNativeLibpcapTruthFile2.class.getName());
//	private static String regexPattern = "(?<timestamp>[\\d-]+),(?<pid>[\\d-]+),(?<sip>[\\d-]+),(?<dip>[\\d-]+),(?<oid>\\d+)\\s+(?<eid>\\d+)";
	private static String regexPattern = "(?<timestamp>[\\d-]+),(?<pid>[\\d-]+),(?<sip>[\\d-\\.]+),(?<dip>[\\d-\\.]+),(?<oid>\\d+)\\s+(?<eid>\\d+)";
    private Pattern rexp;
    private IRI ourEventIRI;
    
	public KIDSNativeLibpcapTruthFile2(){
		labelKey = new HashMap<Integer, Label>() ;
		seenEvents= new HashMap<Integer, EventOccurrence>() ;
		identifyingFeatures.add(IRI.create(featureIRI + "PacketID"));
		//identifyingFeatures.add(IRI.create(featureIRI + "instanceTimestamp"));
		identifyingFeatures.add(IRI.create(featureIRI + "ObservationOrder"));
		identifyingFeatures.add(IRI.create(featureIRI + "IPv4SourceAddressSignalDomain"));
		identifyingFeatures.add(IRI.create(featureIRI + "IPv4DestinationAddressSignalDomain"));
	}
	
	/**
	 * 
	 * @param labelIRI - The location of the labels
	 * @param eventIRI - The IRI of the event we are labeling
	 * @throws IOException 
	 * @throws NumberFormatException 
	 * @throws UnimplementedIdentifyingFeatureException 
	 */
	@Override
	public void init(String labelFileIRI, IRI eventIRI) throws NumberFormatException, IOException, UnimplementedIdentifyingFeatureException {
		ourEventIRI = eventIRI;
		rexp = Pattern.compile(regexPattern);
		// Read in file and build the labelKey
		logme.info(String.format("Reading labels from file: %s", labelFileIRI));
		BufferedReader r = new BufferedReader(new FileReader(new File(labelFileIRI)));
		int lineCount = 0;
		
		String line;
		while ((line = r.readLine()) != null){
			lineCount += 1;
			Matcher rm = rexp.matcher(line);
			if (rm.matches()){
				HashMap<IRI,String> vals = new HashMap<IRI,String>();
				Iterator<IRI> ifs = identifyingFeatures.iterator();
				while (ifs.hasNext()){
					IRI identFeature = ifs.next();
					if (identFeature.toString().equals(AbstractDatasetLabel.featureIRI + "PacketID")){
						vals.put(identFeature, rm.group("pid"));
					} else if (identFeature.toString().equals(AbstractDatasetLabel.featureIRI + "IPv4SourceAddressSignalDomain")){
//						vals.put(identFeature, KIDSSnortIPAddressRange.longIPToString(Long.parseLong(rm.group("sip"))));
						vals.put(identFeature, rm.group("sip"));
					} else if (identFeature.toString().equals(AbstractDatasetLabel.featureIRI + "IPv4DestinationAddressSignalDomain")){
//						vals.put(identFeature, KIDSSnortIPAddressRange.longIPToString(Long.parseLong(rm.group("dip"))));
						vals.put(identFeature, rm.group("dip"));
					} else if (identFeature.toString().equals(AbstractDatasetLabel.featureIRI + "ObservationOrder")){
						vals.put(identFeature, rm.group("oid"));
					}
					//vals.put(identifyingFeatures.get(1), rm.group("timestamp"));
					// These come in as Long values - convert to dotted quad:
				}
//				KIDSSnortDataInstance tempGuy = new KIDSSnortDataInstance(vals);
				KIDSNativeLibpcapDataInstance tempGuy = new KIDSNativeLibpcapDataInstance(vals);
				
				Integer eid = Integer.parseInt(rm.group("eid"));
				if (eid == 0){
					labelKey.put(tempGuy.hashCode(), new Label(EventOccurrence.NONEVENT, false));
				} else {
				    if (!seenEvents.containsKey(eid)){
					    seenEvents.put(eid, EventOccurrence.getEventOccurrence(ourEventIRI, eid));
				    }
				    //System.err.println("Positive instance: " + tempGuy.getID());
				    labelKey.put(tempGuy.hashCode(), new Label(seenEvents.get(eid), true));
				}
				logme.debug(String.format("Added labelKey for instance %s (hash value %d)",tempGuy.getID(),tempGuy.hashCode()));
			} else {
				logme.warn(String.format("Line %s in label file %s could not be matched to regular expression /%s/.", 
						line, labelFileIRI, regexPattern));
			}
		}
		r.close();
		logme.info(String.format("Read %d lines from label File %s, containing %d events identified for %d instances.", 
											lineCount,
											labelFileIRI.toString(),
											seenEvents.keySet().size(),
											labelKey.keySet().size()));
	}

	@Override
	/**
	 * IP ID is: <PID><DIP><SIP>
	 */
	public Label getLabel(DataInstance dve) {
		logme.debug(String.format("Setting label for %s (hashcode %s)...",dve.getID(), dve.hashCode()));
		if (labelKey.containsKey(dve.hashCode())){ //Integer.parseInt(dve.getID()))){
			dve.setLabel(labelKey.get(dve.hashCode()));
			logme.debug(String.format("Found hashkey for code: %s",dve.hashCode()));
		} else {
			//System.err.println("Instance [" + dve.getID() + "] is benign.");
			dve.setLabel(new Label(EventOccurrence.NONEVENT, false));
			logme.debug(String.format("Did not find hashkey for code: %s",dve.hashCode()));
		}
		// If no label is present, assume benign
		//return new Label(EventOccurrence.NONEVENT, false);
		logme.debug(String.format("Set label for instance %s to event %d",dve.getID(), dve.getLabel().getEventOccurrence().getID()));
	    return dve.getLabel();
	}

}
