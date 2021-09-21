package org.openforis.collect.persistence;

import java.sql.Timestamp;

import org.junit.Assert;
import org.junit.Test;
import org.openforis.collect.CollectTest;
import org.openforis.collect.manager.UserManager;
import org.openforis.collect.model.User;
import org.openforis.collect.model.UserGroup;
import org.openforis.collect.model.UserGroup.Visibility;
import org.springframework.beans.factory.annotation.Autowired;

public class UserGroupDaoIntegrationTest extends CollectTest {

	@Autowired
	private UserGroupDao dao;
	@Autowired
	private UserManager userManager;
	
	@Test
	public void testInsert() {
		User admin = userManager.loadAdminUser();
		
		UserGroup group = new UserGroup();
		group.setCreatedByUser(admin);
		group.setCreationDate(new Timestamp(System.currentTimeMillis()));
		group.setDescription("Test description");
		group.setEnabled(true);
		group.setLabel("Test Group");
		group.setName("test");
		group.setSystemDefined(false);
		group.setVisibility(Visibility.PUBLIC);
		
		dao.insert(group);
		
		Assert.assertNotNull(group.getId());
	}
}
