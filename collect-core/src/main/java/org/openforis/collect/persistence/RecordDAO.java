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
import org.openforis.collect.persistence.jooq.tables.records.DataRecord;
import org.openforis.idm.metamodel.CodeAttributeDefinition;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.Schema;
import org.openforis.idm.metamodel.SchemaObjectDefinition;
import org.openforis.idm.metamodel.Survey;
import org.openforis.idm.model.Code;
import org.openforis.idm.model.CodeAttribute;
import org.openforis.idm.model.Date;
import org.openforis.idm.model.DateAttribute;
import org.openforis.idm.model.Entity;
import org.openforis.idm.model.ModelObject;
import org.openforis.idm.model.NumberAttribute;
import org.openforis.idm.model.Time;
import org.openforis.idm.model.TimeAttribute;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author G. Miceli
 * @author M. Togna
 */
public class RecordDAO extends CollectDAO {

	public CollectRecord load(Survey survey, int recordId) throws DataInconsistencyException {
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
		
		loadData(record);
		
		return record;
	}
	
	private void loadData(CollectRecord record) throws DataInconsistencyException {
		Survey survey = record.getSurvey();
		Schema schema = survey.getSchema();
		Factory jf = getJooqFactory();
		Result<Record> data = 
				   jf.select()
					 .from(DATA)
					 .where(DATA.RECORD_ID.equal(record.getId()))
					 .orderBy(DATA.ID)
					 .fetch();
//		Entity entity = null;
		for (Record r : data) {
			Integer parentId = r.getValueAsInteger(DATA.PARENT_ID);
			Integer id = r.getValueAsInteger(DATA.ID);
			Integer defnId = r.getValueAsInteger(DATA.DEFINITION_ID);
			if ( parentId == null ) {
				// Check and process root entity
				Entity rootEntity = record.getRootEntity();
				if ( rootEntity.getDefinition().getId() != defnId ) {
					throw new DataInconsistencyException(DATA.DEFINITION_ID+" does not match "+RECORD.ROOT_ENTITY_ID);
				}
				rootEntity.setId(id);
			} else {
				// Process other objects 
				ModelObject<? extends SchemaObjectDefinition> parent = record.getModelObjectById(parentId);
				if ( parent == null ) {
					throw new DataInconsistencyException("Unknown parent "+parentId+" in "+DATA.PARENT_ID);					
				}
				if ( !(parent instanceof Entity) ) {
					throw new DataInconsistencyException("Invalid parent "+parentId+" in "+DATA.PARENT_ID);					
				}
				Entity parentEntity = (Entity) parent; 
				SchemaObjectDefinition defn = schema.getById(defnId);
				ModelObject o = null;
				if ( defn instanceof EntityDefinition ) {
//					o = parentEntity.addEntity(defn.getName());
				} else if ( defn instanceof CodeAttributeDefinition ) {
//					o = parentEntity.addValue(name, value)
					// TODO Store scheme with codes?
				}
				o.setId(id);
			}
//			}
		}
		// TODO Auto-generated method stub
		
	}

	@Transactional
	public void saveOrUpdate(CollectRecord record) {
		if ( record.getId() == null ) {
			insert(record);
		} else {
			update(record);
			deleteData(record.getId());
		}
		insertData(record);
	}

	private void insert(CollectRecord record) {
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
	
	private void update(CollectRecord record) {
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
				insertData(record, node, i);
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
	
	private void insertData(CollectRecord record, ModelObject<? extends SchemaObjectDefinition> node, int idx) {
		Integer defnId = node.getDefinition().getId();
		if ( defnId == null ) {
			throw new IllegalArgumentException("Null schema object definition id");			
		}
		Factory jf = getJooqFactory();
		int id = jf.nextval(DATA_ID_SEQ).intValue();
		InsertSetMoreStep<DataRecord> insert = 
				jf.insertInto(DATA)
				  .set(DATA.ID, id)
				  .set(DATA.DEFINITION_ID, defnId)
				  .set(DATA.RECORD_ID, record.getId())
				  .set(DATA.IDX, idx+1);
		
		// Store link to parent node
		if ( node.getParent() != null ) {
			insert.set(DATA.PARENT_ID, node.getParent().getId());
		}
		
		if ( node instanceof Entity ) {
			// NOOP
		} else if ( node instanceof CodeAttribute ) {
			Code<?> value = ((CodeAttribute<?>) node).getValue();
			insert.set(DATA.TEXT1, String.valueOf(value.getCode()));
			insert.set(DATA.TEXT2, value.getQualifier());
		} else if ( node instanceof NumberAttribute ) {
			Number value = ((NumberAttribute<?>) node).getValue();
			insert.set(DATA.NUMBER1, value == null ? null : value.doubleValue());
		} else if ( node instanceof DateAttribute ) {
			Date value = ((DateAttribute) node).getValue();
			if ( value != null ) {
				insert.set(DATA.NUMBER1, value.getYear() == null ? null : value.getYear().doubleValue());
				insert.set(DATA.NUMBER2, value.getMonth() == null ? null : value.getMonth().doubleValue());
				insert.set(DATA.NUMBER3, value.getDay() == null ? null : value.getDay().doubleValue());
			}
		} else if ( node instanceof TimeAttribute ) {
			Time value = ((TimeAttribute) node).getValue();
			if ( value != null ) {
				insert.set(DATA.NUMBER1, value.getHour() == null ? null : value.getHour().doubleValue());
				insert.set(DATA.NUMBER2, value.getMinute() == null ? null : value.getMinute().doubleValue());
			}
		} else {
			throw new UnsupportedOperationException("Cannot save "+node.getClass());
		}
		
		insert.execute();
		
		node.setId(id);
	}

	private class ModelObjectStack extends Stack<List<ModelObject<? extends SchemaObjectDefinition>>> {
		private static final long serialVersionUID = 1L;
		
		
		public ModelObjectStack(Entity root) {
			ArrayList<ModelObject<? extends SchemaObjectDefinition>> rootList = new ArrayList<ModelObject<? extends SchemaObjectDefinition>>(1);
			rootList.add(root);
			push(rootList);
		}
//		public ModelObject<? extends SchemaObjectDefinition> push(ModelObject<? extends SchemaObjectDefinition> item) {
//		}
	}
}
