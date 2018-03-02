package org.openforis.collect.model.proxy;

import org.openforis.collect.Proxy;
import org.openforis.collect.model.User;

public class BasicUserProxy implements Proxy {

	private Boolean enabled;
	private Integer id;
	private String username;
	
	public BasicUserProxy(User user) {
		super();
		this.enabled = user.getEnabled();
		this.id = user.getId();
		this.username = user.getUsername();
	}
	
	public User toUser() {
		User user = new User();
		user.setEnabled(enabled);
		user.setId(id);
		user.setUsername(username);
		return user;
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
	
	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}
}
