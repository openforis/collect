/**
 * 
 */
package org.openforis.collect.model.proxy;

import java.util.ArrayList;
import java.util.List;

import org.openforis.collect.model.User;
import org.openforis.collect.model.UserRole;

/**
 * 
 * @author S. Ricci
 *
 */
public class UserProxy extends BasicUserProxy {

	private String password;
	private String rawPassword;
	private List<String> roles;

	public UserProxy(User user) {
		super(user);
		this.roles = user.getRoleCodes();
		//password is not initialized, so the client will not know its value
	}

	public User toUser() {
		User user = super.toUser();
		user.setPassword(password);
		user.setRawPassword(rawPassword);
		user.setRoles(getRolesFromCodes(roles));
		return user;
	}
	
	private List<UserRole> getRolesFromCodes(List<String> codes) {
		List<UserRole> roles = new ArrayList<UserRole>();
		for (String code : codes) {
			UserRole role = UserRole.fromCode(code);
			roles.add(role);
		}
		return roles;
	}

	public List<String> getRoles() {
		return roles;
	}

	public void setRoles(List<String> roles) {
		this.roles = roles;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getRawPassword() {
		return rawPassword;
	}
	
	public void setRawPassword(String rawPassword) {
		this.rawPassword = rawPassword;
	}
}
