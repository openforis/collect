package org.openforis.collect.reporting;

import java.util.List;

import org.openforis.collect.event.RecordStep;
import org.openforis.collect.event.RecordTransaction;
import org.openforis.concurrency.ProgressListener;

public interface ReportingRepositories {

	void createRepositories(String surveyName, ProgressListener progressListener);

	void createRepository(String surveyName, RecordStep recordStep, ProgressListener progressListener);

	void updateRepositories(String surveyName, ProgressListener progressListener);

	void deleteRepositories(String surveyName);

	void process(RecordTransaction recordTransaction);

	List<String> getRepositoryPaths(String surveyName);

	String getRepositoryPath(String surveyName, RecordStep recordStep);
	
}