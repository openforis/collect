/**
 * 
 */
package org.openforis.collect.manager;

import org.openforis.collect.persistence.UserDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author M. Togna
 *
 */
public class UserManager {

	@Autowired
	private UserDao userDao;
	
	@Transactional
	public int getUserId(String username){
		return userDao.getUserId(username);
	}
	
}
