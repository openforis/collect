/**
 * 
 */
package org.openforis.collect.model;

import org.openforis.idm.metamodel.Check;
import org.openforis.idm.model.CheckFailure;

/**
 * @author M. Togna
 * 
 */
public class CheckFailureImpl implements CheckFailure {

	private Check check;

	public CheckFailureImpl() {
		super();
	}

	public CheckFailureImpl(Check check) {
		super();
		this.check = check;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openforis.idm.model.CheckFailure#getCheck()
	 */
	@Override
	public Check getCheck() {
		return check;
	}

}
