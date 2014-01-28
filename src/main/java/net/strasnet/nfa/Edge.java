/**
 * 
 */
package net.strasnet.nfa;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * @author chrisstrasburg
 * An nfa edge.  An edge has the following characteristics:
 *  - Source Node
 *  - Destination Node
 *  - Label
 */
public class Edge {

	private Label myLabel;
	private Node dest;
	
	public Edge (){
		this(null, null);
	}
	
	public Edge(Label l, Node d){
		myLabel = l;
		dest = d;
	}
	
	public Label getLabel(){
		return myLabel;
	}
	
	public void setLabel(Label l){
		myLabel = l;
	}
	
	public Node getDest(){
		return dest;
	}
	
	public void setDest(Node n){
		dest = n;
	}
	
	public String toString(){
		String toReturn = "";
		toReturn = "-- " + myLabel.getValue() + "[" + myLabel.classes.get(new Integer(myLabel.getEClass())) + "] --> " + getDest();
		return toReturn;
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
