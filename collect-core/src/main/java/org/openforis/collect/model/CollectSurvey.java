/**
 * 
 */
package org.openforis.collect.model;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.openforis.idm.metamodel.Survey;
import org.openforis.idm.model.state.ModelDependencies;

/**
 * @author M. Togna
 * 
 */
@XmlRootElement(name = "survey")
public class CollectSurvey extends Survey {
	private static final long serialVersionUID = 1L;

	@XmlTransient
	private ModelDependencies modelDependencies;

	public CollectSurvey() {
		super();
	}

	public ModelDependencies getModelDependencies() {
		return modelDependencies;
	}

	public void setModelDependencies(ModelDependencies modelDependencies) {
		this.modelDependencies = modelDependencies;
		this.modelDependencies.register(this);
	}

}
