/**
 * 
 */
package net.strasnet.kids.ui;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.log4j.LogManager;

/**
 * @author Chris Strasburg
 *
 * This UI to the KIDS system will perform the following operations:
 * 1) Given an input ABOX, perform a KIDS assessment on the specified detectors, choosing the most 
 *    advantageous set of detectors for the listed signals;
 * 2) Once the assessment is complete, shift to 'continuous monitoring' mode; given an ABOX specifying data *sources* (as
 *    opposed to data *sets*), enable the listed detectors on those data sources.
 *    
 *    Detectors applied to data sources still produce instances, which are correlated into correlated data instances according to 
 *    the correlation functions in use. As instances are produced, a streaming detector monitors the set of active signals, and issues
 *    an alert when the set of active signals matches (with some confidence?) the signal signature of an event.
 */
public class RunStreamingKIDSDetector {

	public static final org.apache.log4j.Logger logme = LogManager.getLogger(RunStreamingKIDSDetector.class.getName());


	/**
	 * @param args
	 */
	public static void main(String[] args) {
		/**
		 * Define and check options:
		 */
		HelpFormatter formatter = new HelpFormatter();

		Options options = new Options();
		options.addOption("f", true, "Assessment configuration file to load.");
		options.addOption("s", true, "Streaming configuration file to load.");
		
		CommandLineParser parser = new BasicParser();
		CommandLine cmd = null;
		try {
			cmd = parser.parse(options, args);
		} catch (ParseException e) {
			e.printStackTrace();
			formatter.printHelp("RunStreamingKIDSDetector", options);
			System.exit(1);
		}
		String configFilePath = cmd.getOptionValue("f");
		String streamConfigFilePath = cmd.getOptionValue("s");
		
		/** 
		 * Begin by reading in the config file and performing the assessment
		 */
		if (configFilePath == null){
			formatter.printHelp("RunStreamingKIDSDetector", options);
			System.exit(1);
		}
		//TODO: Use the referenced ABOX to perform an actual assessment across detectors.
		
		/**
		 * Next, initiate streaming mode and run until stopped:
		 */
		//TODO: Pull the list of signals from the assessment rather than this fixed list.
		String OntologyPrefix = "http://www.semantiknit.com/ontologies/2015/03/28/KIDS-CodeRedTest.owl#";
        String[] Signals = {
        		  "PacketLengthGreaterThan239Signal", 
        		  "MaliciousByteSequenceSignal" 
        		}; 
        
        // Instantiate the detector:
        

	}

}
