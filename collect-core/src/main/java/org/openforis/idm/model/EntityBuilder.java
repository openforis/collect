package org.openforis.idm.model;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.openforis.idm.metamodel.AttributeDefinition;
import org.openforis.idm.metamodel.BooleanAttributeDefinition;
import org.openforis.idm.metamodel.CodeAttributeDefinition;
import org.openforis.idm.metamodel.CoordinateAttributeDefinition;
import org.openforis.idm.metamodel.DateAttributeDefinition;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.FileAttributeDefinition;
import org.openforis.idm.metamodel.IdentifiableSurveyObject;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.NumberAttributeDefinition;
import org.openforis.idm.metamodel.RangeAttributeDefinition;
import org.openforis.idm.metamodel.TaxonAttributeDefinition;
import org.openforis.idm.metamodel.TextAttributeDefinition;
import org.openforis.idm.metamodel.TimeAttributeDefinition;
import org.openforis.idm.metamodel.Unit;

/**
 * 
 * @author S. Ricci
 * 
 * Utility class used to add nodes to an existing Entity
 *
 */
public class EntityBuilder {

	public static Entity addEntity(Entity parentEntity, String name) {
		Entity entity = createEntity(parentEntity, name);
		parentEntity.add(entity);
		return entity;
	}

	public static Entity addEntity(Entity parentEntity, String name, int idx) {
		Entity entity = createEntity(parentEntity, name);
		parentEntity.add(entity, idx);
		return entity;
	}

	public static BooleanAttribute addValue(Entity parentEntity, String name, Boolean value, int idx) {
		return addValueInternal(parentEntity, name, new BooleanValue(value), idx, BooleanAttribute.class, BooleanAttributeDefinition.class);
	}

	public static BooleanAttribute addValue(Entity parentEntity, String name, Boolean value) {
		return addValueInternal(parentEntity, name, new BooleanValue(value), null, BooleanAttribute.class, BooleanAttributeDefinition.class);
	}

	public static CodeAttribute addValue(Entity parentEntity, String name, Code value, int idx) {
		return addValueInternal(parentEntity, name, value, idx, CodeAttribute.class, CodeAttributeDefinition.class);
	}

	public static CodeAttribute addValue(Entity parentEntity, String name, Code value) {
		return addValueInternal(parentEntity, name, value, null, CodeAttribute.class, CodeAttributeDefinition.class);
	}

	public static CoordinateAttribute addValue(Entity parentEntity, String name, Coordinate value, int idx) {
		return addValueInternal(parentEntity, name, value, idx, CoordinateAttribute.class, CoordinateAttributeDefinition.class);
	}

	public static CoordinateAttribute addValue(Entity parentEntity, String name, Coordinate value) {
		return addValueInternal(parentEntity, name, value, null, CoordinateAttribute.class, CoordinateAttributeDefinition.class);
	}

	public static FileAttribute addValue(Entity parentEntity, String name, File value, int idx) {
		return addValueInternal(parentEntity, name, value, idx, FileAttribute.class, FileAttributeDefinition.class);
	}

	public static FileAttribute addValue(Entity parentEntity, String name, File value) {
		return addValueInternal(parentEntity, name, value, null, FileAttribute.class, FileAttributeDefinition.class);
	}

	public static RealAttribute addValue(Entity parentEntity, String name, Double value, Unit unit, int idx) {
		return addValueInternal(parentEntity, name, new RealValue(value, getId(unit)), idx, RealAttribute.class, NumberAttributeDefinition.class);
	}

	public static RealAttribute addValue(Entity parentEntity, String name, Double value, Unit unit) {
		return addValueInternal(parentEntity, name, new RealValue(value, getId(unit)), null, RealAttribute.class, NumberAttributeDefinition.class);
	}

	public static IntegerAttribute addValue(Entity parentEntity, String name, Integer value, Unit unit, int idx) {
		return addValueInternal(parentEntity, name, new IntegerValue(value, getId(unit)), idx, IntegerAttribute.class, NumberAttributeDefinition.class);
	}

	public static IntegerAttribute addValue(Entity parentEntity, String name, Integer value, Unit unit) {
		return addValueInternal(parentEntity, name, new IntegerValue(value, getId(unit)), null, IntegerAttribute.class, NumberAttributeDefinition.class);
	}

	public static RealAttribute addValue(Entity parentEntity, String name, Double value, int idx) {
		return addValueInternal(parentEntity, name, new RealValue(value, null), idx, RealAttribute.class, NumberAttributeDefinition.class);
	}

	public static RealAttribute addValue(Entity parentEntity, String name, Double value) {
		return addValueInternal(parentEntity, name, new RealValue(value, null), null, RealAttribute.class, NumberAttributeDefinition.class);
	}

	public static IntegerAttribute addValue(Entity parentEntity, String name, Integer value, int idx) {
		return addValueInternal(parentEntity, name, new IntegerValue(value, null), idx, IntegerAttribute.class, NumberAttributeDefinition.class);
	}

	public static IntegerAttribute addValue(Entity parentEntity, String name, Integer value) {
		return addValueInternal(parentEntity, name, new IntegerValue(value, null), null, IntegerAttribute.class, NumberAttributeDefinition.class);
	}

	public static RealRangeAttribute addValue(Entity parentEntity, String name, RealRange value, int idx) {
		return addValueInternal(parentEntity, name, value, idx, RealRangeAttribute.class, RangeAttributeDefinition.class);
	}

	public static RealRangeAttribute addValue(Entity parentEntity, String name, RealRange value) {
		return addValueInternal(parentEntity, name, value, null, RealRangeAttribute.class, RangeAttributeDefinition.class);
	}

	public static IntegerRangeAttribute addValue(Entity parentEntity, String name, IntegerRange value, int idx) {
		return addValueInternal(parentEntity, name, value, idx, IntegerRangeAttribute.class, RangeAttributeDefinition.class);
	}

	public static IntegerRangeAttribute addValue(Entity parentEntity, String name, IntegerRange value) {
		return addValueInternal(parentEntity, name, value, null, IntegerRangeAttribute.class, RangeAttributeDefinition.class);
	}
	
	public static DateAttribute addValue(Entity parentEntity, String name, Date value, int idx) {
		return addValueInternal(parentEntity, name, value, idx, DateAttribute.class, DateAttributeDefinition.class);
	}

	public static DateAttribute addValue(Entity parentEntity, String name, Date value) {
		return addValueInternal(parentEntity, name, value, null, DateAttribute.class, DateAttributeDefinition.class);
	}
	
	public static TextAttribute addValue(Entity parentEntity, String name, String value, int idx) {
		return addValueInternal(parentEntity, name, new TextValue(value), idx, TextAttribute.class, TextAttributeDefinition.class);
	}

	public static TextAttribute addValue(Entity parentEntity, String name, String value) {
		return addValueInternal(parentEntity, name, new TextValue(value), null, TextAttribute.class, TextAttributeDefinition.class);
	}

	public static TaxonAttribute addValue(Entity parentEntity, String name, TaxonOccurrence value, int idx) {
		return addValueInternal(parentEntity, name, value, idx, TaxonAttribute.class, TaxonAttributeDefinition.class);
	}

	public static TaxonAttribute addValue(Entity parentEntity, String name, TaxonOccurrence value) {
		return addValueInternal(parentEntity, name, value, null, TaxonAttribute.class, TaxonAttributeDefinition.class);
	}

	public static TimeAttribute addValue(Entity parentEntity, String name, Time value, int idx) {
		return addValueInternal(parentEntity, name, value, idx, TimeAttribute.class, TimeAttributeDefinition.class);
	}

	public static TimeAttribute addValue(Entity parentEntity, String name, Time value) {
		return addValueInternal(parentEntity, name, value, null, TimeAttribute.class, TimeAttributeDefinition.class);
	}
	
	private static <T extends Attribute<D, V>, 
				    D extends AttributeDefinition, 
				    V extends Value> 
			T addValueInternal(Entity entity, String name, V value, Integer idx,
					           Class<T> type, Class<D> definitionType) {
		T attr = createNode(entity, name, type, definitionType);
		if ( idx != null ) {
			entity.add(attr, idx);
		} else {
			entity.add(attr);
		}
		attr.setValue(value);
		attr.updateSummaryInfo();
		return attr;
	}

	private static <T extends Node<D>, D extends NodeDefinition> T createNode(Entity entity, String name, Class<T> type, Class<D> definitionType) {
		try {
			EntityDefinition entityDefn = entity.getDefinition();
			NodeDefinition definition = entityDefn.getChildDefinition(name, definitionType);
			Constructor<T> constructor = type.getConstructor(definitionType);
			return constructor.newInstance(definition);
		} catch (SecurityException e) {
			throw new RuntimeException(e);
		} catch (InstantiationException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		} catch (InvocationTargetException e) {
			if (e.getCause() instanceof RuntimeException) {
				throw (RuntimeException) e.getCause();
			} else {
				throw new RuntimeException(e);
			}
		} catch (NoSuchMethodException e) {
			throw new RuntimeException(e);
		}
	}

	public static Entity createEntity(Entity parentEntity, String name) {
		EntityDefinition parentDefn = parentEntity.getDefinition();
		EntityDefinition defn = parentDefn.getChildDefinition(name, EntityDefinition.class);
		Entity entity = (Entity) defn.createNode();
		return entity;
	}

	private static Integer getId(IdentifiableSurveyObject<?> obj) {
		return obj == null ? null : obj.getId();
	}
}
