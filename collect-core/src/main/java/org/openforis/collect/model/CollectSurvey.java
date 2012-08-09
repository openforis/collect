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
@XmlRootElement(name = "survey")
public class CollectSurvey extends Survey {
	private static final long serialVersionUID = 1L;

	private boolean published;
	
	public CollectSurvey() {
		super();
	}

	public boolean isPublished() {
		return published;
	}

	public void setPublished(boolean published) {
		this.published = published;
	}


}
