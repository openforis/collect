package org.openforis.collect.manager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.CollectRecord.Step;
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
import org.openforis.idm.metamodel.ModelVersion;
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
	public CollectRecord generate(CollectSurvey survey, NewRecordParameters parameters) {
		List<AttributeDefinition> keyDefs = getKeyAttributeDefs(survey);
		RecordKey recordKey = new RecordKey(keyDefs, parameters.getRecordKey());
		return generate(survey, parameters, recordKey);
	}
	
	@Transactional
	public CollectRecord generate(CollectSurvey survey, NewRecordParameters parameters, RecordKey recordKey) {
		User user = loadUser(parameters.getUserId(), parameters.getUsername());
		
		EntityDefinition rootEntityDef = StringUtils.isBlank(parameters.getRootEntityName()) ?
				survey.getSchema().getFirstRootEntityDefinition() 
				: survey.getSchema().getRootEntityDefinition(parameters.getRootEntityName());
				
		CollectRecord record = createRecord(survey, rootEntityDef, parameters.getVersionId(), 
				parameters.getStep(), user, recordKey);
		record.setPreview(parameters.isPreview());

		if (parameters.isAddSecondLevelEntities()) {
			addSecondLevelEntities(record, recordKey);
		}
		if (!record.isPreview()) {
			recordManager.save(record);
		}
		return record;
	}
	
	private CollectRecord createRecord(CollectSurvey survey, EntityDefinition rootEntityDef, 
			Integer versionId, Step step, User user, RecordKey recordKey) {
		String rootEntityName = rootEntityDef.getName();
		ModelVersion version = versionId == null ? null : survey.getVersionById(versionId);
		String versionName = version == null ? null : version.getName();
		CollectRecord record = recordManager.create(survey, rootEntityName, user, versionName, null, step);
		if (recordKey.isNotEmpty()) {
			setRecordKeyValues(record, recordKey);
		}
		return record;
	}
	
	private void addSecondLevelEntities(CollectRecord record, RecordKey recordKey) {
		CollectSurvey survey = (CollectSurvey) record.getSurvey();
		List<AttributeDefinition> nonMeasurementKeyDefs = getNonMeasurementKeyDefs(survey);
		List<String> keyValues = recordKey.getValues(nonMeasurementKeyDefs);
		List<SamplingDesignItem> secondLevelSamplingPointItems = samplingDesignManager.loadChildItems(survey.getId(), keyValues);
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
	
	private void setRecordKeyValues(CollectRecord record, RecordKey recordKey) {
		CollectSurvey survey = (CollectSurvey) record.getSurvey();
		List<AttributeDefinition> keyAttributeDefs = survey.getSchema().getFirstRootEntityDefinition()
				.getKeyAttributeDefinitions();
		for (AttributeDefinition keyAttrDef : keyAttributeDefs) {
			String keyPart = recordKey.getValue(keyAttrDef.getPath());
			Attribute<?,Value> keyAttribute = record.findNodeByPath(keyAttrDef.getPath());
			Value value = keyAttrDef.createValue(keyPart);
			recordUpdater.updateAttribute(keyAttribute, value);
		}
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
	
	protected List<AttributeDefinition> getKeyAttributeDefs(CollectSurvey survey) {
		EntityDefinition rootEntityDef = survey.getSchema().getFirstRootEntityDefinition();
		return rootEntityDef.getKeyAttributeDefinitions();
	}
	
	protected List<AttributeDefinition> getMeasurementKeyDefs(CollectSurvey survey) {
		List<AttributeDefinition> keyAttrDefs = getKeyAttributeDefs(survey);
		List<AttributeDefinition> measurementKeyAttrDefs = new ArrayList<AttributeDefinition>();
		for (AttributeDefinition keyAttrDef : keyAttrDefs) {
			if (survey.getAnnotations().isMeasurementAttribute(keyAttrDef)) {
				measurementKeyAttrDefs.add(keyAttrDef);
			}
		}
		return measurementKeyAttrDefs;
	}

	protected List<AttributeDefinition> getNonMeasurementKeyDefs(CollectSurvey survey) {
		List<AttributeDefinition> keyAttrDefs = getKeyAttributeDefs(survey);
		List<AttributeDefinition> measurementKeyDefs = getMeasurementKeyDefs(survey);
		List<AttributeDefinition> result = new ArrayList<AttributeDefinition>(keyAttrDefs);
		Iterator<AttributeDefinition> it = result.iterator();
		while(it.hasNext()) {
			AttributeDefinition keyDef = it.next();
			if (measurementKeyDefs.contains(keyDef)) {
				it.remove();
			}
		}
		return result;
	}
	
	protected User loadUser(Integer userId, String username) {
		if (userId != null) {
			return userManager.loadById(userId);
		} else if (username != null) {
			return userManager.loadByUserName(username);
		} else {
			return null;
		}
	}
	
	public static class NewRecordParameters {
		
		private String username;
		private Integer userId;
		private String rootEntityName;
		private Integer versionId;
		private Step step = Step.ENTRY;
		private boolean preview;
		private boolean addSecondLevelEntities = false;
		private boolean onlyUnanalyzedSamplingPoints = false;
		private List<String> recordKey = new ArrayList<String>();

		public String getUsername() {
			return username;
		}
		
		public void setUsername(String username) {
			this.username = username;
		}
		
		public Integer getUserId() {
			return userId;
		}

		public void setUserId(Integer userId) {
			this.userId = userId;
		}
		
		public String getRootEntityName() {
			return rootEntityName;
		}
		
		public void setRootEntityName(String rootEntityName) {
			this.rootEntityName = rootEntityName;
		}

		public Integer getVersionId() {
			return versionId;
		}
		
		public void setVersionId(Integer versionId) {
			this.versionId = versionId;
		}
		
		public Step getStep() {
			return step;
		}
		
		public void setStep(Step step) {
			this.step = step;
		}
		
		public boolean isPreview() {
			return preview;
		}
		
		public void setPreview(boolean preview) {
			this.preview = preview;
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
	
	public static class RecordKey {
		
		private Map<String,String> valueByPath = new HashMap<String,String>();
		
		public RecordKey() {
		}
		
		public List<String> getValues(List<AttributeDefinition> keyDefs) {
			List<String> values = keyDefs.stream()
					.map(keyDef -> valueByPath.get(keyDef.getPath()))
					.collect(Collectors.toList());
			return values;
		}

		public RecordKey(List<AttributeDefinition> keyDefs, List<String> keys) {
			for (int i = 0; i < keyDefs.size() && i < keys.size(); i++) {
				AttributeDefinition def = keyDefs.get(i);
				putValue(def.getPath(), keys.get(i));
			};
		}

		public String getValue(String path) {
			return valueByPath.get(path);
		}

		public void putValue(String path, String value) {
			valueByPath.put(path, value);
		}
		public boolean isNotEmpty() {
			return ! valueByPath.isEmpty();
		}

		public Map<String, String> getValueByPath() {
			return valueByPath;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((valueByPath == null) ? 0 : valueByPath.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			RecordKey other = (RecordKey) obj;
			if (valueByPath == null) {
				if (other.valueByPath != null)
					return false;
			} else if (!valueByPath.equals(other.valueByPath))
				return false;
			return true;
		}
	}
}
