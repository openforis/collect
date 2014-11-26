/**
 * 
 */
package org.openforis.collect.persistence.jooq;

import java.sql.Connection;

import org.jooq.Sequence;
import org.jooq.TableField;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.idm.metamodel.SurveyObject;

/**
 * @author S. Ricci
 *
 */
public abstract class SurveyObjectMappingDSLContext<T extends SurveyObject> extends MappingDSLContext<T> {

	private static final long serialVersionUID = 1L;
	
	private CollectSurvey survey;

	public SurveyObjectMappingDSLContext(Connection conn,
			TableField<?, Integer> idField,
			Sequence<? extends Number> idSequence, Class<T> clazz, CollectSurvey survey) {
		super(conn, idField, idSequence, clazz);
		this.survey = survey;
	}

	public CollectSurvey getSurvey() {
		return survey;
	}
	
}
