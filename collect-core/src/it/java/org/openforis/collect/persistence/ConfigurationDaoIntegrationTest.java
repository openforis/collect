package org.openforis.collect.persistence;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Set;

import org.junit.Test;
import org.openforis.collect.CollectTest;
import org.openforis.collect.model.Configuration;
import org.openforis.collect.model.Configuration.ConfigurationItem;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author S. Ricci
 */
public class ConfigurationDaoIntegrationTest extends CollectTest {
	//private final Logger log = Logger.getLogger(ConfigurationDaoIntegrationTest.class);
	
	@Autowired
	protected ConfigurationDao configurationDao;
	
	@Test
	public void testCRUD() throws Exception  {
		// SAVE NEW
		Configuration config = new Configuration();
		config.setUploadPath("/home/test/uploadPathTest");
		config.setIndexPath("/home/test/indexPathTest");
		configurationDao.save(config);
		
		// RELOAD
		Configuration reloaded = configurationDao.load();
		assertNotNull(reloaded);
		
		Set<ConfigurationItem> items = reloaded.getProperties();
		assertEquals(2, items.size());
		for (ConfigurationItem item : items) {
			String oldValue = config.get(item);
			assertNotNull(oldValue);
			String newValue = reloaded.get(item);
			assertEquals(oldValue, newValue);
		}
	}
	
}
