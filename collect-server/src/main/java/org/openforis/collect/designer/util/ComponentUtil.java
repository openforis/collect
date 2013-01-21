package org.openforis.collect.designer.util;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.HtmlBasedComponent;

/**
 * 
 * @author S. Ricci
 *
 */
public class ComponentUtil {
	
	public static final String COMPOSER_ID = "$composer";

	@SuppressWarnings("unchecked")
	public static <T> T getComposer(Component view)  {
		return (T) view.getAttribute(COMPOSER_ID);
	}
	
	public static void addClass(HtmlBasedComponent component, String className) {
		String oldSclass = component.getSclass();
		if ( oldSclass == null ) {
			oldSclass = "";
		}
		if ( !  oldSclass.contains(className) ) {
			component.setSclass(oldSclass + " " + className);
		}
	}
	
	public static void removeClass(HtmlBasedComponent component, String className) {
		String oldSclass = component.getSclass();
		if ( oldSclass != null ) {
			component.setSclass(oldSclass.replaceAll(className, ""));
		}
	}

	public static void toggleClass(HtmlBasedComponent component, String className, boolean present) {
		if ( present ) {
			addClass(component, className);
		} else {
			removeClass(component, className);
		}
	}

}
