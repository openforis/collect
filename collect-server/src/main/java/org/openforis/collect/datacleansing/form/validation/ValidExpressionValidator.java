/**
 * 
 */
package org.openforis.collect.datacleansing.form.validation;

import java.util.Locale;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.apache.commons.beanutils.PropertyUtils;
import org.openforis.collect.datacleansing.form.validation.ValidExpression.ExpressionType;
import org.openforis.collect.manager.SessionManager;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.idm.metamodel.NodeDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;

/**
 * @author S. Ricci
 *
 */
public class ValidExpressionValidator implements
		ConstraintValidator<ValidExpression, Object> {
	
	private static final String INVALID_EXPRESSION_MESSAGE_KEY = "validation.invalid_expression";
	
	@Autowired
	private org.openforis.idm.metamodel.expression.ExpressionValidator expressionValidator;
	@Autowired
	private SessionManager sessionManager;
	@Autowired
	private MessageSource messageSource;
	
	private CollectSurvey survey;
	private String expressionFieldName;
	private ExpressionType expressionType;
	private String contextNodeDefinitionIdFieldName;
	private String thisNodeDefinitionIdFieldName;

	@Override
	public void initialize(ValidExpression annotation) {
		survey = sessionManager.getActiveSurvey();
		expressionFieldName = annotation.experssionFieldName();
		expressionType = annotation.expressionType();
		contextNodeDefinitionIdFieldName = annotation.contextNodeDefinitionIdFieldName();
		thisNodeDefinitionIdFieldName = annotation.thisNodeDefinitionIdFieldName();
	}

	@Override
	public boolean isValid(Object bean, ConstraintValidatorContext context) {
		if (bean == null) {
			return true;
		}
		try {
			String expression = (String) PropertyUtils.getProperty(bean, expressionFieldName);
			Integer contextNodeDefId = (Integer) PropertyUtils.getProperty(bean, contextNodeDefinitionIdFieldName);
			Integer thisNodeDefId = (Integer) PropertyUtils.getProperty(bean, thisNodeDefinitionIdFieldName);
			if (expression == null || contextNodeDefId == null || thisNodeDefId == null) {
				return true;
			}
			NodeDefinition contextNodeDef = survey.getSchema().getDefinitionById(contextNodeDefId);
			NodeDefinition thisNodeDef = survey.getSchema().getDefinitionById(thisNodeDefId);
			boolean valid;
			switch(expressionType) {
			case BOOLEAN:
				valid = expressionValidator.validateBooleanExpression(thisNodeDef, expression);
				break;
			default:
				valid = expressionValidator.validateValueExpression(contextNodeDef, thisNodeDef, expression);
			}
			 if(! valid) {
				 String message = messageSource.getMessage(INVALID_EXPRESSION_MESSAGE_KEY, new String[0], Locale.ENGLISH);
				 context.buildConstraintViolationWithTemplate(message)
			            .addPropertyNode(expressionFieldName)
			            .addConstraintViolation();
		    }
		    return valid;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
