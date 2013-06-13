package org.openforis.collect.manager;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openforis.collect.model.CollectSurvey;
import org.openforis.idm.metamodel.CodeList;
import org.openforis.idm.metamodel.CodeListLevel;

/**
 * 
 * Migrates a survey to the latest version of Collect.
 * 
 * 
 * @author S. Ricci
 *
 */
public class SurveyMigrator {
	
	protected static final String INTERNAL_NAME_REGEX = "[a-z][a-z0-9_]*";
	
	public void migrate(CollectSurvey survey) {
		if ( isMigrationNeeded(survey) ) { 
			fixCodeListHierarchyLevelNames(survey);
		}
	}
	
	protected boolean isMigrationNeeded(CollectSurvey survey) {
		//TODO
		return true;
	}

	protected void fixCodeListHierarchyLevelNames(CollectSurvey survey) {
		List<CodeList> codeLists = survey.getCodeLists();
		for (CodeList list : codeLists) {
			List<CodeListLevel> hierarchy = list.getHierarchy();
			for (CodeListLevel level : hierarchy) {
				String name = level.getName();
				Pattern pattern = Pattern.compile(INTERNAL_NAME_REGEX);  
				Matcher matcher = pattern.matcher(name);
				if ( ! matcher.matches() ) {
					String fixedName = fixInternalName(name);
					level.setName(fixedName);
				}
			}
		}
	}

	protected String fixInternalName(String name) {
		name = name.toLowerCase();
		name = name.replaceAll(" ", "_");
		return name;
	}

}
