/**
 * 
 */
package org.openforis.collect.metamodel.proxy;

import java.util.ArrayList;
import java.util.List;

import org.granite.messaging.amf.io.util.externalizer.annotation.ExternalizedProperty;
import org.openforis.collect.Proxy;
import org.openforis.idm.metamodel.SpatialReferenceSystem;

/**
 * @author M. Togna
 * 
 */
public class SpatialReferenceSystemProxy implements Proxy {

	private transient SpatialReferenceSystem spatialReferenceSystem;

	public SpatialReferenceSystemProxy(SpatialReferenceSystem spatialReferenceSystem) {
		super();
		this.spatialReferenceSystem = spatialReferenceSystem;
	}

	static List<SpatialReferenceSystemProxy> fromList(List<SpatialReferenceSystem> list) {
		List<SpatialReferenceSystemProxy> proxies = new ArrayList<SpatialReferenceSystemProxy>();
		if (list != null) {
			for (SpatialReferenceSystem srs : list) {
				proxies.add(new SpatialReferenceSystemProxy(srs));
			}
		}
		return proxies;
	}

	@ExternalizedProperty
	public String getId() {
		return spatialReferenceSystem.getId();
	}

	@ExternalizedProperty
	public List<LanguageSpecificTextProxy> getLabels() {
		return LanguageSpecificTextProxy.fromList(spatialReferenceSystem.getLabels());
	}

	@ExternalizedProperty
	public List<LanguageSpecificTextProxy> getDescriptions() {
		return LanguageSpecificTextProxy.fromList(spatialReferenceSystem.getDescriptions());
	}

	@ExternalizedProperty
	public String getWellKnownText() {
		return spatialReferenceSystem.getWellKnownText();
	}

}
