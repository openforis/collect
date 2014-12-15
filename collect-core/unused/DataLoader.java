package org.openforis.collect.persistence.jooq;

import static org.openforis.collect.persistence.jooq.tables.OfcData.DATA;
import static org.openforis.collect.persistence.jooq.tables.OfcRecord.RECORD;

import java.util.HashMap;
import java.util.Map;

import org.jooq.Record;
import org.jooq.Result;
import org.jooq.impl.Factory;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.persistence.DataInconsistencyException;
import org.openforis.idm.metamodel.AttributeDefinition;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.model.Attribute;
import org.openforis.idm.model.Entity;
import org.openforis.idm.model.Node;

/**
 * @author G. Miceli
 */
public class DataLoader {

	private Factory jooqFactory;
	private Map<Integer, Node<? extends NodeDefinition>> objectsById;
	
	public DataLoader(Factory jooqFactory) {
		this.objectsById = new HashMap<Integer, Node<? extends NodeDefinition>>();
		this.jooqFactory = jooqFactory;
	}

	public void load(CollectRecord record) throws DataInconsistencyException {
		// Fetch all data for one record
		Result<Record> data = 
		  jooqFactory.select()
					 .from(DATA)
					 .where(DATA.RECORD_ID.equal(record.getId()))
					 .orderBy(DATA.ID)
					 .fetch();
		
		// Iterate results and build tree
		for (Record row : data) {
			processRow(record, row);
		}
	}

	private void processRow(CollectRecord record, Record row) throws DataInconsistencyException {
		Integer id = row.getValueAsInteger(DATA.ID);
		Integer parentId = row.getValueAsInteger(DATA.PARENT_ID);
		Integer defnId = row.getValueAsInteger(DATA.DEFINITION_ID);
		Node<?> o;
		if ( parentId == null ) {
			// Process root entity
			o = record.getRootEntity();
			Integer rootEntityDefnId = o.getDefinition().getId();
			if ( !rootEntityDefnId.equals(defnId) ) {
				throw new DataInconsistencyException(DATA.DEFINITION_ID+" "+defnId+" does not match "+RECORD.ROOT_ENTITY_ID+" "+rootEntityDefnId);
			}
		} else {
			// Process other objects 
			Node<? extends NodeDefinition> parent = objectsById.get(parentId);
			if ( parent == null ) {
				throw new DataInconsistencyException("Parent "+parentId+" not yet loaded");					
			}
			if ( !(parent instanceof Entity) ) {
				throw new DataInconsistencyException("Parent "+parentId+" not an entity");
			}
			NodeDefinition defn = record.getSurvey().getSchema().getById(defnId);
			if ( defn == null ) {
				throw new DataInconsistencyException("Unknown schema definition "+DATA.DEFINITION_ID);					
			}
			NodeMapper mapper = NodeMapper.getInstance(defn.getClass());
			Node<?> o1 = mapper.addNode(defn, row, (Entity) parent);
			
			if(defn instanceof AttributeDefinition) {
				String remarks = row.getValue(DATA.REMARKS);
				String s = row.getValueAsString(DATA.SYMBOL);
				Character symbol = null;
				if(s != null && s.length() == 1) {
					symbol = s.charAt(0);
				}
				Attribute<?, ?> attribute = (Attribute<?, ?>) o1;
				attribute.setRemarks(remarks);
				attribute.setSymbol(symbol);
			}
			o = o1;
		}
		objectsById.put(id, o);
	}

}
