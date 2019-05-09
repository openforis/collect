package org.openforis.idm.model.expression.internal;

import org.apache.commons.jxpath.ExpressionContext;
import org.openforis.collect.Environment;

public class EnvironmentFunctions extends CustomFunctions {

	public EnvironmentFunctions(String namespace) {
		super(namespace);
		
		register("desktop", new CustomFunction(0) {
			public Object invoke(ExpressionContext expressionContext, Object[] objects) {
				return !isMobile();
			}
		});
		
		register("mobile", new CustomFunction(0) {
			public Object invoke(ExpressionContext expressionContext, Object[] objects) {
				return isMobile();
			}
		});
	}

	private static boolean isMobile() {
		return Environment.isAndroid();
	}
}
