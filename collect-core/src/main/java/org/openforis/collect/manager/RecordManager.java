/**
 * 
 */
package org.openforis.collect.manager;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.namespace.QName;

import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.CollectRecord.Step;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.User;
import org.openforis.collect.persistence.AccessDeniedException;
import org.openforis.collect.persistence.DuplicateIdException;
import org.openforis.collect.persistence.InvalidIdException;
import org.openforis.collect.persistence.MultipleEditException;
import org.openforis.collect.persistence.NonexistentIdException;
import org.openforis.collect.persistence.RecordDAO;
import org.openforis.collect.persistence.RecordLockedException;
import org.openforis.idm.metamodel.AttributeDefinition;
import org.openforis.idm.metamodel.CodeAttributeDefinition;
import org.openforis.idm.metamodel.CodeList;
import org.openforis.idm.metamodel.CodeListItem;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.ModelVersion;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.model.Code;
import org.openforis.idm.model.CodeAttribute;
import org.openforis.idm.model.Entity;
import org.openforis.idm.model.Node;
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
	private RecordDAO recordDAO;
	
//	@Autowired 
//	private SurveyContext recordContext;
	
	protected void init() {
		unlockAll();
	}
	
	@Transactional
	public void save(CollectRecord record) {
		updateCounts(record);
		
		updateKeys(record);
		
		recordDAO.saveOrUpdate(record);
	}

	@Transactional
	public void delete(int recordId, User user) throws RecordLockedException, AccessDeniedException, MultipleEditException {
		recordDAO.lock(recordId, user);
		recordDAO.delete(recordId);
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
	public CollectRecord checkout(CollectSurvey survey, User user, int recordId) throws RecordLockedException, NonexistentIdException, AccessDeniedException, MultipleEditException {
		CollectRecord record = recordDAO.load(survey, recordId);
		recordDAO.lock(recordId, user);
		return record;
	}

	@Transactional
	public List<CollectRecord> getSummaries(CollectSurvey survey, String rootEntity, String... keys) {
		return recordDAO.loadSummaries(survey, rootEntity, keys);
	}
	
	@Transactional
	public List<CollectRecord> getSummaries(CollectSurvey survey, String rootEntity, int offset, int maxNumberOfRecords, String orderByFieldName, String filter) {
		List<CollectRecord> recordsSummary = recordDAO.loadSummaries(survey, rootEntity, offset, maxNumberOfRecords, orderByFieldName, filter);
		return recordsSummary;
	}

	@Transactional
	public int getCountRecords(EntityDefinition rootEntityDefinition) {
		int count = recordDAO.getRecordCount(rootEntityDefinition);
		return count;
	}

	@Transactional
	public CollectRecord create(CollectSurvey survey, EntityDefinition rootEntityDefinition, User user, String modelVersionName) throws MultipleEditException, AccessDeniedException, RecordLockedException {
		recordDAO.checkLock(user);
		
		CollectRecord record = new CollectRecord(survey, modelVersionName);
		record.createRootEntity(rootEntityDefinition.getName());
		
		record.setCreationDate(new Date());
		record.setCreatedBy(user);
		record.setStep(Step.ENTRY);
		recordDAO.saveOrUpdate(record);
		Integer recordId = record.getId();
		recordDAO.lock(recordId, user);
		return record;
	}

	@Transactional
	public void lock(Record record) {

	}

	@Transactional
	public void unlock(Record record, User user) throws RecordLockedException {
		recordDAO.unlock(record.getId(), user);
	}

	@Transactional
	public void unlockAll() {
		recordDAO.unlockAll();
	}

	@Transactional
	public void updateRootEntityKey(String recordId, String newRootEntityKey) throws DuplicateIdException, InvalidIdException, NonexistentIdException, AccessDeniedException, RecordLockedException {

	}

	@Transactional
	public int promote(CollectSurvey survey, int recordId, User user) throws InvalidIdException, MultipleEditException, NonexistentIdException, AccessDeniedException, RecordLockedException {
		CollectRecord record = recordDAO.load(survey, recordId);
		Step nextStep;
		switch(record.getStep()) {
			case ENTRY:
				nextStep = Step.CLEANSING;
				//clone record and save a copy with the new step
				recordDAO.unlock(recordId, user);
				record.setId(null);
				Date now = new Date();
				record.setSubmittedId(recordId);
				record.setModifiedBy(user);
				record.setModifiedDate(now);
				record.setCreatedBy(user);
				record.setCreationDate(now);
				record.setStep(nextStep);
				recordDAO.insert(record);
				break;
			case CLEANSING:
				nextStep = Step.ANALYSIS;
				recordDAO.unlock(recordId, user);
				record.setStep(nextStep);
				recordDAO.update(record);
				break;
			default:
				throw new IllegalArgumentException("This record cannot be promoted.");
		}
		return record.getId();
	}

	@Transactional
	public void demote(String recordId) throws InvalidIdException, MultipleEditException, NonexistentIdException, AccessDeniedException, RecordLockedException {
	}

	
	public Node<?> deleteNode(Entity parentEntity, Node<?> node) {
		NodeDefinition defn = node.getDefinition();
		String name = defn.getName();
		List<Node<?>> children = parentEntity.getAll(name);
		int index = children.indexOf(node);
		Node<?> deleted = parentEntity.remove(name, index);
		return deleted;
	}
	
	public Entity addEntity(Entity parentEntity, String nodeName, ModelVersion version) {
		Entity entity = parentEntity.addEntity(nodeName);
		addEmptyNodes(entity, version);
		return entity;
	}
	
	public void addEmptyNodes(Entity entity, ModelVersion version) {
		addEmptyEnumeratedEntities(entity, version);
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
						addEntity(entity, nodeDefn.getName(), version);
					}
				} else {
					List<Node<?>> all = entity.getAll(name);
					for (Node<?> node : all) {
						if(node instanceof Entity) {
							addEmptyNodes((Entity) node, version);
							addEmptyEnumeratedEntities((Entity) node, version);
						}
					}
				}
			}
		}
	}
	
	private void addEmptyEnumeratedEntities(Entity entity, ModelVersion version) {
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
								Entity addedEntity = addEntity(entity, childEntityDefn.getName(), version);
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
