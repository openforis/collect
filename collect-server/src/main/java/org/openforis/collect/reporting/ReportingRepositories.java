package org.openforis.collect.reporting;

import java.util.List;

import org.openforis.collect.event.RecordStep;
import org.openforis.collect.event.RecordTransaction;
import org.openforis.concurrency.ProgressListener;

public interface ReportingRepositories {

	void createRepositories(String surveyName, String preferredLanguage, ProgressListener progressListener);

	void createRepository(String surveyName, RecordStep recordStep, String preferredLanguage, ProgressListener progressListener);

	void updateRepositories(String surveyName, String preferredLanguage, ProgressListener progressListener);

	void deleteRepositories(String surveyName);

	void process(RecordTransaction recordTransaction);

	List<String> getRepositoryPaths(String surveyName);

	String getRepositoryPath(String surveyName, RecordStep recordStep);
	
	ReportingRepositoryInfo getInfo(String surveyName);

}