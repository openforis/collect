package org.openforis.idm.metamodel;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;

import org.apache.commons.lang3.StringUtils;

/**
 * @author G. Miceli
 * @author M. Togna
 */
public abstract class SurveyObject implements Serializable, Annotatable{

	private static final long serialVersionUID = 1L;

	private Survey survey;
	
	private Map<QName,String> annotations;

	protected SurveyObject(Survey survey) {
		if ( survey == null ) {
			throw new NullPointerException("survey");
		}
		this.survey = survey;
	}

	public final Survey getSurvey() {
		return survey;
	}

	public final Schema getSchema() {
		return survey == null ? null : survey.getSchema();
	}
	
	public String getAnnotation(QName qname) {
		return annotations == null ? null : annotations.get(qname);
	}

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
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((annotations == null) ? 0 : annotations.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
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