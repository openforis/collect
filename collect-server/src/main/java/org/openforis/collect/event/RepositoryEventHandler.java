package org.openforis.collect.event;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openforis.collect.relational.event.InitializeRDBEvent;
import org.openforis.collect.reporting.ReportingRepositories;
import org.openforis.concurrency.Progress;
import org.openforis.concurrency.ProgressListener;
import org.openforis.rmb.KeepAlive;
import org.openforis.rmb.KeepAliveMessageHandler;

/**
 * 
 * @author D. Wiell
 * @author S. Ricci
 *
 */
public class RepositoryEventHandler implements KeepAliveMessageHandler<Object> {

	private static final Logger LOG = LogManager.getLogger(RepositoryEventHandler.class);
	
	private static final int DEFAULT_MAX_TRY_COUNT = 3;
	private static final long DEFAULT_RETRY_DELAY = 3000;
	
	private ReportingRepositories repositories;
	private Map<String, Boolean> processBlockedBySurvey = new ConcurrentHashMap<String, Boolean>();
	private long retryDelay = DEFAULT_RETRY_DELAY;
	private int maxTryCount = DEFAULT_MAX_TRY_COUNT;

	protected RepositoryEventHandler(ReportingRepositories repositories) {
		this.repositories = repositories;
	}
	
	@Override
	public void handle(Object event, final KeepAlive keepAlive) {
		if (event instanceof SurveyEvent) {
			final SurveyEvent surveyEvent = (SurveyEvent) event;
			String surveyName = surveyEvent.getSurveyName();
			if (isIgnoringSurveyEvents(surveyName)) {
				logEventIgnored(surveyName);
				return;
			}
			boolean succeded = tryToRun(new Runnable() {
				public void run() {
					handleEvent(surveyEvent, keepAlive);
				}
			}, maxTryCount, retryDelay);
			
			if (! succeded) {
				handleFailingEvent(surveyEvent, keepAlive);
			}
		}
	}
	
	private boolean tryToRun(Runnable runnable, int maxTries, long retryDelay) {
		int retryCount = 0;
		while (retryCount < maxTries) {
			try {
				runnable.run();
				return true;
			} catch(Exception e) {
				retryCount ++;
				try {
					Thread.sleep(retryDelay);
				} catch (InterruptedException e1) {
					Thread.currentThread().interrupt();
					throw new RuntimeException("Handler interrupted", e1);
				}
			}
		}
		return false;
	}

	private void handleEvent(SurveyEvent event, final KeepAlive keepAlive) {
		ProgressListener keepAliveListener = createProgressListener(keepAlive);
		String surveyName = event.getSurveyName();
		if (event instanceof RecordTransaction) {
			repositories.process((RecordTransaction) event);
		} else if (event instanceof SurveyCreatedEvent) {
			repositories.createRepositories(surveyName, null, keepAliveListener);
		} else if (event instanceof SurveyUpdatedEvent) {
			repositories.updateRepositories(surveyName, null, keepAliveListener);
		} else if (event instanceof SurveyDeletedEvent) {
			repositories.deleteRepositories(surveyName);
		} else if (event instanceof InitializeRDBEvent) {
			repositories.createRepository(surveyName, ((InitializeRDBEvent) event).getStep(), null, keepAliveListener);
		}
	}

	private ProgressListener createProgressListener(final KeepAlive keepAlive) {
		ProgressListener keepAliveListener = new ProgressListener() {
			public void progressMade(Progress progress) {
				keepAlive.send();
			}
		};
		return keepAliveListener;
	}

	private void handleFailingEvent(SurveyEvent event, KeepAlive keepAlive) {
		String surveyName = ((SurveyEvent) event).getSurveyName();
		if (event instanceof RecordTransaction) {
			try {
				ProgressListener keepAliveListener = createProgressListener(keepAlive);
				repositories.deleteRepositories(surveyName);
				repositories.createRepositories(surveyName, null, keepAliveListener);
				handleEvent(event, keepAlive);
				considerSurveyEvents(surveyName);
			} catch (Exception e) {
				logEventProcessFailed(event, e);
				ignoreSurveyEvents(surveyName);
			}
		} else {
			logEventIgnored(surveyName);
			ignoreSurveyEvents(surveyName);
		}
	}

	private void ignoreSurveyEvents(String surveyName) {
		processBlockedBySurvey.put(surveyName, true);
	}
	
	private void considerSurveyEvents(String surveyName) {
		processBlockedBySurvey.remove(surveyName);
	}
	
	private boolean isIgnoringSurveyEvents(String surveyName) {
		return processBlockedBySurvey.containsKey(surveyName);
	}

	private void logEventIgnored(String surveyName) {
		List<String> rdbFileNames = repositories.getRepositoryPaths(surveyName);
		LOG.error(String.format("Survey event ignored for survey '%s'; reporting repository is in a inconsistent state;"
				+ " delete the reporting repository to force its recreation: %s", 
				surveyName, rdbFileNames));
	}

	private void logEventProcessFailed(SurveyEvent event, Exception e) {
		List<String> rdbFileNames = repositories.getRepositoryPaths(event.getSurveyName());
		LOG.error(String.format("Failed to process survey event %s; reporting repository is in a inconsistent state;"
				+ " delete the reporting repository to force its recreation: %s", 
				event, rdbFileNames), e);
	}

	public void setMaxRetryCount(int maxRetryCount) {
		this.maxTryCount = maxRetryCount;
	}
	
	public void setRetryDelay(long retryDelay) {
		this.retryDelay = retryDelay;
	}

	
}
