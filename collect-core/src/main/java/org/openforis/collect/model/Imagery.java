package org.openforis.collect.model;

import org.openforis.collect.persistence.jooq.tables.pojos.OfcImagery;
import org.openforis.idm.metamodel.PersistedObject;

public class Imagery extends OfcImagery implements PersistedObject {

	private static final long serialVersionUID = 1L;

	public Imagery() {}
	
	public Imagery(OfcImagery imagery) {
		super(imagery);
	}

}
