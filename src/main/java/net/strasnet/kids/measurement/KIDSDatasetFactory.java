package net.strasnet.kids.measurement;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLNamedIndividual;

import net.strasnet.kids.KIDSCanonicalRepresentation;
import net.strasnet.kids.KIDSOntologyDatatypeValuesException;
import net.strasnet.kids.KIDSOntologyObjectValuesException;
import net.strasnet.kids.KIDSOracle;
import net.strasnet.kids.detectors.KIDSAbstractDetector;
import net.strasnet.kids.detectors.UnimplementedIdentifyingFeatureException;
import net.strasnet.kids.detectorsyntaxproducers.KIDSIncompatibleSyntaxException;
import net.strasnet.kids.measurement.correlationfunctions.IncompatibleCorrelationValueException;
import net.strasnet.kids.measurement.datasetlabels.DatasetLabel;
import net.strasnet.kids.measurement.datasetlabels.TruthFileParseException;
import net.strasnet.kids.measurement.datasetviews.DatasetView;
import net.strasnet.kids.measurement.datasetviews.KIDSUnsupportedSchemeException;
import net.strasnet.kids.ui.gui.KIDSGUIOracle;

public class KIDSDatasetFactory {
	
	private static final Logger logme = LogManager.getLogger(KIDSDatasetFactory.class.getName());
	
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
	 * @deprecated
	 */
	public static Dataset createDataset (IRI iri, 
			String className, 
			String dataIRI, 
			String labelIRI, 
			String eventIRI, 
			KIDSMeasurementOracle kidsOracle) 
					throws IOException, KIDSUnsupportedSchemeException, TruthFileParseException{
		// Check to make sure the class exists
		logme.warn(String.format("Using deprecated dataset construction for dataset %s", iri));
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
	 * @deprecated
	 */
	public static Dataset createDataset (IRI iri, 
			String className, 
			String dataIRI, 
			String labelIRI, 
			KIDSMeasurementOracle kidsOracle) 
					throws IOException, KIDSUnsupportedSchemeException{
		logme.warn(String.format("Using deprecated dataset construction for dataset %s", iri));
		logme.debug(String.format("Loading class %s for iri %s (location %s) with label %s.", 
									className,
									iri.getShortForm(),
									dataIRI,
									labelIRI));
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
			logme.error("Class " + className + " found, but not instantiated.\n" + e);
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			logme.error("Class " + className + " could not be found.\n" + e);
			e.printStackTrace();
		}

		return null;
	}
	
	
	/**
	 * This method generates a view label dataset by following these steps:
	 * 1) Loads the view generator for the provided view, using the implementation given by the oracle
	 * 2) Loads the dataset label function, again using the implementation given by the oracle
	 * 3) Constructs the view label dataset by incorporating both the view and the data label
	 * 
	 * @param viewIRI - The dataset on which to evaluate
	 * @param eventIRI - The IRI of the event with respect to which we want labels
	 * @param o - A KIDSMeasurementOracle - the interface with the ontology
	 * @return - A ViewLabelDataset object (which will need to be initialized).
	 * 
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
	public static ViewLabelDataset getViewLabelDatasetFromViewEvent(IRI viewIRI, IRI eventIRI, KIDSMeasurementOracle o) throws 
		KIDSOntologyDatatypeValuesException, 
		InstantiationException, 
		IllegalAccessException, 
		ClassNotFoundException, 
		NumberFormatException, 
		IOException, 
		UnimplementedIdentifyingFeatureException, 
		KIDSOntologyObjectValuesException, 
		KIDSUnEvaluableSignalException, 
		KIDSIncompatibleSyntaxException {
		logme.debug(String.format("Creating viewLabel dataset for view %s, event %s", 
									viewIRI.getShortForm(),
									eventIRI.getShortForm()));
		
		// Get the dataset this view provides a view of:
		Set<IRI> datasetSet = o.getDatasetsForDatasetViewAndEvent(viewIRI, eventIRI);
		
		logme.debug(String.format("Found %d datasets for view %s and event %s.", datasetSet.size(), viewIRI.getShortForm(), eventIRI.getShortForm()));
		
		IRI dataset = datasetSet.iterator().next();
		if (datasetSet.size() > 1){
			logme.warn(String.format("WARNING - Found %d dataseets for view %s and event %s; only the first returned will be used (%s).",
					datasetSet.size(), viewIRI.getShortForm(), eventIRI.getShortForm(), dataset.getShortForm()));
		}
		
		// First, get the set of data labels, satisfying: 
		//    isLabelForEvent(event) ^ isLabelForView (SOME dv)
		    IRI label = o.getLabelForViewAndEvent(viewIRI, eventIRI);
		    if (label != null){
		    	DatasetView dv = getViewGenerator(o.getViewImplementation(viewIRI));
		    	dv.setIRI(viewIRI);
		    	DatasetLabel dl = getViewLabelClass(o.getLabelImplementation(label));
		    	dl.init(o.getLabelLocation(label).toString(),eventIRI); 
		    	ViewLabelDataset vld = new ViewLabelDataset();
		    	vld.setDatasetIRI(dataset);
		    	vld.init(dv, dl, o, eventIRI);
		    	logme.debug("Loaded dataset view " + vld.getIRI());
		    	logme.debug("\t Instances:\t" + vld.numInstances());
		    	logme.debug("\t Events:\t" + vld.numEventOccurrences());
		    	int pos = 0;
		    	int[] pary = vld.numPositiveInstances();
		    	for (int i = 0 ; i < pary.length; i++){
		    		pos += pary[i];
		    	}
		    	
		    	logme.debug("\t Positives:\t" + pos);
		    	return vld;
		    }
		    logme.warn("No label found for dataset view " + viewIRI.getShortForm());
		
		// TODO: If there are multiple view/label possibilities, return the first one
		logme.warn("No valid dataset found for dataset view" + viewIRI.getShortForm() + " and event " + eventIRI.getShortForm());
		return null;
	}

	/**
	 * This method generates a view label dataset by following these steps:
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
		logme.debug(String.format("Creating viewLabel dataset for dataset %s, event %s", 
									d.getShortForm(),
									event.getShortForm()));
		
		// First, get the set of views, satisfying: isViewOf(d)
		List<IRI> views = o.getAvailableViews(d, event);
		
		// Next, get the set of data labels, satisfying: 
		//    isLabelForEvent(event) ^ isLabelForView (SOME dv)
		for (IRI dvc : views){
		    IRI label = o.getLabelForViewAndEvent(dvc, event);
		    if (label != null){
		    	DatasetView dv = getViewGenerator(o.getViewImplementation(dvc));
		    	dv.setIRI(dvc);
		    	DatasetLabel dl = getViewLabelClass(o.getLabelImplementation(label));
		    	dl.init(o.getLabelLocation(label).toString(),event); // TODO: Some day, use IRIs again
		    	ViewLabelDataset vld = new ViewLabelDataset();
		    	vld.setDatasetIRI(d);
		    	vld.init(dv, dl, o, event);
		    	logme.debug("Loaded dataset view " + vld.getIRI());
		    	logme.debug("\t Instances:\t" + vld.numInstances());
		    	logme.debug("\t Events:\t" + vld.numEventOccurrences());
		    	int pos = 0;
		    	int[] pary = vld.numPositiveInstances();
		    	for (int i = 0 ; i < pary.length; i++){
		    		pos += pary[i];
		    	}
		    	
		    	logme.debug("\t Positives:\t" + pos);
		    	return vld;
		    }
		    logme.warn("No label found for dataset view " + dvc.getShortForm());
		}
		
		// TODO: If there are multiple view/label possibilities, return the first one
		logme.warn("No valid view found for dataset " + d.getShortForm() + " and event " + event.getShortForm());
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
			logme.error("Class " + labelImplementation + " found, but not instantiated.\n" + e);
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			logme.error("Class " + labelImplementation + " could not be found.\n" + e);
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
			logme.error("Class " + generatorClass + " found, but not instantiated.\n" + e);
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			logme.error("Class " + generatorClass + " could not be found.\n" + e);
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
		logme.debug(String.format("Getting correlated datatsets for event %s",eventIRI.getShortForm()));
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
		logme.debug(String.format("Found %d qualifying correlation functions over %d dataseets.",ourCFList.size(), dsets.size()));

		// Get a correlated dataset for each correlation function:
		for (CorrelationFunction cf : ourCFList){
			CorrelatedViewLabelDataset cvd = new CorrelatedViewLabelDataset(cf, dsets);
			toReturn.add(cvd);
		}
		return toReturn;
	}

}
