package org.openforis.collect.model;

import org.openforis.idm.metamodel.validation.Validator;
import org.openforis.idm.model.RecordContext;
import org.openforis.idm.model.expression.ExpressionFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * @author G. Miceli
 */
public class CollectRecordContext implements RecordContext {

	@Autowired
	private ExpressionFactory expressionFactory;
	
	@Autowired
	private Validator validator;

	@Override
	public ExpressionFactory getExpressionFactory() {
		return expressionFactory;
	}
	
	@Override
	public Validator getValidator() {
		return validator;
	}
}
