/**
 * 
 */
package org.openforis.collect.designer.viewmodel;

import org.openforis.collect.designer.form.ComparisonCheckFormObject;
import org.openforis.collect.designer.form.SurveyObjectFormObject;
import org.openforis.idm.metamodel.validation.ComparisonCheck;

/**
 * @author riccist
 *
 */
public class ComparisonCheckVM extends CheckVM<ComparisonCheck> {

	@Override
	protected SurveyObjectFormObject<ComparisonCheck> createFormObject() {
		return new ComparisonCheckFormObject();
	}

}
