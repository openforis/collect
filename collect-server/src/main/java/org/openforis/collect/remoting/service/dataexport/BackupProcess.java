package org.openforis.collect.remoting.service.dataexport;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openforis.collect.manager.RecordManager;
import org.openforis.collect.manager.SurveyManager;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.RecordSummarySortField;
import org.openforis.collect.persistence.xml.DataMarshaller;
import org.openforis.collect.remoting.service.dataexport.DataExportState.Format;

/**
 * 
 * @author S. Ricci
 *
 */
public class BackupProcess implements Callable<Void>, DataExportProcess {

	private static Log LOG = LogFactory.getLog(BackupProcess.class);

	private static final String FILE_NAME = "data.zip";

	private RecordManager recordManager;
	private SurveyManager surveyManager;
	private DataMarshaller dataMarshaller;
	
	private File directory;
	private CollectSurvey survey;
	private DataExportState state;
	private int[] stepNumbers;
	private String rootEntityName;
	
	public BackupProcess(SurveyManager surveyManager, RecordManager recordManager, DataMarshaller dataMarshaller, File directory,
			CollectSurvey survey, String rootEntityName, int[] stepNumbers) {
		super();
		this.surveyManager = surveyManager;
		this.recordManager = recordManager;
		this.dataMarshaller = dataMarshaller;
		this.directory = directory;
		this.survey = survey;
		this.rootEntityName = rootEntityName;
		this.stepNumbers = stepNumbers;
		this.state = new DataExportState(Format.XML);
	}

	@Override
	public DataExportState getState() {
		return state;
	}

	@Override
	public void cancel() {
		state.setCancelled(true);
		state.setRunning(false);
	}
	
	@Override
	public boolean isRunning() {
		return state.isRunning();
	}
	
	@Override
	public boolean isComplete() {
		return state.isComplete();
	}
	
	@Override
	public Void call() throws Exception {
		try {
			state.reset();
			List<CollectRecord> recordSummaries = loadAllSummaries();
			if ( recordSummaries != null && stepNumbers != null ) {
				state.setRunning(true);
				String fileName = FILE_NAME;
				File file = new File(directory, fileName);
				if (file.exists()) {
					file.delete();
				}
				FileOutputStream fileOutputStream = new FileOutputStream(file);
				BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream);
				ZipOutputStream zipOutputStream = new ZipOutputStream(bufferedOutputStream);
				backup(zipOutputStream, recordSummaries);
				zipOutputStream.flush();
				zipOutputStream.close();
				if ( ! state.isCancelled() ) {
					state.setComplete(true);
				}
			}
		} catch (Exception e) {
			state.setError(true);
			LOG.error("Error during data export", e);
		} finally {
			state.setRunning(false);
		}
		return null;
	}

	private void backup(ZipOutputStream zipOutputStream, List<CollectRecord> recordSummaries) {
		int total = calculateTotal(recordSummaries);
		state.setTotal(total);
		includeIdml(zipOutputStream);
		for (CollectRecord summary : recordSummaries) {
			if ( ! state.isCancelled() ) {
				int recordStepNumber = summary.getStep().getStepNumber();
				for (int stepNum: stepNumbers) {
					if ( stepNum <= recordStepNumber) {
						backup(zipOutputStream, summary, stepNum);
						state.incrementCount();
					}
				}
			} else {
				state.setRunning(false);
				break;
			}
		}
	}

	private List<CollectRecord> loadAllSummaries() {
		List<CollectRecord> summaries = recordManager.loadSummaries(survey, rootEntityName, 0, Integer.MAX_VALUE, (List<RecordSummarySortField>) null, (String) null);
		return summaries;
	}
	
	private int calculateTotal(List<CollectRecord> recordSummaries) {
		int count = 0;
		for (CollectRecord summary : recordSummaries) {
			int recordStepNumber = summary.getStep().getStepNumber();
			for (int stepNumber: stepNumbers) {
				if ( stepNumber <= recordStepNumber ) {
					count ++;
				}
			}
		}
		return count;
	}
	
	private void includeIdml(ZipOutputStream zipOutputStream) {
		String entryFileName = "idml.xml";
		ZipEntry entry = new ZipEntry(entryFileName);
		try {
			zipOutputStream.putNextEntry(entry);
			surveyManager.marshalSurvey(survey, zipOutputStream);
//			String surveyMarshalled = surveyManager.marshalSurvey(survey);
//			PrintWriter printWriter = new PrintWriter(zipOutputStream);
//			printWriter.write(surveyMarshalled);
			zipOutputStream.closeEntry();
			zipOutputStream.flush();
		} catch (IOException e) {
			String message = "Error while including idml into zip file: " + e.getMessage();
			if (LOG.isErrorEnabled()) {
				LOG.error(message, e);
			}
			throw new RuntimeException(message, e);
		}
	}
	
	private void backup(ZipOutputStream zipOutputStream, CollectRecord summary, int stepNumber) {
		Integer id = summary.getId();
		try {
			CollectRecord record = recordManager.load(survey, id, stepNumber);
			String entryFileName = buildEntryFileName(record, stepNumber);
			ZipEntry entry = new ZipEntry(entryFileName);
			zipOutputStream.putNextEntry(entry);
			OutputStreamWriter writer = new OutputStreamWriter(zipOutputStream);
			dataMarshaller.write(record, writer);
			zipOutputStream.closeEntry();
			zipOutputStream.flush();
		} catch (Exception e) {
			String message = "Error while backing up " + id + " " + e.getMessage();
			if (LOG.isErrorEnabled()) {
				LOG.error(message, e);
			}
			throw new RuntimeException(message, e);
		}
	}
	
	private String buildEntryFileName(CollectRecord record, int stepNumber) {
		return stepNumber + File.separator + record.getId() + ".xml";
	}
	
}
