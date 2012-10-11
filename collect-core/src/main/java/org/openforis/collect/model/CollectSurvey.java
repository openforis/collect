/**
 * 
 */
package org.openforis.collect.model;

import javax.xml.bind.annotation.XmlRootElement;

import org.openforis.collect.metamodel.ui.UIOptions;
import org.openforis.collect.metamodel.ui.UIOptionsConstants;
import org.openforis.idm.metamodel.ApplicationOptions;
import org.openforis.idm.metamodel.Survey;
import org.openforis.idm.metamodel.SurveyContext;

/**
 * @author M. Togna
 * @author S. Ricci
 * 
 */
@XmlRootElement(name = "survey")
public class CollectSurvey extends Survey {
	
	protected CollectSurvey(SurveyContext surveyContext) {
		super(surveyContext);
	}

	private static final long serialVersionUID = 1L;

	public UIOptions getUIOptions() {
		ApplicationOptions applicationOptions = getApplicationOptions(UIOptionsConstants.UI_TYPE);
		return (UIOptions) applicationOptions;
	}

	public void setUIOptions(UIOptions conf) {
		setApplicationOptions(conf);
	}
	
}
