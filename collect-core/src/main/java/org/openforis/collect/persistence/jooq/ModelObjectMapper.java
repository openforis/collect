package org.openforis.collect.persistence.jooq;

import static org.openforis.collect.persistence.jooq.tables.Data.DATA;

import java.util.HashMap;
import java.util.Map;

import org.jooq.InsertSetMoreStep;
import org.jooq.Record;
import org.openforis.idm.metamodel.SchemaObjectDefinition;
import org.openforis.idm.model.Entity;
import org.openforis.idm.model.ModelObject;

/**
 * @author G. Miceli
 */
public class ModelObjectMapper {
	private Map<Class<?>, ModelObjectTypeMapper> mappers;
	
	public ModelObjectMapper() {
		this.mappers = new HashMap<Class<?>, ModelObjectTypeMapper>();
		addMapper(new CodeAttributeRowMapper());
		addMapper(new DateAttributeRowMapper());
		addMapper(new EntityRowMapper());
		addMapper(new NumberAttributeRowMapper());
		addMapper(new TimeAttributeRowMapper());
	}

	private void addMapper(ModelObjectTypeMapper mapper) {
		mappers.put(mapper.getMappedClass(), mapper);
	}

	private ModelObjectTypeMapper getMapper(Class<?> defnClass) {
		ModelObjectTypeMapper mapper = mappers.get(defnClass);
		if ( mapper == null ) {
			throw new UnsupportedOperationException("No ModelObjectMapper registered for "+defnClass);
		}
		return mapper;
	}

	public void setInsertFields(ModelObject<?> obj, InsertSetMoreStep<?> insert) {
		// Store link to parent node
		if ( obj.getParent() != null ) {
			insert.set(DATA.PARENT_ID, obj.getParent().getId());
		}
		SchemaObjectDefinition defn = obj.getDefinition();
		Class<? extends SchemaObjectDefinition> defnClass = defn.getClass();
		ModelObjectTypeMapper mapper = getMapper(defnClass);
		mapper.setInsertFields(obj, insert);
	}
	
	public <D extends SchemaObjectDefinition, O extends ModelObject<D>> ModelObject<?> addObject(D defn, Record r, Entity parent) {
		ModelObjectTypeMapper mapper = getMapper(defn.getClass());
		Integer id = r.getValueAsInteger(DATA.ID);
		ModelObject<?> o = mapper.addObject(defn, r, parent);
		o.setId(id);
		return o;
	}

}
