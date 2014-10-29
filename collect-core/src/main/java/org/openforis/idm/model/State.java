/**
 * 
 */
package org.openforis.idm.model;

import java.util.BitSet;

import org.apache.commons.lang3.StringUtils;

/**
 * @author M. Togna
 * 
 */
public class State {
	
	private static final char TRUE_SYMBOL = '1';
	private static final char FALSE_SYMBOL = '0';

	private static final int N_BITS = 8;
	private static final double MAX_VALUE = Math.pow( 2, N_BITS );

	private transient BitSet bitSet;
	
	public State() {
		bitSet = new BitSet(N_BITS);
	}

	public void set(int position, boolean state) {
		ensurePositionInRange(position);
		bitSet.set(position, state);
	}

	public boolean get(int position) {
		ensurePositionInRange(position);
		return bitSet.get(position);
	}

	public int intValue() {
		String booleanString = getBooleanString();
		return Integer.parseInt( booleanString, 2 );
	}

	private String getBooleanString() {
		StringBuilder str = new StringBuilder(N_BITS);
		for (int i = N_BITS - 1; i >= 0; i--) {
			boolean b = bitSet.get(i);
			str.append( b ? TRUE_SYMBOL : FALSE_SYMBOL );
		}
		return str.toString();
	}

	public void set(int value) {
		if ( value > MAX_VALUE ) {
			throw new IllegalArgumentException("Value cannot be grater than " + MAX_VALUE + ", but it was " + value);
		}
		String binaryString = Integer.toBinaryString(value);
		binaryString = StringUtils.leftPad(binaryString, N_BITS, FALSE_SYMBOL);
		char[] charArray = binaryString.toCharArray();

		int pos = binaryString.length();
		for ( char c : charArray ) {
			set(--pos, c == TRUE_SYMBOL);
		}
	}
	
	public static State parseState(int value) {
		State state = new State();
		state.set(value);
		return state;
	}
	
	private void ensurePositionInRange(int position) {
		if( position < 0 || position >= N_BITS ) {
			throw new IllegalArgumentException("Posion must be greather than 0 and less that  " + (N_BITS-1) );
		}
	}

	@Override
	public String toString() {
		return getBooleanString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((bitSet == null) ? 0 : bitSet.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		State other = (State) obj;
		if (bitSet == null) {
			if (other.bitSet != null)
				return false;
		} else if (!bitSet.equals(other.bitSet))
			return false;
		return true;
	}
}
