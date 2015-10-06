package org.openforis.idm.model.expression.internal;

import java.util.UUID;

import org.apache.commons.jxpath.ExpressionContext;

/**
 * 
 * @author S. Ricci
 *
 */
public class UtilFunctions extends CustomFunctions {

	public UtilFunctions(String namespace) {
		super(namespace);
		
		register("uuid", 0, new CustomFunction() {
			public Object invoke(ExpressionContext expressionContext, Object[] objects) {
				return uuid();
			}
		});
	}
	
	private static String uuid() {
		return UUID.randomUUID().toString();
	}

}
