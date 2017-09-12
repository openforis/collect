package org.openforis.collect.io.data;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.util.IOUtils;
import org.openforis.collect.io.data.DataExportStatus.Format;
import org.openforis.collect.io.data.csv.CSVDataExportParameters;
import org.openforis.collect.io.data.csv.DataTransformation;
import org.openforis.collect.io.data.csv.ModelCsvWriter;
import org.openforis.collect.manager.RecordManager;
import org.openforis.collect.manager.process.AbstractProcess;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.CollectRecord.Step;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.RecordFilter;
import org.openforis.collect.persistence.RecordPersistenceException;
import org.openforis.commons.io.OpenForisIOUtils;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.NodeDefinitionVisitor;
import org.openforis.idm.metamodel.Schema;
import org.openforis.idm.model.expression.InvalidExpressionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * 
 * @author S. Ricci
 *
 * @deprecated Use {@link CSVDataExportJob instead}
 *
 */
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Deprecated
public class CSVDataExportProcess extends AbstractProcess<Void, DataExportStatus> {
	
	private static Log LOG = LogFactory.getLog(CSVDataExportProcess.class);

	@Autowired
	private RecordManager recordManager;
	
	private File outputFile;
	private RecordFilter recordFilter;
	private Integer entityId;
	private boolean alwaysGenerateZipFile;
	private CSVDataExportParameters configuration;
	
	public CSVDataExportProcess() {
		alwaysGenerateZipFile = false;
		configuration = new CSVDataExportParameters();
	}
	
	@Override
	protected void initStatus() {
		this.status = new DataExportStatus(Format.CSV);		
	}
	
	@Override
	public void startProcessing() throws Exception {
		super.startProcessing();
		exportData();
	}
	
	private void exportData() throws Exception {
		BufferedOutputStream bufferedOutputStream = null;
		ZipOutputStream zipOS = null;
		if ( outputFile.exists() ) {
			outputFile.delete();
			outputFile.createNewFile();
		}
		try {
			status.setTotal(calculateTotal());
			FileOutputStream fileOutputStream = new FileOutputStream(outputFile);
			bufferedOutputStream = new BufferedOutputStream(fileOutputStream);
			
			Collection<EntityDefinition> entities = getEntitiesToExport();
			
			if ( entities.size() == 1 && ! alwaysGenerateZipFile ) {
				//export entity into a single csv file 
				EntityDefinition entity = entities.iterator().next();
				exportData(bufferedOutputStream, entity.getId());
			} else {
				//export entities into a zip file containing different csv files
				zipOS = new ZipOutputStream(bufferedOutputStream);
				EntryNameGenerator entryNameGenerator = new EntryNameGenerator();
				for (EntityDefinition entity : entities) {
					String entryName = entryNameGenerator.generateEntryName(entity);
					ZipEntry entry = new ZipEntry(entryName);
					zipOS.putNextEntry(entry);
					exportData(zipOS, entity.getId());
					zipOS.closeEntry();
				}
			}
		} catch (Exception e) {
			status.error();
			status.setErrorMessage(e.getMessage());
			LOG.error(e.getMessage(), e);
			throw e;
		} finally {
			IOUtils.close(zipOS);
			IOUtils.close(bufferedOutputStream);

		}
		//System.out.println("Exported "+rowsCount+" rows from "+read+" records in "+(duration/1000)+"s ("+(duration/rowsCount)+"ms/row).");
	}

//	private String calculateOutputFileName() {
//		return "data.zip";
//		/*
//		StringBuilder sb = new StringBuilder();
//		sb.append(survey.getName());
//		sb.append("_");
//		sb.append(rootEntityName);
//		sb.append("_");
//		sb.append("csv_data");
//		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
//		String today = formatter.format(new Date());
//		sb.append(today);
//		sb.append(".zip");
//		return sb.toString();
//		*/
//	}

	private void exportData(OutputStream outputStream, int entityDefId) throws InvalidExpressionException, IOException, RecordPersistenceException {
		Writer outputWriter = new OutputStreamWriter(outputStream, OpenForisIOUtils.UTF_8);
		CSVDataExportColumnProviderGenerator csvDataExportColumnProviderGenerator = new CSVDataExportColumnProviderGenerator(recordFilter.getSurvey(), configuration);
		DataTransformation transform = csvDataExportColumnProviderGenerator.generateDataTransformation(entityDefId);
		
		@SuppressWarnings("resource")
		//closing modelWriter will close referenced output stream
		ModelCsvWriter modelWriter = new ModelCsvWriter(outputWriter, transform);
		modelWriter.printColumnHeadings();
		
		CollectSurvey survey = recordFilter.getSurvey();
		Step step = recordFilter.getStepGreaterOrEqual();
		List<CollectRecord> summaries = recordManager.loadSummaries(recordFilter);
		for (CollectRecord s : summaries) {
			if ( status.isRunning() ) {
				try {
					CollectRecord record = recordManager.load(survey, s.getId(), step, false);
					modelWriter.printData(record);
				} catch (Exception e) {
					LOG.error(e.getMessage(), e);
				}
				status.incrementProcessed();
			} else {
				break;
			}
		}
		modelWriter.flush();
	}
	
	private Collection<EntityDefinition> getEntitiesToExport() {
		final Collection<EntityDefinition> result = new ArrayList<EntityDefinition>();
		Schema schema = recordFilter.getSurvey().getSchema();
		if ( entityId == null ) {
			EntityDefinition rootEntity = schema.getRootEntityDefinition(recordFilter.getRootEntityId());
			rootEntity.traverse(new NodeDefinitionVisitor() {
				@Override
				public void visit(NodeDefinition node) {
					if ( node instanceof EntityDefinition && node.isMultiple() ) {
						result.add((EntityDefinition) node);
					}
				}
			});
		} else {
			EntityDefinition entity = (EntityDefinition) schema.getDefinitionById(entityId);
			result.add(entity);
		}
		return result;
	}
	
	private int calculateTotal() {
		int totalRecords = recordManager.countRecords(recordFilter);
		Collection<EntityDefinition> entitiesToExport = getEntitiesToExport();
		int result = totalRecords * entitiesToExport.size();
		return result;
	}

	private static class EntryNameGenerator {
		
		private Set<String> entryNames;
		
		public EntryNameGenerator() {
			entryNames = new HashSet<String>();
		}
		
		public String generateEntryName(EntityDefinition entity) {
			String name = entity.getName() + ".csv";
			if ( entryNames.contains(name) ) {
				name = entity.getParentEntityDefinition().getName() + "_" + name;
			}
			entryNames.add(name);
			return name;
		}
	}

	public File getOutputFile() {
		return outputFile;
	}

	public void setOutputFile(File outputFile) {
		this.outputFile = outputFile;
	}

	public RecordFilter getRecordFilter() {
		return recordFilter;
	}
	
	public void setRecordFilter(RecordFilter recordFilter) {
		this.recordFilter = recordFilter;
	}
	
	public Integer getEntityId() {
		return entityId;
	}

	public void setEntityId(Integer entityId) {
		this.entityId = entityId;
	}
	
	public CSVDataExportParameters getConfiguration() {
		return configuration;
	}
	
	public void setConfiguration(CSVDataExportParameters configuration) {
		this.configuration = configuration;
	}

	public boolean isAlwaysGenerateZipFile() {
		return alwaysGenerateZipFile;
	}

	public void setAlwaysGenerateZipFile(boolean alwaysGenerateZipFile) {
		this.alwaysGenerateZipFile = alwaysGenerateZipFile;
	}

}

