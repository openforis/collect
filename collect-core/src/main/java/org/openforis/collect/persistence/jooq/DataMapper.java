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
public class DataMapper {
	private Map<Class<?>, ModelObjectMapper> mappers;
	
	public DataMapper() {
		this.mappers = new HashMap<Class<?>, ModelObjectMapper>();
		addMapper(new CodeAttributeMapper());
		addMapper(new DateAttributeMapper());
		addMapper(new EntityRowMapper());
		addMapper(new NumberAttributeMapper());
		addMapper(new TimeAttributeMapper());
	}

	private void addMapper(ModelObjectMapper mapper) {
		mappers.put(mapper.getMappedClass(), mapper);
	}

	private ModelObjectMapper getMapper(Class<?> defnClass) {
		ModelObjectMapper mapper = mappers.get(defnClass);
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
		ModelObjectMapper mapper = getMapper(defnClass);
		mapper.setInsertFields(obj, insert);
	}
	
	public <D extends SchemaObjectDefinition, O extends ModelObject<D>> ModelObject<?> addObject(D defn, Record r, Entity parent) {
		ModelObjectMapper mapper = getMapper(defn.getClass());
		ModelObject<?> o = mapper.addObject(defn, r, parent);
		return o;
	}

}
