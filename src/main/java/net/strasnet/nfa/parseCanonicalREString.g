grammar parseCanonicalREString;

// An ANTLR grammar which will:
//  <UL>
//    <LI> Parse the string form of a ThompsonNFA and rebuild it. </LI>
//  </UL>

options {
  language = Java;
}

@header {
  package net.strasnet.nfa;
  import java.util.LinkedList;
  import java.util.Iterator;
  import java.util.HashMap;
  }
  
@lexer::header {
  package net.strasnet.nfa;
}

@members {
  ThompsonNFA resultNFA;  
}

// The "base" concept; an entire snort rule
//  A match of the first option produces:
//   * a signature
//   * an IP context, including all features
nfa returns [ThompsonNFA result]
  @init { result = new ThompsonNFA(); }
  : line[result]+
  ;
  
// The part of the rule which specifies action, protocol, source, destination, and direction.
//   Get signal contexts for PROTOCOL as well as signal, if it is a signal.
line[ThompsonNFA result]
  : LAB lineContent[result] RAB NEWLINE
  ;
  
lineContent[ThompsonNFA result]
  : startDeclaration[result] {result.setStart($startDeclaration.n);}
  | finalDeclaration[result] {result.setFinal($finalDeclaration.n);}
  | edge [result]
  ;

startDeclaration [ThompsonNFA t] returns [Node n]
  : START LINEDELIM nodeid {n = t.getNodeByID($nodeid.i);}
  ;

finalDeclaration [ThompsonNFA t] returns [Node n]
  : FINAL LINEDELIM nodeid {n = t.getNodeByID($nodeid.i);}
  ;

edgetype returns [int etype]
  : EPSILON {etype = Label.EPSILON;}
  | LITERAL {etype = Label.LITERAL;}
  | DOTLABEL {etype = Label.DOT;}
  ;
  
edge [ThompsonNFA t] 
  @init {int snodeID; int dnodeID; Edge e = new Edge(); String lv; int lt;}
  @after {e.setLabel(new Label(lv, lt)); 
          Node n = t.getNodeByID(snodeID); // If the node doesn't exist already, create it in the nfa
          Node d = t.getNodeByID(dnodeID);
          e.setDest(d);
          n.addEdge(e);
         }
  : n1=nodeid {snodeID = new Integer($n1.i); System.out.print("<" + snodeID);} 
    LINEDELIM edgetype {lt = $edgetype.etype; System.out.print("," + lt);} 
    LINEDELIM edgevalue {lv = $edgevalue.v; System.out.print("," + lv);} 
    LINEDELIM n2=nodeid {dnodeID = $n2.i; System.out.println("," + dnodeID + ">");}
  ;
  
nodeid returns [int i]
  : DIGIT {i = Integer.parseInt($DIGIT.text);}
  | INTEGER {i = Integer.parseInt($INTEGER.text);}
  ;

edgevalue returns [String v]
  : EPSILON {v = Label.classes.get(Label.EPSILON);}
  | DOT {v = Label.classes.get(Label.DOT);}
  | validchars {v = $validchars.s;}
  ;
  
validchars returns [String s]
  : DIGIT {s = $DIGIT.text;}
  | ESCAPEDCHARS {s = $ESCAPEDCHARS.text;}
  | LETTER {s = $LETTER.text;}
  | ',' {s = ",";}
  ;
    
LAB
  : '<'
  ;
  
RAB
  : '>'
  ;

START
  : 'START'
  ;
  
FINAL
  : 'FINAL'
  ;
  
LINEDELIM
  : ','
  ;

EDGEVALUE
  : 'value'
  ;

EPSILON
  : 'epsilon'
  ;

LITERAL
  : 'Literal'
  ;

DOTLABEL
  : 'DOT'
  ;
  
DOT
  : '.'
  ;
  
DIGIT
  : '0'..'9'
  ;
  
INTEGER
  : DIGIT DIGIT+
  ;
  
BACKSLASH
  : '\\'
  ;
    
ESCAPEDCHARS
  : BACKSLASH ( BACKSLASH | '/' | '.' | '+' | '*' | '|' | '?')
  ;
  
LETTER
  : 'a'..'z'
  | 'A'..'Z'
  ;
  
NEWLINE
  : '\n'
  ;
