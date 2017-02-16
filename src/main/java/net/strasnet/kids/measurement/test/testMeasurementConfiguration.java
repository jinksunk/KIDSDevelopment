package net.strasnet.kids.measurement.test;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.PrefixManager;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.util.DefaultPrefixManager;
import org.semanticweb.owlapi.util.SimpleIRIMapper;

import net.strasnet.kids.KIDSConfigurationException;
import net.strasnet.kids.measurement.KIDSMeasurementConfigurationException;
import net.strasnet.kids.measurement.KIDSMeasurementOracle;

/**
 * 
 * @author Chris Strasburg
 * 
 * This contains methods intended to evaluate measurement configuration files. It
 * can be included in a standalone test suite or user interface code.
 *
 * @see net.strasnet.kids.ui.testMeasurementConfiguration
 */

public class testMeasurementConfiguration {

    public static final Logger logme = LogManager.getLogger(KIDSSignalSelectionInterface.class.getName());
    
    private Map<String, String> pMap = null;
    private KIDSMeasurementOracle myGuy = null;
    private OWLDataFactory odf = null;
    private OWLReasoner r = null;


    /**
     * 
     * @param cfile - The configuration file to base the test on.
     * @throws KIDSMeasurementConfigurationException 
     */
    public testMeasurementConfiguration (String cfile) throws KIDSMeasurementConfigurationException{

    	/** 
    	 * First, ensure that all properties are present and accounted for - this is handled by the
    	 * loading code.
    	 */
    	logme.info(String.format("Loading configuration file... %s", cfile));
    	pMap = KIDSSignalSelectionInterface.loadPropertiesFromFile(
    			cfile, 
    			KIDSSignalSelectionInterface.configFileValues.keySet()
    	);
    	
    	if (pMap == null){
    		throw new KIDSMeasurementConfigurationException();
    	}
    	logme.info("Configuration file loaded.");
    	
    	StringBuilder logm = new StringBuilder("Read configuration properties: \n");
    	for (String k : pMap.keySet()){
    		logm.append(String.format("%s: %s\n",k, pMap.get(k)));
    	}
    	logme.info(logm.toString());
    	
    }
    
    /**
     * Produce an error message if the KB cannot be loaded.
     * @throws OWLOntologyCreationException - If the ontology cannot be loaded for some reason. 
     * 
     */
    public void testKBLoad() throws OWLOntologyCreationException{
    	/**
    	 * Next, ensure that each ontology file exists and is readable (again this is accomplished via the
    	 * oracle loading code).
    	 */
    	myGuy = new KIDSMeasurementOracle();
    	IRI ABOXIRI = IRI.create(pMap.get("ABoxIRI"));
    	IRI ABOXFILE = IRI.create(pMap.get("ABoxFile"));
    	IRI TBOXIRI = IRI.create(pMap.get("TBoxIRI"));
    	IRI TBOXFILE = IRI.create(pMap.get("TBoxFile"));
    	// Create the required mapping between IRI and location:
    	List<SimpleIRIMapper> m = new LinkedList<SimpleIRIMapper>();
    	m.add(new SimpleIRIMapper(ABOXIRI, ABOXFILE));
    	m.add(new SimpleIRIMapper(TBOXIRI, TBOXFILE));
    	try {
    		myGuy.loadKIDS(ABOXIRI, m);
    		odf = myGuy.getOwlDataFactory();
    	   	r = myGuy.getReasoner();
    	} catch (OWLOntologyCreationException e){
    		logme.error("Could not load ontology: %s " + e);
    		throw e;
    	}
    }
    
    /**
     * Ensure the event IRI exists in the KB.
     * @throws KIDSMeasurementConfigurationException 
     */
    public void testEventIRI() throws KIDSMeasurementConfigurationException{
    	// Ask the oracle:
    	OWLOntology o = myGuy.getOntology();
    	PrefixManager p = myGuy.getPrefixManager();
    	
    	StringBuilder logm = new StringBuilder("Using prefix manager with prefixes: \n");
    	for (String name : p.getPrefixNames()){
    		logm.append(String.format("%s: %s\n", name, p.getPrefix(name)));
    	}
    	logme.info(logm);

    	if (! o.containsIndividualInSignature(IRI.create(pMap.get("EventIRI")))){
    		logme.error("Event IRI " + pMap.get("EventIRI") + " not found in ontology.");
    		throw new KIDSMeasurementConfigurationException("Event IRI " + pMap.get("EventIRI") + " not found in ontology.");
    	}
    }
    
    /**
     * Ensure the time period IRI exists in the KB.
     * @throws KIDSMeasurementConfigurationException 
     */
    public void testTimePeriodIRI() throws KIDSMeasurementConfigurationException{
    	// Ask the oracle:
    	OWLOntology o = myGuy.getOntology();
    	PrefixManager p = myGuy.getPrefixManager();
    	
    	StringBuilder logm = new StringBuilder("Using prefix manager with prefixes: \n");
    	for (String name : p.getPrefixNames()){
    		logm.append(String.format("%s: %s\n", name, p.getPrefix(name)));
    	}
    	logme.info(logm);

    	if (!o.containsIndividualInSignature(IRI.create(pMap.get("TimePeriodIRI")))){
    		logme.error("TimePeriod IRI " + pMap.get("TimePeriodIRI") + " not found in ontology.");
    		throw new KIDSMeasurementConfigurationException("Event IRI " + pMap.get("EventIRI") + " not found in ontology.");
    	}
    }
    
    /**
     * This method will test that the following relations are established and properies exist in the ontology:
     *  * At least one signal exists;
     *  * At least one detector exists;
     *  * At least one dataset exists and is readable;
     *  * At least one response exists and is readable;
     *  * Signals are linked to the appropriate contexts, domains, values, manifestations, etc...
     *  * Detectors are identified as able to evaluate various things;
     *  * Java classes exist for detectors, dataset views, label functions
     * @throws KIDSConfigurationException 
     */
    public void testInferredRelations() throws KIDSConfigurationException{
    	OWLOntology o = myGuy.getOntology();
    	PrefixManager p = myGuy.getPrefixManager();
    	OWLObjectProperty eventSignalProp = odf.getOWLObjectProperty(IRI.create("http://solomon.cs.iastate.edu/ontologies/KIDS.owl#isProducerOf"));

    	// First get a list of all the signals known to be produced by an event
    	// There must be at least one.
    	OWLNamedIndividual event = odf.getOWLNamedIndividual(IRI.create(pMap.get("EventIRI")));
    	Set<OWLNamedIndividual> signals = r.getObjectPropertyValues(event, eventSignalProp).getFlattened();

    	/**
    	 * Each signal needs to have the following associated with it:
    	 */
    	logme.info("Testing signals produced by event " + pMap.get("EventIRI"));
    	for (OWLNamedIndividual signal : signals){
    		logme.info("TESTING SIGNAL " + signal.getIRI() + " ...");
    		testSignalRelations(signal);
    		logme.info(" ... SUCCESS.");
    	}
    }
    
    /**
     * This method will, given a signal individual, ensure that various properties and class memberships
     * hold in the reasoner.
     * 
     * Specifically, the following are tested:
     * * signal is a member of the Signal class
     * * signal has a constraint, value, and domain
     * * signal domain has an associated signal context
     * * signal is evaluable by at least one dataset
     * * signal is applied by at least one detector
     * * signal is represented by at least one signal representation
     * * signal is manifested by at least one signal manifestation
     * @throws KIDSConfigurationException 
     */
    public void testSignalRelations(OWLNamedIndividual signal) throws KIDSConfigurationException{
    	
    	IRI [][] checkListAtLeastOne = new IRI[][] {
    			new IRI[] { IRI.create("http://solomon.cs.iastate.edu/ontologies/KIDS.owl#isAppliedByDetector"), 
    					    IRI.create("http://solomon.cs.iastate.edu/ontologies/KIDS.owl#Detector") },
    			new IRI[] { IRI.create("http://solomon.cs.iastate.edu/ontologies/KIDS.owl#hasConstraint"), 
    					    IRI.create("http://solomon.cs.iastate.edu/ontologies/KIDS.owl#SignalConstraint") },
    			new IRI[] { IRI.create("http://solomon.cs.iastate.edu/ontologies/KIDS.owl#hasSignalValue"), 
    					    IRI.create("http://solomon.cs.iastate.edu/ontologies/KIDS.owl#SignalValue") },
    			new IRI[] { IRI.create("http://solomon.cs.iastate.edu/ontologies/KIDS.owl#hasDomain"), 
    					    IRI.create("http://solomon.cs.iastate.edu/ontologies/KIDS.owl#SignalDomain") },
    			new IRI[] { IRI.create("http://solomon.cs.iastate.edu/ontologies/KIDS.owl#isAppliedByDetector"), 
    					    IRI.create("http://solomon.cs.iastate.edu/ontologies/KIDS.owl#Detector") },
    			//new IRI[] { IRI.create("http://solomon.cs.iastate.edu/ontologies/KIDS.owl#isDetectedBy"), 
    			//		    IRI.create("http://solomon.cs.iastate.edu/ontologies/KIDS.owl#Detector") },
    			new IRI[] { IRI.create("http://solomon.cs.iastate.edu/ontologies/KIDS.owl#isEvaluableWithDataset"), 
    					    IRI.create("http://solomon.cs.iastate.edu/ontologies/KIDS.owl#Dataset") },
    			new IRI[] { IRI.create("http://solomon.cs.iastate.edu/ontologies/KIDS.owl#isProducedBy"), 
    					    IRI.create("http://solomon.cs.iastate.edu/ontologies/KIDS.owl#Event") },
    			new IRI[] { IRI.create("http://solomon.cs.iastate.edu/ontologies/KIDS.owl#isRepresentedBy"), 
    					    IRI.create("http://solomon.cs.iastate.edu/ontologies/KIDS.owl#SignalDomainRepresentation") },
    			new IRI[] { IRI.create("http://solomon.cs.iastate.edu/ontologies/KIDS.owl#SignalInManifestation"), 
    					    IRI.create("http://solomon.cs.iastate.edu/ontologies/KIDS.owl#SignalManifestation") }
    	};
    	
    	for (IRI[] args : checkListAtLeastOne){
    		if (args.length != 2){
    			logme.error("Initialization error for argument array in testSignalRelations method.");
    			continue;
    		}
    		SubjectHasAtLeastOneObjectOfClassViaPredicate(
    				signal, 
    				odf.getOWLObjectProperty(args[0]),
    				odf.getOWLClass(args[1])
    				);
    	}
    	
    }
    
    /** Methods below this point should be moved to their own library. */
    
    /**
     * @throws KIDSConfigurationException
     */
    public Set<OWLNamedIndividual> SubjectHasAtLeastOneObjectOfClassViaPredicate(
    		OWLNamedIndividual subject, OWLObjectProperty predicate,
    		OWLClass objectClass) throws KIDSConfigurationException{
    	Set<OWLNamedIndividual> valSet = r.getObjectPropertyValues(subject, predicate).getFlattened();
    	Set<OWLNamedIndividual> clsSet = r.getInstances(odf.getOWLObjectIntersectionOf(
    													    odf.getOWLObjectOneOf(valSet),
    													    objectClass), false).getFlattened();
    	if (valSet.size() == 0){
    		throw new KIDSConfigurationException(String.format("Subject <%s> has no Objects for Predicate <%s>",
    				                                           subject.getIRI(),
    				                                           predicate.getIRI()));
    	} else if (clsSet.size() == 0){
    		StringBuilder xMsg = new StringBuilder();
    		xMsg.append(String.format("Subject<%s> has no Objects of Class <%s> for Predicate <%s>. Non-class values are: \n"));
    		for (OWLNamedIndividual ind : valSet){
    			xMsg.append(String.format(" * %s", ind.getIRI()));
    		}
    		throw new KIDSConfigurationException(xMsg.toString());
    	}
    	/* Check that class is correct */
    	return (clsSet);
    }
}
