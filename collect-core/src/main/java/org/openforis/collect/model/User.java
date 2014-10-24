package org.openforis.collect.model;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author M. Togna
 * @author S. Ricci
 * 
 */
public class User implements org.openforis.collect.api.User {

	private Boolean enabled;
	private Integer id;
	private List<UserRole> roles;
	private String name;
	private String password;
	
	public User() {
		roles = new ArrayList<UserRole>();
	}

	public User(String name) {
		this();
		this.name = name;
	}

	public User(Integer id, String name) {
		this(name);
		this.id = id;
	}

	@Override
	public String getUsername() {
		return getName();
	}
	
	public void addRole(UserRole role) {
		roles.add(role);
	}
	
	public boolean hasRole(UserRole role) {
		return roles.contains(role);
	}
	
	public boolean hasEffectiveRole(UserRole role) {
		int maxHiearachicalOrder = calculateHighestRoleHierarchicalOrder();
		return role.getHierarchicalOrder() <= maxHiearachicalOrder;
	}

	private int calculateHighestRoleHierarchicalOrder() {
		int max = -1;
		for (UserRole role : roles) {
			max = Math.max(max, role.getHierarchicalOrder());
		}
		return max;
	}
	
	public List<String> getRoleCodes() {
		List<String> codes = new ArrayList<String>(getRoles().size());
		for (UserRole role : getRoles()) {
			codes.add(role.getCode());
		}
		return codes;
	}
	
	public Boolean getEnabled() {
		return enabled;
	}

	public void setEnabled(Boolean enabled) {
		this.enabled = enabled;
	}

	/**
	 * Returns an unmodifiable list of user roles
	 * @return
	 */
	public List<UserRole> getRoles() {
		return Collections.unmodifiableList(roles);
	}

	public String getName() {
		return name;
	}

	public void setRoles(List<UserRole> roles) {
		this.roles = roles;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}
	
	public String getPassword() {
		return password;
	}
	
	public void setPassword(String password) {
		this.password = password;
	}

	@Override
	public String toString() {
		StringWriter sw = new StringWriter();
		sw.append("User: '");
		sw.append(getName());
		sw.append("' ");
		sw.append(" roles:");
		sw.append(getRoles().toString());
		return sw.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((enabled == null) ? 0 : enabled.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((password == null) ? 0 : password.hashCode());
		result = prime * result + ((roles == null) ? 0 : roles.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		User other = (User) obj;
		if (enabled == null) {
			if (other.enabled != null)
				return false;
		} else if (!enabled.equals(other.enabled))
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (password == null) {
			if (other.password != null)
				return false;
		} else if (!password.equals(other.password))
			return false;
		if (roles == null) {
			if (other.roles != null)
				return false;
		} else if (!roles.equals(other.roles))
			return false;
		return true;
	}

}