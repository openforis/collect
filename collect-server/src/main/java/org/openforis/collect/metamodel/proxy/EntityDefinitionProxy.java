/**
 * 
 */
package org.openforis.collect.metamodel.proxy;

import java.util.List;

import javax.xml.namespace.QName;

import org.apache.commons.lang3.StringUtils;
import org.granite.messaging.amf.io.util.externalizer.annotation.ExternalizedProperty;
import org.openforis.idm.metamodel.EntityDefinition;

/**
 * @author M. Togna
 * @author S. Ricci
 * 
 */
public class EntityDefinitionProxy extends NodeDefinitionProxy {

	private static final String LAYOUT_FORM = "form";
	private static final String LAYOUT_TABLE = "table";
	private static final QName TAB_DEFINITION_ANNOTATION = new QName("http://www.openforis.org/collect/3.0/ui", "tabDefinition");
	private static final QName COUNT_IN_SUMMARY_LIST_ANNOTATION = new QName("http://www.openforis.org/collect/3.0/collect", "count");
	private static final QName LAYOUT_ANNOTATION = new QName("http://www.openforis.org/collect/3.0/ui", "layout");
	private static final QName SHOW_ROW_NUMBERS_ANNOTATION = new QName("http://www.openforis.org/collect/3.0/ui", "showRowNumbers");

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
		String annotation = entityDefinition.getAnnotation(COUNT_IN_SUMMARY_LIST_ANNOTATION);
		return annotation != null && Boolean.parseBoolean(annotation);
	}

	@ExternalizedProperty
	public boolean isEnumerable() {
		return entityDefinition.isMultiple() && entityDefinition.isEnumerable();
	}

	@ExternalizedProperty
	public String getLayout() {
		String result = entityDefinition.getAnnotation(LAYOUT_ANNOTATION);
		if(StringUtils.isNotBlank(result)) {
			return result;
		} else if(isMultiple() && parent != null) {
			return LAYOUT_TABLE;
		} else if(parent != null) {
			return parent.getLayout();
		} else {
			return LAYOUT_FORM;
		}
	}
	
	@ExternalizedProperty
	public boolean isShowRowNumbers() {
		String showRowNumbersString = entityDefinition.getAnnotation(SHOW_ROW_NUMBERS_ANNOTATION);
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
			String tabDefnName = entityDefinition.getAnnotation(TAB_DEFINITION_ANNOTATION);
			return tabDefnName;
		}
	}

	
}
