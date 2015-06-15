package org.openforis.collect.remoting;

import java.util.Date;

import org.granite.messaging.amf.io.util.externalizer.annotation.ExternalizedProperty;

/**
 * 
 * @author S. Ricci
 *
 */
public class SurveyBackupInfo {
	
	private Date date;
	private int updatedRecordsSinceBackup;
	
	public SurveyBackupInfo(Date date, int updatedRecordsSinceBackup) {
		super();
		this.date = date;
		this.updatedRecordsSinceBackup = updatedRecordsSinceBackup;
	}
	
	@ExternalizedProperty
	public Date getDate() {
		return date;
	}
	
	@ExternalizedProperty
	public int getUpdatedRecordsSinceBackup() {
		return updatedRecordsSinceBackup;
	}
}