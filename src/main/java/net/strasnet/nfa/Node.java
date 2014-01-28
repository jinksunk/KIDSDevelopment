/**
 * 
 */
package net.strasnet.nfa;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author chrisstrasburg
 * An NFA node.  A node has the following properties:
 *  - A Node ID
 *  - A set of outgoing edges
 */
public class Node implements Comparable{
	
	private int NodeID;
	private Map<Label,List<Edge>> edges;
	private Map<Integer,List<Edge>> edgesByDest;
	
	public Node (int id){
		NodeID = id;
		edges = new TreeMap<Label,List<Edge>>();
		edgesByDest = new TreeMap<Integer,List<Edge>>();
	}

	public int getNodeID(){
		return NodeID;
	}
	
	public void setNodeID(int newId){
		NodeID = newId;
	}
	
	public Iterator<Edge> getEdges(){
		// For each list of edges, add to the set of total edges:
		LinkedList<Edge> flattenedEdges = new LinkedList<Edge>();
		Iterator<List<Edge>> i = edges.values().iterator();
		List<Edge> te;
		while (i.hasNext()){
			te = i.next();
			flattenedEdges.addAll(te);
		}
		return flattenedEdges.iterator();
	}
	
	public Iterator<Edge> getEdges(Label l){
		if (!edges.containsKey(l)){
			return null;
		}
		return edges.get(l).iterator();
	}
	
	/**
	 * Returns the list of edges with the given nodeID as a dest
	 * @param nodeID
	 * @return
	 */
	public Iterator<Edge> getEdges(Integer nodeID){
		if (!edgesByDest.containsKey(nodeID)){
			return null;
		}
		return edgesByDest.get(nodeID).iterator();
	}
	
	public String toString(){
		return ""+NodeID;
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// Test our nodes:
		Node n1 = new Node(1);
		int newID = 2;
		
		System.out.println("Created node " + n1);
		System.out.println("Setting node ID to " + newID);
		
		n1.setNodeID(newID);
		System.out.println("Node is now: " + n1);
	}

	public void addEdge(Edge e) {
		addEdgeByLabel(e);
		addEdgeByDest(e);
	}

	private void addEdgeByLabel(Edge e){
		addEdge((TreeMap) edges, e.getLabel(), e);
	}
	
	private void addEdgeByDest(Edge e){
		addEdge((TreeMap) edgesByDest, new Integer(e.getDest().getNodeID()), e);
	}
	
	private void addEdge (TreeMap t, Object k, Edge e){
		List<Edge> myEdges = new LinkedList<Edge>();
		if (t.containsKey(k)){
			myEdges = (List<Edge>) t.get(k);
		} else {
			t.put(k, (List<Edge>)myEdges);
		}
		myEdges.add(e);
	}
	
	@Override
	public int compareTo(Object arg0) {
		Integer newNodeID = ((Node)arg0).getNodeID();
		return new Integer(this.NodeID).compareTo(newNodeID);
	}

	/**
	 * Just remove all edges pointing at the specified node:
	 * @param node
	 */
	public void removeEdgesByDest(Node node) {

		List<Edge> toAxe = edgesByDest.get(new Integer(node.getNodeID()));
		Iterator<Edge> eI = toAxe.iterator();
		while (eI.hasNext()){
			Edge needle = eI.next();
			List<Edge> haystack = edges.get(needle.getLabel());
			haystack.remove(needle);
		}
		edgesByDest.remove(new Integer(node.getNodeID()));
	}

}
