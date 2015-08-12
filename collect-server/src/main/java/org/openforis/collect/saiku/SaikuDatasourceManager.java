package org.openforis.collect.saiku;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import javax.jcr.RepositoryException;

import org.openforis.collect.event.RecordStep;
import org.openforis.collect.manager.SurveyManager;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.relational.RDBReportingRepositories;
import org.openforis.collect.reporting.MondrianSchemaStorageManager;
import org.saiku.datasources.connection.RepositoryFile;
import org.saiku.datasources.datasource.SaikuDatasource;
import org.saiku.service.datasource.RepositoryDatasourceManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * @author S. Ricci
 *
 */
public class SaikuDatasourceManager extends RepositoryDatasourceManager {

	private static final String SQLITE_JDBC_DRIVER = "org.sqlite.JDBC";
	private static final String MONDRIAN_OLAP4J_DRIVER = "mondrian.olap4j.MondrianOlap4jDriver";
	private static final String DATASOURCE_PATH = "/datasources/";
	@Autowired
	private SurveyManager surveyManager;
	@Autowired
	private MondrianSchemaStorageManager mondrianSchemaStorageManager;
	@Autowired
	private RDBReportingRepositories rdbReportingRepositories;
	
	private Map<DatasourceKey, SaikuDatasource> datasourceById = new HashMap<DatasourceKey, SaikuDatasource>();

	@Override
	public void load() {
		datasourceById.clear();
		List<CollectSurvey> surveys = surveyManager.getAll();
		for (CollectSurvey survey : surveys) {
			String surveyName = survey.getName();
			for (RecordStep recordStep : RecordStep.values()) {
				SaikuDatasource ds = createDatasource(surveyName, recordStep);
				datasourceById.put(new DatasourceKey(surveyName, recordStep), ds);
			}
		}
	}

	private SaikuDatasource createDatasource(String surveyName,
			RecordStep recordStep) {
		String id = surveyName + "_" + recordStep.name();
		String repositoryPath = rdbReportingRepositories.getRepositoryPath(surveyName, recordStep);
		String repositoryJdbcUrl = "jdbc:sqlite:" + repositoryPath;
		String jdbcUrl = String.format("jdbc:mondrian:Jdbc=%s;Catalog=mondrian://%s%s.xml;JdbcDrivers=%s", repositoryJdbcUrl, DATASOURCE_PATH, id, SQLITE_JDBC_DRIVER);
		Properties props = new Properties();
		props.put("id", id);
		props.put("driver", MONDRIAN_OLAP4J_DRIVER);
		props.put("location", jdbcUrl);
		props.put("username", "");
		props.put("password", "");
		props.put("path", "");
		return new SaikuDatasource(id, SaikuDatasource.Type.OLAP, props);
	}

	@Override
	public void unload() {
	}

	@Override
	public RepositoryFile getFile(String fileName) {
		try {
			String internalFileData = getInternalFileData(fileName);
			return internalFileData == null ? null: new RepositoryFile(fileName, null, internalFileData.getBytes());
		} catch (RepositoryException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public String getInternalFileData(String fileName) throws RepositoryException {
		if (fileName.startsWith(DATASOURCE_PATH) && fileName.endsWith(".xml")) {
			String datasourceId = fileName.substring(DATASOURCE_PATH.length(), fileName.length() - 4);
			DatasourceKey datasourceKey = DatasourceKey.fromString(datasourceId);
			String surveyName = datasourceKey.surveyName;
			return mondrianSchemaStorageManager.readSchemaFile(surveyName);
		} else {
			return null;
		}
	}

	@Override
	public void createUser(String username) {
		//DO NOTHING
	}
	
	@Override
	public Map<String, SaikuDatasource> getDatasources() {
		Map<String, SaikuDatasource> result = new HashMap<String, SaikuDatasource>();
		for (Entry<DatasourceKey, SaikuDatasource> entry : datasourceById.entrySet()) {
			result.put(entry.getKey().toString(), entry.getValue());
		}
		return result;
	}
	
	@Override
	public SaikuDatasource getDatasource(String id) {
		return datasourceById.get(DatasourceKey.fromString(id));
	}
	
	private static class DatasourceKey {
		String surveyName;
		RecordStep recordStep;
		
		public DatasourceKey(String surveyName, RecordStep recordStep) {
			super();
			this.surveyName = surveyName;
			this.recordStep = recordStep;
		}
		
		static DatasourceKey fromString(String name) {
			RecordStep[] values = RecordStep.values();
			for (RecordStep recordStep : values) {
				String suffix = "_" + recordStep;
				if (name.endsWith(suffix)) {
					String surveyName = name.substring(0, name.length() - suffix.length());
					return new DatasourceKey(surveyName, recordStep);
				}
			}
			throw new IllegalArgumentException("Invalid DatasourceKey name: " + name);
		}
		
		@Override
		public String toString() {
			return surveyName + "_" + recordStep;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result
					+ ((recordStep == null) ? 0 : recordStep.hashCode());
			result = prime * result
					+ ((surveyName == null) ? 0 : surveyName.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			DatasourceKey other = (DatasourceKey) obj;
			if (recordStep != other.recordStep)
				return false;
			if (surveyName == null) {
				if (other.surveyName != null)
					return false;
			} else if (!surveyName.equals(other.surveyName))
				return false;
			return true;
		}

	}
}
