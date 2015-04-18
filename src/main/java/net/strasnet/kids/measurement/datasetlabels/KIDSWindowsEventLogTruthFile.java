/**
 * 
 */
package net.strasnet.kids.measurement.datasetlabels;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.strasnet.kids.measurement.DataInstance;
import net.strasnet.kids.measurement.EventOccurrence;
import net.strasnet.kids.measurement.Label;
import net.strasnet.kids.measurement.datasetinstances.AbstractDataInstance;

import org.semanticweb.owlapi.model.IRI;

/**
 * @author cstras
 *
 * Read a windows event log truth file with lines of the form: EventID,LogRecordID
 * 
 * E.g.:
 * 1,3
 */
public class KIDSWindowsEventLogTruthFile extends AbstractDatasetLabel implements DatasetLabel {

	private static String regexPattern = "(?<eid>\\d+),(?<lid>\\d+)";
	private static String regexIgnorePattern = "#.*";
    private Pattern rexp;
    private Pattern rexpIgnore;
	
	/** Set the identifying features */

	public KIDSWindowsEventLogTruthFile(){
		labelKey = new HashMap<Integer, Label>() ;
		seenEvents= new HashMap<Integer, EventOccurrence>() ;
		identifyingFeatures.add(IRI.create(featureIRI + "NTEventLogRecordID"));
	}

	/* (non-Javadoc)
	 * @see net.strasnet.kids.measurement.datasetlabels.DatasetLabel#getLabel(net.strasnet.kids.measurement.DataInstance)
	 */
	@Override
	public Label getLabel(DataInstance dve) {
		int ourID = Integer.parseInt(dve.getID().split("=")[1]);
		if (labelKey.containsKey(ourID)){
			dve.setLabel(labelKey.get(ourID));
			return dve.getLabel();
		}
		// If no label is present, assume benign
		return new Label(EventOccurrence.NONEVENT, false);
	}

	/* (non-Javadoc)
	 * @see net.strasnet.kids.measurement.datasetlabels.DatasetLabel#init(org.semanticweb.owlapi.model.IRI, org.semanticweb.owlapi.model.IRI)
	 */
	@Override
	public void init(String labelLocation, IRI eventIRI)
			throws NumberFormatException, IOException {
		ourEventIRI = eventIRI;
		rexp = Pattern.compile(regexPattern);
		rexpIgnore = Pattern.compile(regexIgnorePattern);
		// Read in file and build the labelKey
		BufferedReader r = new BufferedReader(new FileReader(new File(labelLocation)));
		
		String line;
		while ((line = r.readLine()) != null){
			Matcher rm = rexp.matcher(line);
			Matcher rmi = rexpIgnore.matcher(line);
			if (rm.matches()){
				Integer lid = Integer.parseInt(rm.group("lid"));
				Integer eid = Integer.parseInt(rm.group("eid"));
				if (eid == 0){
					labelKey.put(lid, new Label(EventOccurrence.NONEVENT, false));
				} else {
				    if (!seenEvents.containsKey(eid)){
					    seenEvents.put(eid, EventOccurrence.getEventOccurrence(eventIRI, eid));
				    }
				    labelKey.put(lid, new Label(seenEvents.get(eid), true));
				}
			} else if (rmi.matches()){
				// Nothing, we can ignore this
			} else {
				throw new IOException("Invalid label file format (cannot match line: " + line + " )");
			}
		}
		r.close();

	}
}
