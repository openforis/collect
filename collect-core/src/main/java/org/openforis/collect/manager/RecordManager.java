/**
 * 
 */
package org.openforis.collect.manager;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.xml.namespace.QName;

import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.CollectRecord.State;
import org.openforis.collect.model.CollectRecord.Step;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.FieldSymbol;
import org.openforis.collect.model.User;
import org.openforis.collect.persistence.MultipleEditException;
import org.openforis.collect.persistence.RecordDao;
import org.openforis.collect.persistence.RecordLockedException;
import org.openforis.collect.persistence.RecordPersistenceException;
import org.openforis.idm.metamodel.AttributeDefinition;
import org.openforis.idm.metamodel.CodeAttributeDefinition;
import org.openforis.idm.metamodel.CodeList;
import org.openforis.idm.metamodel.CodeListItem;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.ModelVersion;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.model.Attribute;
import org.openforis.idm.model.Code;
import org.openforis.idm.model.CodeAttribute;
import org.openforis.idm.model.Entity;
import org.openforis.idm.model.Field;
import org.openforis.idm.model.Node;
import org.openforis.idm.model.NodePointer;
import org.openforis.idm.model.NumberAttribute;
import org.openforis.idm.model.Record;
import org.openforis.idm.model.TextAttribute;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author M. Togna
 * @author S. Ricci
 */
public class RecordManager {
	//private final Log log = LogFactory.getLog(RecordManager.class);
	
	private static final QName COUNT_ANNOTATION = new QName("http://www.openforis.org/collect/3.0/collect", "count");

	@Autowired
	private RecordDao recordDao;
	
	protected void init() {
		unlockAll();
	}
	
	@Transactional
	public void save(CollectRecord record) throws RecordPersistenceException {
		updateCounts(record);
		updateKeys(record);
		Integer id = record.getId();
		if(id == null) {
			recordDao.insert(record);
			User user = record.getModifiedBy();
			id = record.getId();
			recordDao.lock(id, user);
		} else {
			recordDao.update(record);
		}
	}

	@Transactional
	public void delete(int recordId, User user) throws RecordPersistenceException {
		recordDao.lock(recordId, user);
		recordDao.delete(recordId);
	}

	/**
	 * Returns a record and lock it
	 * 
	 * @param survey
	 * @param user
	 * @param recordId
	 * @return
	 * @throws MultipleEditException 
	 */
	@Transactional
	public CollectRecord checkout(CollectSurvey survey, User user, int recordId, int step) throws RecordPersistenceException {
		CollectRecord record = recordDao.load(survey, recordId, step);
		recordDao.lock(recordId, user);
		record.setLockedBy(user);
		return record;
	}

	@Transactional
	public List<CollectRecord> getSummaries(CollectSurvey survey, String rootEntity, String... keys) {
		return recordDao.loadSummaries(survey, rootEntity, keys);
	}
	
	@Transactional
	public List<CollectRecord> getSummaries(CollectSurvey survey, String rootEntity, int offset, int maxNumberOfRecords, String orderByFieldName, String filter) {
		List<CollectRecord> recordsSummary = recordDao.loadSummaries(survey, rootEntity, offset, maxNumberOfRecords, orderByFieldName, filter);
		return recordsSummary;
	}

	@Transactional
	public int getCountRecords(EntityDefinition rootEntityDefinition) {
		int count = recordDao.countRecords(rootEntityDefinition.getId());
		return count;
	}

	@Transactional
	public CollectRecord create(CollectSurvey survey, EntityDefinition rootEntityDefinition, User user, String modelVersionName) throws RecordPersistenceException {
		recordDao.checkLock(user);
		
		CollectRecord record = new CollectRecord(survey, modelVersionName);
		record.createRootEntity(rootEntityDefinition.getName());
		
		record.setCreationDate(new Date());
		record.setCreatedBy(user);
		record.setStep(Step.ENTRY);
		return record;
	}

	@Transactional
	public void unlock(CollectRecord record, User user) throws RecordLockedException {
		recordDao.unlock(record.getId(), user);
		record.setLockedBy(null);
	}

	@Transactional
	public void unlockAll() {
		recordDao.unlockAll();
	}

	@Transactional
	public void promote(CollectSurvey survey, int recordId, int step, User user) throws RecordPersistenceException {
		CollectRecord record = recordDao.load(survey, recordId, step);
		Step currentStep = record.getStep();
		Step nextStep = currentStep.getNext();
		Date now = new Date();
		record.setModifiedBy(user);
		record.setModifiedDate(now);
		//save changes on current step
		recordDao.update(record); 
		//change step and update newly the record
		record.setStep(nextStep);
		record.setState(null);
		recordDao.update(record);
		recordDao.unlock(recordId, user);
	}

	@Transactional
	public void demote(CollectSurvey survey, int recordId, int stepNumber, User user) throws RecordPersistenceException {
		Step step = Step.valueOf(stepNumber);
		Step prevStep = step.getPrevious();
		CollectRecord record = recordDao.load(survey, recordId, prevStep.getStepNumber());
		Date now = new Date();
		record.setModifiedBy(user);
		record.setModifiedDate(now);
		record.setStep(prevStep);
		record.setState(State.REJECTED);
		recordDao.update(record);
		recordDao.unlock(recordId, user);
	}

	
	public Node<?> deleteNode(Node<?> node) {
		Entity parentEntity = node.getParent();
		int index = node.getIndex();
		Node<?> deletedNode = parentEntity.remove(node.getName(), index);
		return deletedNode;
	}
	
	public Entity addEntity(Entity parentEntity, String nodeName) {
		Entity entity = parentEntity.addEntity(nodeName);
		addEmptyNodes(entity);
		return entity;
	}
	
	public void addEmptyNodes(Entity entity) {
		Record record = entity.getRecord();
		ModelVersion version = record.getVersion();
		
		addEmptyEnumeratedEntities(entity);
		EntityDefinition entityDefn = entity.getDefinition();
		List<NodeDefinition> childDefinitions = entityDefn.getChildDefinitions();
		for (NodeDefinition nodeDefn : childDefinitions) {
			if(version.isApplicable(nodeDefn)) {
				String name = nodeDefn.getName();
				if(entity.getCount(name) == 0) {
					if(nodeDefn instanceof AttributeDefinition) {
						Node<?> createNode = nodeDefn.createNode();
						entity.add(createNode);
					} else if(nodeDefn instanceof EntityDefinition && ! nodeDefn.isMultiple()) {
						addEntity(entity, nodeDefn.getName());
					}
				} else {
					List<Node<?>> all = entity.getAll(name);
					for (Node<?> node : all) {
						if(node instanceof Entity) {
							addEmptyNodes((Entity) node);
						}
					}
				}
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	public <V> void setFieldValue(Attribute<?,?> attribute, Object value, String remarks, FieldSymbol symbol, int fieldIdx){
		if(fieldIdx < 0){
			fieldIdx = 0;
		}
		Field<V> field = (Field<V>) attribute.getField(fieldIdx);
		field.setValue((V)value);
		field.setRemarks(remarks);
		Character symbolChar = null;
		if (symbol != null) {
			symbolChar = symbol.getCode();
		}
		field.setSymbol(symbolChar);
	}
	
	@SuppressWarnings("unchecked")
	public <V> void setAttributeValue(Attribute<?,V> attribute, Object value, String remarks){
		attribute.setValue((V)value);
		Field<V> field = (Field<V>) attribute.getField(0);
		field.setRemarks(remarks);
		field.setSymbol(null);
	}
	
	public Set<Attribute<?, ?>> clearValidationResults(Attribute<?,?> attribute){
		Set<Attribute<?,?>> checkDependencies = attribute.getCheckDependencies();
		clearValidationResults(checkDependencies);
		return checkDependencies;
	}

	public void clearValidationResults(Set<Attribute<?, ?>> checkDependencies) {
		for (Attribute<?, ?> attr : checkDependencies) {
			attr.clearValidationResults();
		}
	}
	
	public Set<NodePointer> clearRelevanceRequiredStates(Node<?> node){
		Set<NodePointer> relevantDependencies = node.getRelevantDependencies();
		clearRelevantDependencies(relevantDependencies);
		Set<NodePointer> requiredDependencies = node.getRequiredDependencies();
		requiredDependencies.addAll(relevantDependencies);
		clearRequiredDependencies(requiredDependencies);
		return requiredDependencies;
	}
	
	public void clearRelevantDependencies(Set<NodePointer> nodePointers) {
		for (NodePointer nodePointer : nodePointers) {
			Entity entity = nodePointer.getEntity();
			entity.clearRelevanceState(nodePointer.getChildName());
		}
	}
	
	public void clearRequiredDependencies(Set<NodePointer> nodePointers) {
		for (NodePointer nodePointer : nodePointers) {
			Entity entity = nodePointer.getEntity();
			entity.clearRequiredState(nodePointer.getChildName());
		}
	}
	
	private void addEmptyEnumeratedEntities(Entity entity) {
		Record record = entity.getRecord();
		ModelVersion version = record.getVersion();
		EntityDefinition entityDefn = entity.getDefinition();
		List<NodeDefinition> childDefinitions = entityDefn.getChildDefinitions();
		for (NodeDefinition childDefn : childDefinitions) {
			if(childDefn instanceof EntityDefinition && version.isApplicable(childDefn)) {
				EntityDefinition childEntityDefn = (EntityDefinition) childDefn;
				CodeAttributeDefinition codeDefn = getCodeKeyAttribute(childEntityDefn, version);
				if(codeDefn != null) {
					CodeList list = codeDefn.getList();
					List<CodeListItem> items = list.getItems();
					for (CodeListItem item : items) {
						if(version.isApplicable(item)) {
							String code = item.getCode();
							if(! hasEnumeratedEntity(entity, childEntityDefn, codeDefn, code)) {
								Entity addedEntity = addEntity(entity, childEntityDefn.getName());
								//there will be an empty CodeAttribute after the adding of the new entity
								//set the value into this node
								CodeAttribute addedCode = (CodeAttribute) addedEntity.get(codeDefn.getName(), 0);
								addedCode.setValue(new Code(code));
							}
						}
					}
				}
			}
		}
	}

	private CodeAttributeDefinition getCodeKeyAttribute(EntityDefinition entity, ModelVersion version) {
		List<NodeDefinition> childDefinitions = entity.getChildDefinitions();
		for (NodeDefinition nodeDefn : childDefinitions) {
			if(nodeDefn instanceof CodeAttributeDefinition && version.isApplicable(nodeDefn)) {
				CodeAttributeDefinition codeDefn = (CodeAttributeDefinition) nodeDefn;
				if(codeDefn.isKey()) {
					return codeDefn;
				}
			}
		}
		return null;
	}
	
	private boolean hasEnumeratedEntity(Entity parentEntity, EntityDefinition childEntityDefn, CodeAttributeDefinition codeAttributeDef, String value) {
		List<Node<?>> children = parentEntity.getAll(childEntityDefn.getName());
		for (Node<?> node : children) {
			Entity child = (Entity) node;
			Code code = getCodeAttributeValue(child, codeAttributeDef);
			if(code != null && value.equals(code.getCode())) {
				return true;
			}
		}
		return false;
	}
	
	private Code getCodeAttributeValue(Entity entity, CodeAttributeDefinition def) {
		Node<?> node = entity.get(def.getName(), 0);
		if(node != null) {
			return ((CodeAttribute)node).getValue();
		} else {
			return null;
		}
	}
	
	/**
	 * Returns first level entity definitions of the passed root entity that have the attribute countInSummaryList set to true
	 * 
	 * @param rootEntityDefinition
	 * @return 
	 */
	private List<EntityDefinition> getCountableInList(EntityDefinition rootEntityDefinition) {
		List<EntityDefinition> result = new ArrayList<EntityDefinition>();
		List<NodeDefinition> childDefinitions = rootEntityDefinition.getChildDefinitions();
		for (NodeDefinition childDefinition : childDefinitions) {
			if(childDefinition instanceof EntityDefinition) {
				EntityDefinition entityDefinition = (EntityDefinition) childDefinition;
				String annotation = childDefinition.getAnnotation(COUNT_ANNOTATION);
				if(annotation != null && Boolean.parseBoolean(annotation)) {
					result.add(entityDefinition);
				}
			}
		}
		return result;
	}
	
	private void updateCounts(CollectRecord record) {
		Entity rootEntity = record.getRootEntity();
		EntityDefinition rootEntityDefn = rootEntity.getDefinition();
		List<EntityDefinition> countableDefns = getCountableInList(rootEntityDefn);
		
		//set counts
		List<Integer> counts = new ArrayList<Integer>();
		for (EntityDefinition defn : countableDefns) {
			String name = defn.getName();
			int count = rootEntity.getCount(name);
			counts.add(count);
		}
		record.setEntityCounts(counts);
	}
	
	private void updateKeys(CollectRecord record) {
		Entity rootEntity = record.getRootEntity();
		EntityDefinition rootEntityDefn = rootEntity.getDefinition();
		List<AttributeDefinition> keyDefns = rootEntityDefn.getKeyAttributeDefinitions();
		//set keys
		List<String> keys = new ArrayList<String>();
		for (NodeDefinition def: keyDefns) {
			String name = def.getName();
			Object value = null;
			String textValue = null;
			Node<? extends NodeDefinition> node = rootEntity.get(name, 0);
			if(node instanceof CodeAttribute) {
				Code code = ((CodeAttribute) node).getValue();
				if(code != null) {
					textValue = code.getCode();
				}
			} else if(node instanceof TextAttribute) {
				textValue = ((TextAttribute) node).getValue();
			} else if(node instanceof NumberAttribute<?>) {
				value = ((NumberAttribute<?>) node).getValue();
				if(value != null) {
					textValue = value.toString();
				}
			}
			keys.add(textValue);
		}
		record.setKeys(keys);
	}

}
