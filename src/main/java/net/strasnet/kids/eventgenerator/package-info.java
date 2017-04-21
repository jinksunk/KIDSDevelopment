/**
 * This package contains the code to generate event data for available data sets. This allows 
 * Java code to modify sample data files to add packets, log entries, etc... as well as generating data files.
 * 
 * The overall design includes:
 * <UL>
 * <LI> An interface, KIDSEvent, which defines the methods to get data elements (packets, log lines, etc...)
 *      for an implementing class. </LI>
 * <LI> Code to generate data with corresponding label entries for supported dataset views </LI>
 * </UL>
 */
/**
 * @author cstras
 *
 */
package net.strasnet.kids.eventgenerator;