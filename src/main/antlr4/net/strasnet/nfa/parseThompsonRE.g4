grammar parseThompsonRE;

// An ANTLR grammar which will:
//  <UL>
//    <LI> Parse a regular expression string and build an NFA for it. </LI>
//  </UL>

options {
  language = Java;
}

@header {
  package net.strasnet.nfa;
  import java.util.LinkedList;
  import java.util.List;
  import java.util.Iterator;
  import java.util.HashMap;
  }
  
//@lexer::header {
 // package net.strasnet.nfa;
//}

@members {
  List <Character> opStack;
  List <ThompsonNFA> argStack;
  
  void concatTerm(){
    if (argStack.size() > 1 && argStack.get(argStack.size() - 2) != null){
      ThompsonNFA t2 = argStack.remove(argStack.size() - 1);
      ThompsonNFA t1 = argStack.remove(argStack.size() - 1);
      t1.concat(t2);
      argStack.add(t1);
    }
  }
}

// The "base" concept; an entire snort rule
//  A match of the first option produces:
//   * a signature
//   * an IP context, including all features
nfa returns [ThompsonNFA result]
  @init { ThompsonNFA result; opStack = new LinkedList<Character>(); argStack = new LinkedList<ThompsonNFA>();}
  @after { result = argStack.remove(0); if (argStack.size() > 0){ System.err.println("Parsing incorrect!");}}
  : SLASH expression SLASH modifiers*
  ;
  
// The part of the rule which specifies action, protocol, source, destination, and direction.
//   Get signal contexts for PROTOCOL as well as signal, if it is a signal.
expression
  : catExpression (ALT {argStack.add(null); } catExpression
    { 
      ThompsonNFA test = argStack.remove(argStack.size() - 2);
      if (test != null){ 
        System.err.println("Unmatched '|' found in input!\n (Got partial:)" + test); 
      }
       ThompsonNFA t2 = argStack.remove(argStack.size() - 1);
       //System.out.println("Got partial " + t2);
       ThompsonNFA t1 = argStack.remove(argStack.size() - 1);
       t1.createAlt(t2);
       argStack.add(t1);
     }
     )*
  ;

catExpression
  @after {
    // Keep concatenating until we see a 'null' or the stack is empty:
    int i = argStack.size() - 1;
    while (i > 0 && argStack.get(i-1) != null){
      ThompsonNFA t2 = argStack.remove(i--);
      ThompsonNFA t1 = argStack.remove(i);
      t1.concat(t2);
      argStack.add(t1);
    }
  }
  : term term*
  ;

term
  : atom op?
  ;

atom
  : character
    {
      ThompsonNFA t = ThompsonNFA.createLiteral($character.c);
      argStack.add(t);
    } 
  | DOT
    {
      ThompsonNFA t = ThompsonNFA.createDot();
      argStack.add(t);
    }
  | LPAREN {argStack.add(null);} expression RPAREN {
      ThompsonNFA test = argStack.remove(argStack.size() - 2);
      if (test != null){ 
        System.err.println("Unmatched ')' found in input!\n (Got partial:)" + test); 
      }
    }
  ;

op
  : STAR
    {
      ThompsonNFA t = argStack.remove(argStack.size() - 1);
      t.createStar();
      argStack.add(t);
    } 
  | QUEST 
    {
      ThompsonNFA t = argStack.remove(argStack.size() - 1);
      t.createQuest();
      argStack.add(t);
    } 
  | PLUS
    {
      ThompsonNFA t = argStack.remove(argStack.size() - 1);
      t.createPlus();
      argStack.add(t);
    }
  | qrange
    {
      // The strategy here is, for range (n,m), concatenate 'n' of the previous expression, followed by 'm-n' of the
      // previous expression '?'.
      ThompsonNFA t = argStack.remove(argStack.size() - 1);
      ThompsonNFA tq = t.createCopy();
      tq.createQuest();
      t.multiply($qrange.range.get(0));
      if (($qrange.range.get(1) - $qrange.range.get(0)) > 0){
        tq.multiply($qrange.range.get(1) - $qrange.range.get(0));
        t.concat(tq);
      }
      argStack.add(t);
    }
  ;

qrange returns [List<Integer> range]
  @init {List<Integer> range = new LinkedList<Integer>();}
  : '{' fd=DIGIT+ ',' sd=DIGIT+ '}' {range.add(new Integer($fd.text)); range.add(new Integer($sd.text));}
  | '{' fd=DIGIT+ ',' '}' {range.add(new Integer($fd.text)); range.add(new Integer(-1));}
  | '{' ',' sd=DIGIT+ '}' {range.add(new Integer(0)); range.add(new Integer($sd.text));}
  | '{' sd=DIGIT+ '}' {range.add(new Integer($sd.text)); range.add(new Integer($sd.text));}
  ;

character returns [String c]
  : VALIDCHARS {$c = $VALIDCHARS.text; }
  | ESCAPEDCHARS {$c = $ESCAPEDCHARS.text.substring(1); }
  ;
  
modifiers
  : 'i'
  ;
    
SLASH
  : '/'
  ;
  
STAR
  : '*'
  ;

RPAREN
  : ')'
  ;
  
LPAREN
  : '('
  ;

QUEST
  : '?'
  ;
  
PLUS
  : '+'
  ;
  
ALT
  : '|'
  ;

DOT
  : '.'
  ;
  
DIGIT
  : '0'..'9'
  ;
  
VALIDCHARS
  : ('A'..'Z' | 'a'..'z' | DIGIT | '_' | '-' | '=')
  ;
  
BACKSLASH
  : '\\'
  ;
    
ESCAPEDCHARS
  : BACKSLASH ( BACKSLASH | '/' | '.' | '+' | '*' | '|' | '?' | '{' | '}')
  ;