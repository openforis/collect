/**
 * 
 */
package org.openforis.collect.persistence.jooq;

import org.jooq.Configuration;
import org.jooq.Record;
import org.jooq.Sequence;
import org.jooq.StoreQuery;
import org.jooq.TableField;
import org.openforis.idm.metamodel.PersistedObject;

/**
 * @author S. Ricci
 *
 */
public abstract class PersistedObjectMappingDSLContext<T extends PersistedObject> extends MappingDSLContext<T> {

	private static final long serialVersionUID = 1L;
	
	public PersistedObjectMappingDSLContext(Configuration config,
			TableField<?, Integer> idField,
			Sequence<? extends Number> idSequence, Class<T> clazz) {
		super(config, idField, idSequence, clazz);
	}

	@Override
	protected void fromObject(T o, StoreQuery<?> q) {
	}

	@Override
	protected void fromRecord(Record r, T o) {
		o.setId(r.getValue(getIdField()));
	}
	
	@Override
	protected void setId(T entity, int id) {
		entity.setId(id);
	}

	@Override
	protected Integer getId(T entity) {
		return entity.getId();
	}
	
}
