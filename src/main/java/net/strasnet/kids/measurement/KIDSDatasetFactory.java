package net.strasnet.kids.measurement;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLNamedIndividual;

import net.strasnet.kids.KIDSCanonicalRepresentation;
import net.strasnet.kids.KIDSOntologyDatatypeValuesException;
import net.strasnet.kids.KIDSOntologyObjectValuesException;
import net.strasnet.kids.KIDSOracle;
import net.strasnet.kids.measurement.datasetlabels.DatasetLabel;
import net.strasnet.kids.measurement.datasetviews.DatasetView;
import net.strasnet.kids.measurement.datasetviews.KIDSUnsupportedSchemeException;
import net.strasnet.kids.measurement.datasetviews.KIDSLibpcapDataset.KIDSLibpcapTruthFile.TruthFileParseException;

public class KIDSDatasetFactory {
	/**
	 * 
	 * @param iri - The IRI of the associated dataset itself
	 * @param className - The name of the class to instantiate (must have a 0-arg constructor)
	 * @param dataIRI - An IRI pointing to the data for the actual dataset
	 * @param labelIRI - An IRI pointing to the labels for the dataset, w.r.t. the event in the ontology.
	 * @param eventIRI - An IRI pointing to the event with respect to which the dataset will be interpreted.
	 * @param kidsMeasurementOracle 
	 * @return
	 * @throws IOException 
	 * @throws KIDSUnsupportedSchemeException 
	 * @throws TruthFileParseException 
	 */
	public static Dataset createDataset (IRI iri, 
			String className, 
			String dataIRI, 
			String labelIRI, 
			String eventIRI, 
			KIDSMeasurementOracle kidsOracle) 
					throws IOException, KIDSUnsupportedSchemeException, TruthFileParseException{
		// Check to make sure the class exists
		Dataset toReturn = createDataset(iri,className,dataIRI,labelIRI,kidsOracle);
		toReturn.init(IRI.create(eventIRI));
		return toReturn;
	}
	/**
	 * 
	 * @param iri - The IRI of the associated dataset itself
	 * @param className - The name of the class to instantiate (must have a 0-arg constructor)
	 * @param dataIRI - An IRI pointing to the data for the actual dataset
	 * @param labelIRI - An IRI pointing to the labels for the dataset, w.r.t. the event in the ontology.
	 * @param kidsMeasurementOracle 
	 * @return
	 * @throws IOException 
	 * @throws KIDSUnsupportedSchemeException 
	 * @throws TruthFileParseException 
	 */
	public static Dataset createDataset (IRI iri, 
			String className, 
			String dataIRI, 
			String labelIRI, 
			KIDSMeasurementOracle kidsOracle) 
					throws IOException, KIDSUnsupportedSchemeException{
		// Check to make sure the class exists
		try {
			String strippedName = className;
			if (strippedName.startsWith("\"")){
				strippedName = strippedName.substring(1);
			}
			if (strippedName.endsWith("\"")){
				strippedName = strippedName.substring(0,strippedName.length() - 1);
			}
			Class<?> newClass = Class.forName(strippedName);
			Object instance = newClass.newInstance();
			Dataset toReturn = (Dataset) instance;
			toReturn.setDataIRI(dataIRI);
			toReturn.setLabelIRI(labelIRI);
			toReturn.setDatasetIRI(iri);
			toReturn.setOracle(kidsOracle);
			return toReturn;
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			System.out.println("Class " + className + " found, but not instantiated.\n" + e);
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			System.out.println("Class " + className + " could not be found.\n" + e);
			e.printStackTrace();
		}

		return null;
	}
	
	
	/**
	 * 
	 * @param d - The dataset on which to evalute
	 * @param event - The IRI of the event with respect to which we want labels
	 * @return
	 * @throws ClassNotFoundException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 * @throws KIDSOntologyDatatypeValuesException 
	 * @throws KIDSOntologyObjectValuesException 
	 * @throws IOException 
	 * @throws NumberFormatException 
	 */
	public static Dataset getViewLabelDataset(IRI d, IRI event,
			KIDSMeasurementOracle o) throws KIDSOntologyDatatypeValuesException, InstantiationException, IllegalAccessException, ClassNotFoundException, KIDSOntologyObjectValuesException, NumberFormatException, IOException {
		
		// First, get the set of views, satisfying: isViewOf(d)
		List<OWLNamedIndividual> views = o.getAvailableViews(d, event);
		
		// Next, get the set of data labels, satisfying: 
		//    isLabelForEvent(event) ^ isLabelForView (SOME dv)
		for (OWLNamedIndividual dvc : views){
		    OWLNamedIndividual label = o.getLabelForViewAndEvent(dvc, event);
		    if (label != null){
		    	DatasetView dv = getViewGenerator(o.getViewImplementation(dvc));
		    	dv.setIRI(dvc.getIRI());
		    	DatasetLabel dl = getViewLabelClass(o.getLabelImplementation(label));
		    	dl.init(o.getLabelLocation(label),event);
		    	ViewLabelDataset vld = new ViewLabelDataset();
		    	vld.setDatasetIRI(d);
		    	vld.init(dv, dl, o, event);
		    	return vld;
		    }
		}
		
		// If there are multiple view/label possibilities, return the first one
		return null;
	}
	
	private static DatasetLabel getViewLabelClass(String labelImplementation) {
		try {
			String strippedName = labelImplementation;
			if (strippedName.startsWith("\"")){
				strippedName = strippedName.substring(1);
			}
			if (strippedName.endsWith("\"")){
				strippedName = strippedName.substring(0,strippedName.length() - 1);
			}
			Class<?> newClass = Class.forName(strippedName);
			Object instance = newClass.newInstance();
			DatasetLabel toReturn = (DatasetLabel) instance;
			return toReturn;
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			System.out.println("Class " + labelImplementation + " found, but not instantiated.\n" + e);
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			System.out.println("Class " + labelImplementation + " could not be found.\n" + e);
			e.printStackTrace();
		}

		return null;
	}
	/**
	 * 
	 * @param generatorClass - The classname of the DatasetView to instantiate
	 * @return
	 */
	public static DatasetView getViewGenerator(String generatorClass){
		try {
			String strippedName = generatorClass;
			if (strippedName.startsWith("\"")){
				strippedName = strippedName.substring(1);
			}
			if (strippedName.endsWith("\"")){
				strippedName = strippedName.substring(0,strippedName.length() - 1);
			}
			Class<?> newClass = Class.forName(strippedName);
			Object instance = newClass.newInstance();
			DatasetView toReturn = (DatasetView) instance;
			return toReturn;
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			System.out.println("Class " + generatorClass + " found, but not instantiated.\n" + e);
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			System.out.println("Class " + generatorClass + " could not be found.\n" + e);
			e.printStackTrace();
		}

		return null;
		
	}

}
