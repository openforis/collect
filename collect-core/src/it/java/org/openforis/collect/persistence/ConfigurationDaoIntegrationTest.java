package org.openforis.collect.persistence;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Set;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openforis.collect.model.Configuration;
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
public class ConfigurationDaoIntegrationTest {
	//private final Log log = LogFactory.getLog(ConfigurationDaoIntegrationTest.class);
	
	@Autowired
	protected ConfigurationDao configurationDao;
	
	@Test
	public void testCRUD() throws Exception  {
		// SAVE NEW
		Configuration config = new Configuration();
		config.put("property1", "value1");
		config.put("property2", "value2");
		config.put("property3", "value3");
		configurationDao.save(config);
		
		// RELOAD
		Configuration reloaded = configurationDao.load();
		assertNotNull(reloaded);
		
		Set<String> properties = reloaded.getProperties();
		assertEquals(3, properties.size());
		for (String name : properties) {
			String oldValue = config.get(name);
			assertNotNull(oldValue);
			String newValue = reloaded.get(name);
			assertEquals(oldValue, newValue);
		}
	}
	
}
