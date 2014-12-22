package org.openforis.collect.manager;

import java.util.List;

import org.openforis.collect.persistence.jooq.MappingJooqDaoSupport;
import org.openforis.idm.metamodel.PersistedObject;

/**
 * 
 * @author S. Ricci
 *
 */
public abstract class AbstractPersistedObjectManager<T extends PersistedObject, D extends MappingJooqDaoSupport<T, ?>> {

	protected D dao;
	
	public List<T> loadAll() {
		return dao.loadAll();
	}
	
	public T loadById(int id) {
		T obj = dao.loadById(id);
		return obj;
	}

	public void save(T obj) {
		if (obj.getId() == null) {
			dao.insert(obj);
		} else {
			dao.update(obj);
		}
	}
	
	public void delete(T obj) {
		delete(obj.getId());
	}

	public void delete(int id) {
		dao.delete(id);
	}
	
	public D getDao() {
		return dao;
	}
	
	public void setDao(D dao) {
		this.dao = dao;
	}
	
}
