package org.openforis.idm.metamodel;

public interface PersistedObject<I extends Number> {

	I getId();

	void setId(I id);

}