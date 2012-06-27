/**
 * 
 */
package org.openforis.collect.model;

import javax.xml.bind.annotation.XmlRootElement;

import org.openforis.idm.metamodel.Survey;

/**
 * @author M. Togna
 * 
 */
@XmlRootElement(name = "survey", namespace="http://www.openforis.org/idml/3.0")
public class CollectSurvey extends Survey {
	private static final long serialVersionUID = 1L;

	public CollectSurvey() {
		super();
	}

}
