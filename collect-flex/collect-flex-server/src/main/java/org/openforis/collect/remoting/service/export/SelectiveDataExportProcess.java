package org.openforis.collect.remoting.service.export;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openforis.collect.manager.RecordManager;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.CollectRecord.Step;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.RecordSummarySortField;
import org.openforis.collect.persistence.RecordPersistenceException;
import org.openforis.collect.remoting.service.export.DataExportState.Format;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.Schema;
import org.openforis.idm.model.expression.InvalidExpressionException;
import org.openforis.idm.transform.AutomaticColumnProvider;
import org.openforis.idm.transform.ColumnProvider;
import org.openforis.idm.transform.ColumnProviderChain;
import org.openforis.idm.transform.DataTransformation;
import org.openforis.idm.transform.csv.ModelCsvWriter;

/**
 * 
 * @author S. Ricci
 *
 */
public class SelectiveDataExportProcess implements Callable<Void>, DataExportProcess {
	
	private static Log LOG = LogFactory.getLog(DataExportProcess.class);

	private RecordManager recordManager;
	private File exportDirectory;
	private DataExportState state;
	private CollectSurvey survey;
	private String rootEntityName;
	private int entityId;
	private Step step;
	
	public SelectiveDataExportProcess(RecordManager recordManager, File exportDirectory, 
			CollectSurvey survey, String rootEntityName, int entityId, Step step) {
		super();
		this.recordManager = recordManager;
		this.exportDirectory = exportDirectory;
		this.survey = survey;
		this.rootEntityName = rootEntityName;
		this.entityId = entityId;
		this.step = step;
		this.state = new DataExportState(Format.CSV);
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
			state.setRunning(true);
			exportData();
			state.setComplete(true);
		} catch (Exception e) {
			state.setError(true);
			LOG.error("Error during data export", e);
		} finally {
			state.setRunning(false);
		}
		return null;
	}
	
	private File exportData() throws Exception {
		File file = null;
		file = new File(exportDirectory, "data.zip");
		if ( file.exists() ) {
			file.delete();
			file.createNewFile();
		}
		FileOutputStream fileOutputStream = null;
		ZipOutputStream zipOutputStream = null;
		try {
			fileOutputStream = new FileOutputStream(file);
			BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream);
			zipOutputStream = new ZipOutputStream(bufferedOutputStream);
			ZipEntry entry = new ZipEntry("data.csv");
			zipOutputStream.putNextEntry(entry);
			
			exportData(zipOutputStream);
		} catch (Exception e) {
			throw e;
		} finally {
			zipOutputStream.closeEntry();
			zipOutputStream.flush();
			zipOutputStream.close();
		}
		//System.out.println("Exported "+rowsCount+" rows from "+read+" records in "+(duration/1000)+"s ("+(duration/rowsCount)+"ms/row).");
		return file;
	}

	private void exportData(ZipOutputStream zipOutputStream) throws InvalidExpressionException, IOException, RecordPersistenceException {
		Writer outputWriter = new OutputStreamWriter(zipOutputStream);
		DataTransformation transform = getTransform();
		
		ModelCsvWriter modelWriter = new ModelCsvWriter(outputWriter, transform);
		modelWriter.printColumnHeadings();
		
		List<CollectRecord> summaries = recordManager.loadSummaries(survey, rootEntityName, 0, Integer.MAX_VALUE, (List<RecordSummarySortField>) null, (String) null);
		state.setTotal(calculateTotal(summaries));
		int stepNumber = step.getStepNumber();
		for (CollectRecord s : summaries) {
			if ( ! state.isCancelled() ) {
				if ( stepNumber == s.getStep().getStepNumber() ) {
					CollectRecord record = recordManager.load(survey, s.getId(), stepNumber);
					modelWriter.printData(record);
					state.incrementCount();
				}
			} else {
				break;
			}
		}
	}
	
	private int calculateTotal(List<CollectRecord> recordSummaries) {
		int count = 0;
		int stepNumber = step.getStepNumber();
		for (CollectRecord summary : recordSummaries) {
			int recordStepNumber = summary.getStep().getStepNumber();
			if ( recordStepNumber == stepNumber) {
				count ++;
			}
		}
		return count;
	}

	private DataTransformation getTransform() throws InvalidExpressionException {
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
	
}
