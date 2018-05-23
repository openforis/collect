package org.openforis.idm.model.expression.internal;

import org.apache.commons.jxpath.ExpressionContext;

public class EnvironmentFunctions extends CustomFunctions {

	private static final String MOBILE_VM_VENDOR = "The Android Project";

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
		return MOBILE_VM_VENDOR.equalsIgnoreCase(System.getProperty("java.vendor"));
	}
}
