package org.openforis.collect.datacleansing.form.validation;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

/**
 * 
 * @author S. Ricci
 *
 */
@Target({TYPE, ANNOTATION_TYPE})
@Retention(RUNTIME)
@Constraint(validatedBy = ValidExpressionValidator.class)
@Documented
public @interface ValidExpression {

	String experssionFieldName();
	String contextNodeDefinitionIdFieldName();
	String thisNodeDefinitionIdFieldName();
	ExpressionType expressionType() default ExpressionType.VALUE;
	
	String message() default "{}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
    
    public enum ExpressionType {
    	BOOLEAN, VALUE
    }
    
}
