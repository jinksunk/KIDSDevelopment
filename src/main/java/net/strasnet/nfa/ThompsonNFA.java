/**
 * 
 */
package net.strasnet.nfa;

import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;


/**
 * @author chrisstrasburg
 *
 * This class represents a thompson NFA, which is a method of constructing and evaluating
 * NFA-based regular expressions.
 */
public class ThompsonNFA {
	
	String regularExpression;
	//List<Node> eStack; // Total list of nodes

	Node StartNode;
	Node FinalNode;
	Map <Integer,Node> nodeList;
	static int curNodeID = 0;
	
	/**
	 * This constructor generates  
	 */
	public ThompsonNFA(String string) {
		// Parse the regular expression
		Node n = new Node(curNodeID++);
		StartNode = n;
		FinalNode = n;
		nodeList = new TreeMap<Integer,Node>();
		
		// If a string was passed in, try to parse it as a regular expression OR a canonical form description (see toString()):
		if (string != null && ! string.isEmpty()){
			ThompsonNFA newMe = null;
			if (string.startsWith("/") && string.endsWith("/")){
				// Load our Lexer:
				parseThompsonRELexer lexer = new parseThompsonRELexer();
				lexer = new parseThompsonRELexer(new ANTLRStringStream(string));
			
				// Load our parser:
				CommonTokenStream rulesStream = new CommonTokenStream(lexer);
				parseThompsonREParser parser = new parseThompsonREParser(rulesStream);
				try {
					newMe = parser.nfa();
				} catch (RecognitionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					System.exit(1);
				}
			} else {
				// Assume we are doing a canonical form rebuild:
				// Load our Lexer:
				parseCanonicalREStringLexer lexer;
				lexer = new parseCanonicalREStringLexer(new ANTLRStringStream(string));
			
				// Load our parser:
				CommonTokenStream rulesStream = new CommonTokenStream(lexer);
				parseCanonicalREStringParser parser = new parseCanonicalREStringParser(rulesStream);
				try {
					newMe = parser.nfa();
				} catch (RecognitionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					System.exit(1);
				}
			}
			this.StartNode = newMe.getStart();
			this.FinalNode = newMe.getFinal();
		}
		nodeList.put(this.StartNode.getNodeID(), this.StartNode);
		nodeList.put(this.FinalNode.getNodeID(), this.FinalNode);
	}

	public ThompsonNFA() {
		this("");
	}
	
	/**
	 * Create a copy of the current NFA:
	 * @return A semantic copy of this NFA (node IDs are different, but edge labels and structure are identical).
	 */
	public ThompsonNFA createCopy() {
		ThompsonNFA newGuy = new ThompsonNFA();
		
		// For each node, beginning with Start:
		List<Node> myNodeStack = new LinkedList<Node>();
		TreeMap<Integer,Node> myNodes = new TreeMap<Integer,Node>();
		TreeMap<Integer,Node> newNodeMap = new TreeMap<Integer,Node>();
		
		myNodeStack.add(StartNode);
		myNodes.put(StartNode.getNodeID(), StartNode);
		
		while (myNodeStack.size() > 0){
			// Create the node:
			Node n = myNodeStack.remove(0);
			if (!newNodeMap.containsKey(n.getNodeID())){
				newNodeMap.put(n.getNodeID(), new Node(curNodeID++));
			}
			Node newN = newNodeMap.get(n.getNodeID());
			newGuy.nodeList.put(newN.getNodeID(), newN);
			if (n.equals(StartNode)){
				newGuy.setStart(newN);
			}
			if (n.equals(FinalNode)){
				newGuy.setFinal(newN);
			}
			
			// Create an outgoing edge for each one in the current nfa:
			Iterator<Edge> eList = n.getEdges();
			while (eList.hasNext()){
				Edge e = eList.next();
				Node nDest = e.getDest();
				if (!myNodes.containsKey(nDest.getNodeID())){
					myNodeStack.add(nDest);
					myNodes.put(nDest.getNodeID(), nDest);
				}
				// Create a new edge for the new NFA:
				if (!newNodeMap.containsKey(nDest.getNodeID())){
					newNodeMap.put(nDest.getNodeID(), new Node(curNodeID++));
				}
				Label newL = new Label(e.getLabel().getValue(),e.getLabel().getEClass());
				Node newDest = newNodeMap.get(nDest.getNodeID());
				newGuy.nodeList.put(newDest.getNodeID(), newDest);
				Edge newE = new Edge(newL, newDest);
				newN.addEdge(newE);
			}
		}
		return newGuy;
	}
	
	public Node getStart(){
		return StartNode;
	}

	public Node getFinal(){
		return FinalNode;
	}
	
	public void setStart(Node freshStart){
		this.StartNode = freshStart;
		nodeList.put(freshStart.getNodeID(), freshStart);
	}
	
	public void setFinal(Node newEnding){
		this.FinalNode = newEnding;
		nodeList.put(newEnding.getNodeID(), newEnding);
	}
	
	/**
	 * Returns a list of Nodes, sorted by their ID.
	 * @return
	 */
	public Collection<Node> getNodes(){
		List<Node> myNodeStack = new LinkedList<Node>();
		TreeMap<Integer,Node> myNodes = new TreeMap<Integer,Node>();
		myNodeStack.add(StartNode);
		myNodes.put(StartNode.getNodeID(), StartNode);
		
		while (myNodeStack.size() > 0){
			Node n = myNodeStack.remove(0);
			Iterator<Edge> eList = n.getEdges();
			while (eList.hasNext()){
				Edge e = eList.next();
				Node nDest = e.getDest();
				if (!myNodes.containsKey(nDest.getNodeID())){
					myNodeStack.add(nDest);
					myNodes.put(nDest.getNodeID(), nDest);
				}
			}
		}
		
		return myNodes.values();
		
	}
	
	/**
	 * Concatenates this nfa object with appendage.
	 * @param appendage - The NFA to be grafted onto the end of this one.
	 */
	public void concat(ThompsonNFA appendage){
		Label l = new Label(Label.classes.get(Label.EPSILON), Label.EPSILON);
		Edge e = new Edge(l, appendage.getStart());
		FinalNode.addEdge(e);
		this.setFinal(appendage.getFinal());
	}

	public String toString(){
		// Starting at the beginning, print the NFA as a giant edge list:
		Node cur;
		HashMap<Integer,Node> processed = new HashMap();
		List<Node> toProcess = new LinkedList<Node>();
		toProcess.add(StartNode);
		processed.put(StartNode.getNodeID(),StartNode);
		String s = "";
		s += "<START," + StartNode + ">\n";
		
		while (toProcess.size() > 0){
			cur = toProcess.remove(0);
			processed.put(cur.getNodeID(),cur);
			Iterator<Edge> curI = cur.getEdges();
			while (curI.hasNext()){
				Edge curE = curI.next();
				if (curE.getLabel().getEClass() == Label.NULL){
					continue;
				}
				Node newDest = curE.getDest();
				Label curL = curE.getLabel();
				// For each outgoing edge, create a string for it:
				s += "<" + cur + "," + curL.classes.get(curL.getEClass()) + "," + curL.getValue() + "," + newDest + ">\n";
				
				// If not already processed, add node to the list to process:
				if (!processed.containsKey(newDest.getNodeID())){
					toProcess.add(newDest);
					processed.put(newDest.getNodeID(), newDest);
				}
			}
		}

		s += "<FINAL," + FinalNode + ">\n";
		
		return s;
	}

	
	public void createQuest(){
		Label l = new Label(Label.classes.get(Label.EPSILON), Label.EPSILON);
		Edge e = new Edge(l, FinalNode);
		getStart().addEdge(e);
	}
	
	public void createStar(){
		Label l = new Label(Label.classes.get(Label.EPSILON), Label.EPSILON);
		Edge e = new Edge(l, getStart());
		getFinal().addEdge(e);
		System.out.println(this);
		
		Node newF = new Node(curNodeID++);
		l = new Label(Label.classes.get(Label.EPSILON), Label.EPSILON);
		e = new Edge(l, newF);
		this.setFinal(newF);
		getStart().addEdge(e);
		System.out.println("After addStar:\n" + this);
	}
	
	public void createAlt(ThompsonNFA a){
	//	System.out.println("Creating alt for:\n\tnfa1:\n" + this + "\n\tnfa2:\n" + a);
		Label l = new Label(Label.classes.get(Label.EPSILON), Label.EPSILON);
		Edge e = new Edge(l, getFinal());
		a.getFinal().addEdge(e);
		l = new Label(Label.classes.get(Label.EPSILON), Label.EPSILON);
		e = new Edge(l, a.getStart());
		getStart().addEdge(e);
	//	System.out.println("Result:\n" + this + "\n");

	}
	
	/*
	 * "Syntactic Sugar" methods below here:
	 */
	/**
	 * 
	 */
	public void createPlus() {
		ThompsonNFA newT = this.createCopy();
		newT.createStar();
		this.concat(newT);
	}

	/**
	 * Generate 'n' - 1 copies of this nfa, concatenating them all together.
	 * @param integer
	 */
	public void multiply(Integer n) {
		ThompsonNFA tCopy = this.createCopy();
		for (int i = 0; i < n-1; i++){
			this.concat(tCopy.createCopy());
		}
	}
	
	/** Static Methods **/
	public static ThompsonNFA createLiteral(String s){
		ThompsonNFA n = new ThompsonNFA();
		Node f = new Node(curNodeID++);
		Label l = new Label(s, Label.LITERAL);
		Edge e = new Edge(l, f);
		n.setFinal(f);
		n.getStart().addEdge(e);
		
		return n;
	}
	
	public static ThompsonNFA createDot(){
		ThompsonNFA n = new ThompsonNFA();
		Node f = new Node(curNodeID++);
		Label l = new Label(".", Label.DOT);
		Edge e = new Edge(l, f);
		n.setFinal(f);
		n.getStart().addEdge(e);
		
		return n;
	}
	
	/**
	 *
	 * @param snodeID
	 * @return The node with the given node ID; if that node did not previous exist, it is created and
	 * curNodeID is set to snodeID+1.
	 */
	public Node getNodeByID(int snodeID) {
		Integer snodeIDI = new Integer(snodeID);
		if (!nodeList.containsKey(snodeIDI)){
			nodeList.put(snodeIDI, new Node(snodeID));
		}
		if (ThompsonNFA.curNodeID <= snodeID){
			ThompsonNFA.curNodeID = snodeID + 1;
		}
		return nodeList.get(snodeIDI);
	}
	
	/** Test Code **/
	public static void main(String[] argv){
		ThompsonNFA litTest = ThompsonNFA.createLiteral("h");
/*		
		System.out.println("LitTest nfa:\n" + litTest);
		
		litTest.createQuest();
		
		System.out.println("QuestTest nfa:\n" + litTest);

		litTest = ThompsonNFA.createLiteral("h");
		litTest.createStar();
		System.out.println("StarTest nfa:\n" + litTest);
		
		litTest = ThompsonNFA.createLiteral("h");
		litTest.createAlt(ThompsonNFA.createLiteral("k"));
		System.out.println("AltTest nfa:\n" + litTest);
		
		litTest.concat(ThompsonNFA.createLiteral("m"));
		System.out.println("ConcatTest NFA:\n" + litTest);
		*/
		litTest = new ThompsonNFA("/ab{4,8}/");
		System.out.println("ParsingTest NFA:\n" + litTest);
		
		Collection<Node> nodes = litTest.getNodes();
		Iterator<Node> i = nodes.iterator();
		System.out.println("Get nodes() test:\n");
		while (i.hasNext()){
			System.out.println("\t" + i.next());
		}

		
		System.out.println("Rebuild from string Test:");
		System.out.println("Original NFA:\n-----\n" + litTest);
		String litTestString = litTest.toString();
		ThompsonNFA litTestReborn = new ThompsonNFA(litTestString);
		System.out.println("New NFA:\n-----\n" + litTestReborn);
		
		GNFA gTest = new GNFA(litTestReborn);
		System.out.println("GNFA Test:\n" + gTest);
		
		System.out.println("Reconstructed Regex from GNFA:" + gTest.generateRegex());
	}

}