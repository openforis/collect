package org.openforis.collect.model;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Mino Togna
 * 
 */
public class User {

	/**
	 * @uml.property name="authorities"
	 */
	private List<String> authorities;

	/**
	 * @uml.property name="name"
	 */
	private String name;

	public User(String name) {
		super();
		this.name = name;
	}

	public void addAuthority(String authority) {
		getAuthorities().add(authority);
	}

	/**
	 * Getter of the property <tt>authorities</tt>
	 * 
	 * @return Returns the authorities.
	 * @uml.property name="authorities"
	 */
	public List<String> getAuthorities() {
		if (authorities == null) {
			authorities = new ArrayList<String>();
		}
		return authorities;
	}

	/**
	 * Getter of the property <tt>name</tt>
	 * 
	 * @return Returns the name.
	 * @uml.property name="name"
	 */
	public String getName() {
		return name;
	}

	/**
	 * Setter of the property <tt>authorities</tt>
	 * 
	 * @param authorities
	 *            The authorities to set.
	 * @uml.property name="authorities"
	 */
	public void setAuthorities(List<String> authorities) {
		this.authorities = authorities;
	}

	/**
	 * Setter of the property <tt>name</tt>
	 * 
	 * @param name
	 *            The name to set.
	 * @uml.property name="name"
	 */
	public void setName(String name) {
		this.name = name;
	}

}
