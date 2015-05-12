package org.openforis.collect.persistence.jooq;

import static org.openforis.collect.persistence.jooq.Sequences.OFC_DATA_ID_SEQ;
import static org.openforis.collect.persistence.jooq.tables.OfcData.DATA;

import java.util.HashMap;
import java.util.Map;

import org.jooq.InsertSetMoreStep;
import org.jooq.impl.Factory;
import org.openforis.collect.model.CollectRecord;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.model.Attribute;
import org.openforis.idm.model.Node;

/**
 * @author G. Miceli
 */
public class DataPersister {

	private Map<Integer,Integer> dataIds;
	private Factory jooqFactory;

	public DataPersister(Factory jooqFactory) {
		this.dataIds = new HashMap<Integer, Integer>();
		this.jooqFactory = jooqFactory;
	}

	public void persist(Node<? extends NodeDefinition> node, int idx) {
		CollectRecord record = (CollectRecord) node.getRecord();
		// Get database ID of parent
		Integer internalId = node.getInternalId();
		Integer parentDataId = null;
		if ( node.getParent() != null ) {
			Integer parentId = node.getParent().getInternalId();
			if ( parentId == null ) {
				throw new NullPointerException("Parent id not set ");
			}
			parentDataId = dataIds.get(parentId);
		}
		
		int dataId = insertRow(record, node, parentDataId, idx);
		
		dataIds.put(internalId, dataId);
	}
	
	private int insertRow(CollectRecord record, Node<? extends NodeDefinition> node, Integer parentId, int idx) {
		Integer defnId = node.getDefinition().getId();
		if ( defnId == null ) {
			throw new IllegalArgumentException("Null schema object definition id");			
		}
		int dataRowId = jooqFactory.nextval(DATA_ID_SEQ).intValue();
		InsertSetMoreStep<?> insert = 
				jooqFactory.insertInto(DATA)
				  .set(DATA.ID, dataRowId)
				  .set(DATA.DEFINITION_ID, defnId)
				  .set(DATA.RECORD_ID, record.getId())
				  .set(DATA.IDX, idx+1)
				  .set(DATA.PARENT_ID, parentId);
		
		NodeDefinition defn = node.getDefinition();
		Class<? extends NodeDefinition> defnClass = defn.getClass();
		
		if (node instanceof Attribute) {
			Attribute<?, ?> attr = (Attribute<?, ?>) node;
			if (attr.getRemarks() != null) {
				insert.set(DATA.REMARKS, attr.getRemarks());
			}
			if (attr.getSymbol() != null) {
				insert.set(DATA.SYMBOL, attr.getSymbol().toString());
			}
		}
		
		NodeMapper mapper = NodeMapper.getInstance(defnClass);
		mapper.setFields(node, insert);

		insert.execute();
		
		return dataRowId;
	}
}
