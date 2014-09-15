package net.strasnet.kids.measurement;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLNamedIndividual;

import net.strasnet.kids.KIDSCanonicalRepresentation;
import net.strasnet.kids.KIDSOntologyDatatypeValuesException;
import net.strasnet.kids.KIDSOntologyObjectValuesException;
import net.strasnet.kids.KIDSOracle;
import net.strasnet.kids.detectors.UnimplementedIdentifyingFeatureException;
import net.strasnet.kids.detectorsyntaxproducers.KIDSIncompatibleSyntaxException;
import net.strasnet.kids.measurement.correlationfunctions.IncompatibleCorrelationValueException;
import net.strasnet.kids.measurement.datasetlabels.DatasetLabel;
import net.strasnet.kids.measurement.datasetlabels.TruthFileParseException;
import net.strasnet.kids.measurement.datasetviews.DatasetView;
import net.strasnet.kids.measurement.datasetviews.KIDSUnsupportedSchemeException;

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
	 * This method generated a view label dataset by following these steps:
	 * 1) Loads the view generator for each view, using the implementation given by the oracle
	 * 2) Loads the dataset label function, again using the implementation given by the oracle
	 * 3) Constructs the view label dataset by incorporating both the view and the data label
	 * 
	 * @param d - The dataset on which to evaluate
	 * @param event - The IRI of the event with respect to which we want labels
	 * @param o - A KIDSMeasurementOracle - the interface with the ontology
	 * @return
	 * @throws ClassNotFoundException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 * @throws KIDSOntologyDatatypeValuesException 
	 * @throws KIDSOntologyObjectValuesException 
	 * @throws IOException 
	 * @throws NumberFormatException 
	 * @throws KIDSUnEvaluableSignalException 
	 * @throws KIDSIncompatibleSyntaxException 
	 * @throws UnimplementedIdentifyingFeatureException 
	 */
	public static ViewLabelDataset getViewLabelDataset(IRI d, IRI event,
			KIDSMeasurementOracle o) throws KIDSOntologyDatatypeValuesException, InstantiationException, IllegalAccessException, ClassNotFoundException, KIDSOntologyObjectValuesException, NumberFormatException, IOException, KIDSUnEvaluableSignalException, KIDSIncompatibleSyntaxException, UnimplementedIdentifyingFeatureException {
		
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
		    	System.err.println("Loaded dataset view " + vld.getIRI());
		    	System.err.println("\t Instances:\t" + vld.numInstances());
		    	System.err.println("\t Events:\t" + vld.numEventOccurrences());
		    	int pos = 0;
		    	int[] pary = vld.numPositiveInstances();
		    	for (int i = 0 ; i < pary.length; i++){
		    		pos += pary[i];
		    	}
		    	
		    	System.err.println("\t Positives:\t" + pos);
		    	return vld;
		    }
		    System.err.println("[W] - No label found for dataset view " + dvc.getIRI().getFragment());
		}
		
		// If there are multiple view/label possibilities, return the first one
		System.err.println("[W] - No valid view found for dataset " + d.getFragment() + " and event " + event.getFragment());
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
	
	/**
     * Given a list of dataset IRIs, determine the applicable correlation functions and build correlated data sets
     * for each function.  Return the set of correlated datasets.
	 * @throws KIDSIncompatibleSyntaxException 
	 * @throws KIDSUnEvaluableSignalException 
	 * @throws IOException 
	 * @throws ClassNotFoundException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 * @throws NumberFormatException 
	 * @throws KIDSOntologyObjectValuesException 
	 * @throws KIDSOntologyDatatypeValuesException 
	 * @throws IncompatibleCorrelationValueException 
	 * @throws UnimplementedIdentifyingFeatureException 
	 */
	public static List<CorrelatedViewLabelDataset> getCorrelatedDatasets(Set<String> ourDSIRIList,
			IRI eventIRI, KIDSMeasurementOracle myGuy) throws KIDSOntologyDatatypeValuesException, KIDSOntologyObjectValuesException, NumberFormatException, InstantiationException, IllegalAccessException, ClassNotFoundException, IOException, KIDSUnEvaluableSignalException, KIDSIncompatibleSyntaxException, IncompatibleCorrelationValueException, UnimplementedIdentifyingFeatureException {
		List<CorrelatedViewLabelDataset> toReturn = new LinkedList<CorrelatedViewLabelDataset>();
		HashMap<Dataset,DatasetLabel> dsets = new HashMap<Dataset,DatasetLabel>();
		for (String dsIRI : ourDSIRIList){
			ViewLabelDataset vld = KIDSDatasetFactory.getViewLabelDataset(IRI.create(dsIRI), 
												   eventIRI, 
												   myGuy);
			dsets.put(vld, vld.getDatasetLabel());
		}

		// Get all possible correlation functions between the set of datasets
		Set<CorrelationFunction> ourCFList = myGuy.getCompatibleCorrelationFunctions(dsets.keySet());

		// Get a correlated dataset for each correlation function:
		for (CorrelationFunction cf : ourCFList){
			CorrelatedViewLabelDataset cvd = new CorrelatedViewLabelDataset(cf, dsets);
			toReturn.add(cvd);
		}
		return toReturn;
	}

}
