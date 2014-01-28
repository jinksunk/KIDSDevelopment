/**
 * 
 */
package net.strasnet.nfa;

/**
 * @author chrisstrasburg
 * Similar to a "Label", but generalized so that the value of the label can contain any regular expression.
 */
public class GLabel extends Label {
	public static final int REGEX  = 99999;
	static {
		classes.put(REGEX, "rex");
	}
	public GLabel(String exp) {
		super(exp, REGEX);
	}

}
