/**
 * 
 */
package net.strasnet.kids.ui;

import net.strasnet.kids.KIDSConfigurationException;
import net.strasnet.kids.measurement.KIDSMeasurementConfigurationException;
import net.strasnet.kids.measurement.test.testMeasurementConfiguration;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.log4j.LogManager;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

/**
 * @author cstras
 *
 * Given the path to a configuration file for a kids.strasnet.kids.measurement experiment, 
 * execute the available configuration sanity checks.
 * 
 * @see net.strasnet.kids.measurement.test.testMeasurementConfiguration
 * 
 */
public class testMesurementConfiguration {
	
	public static final org.apache.log4j.Logger logme = LogManager.getLogger(testMesurementConfiguration.class.getName());

	/**
	 * @param args - args[1] - String indicating the location of the configuration file to evaluate.
	 */
	public static void main(String[] args) {
		/**
		 * Define and check options:
		 */
		HelpFormatter formatter = new HelpFormatter();

		Options options = new Options();
		options.addOption("f", true, "Configuration file to check.");
		
		CommandLineParser parser = new BasicParser();
		CommandLine cmd = null;
		try {
			cmd = parser.parse(options, args);
		} catch (ParseException e) {
			e.printStackTrace();
			formatter.printHelp("testMeasurementConfiguration", options);
			System.exit(1);
		}
		String configFilePath = cmd.getOptionValue("f");
		
		/** 
		 * Begin by reading in the config file and instantiating the tests.
		 */
		if (configFilePath == null){
			formatter.printHelp("testMeasurementConfiguration", options);
			System.exit(1);
		}
		

		testMeasurementConfiguration tmc = null;
		try {
			tmc = new testMeasurementConfiguration(configFilePath);
		} catch (KIDSMeasurementConfigurationException e){
			logme.error("Could not load configuration: " + e);
			e.printStackTrace();
			System.exit(1);
		}
		
		try {
			tmc.testKBLoad();
		} catch (OWLOntologyCreationException e) {
			logme.error("Could not load knowledge base: " + e);
			e.printStackTrace();
			System.exit(1);
		}
		try {
			tmc.testEventIRI();
		} catch (KIDSMeasurementConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			tmc.testTimePeriodIRI();
		} catch (KIDSMeasurementConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			tmc.testInferredRelations();
		} catch (KIDSConfigurationException e) {
			logme.error("Invalid Configuration: " + e.getMessage());
			e.printStackTrace();
		}

	}

}
        