/**
 * 
 */
package net.strasnet.kids.test;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.util.SimpleIRIMapper;

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
public class testKIDSOntologySetup {
	public static String DefaultABOXIRI = "https://www.semantiknit.com/ontologies/TESTABOX.owl";
	public static String DefaultABOXLocation = "./resources/ontologies/KIDS/TESTABOX.owl";
	public static String DefaultTBOXLocation = "./resources/ontologies/KIDS/KIDS-TBOX.owl";
	
	protected KIDSOracle testO = null;
	
	/**
	 * 
	 * @param loadABOXLoc An optional location specification for the ABOX; if null, the default
	 *                    ABOX location will be used..
	 * @param loadTBOXLoc An optional location specification for the TBOX; if null, the TBOX will be imported 
	 *                    from the location specified in the ABOX, or TBOXLocation.
	 * @return - An initialized KIDSOntology
	 */
	public KIDSOracle getOntology(String loadABOXLoc, String loadABOXIRI, String loadTBOXLoc){
		if (testO == null){
			// Initialize a new ontology oracle:
			try {
				testO = new KIDSOracle();
				List<SimpleIRIMapper> m = new LinkedList<SimpleIRIMapper>();
				m.add(new SimpleIRIMapper(IRI.create(loadABOXIRI), IRI.create(loadABOXLoc)));
				m.add(new SimpleIRIMapper(IRI.create(KIDSOracle.DEFAULTTBOXIRI), IRI.create(loadTBOXLoc)));
				testO.loadKIDS(IRI.create(loadABOXIRI), m);
			} catch (OWLOntologyCreationException e) {
				e.printStackTrace();
				return null;
			}
		}
		return testO;
	}
	
}
