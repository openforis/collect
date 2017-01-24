/**
 * 
 */
package org.openforis.collect.model.proxy;

import java.util.ArrayList;
import java.util.List;

import org.openforis.collect.Proxy;
import org.openforis.collect.model.User;
import org.openforis.collect.model.UserRole;

/**
 * 
 * @author S. Ricci
 *
 */
public class UserProxy implements Proxy {

	private Boolean enabled;
	private Integer id;
	private String name;
	private String password;
	private String rawPassword;
	private List<String> roles;

	public UserProxy(User user) {
		super();
		this.enabled = user.getEnabled();
		this.id = user.getId();
		this.name = user.getUsername();
		this.roles = user.getRoleCodes();
		//password is not initialized, so the client will not know its value
	}

	public static List<UserProxy> fromList(List<User> users) {
		List<UserProxy> result = new ArrayList<UserProxy>();
		if ( users != null ) {
			for (User user : users) {
				UserProxy proxy = new UserProxy(user);
				result.add(proxy);
			}
		}
		return result;
	}
	
	public User toUser() {
		User user = new User();
		user.setEnabled(enabled);
		user.setId(id);
		user.setUsername(name);
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

	public Boolean getEnabled() {
		return enabled;
	}

	public void setEnabled(Boolean enabled) {
		this.enabled = enabled;
	}
	
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
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
