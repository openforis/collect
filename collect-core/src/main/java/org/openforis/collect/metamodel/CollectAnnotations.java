package org.openforis.collect.metamodel;

import javax.xml.namespace.QName;

import org.apache.commons.lang3.StringUtils;
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
		INCLUDE_IN_DATA_EXPORT(new QName(COLLECT_NAMESPACE_URI, "includeInDataExport"), true);
		
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
			return annotation.getDefaultValue();
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
