package org.openforis.collect.designer.model;

import org.openforis.idm.metamodel.validation.Check;
import org.openforis.idm.metamodel.validation.ComparisonCheck;
import org.openforis.idm.metamodel.validation.CustomCheck;
import org.zkoss.util.resource.Labels;

/**
 * 
 * @author S. Ricci
 *
 */
public enum CheckType {
	
	COMPARISON, CUSTOM;
	
	public static CheckType valueOf(Check<?> check) {
		if ( check instanceof ComparisonCheck ) {
			return COMPARISON;
		} else if ( check instanceof CustomCheck ) {
			return CUSTOM;
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
		}
		return Labels.getLabel(labelKey);
	}

}
