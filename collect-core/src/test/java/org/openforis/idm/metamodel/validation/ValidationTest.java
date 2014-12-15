/**
 * 
 */
package org.openforis.idm.metamodel.validation;

import org.junit.BeforeClass;
import org.openforis.idm.AbstractTest;
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
//		State nodeState = new State(node);
//		return validator.validate(nodeState);
		ValidationResults validationResults = validator.validate(attribute);
		return validationResults;
	}
	
}
