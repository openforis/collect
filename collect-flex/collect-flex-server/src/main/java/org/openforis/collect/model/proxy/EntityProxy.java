/**
 * 
 */
package org.openforis.collect.model.proxy;

import org.openforis.idm.model.Entity;

/**
 * @author M. Togna
 * 
 */
public class EntityProxy implements ModelProxy {

	private transient Entity entity;

	public EntityProxy(Entity entity) {
		this.entity = entity;
	}

}
