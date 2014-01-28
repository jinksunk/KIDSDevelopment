/**
 * 
 */
package net.strasnet.nfa;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;

/**
 * @author chrisstrasburg
 *
 * This class represents a generalized NFA (GNFA) for the purposes of producing a regular expression from 
 * an existing NFA.  Note that:
 *  1) Every DFA is an NFA
 *  2) Every NFA can be represented by a GNFA
 *  3) A GNFA is just an NFA with some special conditions
 */
public class GNFA extends ThompsonNFA {

	public GNFA (ThompsonNFA source){
		setStart(source.StartNode);
		setFinal(source.FinalNode);

		// First, create the start and final states:
		Node newStart = new Node(curNodeID++);
		newStart.addEdge(new Edge(
				new Label(Label.classes.get(Label.EPSILON), Label.EPSILON),
				getStart()
				));
		this.setStart(newStart);

		Node newEnd = new Node(curNodeID++);
		Node oldFinal = getFinal();
		oldFinal.addEdge(new Edge(
				new Label(Label.classes.get(Label.EPSILON), Label.EPSILON),
				newEnd
				));
		this.setFinal(newEnd);
		System.out.println("Prior to adding NULL edges: \n" + this);
		
		// Next, ensure that every node is connected to every other node
		// For each node in the NFA, check it for an edge to each other node
		Collection<Node> allNodes = getNodes();
		Iterator<Node> allNodesI = allNodes.iterator();
		while (allNodesI.hasNext()){
			Node cur = allNodesI.next();
			if (! cur.equals(FinalNode)){

				// For each node, if cur does not have an edge to it, make one:
				Iterator<Node> targ = allNodes.iterator();
				while (targ.hasNext()){
					Node t = targ.next();
					if (cur.getEdges(t.getNodeID()) == null){
						// Add a null edge:
						Label nullL = new Label(Label.classes.get(Label.NULL),Label.NULL);
						Edge nullE = new Edge(nullL, t);
						cur.addEdge(nullE);
					}
				}
			}
		}
	}
	
	/**
	 * Generate the regular expression that this NFA produces.
	 * @return r - regular expression
	 */
	public String generateRegex(){
	  String r = "";
	  
	  // Beginning from StartNode, remove nodes and modify labels:
	  //  REMINDER: Every node has an edge to every other node; no guessing about which edges exist, etc...
	  Collection<Node> myNodes = getNodes();
	  
	  Node[] outerNodes = new Node[myNodes.size()];
	  myNodes.toArray(outerNodes);

	  for (int i = 0; i < outerNodes.length; i ++){
		  if (!(StartNode.equals(outerNodes[i]) || FinalNode.equals(outerNodes[i]))){
			System.out.println("Removing node " + outerNodes[i]);
			// We have a node which is neither an initial or accepting state:
			// For each other incoming node which is not this node:
			Iterator<Node> incomingNodes = getNodes().iterator();
			while (incomingNodes.hasNext()){
				Node curI = incomingNodes.next();
				if (!(curI.equals(FinalNode) || curI.equals(outerNodes[i]))){
					System.out.println("|- Considering node " + curI + " as incoming.");
					Edge eIcandidate = curI.getEdges(outerNodes[i].getNodeID()).next();
					if (eIcandidate.getLabel().getEClass() == Label.NULL){
						curI.removeEdgesByDest(outerNodes[i]);
						continue;
					}
					// For each outgoing node which is not the current outerNode and not initial state:
					Iterator<Node> outgoingNodes = getNodes().iterator();
					while (outgoingNodes.hasNext()){
						Node curO = outgoingNodes.next();
						if (!(curO.equals(StartNode) || curO.equals(outerNodes[i]))){
							Edge eOcandidate = outerNodes[i].getEdges(curO.getNodeID()).next();
							if (eOcandidate.getLabel().getEClass() == Label.NULL){
								continue;
							}
							
							boolean sawEpsilon = false; 
							boolean isAlt = false;
							
							System.out.println("  |- Replacing edges from " + curI + " -> " + outerNodes[i] + " -> " + curO);

							// Build R_{dir} component
							String temp = this.getEdgeLabelsAsRE(curI, curO);
							System.out.print("    |- R_{dir} => ");
							
							if (temp == null){
								System.out.println("<NULL>");
								temp = "";
							} else if (!temp.isEmpty()){
								System.out.println(temp);
								temp += "|";
								isAlt = true;
							} else{
								sawEpsilon = true;
								System.out.println("<epsilon>");
							}
							String resultRE = temp;
							
							// Build R_{in} component
							temp = this.getEdgeLabelsAsRE(curI, outerNodes[i]);
							System.out.print("    |- R_{in} => ");
							if (!temp.isEmpty()){
								System.out.println(temp);
							} else {
								System.out.println("<epsilon>");
							}
							resultRE += temp;
							
							// Build R_{rip} component
							System.out.print("    |- R_{rip} => ");
							temp = this.getEdgeLabelsAsRE(outerNodes[i], outerNodes[i]);
							if (temp == null){
								System.out.println("<NULL>");
								temp = "";
							} else if (!temp.isEmpty()){
								temp = "(" + temp + ")*";
								System.out.println(temp);
							} else {
								System.out.println("<epsilon>");
							}
							resultRE += temp;

							// Build R_{out} component
							System.out.print("    |- R_{out} => ");
							temp = this.getEdgeLabelsAsRE(outerNodes[i], curO);
							if (!temp.isEmpty()){
								System.out.println(temp);
							} else {
								System.out.println("<epsilon>");
							}
							resultRE += temp;
							
							// Create a new edge and add it:
							Label newG;
							if (resultRE.isEmpty()){
								newG = new Label(Label.classes.get(Label.EPSILON), Label.EPSILON);
							} else {
								if (sawEpsilon){
							
								System.out.println("    |- [W] Parallel epsilon edge found; simplifying...");
								resultRE = "(" + resultRE + ")?";
							} else if (isAlt){
								System.out.println("    |- [W] New alternation; parenthesizing...");
								resultRE = "(" + resultRE + ")";
							}
								newG = new GLabel("/" + resultRE + "/");
							}
							
							Edge newEdge = new Edge(newG, curO);
							curI.removeEdgesByDest(curO);
							curI.addEdge(newEdge);
							//System.out.println(resultRE);
							
							
						}
					}
					// Remove edge to dead node from current input:
					curI.removeEdgesByDest(outerNodes[i]);
				}
			}
			System.out.println("After removing node " + outerNodes[i] + ", GNFA is: \n" + this);
		  }
	  }
	  
	  Iterator <Edge> lastEdge = StartNode.getEdges(new Integer(FinalNode.getNodeID()));
	  return lastEdge.next().getLabel().getValue();
	}
	
	private String getEdgeLabelsAsRE(Node in, Node out){
		String resultRE = "";
		Iterator<Edge> curIOEdges = in.getEdges(new Integer(out.getNodeID()));
		//System.out.println("Checking edges between " + in + " and " + out);
		if (curIOEdges == null){
			System.out.println("It appears that " + in + " does not have an edge to " + out + "in GNFA \n" + this);
		}
		boolean sawEpsilon = false;
		boolean sawNull = false;
		boolean isAlt = false;
		while (curIOEdges.hasNext()){
			Edge candidate = curIOEdges.next();
			int eclass = candidate.getLabel().getEClass();
			String cValue = candidate.getLabel().getValue();
			// Remove the surrounding '/' characters:
			if (eclass == GLabel.REGEX){
				cValue = cValue.substring(1, cValue.length() - 1);
			}
			if (eclass == Label.NULL){
				sawNull = true;
				continue;
			}
			if (eclass != Label.EPSILON){
				if (!resultRE.isEmpty()){
					resultRE += "|";
					isAlt = true;
				} 
				resultRE += cValue;
			} else if (eclass == Label.EPSILON){
				sawEpsilon = true;
			}
		}
		if (sawEpsilon && !resultRE.isEmpty()){
			//System.out.println("Parallel epsilon edge; fixing...");
			resultRE = "(" + resultRE + ")?";
		} else if (sawNull && resultRE.isEmpty()){
			// Null edges were seen only:
			return null;
		} else if (isAlt){
			resultRE = "(" + resultRE + ")";
		}
		return resultRE;
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String[] rexes;
		if (args.length > 0){
			rexes = args;
		} else {
			rexes = new String[1];
			rexes[0] = "/a/";
		}
		
		System.out.println("--- GNFA Tests ---");
		
		for (int i = 0; i < rexes.length; i++){
			System.out.println("- Processesing regex: " + rexes[i]);
			ThompsonNFA t = new ThompsonNFA(rexes[i]);
			System.out.println("=- Original NFA:\n" + "-----\n" + t);
		
			GNFA g = new GNFA(t);
			System.out.println("=- GNFA Version:\n" + "-----\n" + g);
			System.out.println("=- Resulting regex: " + g.generateRegex());
		}
	}

}
