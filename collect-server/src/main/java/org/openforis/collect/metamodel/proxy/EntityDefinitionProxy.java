/**
 * 
 */
package org.openforis.collect.metamodel.proxy;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.granite.messaging.amf.io.util.externalizer.annotation.ExternalizedProperty;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.ui.UIConfiguration;
import org.openforis.collect.model.ui.UIConfiguration.Layout;
import org.openforis.idm.metamodel.EntityDefinition;

/**
 * @author M. Togna
 * @author S. Ricci
 * 
 */
public class EntityDefinitionProxy extends NodeDefinitionProxy {

	private transient EntityDefinition entityDefinition;

	public EntityDefinitionProxy(EntityDefinitionProxy parent, EntityDefinition entityDefinition) {
		super(parent, entityDefinition);
		this.entityDefinition = entityDefinition;
	}

	@ExternalizedProperty
	public List<NodeDefinitionProxy> getChildDefinitions() {
		return NodeDefinitionProxy.fromList(this, entityDefinition.getChildDefinitions());
	}

	@ExternalizedProperty
	public boolean isCountInSummaryList() {
		String annotation = entityDefinition.getAnnotation(UIConfiguration.Annotation.COUNT_IN_SUMMARY_LIST.getQName());
		return annotation != null && Boolean.parseBoolean(annotation);
	}

	@ExternalizedProperty
	public boolean isEnumerable() {
		return entityDefinition.isMultiple() && entityDefinition.isEnumerable();
	}

	@ExternalizedProperty
	public String getLayout() {
		CollectSurvey survey = (CollectSurvey) entityDefinition.getSurvey();
		UIConfiguration uiConfiguration = survey.getUIConfiguration();
		Layout layout = uiConfiguration.getLayout(entityDefinition);
		return layout.name().toLowerCase();
	}
	
	@ExternalizedProperty
	public boolean isShowRowNumbers() {
		String showRowNumbersString = entityDefinition.getAnnotation(UIConfiguration.Annotation.SHOW_ROW_NUMBERS.getQName());
		if ( StringUtils.isNotBlank(showRowNumbersString) ) {
			boolean result = Boolean.parseBoolean(showRowNumbersString);
			return result;
		} else {
			return false;
		}
	}
	
	@ExternalizedProperty
	public String getTabDefinitionName() {
		if ( parent != null ) {
			return parent.getTabDefinitionName();
		} else {
			String tabDefnName = entityDefinition.getAnnotation(UIConfiguration.Annotation.TAB_DEFINITION.getQName());
			return tabDefnName;
		}
	}
	
}
