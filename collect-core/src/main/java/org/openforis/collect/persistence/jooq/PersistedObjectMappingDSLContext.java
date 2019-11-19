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
public abstract class PersistedObjectMappingDSLContext<I extends Number, T extends PersistedObject<I>> extends MappingDSLContext<I, T> {

	private static final long serialVersionUID = 1L;
	
	public PersistedObjectMappingDSLContext(Configuration config,
			TableField<?, I> idField,
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
	protected void setId(T entity, I id) {
		entity.setId(id);
	}

	@Override
	protected I getId(T entity) {
		return entity.getId();
	}
	
}
