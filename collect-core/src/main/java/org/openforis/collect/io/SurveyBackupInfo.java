package org.openforis.collect.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.Properties;

import org.openforis.collect.Collect;
import org.openforis.collect.utils.Dates;

/**
 * 
 * @author S. Ricci
 *
 */
public class SurveyBackupInfo {

	private static final String DATE_PROP = "date";
	private static final String COLLECT_VERSION_PROP = "collect_version";
	private static final String SURVEY_URI_PROP = "survey_uri";
	
	private String collectVersion;
	private Date date;
	private String surveyUri;
	
	public SurveyBackupInfo(String surveyUri) {
		this.surveyUri = surveyUri;
		this.collectVersion = Collect.getVersion();
		this.date = new Date();
	}
	
	public void store(OutputStream os) throws IOException {
		Properties props = toProperties();
		props.store(os, null);
	}
	
	protected Properties toProperties() {
		Properties props = new Properties();
		props.setProperty(COLLECT_VERSION_PROP, collectVersion);
		props.setProperty(DATE_PROP, Dates.formatDateToXML(date));
		props.setProperty(SURVEY_URI_PROP, surveyUri);
		return props;
	}

	public static SurveyBackupInfo parse(InputStream is) throws IOException {
		Properties props = new Properties();
		props.load(is);
		SurveyBackupInfo info = parse(props);
		return info;
	}
	
	protected static SurveyBackupInfo parse(Properties props) {
		String uri = props.getProperty(SURVEY_URI_PROP);
		SurveyBackupInfo info = new SurveyBackupInfo(uri);
		info.collectVersion = props.getProperty(COLLECT_VERSION_PROP);
		info.date = Dates.parseXMLDate(props.getProperty(DATE_PROP));
		return info;
	}
	
	public Date getDate() {
		return date;
	}
	
	public void setDate(Date date) {
		this.date = date;
	}
	
	public String getSurveyUri() {
		return surveyUri;
	}
	
	public void setSurveyUri(String surveyUri) {
		this.surveyUri = surveyUri;
	}
	
	public String getCollectVersion() {
		return collectVersion;
	}

	public void setCollectVersion(String collectVersion) {
		this.collectVersion = collectVersion;
	}
	
}
