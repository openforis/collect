/**
 * 
 */
package org.openforis.idm.metamodel.validation;

import org.openforis.idm.metamodel.Survey;
import org.openforis.idm.model.Coordinate;

/**
 * @author M. Togna
 * @author S. Ricci
 *
 */
public interface LookupProvider {

	Object lookup(Survey survey, String name, String attribute, Object... keys);
	
	Coordinate lookupSamplingPointCoordinate(Survey survey, String... keys);
	
	Object lookupSamplingPointData(Survey survey, String attribute, String... keys);
	
}
