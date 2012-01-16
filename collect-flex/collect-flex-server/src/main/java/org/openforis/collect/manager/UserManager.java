/**
 * 
 */
package org.openforis.collect.manager;

import org.openforis.collect.persistence.UserDAO;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author M. Togna
 *
 */
public class UserManager {

	@Autowired
	private UserDAO userDAO;
	
	public int getUserId(String username){
		return userDAO.getUserId(username);
	}
	
}
