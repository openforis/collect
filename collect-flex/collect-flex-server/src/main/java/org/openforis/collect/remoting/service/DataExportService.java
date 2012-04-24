package org.openforis.collect.remoting.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openforis.collect.manager.RecordManager;
import org.openforis.collect.manager.SessionManager;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.CollectRecord.Step;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.RecordSummarySortField;
import org.openforis.collect.persistence.RecordPersistenceException;
import org.openforis.collect.web.session.DataExportState;
import org.openforis.collect.web.session.SessionState;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.Schema;
import org.openforis.idm.metamodel.Survey;
import org.openforis.idm.model.expression.InvalidExpressionException;
import org.openforis.idm.transform.AutomaticColumnProvider;
import org.openforis.idm.transform.ColumnProvider;
import org.openforis.idm.transform.ColumnProviderChain;
import org.openforis.idm.transform.DataTransformation;
import org.openforis.idm.transform.csv.ModelCsvWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

/**
 * 
 * @author S. Ricci
 *
 */
public class DataExportService {

	private static Log LOG = LogFactory.getLog(DataExportService.class);

	@Autowired
	private SessionManager sessionManager;

	@Autowired
	private RecordManager recordManager;

	@Transactional
	public void export(String rootEntityName, int entityId, int stepNumber) {
		SessionState sessionState = sessionManager.getSessionState();
		DataExportState state = sessionState.getDataExtractionState();
		if ( state == null || state.isError() || state.isCancelled() || ! (state.isExtracting() || state.isCompressing()) ) {
			state = new DataExportState();
			state.setExtracting(true);
			CollectSurvey survey = sessionState.getActiveSurvey();
			try {
				DataTransformation transform = getTransform(survey, rootEntityName, entityId);
				String tempPath = "export" + File.separator + "data.csv";
				extractData(state, tempPath, transform, survey, rootEntityName, Step.valueOf(stepNumber));
				
				state.setCompressing(true);
				String destPath = "export" + File.separator + "data.zip";
				compressFile(tempPath, destPath);
				state.setCompressing(false);
			} catch (Exception e) {
				LOG.error(e);
				state.setError(true);
			}
		} else {
			throw new IllegalStateException("Cannot start another data export.");
		}
	}
	
	public void cancel() {
		SessionState sessionState = sessionManager.getSessionState();
		DataExportState state = sessionState.getDataExtractionState();
		if ( state != null ) {
			state.setCancelled(true);
		}
	}

	private void compressFile(String tempPath, String destPath) throws Exception {
		File destFile = new File(destPath);
		destFile.createNewFile();
		ZipOutputStream out = null;
		FileInputStream in = null;
		try {
			out = new ZipOutputStream(new FileOutputStream(destFile));
			in = new FileInputStream(tempPath);
			ZipEntry zipEntry = new ZipEntry(tempPath);
			out.putNextEntry(zipEntry);
			// Transfer bytes from the file to the ZIP file
			int len;
			// Create a buffer for reading the files
			byte[] buf = new byte[1024];
			while ((len = in.read(buf)) > 0) {
				out.write(buf, 0, len);
			}
			// Complete the entry
			out.closeEntry();
		} catch (Exception e) {
			throw e;
		} finally {
			if ( in != null) {
				in.close();
			}
			// Complete the ZIP file
			if ( out != null ) {
				out.close();
			} 
		}
	}
	
	public DataExportState getState() {
		SessionState sessionState = sessionManager.getSessionState();
		DataExportState state = sessionState.getDataExtractionState();
		return state;
	}
	
	private DataTransformation getTransform(Survey survey, String rootEntityName, Integer entityId) throws InvalidExpressionException {
		Schema schema = survey.getSchema();
		EntityDefinition entityDefn = (EntityDefinition) schema.getById(entityId);
		String axisPath = entityDefn.getPath();
		EntityDefinition rowDefn = (EntityDefinition) schema.getByPath(axisPath);
		
		ColumnProvider provider = new ColumnProviderChain(
				/*
				new PivotExpressionColumnProvider("parent()/parent()", 
						new SingleAttributeColumnProvider("id", "cluster_id"),
						new SingleAttributeColumnProvider("region", "cluster_region"),
						new SingleAttributeColumnProvider("district", "cluster_district")),
				new PivotExpressionColumnProvider("parent()", 
						new SingleAttributeColumnProvider("no","plot_no"),	
						new SingleAttributeColumnProvider("subplot", "subplot")),
				//new TaxonColumnProvider("species"),
				 */
				new AutomaticColumnProvider(rowDefn)
				);
		return new DataTransformation(axisPath, provider);
	}
	
	private void extractData(DataExportState state, String destPath, DataTransformation xform, CollectSurvey survey, String rootEntityName, Step step) throws IOException, InvalidExpressionException, RecordPersistenceException {
		ModelCsvWriter modelWriter = null;
		try {
			File file = new File(destPath);
			file.createNewFile();
			FileWriter writer = new FileWriter(file);

			modelWriter = new ModelCsvWriter(writer, xform);
			modelWriter.printColumnHeadings();
			
			// Cycle over data files
			int total = 0;
			int read = 0;
			@SuppressWarnings("unused")
			int rowsCount = 0;
			@SuppressWarnings("unused")
			long start = System.currentTimeMillis();
			List<CollectRecord> summaries = recordManager.loadSummaries(survey, rootEntityName, 0, Integer.MAX_VALUE, (List<RecordSummarySortField>) null, (String) null);
			total = summaries.size();
			state.setTotal(total);
			state.setCount(0);
			for (CollectRecord s : summaries) {
				if ( s.getStep().getStepNumber() <= step.getStepNumber() ) {
					CollectRecord record = recordManager.load(survey, s.getId(), step.getStepNumber());
					rowsCount += modelWriter.printData(record);
					read++;
					state.setCount(read);
				}
			}
			state.setExtracting(false);
			
			//System.out.println("Exported "+rowsCount+" rows from "+read+" records in "+(duration/1000)+"s ("+(duration/rowsCount)+"ms/row).");
		} finally {
			if ( modelWriter!=null ) {
				modelWriter.flush();
				modelWriter.close();
			}
		}
	}
}
