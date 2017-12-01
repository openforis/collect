package org.openforis.collect.manager;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.EntityAddChange;
import org.openforis.collect.model.NodeChange;
import org.openforis.collect.model.NodeChangeSet;
import org.openforis.collect.model.RecordUpdater;
import org.openforis.collect.model.SamplingDesignItem;
import org.openforis.collect.model.User;
import org.openforis.idm.metamodel.AttributeDefinition;
import org.openforis.idm.metamodel.CodeAttributeDefinition;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.EntityDefinition.TraversalType;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.NodeDefinitionVisitor;
import org.openforis.idm.model.Attribute;
import org.openforis.idm.model.Code;
import org.openforis.idm.model.CodeAttribute;
import org.openforis.idm.model.Entity;
import org.openforis.idm.model.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class RecordGenerator {

	@Autowired
	RecordManager recordManager;
	@Autowired
	SurveyManager surveyManager;
	@Autowired
	UserManager userManager;
	@Autowired
	SamplingDesignManager samplingDesignManager;
	
	RecordUpdater recordUpdater = new RecordUpdater();
	
	@Transactional
	public CollectRecord generate(int surveyId, NewRecordParameters parameters, List<String> recordKey) {
		CollectSurvey survey = surveyManager.getById(surveyId);
		User user = userManager.loadById(parameters.getUserId());
		
		CollectRecord record = createRecord(survey, user);
		
		if (CollectionUtils.isNotEmpty(recordKey)) {
			setRecordKeyValues(record, recordKey);
		}
		
		if (parameters.isAddSecondLevelEntities()) {
			addSecondLevelEntities(record, recordKey);
		}
		recordManager.save(record);
		return record;
	}
	
	private CollectRecord createRecord(CollectSurvey survey, User user) {
		EntityDefinition rootEntityDef = survey.getSchema().getFirstRootEntityDefinition();
		String rootEntityName = rootEntityDef.getName();
		CollectRecord record = recordManager.create(survey, rootEntityName, user, null);
		return record;
	}
	
	private void addSecondLevelEntities(CollectRecord record, List<String> recordKey) {
		CollectSurvey survey = (CollectSurvey) record.getSurvey();
		List<SamplingDesignItem> secondLevelSamplingPointItems = samplingDesignManager.loadChildItems(survey.getId(), recordKey);
		List<CodeAttributeDefinition> samplingPointDataCodeAttributeDefs = findSamplingPointCodeAttributes(survey);
		if (! secondLevelSamplingPointItems.isEmpty() && samplingPointDataCodeAttributeDefs.size() > 1) {
			int levelIndex = 1;
			for (SamplingDesignItem samplingDesignItem : secondLevelSamplingPointItems) {
				CodeAttributeDefinition levelKeyDef = samplingPointDataCodeAttributeDefs.get(levelIndex);
				EntityDefinition levelEntityDef = levelKeyDef.getParentEntityDefinition();
				Entity parentLevelEntity = record.getRootEntity();
				NodeChangeSet addEntityChangeSet = recordUpdater.addEntity(parentLevelEntity, levelEntityDef);
				Entity entity = getAddedEntity(addEntityChangeSet);
				CodeAttribute keyAttr = entity.getChild(levelKeyDef);
				recordUpdater.updateAttribute(keyAttr, new Code(samplingDesignItem.getLevelCode(levelIndex + 1)));
			}
		}
	}
	
	private CollectSurvey setRecordKeyValues(CollectRecord record, List<String> recordKey) {
		CollectSurvey survey = (CollectSurvey) record.getSurvey();
		List<AttributeDefinition> keyAttributeDefs = survey.getSchema().getFirstRootEntityDefinition()
				.getKeyAttributeDefinitions();
		for (int i = 0; i < keyAttributeDefs.size(); i++) {
			String keyPart = recordKey.get(i);
			AttributeDefinition keyAttrDef = keyAttributeDefs.get(i);
			Attribute<?,Value> keyAttribute = record.findNodeByPath(keyAttrDef.getPath());
			Value value = keyAttrDef.createValue(keyPart);
			recordUpdater.updateAttribute(keyAttribute, value);
		}
		return survey;
	}
	
	private List<CodeAttributeDefinition> findSamplingPointCodeAttributes(final CollectSurvey survey) {
		EntityDefinition rootEntityDef = survey.getSchema().getFirstRootEntityDefinition();
		final List<CodeAttributeDefinition> samplingPointDataCodeAttributeDefs = new ArrayList<CodeAttributeDefinition>();
		rootEntityDef.traverse(new NodeDefinitionVisitor() {
			public void visit(NodeDefinition def) {
				if (def instanceof CodeAttributeDefinition 
						&& ((CodeAttributeDefinition) def).getList().equals(survey.getSamplingDesignCodeList())) {
					samplingPointDataCodeAttributeDefs.add((CodeAttributeDefinition) def);
				}
			}
		}, TraversalType.BFS);
		return samplingPointDataCodeAttributeDefs;
	}
	
	private Entity getAddedEntity(NodeChangeSet changeSet) {
		List<NodeChange<?>> changes = changeSet.getChanges();
		for (NodeChange<?> nodeChange : changes) {
			if (nodeChange instanceof EntityAddChange) {
				Entity entity = (Entity) nodeChange.getNode();
				return entity;
			}
		}
		throw new IllegalArgumentException("Cannot find added entity in node change set");
	}
	
	protected List<AttributeDefinition> getNonMeasurementKeyDefs(CollectSurvey survey) {
		EntityDefinition rootEntityDef = survey.getSchema().getFirstRootEntityDefinition();
		List<AttributeDefinition> keyAttrDefs = rootEntityDef.getKeyAttributeDefinitions();
		List<AttributeDefinition> nonMeasurementKeyAttrDefs = new ArrayList<AttributeDefinition>();
		for (AttributeDefinition keyAttrDef : keyAttrDefs) {
			if (! survey.getAnnotations().isMeasurementAttribute(keyAttrDef)) {
				nonMeasurementKeyAttrDefs.add(keyAttrDef);
			}
		}
		return nonMeasurementKeyAttrDefs;
	}
	
	public static class NewRecordParameters {
		
		private int userId;
		private String rootEntityName;
		private String versionName;
		private boolean addSecondLevelEntities = false;
		private boolean onlyUnanalyzedSamplingPoints = false;
		private List<String> recordKey;

		public int getUserId() {
			return userId;
		}

		public void setUserId(int userId) {
			this.userId = userId;
		}
		
		public String getRootEntityName() {
			return rootEntityName;
		}
		
		public void setRootEntityName(String rootEntityName) {
			this.rootEntityName = rootEntityName;
		}
		
		public String getVersionName() {
			return versionName;
		}
		
		public void setVersionName(String versionName) {
			this.versionName = versionName;
		}
		
		public boolean isAddSecondLevelEntities() {
			return addSecondLevelEntities;
		}

		public void setAddSecondLevelEntities(boolean addSecondLevelEntities) {
			this.addSecondLevelEntities = addSecondLevelEntities;
		}
		
		public boolean isOnlyUnanalyzedSamplingPoints() {
			return onlyUnanalyzedSamplingPoints;
		}
		
		public void setOnlyUnanalyzedSamplingPoints(boolean onlyUnanalyzedSamplingPoints) {
			this.onlyUnanalyzedSamplingPoints = onlyUnanalyzedSamplingPoints;
		}

		public List<String> getRecordKey() {
			return recordKey;
		}
		
		public void setRecordKey(List<String> recordKey) {
			this.recordKey = recordKey;
		}
	}
}
