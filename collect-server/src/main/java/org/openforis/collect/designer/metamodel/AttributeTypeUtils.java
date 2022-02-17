package org.openforis.collect.designer.metamodel;

import java.util.Locale;

import org.apache.commons.lang3.StringUtils;
import org.openforis.idm.metamodel.AttributeDefinition;
import org.openforis.idm.metamodel.AttributeType;
import org.zkoss.util.resource.Labels;

/**
 * 
 * @author S. Ricci
 *
 */
public abstract class AttributeTypeUtils {
	
	public static AttributeType fromLabel(String label) {
		for (AttributeType attributeType : AttributeType.values()) {
			if (getLabel(attributeType).equals(label)) {
				return attributeType;
			}
		}
		return null;
	}
	
	public static String getLabel(AttributeType type) {
		String labelKey = "survey.schema.attribute.type." + type.name().toLowerCase(Locale.ENGLISH);
		return Labels.getLabel(labelKey);
	}
	
	public static String getLabel(String typeStr) {
		if (StringUtils.isNotBlank(typeStr)) {
			AttributeType type = AttributeType.valueOf(typeStr);
			return getLabel(type);
		} else {
			return null;
		}
	}
	
	public static String getLabel(AttributeDefinition attrDefn) {
		if (attrDefn != null) {
			AttributeType type = AttributeType.valueOf(attrDefn);
			return getLabel(type);
		} else {
			return null;
		}
	}
}