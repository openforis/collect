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
public class User {

	private Boolean enabled;
	private Integer id;
	private List<String> roles;
	private String name;
	private String password;
	
	public User() {
		roles = new ArrayList<String>();
	}

	public User(String name) {
		this();
		this.name = name;
	}

	public User(Integer id, String name) {
		this(name);
		this.id = id;
	}

	public void addRole(String role) {
		roles.add(role);
	}
	
	public boolean hasRole(String role) {
		return roles.contains(role);
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
	public List<String> getRoles() {
		return Collections.unmodifiableList(roles);
	}

	public String getName() {
		return name;
	}

	public void setRoles(List<String> roles) {
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