package org.openforis.collect.persistence;

import java.util.HashMap;

import org.jooq.BatchBindStep;
import org.jooq.InsertValuesStep;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.impl.Factory;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.persistence.jooq.JooqDaoSupport;
import org.openforis.idm.metamodel.AttributeDefinition;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.Schema;
import org.openforis.idm.metamodel.Survey;
import org.openforis.idm.model.Attribute;
import org.openforis.idm.model.Entity;
import org.openforis.idm.model.Field;
import org.openforis.idm.model.Node;
import org.openforis.idm.model.NodeVisitor;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author G. Miceli
 */
public class DataDao extends JooqDaoSupport {

	@Transactional
	public void insertData(CollectRecord record) {
		// N.B.: traversal order matters; dfs so that parent id's are assigned before children
//		Entity root = record.getRootEntity();
//		InsertQueryNodeVisitor visitor = new InsertQueryNodeVisitor();
//		root.traverse(visitor);
//		visitor.entitiesBatch().execute();
//		visitor.attributeValuesBatch().execute();
	}

//	private class InsertQueryNodeVisitor implements NodeVisitor {
//		private BatchBindStep entitiesBatch;
//		private BatchBindStep attributeValuesBatch;
//		
//		public InsertQueryNodeVisitor() {
//			Factory jf = getJooqFactory();
//			InsertValuesStep<?> entityInsert = jf.insertInto(
//					OFC_ENTITY, 
//					OFC_ENTITY.ID,
//					OFC_ENTITY.DEFINITION_ID, 
//					OFC_ENTITY.RECORD_ID, 
//					OFC_ENTITY.POSITION, 
//					OFC_ENTITY.PARENT_ID
//				)
//				.values(null, null, null, null, null);
//			this.entitiesBatch = jf.batch(entityInsert);	
//			
//			InsertValuesStep<?> attributeValueInsert = jf.insertInto(
//					OFC_ATTRIBUTE_VALUE, 
//					OFC_ATTRIBUTE_VALUE.ID,
//					OFC_ATTRIBUTE_VALUE.DEFINITION_ID, 
//					OFC_ATTRIBUTE_VALUE.RECORD_ID, 
//					OFC_ATTRIBUTE_VALUE.POSITION, 
//					OFC_ATTRIBUTE_VALUE.ENTITY_ID,
//					OFC_ATTRIBUTE_VALUE.FIELD,
//					OFC_ATTRIBUTE_VALUE.VALUE,
//					OFC_ATTRIBUTE_VALUE.REMARKS,
//					OFC_ATTRIBUTE_VALUE.SYMBOL
//				)
//				.values(null, null, null, null, null, null, null, null, null);
//			this.attributeValuesBatch = jf.batch(attributeValueInsert);
//		}
//		
//		@Override
//		public void visit(Node<? extends NodeDefinition> node, int idx) {
//			insert(node, idx);
//		}
//		
//		public BatchBindStep entitiesBatch() {
//			return entitiesBatch;
//		}
//		
//		public BatchBindStep attributeValuesBatch() {
//			return attributeValuesBatch;
//		}
//		
//		private void insert(Node<?> node, int idx) {
//			CollectRecord record = (CollectRecord) node.getRecord();
//			Integer recordId = record.getId();
//			Integer parentId = getParentId(node);
//			NodeDefinition defn = node.getDefinition();
//			Integer defnId = defn.getId();
//			if ( defnId == null ) {
//				throw new NullPointerException("Null schema object definition id");			
//			}
//			if ( node instanceof Entity ) {
//				Integer id = addEntityRecord(defnId, recordId, idx+1, parentId);
//				node.setId(id);
//			} else if ( node instanceof Attribute ) {
//				addAttribute(defnId, recordId, idx + 1, parentId, (Attribute<?,?>) node);
//			} else {
//				throw new RuntimeException("Unknown node "+node.getClass());
//			}
//		}
//
//		private void addAttribute(Integer defnId, Integer recordId, Integer position, Integer parentId, Attribute<?, ?> attribute) {
//			for (int f = 0; f < attribute.getFieldCount(); f++) {
//				AttributeField<?> field = attribute.getField(f);
//				Object value = field.getValue();
//				String valueString = value == null ? null : value.toString();
//				Character symbol = field.getSymbol();
//				String symbolString = symbol == null ? null : symbol.toString();
//				addAttributeValueRecord(defnId, recordId, position, parentId, f+1, valueString, field.getRemarks(), symbolString);
////		private Integer addRecord(Integer defnId, Integer recordId, Integer idx, Integer parentId, Integer field, String value, String remarks, String symbol) {
////		}
//			}
//		}
//		
//		private Integer addAttributeValueRecord(int defnId, int recordId, int idx, Integer parentId, int fieldNo, String value, String remarks, String symbol) {
//			Factory jf = getJooqFactory();
//			Integer id = jf.nextval(OFC_ATTRIBUTE_VALUE_ID_SEQ).intValue();
//			attributeValuesBatch.bind(id, defnId, recordId, idx, parentId, fieldNo, value, remarks, symbol);
//			return id;
//		}
//		
//		private Integer addEntityRecord(int defnId, int recordId, int idx, Integer parentId) {
//			Factory jf = getJooqFactory();
//			Integer id = jf.nextval(OFC_ENTITY_ID_SEQ).intValue();
//			entitiesBatch.bind(id, defnId, recordId, idx, parentId);
//			return id;
//		}
//
//		private Integer getParentId(Node<?> node) {
//			Integer parentId = null;
//			Entity parent = node.getParent();
//			if ( parent != null ) {
//				parentId = parent.getId();
//				if ( parentId == null ) {
//					throw new IllegalStateException("Unsaved parent");
//				}
//			}
//			return parentId;
//		}
//	}
	

	void load(CollectRecord record) throws DataInconsistencyException {
//		// Fetch all data for one record
//		Factory jooqFactory = getJooqFactory();
//		Result<Record> entityRows = 
//		  jooqFactory.select()
//					 .from(OFC_ENTITY)
//					 .where(OFC_ENTITY.RECORD_ID.equal(record.getId()))
//					 .orderBy(OFC_ENTITY.ID)
//					 .fetch();
//		
//		NodeResultMapper mapper = new NodeResultMapper(record);
//		if ( entityRows.isEmpty() ) {
//			throw new DataInconsistencyException("Record "+record.getId()+" has not root entity");
//		}
//		mapper.createEntities(entityRows);
//		
//		Result<Record> attributeValueRows = 
//				  jooqFactory.select()
//							 .from(OFC_ATTRIBUTE_VALUE)
//							 .where(OFC_ATTRIBUTE_VALUE.RECORD_ID.equal(record.getId()))
//							 .orderBy(OFC_ATTRIBUTE_VALUE.ID)
//							 .fetch();
//		
//
//		mapper.createAttributes(attributeValueRows);
	}

	
//	private static class NodeResultMapper extends HashMap<Integer, Entity> {
//		private static final long serialVersionUID = 1L;
//		
//		private CollectRecord record;
//		private Schema schema;
//		
//		public NodeResultMapper(CollectRecord record) {
//			this.record = record;
//			Survey survey = record.getSurvey();
//			this.schema = survey.getSchema();
//		}
//		
//		public void createEntities(Result<Record> entityRows) {
//			// Iterate results and build tree
//			for (Record row : entityRows) {
//				Integer id = row.getValue(OFC_ENTITY.ID);
//				Integer parentId = row.getValue(OFC_ENTITY.PARENT_ID);
//				Integer defnId = row.getValue(OFC_ENTITY.DEFINITION_ID);
//				int idx = row.getValue(OFC_ENTITY.POSITION)-1;
//				
//				NodeDefinition defn = schema.getById(defnId);
//				if ( defn == null ) {
//					throw new DataInconsistencyException("Unknown entity definition "+defnId+" in "+OFC_ENTITY+" "+id);					
//				}
//				if ( !(defn instanceof EntityDefinition) ) {
//					throw new DataInconsistencyException("Wrong definition type "+defnId+" ("+defn.getClass().getSimpleName()+") in "+OFC_ENTITY+" "+id);
//				}
//				String name = defn.getName();
//				Node<?> node;
//				if ( parentId == null ) {
//					// Process root entity
//					node = record.createRootEntity(defn.getName());
//				} else {
//					// Process other entities 
//					Entity parent = get(parentId);
//					node = parent.get(name, idx);
//					if ( node == null ) {
//						// Create new node
//						node = defn.createNode();
//						parent.add(node);
//					}
//				}
//				put(id, (Entity) node);
//			}		
//		}
//		
//		public void createAttributes(Result<Record> attributeValueRows) {
//			// Iterate results and build tree
//			for (Record row : attributeValueRows) {
//				Integer id = row.getValue(OFC_ATTRIBUTE_VALUE.ID);
//				Integer parentId = row.getValue(OFC_ATTRIBUTE_VALUE.ENTITY_ID);
//				Integer defnId = row.getValue(OFC_ATTRIBUTE_VALUE.DEFINITION_ID);
//				Integer fieldNo = row.getValue(OFC_ATTRIBUTE_VALUE.FIELD)-1;
//				int idx = row.getValue(OFC_ATTRIBUTE_VALUE.POSITION)-1;
//				String remarks = row.getValue(OFC_ATTRIBUTE_VALUE.REMARKS);
//				String symbol = row.getValue(OFC_ATTRIBUTE_VALUE.SYMBOL);
//				String value = row.getValue(OFC_ATTRIBUTE_VALUE.VALUE);
//				
//				NodeDefinition defn = schema.getById(defnId);
//				if ( defn == null ) {
//					throw new DataInconsistencyException("Unknown attribute definition "+defnId+" in "+OFC_ATTRIBUTE_VALUE+" "+id);					
//				}
//				if ( !(defn instanceof AttributeDefinition) ) {
//					throw new DataInconsistencyException("Wrong definition type "+defnId+" ("+defn.getClass().getSimpleName()+") in "+OFC_ENTITY+" "+id);
//				}
//				if ( parentId == null ) {
//					throw new DataInconsistencyException("Null entity id for attribute "+id);
//				}
//				String name = defn.getName();
//				// Process other objects 
//				Entity parent = get(parentId);
//				Node<?> node = parent.get(name, idx);
//				if ( node == null ) {
//					// Create new node
//					node = defn.createNode();
//					parent.add(node);
//				}
//				// Copy attribute field
//				AttributeField<?> field = ((Attribute<?,?>) node).getField(fieldNo);
//				field.setValueFromString(value);
//				field.setRemarks(remarks);
//				field.setSymbol(symbol == null ? null : symbol.charAt(0));
//			}		
//		}
//
//		@Override
//		public Entity get(Object parentId) {
//			Entity parent = super.get(parentId);
//			if ( parent == null ) {
//				throw new DataInconsistencyException("Entity "+parentId+" not yet loaded");					
//			}
//			return parent;
//		}
//	}
}
