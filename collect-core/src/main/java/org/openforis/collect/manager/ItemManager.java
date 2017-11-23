package org.openforis.collect.manager;

import java.util.List;

import org.openforis.collect.model.User;
import org.openforis.idm.metamodel.PersistedObject;

public interface ItemManager<T extends PersistedObject, I extends Object> {

	List<T> loadAll();

	T loadById(I id);

	T save(T obj, User modifiedByUser);

	void delete(T obj);

	void deleteById(I id);

}