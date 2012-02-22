package org.openforis.collect.persistence;

import static org.openforis.collect.persistence.jooq.Sequences.DATA_ID_SEQ;
import static org.openforis.collect.persistence.jooq.tables.Data.DATA;

import java.util.HashMap;

import org.jooq.InsertQuery;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.impl.Factory;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.persistence.jooq.JooqDaoSupport;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.Schema;
import org.openforis.idm.metamodel.Survey;
import org.openforis.idm.model.Attribute;
import org.openforis.idm.model.Entity;
import org.openforis.idm.model.Field;
import org.openforis.idm.model.Node;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author G. Miceli
 */
public class DataDao extends JooqDaoSupport {

	@Transactional
	void insert(Node<? extends NodeDefinition> node, int idx) {
		if ( node instanceof Entity ) {
			insertEntity((Entity) node, idx);
		} else if ( node instanceof Attribute ) {
			insertAttribute((Attribute<?,?>) node, idx);
		} else {
			throw new RuntimeException("Unknown node "+node.getClass());
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


	private InsertQuery<?> createInsertQuery(Node<?> node, int idx) {
		CollectRecord record = (CollectRecord) node.getRecord();
		Integer parentId = getParentId(node);
		NodeDefinition defn = node.getDefinition();
		Integer defnId = defn.getId();
		if ( defnId == null ) {
			throw new NullPointerException("Null schema object definition id");			
		}
		Factory jf = getJooqFactory();
		Integer id = jf.nextval(DATA_ID_SEQ).intValue();
		InsertQuery<?> query = jf.insertQuery(DATA);
		query.addValue(DATA.ID, id);
		query.addValue(DATA.DEFINITION_ID, defnId);
		query.addValue(DATA.RECORD_ID, record.getId());
		query.addValue(DATA.POSITION, idx+1);
		query.addValue(DATA.PARENT_ID, parentId);
		if ( node instanceof Entity ) {
			node.setId(id);
		}
		return query;
	}
	
	private void insertEntity(Entity entity, int idx) {
		InsertQuery<?> q = createInsertQuery(entity, idx);
		q.addValue(DATA.FIELD, 1);
		q.execute();
	}

	private void insertAttribute(Attribute<?,?> attribute, int idx) {
		for (int f = 0; f < attribute.getFieldCount(); f++) {
			Field<?> field = attribute.getField(f);
			Object value = field.getValue();
			Character symbol = field.getSymbol();
			InsertQuery<?> q = createInsertQuery(attribute, idx);
			q.addValue(DATA.FIELD, f+1);
			q.addValue(DATA.VALUE, value == null ? null : value.toString());
			q.addValue(DATA.REMARKS, field.getRemarks());
			q.addValue(DATA.SYMBOL, symbol == null ? null : symbol.toString());
			q.execute();
		}
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
