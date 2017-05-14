/**
 * 
 */
package net.strasnet.kids.test;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyID;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.util.SimpleIRIMapper;

import net.strasnet.kids.KIDSOntologyDatatypeValuesException;
import net.strasnet.kids.KIDSOntologyObjectValuesException;
import net.strasnet.kids.KIDSOracle;
import net.strasnet.kids.test.testKIDSOntologySetup;

/**
 * @author cstras
 *
 */
public class testKIDSOracle {

	/**
	 * @throws java.lang.Exception
	 */
	private static final Logger logme = LogManager.getLogger(testKIDSOracle.class.getName());
	
	// If an entry is mapped to 'True', remove it after testing; if it is 'False', leave it.
	public static final Map<File, Boolean> tempFilesCreated = new HashMap<File, Boolean>();
	public static final String defaultTempFileLocation = "./tmp";
	public static File tempDir = null;
	public static Boolean removeTempDir = false; // If true, will remove the (created) temp dir after tests, if possible.
	
	// Generally we want to use the current TBOX file:
	public static File defaultTBOXFileLoc = new File("./resources/ontologies/KIDS/KIDS-TBOX.owl");
	
	// Establish a 'standard' ABOX file for testing:
	public static File defaultABOXFileLoc = new File("./resources/ontologies/KIDS/TESTABOX.owl");
	
	// Use a consistent IRI for the test abox ontology:
	public static final IRI DEFAULTABOXIRI = IRI.create("http://www.semantiknit.com/ontologies/kids-abox-test.owl");
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {

		/* First, find or create a temp-file location for created ontologies. */
		LinkedList<File> altStack = new LinkedList<File>();
		altStack.push(new File(System.getProperty("java.io.tmpdir")));
		altStack.push(new File(defaultTempFileLocation)); // Always push last, so it's the first one tried.

		while (tempDir == null && !altStack.isEmpty()){
			File tempFolder = altStack.pop();
		
			if (tempFolder.exists()){
				logme.debug(String.format("Temp location %s already exists, checking properties...", tempFolder.getCanonicalPath()));
				if (!tempFolder.isDirectory()){
					logme.debug(String.format("Temp location %s is not a directory.", tempFolder.getCanonicalPath()));
					tempFolder = null;
				}
			} else {
				logme.debug(String.format("Creating temp location %s...",tempFolder.getCanonicalPath()));
				if (!tempFolder.mkdirs()){
					logme.debug(String.format("Could not create temporary location %s.",tempFolder.getCanonicalPath()));
					tempFolder = null;
				} else {
					// Only remove the temp dir if we create it.
					removeTempDir = true;
				}
			}
			tempDir = tempFolder.getCanonicalFile();
		}
		
		if (tempDir == null){
			logme.error("Could not find writable temp file location; cannot run tests (set value at command line and try again, e.g. '-Djava.io.tmpdir=<path>'.");
			System.exit(1);
		}
		logme.info(String.format("Temporary files located in folder %s.", tempDir.getCanonicalPath()));
		
		/* Next, ensure the TBOX location is valid */
		if (!defaultTBOXFileLoc.exists()){
			logme.error(String.format("Could not find TBOX file distributed with project (%s) - broken distribution?",defaultTBOXFileLoc.getAbsolutePath()));
			tearDownAfterClass();
			System.exit(1);
		}
		defaultTBOXFileLoc = defaultTBOXFileLoc.getCanonicalFile();
	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		// Remove temp-created files
		for (File f : tempFilesCreated.keySet()){
			if (tempFilesCreated.get(f)){
				logme.debug(String.format("Removing file %s",f.getCanonicalPath()));
			} else {
				logme.info(String.format("File %s left for debugging.",f.getCanonicalPath()));
				removeTempDir = false;
			}
		}
		
		if (removeTempDir){
			String[] remainingFiles = tempDir.list();
			if (remainingFiles == null){
				logme.error(String.format("Could not remove %s: not a directory!",tempDir));
			} else if (remainingFiles.length != 0){
				logme.warn(String.format("Could not remove %s: not empty!",tempDir));
				logme.debug(String.format("Files remaining include %s...",remainingFiles[0]));
			} else {
				if (tempDir.delete()){
					logme.info(String.format("Removed temp folder %s.",tempDir.getCanonicalPath()));
				} else {
					logme.warn(String.format("Could not remove temp folder %s.",tempDir.getCanonicalPath()));
				}
			}
		}
	}

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}
	
	/* *** Support Classes *** */
	
	/**
	 * 
	 * @param ABOXIRI
	 * @param ABOXLOCIRI
	 * @param TBOXIRI
	 * @param TBOXLOCIRI
	 * @return - A list of two mappings: ABOXIRI -> ABOXLOCIRI, and TBOXIRI -> TBOXLOCIRI
	 */
	public List<SimpleIRIMapper> getMapper(IRI ABOXIRI, IRI ABOXLOCIRI, IRI TBOXIRI, IRI TBOXLOCIRI){
		List<SimpleIRIMapper> toReturn = new LinkedList<SimpleIRIMapper>();
		toReturn.add(new SimpleIRIMapper(ABOXIRI, ABOXLOCIRI));
		toReturn.add(new SimpleIRIMapper(TBOXIRI, TBOXLOCIRI));
		return toReturn;
	}
	
	/**
	 * 
	 * @return - A location available to create a new temporary ABOX file.
	 * @throws IOException if a new temp ABOX file cannot be created.
	 */
	public IRI getTempABOXFileLoc(){
		
		try {
			File newFile = File.createTempFile("KIDSAbox-test-", ".owl", tempDir);
			logme.debug(String.format("Created new temporary ABOX file: %s", newFile.getCanonicalPath()));
			tempFilesCreated.put(newFile, true);
			return IRI.create(newFile);
		} catch (IOException e){
			logme.error("Could not create new temporary ontology file: ",e);
			return null;
		}
		
	}
	
	/**
	 * Create the 'default' temporary kids ABOX file.
	 * 
	 * @param o - The oracle to create the ABOX through;
	 * 
	 * @return - An IRI pointing to the file location.
	 */
	public IRI createTempKIDS(KIDSOracle o){
		IRI testABOXIRI = DEFAULTABOXIRI;
		IRI testTBOXIRI = IRI.create(KIDSOracle.DEFAULTTBOXIRI);
		IRI testABOXLocIRI = getTempABOXFileLoc();
		IRI testTBOXLocIRI = IRI.create(defaultTBOXFileLoc);
		
		IRI toReturn = null;
		
		List<SimpleIRIMapper> myMap = getMapper(testABOXIRI, testABOXLocIRI, testTBOXIRI, testTBOXLocIRI);
		
		try {
			o.createKIDS(testABOXIRI, testTBOXIRI, myMap);
			toReturn = testABOXLocIRI;
		} catch (OWLOntologyCreationException | OWLOntologyStorageException e) {
			e.printStackTrace();
			fail(String.format("Exception during ontology creation: %s", e));
		}
		
		return toReturn;
		
	}
	
	/**
	 * Load the default test abox, pre-populated with individuals and relations.
	 * 
	 * @return - an instance of the KIDSOracle initialized with the ABOX.
	 */
	public KIDSOracle loadDefaultABOX(){
		KIDSOracle myTestO = new KIDSOracle();
		
		List<SimpleIRIMapper> m = getMapper( DEFAULTABOXIRI, IRI.create(defaultABOXFileLoc),
					   IRI.create(KIDSOracle.DEFAULTTBOXIRI), 
					   IRI.create(defaultTBOXFileLoc));

		try {
			myTestO.loadKIDS(DEFAULTABOXIRI, m);
			return myTestO;
		} catch (OWLOntologyCreationException e) {
			e.printStackTrace();
			fail(String.format("Exception during abox creation: %s", e));
		}
		return null;
	} 

	/* *** Tests *** */

	/**
	 * Test method for {@link net.strasnet.kids.KIDSOracle#createKIDS(org.semanticweb.owlapi.model.IRI, org.semanticweb.owlapi.model.IRI, java.util.List)}.
	 */
	@Test
	public final void testCreateKIDSIRIIRIListOfSimpleIRIMapper() {
		// Create a temporary ontology in the temp folder. A created ontology should have an ontology ID, correctly
		// import the TBOX.
		
		KIDSOracle myTestO = new KIDSOracle();
		IRI testABOXLocIRI = createTempKIDS(myTestO);
		
		tempFilesCreated.put(new File(testABOXLocIRI.toURI()), false);
			
		assertTrue(myTestO.getABOXIRI().equals(DEFAULTABOXIRI));
		assertTrue(myTestO.getABOXLocIRI().equals(testABOXLocIRI));
		assertTrue(myTestO.getTBOXIRI().equals(IRI.create(KIDSOracle.DEFAULTTBOXIRI)));
		assertTrue(myTestO.getTBOXLocIRI().equals(IRI.create(defaultTBOXFileLoc)));
		
		// Now, save the created ontology, re-load it, and ensure the properties are still valid:
		try {
			myTestO.saveKIDS();
		} catch (OWLOntologyStorageException e) {
			fail(String.format("Could not write ABOX file %s via Oracle: %s", testABOXLocIRI.toString(), e));
			e.printStackTrace();
		}

		tempFilesCreated.put(new File(testABOXLocIRI.toURI()), true);
		
	}

	/**
	 * Test method for {@link net.strasnet.kids.KIDSOracle#loadKIDS(org.semanticweb.owlapi.model.IRI, org.semanticweb.owlapi.model.IRI, java.util.List)}.
	 */
	@Test
	public final void testLoadKIDSIRIIRIListOfSimpleIRIMapper() {
		// Create a temporary ontology in the temp folder. A created ontology should have an ontology ID, correctly
		// import the TBOX.
		
		KIDSOracle myTestO = new KIDSOracle();
		IRI testABOXLocIRI = createTempKIDS(myTestO);
		tempFilesCreated.put(new File(testABOXLocIRI.toURI()), false);

		OWLOntologyID testOntologyID = myTestO.getOntology().getOntologyID();
			
		// Now, save the created ontology, re-load it, and ensure the properties are still valid:
		try {
			myTestO.saveKIDS();
		} catch (OWLOntologyStorageException e) {
			fail(String.format("Could not write ABOX file %s via Oracle: %s", testABOXLocIRI.toString(), e));
			e.printStackTrace();
		}

		// Create an entirely new KIDSOracle to load it again:
		KIDSOracle testLoadO = new KIDSOracle();
		try {
			testLoadO.loadKIDS(DEFAULTABOXIRI, 
							   IRI.create(KIDSOracle.DEFAULTTBOXIRI), 
							   getMapper(
									   DEFAULTABOXIRI, 
									   testABOXLocIRI, 
									   IRI.create(KIDSOracle.DEFAULTTBOXIRI), 
									   IRI.create(defaultTBOXFileLoc)));
			
			// The re-loaded oracle should match all the location tests:
			assertTrue(testLoadO.getABOXIRI().equals(DEFAULTABOXIRI));
			assertTrue(testLoadO.getABOXLocIRI().equals(testABOXLocIRI));
			assertTrue(testLoadO.getTBOXIRI().equals(IRI.create(KIDSOracle.DEFAULTTBOXIRI)));
			assertTrue(testLoadO.getTBOXLocIRI().equals(IRI.create(defaultTBOXFileLoc)));
			
			// It should have a valid OntologyID, and version ID:
			assertTrue(testLoadO.getOntology().getOntologyID().equals(testOntologyID));
			
			// It should have a properly imported TBOX:
			assertTrue(testLoadO.TBOXImported());
			
			// It should have an initialized, functional reasoner:
			// TODO: Add test for reasoner here:
		
		} catch (OWLOntologyCreationException e) {
			fail(String.format("Could not re-load ABOX file %s: %s", testABOXLocIRI.toString(), e));
			e.printStackTrace();
		}
		tempFilesCreated.put(new File(testABOXLocIRI.toURI()), true);
	}

	/**
	 * Test method for {@link net.strasnet.kids.KIDSOracle#getMaliciousSignals()}.
	 */
	@Test
	public final void testGetMaliciousSignals() {
		// First, load the populated test abox:
		KIDSOracle testO = loadDefaultABOX();
		
		// Call the get malicious signals method and evaluate:
		List<IRI> sigList = testO.getMaliciousSignals();
		
		assertTrue(sigList.size() == 1);
		assertTrue(sigList.get(0).equals(IRI.create(DEFAULTABOXIRI.toString() + "#s1")));

		
	}

	/**
	 * Test method for {@link net.strasnet.kids.KIDSOracle#getIndividualSet(org.semanticweb.owlapi.model.OWLClassExpression)}.
	 */
	@Test
	public final void testGetIndividualSet() {
		// First, load the populated test abox:
		KIDSOracle testO = loadDefaultABOX();
		
		Set<OWLNamedIndividual> individuals = testO.getIndividualSet(testO.getOwlDataFactory().getOWLClass(
				IRI.create(KIDSOracle.DEFAULTTBOXIRI.toString() + "#SignalValue")));
		
		int count = 0;
		for (OWLNamedIndividual i : individuals){
			assertTrue(i.getIRI().equals(IRI.create(DEFAULTABOXIRI.toString() + "#count10")));
			count++;
		}
		
		assertTrue(count == 1);
	}

	/**
	 * Test method for {@link net.strasnet.kids.KIDSOracle#getIndividuals(org.semanticweb.owlapi.model.OWLClassExpression)}.
	 */
	@Test
	public final void testGetIndividuals() {
		// First, load the populated test abox:
		KIDSOracle testO = loadDefaultABOX();
		
		Iterator<OWLNamedIndividual> individuals = testO.getIndividuals(testO.getOwlDataFactory().getOWLClass(
				IRI.create(KIDSOracle.DEFAULTTBOXIRI.toString() + "#SignalValue")));
		
		int count = 0;
		while (individuals.hasNext()){
			OWLNamedIndividual i = individuals.next();
			assertTrue(i.getIRI().equals(IRI.create(DEFAULTABOXIRI.toString() + "#count10")));
			count++;
		}
		
		assertTrue(count == 1);
	}

	/**
	 * Test method for {@link net.strasnet.kids.KIDSOracle#getSignalValue(org.semanticweb.owlapi.model.OWLNamedIndividual)}.
	 * @throws KIDSOntologyDatatypeValuesException 
	 */
	@Test
	public final void testGetSignalValue() throws KIDSOntologyDatatypeValuesException {
		// First, load the populated test abox:
		KIDSOracle testO = loadDefaultABOX();
		
		// Call the get malicious signals method and evaluate:
		String sigValue = testO.getSignalValue(IRI.create(DEFAULTABOXIRI.toString() + "#s1"));
		
		assertTrue(sigValue == "10");
	}

	/**
	 * Test method for {@link net.strasnet.kids.KIDSOracle#getSignalDomain(org.semanticweb.owlapi.model.OWLNamedIndividual)}.
	 * @throws KIDSOntologyObjectValuesException 
	 */
	@Test
	public final void testGetSignalDomain() throws KIDSOntologyObjectValuesException {
		// First, load the populated test abox:
		KIDSOracle testO = loadDefaultABOX();
		
		// Call the get malicious signals method and evaluate:
		IRI sigValue = testO.getSignalDomain(IRI.create(DEFAULTABOXIRI.toString() + "#s1"));
		
		assertTrue(sigValue.equals(IRI.create(testO.DEFAULTTBOXIRI.toString() + "#uniqueSourceIPAddressCount")));
	}

	/**
	 * Test method for {@link net.strasnet.kids.KIDSOracle#getValidFileURI(java.io.File)}.
	 * @throws URISyntaxException 
	 * @throws IOException 
	 */
	@Test
	public final void testGetValidFileURI() throws URISyntaxException, IOException {
		KIDSOracle testO = loadDefaultABOX();

		// Get the current working directory:
		String cwd = System.getProperty("user.dir");

		// Ensure that a valid URI is returned for a relative filename:
		String filename1 = "./test.txt";
		String canFN1 = "file://" + cwd + "/test.txt";
		URI testURI = URI.create(canFN1);
		URI oracleURI = testO.getValidFileURI(new File(filename1));
		
		assertTrue(testURI.equals(oracleURI));

		// Ensure that a valid URI is returned for an absolute filename:
		String filename2 = cwd + "/test.txt";
		String canFN2 = "file://" + filename2;
		URI testURI2 = URI.create(canFN2);
		URI oracleURI2 = testO.getValidFileURI(new File(filename2));
		
		assertTrue(testURI2.equals(oracleURI2));
	}

	/**
	 * Test method for {@link net.strasnet.kids.KIDSOracle#getEvaluableDetectorsForEventTimePeriod(org.semanticweb.owlapi.model.IRI, org.semanticweb.owlapi.model.IRI)}.
	 */
	@Test
	public final void testGetEvaluableDetectorsForEventTimePeriod() {
		KIDSOracle testO = loadDefaultABOX();
		
		IRI evIRI = IRI.create(DEFAULTABOXIRI.toString() + "#t1");
		IRI tpIRI = IRI.create(DEFAULTABOXIRI.toString() + "#tp1");
		
		// Check time period:
		Set<IRI> detectors = testO.getEvaluableDetectorsForEventTimePeriod(evIRI, tpIRI);
		
		assertTrue(detectors.size() == 1);
		assertTrue(detectors.iterator().next().equals(IRI.create(DEFAULTABOXIRI.toString() + "#snort1")));
	}

}
