package org.openforis.collect.metamodel;

import static org.openforis.collect.metamodel.ui.UIOptionsConstants.UI_NAMESPACE_URI;

import javax.xml.namespace.QName;

import org.apache.commons.lang3.StringUtils;
import org.openforis.collect.metamodel.ui.UIOptionsConstants;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.idm.metamodel.CalculatedAttributeDefinition;
import org.openforis.idm.metamodel.NodeDefinition;

/**
 * 
 * @author S. Ricci
 *
 */
public class CollectAnnotations {

	public static String COLLECT_NAMESPACE_URI = "http://www.openforis.org/collect/3.0/collect";
	public static String COLLECT_PREFIX = "collect";

	public enum Annotation {
		//collect namespace
		INCLUDE_IN_DATA_EXPORT(new QName(COLLECT_NAMESPACE_URI, "includeInDataExport"), true),
		
		//ui namespace
		TAB_SET(new QName(UI_NAMESPACE_URI, UIOptionsConstants.TAB_SET_NAME)),
		TAB_NAME(new QName(UI_NAMESPACE_URI, UIOptionsConstants.TAB)),
		LAYOUT(new QName(UI_NAMESPACE_URI, UIOptionsConstants.LAYOUT)),
		DIRECTION(new QName(UI_NAMESPACE_URI, UIOptionsConstants.DIRECTION)),
		COUNT_IN_SUMMARY_LIST(new QName(UI_NAMESPACE_URI, UIOptionsConstants.COUNT)),
		SHOW_ROW_NUMBERS(new QName(UI_NAMESPACE_URI, UIOptionsConstants.SHOW_ROW_NUMBERS)),
		AUTOCOMPLETE(new QName(UI_NAMESPACE_URI, UIOptionsConstants.AUTOCOMPLETE)),
		FIELDS_ORDER(new QName(UI_NAMESPACE_URI, UIOptionsConstants.FIELDS_ORDER)),
		VISIBLE_FIELDS(new QName(UI_NAMESPACE_URI, UIOptionsConstants.VISIBLE_FIELDS)),
		SHOW_ALLOWED_VALUES_PREVIEW(new QName(UI_NAMESPACE_URI, UIOptionsConstants.SHOW_ALLOWED_VALUES_PREVIEW)),
		SHOW_IN_UI(new QName(UI_NAMESPACE_URI, UIOptionsConstants.SHOW_IN_UI), false);
		
		private QName qName;
		private Object defaultValue;

		private Annotation(QName qname) {
			this.qName = qname;
		}

		private Annotation(QName qname, Object defaultValue) {
			this(qname);
			this.defaultValue = defaultValue;
		}
		
		public QName getQName() {
			return qName;
		}

		@SuppressWarnings("unchecked")
		public <T extends Object> T getDefaultValue() {
			return (T) defaultValue;
		}
	}
	
	private CollectSurvey survey;
	
	public CollectAnnotations(CollectSurvey survey) {
		super();
		this.survey = survey;
	}
	
	public boolean isIncludedInDataExport(CalculatedAttributeDefinition defn) {
		return getAnnotationBooleanValue(defn, Annotation.INCLUDE_IN_DATA_EXPORT);
	}
	
	public void setIncludeInDataExport(CalculatedAttributeDefinition defn, boolean value) {
		setAnnotationValue(defn, Annotation.INCLUDE_IN_DATA_EXPORT, value);
	}
	
	public CollectSurvey getSurvey() {
		return survey;
	}
	
	private boolean getAnnotationBooleanValue(NodeDefinition defn, Annotation annotation) {
		String annotationValue = defn.getAnnotation(annotation.getQName());
		if ( StringUtils.isBlank(annotationValue) ) {
			Boolean defaultValue = annotation.getDefaultValue();
			return defaultValue.booleanValue();
		} else {
			return Boolean.valueOf(annotationValue);
		}
	}

	private void setAnnotationValue(NodeDefinition defn, Annotation annotation, boolean value) {
		String annotationValue;
		if ( annotation.getDefaultValue().equals(value) ) {
			annotationValue = null;
		} else {
			annotationValue = Boolean.toString(value);
		}
		defn.setAnnotation(annotation.getQName(), annotationValue);
	}
	
}
