package org.openforis.collect.designer.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.openforis.idm.metamodel.AttributeType;
import org.openforis.idm.metamodel.validation.Check;
import org.openforis.idm.metamodel.validation.ComparisonCheck;
import org.openforis.idm.metamodel.validation.CustomCheck;
import org.openforis.idm.metamodel.validation.DistanceCheck;
import org.openforis.idm.metamodel.validation.PatternCheck;
import org.openforis.idm.metamodel.validation.UniquenessCheck;
import org.zkoss.util.resource.Labels;

/**
 * 
 * @author S. Ricci
 *
 */
public enum CheckType {
	
	COMPARISON, CUSTOM, DISTANCE, PATTERN, UNIQUENESS;
	
	public static CheckType valueOf(Check<?> check) {
		if ( check instanceof ComparisonCheck ) {
			return COMPARISON;
		} else if ( check instanceof CustomCheck ) {
			return CUSTOM;
		} else if ( check instanceof DistanceCheck ) {
			return DISTANCE;
		} else if ( check instanceof PatternCheck ) {
			return PATTERN;
		} else if ( check instanceof UniquenessCheck) {
			return UNIQUENESS;
		} else {
			throw new IllegalArgumentException("Check type not supported: " + check.getClass().getName());
		}
	}
	
	public static Check<?> createCheck(CheckType type) {
		switch (type) {
		case COMPARISON:
			return new ComparisonCheck();
		case CUSTOM:
			return new CustomCheck();
		case DISTANCE:
			return new DistanceCheck();
		case PATTERN:
			return new PatternCheck();
		case UNIQUENESS:
			return new UniquenessCheck();
		default:
			throw new IllegalArgumentException("Check type not supported: " + type);
		}
	}
	
	public static List<CheckType> compatibleValues(AttributeType attributeType) {
		List<CheckType> list = new ArrayList<CheckType>(Arrays.asList(values()));
		if (attributeType != AttributeType.COORDINATE) {
			list.remove(CheckType.DISTANCE);
		}
		if (attributeType != AttributeType.TEXT) {
			list.remove(CheckType.PATTERN);
		}
		return list;
	}
	
	public String getLabel() {
		String labelKey = null;
		switch (this) {
		case COMPARISON:
			labelKey = "survey.schema.node.check.type.comparison";
			break;
		case CUSTOM:
			labelKey = "survey.schema.node.check.type.custom";
			break;
		case DISTANCE:
			labelKey = "survey.schema.node.check.type.distance";
			break;
		case PATTERN:
			labelKey = "survey.schema.node.check.type.pattern";
			break;
		case UNIQUENESS:
			labelKey = "survey.schema.node.check.type.uniqueness";
			break;
		}
		return Labels.getLabel(labelKey);
	}
	
	public String getDefaultMessage() {
		String labelKey = null;
		switch (this) {
		case COMPARISON:
			labelKey = "survey.schema.node.check.type.comparison.default_message";
			break;
		case CUSTOM:
			labelKey = "survey.schema.node.check.type.custom.default_message";
			break;
		case DISTANCE:
			labelKey = "survey.schema.node.check.type.distance.default_message";
			break;
		case PATTERN:
			labelKey = "survey.schema.node.check.type.pattern.default_message";
			break;
		case UNIQUENESS:
			labelKey = "survey.schema.node.check.type.uniqueness.default_message";
			break;
		}
		return Labels.getLabel(labelKey);
	}

}
