package org.openforis.collect.persistence;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;

import org.apache.commons.io.IOUtils;
import org.jooq.Record;
import org.openforis.collect.manager.SurveyMigrator;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.SurveySummary;
import org.openforis.collect.persistence.jooq.JooqDaoSupport;
import org.openforis.collect.persistence.xml.CollectSurveyIdmlBinder;
import org.openforis.commons.io.OpenForisIOUtils;
import org.openforis.idm.metamodel.Survey;
import org.openforis.idm.metamodel.xml.IdmlParseException;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *  @author S. Ricci
 */
abstract class SurveyBaseDao extends JooqDaoSupport {
	
	@Autowired
	protected CollectSurveyIdmlBinder surveySerializer;
	
	public void init() {
	}

	protected abstract CollectSurvey processSurveyRow(Record row);

	protected abstract SurveySummary processSurveySummaryRow(Record row);
	
	public CollectSurvey unmarshalIdml(String idml) throws IdmlParseException {
		byte[] bytes;
		try {
			bytes = idml.getBytes("UTF-8");
			ByteArrayInputStream is = new ByteArrayInputStream(bytes);
			return unmarshalIdml(is);
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}
	
	public CollectSurvey unmarshalIdml(InputStream is) throws IdmlParseException {
		return unmarshalIdml(OpenForisIOUtils.toReader(is));
	}
	
	public CollectSurvey unmarshalIdml(InputStream is, boolean includeCodeListItems) throws IdmlParseException {
		return unmarshalIdml(OpenForisIOUtils.toReader(is), includeCodeListItems);
	}

	public CollectSurvey unmarshalIdml(Reader reader) throws IdmlParseException {
		return unmarshalIdml(reader, true);
	}
	
	public CollectSurvey unmarshalIdml(Reader reader, boolean includeCodeListItems) throws IdmlParseException {
		try {
			CollectSurvey survey = (CollectSurvey) surveySerializer.unmarshal(reader, includeCodeListItems);
			SurveyMigrator migrator = getSurveyMigrator();
			migrator.migrate(survey);
			return survey;
		} finally {
			IOUtils.closeQuietly(reader);
		}
	}

	public String marshalSurvey(Survey survey) throws SurveyImportException {
		return surveySerializer.marshal(survey);
	}
	
	protected SurveyMigrator getSurveyMigrator() {
		return new SurveyMigrator();
	}
	
	public CollectSurveyIdmlBinder getSurveySerializer() {
		return surveySerializer;
	}
	
	public void setSurveySerializer(CollectSurveyIdmlBinder surveySerializer) {
		this.surveySerializer = surveySerializer;
	}
	
}
