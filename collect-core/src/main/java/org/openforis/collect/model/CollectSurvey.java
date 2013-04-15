/**
 * 
 */
package org.openforis.collect.model;

import java.util.List;

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
public class CollectSurvey extends Survey {
	
	protected CollectSurvey(SurveyContext surveyContext) {
		super(surveyContext);
	}

	private static final long serialVersionUID = 1L;

	public String getDefaultLanguage() {
		List<String> languages = getLanguages();
		if ( languages == null || languages.isEmpty() ) {
			return null;
		} else {
			return languages.get(0);
		}
	}
	
	public UIOptions createUIOptions() {
		return new UIOptions(this);
	}
	
	public UIOptions getUIOptions() {
		ApplicationOptions applicationOptions = getApplicationOptions(UIOptionsConstants.UI_TYPE);
		return (UIOptions) applicationOptions;
	}
	
	@Override
	public void addApplicationOptions(ApplicationOptions options) {
		super.addApplicationOptions(options);
		if ( options instanceof UIOptions ) {
			((UIOptions) options).setSurvey(this);
		}
	}

}
