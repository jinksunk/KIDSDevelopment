/**
 * 
 */
package net.strasnet.kids.test;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.apache.log4j.LogManager;
import org.junit.BeforeClass;
import org.junit.Test;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.ClassExpressionType;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataHasValue;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectHasValue;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.model.PrefixManager;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.util.DefaultPrefixManager;
import org.semanticweb.owlapi.util.SimpleIRIMapper;

import com.clarkparsia.pellet.owlapiv3.PelletReasonerFactory;

import net.strasnet.kids.KIDSOracle;

/**
 * @author cstras
 * 
 * This class should serve as a base class that handles common test setup tasks, e.g.:
 * 
 * * Initialize the KIDSOracle ;
 * * Prompt the user for required parameters ;
 *
 */
public class testKIDSTBOX {
	public static String DefaultTBOXIRI = KIDSOracle.DEFAULTTBOXIRI;
	public static String DefaultTBOXLocation = "./resources/ontologies/KIDS/KIDS-TBOX.owl";
	public static IRI KIDS = IRI.create(DefaultTBOXIRI);
	
	private static OWLOntology o;
	private static OWLDataFactory df;
	private static OWLReasoner r;
	private static OWLOntologyManager om;
	private static PrefixManager p;
	
	private Map<OWLClass, Set<OWLNamedIndividual>> instanceSets = new HashMap<OWLClass, Set<OWLNamedIndividual>>();

	private static final org.apache.log4j.Logger logme = LogManager.getLogger(testKIDSTBOX.class.getName());
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		om = OWLManager.createOWLOntologyManager();
		DefaultTBOXLocation = new File(new File(DefaultTBOXLocation).getCanonicalPath()).toURI().toString();
		om.getIRIMappers().add(new SimpleIRIMapper(IRI.create(DefaultTBOXLocation),
														IRI.create(DefaultTBOXIRI)));
		try {

			logme.info(String.format("[setupBeforeClass] Loading TBOX %s from location %s ", DefaultTBOXIRI, DefaultTBOXLocation));

			o = om.loadOntologyFromOntologyDocument(IRI.create(DefaultTBOXLocation));
			df = om.getOWLDataFactory();
			p = new DefaultPrefixManager(DefaultTBOXIRI+"#");

			// Initialize a reasoner:
			PelletReasonerFactory rf = new PelletReasonerFactory();
			r = rf.createNonBufferingReasoner(o);
			r.precomputeInferences();
			assert r.isConsistent();
		} catch (OWLOntologyCreationException e) {
			System.out.println("Failed to load ontology: " + e);
			e.printStackTrace();
			throw e;
		}
	}
	
	private Set<OWLNamedIndividual> getInstances(OWLClass vclass){
		if (!instanceSets.containsKey(vclass)){
		    instanceSets.put(vclass, r.getInstances(vclass, false).getFlattened());
		}
		return instanceSets.get(vclass);
	}
	
	/**
	 * 
	 * @param subject - The class we are checking properties of
	 * @param toCheck - The set of class expressions (equivalent or sub) to evaluate
	 * @param cetype  - The type of class expression we need (e.g. DATAHASVALUE)
	 * @param valueClass - The class that the individual must be a member of.
	 * @return
	 */
	private boolean checkForIndividualInClassExpression(OWLClass subject, 
			Set<? extends OWLClassAxiom> toCheck, ClassExpressionType cetype, OWLClass valueClass){

		Set<OWLNamedIndividual> candidates = this.getInstances(valueClass);
		boolean toReturn = false;

		// For each equivalent class definition, check that at least one of them includes a 
		for (OWLClassAxiom eq : toCheck){
			Set<OWLClassExpression> ceset = eq.getNestedClassExpressions();
			for (OWLClassExpression ce : ceset){
				if (ce.getClassExpressionType() == cetype){
					Set<OWLNamedIndividual> ov = ((OWLObjectHasValue)ce).getIndividualsInSignature();
					for (OWLNamedIndividual ovi : ov){
						// Determine if the individual is in the Constraint or Domain class:
						if (candidates.contains(ovi)){
							toReturn = true;
						}
					}
				}
			}
		}
		return toReturn;
	}
	
	/**
	 * 
	 * @param subject - The class we are checking properties of
	 * @param toCheck - The set of class expressions (equivalent or sub) to evaluate
	 * @param targetProperty - The data property that must be present
	 * @return
	 */
	private boolean checkForDataValueInClassExpression(OWLClass subject, 
			Set<? extends OWLClassAxiom> toCheck, OWLDataProperty targetProperty){

		boolean toReturn = false;

		// For each equivalent class definition, check that at least one of them includes a 
		for (OWLClassAxiom eq : toCheck){
			Set<OWLClassExpression> ceset = eq.getNestedClassExpressions();
			for (OWLClassExpression ce : ceset){
				if (ce.getClassExpressionType() == ClassExpressionType.DATA_HAS_VALUE){
					Set<OWLDataProperty> ov = ((OWLDataHasValue)ce).getDataPropertiesInSignature();
					for (OWLDataProperty ovi : ov){
						// Determine if the individual is in the Constraint or Domain class:
						if (targetProperty.equals(ovi)){
							toReturn = true;
						}
					}
				}
			}
		}
		return toReturn;
	}
	
	/**
	 * 
	 * @param subject - The class we are checking properties of
	 * @param toCheck - The set of class expressions (equivalent or sub) to evaluate
	 * @param targetProperty - The object property that must be present
	 * @return
	 */
	private boolean checkForIndividualPropertyDefinition(OWLNamedIndividual subject, 
			OWLObjectProperty targetProperty){

		Set<OWLObjectPropertyAssertionAxiom> toCheck2 = o.getObjectPropertyAssertionAxioms(subject);
		for (OWLObjectPropertyAssertionAxiom opaa : toCheck2){
			Set<OWLObjectProperty> toCheck3 = opaa.getObjectPropertiesInSignature();
			if (toCheck3.contains(targetProperty)){
				return true;
			}
		}
		return false;
	}

	/**
	 * 
	 * @param subject - The class we are checking properties of
	 * @param toCheck - The set of class expressions (equivalent or sub) to evaluate
	 * @param targetProperty - The object property that must be present
	 * @return
	 */
	private boolean checkForIndividualDataDefinition(OWLNamedIndividual subject, 
			OWLDataProperty targetProperty){
		Set<OWLDataPropertyAssertionAxiom> toCheck2 = o.getDataPropertyAssertionAxioms(subject);
		for (OWLDataPropertyAssertionAxiom dpaa : toCheck2){
			Set<OWLDataProperty> toCheck3 = dpaa.getDataPropertiesInSignature();
			if (toCheck3.contains(targetProperty)){
				return true;
			}
		}
		return false;
	}
	
	
	/**
	 * Test that all subclasses of Signal have a domain and constraint defined for them.
	 */
	@Test
	public void testSignalSubclassEquivalences(){
		// Test set:
		Map<OWLClass, Boolean> resultSet = new HashMap<OWLClass, Boolean>();
		
		// Get the set of all subclasses of signal:
		Set<OWLClass> cset = r.getSubClasses(df.getOWLClass("Signal",p), false).getFlattened();
		logme.debug(String.format("Found %d subclasses of Signal to evaluate.", cset.size()));
		
		for (OWLClass c : cset){
			// Skip bottom:
			if (c.isBottomEntity()){
				continue;
			}
			// For each equivalent class definition, check that at least one of them includes a 
			Set<? extends OWLClassAxiom> eqCls = o.getEquivalentClassesAxioms(c);

			resultSet.put(c, false);
			boolean constraintSpecified = checkForIndividualInClassExpression(c, eqCls, 
					ClassExpressionType.OBJECT_HAS_VALUE, df.getOWLClass("SignalConstraint",p));
			boolean domainSpecified = checkForIndividualInClassExpression(c, eqCls,
					ClassExpressionType.OBJECT_HAS_VALUE, df.getOWLClass("SignalDomain",p));

			resultSet.put(c, constraintSpecified && domainSpecified);
			logme.debug(String.format(" --> Signal Equivalence Class Test: %55s = %6b (constraint: %6b, domain: %6b)",
					c.getIRI().getShortForm(), constraintSpecified && domainSpecified,
					constraintSpecified, domainSpecified));
		}
		
		for (OWLClass cresult : resultSet.keySet() ){
			assertTrue(resultSet.get(cresult));
		}
	}
	/**
	 * Test that all subclasses of Detector specify a Detector Syntax value, and
	 * an implementation class value as equivalent classes. Also ensure that at least one
	 * SignalManifestation value is specified as a subclass specification.
	 */
	@Test
	public void testDetectorSubclassEquivalences(){
		// Test set:
		Map<OWLClass, Boolean> resultSet = new HashMap<OWLClass, Boolean>();
		
		// Get the set of all subclasses of signal:
		Set<OWLClass> dset = r.getSubClasses(df.getOWLClass("Detector",p), false).getFlattened();
		logme.debug(String.format("Found %d subclasses of Detector to evaluate.", dset.size()));
		
		Set<OWLNamedIndividual> syntaxes = r.getInstances(df.getOWLClass("DetectorSyntax", p), false).getFlattened();
		Set<OWLNamedIndividual> manifestations = r.getInstances(df.getOWLClass("SignalManfiestation", p), false).getFlattened();
		OWLDataProperty impProp = df.getOWLDataProperty("hasImplementationClass",p);

		for (OWLClass c : dset){
			// Skip bottom:
			if (c.isBottomEntity()){
				continue;
			}

			// For each equivalent class definition, check that at least one of them includes a 
			// value for implementationClass and a value for syntax.
			Set<? extends OWLClassAxiom> eqCls = o.getEquivalentClassesAxioms(c);
			Set<? extends OWLClassAxiom> subCls = o.getSubClassAxiomsForSubClass(c);

			resultSet.put(c, false);
			boolean manifestationSpecified = this.checkForIndividualInClassExpression(c, subCls, 
					ClassExpressionType.OBJECT_HAS_VALUE, df.getOWLClass("SignalManifestation",p));
			boolean syntaxSpecified = this.checkForIndividualInClassExpression(c, eqCls, 
					ClassExpressionType.OBJECT_HAS_VALUE, df.getOWLClass("DetectorSyntax",p));
			boolean implementationSpecified = this.checkForDataValueInClassExpression(c, eqCls, impProp);

			resultSet.put(c, syntaxSpecified && implementationSpecified && manifestationSpecified);
			logme.debug(String.format(" --> Detector Subclass Test: %55s = %6b (syntax: %6b, implementation: %6b, manifestation: %6b)",
					c.getIRI().getShortForm(), syntaxSpecified && implementationSpecified && manifestationSpecified,
					syntaxSpecified, implementationSpecified, manifestationSpecified));
		}
		
		for (OWLClass cresult : resultSet.keySet() ){
			assertTrue(resultSet.get(cresult));
		}
	}
	
	/** DatasetViewImplementation subclasses should have an equivalent class definition specifying the 
	 * view production implementation.
	 * 
	 * Other subclasses of DatasetView should specify the contexts they expose as subclass definitions.
	 * 
	 * All DatasetView instances should be a member of a DatasetViewImplementation subclass.
	 * 
	 */
	@Test
	public void testDatasetViewDefinitions(){
		// Test set:
		Map<OWLEntity, Boolean> resultSet = new HashMap<OWLEntity, Boolean>();
		
		// Get the set of all subclasses of DatasetViewImplementation, and the remainig subsets of DatasetView:
		Set<OWLClass> dviset = r.getSubClasses(df.getOWLClass("DatasetViewImplementationSubclasses",p), false).getFlattened();
		Set<OWLClass> dvset = r.getSubClasses(df.getOWLClass("DatasetView",p), false).getFlattened();
		dvset.removeAll(dviset);
		Set<OWLNamedIndividual> dvindset = r.getInstances(df.getOWLClass("DatasetView",p), false).getFlattened();

		logme.debug(String.format("Found %d subclasses of DatasetViewImplementation to evaluate.", dviset.size()));
		logme.debug(String.format("Found %d subclasses of DatasetView (-DatasetViewImplementation) to evaluate.", dvset.size()));
		logme.debug(String.format("Found %d individuals of type DatasetView to evaluate.", dvindset.size()));
		
		Set<OWLNamedIndividual> syntaxes = r.getInstances(df.getOWLClass("DetectorSyntax", p), false).getFlattened();
		Set<OWLNamedIndividual> manifestations = r.getInstances(df.getOWLClass("SignalManfiestation", p), false).getFlattened();
		OWLDataProperty impProp = df.getOWLDataProperty("viewProductionImplementation",p);

		for (OWLClass c : dviset){
			// Skip bottom:
			if (c.isBottomEntity()){
				continue;
			}
			Set<? extends OWLClassAxiom> eqCls = o.getEquivalentClassesAxioms(c);

			// For each equivalent class definition, check that at least one of them includes a 
			// value for implementationClass and a value for syntax.
			boolean implementationSpecified = this.checkForDataValueInClassExpression(c, eqCls, impProp);
			resultSet.put(c, implementationSpecified);
			logme.debug(String.format(" --> DatasetViewImplementation Subclass Test: %55s = %6b (implementation: %6b)",
					c.getIRI().getShortForm(), implementationSpecified, implementationSpecified));
		}

		for (OWLClass c : dvset){
			// Skip bottom:
			if (c.isBottomEntity()){
				continue;
			}
			Set<? extends OWLClassAxiom> subCls = o.getSubClassAxiomsForSubClass(c);

			// For each equivalent class definition, check that at least one of them includes a 
			// value for implementationClass and a value for syntax.
			boolean exposedContextsSpecified = this.checkForIndividualInClassExpression(c, subCls, 
					ClassExpressionType.OBJECT_HAS_VALUE, df.getOWLClass("SignalDomainContext",p));
			resultSet.put(c, exposedContextsSpecified);
			logme.debug(String.format(" --> DatasetView Subclass Test: %55s = %6b (exposedContexts: %6b)",
					c.getIRI().getShortForm(), exposedContextsSpecified, exposedContextsSpecified));
		}
		
		for (OWLNamedIndividual i : dvindset){
			boolean individualInClass = 
					r.isEntailed(df.getOWLClassAssertionAxiom(df.getOWLClass("DatasetViewImplementationSubclasses",p), i));

			resultSet.put(i, individualInClass);
			logme.debug(String.format(" --> Datasetview Individual Test: %55s = %6b (memberOfImplementation: %6b)",
					i.getIRI().getShortForm(), individualInClass, individualInClass));
		}
		
		for (OWLEntity cresult : resultSet.keySet() ){
			assertTrue(resultSet.get(cresult));
		}
	}
	
	
	/**
	 * Each DatasetLabel subclass has an equivalent class defined by DatasetLabelImplementation 
	 * dataValue specification.
	 */
	@Test
	public void testDatasetLabelDefinitions(){
		// Test set:
		Map<OWLClass, Boolean> resultSet = new HashMap<OWLClass, Boolean>();
		
		// Get the set of all subclasses of signal:
		Set<OWLClass> dset = r.getSubClasses(df.getOWLClass("DatasetLabel",p), false).getFlattened();
		logme.debug(String.format("Found %d subclasses of DatasetLabel to evaluate.", dset.size()));
		
		OWLDataProperty impProp = df.getOWLDataProperty("hasLabelFunction",p);

		for (OWLClass c : dset){
			// Skip bottom:
			if (c.isBottomEntity()){
				continue;
			}

			// For each equivalent class definition, check that at least one of them includes a 
			// value for implementationClass and a value for syntax.
			Set<? extends OWLClassAxiom> eqCls = o.getEquivalentClassesAxioms(c);
			boolean implementationSpecified = this.checkForDataValueInClassExpression(c, eqCls, impProp);

			resultSet.put(c, implementationSpecified);
			logme.debug(String.format(" --> DatasetLabel Subclass Test: %55s = %6b (implementation: %6b)",
					c.getIRI().getShortForm(), implementationSpecified, implementationSpecified));
		}
		
		for (OWLClass cresult : resultSet.keySet() ){
			assertTrue(resultSet.get(cresult));
		}
	}

	/**
	 * Every response individual must include subclass definitions that indicate resources that *can* be used, 
	 * and those that are *required*.
	 */
	@Test
	public void testResponseDefinitions(){
		// Test set:
		Map<OWLNamedIndividual, Boolean> resultSet = new HashMap<OWLNamedIndividual, Boolean>();
		
		// Get the set of all subclasses of signal:
		Set<OWLNamedIndividual> dset = r.getInstances(df.getOWLClass("Response",p), false).getFlattened();
		logme.debug(String.format("Found %d instances of Response to evaluate.", dset.size()));
		

		for (OWLNamedIndividual i : dset){
			// For each individual, check that at least one of them includes a 
			// value for the required property:
			boolean requiresResourcesSatisfied = 
					this.checkForIndividualPropertyDefinition(i, df.getOWLObjectProperty("requiresResource",p));
			boolean usesResourcesSatisfied = 
					this.checkForIndividualPropertyDefinition(i, df.getOWLObjectProperty("usesResource",p));

			resultSet.put(i, requiresResourcesSatisfied && usesResourcesSatisfied);
			logme.debug(String.format(" --> Response Definition Test: %55s = %6b (requires: %6b, uses: %6b)",
					i.getIRI().getShortForm(), requiresResourcesSatisfied && usesResourcesSatisfied, 
					requiresResourcesSatisfied, usesResourcesSatisfied));
		}
		
		for (OWLNamedIndividual cresult : resultSet.keySet() ){
			assertTrue(resultSet.get(cresult));
		}
	}

	
	/**
	 * Every SignalDomain subclass should have a representative individual in the TBOX.
	 * SignalDomain individuals should *not* specify the contexts that provide them; these should be specified
	 * as subclass definitions of the signal context class.
	 */
	@Test
	public void testSignalDomainDefinitions(){
		// Test set:
		Map<OWLEntity, Boolean> resultSet = new HashMap<OWLEntity, Boolean>();
		
		// Get the set of all subclasses of signal:
		Set<OWLClass> cset = r.getSubClasses(df.getOWLClass("SignalDomain",p), false).getFlattened();
		logme.debug(String.format("Found %d subclasses of SignalDomain to evaluate.", cset.size()));
		

	    // Every SignalDomain subclass should have a representative individual in the TBOX.
		for (OWLClass c : cset){
			// For each subclass, check that at least one individual is a member:
			boolean representativeInstance = r.getInstances(c, true).getFlattened().size() > 0;

			resultSet.put(c, representativeInstance);
			logme.debug(String.format(" --> SignalDomain Definitions Test: %55s = %6b (representativeInstance: %6b)",
					c.getIRI().getShortForm(), representativeInstance, representativeInstance));
		}

	   // SignalDomain individuals should *not* specify the contexts that provide them; these should be specified
	   // as subclass definitions of the signal context class.
		Set<OWLNamedIndividual> iset = r.getInstances(df.getOWLClass("SignalDomain",p), false).getFlattened();
		for (OWLNamedIndividual i : iset){
			boolean cleanOfContext = !this.checkForIndividualPropertyDefinition(i, 
					df.getOWLObjectProperty("isInContext",p));
			resultSet.put(i, cleanOfContext);
			logme.debug(String.format(" --> SignalDomain Definitions Test: %55s = %6b (cleanOfContextDefinition: %6b)",
					i.getIRI().getShortForm(), cleanOfContext, cleanOfContext));
		}

		for (OWLEntity cresult : resultSet.keySet() ){
			assertTrue(resultSet.get(cresult));
		}
	}

	/**
	 * Every SignalConstraint subclass should have a representative individual in the TBOX.
	 */
	@Test
	public void testSignalConstraintDefinitions(){
		// Test set:
		Map<OWLEntity, Boolean> resultSet = new HashMap<OWLEntity, Boolean>();
		
		// Get the set of all subclasses of signal:
		Set<OWLClass> cset = r.getSubClasses(df.getOWLClass("SignalConstraint",p), false).getFlattened();
		logme.debug(String.format("Found %d subclasses of SignalConstraint to evaluate.", cset.size()));
		

	    // Every SignalConstraint subclass should have a representative individual in the TBOX.
		for (OWLClass c : cset){
			// For each subclass, check that at least one individual is a member:
			boolean representativeInstance = r.getInstances(c, true).getFlattened().size() > 0;

			resultSet.put(c, representativeInstance);
			logme.debug(String.format(" --> SignalConstraint Definitions Test: %55s = %6b (representativeInstance: %6b)",
					c.getIRI().getShortForm(), representativeInstance, representativeInstance));
		}

		for (OWLEntity cresult : resultSet.keySet() ){
			assertTrue(resultSet.get(cresult));
		}
	}
	
	/**
	 * Every SignalDomainContext subclass should have a representative individual *and* specify, as subclass
	 * definitions, the signal domains it includes. 
	 */
	@Test
	public void testSignalDomainContextDefinitions(){
		// Test set:
		Map<OWLEntity, Boolean> resultSet = new HashMap<OWLEntity, Boolean>();
		
		// Get the set of all subclasses of signal:
		Set<OWLClass> cset = r.getSubClasses(df.getOWLClass("SignalDomainContext",p), false).getFlattened();
		logme.debug(String.format("Found %d subclasses of SignalDomainContext to evaluate.", cset.size()));
		

	    // Every SignalDomainContext subclass should have a representative individual in the TBOX.
		for (OWLClass c : cset){
			// For each subclass, check that at least one individual is a member:
			boolean representativeInstance = r.getInstances(c, true).getFlattened().size() > 0;
			boolean signalDomainDefined = this.checkForIndividualInClassExpression(
					c, o.getSubClassAxiomsForSubClass(c), ClassExpressionType.OBJECT_HAS_VALUE, 
					df.getOWLClass("SignalDomain",p));

			resultSet.put(c, representativeInstance && signalDomainDefined);
			logme.debug(String.format(" --> SignalDomainContext Definitions Test: %55s = %6b (representativeInstance: %6b, signalDomain: %6b)",
					c.getIRI().getShortForm(), representativeInstance&&signalDomainDefined,
					representativeInstance, signalDomainDefined));
		}

		for (OWLEntity cresult : resultSet.keySet() ){
			assertTrue(resultSet.get(cresult));
		}
	}

	
	/**
	 * Every subclass of SignalDomainRepresentation should have:
	 * * An equivalent class definition that indicates the context it is produced by, and the signal domain
	 *   it represents;
	 * * A subclass definition specifying the signal manifestation(s) that bring it into existence.
	 */
	@Test
	public void testSignalDomainRepresentationDefinitions(){
		// Test set:
		Map<OWLEntity, Boolean> resultSet = new HashMap<OWLEntity, Boolean>();
		
		// Get the set of all subclasses of signal:
		Set<OWLClass> cset = r.getSubClasses(df.getOWLClass("SignalDomainRepresentation",p), false).getFlattened();
		logme.debug(String.format("Found %d subclasses of SignalDomainContext to evaluate.", cset.size()));
		

	    // Every SignalDomainContext subclass should have a representative individual in the TBOX.
		for (OWLClass c : cset){
			Set<OWLEquivalentClassesAxiom> eqs = o.getEquivalentClassesAxioms(c);
			Set<OWLSubClassOfAxiom> subs = o.getSubClassAxiomsForSubClass(c);

			// For each subclass, check that at least one individual is a member:
			boolean producingContextDefined = this.checkForIndividualInClassExpression(
					c, eqs, ClassExpressionType.OBJECT_HAS_VALUE, df.getOWLClass("SignalDomainContext",p));
			boolean signalDomainRepresented = this.checkForIndividualInClassExpression(
					c, eqs, ClassExpressionType.OBJECT_HAS_VALUE, df.getOWLClass("SignalDomain",p));
			boolean signalManifestation = this.checkForIndividualInClassExpression(
					c, subs, ClassExpressionType.OBJECT_HAS_VALUE, df.getOWLClass("SignalManifestation",p));

			resultSet.put(c, producingContextDefined && signalDomainRepresented && signalManifestation);
			logme.debug(String.format(" --> SignalDomainRepresentation Definitions Test: %55s = %6b (context: %6b, domain: %6b, manifestation: %6b)",
					c.getIRI().getShortForm(), producingContextDefined&&signalDomainRepresented && signalManifestation,
					producingContextDefined, signalDomainRepresented, signalManifestation));
		}

		for (OWLEntity cresult : resultSet.keySet() ){
			assertTrue(resultSet.get(cresult));
		}
	}

	
	/**
	 * Every SignalValue subclass should specify at least one subclass definition indicating the DetectorSyntax
	 * individual(s) that can represent the signal value.
	 */
	@Test
	public void testSignalValueDefinitions(){
		// Test set:
		Map<OWLEntity, Boolean> resultSet = new HashMap<OWLEntity, Boolean>();
		
		// Get the set of all subclasses of signal:
		Set<OWLClass> cset = r.getSubClasses(df.getOWLClass("SignalValue",p), false).getFlattened();
		logme.debug(String.format("Found %d subclasses of SignalValue to evaluate.", cset.size()));
		

	    // Every SignalDomainContext subclass should have a representative individual in the TBOX.
		for (OWLClass c : cset){
			Set<OWLSubClassOfAxiom> subs = o.getSubClassAxiomsForSubClass(c);

			// For each subclass, check that at least one individual is a member:
			boolean syntaxSpecified = this.checkForIndividualInClassExpression(
					c, subs, ClassExpressionType.OBJECT_HAS_VALUE, df.getOWLClass("DetectorSyntax",p));

			resultSet.put(c, syntaxSpecified);
			logme.debug(String.format(" --> SignalValue Definitions Test: %55s = %6b (syntax: %6b)",
					c.getIRI().getShortForm(), syntaxSpecified, syntaxSpecified));
		}

		for (OWLEntity cresult : resultSet.keySet() ){
			assertTrue(resultSet.get(cresult));
		}
	}

	
	/**
	 * Every correlation relation individual must specify an implementation value.
	 */
	@Test
	public void testCorrelationRelationDefinitions(){
		// Test set:
		Map<OWLEntity, Boolean> resultSet = new HashMap<OWLEntity, Boolean>();
		
		// Get the set of all subclasses of signal:
		Set<OWLNamedIndividual> corInd = r.getInstances(df.getOWLClass("CorrelationRelation",p), false).getFlattened();
		logme.debug(String.format("Found %d individuals of CorrelationRelation to evaluate.", corInd.size()));
		
		for (OWLNamedIndividual c : corInd){

			// For each subclass, check that at least one individual is a member:
			boolean implementationValue = this.checkForIndividualDataDefinition(c, df.getOWLDataProperty("hasCorrelationRelationImplementation",p));

			resultSet.put(c, implementationValue);
			logme.debug(String.format(" --> CorrelationRelation Definitions Test: %55s = %6b (implementation: %6b)",
					c.getIRI().getShortForm(), implementationValue, implementationValue));
		}

		for (OWLEntity cresult : resultSet.keySet() ){
			assertTrue(resultSet.get(cresult));
		}
	}

	
	/**
	 * Every detectorSyntax individual must have a syntaxProductionImplementation data value that indicates the 
	 * Java class that can produce it.
	 */
	@Test
	public void testDetectorSyntaxDefinitions(){
		// Test set:
		Map<OWLEntity, Boolean> resultSet = new HashMap<OWLEntity, Boolean>();
		
		// Get the set of all subclasses of signal:
		Set<OWLNamedIndividual> cset = r.getInstances(df.getOWLClass("DetectorSyntax",p), false).getFlattened();
		logme.debug(String.format("Found %d instances of DetectorSyntax to evaluate.", cset.size()));
		

	    // Every SignalDomainContext subclass should have a representative individual in the TBOX.
		for (OWLNamedIndividual c : cset){

			// For each subclass, check that at least one individual is a member:
			boolean implementationDefined = this.checkForIndividualDataDefinition(c, 
					df.getOWLDataProperty("hasSyntaxProductionImplementation", p));

			resultSet.put(c, implementationDefined);
			logme.debug(String.format(" --> DetectorSyntax Definitions Test: %55s = %6b (implementation: %6b)",
					c.getIRI().getShortForm(), implementationDefined, implementationDefined));
		}

		for (OWLEntity cresult : resultSet.keySet() ){
			assertTrue(resultSet.get(cresult));
		}
	}

	
}
