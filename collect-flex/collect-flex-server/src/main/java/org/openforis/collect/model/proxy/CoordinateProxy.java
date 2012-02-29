/**
 * 
 */
package org.openforis.collect.model.proxy;

import org.granite.messaging.amf.io.util.externalizer.annotation.ExternalizedProperty;
import org.openforis.collect.Proxy;
import org.openforis.idm.model.Coordinate;

/**
 * @author S. Ricci
 *
 */
public class CoordinateProxy implements Proxy {

	private transient Coordinate coordinate;

	public CoordinateProxy(Coordinate coordinate) {
		super();
		this.coordinate = coordinate;
	}

	@ExternalizedProperty
	public String getSrsId() {
		return coordinate.getSrsId();
	}
	
	@ExternalizedProperty
	public Double getX() {
		return coordinate.getX();
	}

	@ExternalizedProperty
	public Double getY() {
		return coordinate.getY();
	}
	
}
