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
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;

import net.strasnet.kids.KIDSOntologyDatatypeValuesException;
import net.strasnet.kids.KIDSOntologyObjectValuesException;
import net.strasnet.kids.detectorsyntaxproducers.KIDSDetectorSyntax;
import net.strasnet.kids.detectorsyntaxproducers.KIDSIncompatibleSyntaxException;
import net.strasnet.kids.measurement.DataInstance;
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

    protected TreeMap<String,Integer> orderMap;
	protected IRI ourIRI = null;
	protected String executionCommand;
	protected KIDSMeasurementOracle myGuy = null;
	protected KIDSDetectorSyntax ourSyn = null; 
	protected HashMap<IRI, Set<DataInstance>> sigMap;
	//protected Set<Set<DataInstance>> sigSets;
	private static HashMap<DataInstance, DataInstance> dataInstancePool = new HashMap<DataInstance, DataInstance>();
	protected static String featureIRI = "http://solomon.cs.iastate.edu/ontologies/KIDS.owl#";

	/* (non-Javadoc)
	 * @see net.strasnet.kids.detectors.KIDSDetector#getMatchingInstances(java.util.Set, net.strasnet.kids.measurement.datasetviews.DatasetView)
	 */
	@Override
	public Set<DataInstance> getMatchingInstances(Set<IRI> signals,
			DatasetView v) throws IOException,
			KIDSOntologyObjectValuesException,
			KIDSOntologyDatatypeValuesException,
			KIDSIncompatibleSyntaxException, KIDSUnEvaluableSignalException, UnimplementedIdentifyingFeatureException {
		//First, check the cache to see if we've already evaluated these signals:
		Set <DataInstance> matches = new HashSet<DataInstance>();
		boolean firstSignal = true;
		for (IRI signal : signals){
			if (sigMap.containsKey(signal)){
				System.err.println(String.format("[D] AbstractDetector - Matched signal %s; using %d cached values.",signal,this.sigMap.get(signal).size()));
				// We want the intersection of data instances:
				if (firstSignal){
					matches.addAll(sigMap.get(signal));
					firstSignal = false;
				} else {
					matches.retainAll(sigMap.get(signal));
				}
			} else {
				System.err.println(String.format("[D] AbstractDetector - No cache entry for signal %s.",signal));
				System.err.println("\t(Cache consists of:");
				for (IRI cEntry : sigMap.keySet()){
					System.err.println(String.format("\t%s.",cEntry));
				}
				System.err.println("\t)");
				return null;
			}
		}
		return matches;
	}

	/* (non-Javadoc)
	 * @see net.strasnet.kids.detectors.KIDSDetector#init(java.lang.String, org.semanticweb.owlapi.model.IRI, net.strasnet.kids.measurement.KIDSMeasurementOracle)
	 */
	@Override
	public void init(String toExecute, IRI detectorIRI, KIDSMeasurementOracle o)
			throws KIDSOntologyObjectValuesException,
			KIDSOntologyDatatypeValuesException, InstantiationException,
			IllegalAccessException, ClassNotFoundException {
		if (sigMap != null){
			System.err.println("[W] - Warning, initializing already-initialized detector!");
		} else {
			myGuy = o;
			ourIRI = detectorIRI;
			executionCommand = toExecute;
			ourSyn = o.getDetectorSyntax(ourIRI);
			sigMap = new HashMap<IRI, Set<DataInstance>>();
	        orderMap = new TreeMap<String,Integer>();
//			sigSets = new HashSet<Set<DataInstance>>();
		}
	}

	/* (non-Javadoc)
	 * @see net.strasnet.kids.detectors.KIDSDetector#getIRI()
	 */
	@Override
	public IRI getIRI() {
		return this.ourIRI;
	}

	/**
	 * If we already have an instance for this data instance, return that one instead, 
	 * otherwise, add this to the static map, and return it.
	 * @param di
	 * @return
	 */
	public static DataInstance getDataInstance(DataInstance di){
		if (! KIDSAbstractDetector.dataInstancePool.containsKey(di)){
			KIDSAbstractDetector.dataInstancePool.put(di, di);
		}
		return dataInstancePool.get(di);
	}
	
	/**
	 * 
	 * @return The number of data instances in the data pool.
	 */
	public static int getDataInstancePoolSize(){
		return KIDSAbstractDetector.dataInstancePool.size();
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
	    Set<DataInstance> toReturn;
	    DatasetView v;
	    boolean processStream = false;
	    
	    StreamGobbler(InputStream is, String type)
	    {
	        this.is = is;
	        this.type = type;
	        mine = new StringBuilder();
	        toReturn = new HashSet<DataInstance>();
	    }
	    
	    StreamGobbler(InputStream is, String type, DatasetView v)
	    {
	        this.is = is;
	        this.type = type;
	        mine = new StringBuilder();
	        toReturn = new HashSet<DataInstance>();
	        this.v = v;
	        processStream = true;
	    }
	    
	    public String getOutput() {
			return mine.toString();
		}
	    
	    public Set<DataInstance> getReturnSet(){
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
	public void addOrderKeyToIDMap(List<IRI> orderKeys, HashMap<IRI,String> idmap){

		StringBuilder orderKeyBuilder = new StringBuilder();
		for (IRI k : orderKeys){
			orderKeyBuilder.append(idmap.get(k));
		}

	// Add order features:
		int orderVal = 1;
		String orderKey = orderKeyBuilder.toString();
		if (orderMap.containsKey(orderKey)){
				orderVal = orderMap.get(orderKey)+1;
		}
		orderMap.put(orderKey.toString(), new Integer(orderVal));
		idmap.put(IRI.create(featureIRI + "ObservationOrder"), String.format("%d",orderVal));
	}

}
