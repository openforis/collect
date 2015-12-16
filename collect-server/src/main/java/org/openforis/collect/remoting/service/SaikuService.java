package org.openforis.collect.remoting.service;

import javax.servlet.http.HttpServletRequest;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.granite.context.GraniteContext;
import org.granite.messaging.webapp.HttpGraniteContext;
import org.openforis.collect.concurrency.CollectJobManager;
import org.openforis.collect.concurrency.SurveyLockingJob;
import org.openforis.collect.manager.SurveyManager;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.reporting.ReportingRepositories;
import org.openforis.collect.reporting.SaikuConfiguration;
import org.openforis.concurrency.DetailedProgressListener;
import org.openforis.concurrency.Progress;
import org.openforis.concurrency.Task;
import org.openforis.concurrency.proxy.JobProxy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 
 * @author S. Ricci
 *
 */
@Component
public class SaikuService {

	@Autowired
	private ReportingRepositories reportingRepositories;
	@Autowired
	private SurveyManager surveyManager;
	@Autowired
	private CollectJobManager jobManager;
	
	public boolean isSaikuAvailable() {
		String url = getSaikuUrl();
		return testUrl(url);
	}
	
	public JobProxy generateRdb(final String surveyName) {
		SurveyLockingJob job = new SurveyLockingJob() {
			protected void buildTasks() throws Throwable {
				addTask(new Task() {
					protected void execute() throws Throwable {
						reportingRepositories.createRepositories(surveyName, new DetailedProgressListener() {
							public void progressMade() {}

							public void progressMade(Progress progress) {
								setItemsProcessed(progress.getProcessedItems());
								setTotalItems(progress.getTotalItems());
							}
						});
					}
				});
			}
		};
		CollectSurvey survey = surveyManager.get(surveyName);
		job.setSurvey(survey);
		jobManager.startSurveyJob(job);
		return new JobProxy(job);
	}

	private String getSaikuUrl() {
		HttpGraniteContext graniteContext = (HttpGraniteContext) GraniteContext.getCurrentInstance();
		HttpServletRequest request = graniteContext.getRequest();
		String protocol = "http";
		String url = String.format("%s://%s:%s/%s", protocol, request.getLocalAddr(), request.getLocalPort(), SaikuConfiguration.getInstance().getContextPath());
		return url;
	}

	private boolean testUrl(String url) {
		try {
			HttpClientBuilder cb = HttpClientBuilder.create();
			CloseableHttpClient httpClient = cb.build();
			HttpHead req = new HttpHead(url);
			CloseableHttpResponse resp = httpClient.execute(req);
			int statusCode = resp.getStatusLine().getStatusCode();
			return statusCode == 200;
		} catch (Exception e) {
			return false;
		}
	}

}
