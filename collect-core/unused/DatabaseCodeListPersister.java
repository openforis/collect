package org.openforis.collect.persistence;

import org.openforis.collect.model.CollectSurvey;
import org.openforis.idm.metamodel.CodeList;
import org.openforis.idm.metamodel.CodeListItemPersister;
import org.openforis.idm.metamodel.PersistedCodeListItem;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * @author S. Ricci
 *
 */
public class DatabaseCodeListPersister implements CodeListItemPersister {
	
	@Autowired
	private CodeListItemDao codeListItemDao;

	@Override
	public void save(PersistedCodeListItem item) {
		if ( item.getSystemId() == null ) {
			codeListItemDao.insert(item);
		} else {
			codeListItemDao.update(item);
		}
	}

	@Override
	public void delete(PersistedCodeListItem item) {
		codeListItemDao.delete(item.getId());
	}

	@Override
	public void deleteAllItems(CodeList list) {
		CollectSurvey survey = (CollectSurvey) list.getSurvey();
		codeListItemDao.deleteBySurvey(survey.getId(), survey.isWork());
	}

}
