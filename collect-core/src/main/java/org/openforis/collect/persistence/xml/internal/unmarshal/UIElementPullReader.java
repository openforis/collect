package org.openforis.collect.persistence.xml.internal.unmarshal;

import org.openforis.collect.model.CollectSurvey;
import org.openforis.idm.metamodel.xml.internal.unmarshal.XmlPullReader;

/**
 * Base class for every UI option xml pull reader class
 */
public abstract class UIElementPullReader extends XmlPullReader {

	protected UIElementPullReader(String namespace, String tagName) {
		super(namespace, tagName);
	}

	public UIElementPullReader(String namespace, String tagName, Integer maxCount) {
		super(namespace, tagName, maxCount);
	}
	
	public CollectSurvey getSurvey() {
		XmlPullReader currentPR = this;
		while ( ! (currentPR instanceof UITabSetPR) ) {
			currentPR = currentPR.getParentReader();
		}
		return ((UITabSetPR) currentPR).getSurvey();
	}

}
