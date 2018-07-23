package org.openforis.idm.metamodel.xml.internal.marshal;

import static org.openforis.idm.metamodel.xml.IdmlConstants.DEPRECATED;
import static org.openforis.idm.metamodel.xml.IdmlConstants.SINCE;

import java.io.IOException;

import javax.xml.namespace.QName;

import org.openforis.idm.metamodel.Annotatable;
import org.openforis.idm.metamodel.VersionableSurveyObject;

/**
 * 
 * @author G. Miceli
 *
 * @param <T>
 * @param <P>
 */
public abstract class VersionableSurveyObjectXS<T extends VersionableSurveyObject, P>
		extends XmlSerializerSupport<T, P> {

	protected VersionableSurveyObjectXS(String tag) {
		super(tag);
	}

	@Override
	protected void attributes(T o) throws IOException {
		attribute(SINCE, o.getSinceVersionName());
		attribute(DEPRECATED, o.getDeprecatedVersionName());
		if ( o instanceof Annotatable ) {
			Annotatable a= (Annotatable) o;
			for (QName qname : a.getAnnotationNames()) {
				String value = a.getAnnotation(qname);
				attribute(qname.getNamespaceURI(), qname.getLocalPart(), value);
			}
		}
	}
}
