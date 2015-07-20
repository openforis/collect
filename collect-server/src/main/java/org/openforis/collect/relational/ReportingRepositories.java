package org.openforis.collect.relational;

import java.util.List;

import org.openforis.collect.event.RecordStep;
import org.openforis.collect.event.RecordTransaction;

public interface ReportingRepositories {

	void createRepositories(String surveyName);

	void createRepository(String surveyName, RecordStep recordStep)
			throws CollectRdbException;

	void process(RecordTransaction recordTransaction);

	void deleteRepositories(String surveyName);

	List<String> getRepositoryPaths(String surveyName);

}