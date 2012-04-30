package org.openforis.collect.remoting.service.backup;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openforis.collect.manager.RecordManager;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.CollectRecord.Step;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.User;
import org.openforis.collect.persistence.xml.DataMarshaller;

/**
 * 
 * @author S. Ricci
 *
 */
public class BackupProcess implements Callable<Void> {

	private static Log LOG = LogFactory.getLog(BackupProcess.class);
	
	private RecordManager recordManager;
	private DataMarshaller dataMarshaller;
	
	private File directory;
	private CollectSurvey survey;
	private String rootEntityName;
	private List<CollectRecord> recordSummaries;
	private Step[] steps;
	private User user;
	private int count = -1;
	private int total = -1;
	private boolean active = false;
	private boolean cancelled = false;
	
	public BackupProcess(RecordManager recordManager, DataMarshaller dataMarshaller, File directory, CollectSurvey survey, String rootEntityName) {
		super();
		this.recordManager = recordManager;
		this.dataMarshaller = dataMarshaller;
		this.directory = directory;
		this.survey = survey;
		this.rootEntityName = rootEntityName;
	}

	@Override
	public Void call() throws Exception {
		this.active = Boolean.TRUE;
		this.cancelled = Boolean.FALSE;

		String fileName = buildFileName();
		File file = new File(directory, fileName);
		if (file.exists()) {
			file.delete();
		}
		FileOutputStream fileOutputStream = new FileOutputStream(file);
		BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream);
		ZipOutputStream zipOutputStream = new ZipOutputStream(bufferedOutputStream);
		count = 0;
		if ( recordSummaries != null && steps != null ) {
			total = calculateTotal();
			for (CollectRecord summary : recordSummaries) {
				if ( ! cancelled ) {
					int recordStepNumber = summary.getStep().getStepNumber();
					for (Step step: steps) {
						int stepNumber = step.getStepNumber();
						if ( stepNumber <= recordStepNumber) {
							backup(summary, step, zipOutputStream);
							count++;
						}
					}
				} else {
					finish();
				}
			}
		}
		zipOutputStream.flush();
		zipOutputStream.close();

		finish();
		return null;
	}
	
	private int calculateTotal() {
		int count = 0;
		for (CollectRecord summary : recordSummaries) {
			int recordStepNumber = summary.getStep().getStepNumber();
			for (Step step: steps) {
				int stepNumber = step.getStepNumber();
				if ( stepNumber <= recordStepNumber ) {
					count ++;
				}
			}
		}
		return count;
	}
	
	private synchronized void backup(CollectRecord summary, Step step, ZipOutputStream zipOutputStream) {
		Integer id = summary.getId();
		try {
			int stepNumber = step.getStepNumber();
			CollectRecord record = recordManager.load(survey, id, stepNumber);
			String entryFileName = buildEntryFileName(record, stepNumber);
			ZipEntry entry = new ZipEntry(entryFileName);
			zipOutputStream.putNextEntry(entry);
			OutputStreamWriter writer = new OutputStreamWriter(zipOutputStream);
			dataMarshaller.write(record, writer);
			zipOutputStream.closeEntry();
			zipOutputStream.flush();
		} catch (Exception e) {
			if (LOG.isErrorEnabled()) {
				LOG.error("Error while backing up " + id, e);
			}
			throw new RuntimeException("Error while backing up " + id, e);
		}
	}
	
	public void cancel() throws Exception {
		this.cancelled = true;
		this.recordSummaries = null;
		this.count = -1;

		File file = new File(directory, survey + "_" + user);
		if (file.exists()) {
			file.delete();
		}
		finish();
	}
	
	private String buildEntryFileName(CollectRecord record, int stepNumber) {
		return stepNumber + File.separator + record.getId() + ".xml";
	}
	
	private String buildFileName() {
		String surveyName = survey.getName();
		String fileName = surveyName +  "_" + rootEntityName + "_" + user.getName() + ".zip";
		return fileName;
	}

	private void finish() {
		this.active = false;
	}
	
	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(this.survey.getId()).toHashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		BackupProcess other = (BackupProcess) obj;
		if (survey == null) {
			if (other.survey != null)
				return false;
		} else if (!this.survey.getId().equals(other.survey.getId()))
			return false;
		return true;
	}

	public String getRootEntityName() {
		return rootEntityName;
	}

	public void setRootEntityName(String rootEntityName) {
		this.rootEntityName = rootEntityName;
	}

	public boolean isActive() {
		return active;
	}

	public boolean isCancelled() {
		return cancelled;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public int getTotal() {
		return total;
	}

	public int getCount() {
		return count;
	}

	public RecordManager getRecordManager() {
		return recordManager;
	}

	public void setRecordManager(RecordManager recordManager) {
		this.recordManager = recordManager;
	}

	public DataMarshaller getDataMarshaller() {
		return dataMarshaller;
	}

	public void setDataMarshaller(DataMarshaller dataMarshaller) {
		this.dataMarshaller = dataMarshaller;
	}

	public List<CollectRecord> getRecordSummaries() {
		return recordSummaries;
	}

	public void setRecordSummaries(List<CollectRecord> recordSummaries) {
		this.recordSummaries = recordSummaries;
	}

	public Step[] getSteps() {
		return steps;
	}

	public void setSteps(Step[] steps) {
		this.steps = steps;
	}
	
	
}
