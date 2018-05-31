package org.openforis.idm.metamodel;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;

import org.apache.commons.lang3.StringUtils;
import org.openforis.commons.lang.DeepComparable;

/**
 * @author G. Miceli
 * @author M. Togna
 */
public abstract class SurveyObject implements Serializable, Annotatable, DeepComparable {

	private static final long serialVersionUID = 1L;

	private Survey survey;
	
	private Map<QName,String> annotations;

	protected SurveyObject(Survey survey) {
		if ( survey == null ) {
			throw new NullPointerException("survey");
		}
		this.survey = survey;
	}
	
	@SuppressWarnings("unchecked")
	protected SurveyObject(Survey survey, SurveyObject surveyObject) {
		this(survey);
		this.annotations = surveyObject.annotations == null ? null : 
			(Map<QName, String>) ((HashMap<QName, String>) surveyObject.annotations).clone();
	}

	@SuppressWarnings("unchecked")
	public final <S extends Survey> S getSurvey() {
		return (S) survey;
	}
	
	public <S extends Survey> void replaceSurvey(S survey) {
		this.survey = survey;
	}
	
	public final Schema getSchema() {
		return survey == null ? null : survey.getSchema();
	}
	
	@Override
	public String getAnnotation(QName qname) {
		return annotations == null ? null : annotations.get(qname);
	}

	@Override
	public void setAnnotation(QName qname, String value) {
		if ( annotations == null ) {
			annotations = new HashMap<QName, String>();
		}
		if (StringUtils.isNotBlank(value)) {
			annotations.put(qname, value);
		} else {
			annotations.remove(qname);
		}
	}

	public Map<QName, String> getAnnotations() {
		return annotations;
	}
	
	public void setAnnotations(Map<QName, String> annotations) {
		if (annotations == null) {
			this.annotations = null;
		} else {
			this.annotations = new HashMap<QName, String>(annotations);
		}
	}
	
	@Override
	public Set<QName> getAnnotationNames() {
		if ( annotations == null ) {
			return Collections.emptySet();
		} else {
			return Collections.unmodifiableSet(annotations.keySet());
		}
	}

	void detach() {
		this.survey = null;
	}
	
	public boolean isDetached() {
		return survey == null;
	}

	@Override
	public boolean deepEquals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SurveyObject other = (SurveyObject) obj;
		if (annotations == null) {
			if (other.annotations != null)
				return false;
		} else if (!annotations.equals(other.annotations))
			return false;
		return true;
	}
	
}