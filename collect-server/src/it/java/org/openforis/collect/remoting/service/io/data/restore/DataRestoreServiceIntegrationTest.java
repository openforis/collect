package org.openforis.collect.remoting.service.io.data.restore;

import java.io.File;
import java.io.IOException;

import org.junit.Ignore;
import org.junit.Test;
import org.openforis.collect.CollectIntegrationTest;
import org.springframework.remoting.rmi.RmiProxyFactoryBean;

/**
 * 
 * @author S. Ricci
 *
 */
public class DataRestoreServiceIntegrationTest extends CollectIntegrationTest {

	@Test
	@Ignore
	public void test() throws IOException {
		RmiProxyFactoryBean factoryBean = new RmiProxyFactoryBean();
		factoryBean.setServiceInterface(DataRestoreService.class);
		factoryBean.setServiceUrl("rmi://localhost:1099/dataRestoreService");
		factoryBean.afterPropertiesSet();
		
		DataRestoreService dataRestoreService = (DataRestoreService) factoryBean.getObject();
		File backupFile = File.createTempFile("test", ".collect-backup");
		dataRestoreService.startSurveyDataRestore("test", backupFile);
	}
	
	
}
