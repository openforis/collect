/**
 * 
 */
package org.openforis.collect.metamodel.proxy;

import java.util.ArrayList;
import java.util.List;

import org.granite.messaging.amf.io.util.externalizer.annotation.ExternalizedProperty;
import org.openforis.collect.Proxy;
import org.openforis.idm.metamodel.ModelVersion;

/**
 * @author M. Togna
 * 
 */
public class ModelVersionProxy implements Proxy {

	private transient ModelVersion version;

	public ModelVersionProxy(ModelVersion version) {
		super();
		this.version = version;
	}

	static List<ModelVersionProxy> fromList(List<ModelVersion> list) {
		List<ModelVersionProxy> proxies = new ArrayList<ModelVersionProxy>();
		if (list != null) {
			for (ModelVersion v : list) {
				proxies.add(new ModelVersionProxy(v));
			}
		}
		return proxies;
	}

	@ExternalizedProperty
	public String getName() {
		return version.getName();
	}

	@ExternalizedProperty
	public List<LanguageSpecificTextProxy> getLabels() {
		return LanguageSpecificTextProxy.fromList(version.getLabels());
	}

	@ExternalizedProperty
	public List<LanguageSpecificTextProxy> getDescriptions() {
		return LanguageSpecificTextProxy.fromList(version.getDescriptions());
	}

	@ExternalizedProperty
	public String getDate() {
		return version.getDate();
	}

}
