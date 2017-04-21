/**
 * 
 */
package net.strasnet.kids.detectorsyntaxproducers;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.semanticweb.owlapi.model.IRI;

import net.strasnet.kids.KIDSOntologyDatatypeValuesException;
import net.strasnet.kids.KIDSOntologyObjectValuesException;
import net.strasnet.kids.constraint.IntegerRange;
import net.strasnet.kids.constraint.IntegerRangeSet;
import net.strasnet.kids.detectors.KIDSGrepDetector;
import net.strasnet.kids.lib.IPv4Address;
import net.strasnet.kids.measurement.KIDSMeasurementOracle;
import net.strasnet.kids.measurement.KIDSUnEvaluableSignalException;

/**
 * @author cstras
 * 
 * This class provides common methods to support the use of extended grep-based detectors / views. It 
 * should be extended by implementing classes.
 * 
 */
public abstract class AbstractGrepSyntax implements KIDSDetectorSyntax {
	
	private static final Logger logme = LogManager.getLogger(AbstractGrepSyntax.class.getName());

	/**
	 * Each extended grep syntax component represents a regular expression that can be used to build a larger 
	 * regular expression. The size limit of an extended grep expression is (???) - If the expression will exceed that
	 * length, it will be split and chained together as multiple invocations.
	 * 
	 * @author cstras
	 *
	 */
	public static final Map <IRI, Map<IRI, ExtendedGrepSyntaxComponent>> validSignalClassMap = 
			new HashMap<IRI, Map<IRI, ExtendedGrepSyntaxComponent>>();

	public interface ExtendedGrepSyntaxComponent {
		public String getSyntaxForm (String sVal, IRI signalConstraint);
	}

	/**
	 * The integer range-set component will convert from integer range set canonical signal values
	 * to an extended grep syntax.
	 * 
	 * Examples of an integer range set canonical form include:
	 * The integerRangeSet has the canonical format [begin:end,b2:e2,...], where each pair of values 
	 * separated by a ':' is a pair of integers.  It also guarantees that each b* <= e*.
	 *
	 * Note that ranges are inclusive on both ends.
	 */
	public class IntegerRangeSetComponent implements ExtendedGrepSyntaxComponent {
		@Override
		/**
		 * Because grep works on a character basis, we need to convert the range set into a set of
		 * strings that will match the correct values.
		 * 
		 * A naive way to do this would be to create a list of all the numbers included, but that could be very large.
		 * 
		 * The only specific characters we need to worry about are those that have digits not subsumed by the range:
		 * 
		 * E.g.: for 1000-2000, we want (1\d{3} | 2000)
		 */
		public String getSyntaxForm(String sVal, IRI signalConsraint){
			
			IntegerRangeSet irs = new IntegerRangeSet(sVal);
			StringBuilder expr = new StringBuilder();
			StringBuilder delim = new StringBuilder("|");
			
			logme.debug(String.format("Building grep component for value %s", sVal));
			
			// Get the beginning and end integer:
			for (IntegerRange ir : irs){
				String start = String.valueOf(ir.getStartValue());
				String end = String.valueOf(ir.getEndValue());
				
				if (start.length() == end.length()){
					logme.debug(String.format("Integers %s and %s are the same length:", start, end));
					expr.append(this.sameLengthMToN(start, end));
					expr.append("|");
				} else {
					// The integers have different numbers of digits:
					// Start by getting the smaller up to the same size, but all nines:
					logme.debug(String.format("Integers %s and %s are different lengths:", start, end));
					int numDigits = start.length();
					
					StringBuilder tN = new StringBuilder();
					for (int i = 0; i < numDigits; i++){
						tN.append("9");
					}
					
					expr.append(this.sameLengthMToN(start, tN.toString()));
					expr.append("|");

					logme.debug(String.format("Including expression for (%s,%s): ", start, tN.toString(), expr));
					
					// Now, for each digit up to end.length(); do the same thing:
					numDigits++;

					while (numDigits < end.length()){
						tN.append("9");
						expr.append(String.join("|",this.upToN(tN.toString(), true)));
						expr.append("|");
						numDigits++;
						logme.debug(String.format("Including expression for upToN(%s)", tN.toString()));
					}
					
					// Finally, add the expression for the larger number:
					expr.append(String.join("|", this.upToN(end, true)));
					expr.append("|");
					logme.debug(String.format("Including expression for upToN(%s)", end));
					
				}
				
				expr.deleteCharAt(expr.length() -1);
				logme.debug(String.format("Expression after (%s,%s): %s", start, end, expr.toString()));

			}
				
			// Finally, wrap in parentheses and beginning/ending delimeters:
			expr.append(")$");
			expr.insert(0, "^(");
			return expr.toString();

		}
		
		/**
		 * Given two integers of the same length, return a regular expression that will match them.
		 * @param M
		 * @param N
		 * @return
		 */
		private String sameLengthMToN(String M, String N){
			StringBuilder expr = new StringBuilder();
			StringBuilder prefix = new StringBuilder();

			// If the numbers are both only one digit, we can simply return them as the range:
			
			// Start by checking for a common prefix:
			int digitsLeft = M.length();
			for (int i = 0; 
				i <M.length() && (Integer.parseInt(N.substring(i,i+1)) ==
									      Integer.parseInt(M.substring(i,i+1))); 
				i++){
				prefix.append(M.substring(i, i+1));
				digitsLeft--;
			}
				
			if (digitsLeft == 1){
				expr.append(String.format("%s[%s-%s]|", prefix,M.substring(M.length()-1),N.substring(N.length()-1)));
			} else {
				// We now have two strings of equal length, where the most significant digit of the larger integer is
				// greater than the most significant digit of the smaller integer.
			
				expr.append(String.format("%s%s[%s-9]|", prefix, M.substring(prefix.length(), M.length()-1), M.substring(M.length() -1)));
				StringBuilder suffix = new StringBuilder("[0-9]");
				for (int i = 1; i < digitsLeft-1 ; i++){
					int digit = Integer.parseInt(M.substring(M.length() - i - 1, M.length() - i));
					if (digit < 9){
						expr.append(String.format("%s%s[%d-9]%s|",
						prefix,
						M.substring(prefix.length(), M.length()-i -1),
						digit+1,
						suffix));
					}
					suffix.append("[0-9]");
				}
				
				int smalld = Integer.parseInt(M.substring(prefix.length(), prefix.length()+1));
				int bigd = Integer.parseInt(N.substring(prefix.length(), prefix.length()+1));
				
				if (bigd - smalld > 1){
					expr.append(String.format("%s[%d-%d]%s",prefix,smalld+1,bigd-1,suffix));
					expr.append("|");
				}
				
				// Finally, work up from the end for the big number too:
				suffix = new StringBuilder();
				for (int i = 0; i < digitsLeft -2; i++){
					int digit = Integer.parseInt(N.substring(N.length() - i - 1, N.length() - i));
					if (digit < 9){
						expr.append(String.format("%s%s[0-%d]%s|",
							prefix,
							N.substring(prefix.length(), N.length()-i -1),
							digit-1,
							suffix));
					}
					suffix.append("[0-9]");
				}
				expr.append(String.format("%s%s[0-%s]%s|",
					prefix,
					N.substring(prefix.length(), prefix.length() + 1),
					N.substring(prefix.length()+1, prefix.length() + 2),
					suffix));
			}
			
			expr.deleteCharAt(expr.length()-1);
			
			return expr.toString();
			
		}
		
		/**
		 * Will generate a regular expression to match all integers of length (n.length()) up to and
		 * including n.
		 * @param n
		 * @return
		 */
		private List<String> upToN(String n, boolean first){
			logme.debug(String.format("Computing upToN(%s)",n));
			List<String> toReturn = new LinkedList<String>();
			if (n.length() == 1){
				toReturn.add(String.format("[0-%s]", n));
			} else {
				int digit = Integer.parseInt(n.substring(0, 1));
				List<String> mStrings = upToN(n.substring(1, n.length()), false);
				for (String t : mStrings){
					toReturn.add(String.format("%d%s", digit, t));
				}
				StringBuilder parts = new StringBuilder();
				for (int i = 0; i < n.length() - 1; i++){
					parts.append("[0-9]");
				}
				if (first){
					if (digit > 1){
						toReturn.add(String.format("[1-%d]%s",digit-1,parts));
					}
				} else {
					if (digit > 0){
						toReturn.add(String.format("[0-%d]%s",digit-1,parts));
					}
				}
			}
			StringBuilder sendingBack = new StringBuilder();
			for (String returning: toReturn){
				sendingBack.append(returning + "|");
			}
			logme.debug(String.format("upToN(%s) returning %s",n, sendingBack));
			return toReturn;
		}

	}
	
	/**
	 * The regular grammar component will convert from regular grammar canonical signal values
	 * to an extended grep syntax.
	 * 
	 * Examples of a regular grammar canonical form include:
	 * 
	 * @author cstras
	 *
	 */
	public class RegularGrammarComponent implements ExtendedGrepSyntaxComponent {
		@Override
		/**
		 * Regular grammars are specified using the form: /expr/, where expr consists of literals, {m,n}, ?, *, +.
		 * This form should be compatible with grep -E as is, however, a ThompsonNFA implementation is available.
		 */
		public String getSyntaxForm(String sVal, IRI signalConsraint){
			String returnVal = sVal;

			// Strip off the '/'s, if present:
			if (sVal.substring(0,1) == "/" && sVal.substring(sVal.length()-1,sVal.length()) == "/"){
				returnVal = sVal.substring(1,sVal.length()-1);
			}
			return returnVal;
		}

	}
	
	public class IPSyntaxComponent implements ExtendedGrepSyntaxComponent {
		
		/**
		 * This class handles translating the canonical IPRange signal representation into an extended grep expression.
		 */

		private Pattern p = Pattern.compile("\\[(?<IPRanges>(([\\d\\.\\/]+),)*([\\d\\.\\/]+))\\]");

		@Override
		public String getSyntaxForm(String sVal, IRI signalConsraint){
			Set<String> octetValues = new HashSet<String>();
			// Parse out the range set thing.  The canonical form is:
			// [aaa.bbb.ccc.ddd/www.xxx.yyy.zzz,...]  So, first split on comma, then determine how many class 'a', 'b', 'c', 'd', etc... there are, and 
			// build the string from that.  It might be a little complicated...
			Matcher m = p.matcher(sVal);
			if (m.matches()){
				String rangeSet = m.group("IPRanges");
				String[] ranges = rangeSet.split(",");

				for (String range : ranges){
					// Determine how many 'classes' are included in the netmask.  If it is all '1's, just include the full IP.  If it is 
					// an exact class 'B', just include the first two octets.  Otherwise, list the octets at the appropriate level.
					String[] components = range.split("/");
					String ip = components[0];
					String nm = components[1];
					// How many bits are in the netmask?
					int[] componentList = IPv4Address.toArray(nm);
					int componentIndex = 0;
					for (int i = 3; i >= 0; i--){
						if (componentList[i] != 0){
							componentIndex = i;
							i = -1;
						}
					}
					// Component index is = the last non-0 octet in the netmask
					
					// If the component index is 255, we just have one thing to list. Otherwise, if it is 'n', we have 255 - n things to list.
					int[] ipOctetList  = IPv4Address.toArray(ip);
					StringBuilder retExpression = new StringBuilder();

					// All the 255 octet values in the netmask can be copied over as-is from the IP address.
					int i = 0;
					for (i = 0; i <= componentIndex && componentList[i] == 255; i++){
						retExpression.append(ipOctetList[i] + ".");
					}
					
					// If there is a non-0 and non-255 value in the netmask, that octet must be enumerated into discrete values:
					if (i < componentList.length && componentList[i] != 0){
						String ipPrefix = retExpression.toString();
						for (int j = 0; j <= (255 - componentList[i]); j++){
						    octetValues.add(ipPrefix + String.valueOf(ipOctetList[i] + j));
						}
					} else {
						retExpression.deleteCharAt(retExpression.lastIndexOf("."));
						octetValues.add(retExpression.toString());
					}
				}
			}
			
			StringBuilder sValBuilder = new StringBuilder();
			sValBuilder.append("(");
			for (String ipVal : octetValues){
				sValBuilder.append(ipVal + "|");
			}
			sValBuilder.deleteCharAt(sValBuilder.lastIndexOf("|"));
			sValBuilder.append(")");
			return sValBuilder.toString();
		}
		
	}

}
