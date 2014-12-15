/**
 * 
 */
package org.openforis.collect.metamodel.validation;

import org.junit.BeforeClass;
import org.openforis.collect.AbstractTest;
import org.openforis.idm.metamodel.validation.ValidationResults;
import org.openforis.idm.metamodel.validation.Validator;
import org.openforis.idm.model.Attribute;

/**
 * @author M. Togna
 *
 */
public abstract class ValidationTest  extends AbstractTest{

	protected static Validator validator;

	@BeforeClass
	public static void init(){
		validator = new Validator();
	}

	protected ValidationResults validate(Attribute<?, ?> attribute){
		ValidationResults validationResults = validator.validate(attribute);
		return validationResults;
	}

}