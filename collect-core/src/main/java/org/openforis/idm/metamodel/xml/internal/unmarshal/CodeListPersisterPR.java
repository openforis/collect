package org.openforis.idm.metamodel.xml.internal.unmarshal;

import org.openforis.idm.metamodel.Survey;


/**
 * 
 * @author S. Ricci
 *
 */
public class CodeListPersisterPR extends CodeListPR {

	@Override
	protected CodeListItemsPR createCodeListItemsPR() {
		return new CodeListItemsPersisterPR();
	}
	
	public Survey getSurvey() {
		XmlPullReader parent = getParentReader();
		while ( parent != null ) {
			if ( parent instanceof SurveyCodeListImporterPR ) {
				return ((SurveyCodeListImporterPR) parent).getSurvey();
			}
			parent = parent.getParentReader();
		}
		return null;
	}
	
	@Override
	protected void addCodeListToSurvey() {
		//do nothing, avoid adding the code list 2 times to the survey
	}

}
