package org.openforis.collect.model;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * @author M. Togna
 * 
 */
public class User {

	private int id;
	private List<String> roles;
	private String name;

	public User(int id, String name) {
		this.id = id;
		this.name = name;
	}

	public void addAuthority(String authority) {
		this.getRoles().add(authority);
	}

	public List<String> getRoles() {
		if (this.roles == null) {
			this.roles = new ArrayList<String>();
		}
		return this.roles;
	}

	public String getName() {
		return this.name;
	}

	public void setRoles(List<String> authorities) {
		this.roles = authorities;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
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
	
}