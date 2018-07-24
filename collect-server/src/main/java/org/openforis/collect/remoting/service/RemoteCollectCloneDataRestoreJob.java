package org.openforis.collect.remoting.service;


import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openforis.collect.io.data.backup.BackupStorageManager;
import org.openforis.collect.manager.ConfigurationManager;
import org.openforis.collect.model.Configuration.ConfigurationItem;
import org.openforis.collect.web.controller.DataRestoreController.RemoteDataRestoreResponse;
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
		
	private static final Logger LOG = LogManager.getLogger(RemoteCollectCloneDataRestoreJob.class);

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
		private static final int BACKUP_SEND_TIMEOUT_MILLIS = BACKUP_SEND_TIMEOUT_MINS * 60 * 1000;
		private HttpUriRequest request;
		
		@Override
		protected void createInternalVariables() throws Throwable {
			super.createInternalVariables();
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
			
			String postUrl = remoteCollectCloneUrl + String.format("/surveys/%s/data/restore-remotely.json", surveyName);
			HttpPost post = new HttpPost(postUrl);
			RequestConfig config = RequestConfig.custom().setConnectTimeout(BACKUP_SEND_TIMEOUT_MILLIS).build();
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
		private static final int REQUEST_TIMEOUT_MILLIS = REQUEST_TIMEOUT_MINS * 60 * 1000;
		private HttpRequestBase jobStatusRequest;
		
		@Override
		protected long countTotalItems() {
			return 100;
		}
		
		@Override
		protected void createInternalVariables() throws Throwable {
			super.createInternalVariables();
			jobStatusRequest = createRequest();
		}
		
		private HttpRequestBase createRequest() {
			String remoteCollectCloneUrl = configurationManager.getConfiguration().get(ConfigurationItem.REMOTE_CLONE_URL);
//				String restoreKey = configurationManager.getConfiguration().get(ConfigurationItem.REMOTE_RESTORE_KEY);
			String url = remoteCollectCloneUrl + String.format("/surveys/data/restore/jobs/%s/status.json", remoteJobId);
			HttpGet request = new HttpGet(url);
			request.setConfig(RequestConfig.custom().setConnectTimeout(REQUEST_TIMEOUT_MILLIS).build());
			return request;
		}
		
		private HttpRequestBase createRemoteJobAbortRequest() {
			String remoteCollectCloneUrl = configurationManager.getConfiguration().get(ConfigurationItem.REMOTE_CLONE_URL);
			String url = remoteCollectCloneUrl + String.format("/surveys/data/restore/jobs/%s/abort.json", remoteJobId);
			HttpGet request = new HttpGet(url);
			request.setConfig(RequestConfig.custom().setConnectTimeout(REQUEST_TIMEOUT_MILLIS).build());
			return request;
		}
		
		@Override
		protected void execute() throws Throwable {
			while (isRunning()) {
				updateRemoteJobStatus();
				if (isRunning()) {
					Thread.sleep(UPDATE_PERIOD);
				}
			}
		}

		private void updateRemoteJobStatus() {
			RemoteDataRestoreResponse response = fetchDataRestoreStatus();
			handleResponse(response);
		}

		private void handleResponse(RemoteDataRestoreResponse response) {
			if (response == null || response.isStatusError()) {
				String errorMessage = response == null ? "Error fetching data restore status" : response.getErrorMessage();
				errorOccurred(errorMessage);
			} else {
				switch (response.getJobStatus()) {
				case PENDING:
					break;
				case RUNNING:
			    	setProcessedItems(response.getJobProgress());
			    	break;
				case FAILED:
					setErrorMessage(response.getJobErrorMessage());
					break;
				case ABORTED:
					changeStatus(Status.ABORTED);
					break;
				case COMPLETED:
					setProcessedItems(100);
					changeStatus(Status.COMPLETED);
					break;
				}
			}
		}
		
		@Override
		public void abort() {
			super.abort();
			if (jobStatusRequest != null) {
				jobStatusRequest.abort();
			}
			abortRemoteJob();
		}

		private RemoteDataRestoreResponse fetchDataRestoreStatus() {
			return executeRequest(this.jobStatusRequest);
		}

		private RemoteDataRestoreResponse executeRequest(HttpRequestBase request) {
			try {
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

		private void abortRemoteJob() {
			HttpRequestBase request = createRemoteJobAbortRequest();
			RemoteDataRestoreResponse response = executeRequest(request);
			handleResponse(response);
		}
		
		private void errorOccurred(String errorMessage) {
			changeStatus(Status.FAILED);
			setErrorMessage(errorMessage);
		}
		
	}
	
	private RemoteDataRestoreResponse extractRestoreResponse(InputStream is) throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		RemoteDataRestoreResponse response = mapper.readValue(is, RemoteDataRestoreResponse.class);
		return response;
	}
	
}