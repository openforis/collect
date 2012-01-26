/**
 * 
 */
package org.openforis.collect.model.proxy;

import org.openforis.collect.Proxy;
import org.openforis.idm.model.Entity;

/**
 * @author M. Togna
 * 
 */
public class EntityProxy implements Proxy {

	private transient Entity entity;

	public EntityProxy(Entity entity) {
		this.entity = entity;
	}

}
