/**
 * 
 */
package org.openforis.collect.persistence.jooq;

import org.jooq.Configuration;
import org.jooq.Record;
import org.jooq.Sequence;
import org.jooq.StoreQuery;
import org.jooq.TableField;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.idm.metamodel.PersistedSurveyObject;

/**
 * @author S. Ricci
 *
 */
public abstract class SurveyObjectMappingDSLContext<I extends Number, T extends PersistedSurveyObject<I>> extends MappingDSLContext<I, T> {

	private static final long serialVersionUID = 1L;
	
	protected CollectSurvey survey;

	public SurveyObjectMappingDSLContext(Configuration config,
			TableField<?, I> idField,
			Sequence<? extends Number> idSequence, 
			Class<T> clazz, CollectSurvey survey) {
		super(config, idField, idSequence, clazz);
		this.survey = survey;
	}

	@Override
	protected void fromObject(T o, StoreQuery<?> q) {
		q.addValue(getIdField(), o.getId());
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
	
	public CollectSurvey getSurvey() {
		return survey;
	}
	
}
