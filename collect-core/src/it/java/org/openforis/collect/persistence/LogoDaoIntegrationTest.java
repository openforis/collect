package org.openforis.collect.persistence;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openforis.collect.model.Logo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

@RunWith( SpringJUnit4ClassRunner.class )
@ContextConfiguration( locations = {"classpath:test-context.xml"} )
@TransactionConfiguration(defaultRollback=true)
@Transactional
public class LogoDaoIntegrationTest {
	//private final Log log = LogFactory.getLog(LogoDaoIntegrationTest.class);
	
	@Autowired
	protected LogoDao logoDao;
	
	@Test
	public void testCRUD() throws Exception  {
		// SAVE NEW
		Logo logo = createTestLogo(1, "test_logo.jpg");
		logoDao.insert(logo);
		
		byte[] saved = logo.getImage();
		
		// RELOAD
		logo = logoDao.loadById(1);
		byte[] reloaded = logo.getImage();
		boolean equals = Arrays.equals(saved, reloaded);
		
		assertEquals(true, equals);
	}
	
	private Logo createTestLogo(int i, String fileName) throws IOException {
		URL idm = ClassLoader.getSystemResource(fileName);
		InputStream is = null;
		ByteArrayOutputStream bos = null;
		try {
			is = idm.openStream();
	        bos = new ByteArrayOutputStream();
	        byte[] buf = new byte[1024];
	        for (int readNum; (readNum = is.read(buf)) != -1;) {
	            bos.write(buf, 0, readNum); //no doubt here is 0
	        }
	        byte[] bytes = bos.toByteArray();
	        Logo logo = new Logo(i, bytes);
	        return logo;
		} catch (IOException e) {
			try {
				if(is != null) {
					is.close();
				}
				if(bos != null) {
					bos.close();
				}
			} catch (IOException e1) {
			}
			throw new IOException("Error creating test logo");
		}
	}
}
