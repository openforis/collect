/**
 * 
 */
package org.openforis.collect.designer.viewmodel;

import org.zkoss.bind.annotation.Init;


/**
 * @author S. Ricci
 *
 */
public class CollectEarthPreviewPopUpVM extends SurveyBaseVM {

	@Init(superclass=false)
	public void init() {
		super.init();
	}
	
	public String getContentUrl() {
		return "/collectearthpreview.html?surveyId=" + getSurveyId();
	}
	
}
