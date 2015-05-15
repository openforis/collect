/**
 * 
 */
package org.openforis.collect.metamodel.proxy;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.granite.messaging.amf.io.util.externalizer.annotation.ExternalizedProperty;
import org.openforis.idm.metamodel.ModelVersion;

/**
 * @author M. Togna
 * @author S. Ricci
 * 
 */
public class ModelVersionProxy extends IdentifiableSurveyObjectProxy {

	private transient ModelVersion version;
	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

	public ModelVersionProxy(ModelVersion version) {
		super(version);
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
		Date date = version.getDate();
		return date == null ? null: DATE_FORMAT.format(date);
	}

}
