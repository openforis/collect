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
import org.openforis.collect.model.User;
import org.openforis.collect.persistence.AccessDeniedException;
import org.openforis.collect.persistence.DuplicateIdException;
import org.openforis.collect.persistence.InvalidIdException;
import org.openforis.collect.persistence.MultipleEditException;
import org.openforis.collect.persistence.NonexistentIdException;
import org.openforis.collect.persistence.RecordDAO;
import org.openforis.collect.persistence.RecordLockedException;
import org.openforis.idm.metamodel.AttributeDefinition;
import org.openforis.idm.metamodel.BooleanAttributeDefinition;
import org.openforis.idm.metamodel.CodeAttributeDefinition;
import org.openforis.idm.metamodel.CodeList;
import org.openforis.idm.metamodel.CodeListItem;
import org.openforis.idm.metamodel.CoordinateAttributeDefinition;
import org.openforis.idm.metamodel.DateAttributeDefinition;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.ModelVersion;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.NumericAttributeDefinition;
import org.openforis.idm.metamodel.NumericAttributeDefinition.Type;
import org.openforis.idm.metamodel.Survey;
import org.openforis.idm.metamodel.TaxonAttributeDefinition;
import org.openforis.idm.metamodel.TextAttributeDefinition;
import org.openforis.idm.metamodel.TimeAttributeDefinition;
import org.openforis.idm.model.Attribute;
import org.openforis.idm.model.Code;
import org.openforis.idm.model.CodeAttribute;
import org.openforis.idm.model.Coordinate;
import org.openforis.idm.model.Entity;
import org.openforis.idm.model.Node;
import org.openforis.idm.model.NumberAttribute;
import org.openforis.idm.model.Record;
import org.openforis.idm.model.RecordContext;
import org.openforis.idm.model.TaxonOccurrence;
import org.openforis.idm.model.TextAttribute;
import org.openforis.idm.model.Time;
import org.openforis.idm.model.expression.ExpressionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author M. Togna
 * @author S. Ricci
 */
public class RecordManager implements RecordContext {
	//private final Log log = LogFactory.getLog(RecordManager.class);
	
	private static final QName COUNT_ANNOTATION = new QName("http://www.openforis.org/collect/3.0/collect", "count");

	@Autowired
	private RecordDAO recordDAO;

	@Autowired
	private ExpressionFactory expressionFactory;
	
	protected void init() {
		unlockAll();
	}
	
	@Override
	public ExpressionFactory getExpressionFactory() {
		return expressionFactory;
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
	public CollectRecord checkout(Survey survey, User user, int recordId) throws RecordLockedException, NonexistentIdException, AccessDeniedException, MultipleEditException {
		CollectRecord record = recordDAO.load(survey, this, recordId);
		recordDAO.lock(recordId, user);
		return record;
	}

	@Transactional
	public List<CollectRecord> getSummaries(Survey survey, String rootEntity, int offset, int maxNumberOfRecords, String orderByFieldName, String filter) {
		List<CollectRecord> recordsSummary = recordDAO.loadSummaries(survey, this, rootEntity, offset, maxNumberOfRecords, orderByFieldName, filter);
		return recordsSummary;
	}

	@Transactional
	public int getCountRecords(EntityDefinition rootEntityDefinition) {
		int count = recordDAO.getRecordCount(rootEntityDefinition);
		return count;
	}

	@Transactional
	public CollectRecord create(Survey survey, EntityDefinition rootEntityDefinition, User user, String modelVersionName) throws MultipleEditException, AccessDeniedException, RecordLockedException {
		recordDAO.checkLock(user);
		
		CollectRecord record = new CollectRecord(this, survey, modelVersionName);
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
	public void promote(String recordId) throws InvalidIdException, MultipleEditException, NonexistentIdException, AccessDeniedException, RecordLockedException {
	}

	@Transactional
	public void demote(String recordId) throws InvalidIdException, MultipleEditException, NonexistentIdException, AccessDeniedException, RecordLockedException {
	}

	
	public Node<?> deleteNode(Entity parentEntity, Node<?> node) {
		NodeDefinition def = node.getDefinition();
		String name = def.getName();
		List<Node<?>> children = parentEntity.getAll(name);
		int index = children.indexOf(node);
		Node<?> deleted = parentEntity.remove(name, index);
		return deleted;
	}
	
	public Attribute<?, ?> addAttribute(Entity parentEntity, AttributeDefinition def, Object value, Character symbol, String remarks) {
		String name = def.getName();
		Attribute<?, ?> result = null;
		if(def instanceof BooleanAttributeDefinition) {
			result = parentEntity.addValue(name, (Boolean) value);
		} else if(def instanceof CodeAttributeDefinition) {
			result = parentEntity.addValue(name, (Code) value);
		} else if(def instanceof CoordinateAttributeDefinition) {
			result = parentEntity.addValue(name, (Coordinate) value);
		} else if(def instanceof DateAttributeDefinition) {
			result = parentEntity.addValue(name, (org.openforis.idm.model.Date) value);
		} else if(def instanceof NumericAttributeDefinition) {
			Type type = ((NumericAttributeDefinition) def).getType();
			switch(type) {
				case INTEGER:
					result = parentEntity.addValue(name, (Integer) value);
					break;
				case REAL:
					result = parentEntity.addValue(name, (Double) value);
					break;
			}
		} else if(def instanceof TaxonAttributeDefinition) {
			result = parentEntity.addValue(name, (TaxonOccurrence) value);
		} else if(def instanceof TextAttributeDefinition) {
			result = parentEntity.addValue(name, (String) value);
		} else if(def instanceof TimeAttributeDefinition) {
			result = parentEntity.addValue(name, (Time) value);
		}
		
		result.setSymbol(symbol);
		result.setRemarks(remarks);
		return result;
	}
	
	
	
	public List<Attribute<?, ?>> updateAttributes(Entity parentEntity, AttributeDefinition def, List<?> values, Character symbol, String remarks) {
		List<Attribute<?, ?>> result;
		if(def.isMultiple()) {
			String name = def.getName();
			//remove old attributes
			int count = parentEntity.getCount(def.getName());
			for (int i = count - 1; i >= 0; i--) {
				parentEntity.remove(name, i);
			}
			//add new attributes
			result = addAttributes(parentEntity, def, values, symbol, remarks);
		} else {
			throw new RuntimeException("Multiple attribute expected");
		}
		return result;
	}

	public List<Attribute<?, ?>> addAttributes(Entity parentEntity, AttributeDefinition def, List<?> values, Character symbol, String remarks) {
		List<Attribute<?, ?>> result = new ArrayList<Attribute<?,?>>();
		if(values != null) {
			for (Object v : values) {
				Attribute<?, ?> attribute = addAttribute(parentEntity, def, v, symbol, remarks);
				result.add(attribute);
			}
		} else {
			Attribute<?, ?> attribute = addAttribute(parentEntity, def, null, symbol, remarks);
			result.add(attribute);
		}
		return result;
	}
	
	public Entity addEntity(Entity parentEntity, String nodeName, ModelVersion version) {
		Entity entity = parentEntity.addEntity(nodeName);
		addEmptyAttributes(entity, version);
		addEmptyEnumeratedEntities(entity, version);
		return entity;
	}
	
	public void addEmptyAttributes(Entity entity, ModelVersion version) {
		EntityDefinition entityDef = entity.getDefinition();
		List<NodeDefinition> childDefinitions = entityDef.getChildDefinitions();
		for (NodeDefinition nodeDef : childDefinitions) {
			if(version.isApplicable(nodeDef)) {
				String name = nodeDef.getName();
				if(entity.getCount(name) == 0) {
					if(nodeDef instanceof AttributeDefinition) {
						addAttribute(entity, (AttributeDefinition) nodeDef, null, null, null);
					} else if(nodeDef instanceof EntityDefinition && ! nodeDef.isMultiple()) {
						addEntity(entity, nodeDef.getName(), version);
					}
				}
			}
		}
	}
	
	public void addEmptyEnumeratedEntities(Entity entity, ModelVersion version) {
		EntityDefinition entityDef = entity.getDefinition();
		List<NodeDefinition> childDefinitions = entityDef.getChildDefinitions();
		for (NodeDefinition childDef : childDefinitions) {
			if(childDef instanceof EntityDefinition && version.isApplicable(childDef)) {
				EntityDefinition childEntityDef = (EntityDefinition) childDef;
				CodeAttributeDefinition codeDef = getEnumeratingAttribute(childEntityDef, version);
				if(codeDef != null) {
					CodeList list = codeDef.getList();
					List<CodeListItem> items = list.getItems();
					for (CodeListItem item : items) {
						if(version.isApplicable(item)) {
							String code = item.getCode();
							if(! hasEnumeratedEntity(entity, childEntityDef, codeDef, code)) {
								Entity addedEntity = addEntity(entity, childEntityDef.getName(), version);
								//there will be an empty CodeAttribute after the adding of the new entity
								//set the value into this node
								CodeAttribute addedCode = (CodeAttribute) addedEntity.get(codeDef.getName(), 0);
								addedCode.setValue(new Code(code));
							}
						}
					}
				}
			}
		}
	}

	private CodeAttributeDefinition getEnumeratingAttribute(EntityDefinition entity, ModelVersion version) {
		List<NodeDefinition> childDefinitions = entity.getChildDefinitions();
		for (NodeDefinition nodeDef : childDefinitions) {
			if(nodeDef instanceof CodeAttributeDefinition && version.isApplicable(nodeDef)) {
				CodeAttributeDefinition codeDef = (CodeAttributeDefinition) nodeDef;
				if(codeDef.isKey() && codeDef.getList() != null) {
					return codeDef;
				}
			}
		}
		return null;
	}
	
	private boolean hasEnumeratedEntity(Entity parentEntity, EntityDefinition entity, CodeAttributeDefinition code, String value) {
		List<Node<?>> children = parentEntity.getAll(entity.getName());
		for (Node<?> node : children) {
			Entity child = (Entity) node;
			Code fixedValue = getFixedCode(child, code);
			if(fixedValue != null && fixedValue.getCode().equals(value)) {
				return true;
			}
		}
		return false;
	}
	
	private Code getFixedCode(Entity entity, CodeAttributeDefinition def) {
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
		EntityDefinition rootEntityDef = rootEntity.getDefinition();
		List<EntityDefinition> countableDefns = getCountableInList(rootEntityDef);
		
		//set counts
		List<Integer> counts = new ArrayList<Integer>();
		for (EntityDefinition def : countableDefns) {
			String name = def.getName();
			int count = rootEntity.getCount(name);
			counts.add(count);
		}
		record.setEntityCounts(counts);
	}
	
	private void updateKeys(CollectRecord record) {
		Entity rootEntity = record.getRootEntity();
		EntityDefinition rootEntityDef = rootEntity.getDefinition();
		List<AttributeDefinition> keyDefns = rootEntityDef.getKeyAttributeDefinitions();
		//set keys
		List<String> keys = new ArrayList<String>();
		for (AttributeDefinition def: keyDefns) {
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
