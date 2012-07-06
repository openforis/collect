/**
 * 
 */
package org.openforis.collect.manager;

import java.util.List;

import org.openforis.collect.model.User;
import org.openforis.collect.persistence.UserDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author M. Togna
 * @author S. Ricci
 */
public class UserManager {

	@Autowired
	private UserDao userDao;
	
	@Transactional
	public int getUserId(String username){
		return userDao.getUserId(username);
	}
	
	@Transactional
	public User loadById(int userId){
		return userDao.loadById(userId);
	}
	
	public User loadByUserName(String userName){
		return userDao.loadByUserName(userName);
	}
	
	public List<User> loadAll() {
		return userDao.loadAll();
	}
	
	@Transactional
	public void update(User user) {
		userDao.update(user);
	}
	
	@Transactional
	public void insert(User user) {
		userDao.insert(user);
	}
	
	@Transactional
	public void delete(int id) {
		userDao.delete(id);
	}
}
