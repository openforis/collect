package org.openforis.collect.datacleansing;

import java.util.Arrays;
import java.util.Date;
import java.util.UUID;

import org.junit.Before;
import org.openforis.collect.CollectIntegrationTest;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.persistence.SurveyImportException;
import org.openforis.idm.metamodel.AttributeDefinition;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.PersistedSurveyObject;
import org.openforis.idm.metamodel.xml.IdmlParseException;

/**
 * 
 * @author S. Ricci
 *
 */
public abstract class DataCleansingIntegrationTest extends CollectIntegrationTest {
	
	protected CollectSurvey survey;
	
	@Before
	public void init() throws SurveyImportException, IdmlParseException {
		survey = importModel();
	}
	
	public static class PersistedSurveyObjectBuilder<T extends PersistedSurveyObject> {
		
		protected T obj;
		
		public PersistedSurveyObjectBuilder(CollectSurvey survey, Class<T> type) {
			try {
				obj = (T) type.getDeclaredConstructor(CollectSurvey.class).newInstance(survey);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		
		public PersistedSurveyObjectBuilder<T> id(Integer id) {
			obj.setId(id);
			return this;
		}
		
		public PersistedSurveyObjectBuilder<T> uuid(String uuid) {
			obj.setUuid(UUID.fromString(uuid));
			return this;
		}
		
		public PersistedSurveyObjectBuilder<T> creationDate(Date date) {
			obj.setCreationDate(date);
			return this;
		}
		
		public PersistedSurveyObjectBuilder<T> modifiedDate(Date date) {
			obj.setModifiedDate(date);
			return this;
		}
		
		public T build() {
			return obj;
		}
		
	}
	
	public static class DataQueryBuilder extends PersistedSurveyObjectBuilder<DataQuery> {
		
		public DataQueryBuilder(CollectSurvey survey) {
			super(survey, DataQuery.class);
		}

		@Override
		public DataQueryBuilder id(Integer id) {
			return (DataQueryBuilder) super.id(id);
		}
		
		@Override
		public DataQueryBuilder uuid(String uuid) {
			return (DataQueryBuilder) super.uuid(uuid);
		}
		
		@Override
		public DataQueryBuilder creationDate(Date date) {
			return (DataQueryBuilder) super.creationDate(date);
		}
		
		@Override
		public DataQueryBuilder modifiedDate(Date date) {
			return (DataQueryBuilder) super.modifiedDate(date);
		}
		
		public DataQueryBuilder title(String title) {
			obj.setTitle(title);
			return this;
		}
		
		public DataQueryBuilder description(String description) {
			obj.setDescription(description);
			return this;
		}
		
		public DataQueryBuilder conditions(String conditions) {
			obj.setConditions(conditions);
			return this;
		}
		
		public DataQueryBuilder attributeId(int attributeId) {
			obj.setAttributeDefinitionId(attributeId);
			return this;
		}
		
		public DataQueryBuilder attribute(AttributeDefinition attribute) {
			obj.setAttributeDefinition(attribute);
			return this;
		}
		
		public DataQueryBuilder entity(EntityDefinition entity) {
			obj.setEntityDefinition(entity);
			return this;
		}

		public DataQueryBuilder entityId(int entityId) {
			obj.setEntityDefinitionId(entityId);
			return this;
		}
		
	}
	
	public static class DataErrorTypeBuilder extends PersistedSurveyObjectBuilder<DataErrorType> {
		
		public DataErrorTypeBuilder(CollectSurvey survey) {
			super(survey, DataErrorType.class);
		}

		@Override
		public DataErrorTypeBuilder id(Integer id) {
			return (DataErrorTypeBuilder) super.id(id);
		}
		
		@Override
		public DataErrorTypeBuilder uuid(String uuid) {
			return (DataErrorTypeBuilder) super.uuid(uuid);
		}
		
		@Override
		public DataErrorTypeBuilder creationDate(Date date) {
			return (DataErrorTypeBuilder) super.creationDate(date);
		}
		
		@Override
		public DataErrorTypeBuilder modifiedDate(Date date) {
			return (DataErrorTypeBuilder) super.modifiedDate(date);
		}
		
		public DataErrorTypeBuilder code(String code) {
			obj.setCode(code);
			return this;
		}
		
		public DataErrorTypeBuilder description(String description) {
			obj.setDescription(description);
			return this;
		}

		public DataErrorTypeBuilder label(String label) {
			obj.setLabel(label);
			return this;
		}
		
	}
	
	public static class DataErrorQueryBuilder extends PersistedSurveyObjectBuilder<DataErrorQuery> {
		
		public DataErrorQueryBuilder(CollectSurvey survey) {
			super(survey, DataErrorQuery.class);
		}

		@Override
		public DataErrorQueryBuilder id(Integer id) {
			return (DataErrorQueryBuilder) super.id(id);
		}
		
		@Override
		public DataErrorQueryBuilder uuid(String uuid) {
			return (DataErrorQueryBuilder) super.uuid(uuid);
		}
		
		@Override
		public DataErrorQueryBuilder creationDate(Date date) {
			return (DataErrorQueryBuilder) super.creationDate(date);
		}
		
		@Override
		public DataErrorQueryBuilder modifiedDate(Date date) {
			return (DataErrorQueryBuilder) super.modifiedDate(date);
		}
		
		public DataErrorQueryBuilder query(DataQuery query) {
			obj.setQuery(query);
			return this;
		}
		
		public DataErrorQueryBuilder type(DataErrorType type) {
			obj.setType(type);
			return this;
		}
		
	}

	public static class DataCleansingStepBuilder extends PersistedSurveyObjectBuilder<DataCleansingStep> {

		public DataCleansingStepBuilder(CollectSurvey survey) {
			super(survey, DataCleansingStep.class);
		}
		
		@Override
		public DataCleansingStepBuilder id(Integer id) {
			return (DataCleansingStepBuilder) super.id(id);
		}
		
		@Override
		public DataCleansingStepBuilder uuid(String uuid) {
			return (DataCleansingStepBuilder) super.uuid(uuid);
		}
		
		@Override
		public DataCleansingStepBuilder creationDate(Date date) {
			return (DataCleansingStepBuilder) super.creationDate(date);
		}
		
		@Override
		public DataCleansingStepBuilder modifiedDate(Date date) {
			return (DataCleansingStepBuilder) super.modifiedDate(date);
		}
		
		public DataCleansingStepBuilder description(String description) {
			obj.setDescription(description);
			return this;
		}
		
		public DataCleansingStepBuilder fieldFixExpressions(String... fieldFixExpressions) {
			obj.setFieldFixExpressions(Arrays.asList(fieldFixExpressions));
			return this;
		}
		
		public DataCleansingStepBuilder fixExpression(String fixExpression) {
			obj.setFixExpression(fixExpression);
			return this;
		}
		
		public DataCleansingStepBuilder queryId(int queryId) {
			obj.setQueryId(queryId);
			return this;
		}
		
		public DataCleansingStepBuilder query(DataQuery query) {
			obj.setQuery(query);
			return this;
		}
		
		public DataCleansingStepBuilder title(String title) {
			obj.setTitle(title);
			return this;
		}
		
	}
		
	public static class DataCleansingChainBuilder extends PersistedSurveyObjectBuilder<DataCleansingChain> {

		public DataCleansingChainBuilder(CollectSurvey survey) {
			super(survey, DataCleansingChain.class);
		}
		
		@Override
		public DataCleansingChainBuilder id(Integer id) {
			return (DataCleansingChainBuilder) super.id(id);
		}
		
		@Override
		public DataCleansingChainBuilder uuid(String uuid) {
			return (DataCleansingChainBuilder) super.uuid(uuid);
		}
		
		@Override
		public DataCleansingChainBuilder creationDate(Date date) {
			return (DataCleansingChainBuilder) super.creationDate(date);
		}
		
		@Override
		public DataCleansingChainBuilder modifiedDate(Date date) {
			return (DataCleansingChainBuilder) super.modifiedDate(date);
		}
		
		public DataCleansingChainBuilder description(String description) {
			obj.setDescription(description);
			return this;
		}

		public DataCleansingChainBuilder step(DataCleansingStep step) {
			obj.addStep(step);
			return this;
		}

		public DataCleansingChainBuilder title(String title) {
			obj.setTitle(title);
			return this;
		}

	}
	
	public static class DataCleansingMetadataBuilder {
		
		private CollectSurvey survey;
		private PersistedSurveyObjectBuilder<?>[] builders;
		
		public DataCleansingMetadataBuilder(CollectSurvey survey, PersistedSurveyObjectBuilder<?>... builders) {
			this.survey = survey;
			this.builders = builders;
		}
		
		public DataCleansingMetadata build() {
			DataCleansingMetadata metadata = new DataCleansingMetadata(survey);
			for (PersistedSurveyObjectBuilder<?> builder : builders) {
				if (builder instanceof DataQueryBuilder) {
					metadata.getDataQueries().add((DataQuery) builder.build());
				} else if (builder instanceof DataErrorTypeBuilder) {
					metadata.getDataErrorTypes().add((DataErrorType) builder.build());
				} else if (builder instanceof DataErrorQueryBuilder) {
					metadata.getDataErrorQueries().add((DataErrorQuery) builder.build());
				} else if (builder instanceof DataCleansingStepBuilder) {
					metadata.getCleansingSteps().add((DataCleansingStep) builder.build());
				} else if (builder instanceof DataCleansingChainBuilder) {
					metadata.getCleansingChains().add((DataCleansingChain) builder.build());
				}
			}
			return metadata;
		}
		
	}
	
	public DataCleansingMetadataBuilder metadata(PersistedSurveyObjectBuilder<?>... builders) {
		return new DataCleansingMetadataBuilder(survey, builders);
	}
	
	public DataQueryBuilder dataQuery() {
		return new DataQueryBuilder(survey);
	}
	
	public DataErrorTypeBuilder dataErrorType() {
		return new DataErrorTypeBuilder(survey);
	}
	
	public DataErrorQueryBuilder dataErrorQuery() {
		return new DataErrorQueryBuilder(survey);
	}
	
	public DataCleansingStepBuilder dataCleansingStep() {
		return new DataCleansingStepBuilder(survey);
	}
	
	public DataCleansingChainBuilder dataCleansingChain() {
		return new DataCleansingChainBuilder(survey);
	}
	
}
