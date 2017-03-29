/**
 * 
 */
package net.strasnet.kids.test.streaming;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Map;

import org.coode.owlapi.turtle.TurtleOntologyFormat;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.formats.PrefixDocumentFormat;
import org.semanticweb.owlapi.formats.TurtleDocumentFormat;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.vocab.PrefixOWLOntologyFormat;

/**
 * @author cstras
 *
 */
public class OWLAPIPrefixTest {
	private OWLOntologyManager om = null;
	private OWLDataFactory dfact = null;
	private OWLOntology a_onto = null;
	private OWLOntology b_onto = null;
	String a_prefString = "kids";
	String b_prefString = "abox";
	IRI aIRI = IRI.create("http://www.semantiknit.com/ontologies/a.owl");
	IRI bIRI = IRI.create("http://www.semantiknit.com/ontologies/b.owl");
	IRI aPrefIRI = IRI.create(aIRI.toString() + "#");
	IRI bPrefIRI = IRI.create(bIRI.toString() + "#");

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		// Create two ontologies, a and b
		om = OWLManager.createOWLOntologyManager();
		dfact = om.getOWLDataFactory();
		a_onto = om.createOntology(aIRI);
		b_onto = om.createOntology(bIRI);
	}

	@Test
	public void test() {
		System.out.println("Testing prefixes for ontology.");
		OWLClass cat = dfact.getOWLClass(IRI.create(aIRI.toString() + "#" + "Cat"));
		OWLClass dog = dfact.getOWLClass(IRI.create(bIRI.toString() + "#" + "Dog"));
		OWLAxiom ax = dfact.getOWLDeclarationAxiom(cat);
		OWLAxiom ax2 = dfact.getOWLDeclarationAxiom(dog);
		OWLAxiom ax3 = dfact.getOWLSubClassOfAxiom(cat, dog);
		om.addAxiom(a_onto, ax);
		om.addAxiom(b_onto, ax2);
		om.addAxiom(a_onto, ax3);
		om.addAxiom(b_onto, ax3);
		
		PrefixDocumentFormat turtleFormatA = new TurtleDocumentFormat();
		turtleFormatA.setDefaultPrefix(aIRI.toString() + "#");
		turtleFormatA.setPrefix("ontb", bIRI.toString() + "#");
		PrefixDocumentFormat turtleFormatB = new TurtleDocumentFormat();
		turtleFormatB.setDefaultPrefix(bIRI.toString() + "#");
		turtleFormatB.setPrefix("onta", aIRI.toString() + "#");

		System.out.println("Axiom 3: " + ax3);
		System.out.println(String.format("OWLClass toString(): %s", cat.toString()));
		System.out.println(String.format("OWLClass getNamespace(): %s", cat.getIRI().getNamespace()));
		System.out.println(String.format("OWLClass getScheme(): %s", cat.getIRI().getScheme()));
		System.out.println(String.format("OWLClass getShortForm(): %s", cat.getIRI().getShortForm()));
		System.out.println(String.format("OWLClass prefixedBy(kids:): %s", cat.getIRI().prefixedBy("kids:")));
		
		ByteArrayOutputStream abaos = new ByteArrayOutputStream();
		String adoc = "";
		ByteArrayOutputStream bbaos = new ByteArrayOutputStream();
		String bdoc = "";

		try {
			om.saveOntology(a_onto, turtleFormatA, abaos);
			adoc = abaos.toString();
			om.saveOntology(b_onto, turtleFormatB, bbaos);
			bdoc = bbaos.toString();
			System.out.println(adoc);
			//System.out.println(bbaos.toString());
		} catch (OWLOntologyStorageException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {

			OWLOntologyManager om2 = OWLManager.createOWLOntologyManager();
			OWLDataFactory dfact2 = om2.getOWLDataFactory();
			OWLOntology aontotwo = om2.loadOntologyFromOntologyDocument(new ByteArrayInputStream(adoc.getBytes()));
			PrefixDocumentFormat fa = (PrefixDocumentFormat) om2.getOntologyFormat(aontotwo);
			Map<String, String> famap = fa.getPrefixName2PrefixMap();
			System.out.println(String.format("Prefix map has %d elements.", famap.size()));
			System.out.println(String.format("Reloaded ontology has %d elements.",aontotwo.getAxiomCount()));
			for (String key : famap.keySet()){
				System.out.println(String.format("Format prefix %s => %s", key, famap.get(key)));
			}
			String dIRI;
			if (fa.getPrefixIRI(IRI.create(bIRI.toString() + "#")) != null){
				dIRI = dog.getIRI().prefixedBy(fa.getPrefixIRI(bIRI));
			} else {
				dIRI = dog.getIRI().toString();
			}
			System.out.println(String.format("Dog class in ontology a: %s ",dIRI));
		} catch (OWLOntologyCreationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		

		fail("Not yet implemented");
	}

}
