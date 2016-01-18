package org.openforis.collect.persistence;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.openforis.collect.CollectTest;
import org.openforis.collect.model.Logo;
import org.openforis.collect.model.LogoPosition;
import org.springframework.beans.factory.annotation.Autowired;

public class LogoDaoIntegrationTest extends CollectTest {

	private static final int TEST_LOGO_ID = 100;
	
	@Autowired
	protected LogoDao logoDao;

	@Test
	public void testCRUD() throws Exception  {
		// SAVE NEW
		Logo logo = createTestLogo(TEST_LOGO_ID, LogoPosition.TOP_RIGHT, "test_logo.jpg");
		byte[] savedData = logo.getImage();
		logoDao.insert(logo);

		// RELOAD
		Logo reloadedLogo = logoDao.loadById(TEST_LOGO_ID);
		assertNotNull(reloadedLogo);
		byte[] reloadedData = reloadedLogo.getImage();
		assertTrue(Arrays.equals(savedData, reloadedData));
	}

	private Logo createTestLogo(int id, LogoPosition p, String fileName) throws IOException {
		URL imageUrl = ClassLoader.getSystemResource(fileName);
		InputStream is = imageUrl.openStream();
		byte[] data = IOUtils.toByteArray(is);
		Logo logo = new Logo(id, p, data, null);
		return logo;
	}
}
