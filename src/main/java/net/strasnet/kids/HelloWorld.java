/**
 * 
 */
package net.strasnet.kids;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.*;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.util.SimpleIRIMapper;
import java.net.UnknownHostException; 
import java.io.FileNotFoundException; 
import java.io.File;


/**
 * @author chrisstrasburg
 *
 */
public class HelloWorld {
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String ontologyFile = "file:///Users/chrisstrasburg/Documents/academic-research/phd-prelim/Ontologies/Undercoffer-2004-withSignals.owl";
		try {
			OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
			IRI iri = IRI.create(ontologyFile);
			OWLOntology pizzaOntology = manager.loadOntology(iri);
			OWLDataFactory odg = manager.getOWLDataFactory();
			//manager.addAxiom(pizzaOntology, odf.getOWLSubClassOfAxiom(arg0, arg1))
			//OWLClass t1 = odf.getOWLClass(IRI.create("http://www.strasnet.net/ontologies/pizza/pizza.owl#thing1"));
			//OWLClass t2 = odf.getOWLClass(IRI.create("http://www.strasnet.net/ontologies/pizza/pizza.owl#thing2"));
			//OWLAxiom a1 = odf.getOWLSubClassOfAxiom(t1,t2);
			//manager.applyChange(new AddAxiom(pizzaOntology,a1));
			Set <OWLClass> s = pizzaOntology.getClassesInSignature();
			Iterator<OWLClass> i = s.iterator();
			System.out.println("Printing " + s.size() + " classes from " + pizzaOntology);
			while (i.hasNext()){
				System.out.println(i.next());
			}
			Set <OWLAxiom> s2 = pizzaOntology.getAxioms();
			Iterator<OWLAxiom> i2 = s2.iterator();
			while (i2.hasNext()){
				System.out.println(i2.next());
			}
			//OWLClass newClass = odf.getOWLClass(IRI.create("http://xmlns.com/foaf/0.1/Person"));
			//manager.
			System.out.println(pizzaOntology);
		//} catch (OWLOntologyCreationIOException e) {
		} catch (Exception e) {
			// IOExceptions during loading get wrapped in an OWLOntologyCreationIOException
			IOException ioException = (IOException) e.getCause();
			if (ioException instanceof FileNotFoundException) {
			 	System.out.println("Could not load ontology. File not found: " + ioException.getMessage());
			}
			else if (ioException instanceof UnknownHostException) {
			 	System.out.println("Could not load ontology. Unknown host: " + ioException.getMessage());
			}
			else {
			 	System.out.println("Could not load ontology: " + e);
			 	e.printStackTrace();
			}
		}
		/**
		catch (UnparsableOntologyException e) {
			// If there was a problem loading an ontology because there are syntax errors in the document (file) that
			// represents the ontology then an UnparsableOntologyException is thrown
			System.out.println("Could not parse the ontology: " + e.getMessage());
			// A map of errors can be obtained from the exception
			Map<OWLParser, OWLParserException> exceptions = e.getExceptions();
			// The map describes which parsers were tried and what the errors were
			for (OWLParser parser : exceptions.keySet()) {
			 	System.out.println("Tried to parse the ontology with the " + parser.getClass().getSimpleName() + " parser");
			 	System.out.println("Failed because: " + exceptions.get(parser).getMessage());
			}
		}
		catch (UnloadableImportException e) {
			// If our ontology contains imports and one or more of the imports could not be loaded then an
			// UnloadableImportException will be thrown (depending on the missing imports handling policy)
			System.out.println("Could not load import: " + e.getImportsDeclaration());
			// The reason for this is specified and an OWLOntologyCreationException
			OWLOntologyCreationException cause = e.getOntologyCreationException();
			System.out.println("Reason: " + cause.getMessage());
		}
		catch (OWLOntologyCreationException e) {
			System.out.println("Could not load ontology: " + e.getMessage());
		}  */
	}

}
