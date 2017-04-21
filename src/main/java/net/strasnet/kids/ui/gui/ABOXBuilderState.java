/**
 * 
 */
package net.strasnet.kids.ui.gui;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JFrame;

import org.json.simple.JSONObject;
import org.apache.log4j.LogManager;
import org.json.simple.JSONArray;
import org.json.simple.parser.ParseException;

import org.json.simple.parser.JSONParser;

/**
 * @author cstras
 *
 * This class represents a GUI state, providing support to save and load information such as
 * a list of recently loaded ABOX files, last used TBOX file, etc... 
 * 
 * Each component of the ABOXBuilder can check for and save / access configuration elements
 * under it's own object state. The assumed structure is:
 * 
 * {
 *   "componentid": {
 *     "component-element1": "val1",
 *     "component-element2": ["val2", "val3"],
 *     "component-element3": {
 *       "field1": "val4",
 *       "field2": "val5",
 *     },
 *     ...
 *   },
 *   ...
 * }
 * 
 */
public class ABOXBuilderState {
	
	public static final org.apache.log4j.Logger logme = LogManager.getLogger(ABOXBuilderState.class.getName());
	
	JSONObject rootmap = null;
	private File st8file = null;
	
	public static final String defaultStateFile = ".ABOXBuilderStateFile";
	
	/**
	 * Initializes the builder state with a file to read from / write to.
	 * @param statefile
	 */
	public ABOXBuilderState(File statefile, JFrame frame){
		String ourFString = defaultStateFile;
		rootmap = new JSONObject();
	    try {
			readConfig(statefile);
			ourFString = statefile.getPath();
		} catch (FileNotFoundException e) {
			logme.info(String.format("Configuration file %s not found; creating.", statefile.getAbsolutePath()));
			rootmap = new JSONObject();
		} catch (IOException e) {
			logme.error(String.format("Problem reading state from file %s: %s; using empty state.", 
					statefile.getAbsolutePath(), e.getMessage()));
			rootmap = new JSONObject();
		} catch (ParseException e) {
			logme.error(String.format("Parser error in config file %s: %s.", 
					statefile.getAbsolutePath(), e.getMessage()));
			rootmap = new JSONObject();
		} finally {
			st8file = new File(ourFString);
			logme.debug(String.format("Loaded state contains %d data elements; file path set to %s.", 
					rootmap.size(), st8file.getAbsolutePath()));
			frame.addWindowListener(new WindowAdapter(){
						public void windowClosing(WindowEvent e) {
							// Save the state:
							try {
								writeConfig();
								logme.debug(String.format("Saved state to file: %s", 
										st8file.getAbsolutePath()));
							} catch (IOException e1) {
								logme.error(String.format("Could not save state to file: %s: %s", 
										st8file.getAbsolutePath(), e1.getMessage()));
							}
						}
					});
		}
	}
	
	/**
	 * A constructor to create an empty state object with the default state file.
	 */
	public ABOXBuilderState(JFrame frame){
		this(new File(defaultStateFile), frame);
	}
	
	/**
	 * Populate this object with the configuration file information:
	 * @param statefile
	 * @return 
	 * @throws ParseException 
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 */
	public void readConfig(File statefile) throws FileNotFoundException, IOException, ParseException{
		//JsonReader jrdr = Json.createReader(new FileInputStream(statefile));
		JSONParser parser = new JSONParser();
		if (!rootmap.isEmpty()){
			logme.warn("Overwriting existing configuration.");
		}
		rootmap = (JSONObject) parser.parse(new FileReader(statefile));
	}
	
	/**
	 * Write this object to the configuration file:
	 * @param statefile - the file object to write to 
	 * 
	 * @throws IOException 
	 */
	public void writeConfig() throws IOException{
		logme.debug(String.format("Writing configuration (%d elements) to: %s", 
				this.rootmap.size(),
				this.st8file.getAbsolutePath()));
		if (rootmap != null){
			FileWriter fwriter = new FileWriter(st8file);
			String jstring = rootmap.toJSONString();
			fwriter.write(rootmap.toJSONString());
			fwriter.flush();
			fwriter.close();
			logme.debug(String.format("Wrote JSON string: %s", jstring));
		} else {
			logme.warn(String.format("No configuration exists, not writing file %s", st8file.getAbsolutePath()));
		}
	}
	
	/**
	 * 
	 * @param componentid - 
	 * @param componentvalues
	 */
	public void putConfigSection(String componentid, Map<String, String> componentvalues){
		HashMap<String, String> valuemap = new HashMap<String, String>();
		
		for (String k : componentvalues.keySet()){
			// If it is a simple value, add/overwrite it:
			valuemap.put(k, componentvalues.get(k));
		}
		rootmap.put(componentid, valuemap);
		logme.debug(String.format("Put config section %s with %d elements.", 
				componentid, valuemap.size()));
	}
	
	/**
	 * 
	 * @param componentid
	 * @return A map containing the values for this section of the config, or a new, empty, Map if the config section was not
	 *         found.
	 */
	public Map<String, String> getConfigSection(String componentid){
		Map <String, String> toReturn;
		if (!rootmap.containsKey(componentid)){
			logme.info(String.format("No section found for %s; creating new.", componentid));
			toReturn = new HashMap<String, String>();
		} else {
			toReturn = ((Map<String, String>)rootmap.get(componentid));
		}
		logme.debug(String.format("Returning config section %s with %d elements.", 
				componentid, toReturn.size()));
		return toReturn;
	}

}
