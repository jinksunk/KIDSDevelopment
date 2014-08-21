/**
 * This package contains the classes for producing bro syntaxes.  These are intended to be stand alone files, where:
 * * Each context represents a bro event (e.g. TCPPacketEvent), 
 * * Each domain / constraint pair produce a different bro function to be called from the context
 * * Multiple signals within a single context are 'and'ed together
 *    
 */
/**
 * @author cstras
 *
 */
package net.strasnet.kids.bro;