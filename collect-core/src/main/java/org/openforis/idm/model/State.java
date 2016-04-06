/**
 * 
 */
package org.openforis.idm.model;

import java.util.BitSet;

/**
 * @author M. Togna
 * 
 */
public class State {
	
	private static final int N_BITS = 8;
	private static final double MAX_VALUE = Math.pow( 2, N_BITS );

	private transient BitSet bitSet;
	
	public State() {
		bitSet = new BitSet(N_BITS);
	}

	public static State parseState(int value) {
		State state = new State();
		state.set(value);
		return state;
	}
	
	public void set(int position, boolean state) {
		ensurePositionInRange(position);
		setInternal(position, state);
	}

	public boolean get(int position) {
		ensurePositionInRange(position);
		return getInternal(position);
	}

	public boolean isEmpty() {
		for (int i = 0; i < N_BITS; i++) {
			if (getInternal(i)) {
				return false;
			}
		}
		return true;
	}
	
	public int intValue() {
		int value = 0;
		for (int i = 0; i < N_BITS; i++) {
			if (getInternal(i)) {
				value += (1 << i); //value += 2 powered by i
			}
		}
		return value;
	}

	public void set(int value) {
		if ( value > MAX_VALUE ) {
			throw new IllegalArgumentException("Value cannot be grater than " + MAX_VALUE + ", but it was " + value);
		}
		int currentValue = value;
		for (int i = 0; i < N_BITS; i++) {
			setInternal(i, currentValue % 2 > 0);
			currentValue >>= 1;
		}
	}
	
	private void ensurePositionInRange(int position) {
		if( position < 0 || position >= N_BITS ) {
			throw new IllegalArgumentException("Posion must be greather than 0 and less that  " + (N_BITS-1) );
		}
	}
	
	private boolean getInternal(int position) {
		return bitSet.get(position);
	}

	private void setInternal(int position, boolean state) {
		bitSet.set(position, state);
	}

	@Override
	public String toString() {
		return Integer.toString(intValue(), 2);
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
