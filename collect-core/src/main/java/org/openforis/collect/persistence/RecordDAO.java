package org.openforis.collect.persistence;

import static org.openforis.collect.persistence.jooq.Sequences.DATA_ID_SEQ;
import static org.openforis.collect.persistence.jooq.Sequences.RECORD_ID_SEQ;
import static org.openforis.collect.persistence.jooq.tables.Data.DATA;
import static org.openforis.collect.persistence.jooq.tables.Record.RECORD;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.jooq.InsertSetMoreStep;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.impl.Factory;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.persistence.jooq.ModelObjectMapper;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.Schema;
import org.openforis.idm.metamodel.SchemaObjectDefinition;
import org.openforis.idm.metamodel.Survey;
import org.openforis.idm.model.Entity;
import org.openforis.idm.model.ModelObject;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author G. Miceli
 * @author M. Togna
 */
public class RecordDAO extends CollectDAO {

	private ModelObjectMapper jooqMapper;
	
	public RecordDAO() {
		this.jooqMapper = new ModelObjectMapper();
	}
	
	@Transactional
	public CollectRecord load(Survey survey, int recordId) throws DataInconsistencyException {
		CollectRecord record = loadRecord(survey, recordId);	
		loadData(record);
		
		return record;
	}

	@Transactional
	public void saveOrUpdate(CollectRecord record) {
		if ( record.getId() == null ) {
			insertRecord(record);
		} else {
			updateRecord(record);
			deleteData(record.getId());
		}
		insertData(record);
	}

	private CollectRecord loadRecord(Survey survey, int recordId) {
		Factory jf = getJooqFactory();
		Record r = jf.select().from(RECORD).where(RECORD.ID.equal(recordId)).fetchOne();
		int rootEntityId = r.getValueAsInteger(RECORD.ROOT_ENTITY_ID);
		String version = r.getValueAsString(RECORD.MODEL_VERSION);
		
		Schema schema = survey.getSchema();
		SchemaObjectDefinition rootEntityDefn = schema.getById(rootEntityId);
		if ( rootEntityDefn == null ) {
			throw new NullPointerException("Unknown root entity id "+rootEntityId);
		}
		String rootEntityName = rootEntityDefn.getName();
		
		CollectRecord record = new CollectRecord(survey, rootEntityName, version);
		record.setId(recordId);
		record.setCreationDate(r.getValueAsDate(RECORD.DATE_CREATED));
		record.setCreatedBy(r.getValueAsString(RECORD.CREATED_BY));
		record.setModifiedDate(r.getValueAsDate(RECORD.DATE_MODIFIED));
		record.setModifiedBy(r.getValueAsString(RECORD.MODIFIED_BY));
		
		return record;
	}
	
	private void loadData(CollectRecord record) throws DataInconsistencyException {
		Survey survey = record.getSurvey();
		Schema schema = survey.getSchema();
		Factory jf = getJooqFactory();
		// Fetch all data for record
		Result<Record> data = 
				   jf.select()
					 .from(DATA)
					 .where(DATA.RECORD_ID.equal(record.getId()))
					 .orderBy(DATA.ID)
					 .fetch();

		for (Record row : data) {
			Integer parentId = row.getValueAsInteger(DATA.PARENT_ID);
			Integer defnId = row.getValueAsInteger(DATA.DEFINITION_ID);
			Entity parent;
			if ( parentId == null ) {
				// Check and process root entity
				Entity rootEntity = record.getRootEntity();
				if ( rootEntity.getDefinition().getId() != defnId ) {
					throw new DataInconsistencyException(DATA.DEFINITION_ID+" does not match "+RECORD.ROOT_ENTITY_ID);
				}
				parent = null;
			} else {
				// Process other objects 
				parent = getParentEntity(record, parentId); 
			}
			SchemaObjectDefinition defn = getDefinition(schema, defnId);
			jooqMapper.addObject(defn, row, parent);
		}
	}

	private SchemaObjectDefinition getDefinition(Schema schema, Integer defnId)
			throws DataInconsistencyException {
		SchemaObjectDefinition defn = schema.getById(defnId);
		if ( defn == null ) {
			throw new DataInconsistencyException("Unknown schema definition "+DATA.DEFINITION_ID);					
		}
		return defn;
	}

	private Entity getParentEntity(CollectRecord record, int parentId) throws DataInconsistencyException {
		ModelObject<? extends SchemaObjectDefinition> parentObject = record.getModelObjectById(parentId);
		if ( parentObject == null ) {
			throw new DataInconsistencyException("Parent "+parentId+" not yet loaded for "+DATA.PARENT_ID);					
		}
		if ( !(parentObject instanceof Entity) ) {
			throw new DataInconsistencyException("Invalid parent "+parentId+" for "+DATA.PARENT_ID);
		}
		return (Entity) parentObject;
	}

	private void insertRecord(CollectRecord record) {
		EntityDefinition rootEntityDefinition = record.getRootEntity().getDefinition();
		Integer rootEntityId = rootEntityDefinition.getId();
		if ( rootEntityId == null ) {
			throw new IllegalArgumentException("Null schema object definition id");
		}
		// Insert into SURVEY table
		Factory jf = getJooqFactory();
		int recordId = jf.nextval(RECORD_ID_SEQ).intValue();
		jf.insertInto(RECORD)
		  .set(RECORD.ID, recordId)
		  .set(RECORD.ROOT_ENTITY_ID, rootEntityId)
		  .set(RECORD.DATE_CREATED, toTimestamp(record.getCreationDate()))
		  .set(RECORD.CREATED_BY, record.getCreatedBy())
		  .set(RECORD.DATE_MODIFIED, toTimestamp(record.getModifiedDate()))
		  .set(RECORD.MODIFIED_BY, record.getModifiedBy())
		  .set(RECORD.MODEL_VERSION, record.getVersion().getName())
		  .execute();
		record.setId(recordId);
	}
	
	private void updateRecord(CollectRecord record) {
		EntityDefinition rootEntityDefinition = record.getRootEntity().getDefinition();
		Integer recordId = record.getId();
		if ( recordId == null ) {
			throw new IllegalArgumentException("Cannot update unsaved record");
		}
		Integer rootEntityId = rootEntityDefinition.getId();
		if ( rootEntityId == null ) {
			throw new IllegalArgumentException("Null schema object definition id");
		}
		// Insert into SURVEY table
		Factory jf = getJooqFactory();
		jf.update(RECORD)
		  .set(RECORD.ROOT_ENTITY_ID, rootEntityId)
		  .set(RECORD.DATE_CREATED, toTimestamp(record.getCreationDate()))
		  .set(RECORD.CREATED_BY, record.getCreatedBy())
		  .set(RECORD.DATE_MODIFIED, toTimestamp(record.getModifiedDate()))
		  .set(RECORD.MODIFIED_BY, record.getModifiedBy())
		  .set(RECORD.MODEL_VERSION, record.getVersion().getName())
		  .where(RECORD.ID.equal(recordId))
		  .execute();
	}

	private void deleteData(int recordId) {
		Factory jf = getJooqFactory();
		jf.delete(DATA)
		  .where(DATA.RECORD_ID.equal(recordId))
		  .execute();
	}

	// N.B.: traversal order matters; parent id's must be set before children!
	private void insertData(CollectRecord record) {
		// Initialize stack with root entity
		Entity root = record.getRootEntity();
		ModelObjectStack stack = new ModelObjectStack(root);
		// While there are still nodes to insert
		while (!stack.isEmpty()) {
			// Pop the next list of nodes to insert
			List<ModelObject<? extends SchemaObjectDefinition>> nodes = stack.pop();
			// Insert this list in order
			for (int i=0; i<nodes.size(); i++) {
				ModelObject<? extends SchemaObjectDefinition> node = nodes.get(i);
				insertDataRow(record, node, i);
				// For entities, add existing child nodes to the stack
				if (node instanceof Entity) {
					Entity entity = (Entity) node;
					List<SchemaObjectDefinition> childDefns = entity.getDefinition().getChildDefinitions();
					for (SchemaObjectDefinition childDefn : childDefns) {
						List<ModelObject<? extends SchemaObjectDefinition>> children = entity.getAll(childDefn.getName());
						if ( children != null ) {
							stack.push(children);
						}					
					}
				}
			}
		}
		
	}
	
	private void insertDataRow(CollectRecord record, ModelObject<? extends SchemaObjectDefinition> node, int idx) {
		Integer defnId = node.getDefinition().getId();
		if ( defnId == null ) {
			throw new IllegalArgumentException("Null schema object definition id");			
		}
		Factory jf = getJooqFactory();
		int dataRowId = jf.nextval(DATA_ID_SEQ).intValue();
		InsertSetMoreStep<?> insert = 
				jf.insertInto(DATA)
				  .set(DATA.ID, dataRowId)
				  .set(DATA.DEFINITION_ID, defnId)
				  .set(DATA.RECORD_ID, record.getId())
				  .set(DATA.IDX, idx+1);
		jooqMapper.setInsertFields(node, insert);

		insert.execute();
		
		node.setId(dataRowId);
	}

	private class ModelObjectStack extends Stack<List<ModelObject<? extends SchemaObjectDefinition>>> {
		private static final long serialVersionUID = 1L;
		
		public ModelObjectStack(Entity root) {
			ArrayList<ModelObject<? extends SchemaObjectDefinition>> rootList = new ArrayList<ModelObject<? extends SchemaObjectDefinition>>(1);
			rootList.add(root);
			push(rootList);
		}
	}
}
