package org.openforis.collect.datacleansing;

import java.util.Arrays;
import java.util.Date;
import java.util.UUID;

import org.junit.Before;
import org.openforis.collect.CollectIntegrationTest;
import org.openforis.collect.datacleansing.DataCleansingStepValue.UpdateType;
import org.openforis.collect.datacleansing.DataQuery.ErrorSeverity;
import org.openforis.collect.manager.UserManager;
import org.openforis.collect.manager.exception.SurveyValidationException;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.User;
import org.openforis.collect.persistence.SurveyImportException;
import org.openforis.idm.metamodel.AttributeDefinition;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.PersistedSurveyObject;
import org.openforis.idm.metamodel.xml.IdmlParseException;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * @author S. Ricci
 *
 */
public abstract class DataCleansingIntegrationTest extends CollectIntegrationTest {
	
	@Autowired
	private UserManager userManager;

	protected CollectSurvey survey;
	protected User adminUser;
	
	@Before
	public void init() throws SurveyImportException, IdmlParseException, SurveyValidationException {
		survey = importModel();
		adminUser = userManager.loadAdminUser();
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
		
		public DataQueryBuilder type(DataQueryType type) {
			obj.setType(type);
			return this;
		}
		
		public DataQueryBuilder severity(ErrorSeverity severity) {
			obj.setErrorSeverity(severity);
			return this;
		}
	}
	
	public static class DataQueryTypeBuilder extends PersistedSurveyObjectBuilder<DataQueryType> {
		
		public DataQueryTypeBuilder(CollectSurvey survey) {
			super(survey, DataQueryType.class);
		}

		@Override
		public DataQueryTypeBuilder id(Integer id) {
			return (DataQueryTypeBuilder) super.id(id);
		}
		
		@Override
		public DataQueryTypeBuilder uuid(String uuid) {
			return (DataQueryTypeBuilder) super.uuid(uuid);
		}
		
		@Override
		public DataQueryTypeBuilder creationDate(Date date) {
			return (DataQueryTypeBuilder) super.creationDate(date);
		}
		
		@Override
		public DataQueryTypeBuilder modifiedDate(Date date) {
			return (DataQueryTypeBuilder) super.modifiedDate(date);
		}
		
		public DataQueryTypeBuilder code(String code) {
			obj.setCode(code);
			return this;
		}
		
		public DataQueryTypeBuilder description(String description) {
			obj.setDescription(description);
			return this;
		}

		public DataQueryTypeBuilder label(String label) {
			obj.setLabel(label);
			return this;
		}
		
	}
	
	public static class DataQueryGroupBuilder extends PersistedSurveyObjectBuilder<DataQueryGroup> {
		
		public DataQueryGroupBuilder(CollectSurvey survey) {
			super(survey, DataQueryGroup.class);
		}

		@Override
		public DataQueryGroupBuilder id(Integer id) {
			return (DataQueryGroupBuilder) super.id(id);
		}
		
		@Override
		public DataQueryGroupBuilder uuid(String uuid) {
			return (DataQueryGroupBuilder) super.uuid(uuid);
		}
		
		@Override
		public DataQueryGroupBuilder creationDate(Date date) {
			return (DataQueryGroupBuilder) super.creationDate(date);
		}
		
		@Override
		public DataQueryGroupBuilder modifiedDate(Date date) {
			return (DataQueryGroupBuilder) super.modifiedDate(date);
		}
		
		public DataQueryGroupBuilder title(String title) {
			obj.setTitle(title);
			return this;
		}
		
		public DataQueryGroupBuilder description(String description) {
			obj.setDescription(description);
			return this;
		}
		
		public DataQueryGroupBuilder query(DataQuery query) {
			obj.addQuery(query);
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
		
		public DataCleansingStepBuilder attributeFixExpression(String fixExpression) {
			return attributeFixExpression(null, fixExpression);
		}
		
		public DataCleansingStepBuilder attributeFixExpression(String condition, String fixExpression) {
			DataCleansingStepValue value = new DataCleansingStepValue();
			value.setUpdateType(UpdateType.ATTRIBUTE);
			value.setFixExpression(fixExpression);
			obj.getUpdateValues().add(value);
			return this;
		}

		public DataCleansingStepBuilder fieldFixExpressions(String... fixExpressions) {
			return fieldFixExpressions(null, fixExpressions);
		}
		
		public DataCleansingStepBuilder fieldFixExpressions(String condition, String... fixExpressions) {
			DataCleansingStepValue value = new DataCleansingStepValue();
			value.setUpdateType(UpdateType.FIELD);
			value.setFieldFixExpressions(Arrays.asList(fixExpressions));
			obj.getUpdateValues().add(value);
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
				if (builder instanceof DataQueryTypeBuilder) {
					metadata.getDataQueryTypes().add((DataQueryType) builder.build());
				} else if (builder instanceof DataQueryBuilder) {
					metadata.getDataQueries().add((DataQuery) builder.build());
				} else if (builder instanceof DataQueryGroupBuilder) {
					metadata.getDataQueryGroups().add((DataQueryGroup) builder.build());
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
	
	public DataQueryTypeBuilder dataQueryType() {
		return new DataQueryTypeBuilder(survey);
	}
	
	public DataQueryGroupBuilder dataQueryGroup() {
		return new DataQueryGroupBuilder(survey);
	}
	
	public DataCleansingStepBuilder dataCleansingStep() {
		return new DataCleansingStepBuilder(survey);
	}
	
	public DataCleansingChainBuilder dataCleansingChain() {
		return new DataCleansingChainBuilder(survey);
	}
	
}
