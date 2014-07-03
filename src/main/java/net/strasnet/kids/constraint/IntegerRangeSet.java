/**
 * 
 */
package net.strasnet.kids.constraint;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author cstras
 *  This class represents a set of integer ranges.  It is iterable, providing an iterator
 *  over IntegerRange objects.  It expects input in the form:
 *  [i1:i2,i3:i4,...], and assumes the following:
 *  1) Integer ranges are non-overlapping (or that the overlap is intentional)
 *  2) In a pair ix:iy, ix <= iy
 *  3) There is at least one range in the set, i.e. [] is invalid.
 *  4) Each pair has two values, that is: [ix:,iw:iy] is invalid.
 *  
 *  Iteration is unordered.
 */

	public class IntegerRangeSet implements Iterable<IntegerRange> {

		private Pattern p = Pattern.compile("\\[(?<integerRanges>(\\d+:\\d+,)*(\\d+:\\d+))\\]");
		private String sourceString;
		private Set<IntegerRange> ourRanges;
		
		public IntegerRangeSet(String sVal){
			sourceString = sVal;
			ourRanges = new HashSet<IntegerRange>();

			Matcher m = p.matcher(sourceString);
			StringBuilder sValBuilder = new StringBuilder();
			sValBuilder.append("");
			if (m.matches()){
				String rangeSet = m.group("integerRanges");
				String[] ranges = rangeSet.split(",");
				for (String range : ranges){
					String[] components = range.split(":");
					IntegerRange tRange = new IntegerRange(Integer.parseInt(components[0]), Integer.parseInt(components[1]));
					ourRanges.add(tRange);
				}
			}
		}

		@Override
		public Iterator<IntegerRange> iterator() {
			return ourRanges.iterator();
	}

}
