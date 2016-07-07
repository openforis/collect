package org.openforis.idm.metamodel.xml;

import org.openforis.idm.metamodel.ApplicationOptions;
import org.openforis.idm.metamodel.Survey;

/**
 * @author G. Miceli
 */
public interface ApplicationOptionsBinder<T extends ApplicationOptions> {
	/**
	 * 
	 * @param survey the current survey being unmarshalled
	 * @param type the type of application options that will be unmarshalled
	 * @param body the text or well-formed XML contained in the body of the 
	 * IDML options element 
	 * @return the converted {@link ApplicationOptions} object
	 */
	T unmarshal(Survey survey, String type, String body);

	/**
	 * 
	 * @param options a valid {@link ApplicationOptions} object  
	 * @return the text or well-formed XML to be placed inside the IDML 
	 * options element
	 */
	String marshal(T options, String defaultLanguage);
	
	
	/**
	 * 
	 * @param type
	 * @return true if the namespaceUri specified is supported by this marshallar
	 */
	boolean isSupported(String type);
}
