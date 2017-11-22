/**
 * 
 */
package org.openforis.collect.remoting.service;

import java.util.List;

import org.openforis.collect.manager.CannotDeleteUserException;
import org.openforis.collect.manager.SessionManager;
import org.openforis.collect.manager.UserManager;
import org.openforis.collect.manager.UserPersistenceException;
import org.openforis.collect.model.User;
import org.openforis.collect.model.proxy.UserProxy;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author S. Ricci
 */
public class UserService {

	@Autowired
	private UserManager userManager;
	@Autowired
	private SessionManager sessionManager;

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

	public UserProxy save(UserProxy user) throws UserPersistenceException {
		User u = user.toUser();
		userManager.save(u, sessionManager.getLoggedUser());
		UserProxy proxy = new UserProxy(u);
		return proxy;
	}

	public void delete(int id) throws CannotDeleteUserException {
		userManager.deleteById(id);
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
