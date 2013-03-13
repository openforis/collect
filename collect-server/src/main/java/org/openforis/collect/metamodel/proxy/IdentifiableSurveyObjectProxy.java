/**
 * 
 */
package org.openforis.collect.metamodel.proxy;

import org.granite.messaging.amf.io.util.externalizer.annotation.ExternalizedProperty;
import org.openforis.collect.Proxy;
import org.openforis.idm.metamodel.IdentifiableSurveyObject;

/**
 * @author S. Ricci
 *
 */
public class IdentifiableSurveyObjectProxy implements Proxy {

	private transient IdentifiableSurveyObject identifiableSurveyObject;

	protected IdentifiableSurveyObjectProxy(
			IdentifiableSurveyObject identifiableSurveyObject) {
		super();
		this.identifiableSurveyObject = identifiableSurveyObject;
	}

	@ExternalizedProperty
	public int getId() {
		return identifiableSurveyObject.getId();
	}
	
}
