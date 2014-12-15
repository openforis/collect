/**
 * 
 */
package org.openforis.collect.model;

import org.openforis.idm.metamodel.Survey;
import org.openforis.idm.metamodel.validation.LookupProvider;
import org.openforis.idm.model.Coordinate;

/**
 * @author M. Togna
 * 
 */
public class TestLookupProviderImpl implements LookupProvider {

	public Coordinate coordinate;
	public Object samplingPointData;

	@Override
	public Object lookup(Survey survey, String name, String attribute, Object... keys) {
		return coordinate == null ? null : coordinate.toString();
	}
	
	@Override
	public Coordinate lookupSamplingPointCoordinate(Survey survey, String... keys) {
		return coordinate;
	}
	
	@Override
	public Object lookupSamplingPointData(Survey survey, String attribute, String... keys) {
		return samplingPointData;
	}

}