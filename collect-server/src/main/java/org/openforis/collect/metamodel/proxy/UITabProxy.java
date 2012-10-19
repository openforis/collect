package org.openforis.collect.metamodel.proxy;

import java.util.ArrayList;
import java.util.List;

import org.granite.messaging.amf.io.util.externalizer.annotation.ExternalizedProperty;
import org.openforis.collect.metamodel.ui.UITab;
import org.openforis.idm.metamodel.LanguageSpecificText;

/**
 * 
 * @author S. Ricci
 *
 */
public class UITabProxy extends UITabSetProxy {
	
	private transient UITab uiTab;

	public static List<UITabProxy> fromList(List<UITab> tabs) {
		ArrayList<UITabProxy> result = new ArrayList<UITabProxy>();
		for (UITab tab : tabs) {
			UITabProxy proxy = new UITabProxy(tab);
			result.add(proxy);
		}
		return result;
	}
	
	public UITabProxy(UITab uiTab) {
		super(uiTab);
		this.uiTab = uiTab;
	}

	@ExternalizedProperty
	public List<LanguageSpecificTextProxy> getLabels() {
		List<LanguageSpecificText> labels = uiTab.getLabels();
		return LanguageSpecificTextProxy.fromList(labels);
	}

}
