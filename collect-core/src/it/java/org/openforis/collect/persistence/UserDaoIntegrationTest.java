package org.openforis.collect.persistence;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openforis.collect.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author S. Ricci
 */
@RunWith( SpringJUnit4ClassRunner.class )
@ContextConfiguration( locations = {"classpath:test-context.xml"} )
@TransactionConfiguration(defaultRollback=true)
@Transactional
public class UserDaoIntegrationTest {
	//private final Log log = LogFactory.getLog(ConfigurationDaoIntegrationTest.class);
	
	@Autowired
	protected UserDao userDao;
	
	@Test
	public void testCRUD() throws Exception  {
		// SAVE NEW
		User user = new User();
		user.setEnabled(Boolean.TRUE);
		user.setName("user1");
		user.setPassword("pass1");
		user.addRole("role1");
		user.addRole("role2");
		
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
