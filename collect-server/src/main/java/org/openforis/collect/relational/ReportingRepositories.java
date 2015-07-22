package org.openforis.collect.relational;

import java.util.List;

import org.openforis.collect.event.RecordStep;
import org.openforis.collect.event.RecordTransaction;

public interface ReportingRepositories {

	void createRepositories(String surveyName);

	void createRepository(String surveyName, RecordStep recordStep);

	void updateRepositories(String surveyName);

	void deleteRepositories(String surveyName);

	void process(RecordTransaction recordTransaction);

	List<String> getRepositoryPaths(String surveyName);

}