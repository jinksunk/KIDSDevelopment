/**
 * 
 */
package net.strasnet.nfa;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author chrisstrasburg
 * A label on an NFA Edge.  Defines:
 * - The legal label classes (e.g. Literal, Operator)
 * - The label value (String)
 */
public class Label implements Comparable<Label> {

	public static final int LITERAL = 0;
	public static final int OPERATOR = 1;
	public static final int DOT = 2;
	public static final int EPSILON = 3;
	public static final int NULL = 4;
	
	public static Map<Integer, String> classes = new TreeMap<Integer, String>();
	static {
		classes.put(LITERAL, "Literal");
		classes.put(OPERATOR, "Operator");
		classes.put(DOT, "Dot");
		classes.put(EPSILON, "epsilon");
		classes.put(NULL, "null");		
	}
	
	private String value;
	private int myClass;
	
	public Label(String v, int c){
		value = v;
		myClass = c;
	}
	
	public String getValue (){
		return value;
	}
	
	public int getEClass (){
		return myClass;
	}
	
	public void setValue(String newVal){
		value = newVal;
	}
	
	public void setEClass(int newC){
		if (classes.containsKey(newC)){
  		  myClass = newC;
		} else {
			System.out.println("Warning: class value " + newC + " not a known eClass");
		}
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

	@Override
	public int compareTo(Label arg0) {
		// TODO Auto-generated method stub
		if (myClass == (((Label)arg0).getEClass())){
			return this.getValue().compareTo(((Label)arg0).getValue());
		}
		return new Integer(myClass).compareTo(new Integer(((Label)arg0).getEClass()));
	}

}
