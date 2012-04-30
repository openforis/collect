package org.openforis.collect.remoting.service.export;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
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
public class DataExportProcess implements Callable<Void> {
	
	private static Log LOG = LogFactory.getLog(DataExportProcess.class);

	private RecordManager recordManager;
	private File exportDirectory;
	private DataExportState state;
	private CollectSurvey survey;
	private String rootEntityName;
	private int entityId;
	private Step step;
	private boolean running;
	
	public DataExportProcess(RecordManager recordManager, File exportDirectory, DataExportState state, 
			CollectSurvey survey, String rootEntityName, int entityId, Step step) {
		super();
		this.recordManager = recordManager;
		this.state = state;
		this.exportDirectory = exportDirectory;
		this.survey = survey;
		this.rootEntityName = rootEntityName;
		this.entityId = entityId;
		this.step = step;
	}

	@Override
	public Void call() throws Exception {
		running = true;
		try {
			File tempFile = null;
			state.setExporting(true);
			tempFile = exportData();
			state.setExporting(false);
			
			if( ! state.isCancelled() && ! state.isError() ) {
				state.setCompressing(true);
				compress(tempFile);
				state.setCompressing(false);
			}
			state.setComplete(true);
		} catch (Exception e) {
			state.setError(true);
			LOG.error(e);
		}
		running = false;
		return null;
	}

	public void cancel() {
		state.setCancelled(true);
	}
	
	private File exportData() throws Exception {
		File tempFile = null;
		ModelCsvWriter modelWriter = null;
		try {
			tempFile = new File(exportDirectory, "data.csv");
			if ( tempFile.exists() ) {
				tempFile.delete();
				tempFile.createNewFile();
			}
			Writer outputWriter = new FileWriter(tempFile);
			
			DataTransformation transform = getTransform();
			
			modelWriter = new ModelCsvWriter(outputWriter, transform);
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
				if ( ! state.isCancelled() ) {
					if ( s.getStep().getStepNumber() <= step.getStepNumber() ) {
						CollectRecord record = recordManager.load(survey, s.getId(), step.getStepNumber());
						rowsCount += modelWriter.printData(record);
						read++;
						state.setCount(read);
					}
				} else {
					break;
				}
			}
			//System.out.println("Exported "+rowsCount+" rows from "+read+" records in "+(duration/1000)+"s ("+(duration/rowsCount)+"ms/row).");
		} catch(Exception e) {
			throw e;
		} finally {
			if ( modelWriter!=null ) {
				modelWriter.flush();
				modelWriter.close();
			}
		}
		return tempFile;
	}

	private File compress(File source) throws Exception {
		File destFile = new File(exportDirectory, "data.zip");
		if ( destFile.exists() ) {
			destFile.delete();
			destFile.createNewFile();
		}
		ZipOutputStream out = null;
		FileInputStream in = null;
		try {
			out = new ZipOutputStream(new FileOutputStream(destFile));
			in = new FileInputStream(source);
			ZipEntry zipEntry = new ZipEntry(source.getName());
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
			out.flush();
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
		return destFile;
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
	
	public CollectSurvey getSurvey() {
		return survey;
	}

	public void setSurvey(CollectSurvey survey) {
		this.survey = survey;
	}

	public String getRootEntityName() {
		return rootEntityName;
	}

	public void setRootEntityName(String rootEntityName) {
		this.rootEntityName = rootEntityName;
	}

	public Step getStep() {
		return step;
	}

	public void setStep(Step step) {
		this.step = step;
	}

	public boolean isRunning() {
		return running;
	}
	
	
}
