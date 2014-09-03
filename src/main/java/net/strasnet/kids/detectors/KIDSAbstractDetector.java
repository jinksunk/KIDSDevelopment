/**
 * 
 */
package net.strasnet.kids.detectors;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;

import net.strasnet.kids.KIDSOntologyDatatypeValuesException;
import net.strasnet.kids.KIDSOntologyObjectValuesException;
import net.strasnet.kids.detectorsyntaxproducers.KIDSDetectorSyntax;
import net.strasnet.kids.detectorsyntaxproducers.KIDSIncompatibleSyntaxException;
import net.strasnet.kids.measurement.KIDSMeasurementOracle;
import net.strasnet.kids.measurement.KIDSUnEvaluableSignalException;
import net.strasnet.kids.measurement.datasetviews.DatasetView;

import org.semanticweb.owlapi.model.IRI;

/**
 * @author cstras
 * The purpose of this class is to contain generic methods and common code required for
 * all KIDS detectors.
 *
 */
public class KIDSAbstractDetector implements KIDSDetector {

	protected IRI ourIRI = null;
	protected String executionCommand;
	protected KIDSMeasurementOracle myGuy = null;
	protected KIDSDetectorSyntax ourSyn = null; 

	/* (non-Javadoc)
	 * @see net.strasnet.kids.detectors.KIDSDetector#getMatchingInstances(java.util.Set, net.strasnet.kids.measurement.datasetviews.DatasetView)
	 */
	@Override
	public Set<Map<IRI, String>> getMatchingInstances(Set<IRI> signals,
			DatasetView v) throws IOException,
			KIDSOntologyObjectValuesException,
			KIDSOntologyDatatypeValuesException,
			KIDSIncompatibleSyntaxException, KIDSUnEvaluableSignalException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see net.strasnet.kids.detectors.KIDSDetector#init(java.lang.String, org.semanticweb.owlapi.model.IRI, net.strasnet.kids.measurement.KIDSMeasurementOracle)
	 */
	@Override
	public void init(String toExecute, IRI detectorIRI, KIDSMeasurementOracle o)
			throws KIDSOntologyObjectValuesException,
			KIDSOntologyDatatypeValuesException, InstantiationException,
			IllegalAccessException, ClassNotFoundException {
		myGuy = o;
		ourIRI = detectorIRI;
		executionCommand = toExecute;
		ourSyn = o.getDetectorSyntax(ourIRI);
	}

	/* (non-Javadoc)
	 * @see net.strasnet.kids.detectors.KIDSDetector#getIRI()
	 */
	@Override
	public IRI getIRI() {
		return this.ourIRI;
	}
	
	/**
	 * 
	 * @author cstras
	 * This abstract class outlines the required functionality of a stream gobbler for running local
	 * commands.  The 'run' method needs to be implemented to perform processing of command output 
	 * according to the specific detector.
	 */
	abstract class StreamGobbler extends Thread
	{
	    InputStream is;
	    String type;
	    StringBuilder mine;
	    Set<Map<IRI, String>> toReturn;
	    DatasetView v;
	    boolean processStream = false;
	    
	    StreamGobbler(InputStream is, String type)
	    {
	        this.is = is;
	        this.type = type;
	        mine = new StringBuilder();
	        toReturn = new HashSet<Map<IRI,String>>();
	    }
	    
	    StreamGobbler(InputStream is, String type, DatasetView v)
	    {
	        this.is = is;
	        this.type = type;
	        mine = new StringBuilder();
	        toReturn = new HashSet<Map<IRI,String>>();
	        this.v = v;
	        processStream = true;
	    }
	    
	    public String getOutput() {
			return mine.toString();
		}
	    
	    public Set<Map<IRI,String>> getReturnSet(){
	    	return toReturn;
	    }
		
	   /** ***  **   *
	    *  Utility classes below here in common to various detectors
	    */
	    
		/**
		 * Given a timestamp in snort format (), return a unix epoch timestamp instead.
		 * @param snortTS (e.g. 09/23-00:08:08.040748)
		 * @return corresponding unix epoch timestamp
		 * @throws ParseException 
		 */
		public String parseSnortTimestamp(String snortTS) throws ParseException{
			SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yy-HH:mm:ss.SSSSSS");
			return ("" + (sdf.parse("" + snortTS).getTime()/1000));
		}
		
		public long getIPAsInt(String IPDottedQuad) throws UnknownHostException{
			byte[] bytes = InetAddress.getByName(IPDottedQuad).getAddress();
			long iVal = 0;
			for (int i = 0; i < 4; i++){
				iVal <<= 8;
				iVal |= bytes[i] & 0xff;
				//iVal += ((long)(bytes[i] << (24 - (8*i)))) & 0xffffffff;
			}
			return iVal;
			
		}
	}
}
