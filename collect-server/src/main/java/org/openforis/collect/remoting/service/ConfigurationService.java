/**
 * 
 */
package org.openforis.collect.remoting.service;

import java.io.IOException;
import java.io.InputStream;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openforis.collect.Collect;
import org.openforis.collect.CollectInfo;
import org.openforis.collect.io.data.backup.BackupStorageManager;
import org.openforis.collect.manager.ConfigurationManager;
import org.openforis.collect.manager.RecordFileManager;
import org.openforis.collect.manager.RecordIndexException;
import org.openforis.collect.manager.RecordIndexManager;
import org.openforis.collect.model.Configuration.ConfigurationItem;
import org.openforis.collect.model.proxy.ConfigurationProxy;
import org.openforis.commons.versioning.Version;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author S. Ricci
 */
public class ConfigurationService {

	protected static final Logger LOG = LogManager.getLogger(ConfigurationService.class);
	
	@Autowired
	private ConfigurationManager configurationManager;
	@Autowired
	private RecordFileManager recordFileManager;
	@Autowired
	private BackupStorageManager backupStorageManager;
	@Autowired
	@Qualifier("persistedRecordIndexManager")
	private RecordIndexManager recordIndexManager;
	
	//transient instance variables
	private transient String defaultRecordFileUploadPath;
	private transient String defaultRecordIndexPath;
	private transient String defaultBackupStoragePath;

	public void init() {
		defaultRecordFileUploadPath = recordFileManager.getDefaultStorageDirectory().getAbsolutePath();
		defaultRecordIndexPath = recordIndexManager.getDefaultStorageDirectory().getAbsolutePath();
		defaultBackupStoragePath = backupStorageManager.getDefaultStorageDirectory().getAbsolutePath();
	}
	
	public ConfigurationProxy loadConfiguration() {
		return new ConfigurationProxy(configurationManager.getConfiguration(), 
				defaultRecordFileUploadPath,
				defaultRecordIndexPath,
				defaultBackupStoragePath);
	}
	
	public void updateUploadPath(String uploadPath) {
		configurationManager.updateUploadPath(uploadPath);
		recordFileManager.init();
	}
	
	public void updateIndexPath(String indexPath) throws RecordIndexException {
		configurationManager.updateIndexPath(indexPath);
		boolean initialized = recordIndexManager.init();
		if ( ! initialized ) {
			throw new RuntimeException("Error initializing index path");
		}
	}
	
	public void updateConfigurationItem(String configurationItemName, String value) {
		ConfigurationItem item = ConfigurationItem.valueOf(configurationItemName);
		configurationManager.updateConfigurationItem(item, value);
	}
	
	public CollectInfo loadRemoteCloneInfo() {
		String remoteCloneUrl = configurationManager.getConfiguration().get(ConfigurationItem.REMOTE_CLONE_URL);
		String remoteInfoUrl = remoteCloneUrl + "/info.json";
		try {
			HttpClientBuilder clientBuilder = HttpClientBuilder.create();
			CloseableHttpClient httpClient = clientBuilder.build();
			
			HttpGet request = new HttpGet(remoteInfoUrl);
			request.setConfig(RequestConfig.custom().setConnectTimeout(20000).build());
			HttpResponse response = httpClient.execute(request);
			HttpEntity entity = response.getEntity();

			if (entity != null) {
			    InputStream is = entity.getContent();
			    CollectInfo info = extractCollectInfo(is);
			    return info;
			} else {
				LOG.error("Error getting remote Collect version information: Invalid response");
			}
		} catch (Exception e) {
			LOG.error("Error getting remote Collect version information: " + e.getMessage());
		}
		return null;
	}
	
	public boolean isRemoteCloneValid() {
		CollectInfo remoteCloneInfo = loadRemoteCloneInfo();
		return remoteCloneInfo != null && Collect.VERSION.compareTo(new Version(remoteCloneInfo.getVersion())) >= 0;
	}
	
	private CollectInfo extractCollectInfo(InputStream is) throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		CollectInfo info = mapper.readValue(is, CollectInfo.class);
		return info;
	}
}
