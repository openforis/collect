package org.openforis.collect.relational.model;

import org.openforis.idm.metamodel.CodeList;
import org.openforis.idm.metamodel.CodeListItem;
import org.openforis.idm.metamodel.CodeListService;
import org.openforis.idm.metamodel.PersistedCodeListItem;
import org.openforis.idm.metamodel.Survey;
import org.openforis.idm.metamodel.SurveyContext;

/**
 * 
 * @author S. Ricci
 *
 */
public class CodeParentKeyColumn extends IdColumn<CodeListItem> {

	CodeParentKeyColumn(String name) {
		super(name, true);
	}

	@Override
	public Object extractValue(CodeListItem source) {
		CodeList list = source.getCodeList();
		if ( list.isExternal() ) {
			throw new UnsupportedOperationException("External code list not supported");
		} else {
			CodeListItem parent;
			if ( list.isEmpty() ) {
				CodeListService codeListService = getCodeListService(list);
				parent = codeListService.loadParentItem((PersistedCodeListItem) source);
			} else {
				parent = source.getParentItem();
			}
			if ( parent == null ) {
				throw new NullPointerException("Parent code item");
			}
			Integer parentId = parent.getId();
			if ( parentId == null ) {
				throw new NullPointerException("Parent code item id");
			}
			return parentId;
		}
	}

	protected CodeListService getCodeListService(CodeList list) {
		Survey survey = list.getSurvey();
		SurveyContext context = survey.getContext();
		CodeListService codeListService = context.getCodeListService();
		return codeListService;
	}
	
}
