package org.openforis.collect.manager;

import java.util.List;

import org.openforis.idm.metamodel.PersistedObject;

public interface ItemManager<T extends PersistedObject, I extends Object> {

	List<T> loadAll();

	T loadById(I id);

	T save(T obj);

	void delete(T obj);

	void deleteById(I id);

}