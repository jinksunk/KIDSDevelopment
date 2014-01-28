package net.strasnet.kids.measurement.correlationfunctions;

public class IncompatibleCorrelationValueException extends Exception {
	String message;
	
	public IncompatibleCorrelationValueException(String message){
		this.message = message;
	}
	
	@Override
	public String getMessage(){
		return message;
	}
}
