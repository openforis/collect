package org.openforis.collect.persistence.jooq;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.jooq.InsertSetStep;
import org.jooq.Record;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.model.Entity;
import org.openforis.idm.model.Node;

/**
 * @author G. Miceli
 */
abstract class NodeMapper {
	private static Map<Class<?>, NodeMapper> MAPPERS;

	static {
		MAPPERS = new HashMap<Class<?>, NodeMapper>();
		addMapper(new EntityMapper());
		addMapper(new BooleanAttributeMapper());
		addMapper(new CodeAttributeMapper());
		addMapper(new CoordinateAttributeMapper());
		addMapper(new DateAttributeMapper());
		addMapper(new FileAttributeMapper());
		addMapper(new NumberAttributeMapper());
		addMapper(new TaxonAttributeMapper());
		addMapper(new TextAttributeMapper());
		addMapper(new TimeAttributeMapper());
	}

	private static void addMapper(NodeMapper mapper) {
		MAPPERS.put(mapper.getMappedClass(), mapper);
	}

	public static NodeMapper getInstance(Class<?> defnClass) {
		NodeMapper mapper = MAPPERS.get(defnClass);
		if (mapper == null) {
			throw new UnsupportedOperationException("No NodeMapper registered for " + defnClass);
		}
		return mapper;
	}
	
	abstract Class<? extends NodeDefinition> getMappedClass(); 
	
	abstract void setFields(Node<?> node, InsertSetStep<?> insert);
	
	abstract Node<?> addNode(NodeDefinition defn, Record r, Entity parent);
	
	static BigDecimal toNumeric(Number value) {
		if ( value == null ) {
			return null; 
		} else {
			return BigDecimal.valueOf(value.doubleValue());
		}
	}
	
	static BigDecimal toNumeric(Boolean value) {
		if (value == null) {
			return null;
		} else {
			return value ? BigDecimal.valueOf(1) : BigDecimal.valueOf(0);
		}
	}
}
