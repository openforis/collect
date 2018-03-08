package org.openforis.collect.manager;

import static org.openforis.collect.Collect.VERSION;

import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openforis.collect.metamodel.CollectAnnotations;
import org.openforis.collect.metamodel.SurveyTarget;
import org.openforis.collect.metamodel.ui.UIConfiguration;
import org.openforis.collect.metamodel.ui.UIModelObject;
import org.openforis.collect.metamodel.ui.UITable;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.commons.versioning.Version;
import org.openforis.idm.metamodel.CodeList;
import org.openforis.idm.metamodel.CodeListLevel;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.NodeDefinitionVisitor;

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
	
//	private static final String MIGRATION_NEEDED_COLLECT_VERSION = "3.4.0";
	
	private static final Version ENUMERATE_VERSION = new Version("3.20.22");
	
	public void migrate(final CollectSurvey survey) {
		fixCodeListHierarchyLevelNames(survey);
		
		if (survey.getCollectVersion().compareTo(ENUMERATE_VERSION) < 0) {
			survey.getSchema().traverse(new NodeDefinitionVisitor() {
				public void visit(NodeDefinition defn) {
					if (defn instanceof EntityDefinition && defn.isMultiple()) {
						EntityDefinition entityDefn = (EntityDefinition) defn;
						UIConfiguration uiConfig = survey.getUIConfiguration();
						if (uiConfig != null) {
							UIModelObject uiModelObject = uiConfig.getModelObjectByNodeDefinitionId(entityDefn.getId());
							CollectAnnotations annotations = survey.getAnnotations();
							if (survey.getTarget() == SurveyTarget.COLLECT_EARTH
									|| uiModelObject == null || uiModelObject instanceof UITable) {
								if (defn.getMinCountExpression() != null) {
									annotations.setAutoGenerateMinItems(entityDefn, true);
								}
								if (entityDefn.isEnumerable()) {
									entityDefn.setEnumerate(true);
								}
							}
						}
					}
				}
			});
		}
		survey.setCollectVersion(VERSION);
	}
	
	public boolean isMigrationNeeded(CollectSurvey survey) {
		//TODO check this
//		return survey.getCollectVersion().compareTo(new Version(MIGRATION_NEEDED_COLLECT_VERSION)) <= 0;
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
		name = name.toLowerCase(Locale.ENGLISH);
		name = name.replaceAll(" ", "_");
		return name;
	}

}
