package org.openforis.collect.persistence;

import static org.openforis.collect.persistence.jooq.Sequences.SCHEMA_DEFINITION_ID_SEQ;
import static org.openforis.collect.persistence.jooq.Sequences.SURVEY_ID_SEQ;
import static org.openforis.collect.persistence.jooq.tables.SchemaDefinition.SCHEMA_DEFINITION;
import static org.openforis.collect.persistence.jooq.tables.Survey.SURVEY;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jooq.Record;
import org.jooq.Result;
import org.jooq.impl.Factory;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.Schema;
import org.openforis.idm.metamodel.Survey;
import org.openforis.idm.metamodel.xml.InvalidIdmlException;
import org.openforis.idm.metamodel.xml.SurveyMarshaller;
import org.openforis.idm.metamodel.xml.SurveyUnmarshaller;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author G. Miceli
 * @author M. Togna
 */
public class SurveyDAO extends CollectDAO {

	private Map<String, Survey> surveysByName;
	private Map<Integer, Survey> surveysById;

	public SurveyDAO() {
		surveysById = new HashMap<Integer, Survey>();
		surveysByName = new HashMap<String, Survey>();
	}

	@Transactional
	public void importModel(Survey survey) throws SurveyImportException {
		String idml = marshalSurvey(survey);

		// Insert into SURVEY table
		Factory jf = getJooqFactory();
		int surveyId = jf.nextval(SURVEY_ID_SEQ).intValue();
		jf.insertInto(SURVEY).set(SURVEY.ID, surveyId).set(SURVEY.NAME, survey.getName()).set(SURVEY.IDML, idml).execute();

		survey.setId(surveyId);

		// Insert SCHEMA_DEFINITIONs
		Schema schema = survey.getSchema();
		Collection<NodeDefinition> definitions = schema.getDefinitions();
		for (NodeDefinition definition : definitions) {
			int definitionId = jf.nextval(SCHEMA_DEFINITION_ID_SEQ).intValue();
			String path = definition.getPath();
			jf.insertInto(SCHEMA_DEFINITION).set(SCHEMA_DEFINITION.ID, definitionId).set(SCHEMA_DEFINITION.SURVEY_ID, surveyId).set(SCHEMA_DEFINITION.PATH, path).execute();
			definition.setId(definitionId);
		}

		surveysById.put(survey.getId(), survey);
		surveysByName.put(survey.getName(), survey);
	}

	public Survey load(int id) {
		Survey survey = surveysById.get(id);
		return survey;
	}

	public Survey load(String name) {
		Survey survey = surveysByName.get(name);
		return survey;
	}

	@Transactional
	public List<Survey> loadAll() {
		Factory jf = getJooqFactory();
		List<Survey> surveys = new ArrayList<Survey>();
		Result<Record> results = jf.select().from(SURVEY).fetch();
		for (Record row : results) {
			Survey survey = processSurveyRow(row);
			if (survey != null) {
				loadNodeDefinitions(survey);
				surveys.add(survey);

				surveysById.put(survey.getId(), survey);
				surveysByName.put(survey.getName(), survey);
			}
		}
		return surveys;
	}

	private Survey processSurveyRow(Record row) {
		try {
			if (row == null) {
				return null;
			}
			String idml = row.getValueAsString(SURVEY.IDML);
			Survey survey = unmarshalIdml(idml);
			survey.setId(row.getValueAsInteger(SURVEY.ID));
			return survey;
		} catch (IOException e) {
			throw new RuntimeException("Error deserializing IDML from database", e);
		}
	}

	private Survey unmarshalIdml(String idml) throws IOException {
		byte[] bytes = idml.getBytes("UTF-8");
		ByteArrayInputStream is = new ByteArrayInputStream(bytes);
		SurveyUnmarshaller su = new SurveyUnmarshaller();
		Survey survey;
		try {
			survey = su.unmarshal(is);
		} catch (InvalidIdmlException e) {
			throw new DataInconsistencyException("Invalid idm");
		}
		return survey;
	}

	private void loadNodeDefinitions(Survey survey) {
		Factory jf = getJooqFactory();
		// Internal IDs by path and associate with each node in tree
		Schema schema = survey.getSchema();
		Result<Record> result = jf.select().from(SCHEMA_DEFINITION).where(SCHEMA_DEFINITION.SURVEY_ID.equal(survey.getId())).fetch();
		for (Record defnRecord : result) {
			int defnId = defnRecord.getValueAsInteger(SCHEMA_DEFINITION.ID);
			String path = defnRecord.getValueAsString(SCHEMA_DEFINITION.PATH);
			NodeDefinition defn = schema.getByPath(path);
			defn.setId(defnId);
		}
	}

	private String marshalSurvey(Survey survey) throws SurveyImportException {
		try {
			// Serialize Survey to XML
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			SurveyMarshaller sm = new SurveyMarshaller();
			sm.setIndent(true);
			sm.marshal(survey, os);
			return os.toString("UTF-8");
		} catch (IOException ex) {
			throw new SurveyImportException("Error unmarshalling survey", ex);
		}
	}
}
