package org.openforis.collect.reporting;

import java.io.File;
import java.util.Locale;

import org.apache.commons.io.FileUtils;
import org.openforis.collect.CollectInternalInfo;
import org.openforis.collect.event.RecordStep;
import org.openforis.collect.manager.BaseStorageManager;
import org.openforis.collect.model.Configuration.ConfigurationItem;
import org.openforis.collect.relational.CollectLocalRDBStorageManager;
import org.openforis.collect.remoting.service.CollectInfoService;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 
 * @author S. Ricci
 *
 */
@Component
public class SaikuDatasourceStorageManager extends BaseStorageManager implements InitializingBean {
	
	private static final long serialVersionUID = 1L;
	
	private static final String DATASOURCE_SUFFIX = "_ds";
	private static final String DATASOURCES_PATH = "WEB-INF" + File.separator + "classes" + File.separator + "saiku-datasources";

	private static final String DATASOURCE_CONTENT_FORMAT = 
			  "type=OLAP\r\n"
			+ "name=%s_%s\r\n"
			+ "driver=mondrian.olap4j.MondrianOlap4jDriver\r\n"
			+ "location=jdbc:mondrian:Jdbc=%s;Catalog=%s;JdbcDrivers=org.sqlite.JDBC\r\n"
			+ "username=dbuser\r\n"
			+ "password=password";
	
	@Autowired
	private CollectInfoService infoService;
	@Autowired
	private SaikuConfiguration saikuConfiguration;
	@Autowired
	private MondrianSchemaStorageManager mondrianSchemaStorageManager;
	@Autowired
	private CollectLocalRDBStorageManager rdbStorageManager;
	
	@Override
	public void afterPropertiesSet() throws Exception {
		init();
	}

	public void init() {
		CollectInternalInfo info = infoService.getInternalInfo();
		this.setDefaultRootStoragePath(info.getWebappsPath());
		this.setDefaultSubFolder(saikuConfiguration.getContextPath());
		super.initStorageDirectory(ConfigurationItem.SAIKU_BASE_DIR, false);
	}

	public boolean isSaikuAvailable() {
		return storageDirectory.exists();
	}
	
	public File getDatasourcesDirectory() {
		return new File(storageDirectory, DATASOURCES_PATH);
	}
	
	private File getDatasourceFile(String name, RecordStep recordStep) {
		return new File(getDatasourcesDirectory(), name + "_" + recordStep.name().toLowerCase(Locale.ENGLISH) + DATASOURCE_SUFFIX);
	}
	
	public void writeDatasourceFile(String surveyName, RecordStep recordStep) {
		try {
			File rdbFile = rdbStorageManager.getRDBFile(surveyName, recordStep);
			File mondrianSchemaFile = mondrianSchemaStorageManager.getSchemaFile(surveyName);
			String content = String.format(DATASOURCE_CONTENT_FORMAT
					, surveyName
					, recordStep.name().toLowerCase(Locale.ENGLISH)
					, "jdbc:sqlite:" + formatPath(rdbFile.getAbsolutePath())
					, formatPath(mondrianSchemaFile.getAbsolutePath())
			);
			File file = getDatasourceFile(surveyName, recordStep);
			FileUtils.write(file, content, "UTF-8");
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public boolean deleteDatasourceFile(String surveyName, RecordStep recordStep) {
		File file = getDatasourceFile(surveyName, recordStep);
		return file.delete();
	}
	
	/**
	 * Formats a file path to be compatible with Saiku datasource file path format
	 */
	private static String formatPath(String path) {
		return path.replaceAll("\\\\", "/");
	}
	
}
