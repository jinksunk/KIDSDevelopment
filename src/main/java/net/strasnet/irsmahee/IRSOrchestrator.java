package net.strasnet.irsmahee;

import net.strasnet.kids.KIDSOntologyDatatypeValuesException;
import net.strasnet.kids.KIDSOntologyObjectValuesException;
import net.strasnet.kids.detectors.UnimplementedIdentifyingFeatureException;
import net.strasnet.kids.detectorsyntaxproducers.KIDSIncompatibleSyntaxException;
import net.strasnet.kids.measurement.*;
import net.strasnet.kids.measurement.correlationfunctions.IncompatibleCorrelationValueException;
import org.apache.logging.log4j.LogManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.util.SimpleIRIMapper;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * Created by maheedhar on 6/3/16.
 */
public class IRSOrchestrator {

    public static final org.apache.logging.log4j.Logger logme = LogManager.getLogger(IRSOrchestrator.class.getName());

    private Set<IRI> signals = null;
    private static final HashMap<String,String> configFileValues = new HashMap<String,String>();
    static {
        configFileValues.put("ABoxFile", "/dev/null");
        configFileValues.put("ABoxIRI", "/dev/null");
        configFileValues.put("TBoxFile", "/dev/null");
        configFileValues.put("TBoxIRI", "/dev/null");

        // TODO: Read the set of events from the ontology
        configFileValues.put("EventIRI", "/dev/null");
        //configFileValues.put("DatasetIRI", "/dev/null");

        // TODO: Read the available time periods from the ontology
        configFileValues.put("TimePeriodIRI", "/dev/null");
    }

    public IRSOrchestrator(){
        signals = new HashSet<IRI>();
    }


    /**
     * Test the signal set returned from the ontology.
     * Changes to support correlation -- Done
     * @throws UnimplementedIdentifyingFeatureException
     */
    public void testSignalSet(String ABOXFile,
                              String ABOXIRI,
                              String TBOXFile,
                              String TBOXIRI,
                              String EventIRI,
                              String DatasetIRI,
                              String TimePeriodIRI) throws UnimplementedIdentifyingFeatureException{
        KIDSMeasurementOracle myGuy = null;
        try {
            myGuy = new KIDSMeasurementOracle();
            List<SimpleIRIMapper> m = new LinkedList<SimpleIRIMapper>();
            m.add(new SimpleIRIMapper(IRI.create(ABOXIRI), IRI.create(ABOXFile)));
            m.add(new SimpleIRIMapper(IRI.create(TBOXIRI), IRI.create(TBOXFile)));
            myGuy.loadKIDS(IRI.create(ABOXIRI), m);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        } catch (Exception e){
            logme.error(e);
            e.printStackTrace();
            System.exit(1);
        }

        try {
            // If we have a time-period, get datasets from the time period.  Otherwise (for backward compatibility), load
            // the specified dataset.
            Set<String> ourDSIRIList = new HashSet<String>();
            HashMap<IRI,Set<IRI>> CVEtoAvailableSignalsMap = new HashMap<>();
            PreferentialIRSEngine preferentialIRSEngine = new PreferentialIRSEngine(myGuy);
            //todo have the above hashmap as high as possible so that the map can have multiple CVE
            if (TimePeriodIRI == null){
                ourDSIRIList.add(DatasetIRI);
            } else {
                //      Okay, so the Oracle should just return the list, and the Factory should
                //      have a 'Get All Datasets' method which returns individual + correlated ones.
                //      Hmm, so are we using this to check multiple correlation functions?
                ourDSIRIList = myGuy.getDatasetListForEventAndTimePeriod(IRI.create(EventIRI), IRI.create(TimePeriodIRI));
            }

            // For each dataset, get the set of signals evaluable with that dataset and this event:
            StringBuilder dsStringList = new StringBuilder();
            for (String dsIRI : ourDSIRIList){
                dsStringList.append("dsIRI,");
                signals.addAll(myGuy.getSignalsForDatasetAndEvent(IRI.create(dsIRI), IRI.create(EventIRI)));
            }
            logme.info(String.format("Evaluating signals which are evaluable against event %s with a dataset from [%s] ",EventIRI,dsStringList.toString()));
            logme.info(String.format("Evaluating %d signals",signals.size()));
            StringBuilder signalList = new StringBuilder();
            for (IRI sigString : signals){
                signalList.append(sigString.getFragment() + ",");
            }
            logme.debug(String.format("[%s]",signalList.toString()));

            //      Create the datasets first, then iterate over the dataset objects rather than the IRIs
            //      When returning the datasets, return a <Dataset,Set<SignalIRI>> map, where the signal set is
            //      the set of signals which can actually be evaluated over the data set.  Should be the union of
            //      individual signal sets for individual datasets.
            List<CorrelatedViewLabelDataset> DSOBJList = KIDSDatasetFactory.getCorrelatedDatasets(ourDSIRIList, IRI.create(EventIRI), myGuy);

            for (CorrelatedViewLabelDataset ourDS: DSOBJList){
                // For each of these signals, we need to map to a dataset and detector
                //Dataset ourDS = KIDSDatasetFactory.getViewLabelDataset(IRI.create(DatasetIRI), IRI.create(EventIRI), myGuy);
                HashSet<IRI> maliciousSignals = new HashSet<>();
                for (IRI curSig : signals){
                    Set<IRI> cSigsToEval = new HashSet<IRI>(signals);
                    //can run this for each of the CVE and put in the map
                    HashMap<Set<IRI>,Set<DataInstance>> matchingInstances = ourDS.getMatchingDataInstances(cSigsToEval);
                    //todo still need to figure out efficient strategy for getting data instances relevant to signals
                    //todo because when all signals are sent, the result is zero because of the retainAll() call in AbstractDatasetView
                    for(Set<IRI> signals :  matchingInstances.keySet()){
                        if(matchingInstances.get(signals).size()>0)
                            maliciousSignals.addAll(signals);
                    }
                    if(maliciousSignals.size()>2){
                        break;
                    }
                    cSigsToEval.remove(curSig);
                }

                CVEtoAvailableSignalsMap.put(IRI.create(EventIRI),maliciousSignals);
                preferentialIRSEngine.getBestSetOfResponses(CVEtoAvailableSignalsMap);
            }

            //KIDSSnortDetectorSyntax kds = new KIDSSnortDetectorSyntax();
            //kds.init(myGuy);
            //System.out.println("Optimal Snort rule: \n" + FileUtils.readFile(kds.getDetectorSyntax(rr.ourSigs)));
        } catch (KIDSOntologyDatatypeValuesException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InstantiationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (KIDSOntologyObjectValuesException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (NumberFormatException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (KIDSIncompatibleSyntaxException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (KIDSUnEvaluableSignalException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IncompatibleCorrelationValueException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    /**
     * UTILITY METHODS
     */


    /**
     *
     * @param sourceFile - A string giving the path of a configuration file to load values from.
     * @param requiredValueSet - A set of keys which *must* be present in the properties file for it to be considered
     * a complete configuration.  All values from the properties file will be loaded regardless, however, if any of these
     * required values are missing it will produce error messages and the method will return a null value.
     * @return A hash map from property keys to property values, as defined in the file
     */
    public static HashMap<String,String> loadPropertiesFromFile(String sourceFile, Set<String> requiredValueSet){
        boolean cerr = false;
        Properties p = new Properties ();
        HashMap<String,String> cVals = new HashMap<String,String>();

        try {
            p.load(new FileReader(new File(sourceFile)));
            for (String cstring : requiredValueSet){
                if (! p.containsKey(cstring)){
                    logme.error("Config file does not define property " + (String)cstring);
                    cerr = true;
                }
            }
            for (Object kstring : p.keySet()){
                if (!requiredValueSet.contains((String)kstring)){
                    logme.error("Config file contains unknown property " + (String)kstring);
                    cerr = true;
                }
                cVals.put((String)kstring, (String)p.getProperty((String)kstring));
            }

            if (cerr){
                return null;
            }

        } catch (IOException e1) {
            e1.printStackTrace();
            return null;
        }
        return cVals;
    }

    /**
     * @param args - one arg is the libpcap file to parse
     * run the tests
     * @throws UnimplementedIdentifyingFeatureException
     */
    public static void main(String[] args) throws UnimplementedIdentifyingFeatureException {
        String usage = "Usage: IRSOrchestrator <pathToConfigFile>";
        if (args.length != 1){
            logme.error(usage);
            java.lang.System.exit(1);
        }

        HashMap<String,String> cVals = IRSOrchestrator.loadPropertiesFromFile(args[0], IRSOrchestrator.configFileValues.keySet());

        IRSOrchestrator kss = new IRSOrchestrator();
        //TODO: Populate ABOX File
        kss.testSignalSet(cVals.get("ABoxFile"),
                cVals.get("ABoxIRI"),
                cVals.get("TBoxFile"),
                cVals.get("TBoxIRI"),
                cVals.get("EventIRI"),
                cVals.get("DatasetIRI"),
                cVals.get("TimePeriodIRI"));


    }
}
