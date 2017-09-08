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
		D extends PersistedObjectDao<T, I>> implements ItemManager<T, I> {

	protected D dao;
	
	/* (non-Javadoc)
	 * @see org.openforis.collect.manager.ItemManager#loadAll()
	 */
	@Override
	public List<T> loadAll() {
		return dao.loadAll();
	}
	
	/* (non-Javadoc)
	 * @see org.openforis.collect.manager.ItemManager#loadById(I)
	 */
	@Override
	public T loadById(I id) {
		T obj = dao.loadById(id);
		return obj;
	}

	/* (non-Javadoc)
	 * @see org.openforis.collect.manager.ItemManager#save(T)
	 */
	@Override
	public T save(T obj) {
		if (obj.getId() == null) {
			dao.insert(obj);
		} else {
			dao.update(obj);
		}
		return obj;
	}
	
	/* (non-Javadoc)
	 * @see org.openforis.collect.manager.ItemManager#delete(T)
	 */
	@Override
	@SuppressWarnings("unchecked")
	public void delete(T obj) {
		deleteById((I) obj.getId());
	}

	/* (non-Javadoc)
	 * @see org.openforis.collect.manager.ItemManager#delete(I)
	 */
	@Override
	public void deleteById(I id) {
		dao.delete(id);
	}
	
	public D getDao() {
		return dao;
	}
	
	public void setDao(D dao) {
		this.dao = dao;
	}
	
}
