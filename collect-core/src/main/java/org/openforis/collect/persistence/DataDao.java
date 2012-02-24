package org.openforis.collect.persistence;

import static org.openforis.collect.persistence.jooq.Sequences.DATA_ID_SEQ;
import static org.openforis.collect.persistence.jooq.tables.Data.DATA;

import java.util.HashMap;

import org.jooq.BatchBindStep;
import org.jooq.InsertValuesStep;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.impl.Factory;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.persistence.jooq.JooqDaoSupport;
import org.openforis.collect.persistence.jooq.tables.records.DataRecord;
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
		Entity root = record.getRootEntity();
		InsertQueryNodeVisitor visitor = new InsertQueryNodeVisitor();
		root.traverse(visitor);
		BatchBindStep batch = visitor.getBatch();
		batch.execute();
	}

	private class InsertQueryNodeVisitor implements NodeVisitor {
		private BatchBindStep batch;
		
		public InsertQueryNodeVisitor() {
			Factory jf = getJooqFactory();
			InsertValuesStep<DataRecord> query = jf.insertInto(
					DATA, 
					DATA.ID,
					DATA.DEFINITION_ID, 
					DATA.RECORD_ID, 
					DATA.POSITION, 
					DATA.PARENT_ID,
					DATA.FIELD,
					DATA.VALUE,
					DATA.REMARKS,
					DATA.SYMBOL
				)
				.values(null, null, null, null, null, null, null, null, null);
			batch = jf.batch(query);
		}
		
		@Override
		public void visit(Node<? extends NodeDefinition> node, int idx) {
			insert(node, idx);
		}
		
		public BatchBindStep getBatch() {
			return batch;
		}
		
		private void insert(Node<?> node, int idx) {
			CollectRecord record = (CollectRecord) node.getRecord();
			Integer recordId = record.getId();
			Integer parentId = getParentId(node);
			NodeDefinition defn = node.getDefinition();
			Integer defnId = defn.getId();
			if ( defnId == null ) {
				throw new NullPointerException("Null schema object definition id");			
			}
			Integer id;
			if ( node instanceof Entity ) {
				id = addRecord(defnId, recordId, idx + 1, parentId, 1, null, null, null);
				node.setId(id);
			} else if ( node instanceof Attribute ) {
				addAttribute(defnId, recordId, idx + 1, parentId, (Attribute<?,?>) node);
			} else {
				throw new RuntimeException("Unknown node "+node.getClass());
			}
		}

		private void addAttribute(Integer defnId, Integer recordId, Integer position, Integer parentId, Attribute<?, ?> attribute) {
			for (int f = 0; f < attribute.getFieldCount(); f++) {
				Field<?> field = attribute.getField(f);
				Object value = field.getValue();
				String valueString = value == null ? null : value.toString();
				Character symbol = field.getSymbol();
				String symbolString = symbol == null ? null : symbol.toString();
				addRecord(defnId, recordId, position, parentId, f+1, valueString, field.getRemarks(), symbolString);
			}
		}
		
		private Integer addRecord(Integer defnId, Integer recordId, Integer idx, Integer parentId, Integer field, String value, String remarks, String symbol) {
			Factory jf = getJooqFactory();
			Integer id = jf.nextval(DATA_ID_SEQ).intValue();
			batch.bind(id, defnId, recordId, idx, parentId, field, value, remarks, symbol);
			return id;
		}
		
		private Integer getParentId(Node<?> node) {
			Integer parentId = null;
			Entity parent = node.getParent();
			if ( parent != null ) {
				parentId = parent.getId();
				if ( parentId == null ) {
					throw new IllegalStateException("Unsaved parent");
				}
			}
			return parentId;
		}
	}
	

	void load(CollectRecord record) throws DataInconsistencyException {
		// Fetch all data for one record
		Factory jooqFactory = getJooqFactory();
		Result<Record> data = 
		  jooqFactory.select()
					 .from(DATA)
					 .where(DATA.RECORD_ID.equal(record.getId()))
					 .orderBy(DATA.ID)
					 .fetch();
		
		ParentIdMap parents = new ParentIdMap();

		// Iterate results and build tree
		for (Record row : data) {
			Integer id = row.getValue(DATA.ID);
			Integer parentId = row.getValue(DATA.PARENT_ID);
			Integer defnId = row.getValue(DATA.DEFINITION_ID);
			Integer fieldNo = row.getValue(DATA.FIELD)-1;
			int idx = row.getValue(DATA.POSITION)-1;
			String remarks = row.getValue(DATA.REMARKS);
			String symbol = row.getValue(DATA.SYMBOL);
			String value = row.getValue(DATA.VALUE);
			
			Survey survey = record.getSurvey();
			Schema schema = survey.getSchema();
			NodeDefinition defn = schema.getById(defnId);
			if ( defn == null ) {
				throw new DataInconsistencyException("Unknown schema definition "+DATA.DEFINITION_ID);					
			}
			String name = defn.getName();
			Node<?> node;
			if ( parentId == null ) {
				// Process root entity
				node = record.createRootEntity(defn.getName());
			} else {
				// Process other objects 
				Entity parent = parents.get(parentId);
				node = parent.get(name, idx);
				if ( node == null ) {
					// Created new node
					node = defn.createNode();
					parent.add(node);
				}
				// Copy attribute field
				if ( node instanceof Attribute ) {
					Field<?> field = ((Attribute<?,?>) node).getField(fieldNo);
					field.setValueFromString(value);
					field.setRemarks(remarks);
					field.setSymbol(symbol == null ? null : symbol.charAt(0));
				}
			}
			if ( node instanceof Entity ) {
				parents.put(id, (Entity) node);
			}
		}
	}

	
	private static class ParentIdMap extends HashMap<Integer, Entity> {
		private static final long serialVersionUID = 1L;
		@Override
		public Entity get(Object parentId) {
			Entity parent = super.get(parentId);
			if ( parent == null ) {
				throw new DataInconsistencyException("Parent "+parentId+" not yet loaded");					
			}
			if ( !(parent instanceof Entity) ) {
				throw new DataInconsistencyException("Parent "+parentId+" not an entity");
			}
			return parent;
		}
	}
}
