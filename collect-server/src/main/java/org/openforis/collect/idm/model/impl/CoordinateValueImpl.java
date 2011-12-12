/**
 * 
 */
package org.openforis.collect.idm.model.impl;

import org.apache.commons.lang3.StringUtils;
import org.openforis.idm.metamodel.SpatialReferenceSystem;
import org.openforis.idm.model.Coordinate;

/**
 * @author M. Togna
 * 
 */
public class CoordinateValueImpl extends AbstractValue implements Coordinate {

	public CoordinateValueImpl(String x, String y, String z, String srsId) {
		super(x);
		super.setText2(x);
		super.setText3(z);
		super.setText4(srsId);
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
			return Long.parseLong(getText1());
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
			return Long.parseLong(getText2());
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
			return Long.parseLong(getText3());
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

	@Override
	public boolean isFormatValid() {
		return !(getX() == null || getY() == null || StringUtils.isEmpty(getText4()));
	}

	public static void main(String[] args) {
		String x = "423423";
		String y = "fds";
		String srsId = "fsdfsa";
		CoordinateValueImpl c = new CoordinateValueImpl(x, y, srsId);
		System.out.println(c.isFormatValid());
		System.exit(0);
	}

}
