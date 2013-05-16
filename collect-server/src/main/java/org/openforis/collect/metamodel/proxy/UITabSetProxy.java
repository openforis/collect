package org.openforis.collect.metamodel.proxy;

import java.util.ArrayList;
import java.util.List;

import org.granite.messaging.amf.io.util.externalizer.annotation.ExternalizedProperty;
import org.openforis.collect.Proxy;
import org.openforis.collect.metamodel.ui.UITabSet;

/**
 * 
 * @author S. Ricci
 *
 */
public class UITabSetProxy implements Proxy {
	
	private transient UITabSet tabSet;

	public static List<UITabSetProxy> fromList(List<UITabSet> tabSets) {
		List<UITabSetProxy> result = new ArrayList<UITabSetProxy>();
		for (UITabSet tabSet : tabSets) {
			UITabSetProxy proxy = new UITabSetProxy(tabSet);
			result.add(proxy);
		}
		return result;
	}

	public UITabSetProxy(UITabSet tabSet) {
		super();
		this.tabSet = tabSet;
	}

	@ExternalizedProperty
	public String getName() {
		return tabSet.getName();
	}

	@ExternalizedProperty
	public List<UITabProxy> getTabs() {
		return UITabProxy.fromTabList(tabSet.getTabs());
	}

}
