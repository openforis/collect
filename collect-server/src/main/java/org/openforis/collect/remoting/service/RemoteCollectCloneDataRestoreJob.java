package org.openforis.collect.remoting.service;


import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.openforis.collect.io.data.backup.BackupStorageManager;
import org.openforis.collect.manager.ConfigurationManager;
import org.openforis.collect.model.Configuration.ConfigurationItem;
import org.openforis.collect.web.controller.RestoreController.RemoteDataRestoreResponse;
import org.openforis.concurrency.Job;
import org.openforis.concurrency.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 
 * @author S. Ricci
 *
 */
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE) 
public class RemoteCollectCloneDataRestoreJob extends Job {
		
		private static Log LOG = LogFactory.getLog(RemoteCollectCloneDataRestoreJob.class);

		@Autowired
		private BackupStorageManager backupStorageManager;
		@Autowired
		private ConfigurationManager configurationManager;
		
		private String surveyName;
		private String remoteJobId;
		
		@Override
		protected void buildTasks() throws Throwable {
			addTask(new BackupSendTask());
			addTask(new RestoreStatusUpdateTask());
		}
		
		public String getSurveyName() {
			return surveyName;
		}
		
		public void setSurveyName(String surveyName) {
			this.surveyName = surveyName;
		}
		
		private class BackupSendTask extends Task {

			private static final int BACKUP_SEND_TIMEOUT_MINS = 30;
			private HttpUriRequest request;
			
			@Override
			protected void initInternal() throws Throwable {
				super.initInternal();
				request = createRequest();
			}
			
			@Override
			protected void execute() throws Throwable {
				//Execute and get the response.
				HttpClientBuilder clientBuilder = HttpClientBuilder.create();
				CloseableHttpClient httpClient = null;
				try {
					httpClient = clientBuilder.build();
					HttpResponse response = httpClient.execute(request);
					HttpEntity entity = response.getEntity();
					
					if (entity == null) {
						changeStatus(Status.FAILED);
						setErrorMessage("Invalid response");
					} else {
					    InputStream is = entity.getContent();
					    RemoteDataRestoreResponse restoreResponse = extractRestoreResponse(is);
					    if (restoreResponse.isStatusOk()) {
					    	remoteJobId = restoreResponse.getJobId();
					    } else {
					    	changeStatus(Status.FAILED);
					    	setErrorMessage(restoreResponse.getErrorMessage());
					    }
					}
				} finally {
					IOUtils.closeQuietly(httpClient);
				}
			}

			private HttpPost createRequest() {
				String remoteCollectCloneUrl = configurationManager.getConfiguration().get(ConfigurationItem.REMOTE_CLONE_URL);
				String restoreKey = configurationManager.getConfiguration().get(ConfigurationItem.REMOTE_RESTORE_KEY);
				File lastBackupFile = backupStorageManager.getLastBackupFile(surveyName);
				
				String postUrl = remoteCollectCloneUrl + "/survey-data/restore-remotely.json";
				HttpPost post = new HttpPost(postUrl);
				RequestConfig config = RequestConfig.custom().setConnectTimeout(BACKUP_SEND_TIMEOUT_MINS * 60 * 1000).build();
				post.setConfig(config);
				// Request parameters and other properties.
				MultipartEntityBuilder multipartEntityBuilder = MultipartEntityBuilder.create();
				multipartEntityBuilder.addBinaryBody("fileData", lastBackupFile);
				multipartEntityBuilder.addPart("name",  new StringBody(lastBackupFile.getName(), ContentType.TEXT_PLAIN));
				multipartEntityBuilder.addPart("surveyName", new StringBody(surveyName, ContentType.TEXT_PLAIN));
				multipartEntityBuilder.addPart("restoreKey", new StringBody(restoreKey, ContentType.TEXT_PLAIN));
			    post.setEntity(multipartEntityBuilder.build());
				return post;
			}

			@Override
			public void abort() {
				super.abort();
				if (request != null) {
					request.abort();
				}
			}
		}
		
		private class RestoreStatusUpdateTask extends Task {
			
			private static final long UPDATE_PERIOD = 5000;
			private static final int REQUEST_TIMEOUT_MINS = 2;
			private Timer timer;
			private HttpRequestBase request;
			
			@Override
			protected long countTotalItems() {
				return 100;
			}
			
			@Override
			protected void initInternal() throws Throwable {
				super.initInternal();
				request = createRequest();
			}
			
			private HttpRequestBase createRequest() {
				String remoteCollectCloneUrl = configurationManager.getConfiguration().get(ConfigurationItem.REMOTE_CLONE_URL);
//				String restoreKey = configurationManager.getConfiguration().get(ConfigurationItem.REMOTE_RESTORE_KEY);
				String url = remoteCollectCloneUrl + String.format("/survey-data/restore-jobs/%s/status.json", remoteJobId);
				HttpGet request = new HttpGet(url);
				return request;
			}
			
			@Override
			protected void execute() throws Throwable {
				timer = new Timer();
				timer.schedule(new TimerTask() {
					@Override
					public void run() {
						RemoteDataRestoreResponse dataRestoreStatusResponse = fetchDataRestoreStatus();
						if (dataRestoreStatusResponse == null || dataRestoreStatusResponse.isStatusError()) {
							String errorMessage = dataRestoreStatusResponse == null ? "Error fetching data restore status" : dataRestoreStatusResponse.getErrorMessage();
							errorOccurred(errorMessage);
						} else {
							switch (dataRestoreStatusResponse.getJobStatus()) {
							case PENDING:
								break;
							case RUNNING:
						    	setItemsProcessed(dataRestoreStatusResponse.getJobProgress());
						    	break;
							case FAILED:
								setErrorMessage(dataRestoreStatusResponse.getJobErrorMessage());
								timer.cancel();
								break;
							case ABORTED:
								changeStatus(Status.ABORTED);
								timer.cancel();
								break;
							case COMPLETED:
								setItemsProcessed(100);
								changeStatus(Status.COMPLETED);
								timer.cancel();
								break;
							}
						}
					}
				}, 0, UPDATE_PERIOD);
			}
			
			@Override
			public void abort() {
				super.abort();
				if (request != null) {
					request.abort();
				}
			}

			private RemoteDataRestoreResponse fetchDataRestoreStatus() {
				try {
					RequestConfig config = RequestConfig.custom().setConnectTimeout(REQUEST_TIMEOUT_MINS * 60 * 1000).build();
					request.setConfig(config);
					
					//Execute and get the response.
					HttpClientBuilder clientBuilder = HttpClientBuilder.create();
					CloseableHttpClient httpClient = clientBuilder.build();
					HttpResponse response = httpClient.execute(request);
					HttpEntity entity = response.getEntity();
	
					if (entity == null) {
						errorOccurred("Invalid response");
						return null;
					} else {
					    InputStream is = entity.getContent();
					    RemoteDataRestoreResponse restoreResponse = extractRestoreResponse(is);
					    return restoreResponse;
					}
				} catch (Exception e) {
					LOG.error(e);
					return null;
				}
			}

			private void errorOccurred(String errorMessage) {
				changeStatus(Status.FAILED);
				setErrorMessage(errorMessage);
				timer.cancel();
			}
			
		}
		
		private RemoteDataRestoreResponse extractRestoreResponse(InputStream is) throws IOException {
			ObjectMapper mapper = new ObjectMapper();
			mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
			RemoteDataRestoreResponse response = mapper.readValue(is, RemoteDataRestoreResponse.class);
			return response;
		}
		
	}