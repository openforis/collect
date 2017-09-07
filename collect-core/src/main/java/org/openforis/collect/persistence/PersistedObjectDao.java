package org.openforis.collect.persistence;

import java.util.List;

import org.openforis.idm.metamodel.PersistedObject;

public interface PersistedObjectDao<O extends PersistedObject, I extends Object> {
	
	List<O> loadAll();
	
	O loadById(I id);

	void insert(O item);
	
	void update(O item);
	
	void delete(I id);
}
