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
		result = prime * result + ((id == null) ? 0 : id.hashCode());
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
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}
	
	

}