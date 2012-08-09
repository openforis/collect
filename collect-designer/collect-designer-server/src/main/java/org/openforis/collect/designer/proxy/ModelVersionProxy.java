/**
 * 
 */
package org.openforis.collect.designer.proxy;

import java.util.List;

import org.openforis.idm.metamodel.LanguageSpecificText;
import org.openforis.idm.metamodel.ModelVersion;

/**
 * @author S. Ricci
 *
 */
public class ModelVersionProxy extends ProxyBase {

	private ModelVersion modelVersion;

	public ModelVersionProxy(ModelVersion modelVersion) {
		super();
		this.modelVersion = modelVersion;
	}

	public int getId() {
		return modelVersion.getId();
	}

	public String getName() {
		return modelVersion.getName();
	}

	public List<LanguageSpecificText> getLabels() {
		return modelVersion.getLabels();
	}

	public List<LanguageSpecificText> getDescriptions() {
		return modelVersion.getDescriptions();
	}

	public String getDate() {
		return modelVersion.getDate();
	}
	
	
	
}
