package org.openforis.collect.manager;

import java.util.List;

import org.openforis.collect.persistence.PersistedObjectDao;
import org.openforis.idm.metamodel.PersistedObject;

/**
 * 
 * @author S. Ricci
 *
 */
public abstract class AbstractPersistedObjectManager<T extends PersistedObject, I extends Object, 
		D extends PersistedObjectDao<T, I>> {

	protected D dao;
	
	public List<T> loadAll() {
		return dao.loadAll();
	}
	
	public T loadById(I id) {
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
	
	@SuppressWarnings("unchecked")
	public void delete(T obj) {
		delete((I) obj.getId());
	}

	public void delete(I id) {
		dao.delete(id);
	}
	
	public D getDao() {
		return dao;
	}
	
	public void setDao(D dao) {
		this.dao = dao;
	}
	
}
