grammar snortRules;

// An ANTLR grammar which will:
//  <UL>
//    <LI> Parse a snort rules file </LI>
//    <LI> Extract signals, signal contexts, features, events, signatures </LI>
//    <LI> Update a knowledge base with IDS, signals, signal contexts,  features, events, signatures </LI>
//  </UL>
//
// During initialization, the following axioms are checked / added to the knowledge base:
//  <UL>
//    <LI> Contexts for IP, TCP, UDP, ICMP </LI>
//    <LI> Features for each defined context </LI>
//  </UL>
options {
  language = Java;
}

@header {
  package net.strasnet.kids.datasources;
  import org.semanticweb.owlapi.model.OWLOntology;
  import org.semanticweb.owlapi.model.AddAxiom;
  import org.semanticweb.owlapi.model.IRI;
  import org.semanticweb.owlapi.model.OWLClass;
  import org.semanticweb.owlapi.model.OWLDataFactory;
  import org.semanticweb.owlapi.model.OWLNamedIndividual;
  import org.semanticweb.owlapi.reasoner.OWLReasoner;
  import java.net.InetAddress;
  import java.net.Inet4Address;
  import java.net.UnknownHostException;
  import java.util.LinkedList;
  import java.util.Iterator;
  import java.util.HashMap;
  import net.strasnet.kids.*;
  }
  
@lexer::header {
  package net.strasnet.kids.datasources;
}

@members {

HashMap<IRI, KIDSContext> myCs = new HashMap<IRI, KIDSContext>();
HashMap<IRI, KIDSFeature> myFs = new HashMap<IRI, KIDSFeature>();

KIDSContext getContext(KIDSContext candidate){
  if (myCs.containsKey(candidate.getIRI())){
    return myCs.get(candidate.getIRI());
  } else {
    myCs.put(candidate.getIRI(), candidate);
    return candidate;
  }
}

KIDSFeature getFeature(KIDSFeature candidate){
  if (myFs.containsKey(candidate.getIRI())){
    return myFs.get(candidate.getIRI());
  } else {
    myFs.put(candidate.getIRI(), candidate);
    return candidate;
  }
}

}

// The "base" concept; an entire snort rule
//  A match of the first option produces:
//   * a signature
//   * an IP context, including all features
rule [OWLOntology onto, IRI ontoIRI, OWLDataFactory f, OWLReasoner r] returns [KIDSSnortSignature toadd]
  @init { toadd = new KIDSSnortSignature(onto, ontoIRI, f, r); }
  : header[onto, ontoIRI, f, r, $toadd] 
               ('(' body [onto,ontoIRI,f,r, $toadd] ) ')'? 
                            
  | NEWLINE
  | COMMENT
  | EOF
  ;
  
// The part of the rule which specifies action, protocol, source, destination, and direction.
//   Get signal contexts for PROTOCOL as well as signal, if it is a signal.
header [OWLOntology onto, IRI ontoIRI, OWLDataFactory f, OWLReasoner r, KIDSSnortSignature kss] 
  : RULEACTION PROTOCOL snetspec[onto, ontoIRI, f, r, kss] dirspec[onto, ontoIRI, f, r, kss] dnetspec[onto, ontoIRI, f, r, kss] // TODO: replace feature string w/ feature object
    { KIDSIPProtocolNumberFeature kipf = (KIDSIPProtocolNumberFeature) getFeature (new KIDSIPProtocolNumberFeature(onto, ontoIRI, f, r));
      KIDSIPPacketContext kipc = (KIDSIPPacketContext) getContext(new KIDSIPPacketContext(onto, ontoIRI, f, r));
      kipf.setContext(kipc);
      
      KIDSIPProtocolNumberSignal kps = new KIDSIPProtocolNumberSignal(onto, ontoIRI, f, r);
      Integer p;
      if ($PROTOCOL.text.equals("tcp")){
        p = new Integer(6);
      } else if ($PROTOCOL.text.equals("icmp")){
        p = new Integer(1);
      } else if ($PROTOCOL.text.equals("udp")){
        p = new Integer(17);
      } else {
        p = new Integer(-1);
      }
      kps.addDefinition(p);
      kps.setFeature(kipf);
      kss.addSignal(kps); 
    }
  ;
  
// The source network addess specification:
snetspec [OWLOntology onto, IRI ontoIRI, OWLDataFactory f, OWLReasoner r, KIDSSnortSignature kss]
  : ks=netspec[onto, ontoIRI, f, r] {
      if (ks != null){
        KIDSSourceIPAddressFeature ksipf = (KIDSSourceIPAddressFeature) getFeature(new KIDSSourceIPAddressFeature(onto, ontoIRI, f, r));
        KIDSIPPacketContext kipc = (KIDSIPPacketContext) getContext(new KIDSIPPacketContext(onto, ontoIRI, f, r));
        ksipf.setContext(kipc);
        ks.setFeature(ksipf);
        kss.addSignal(ks);
      } 
    }
  ;
  
// The destination network addess specification:
dnetspec [OWLOntology onto, IRI ontoIRI, OWLDataFactory f, OWLReasoner r, KIDSSnortSignature kss]
  : ks=netspec[onto, ontoIRI, f, r] {
      if (ks != null){
        KIDSDestinationIPAddressFeature ksipf = (KIDSDestinationIPAddressFeature) getFeature(new KIDSDestinationIPAddressFeature(onto, ontoIRI, f, r));
        KIDSIPPacketContext kipc = (KIDSIPPacketContext) getContext(new KIDSIPPacketContext(onto, ontoIRI, f, r));
        ksipf.setContext(kipc);
        ks.setFeature(ksipf);
        kss.addSignal(ks);
      } 
    }
  ;
  
// The network address and, if applicable, port number of the signature.  
netspec [OWLOntology onto, IRI ontoIRI, OWLDataFactory f, OWLReasoner r]  returns [KIDSSignal kas]
  : netaddrspec[onto, ontoIRI, f, r] 
     {
      if ($netaddrspec.toadd.size() > 0){
        kas = new KIDSIPAddressSignal(onto, ontoIRI, f, r);
        Iterator<KIDSSnortIPAddressRange> kiprI = $netaddrspec.toadd.iterator();
        while (kiprI.hasNext()){
          kas.addDefinition(kiprI.next());
        }
      } 
     }
    (netportspec[onto, ontoIRI, f, r] 
     {
       // KIDSPortNumberSignal kpns = new KIDSPortNumberSignal();  //TODO: Add to signature, and add feature to signal.
       // $toadd.addAll($netportspec.toadd);
     }
    )?
  ;

//   * Get signals source/dest IP specification, if appropriate
netaddrspec[OWLOntology onto, IRI ontoIRI, OWLDataFactory f, OWLReasoner r] returns [List<KIDSSnortIPAddressRange> toadd]
  @init { $toadd = new LinkedList<KIDSSnortIPAddressRange>(); }
  : addrsetspec[onto, ontoIRI, f, r] {$toadd = $addrsetspec.toadd; }
  | SNORTVAR
  | 'any'
  ;
  
addrsetspec[OWLOntology onto, IRI ontoIRI, OWLDataFactory f, OWLReasoner r] returns [List<KIDSSnortIPAddressRange> toadd]
  @init { $toadd = new LinkedList<KIDSSnortIPAddressRange>(); }
  : nms1=netmaskspec[onto, ontoIRI, f, r] {$toadd.add($nms1.toadd); }
  | '[' ( nms1=netmaskspec[onto, ontoIRI, f, r]{$toadd.add($nms1.toadd); } ',' )* nms2=netmaskspec[onto, ontoIRI, f, r]{$toadd.add($nms2.toadd); } ']'
  ;

// Generates individuals for IP-based signals
netmaskspec[OWLOntology onto, IRI ontoIRI, OWLDataFactory f, OWLReasoner r] returns [KIDSSnortIPAddressRange toadd]
  @init { String netmask = "255.255.255.255"; String ip = "";}
  @after { 
    try {
      $toadd = new KIDSSnortIPAddressRange(InetAddress.getByName(ip), netmask); 
    } catch (UnknownHostException e){
      System.err.println("Parser Error: Cannot parse " + ip + "\n");
      e.printStackTrace();
    }
  }
  : ips1=ipspec{ip = $ips1.ipaddr; } ('/' ips2=ipspec {netmask = $ips2.ipaddr; })?
  ;
  
ipspec returns [String ipaddr]
  : o1=INTEGER '.' o2=INTEGER '.' o3=INTEGER '.' o4=INTEGER
     {ipaddr = $o1.text + "." + $o2.text + "." + $o3.text + "." + $o4.text; }
  ;

//  * Get signals source/dest port numbers, if appropriate
netportspec[OWLOntology onto, IRI ontoIRI, OWLDataFactory f, OWLReasoner r] returns [List<String> toadd]
  @init { $toadd = new LinkedList<String>(); }
  : INTEGER {$toadd.add($INTEGER.text);}
  | SNORTVAR //TODO: environment relative signals?
  | 'any'
  ;

// * Get traffic direction signal, if restricted
dirspec[OWLOntology onto, IRI ontoIRI, OWLDataFactory f, OWLReasoner r, KIDSSnortSignature kss] returns [List<String> toadd]
  @init { $toadd = new LinkedList<String>(); }
  : '->' 
  | '<>'
  ;

// The "meat" of the rule; this is where most of the data-level signals are obtained.  In addition, features of protocols from IP on up
// are mined from the body.
body[OWLOntology onto, IRI ontoIRI, OWLDataFactory f, OWLReasoner r, KIDSSnortSignature kss]
  : (ruleoption[onto, ontoIRI, f, r, kss] OPTIONDELIM)+
  ;

ruleoption[OWLOntology onto, IRI ontoIRI, OWLDataFactory f, OWLReasoner r, KIDSSnortSignature kss]
  : generaloption[onto, ontoIRI, f, r, kss]
  | payloadoption[onto, ontoIRI, f, r, kss]
  | nonpayloadoption[onto, ontoIRI, f, r, kss]
//  | postdetectionoption
  | unknownoption
  ;

generaloption[OWLOntology onto, IRI ontoIRI, OWLDataFactory f, OWLReasoner r, KIDSSnortSignature kss]
  : 'msg' KEYWORDDELIM msgparams[onto, ontoIRI, f, r, kss]
  | 'reference' KEYWORDDELIM referenceparams[onto, ontoIRI, f, r, kss]
  | 'gid' KEYWORDDELIM gidparams
  | 'sid' KEYWORDDELIM sidparams[onto, ontoIRI, f, r, kss]
  | 'rev' KEYWORDDELIM revparams
  | 'classtype' KEYWORDDELIM CLASSTYPES
  | 'priority' KEYWORDDELIM priorityparams
  | 'metadata' KEYWORDDELIM metadataparams
  ;

msgparams[OWLOntology onto, IRI ontoIRI, OWLDataFactory f, OWLReasoner r, KIDSSnortSignature kss]
  : QUOTEDSTRING {$kss.setEvent($QUOTEDSTRING.text);}
  ;

referenceparams[OWLOntology onto, IRI ontoIRI, OWLDataFactory f, OWLReasoner r, KIDSSnortSignature kss]
  : rt=('url' | 'cve') ',' od=~OPTIONDELIM* {$kss.addRef($rt.text, $od.text);}
  ;

sidparams[OWLOntology onto, IRI ontoIRI, OWLDataFactory f, OWLReasoner r, KIDSSnortSignature kss]
  : INTEGER {$kss.setSID($INTEGER.text);}
  ;

gidparams
  : INTEGER
  ;

revparams
  : INTEGER
  ;

priorityparams
  : INTEGER
  ;

metadataparams
  : METADATATYPES
  | othermetadata
  ;

othermetadata
  : ~(METADATATYPES) ~(OPTIONDELIM)
  ;

payloadoption[OWLOntology onto, IRI ontoIRI, OWLDataFactory f, OWLReasoner r, KIDSSnortSignature kss]
  @init { KIDSDataSignal curCD = new KIDSDataSignal(onto, ontoIRI, f, r);
          KIDSIPDataFeature kipdf = (KIDSIPDataFeature) getFeature(new KIDSIPDataFeature(onto, ontoIRI, f, r));
          KIDSIPPacketContext kipc = (KIDSIPPacketContext) getContext(new KIDSIPPacketContext(onto, ontoIRI, f, r));
          kipdf.setContext(kipc);
          curCD.setFeature(kipdf);
        }
  @after { curCD.finalizeRE();  kss.addSignal(curCD); }
  : content[onto, ontoIRI, f, r, curCD] (OPTIONDELIM contentmodifiers[onto,ontoIRI,f,r,curCD])* 
  | uricontent[onto, ontoIRI, f, r, kss, curCD] (OPTIONDELIM uricontentmodifiers[onto,ontoIRI,f,r,curCD])*
  ;
  
content[OWLOntology onto, IRI ontoIRI, OWLDataFactory f, OWLReasoner r, KIDSDataSignal curCD]
  : 'content:' QUOTEDSTRING 
    {
      List<String>l = new LinkedList<String>(); 
      l.add(curCD.DataComponent);
      l.add($QUOTEDSTRING.text); 
      curCD.addDefinition(l);
    }
  ;
  
contentmodifiers[OWLOntology onto, IRI ontoIRI, OWLDataFactory f, OWLReasoner r, KIDSDataSignal curCD] // todo: remove curFeature, curContext
  : within[onto, ontoIRI, f, r,curCD]
  | nocase[onto, ontoIRI, f, r,curCD]
  | offset[onto, ontoIRI, f, r,curCD]
  | depth[onto, ontoIRI, f, r, curCD]
  | distance[onto, ontoIRI, f, r, curCD]
  | rawbytes[onto, ontoIRI, f, r, curCD]
  | http_client_body
    { 
      KIDSHTTPPacketContext hpc = (KIDSHTTPPacketContext) getContext(new KIDSHTTPPacketContext(onto, ontoIRI, f, r));
      KIDSHTTPClientBodyFeature khcbf = (KIDSHTTPClientBodyFeature) getFeature(new KIDSHTTPClientBodyFeature(onto, ontoIRI, f, r));
      khcbf.setContext(hpc);
      curCD.setFeature(khcbf);
    }
  | http_cookie
  | http_raw_cookie
  | http_header
  | http_raw_header
  | http_method
  | http_uri
      { 
      KIDSHTTPPacketContext hpc = (KIDSHTTPPacketContext) getContext(new KIDSHTTPPacketContext(onto, ontoIRI, f, r));
      KIDSHTTPURIFeature khcbf = (KIDSHTTPURIFeature) getFeature(new KIDSHTTPURIFeature(onto, ontoIRI, f, r));
      khcbf.setContext(hpc);
      curCD.setFeature(khcbf);
    }
  | http_raw_uri
  | http_stat_msg
  | http_stat_code
  | http_encode
  ;
  
within[OWLOntology onto, IRI ontoIRI, OWLDataFactory f, OWLReasoner r, KIDSDataSignal curCD]
  : 'within:' INTEGER
      { 
        List<String> l = new LinkedList<String>();
        l.add(curCD.WithinComponent);
        l.add($INTEGER.text);
        curCD.addDefinition(l);
      }
  ;

nocase[OWLOntology onto, IRI ontoIRI, OWLDataFactory f, OWLReasoner r, KIDSDataSignal curCD]
  : 'nocase' 
      { 
        List<String> l = new LinkedList<String>();
        l.add(curCD.NocaseComponent);
        l.add(null);
        curCD.addDefinition(l);
      }
  ;

offset[OWLOntology onto, IRI ontoIRI, OWLDataFactory f, OWLReasoner r, KIDSDataSignal curCD]
  : 'offset:' INTEGER
      { 
        List<String> l = new LinkedList<String>();
        l.add(curCD.OffsetComponent);
        l.add($INTEGER.text);
        curCD.addDefinition(l);
      }
  ;
  
depth[OWLOntology onto, IRI ontoIRI, OWLDataFactory f, OWLReasoner r, KIDSDataSignal curCD]
  : 'depth:' INTEGER
      { 
        List<String> l = new LinkedList<String>();
        l.add(curCD.DepthComponent);
        l.add($INTEGER.text);
        curCD.addDefinition(l);
      }
  ;
    
distance[OWLOntology onto, IRI ontoIRI, OWLDataFactory f, OWLReasoner r, KIDSDataSignal curCD]
  : 'distance:' INTEGER
      { 
        List<String> l = new LinkedList<String>();
        l.add(curCD.DistanceComponent);
        l.add($INTEGER.text);
        curCD.addDefinition(l);
      }
  ;

rawbytes[OWLOntology onto, IRI ontoIRI, OWLDataFactory f, OWLReasoner r, KIDSDataSignal curCD]
  : 'rawbytes'
      { 
        List<String> l = new LinkedList<String>();
        l.add(curCD.RawbytesComponent);
        l.add(null);
        curCD.addDefinition(l);
      }
  ;
  
//
// http* options change the scope of the preceding content modifier to only the HTTP body.
// The method assumeContentSignals() has been provided to allow subcontexts to assume ownership
// of a parent contexts signals (note this is called by the subcontexts themselved, not by
// the parser code).
// 
// Each of the http* matches must check to see if the IPPacket has a subcontext, and if that
// subcontext has an HTTPPacket subcontext.  If now, the rule will must create it.
//
http_client_body
  : 'http_client_body'
  ;
  
http_cookie
  : 'http_cookie'
  ;

http_raw_cookie
  : 'http_raw_cookie'
  ;

http_header
  : 'http_header'
  ;

http_raw_header
  : 'http_raw_header'
  ;

http_method
  : 'http_method'
  ;

http_uri
  : 'http_uri'
  ;

http_raw_uri
  : 'http_raw_uri'
  ;

http_stat_code
  : 'http_stat_code'
  ;

http_stat_msg
  : 'http_stat_msg'
  ;

http_encode
  : 'http_encode'
  ;

//threshold[OWLOntology onto, IRI ontoIRI, OWLDataFactory f, OWLReasoner r, KIDSSnortSignature kss]
//  : 'threshold:' (thresholdspec ','?)+
//  ;
//
//thresholdspec[OWLOntology onto, IRI ontoIRI, OWLDataFactory f, OWLReasoner r, KIDSSnortSignature kss]
//  : 'type' ttype
//  | 'track' ttrack
//  | 'count' tcount
//  | 'seconds' tsecs
//  ;
//  
//ttype[OWLOntology onto, IRI ontoIRI, OWLDataFactory f, OWLReasoner r, KIDSSnortSignature kss]
//  : 'limit'
//  | 'both'
//  ;
//  
//ttrack[OWLOntology onto, IRI ontoIRI, OWLDataFactory f, OWLReasoner r, KIDSSnortSignature kss]
//  : 'by_src'
//  ;
//  
//tcount[OWLOntology onto, IRI ontoIRI, OWLDataFactory f, OWLReasoner r, KIDSSnortSignature kss]
//  : INTEGER
//  ;
//  
//tsecs[OWLOntology onto, IRI ontoIRI, OWLDataFactory f, OWLReasoner r, KIDSSnortSignature kss]
//  : INTEGER
//  ;
//

uricontent  [OWLOntology onto, IRI ontoIRI, OWLDataFactory f, OWLReasoner r, KIDSSnortSignature kss, KIDSDataSignal curCD]
  : 'uricontent:' QUOTEDSTRING
    {
      List<String>l = new LinkedList<String>(); 
      l.add(curCD.DataComponent);
      l.add($QUOTEDSTRING.text); 
      curCD.addDefinition(l);
      
      KIDSHTTPPacketContext hpc = (KIDSHTTPPacketContext) getContext(new KIDSHTTPPacketContext(onto, ontoIRI, f, r));
      KIDSHTTPURIFeature khcbf = (KIDSHTTPURIFeature) getFeature(new KIDSHTTPURIFeature(onto, ontoIRI, f, r));
      khcbf.setContext(hpc);
      curCD.setFeature(khcbf);
    }
  ;
  
uricontentmodifiers [OWLOntology onto, IRI ontoIRI, OWLDataFactory f, OWLReasoner r, KIDSDataSignal curCD]
  : within[onto, ontoIRI, f, r, curCD]
  | nocase[onto, ontoIRI, f, r, curCD]
  | offset[onto, ontoIRI, f, r, curCD]
  | depth[onto, ontoIRI, f, r, curCD]
  | distance[onto, ontoIRI, f, r, curCD]
  ;
  
pcre
  : 'pcre:' QUOTEDSTRING
//    {
//      List<String>l = new LinkedList<String>(); 
//      l.add(curCD.pcreComponent);
//      l.add($QUOTEDSTRING.text); 
//      curCD.addDefinition(l);
//    }
  ;

nonpayloadoption[OWLOntology onto, IRI ontoIRI, OWLDataFactory f, OWLReasoner r, KIDSSnortSignature kss]
  : flow[onto, ontoIRI, f, r, kss] { 
         }
  | itype[onto, ontoIRI, f, r, kss] { 
         }
  | icode[onto, ontoIRI, f, r, kss] { 
         }
  ;

itype[OWLOntology onto, IRI ontoIRI, OWLDataFactory f, OWLReasoner r, KIDSSnortSignature kss]
  : 'itype:' INTEGER //TODO: Set feature and context in signal
       {
       }
  ;
  
icode[OWLOntology onto, IRI ontoIRI, OWLDataFactory f, OWLReasoner r, KIDSSnortSignature kss]
  : 'icode:' INTEGER //TODO: Set feature and context in signal
       {
       }
  ;
  
flow[OWLOntology onto, IRI ontoIRI, OWLDataFactory f, OWLReasoner r, KIDSSnortSignature kss] returns [String toadd]
  @init{String[] flowargs = new String[4];} //TODO: Set context, feature, and signal
  : 'flow:' (fd2=flowdirection{flowargs[0] = $fd2.toadd;}
              | fs3=flowstate {flowargs[1] = $fs3.toadd;}
              | fs4=flowstream {flowargs[2] = $fs4.toadd;}
              | ff2=flowfrag{flowargs[3] = $ff2.toadd;}
            ) 
            (',' 
              (fs1=flowstate{flowargs[1] = $fs1.toadd;}
                | fd1=flowdirection{flowargs[0] = $fd1.toadd;}
                | fs2=flowstream{flowargs[2] = $fs2.toadd;}
                | ff1=flowfrag{flowargs[3] = $ff1.toadd;}
              )
            )*
  ;

flowdirection returns [String toadd]
  : 'to_server' {$toadd = "to_server";}
  | 'from_server' {$toadd = "from_server";}
  | 'to_client' {$toadd = "from_server";}
  | 'from_client' {$toadd = "to_server";}
  ;

flowstate returns [String toadd]
  : 'established' {$toadd = ",established";}
  | 'not_established' {$toadd = ",not_established";}
  | 'stateless'
  ;

flowstream returns [String toadd]
  : 'no_stream' {$toadd = ",no_stream"; }
  | 'only_stream' {$toadd = ",only_stream"; }
  ;

flowfrag returns [String toadd]
  : 'no_frag' {$toadd = ",no_frag"; }
  | 'only_frag' {$toadd = ",only_frag"; }
  ;

//flags
//  : 'flags:' flagspec
//  ;
//  
//flagspec
//  : ('S' | 'F' | 'A' | 'R' | 'P' | 'U' | '0')+ (',' ('12' | '1' | '2'))?
//  ;
  
//postdetectionoption[OWLOntology onto, IRI ontoIRI, OWLDataFactory f, OWLReasoner r, KIDSSnortSignature kss]
//  : postdetectionkeyword ':' post-detectionparams
//  ;
//  
unknownoption
  : op1=~(OPTIONDELIM | KEYWORDDELIM)+ KEYWORDDELIM? ~(OPTIONDELIM | KEYWORDDELIM)*{
      System.out.println("WARNING: Unknown Option Encountered: " + $op1);
    }
  ;
    
COMMENT
  : '#' ~( '\r' | '\n' )* (NEWLINE | EOF) {$channel = HIDDEN; }
  ;
  
WS 
  : (' ' | '\t') {$channel = HIDDEN; }
  ;

NOTNEWLINE
  : '\\' '\r'? '\n'
  | '\\' '\r'
  ;

NEWLINE
  : '\r'? '\n'
  | '\r'
  ;

SNORTVAR
  : '$'('A'..'Z' | '_')+
  ;
  
INTEGER 
  : '0'..'9'+
  ;
  
QUOTEDSTRING
  : '"' ('\\' '"' | '\\' ~'"' | ~('\\' | '"') )+ '"'
  ;

OPTIONDELIM
  : ';'
  ;
  
KEYWORDDELIM
  : ':'
  ;
  
CLASSTYPES
  : 'attempted-admin'
  | 'attempted-dos'
  | 'attempted-recon'
  | 'attempted-user'
  | 'bad-unknown'
  | 'default-login-attempt'
  | 'denial-of-service'
  | 'icmp-event'
  | 'inappropriate-content'
  | 'misc-activity'
  | 'misc-attack'
  | 'network-scan'
  | 'non-standard-protocol'
  | 'not-suspicious'
  | 'policy-violation'
  | 'protocol-command-decode'
  | 'rpc-portmap-decode'
  | 'shellcode-detect'
  | 'string-detect'
  | 'successful-admin'
  | 'successful-dos'
  | 'successful-recon-largescale'
  | 'successful-recon-limited'
  | 'successful-user'
  | 'suspicious-filename-detect'
  | 'suspicious-login'
  | 'system-call-detect'
  | 'tcp-connection'
  | 'trojan-activity'
  | 'unknown'
  | 'unsuccessful-user'
  | 'unusual-client-port-connection'
  | 'web-application-activity'
  | 'web-application-attack'
  ;
  
METADATATYPES
  : 'engine' 'shared'
  | 'soid' INTEGER
  | 'service' ~OPTIONDELIM+
  ;
  
  
RULEACTION
  : 'alert'
  | 'log'
  | 'pass'
  | 'activate'
  | 'dynamic'
  | 'drop'
  | 'reject'
  | 'sdrop'
  ;  
  
PROTOCOL 
  : 'tcp' 
  | 'udp' 
  | 'icmp' 
  | 'ip'
  ;
  
VALIDCHARS
  : ('A'..'Z' | 'a'..'z' | '0'..'9' | '_' | '-' | '?' | '=' | '/')+
  ;