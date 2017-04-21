/**
 * 
 */
package net.strasnet.kids.detectors;

/**
 * @author cstras
 * Represents the case where a detector has not implemented an extraction for an identifying feature
 * required by the view it processes.
 */
public class UnimplementedIdentifyingFeatureException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7902853608978564359L;
	private String unimplementedFeature = null;
	
	public UnimplementedIdentifyingFeatureException(String msg, String feature){
		super(msg + " (feature: " + feature);
		unimplementedFeature = feature;
	}

	public String getFeature() {
		return unimplementedFeature;
	}


}
