package org.openforis.collect.persistence;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Test;
import org.openforis.collect.CollectTest;
import org.openforis.collect.model.User;
import org.openforis.collect.model.UserRole;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author S. Ricci
 */
public class UserDaoIntegrationTest extends CollectTest {
	//private final Log log = LogFactory.getLog(ConfigurationDaoIntegrationTest.class);
	
	@Autowired
	protected UserDao userDao;
	
	@Test
	public void testCRUD() throws Exception  {
		// SAVE NEW
		User user = new User();
		user.setEnabled(Boolean.TRUE);
		user.setUsername("user1");
		user.setPassword("pass1");
		user.addRole(UserRole.ENTRY);
		user.addRole(UserRole.CLEANSING);
		
		userDao.insert(user);
		Integer id = user.getId();
		
		User reloaded = userDao.loadById(id);
		assertNotNull(reloaded);
		
		assertEquals(user, reloaded);
		
		userDao.delete(id);
		
		reloaded = userDao.loadById(id);
		assertNull(reloaded);
	}
	
}
