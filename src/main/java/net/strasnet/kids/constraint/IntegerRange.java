package net.strasnet.kids.constraint;

/**
 * 
 * @author cstras
 * This class represents an Integer Range object.
 */

	public class IntegerRange {
		private int sVal;
		private int eVal;
		
		public IntegerRange(int s, int e){
			sVal = s;
			eVal = e;
		}
		
		public int getStartValue(){
			return sVal;
		}

		public int getEndValue(){
			return eVal;
		}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
