package org.openforis.collect.persistence.jooq;

import static org.openforis.collect.persistence.jooq.tables.Data.DATA;

import java.util.HashMap;
import java.util.Map;

import org.jooq.InsertSetMoreStep;
import org.jooq.Record;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.model.Entity;
import org.openforis.idm.model.Node;

/**
 * @author G. Miceli
 */
public class DataMapper {
	private Map<Class<?>, NodeMapper> mappers;
	
	public DataMapper() {
		this.mappers = new HashMap<Class<?>, NodeMapper>();
		addMapper(new CodeAttributeMapper());
		addMapper(new DateAttributeMapper());
		addMapper(new EntityRowMapper());
		addMapper(new NumberAttributeMapper());
		addMapper(new TimeAttributeMapper());
	}

	private void addMapper(NodeMapper mapper) {
		mappers.put(mapper.getMappedClass(), mapper);
	}

	private NodeMapper getMapper(Class<?> defnClass) {
		NodeMapper mapper = mappers.get(defnClass);
		if ( mapper == null ) {
			throw new UnsupportedOperationException("No NodeMapper registered for "+defnClass);
		}
		return mapper;
	}

	public void setInsertFields(Node<?> obj, InsertSetMoreStep<?> insert) {
		// Store link to parent node
		if ( obj.getParent() != null ) {
			insert.set(DATA.PARENT_ID, obj.getParent().getId());
		}
		NodeDefinition defn = obj.getDefinition();
		Class<? extends NodeDefinition> defnClass = defn.getClass();
		NodeMapper mapper = getMapper(defnClass);
		mapper.setInsertFields(obj, insert);
	}
	
	public <D extends NodeDefinition, O extends Node<D>> Node<?> addObject(D defn, Record r, Entity parent) {
		NodeMapper mapper = getMapper(defn.getClass());
		Node<?> o = mapper.addObject(defn, r, parent);
		return o;
	}

}
