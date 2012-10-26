package org.openforis.collect.designer.model;

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

}
