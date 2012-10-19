/**
 * 
 */
package org.openforis.collect.metamodel.proxy;

import java.util.List;

import org.granite.messaging.amf.io.util.externalizer.annotation.ExternalizedProperty;
import org.openforis.collect.Proxy;
import org.openforis.collect.metamodel.ui.UIOptions;
import org.openforis.collect.metamodel.ui.UITabSet;

/**
 * @author riccist
 *
 */
public class UIOptionsProxy implements Proxy {
	
	private transient UIOptions uiOptions;

	public UIOptionsProxy(UIOptions uiOptions) {
		super();
		this.uiOptions = uiOptions;
	}

	@ExternalizedProperty
	public List<UITabSetProxy> getTabSets() {
		List<UITabSet> tabSets = uiOptions.getTabSets();
		return UITabSetProxy.fromList(tabSets);
	}
	
	

}
