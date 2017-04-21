/**
 * 
 */
package net.strasnet.kids.eventgenerator.dataelement;

/**
 * @author cstras
 * Defines the methods required to support the generic handling of data elements.
 */
public interface KIDSDataElement {
	
	public enum KIDSDataElementType{
		LIBPCAPDataElement;
	}

	KIDSDataElementType getType();

}
