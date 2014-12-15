package org.openforis.collect.manager;

import org.openforis.collect.persistence.jooq.MappingJooqDaoSupport;
import org.openforis.idm.metamodel.PersistedObject;

/**
 * 
 * @author S. Ricci
 *
 */
public abstract class AbstractPersistedObjectManager<T extends PersistedObject, D extends MappingJooqDaoSupport<T, ?>> {

	private D dao;
	
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
		dao.delete(obj.getId());
	}
	
	public D getDao() {
		return dao;
	}
	
	public void setDao(D dao) {
		this.dao = dao;
	}
	
}
