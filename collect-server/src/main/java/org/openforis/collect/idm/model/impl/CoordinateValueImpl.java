/**
 * 
 */
package org.openforis.collect.idm.model.impl;

import org.openforis.idm.metamodel.SpatialReferenceSystem;
import org.openforis.idm.model.Coordinate;

/**
 * @author M. Togna
 * 
 */
public class CoordinateValueImpl extends AbstractValue implements Coordinate {

	public CoordinateValueImpl(String x, String y, String z, String srsId) {
		super(x);
		super.setValue2(x);
		super.setValue3(z);
		super.setValue4(srsId);
	}

	public CoordinateValueImpl(String x, String y, String srsId) {
		this(x, y, null, srsId);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openforis.idm.model.Coordinate#getX()
	 */
	@Override
	public Long getX() {
		try {
			return Long.parseLong(getValue1());
		} catch (NumberFormatException e) {
			return null;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openforis.idm.model.Coordinate#getY()
	 */
	@Override
	public Long getY() {
		try {
			return Long.parseLong(getValue2());
		} catch (NumberFormatException e) {
			return null;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openforis.idm.model.Coordinate#getZ()
	 */
	@Override
	public Long getZ() {
		try {
			return Long.parseLong(getValue3());
		} catch (NumberFormatException e) {
			return null;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openforis.idm.model.Coordinate#getSpatialReferenceSystem()
	 */
	@Override
	public SpatialReferenceSystem getSpatialReferenceSystem() {
		// TODO Auto-generated method stub
		return null;
	}

}
