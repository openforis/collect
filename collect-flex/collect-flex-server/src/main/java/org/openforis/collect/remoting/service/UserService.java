/**
 * 
 */
package org.openforis.collect.remoting.service;

import java.util.List;

import org.openforis.collect.manager.UserManager;
import org.openforis.collect.model.User;
import org.openforis.collect.model.proxy.UserProxy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.encoding.Md5PasswordEncoder;

/**
 * @author S. Ricci
 */
public class UserService {

	@Autowired
	private UserManager userManager;

	public UserProxy loadById(int userId) {
		User user = userManager.loadById(userId);
		return getProxy(user);
	}

	public UserProxy loadByUserName(String userName) {
		User user = userManager.loadByUserName(userName);
		return getProxy(user);
	}
	
	public List<UserProxy> loadAll() {
		List<UserProxy> result = null;
		List<User> users = userManager.loadAll();
		if ( users != null ) {
			result = UserProxy.fromList(users);
		}
		return result;
	}

	public UserProxy save(UserProxy user) {
		User u = user.toUser();
		Integer userId = u.getId();
		if ( userId == null ) {
			userManager.insert(u);
		} else {
			User oldUser = userManager.loadById(userId);
			String password = u.getPassword();
			if ( password == null ) {
				//preserve old password
				u.setPassword(oldUser.getPassword());
			} else {
				//hash password
				Md5PasswordEncoder passwordEncoder = new Md5PasswordEncoder();
				passwordEncoder
			}
			userManager.update(u);
		}
		UserProxy proxy = new UserProxy(u);
		return proxy;
	}

	public void delete(int id) {
		userManager.delete(id);
	}

	private UserProxy getProxy(User user) {
		if ( user != null ) {
			UserProxy proxy = new UserProxy(user);
			return proxy;
		} else {
			return null;
		}
	}

}
