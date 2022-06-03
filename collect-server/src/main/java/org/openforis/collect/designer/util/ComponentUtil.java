package org.openforis.collect.designer.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.compress.utils.CharsetNames;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.zkoss.bind.Binder;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Execution;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.HtmlBasedComponent;
import org.zkoss.zk.xel.impl.ExecutionResolver;

/**
 * 
 * @author S. Ricci
 *
 */
public class ComponentUtil {
	
	public static final String COMPOSER_ID = "$composer";
	private static final String BINDER_ID = "$BINDER$";
	private static final String FORM_ID_ATTRIBUTE = "$FORM_ID$";

	/**
	 * Returns the component handled by the current {@link Execution} object
	 * @return
	 */
	public static Component getCurrentComponent() {
		Execution execution = Executions.getCurrent();
		ExecutionResolver variableResolver = (ExecutionResolver) execution.getVariableResolver();
		Object self = variableResolver.getSelf();
		Component component = (Component) self;
		return component;
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T getComposer(Component view)  {
		return (T) view.getAttribute(COMPOSER_ID);
	}
	
	public static Binder getBinder(Component component) {
		Binder binder = (Binder) component.getAttribute(BINDER_ID);
		return binder;
	}
	
	public static Object getForm(Binder binder) {
		Component view = binder.getView();
		return getForm(view);
	}

	public static Object getForm(Component view) {
		String formId = (String) view.getAttribute(FORM_ID_ATTRIBUTE);
		return formId == null ? null : view.getAttribute(formId);
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T getViewModel(Component component) {
		Binder binder = getBinder(component);
		Object viewModel = binder.getViewModel();
		return (T) viewModel;
	}
	
	public static <T> T getAncestorViewModel(Class<T> viewModelClass) {
		Component component = getCurrentComponent();
		return component == null ? null: getAncestorViewModel(component, viewModelClass);
	}
		
	@SuppressWarnings("unchecked")
	public static <T> T getAncestorViewModel(Component component, Class<T> viewModelClass) {
		Component currentParent = component.getParent();
		while ( currentParent != null ) {
			Binder binder = getBinder(currentParent);
			if ( binder != null ) {
				Object vm = binder.getViewModel();
				if ( vm != null && viewModelClass.isAssignableFrom(vm.getClass()) ) {
					return (T) vm;
				}
			}
			currentParent = currentParent.getParent();
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	public static <T> T getClosest(Component from, Class<T> type) {
		Component currentParent = from.getParent();
		while ( currentParent != null ) {
			if (type.isAssignableFrom(currentParent.getClass())) {
				return (T) currentParent;
			}
			currentParent = currentParent.getParent();
		}
		return null;
	}
	
	public static void addClass(HtmlBasedComponent component, String className) {
		String oldSclass = component.getSclass();
		if ( oldSclass == null ) {
			oldSclass = "";
		}
		if ( ! oldSclass.contains(className) ) {
			component.setSclass(oldSclass + " " + className);
		}
	}
	
	public static void removeClass(HtmlBasedComponent component, String className) {
		String oldSclass = component.getSclass();
		if ( oldSclass != null ) {
			component.setSclass(oldSclass.replaceAll("(?:^|\\s)" + className + "(?!\\S)", ""));
		}
	}

	public static void toggleClass(HtmlBasedComponent component, String className, boolean present) {
		if ( present ) {
			addClass(component, className);
		} else {
			removeClass(component, className);
		}
	}

	public static String createUrl(String base, Map<String, String> queryParams) {
		List<BasicNameValuePair> convertedParams = new ArrayList<BasicNameValuePair>();
		Set<Entry<String, String>> paramsEntrySet = queryParams.entrySet();
		for (Entry<String, String> param : paramsEntrySet) {
			BasicNameValuePair valuePair = new BasicNameValuePair(param.getKey(), param.getValue());
			convertedParams.add(valuePair);
		}
		String queryString = URLEncodedUtils.format(convertedParams, CharsetNames.UTF_8);
		String result = base + "?" + queryString;
		return result;
	}

}
