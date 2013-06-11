package org.openforis.collect.persistence;

import org.openforis.collect.model.CollectSurvey;
import org.openforis.idm.metamodel.CodeList;
import org.openforis.idm.metamodel.ExternalCodeListItem;
import org.openforis.idm.metamodel.ExternalCodeListPersister;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * @author S. Ricci
 *
 */
public class DatabaseExternalCodeListPersister implements ExternalCodeListPersister {
	
	@Autowired
	private CodeListItemDao codeListItemDao;

	@Override
	public void save(ExternalCodeListItem item) {
		if ( item.getSystemId() == null ) {
			codeListItemDao.insert(item);
		} else {
			codeListItemDao.update(item);
		}
	}

	@Override
	public void delete(ExternalCodeListItem item) {
		codeListItemDao.delete(item.getId());
	}

	@Override
	public void deleteAll(CodeList list) {
		CollectSurvey survey = (CollectSurvey) list.getSurvey();
		codeListItemDao.deleteBySurvey(survey.isWork(), survey.getId());
	}

}
