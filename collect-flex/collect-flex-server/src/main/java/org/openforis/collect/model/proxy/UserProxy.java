/**
 * 
 */
package org.openforis.collect.model.proxy;

import java.util.List;

import org.granite.messaging.amf.io.util.externalizer.annotation.ExternalizedProperty;
import org.openforis.collect.Proxy;
import org.openforis.collect.model.User;

/**
 * @author S. Ricci
 *
 */
public class UserProxy implements Proxy {

	private transient User user;

	public UserProxy(User user) {
		super();
		this.user = user;
	}

	@ExternalizedProperty
	public String getName() {
		return user.getName();
	}

	@ExternalizedProperty
	public int getId() {
		return user.getId();
	}

	@ExternalizedProperty
	public List<String> getRoles() {
		return user.getRoles();
	}
	
}
