package org.openforis.collect.saiku;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.openforis.collect.event.RecordStep;
import org.openforis.collect.manager.SurveyManager;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.relational.RDBReportingRepositories;
import org.saiku.datasources.datasource.SaikuDatasource;
import org.saiku.service.datasource.RepositoryDatasourceManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * @author S. Ricci
 *
 */
public class CollectSaikuDatasourceManager extends RepositoryDatasourceManager {

	@Autowired
	private SurveyManager surveyManager;
	@Autowired
	private SaikuMondrianSchemaProvider mondrianSchemaProvider;
	@Autowired
	private RDBReportingRepositories rdbReportingRepositories;
	
	private Map<String, SaikuDatasource> datasourceById = new HashMap<String, SaikuDatasource>();

	@Override
	public void load() {
		datasourceById.clear();
		List<CollectSurvey> surveys = surveyManager.getAll();
		for (CollectSurvey survey : surveys) {
			String surveyName = survey.getName();
			for (RecordStep recordStep : RecordStep.values()) {
				SaikuDatasource ds = createDatasource(surveyName, recordStep);
				datasourceById.put(ds.getName(), ds);
			}
		}
	}

	private SaikuDatasource createDatasource(String surveyName,
			RecordStep recordStep) {
		String id = surveyName + "_" + recordStep.name();
		String repositoryPath = rdbReportingRepositories.getRepositoryPath(surveyName, recordStep);
		String jdbcUrl = "jdbc:sqlite:" + repositoryPath;
		Properties props = new Properties();
		props.put("driver", "org.sqlite.JDBC");
		props.put("location", jdbcUrl);
		props.put("username", "");
		props.put("password", "");
		props.put("path", "");
		props.put("id", id);
//				if (file.getSecurityenabled() != null) {
//					props.put("security.enabled", file.getSecurityenabled());
//				}
//				if (file.getSecuritytype() != null) {
//					props.put("security.type", file.getSecuritytype());
//				}
//				if (file.getSecuritymapping() != null) {
//					props.put("security.mapping", file.getSecuritymapping());
//				}
//				if (file.getAdvanced() != null) {
//					props.put("advanced", file.getAdvanced());
//				}
		SaikuDatasource.Type t = SaikuDatasource.Type.valueOf("OLAP");
		SaikuDatasource ds = new SaikuDatasource(id, t, props);
		return ds;
	}

	@Override
	public void unload() {
	}

	@Override
	public Map<String, SaikuDatasource> getDatasources() {
		return datasourceById;
	}
	
	@Override
	public SaikuDatasource getDatasource(String id) {
		return datasourceById.get(id);
	}
	
}
