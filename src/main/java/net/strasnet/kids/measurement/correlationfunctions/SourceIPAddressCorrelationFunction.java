package net.strasnet.kids.measurement.correlationfunctions;

import net.strasnet.kids.measurement.CorrelationFunction;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class SourceIPAddressCorrelationFunction implements CorrelationFunction {
	public static final String kidsTbox = "http://solomon.cs.iastate.edu/ontologies/KIDS.owl";
	public static final String relatedResource = kidsTbox + "#srcIPAddress";
	
	
	@Override
	public boolean isCorrelated (String ip1, String ip2) throws IncompatibleCorrelationValueException{
		InetAddress i1;
		InetAddress i2;
		try {
			i1 = InetAddress.getByName(ip1);
			i2 = InetAddress.getByName(ip2);
		} catch (UnknownHostException e) {
			throw new IncompatibleCorrelationValueException("Value " + ip1 + " is not comparable to value " + ip2);
		}
		return i1.equals(i2);
	}
}
