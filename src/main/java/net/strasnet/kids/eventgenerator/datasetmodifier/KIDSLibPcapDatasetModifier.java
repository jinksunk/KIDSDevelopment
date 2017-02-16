/**
 * 
 */
package net.strasnet.kids.eventgenerator.datasetmodifier;

import org.apache.log4j.LogManager;

import net.strasnet.kids.eventgenerator.dataelement.KIDSDataElement;
import net.strasnet.kids.eventgenerator.dataelement.KIDSDataElement.KIDSDataElementType;
import net.strasnet.kids.exceptions.KIDSTypeMismatchException;
import net.strasnet.kids.ui.gui.ABOXBuilderController;

/**
 * @author cstras
 * Implements the methods necessary to modify a libpcap file
 */
public class KIDSLibPcapDatasetModifier extends KIDSAbstractDatasetModifier implements KIDSDatasetModifier {
	
	public static final org.apache.log4j.Logger logme = LogManager.getLogger(KIDSLibPcapDatasetModifier.class.getName());
	
	public KIDSLibPcapDatasetModifier(){
		
	}

	@Override
	/**
	 * This method will buffer added elements until the 'close' method is called.
	 */
	public void addDataElement(KIDSDataElement e) throws KIDSTypeMismatchException {
		if (e.getType() != KIDSDataElementType.LIBPCAPDataElement){
			throw new KIDSTypeMismatchException(String.format("KIDSLibPcapDatasetModifier cannot process data elements of type %s",e.getType()));
		}
		
	}

	@Override
	public void close() {
		// TODO Auto-generated method stub
		
	}

}
