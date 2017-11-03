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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.lucene.util.IOUtils;
import org.openforis.collect.concurrency.SurveyLockingJob;
import org.openforis.collect.io.data.csv.CSVDataExportParameters;
import org.openforis.collect.io.data.csv.DataTransformation;
import org.openforis.collect.io.data.csv.ModelCsvWriter;
import org.openforis.collect.manager.RecordManager;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.CollectRecordSummary;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.RecordFilter;
import org.openforis.collect.persistence.RecordPersistenceException;
import org.openforis.commons.io.OpenForisIOUtils;
import org.openforis.concurrency.Task;
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
 */
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class CSVDataExportJob extends SurveyLockingJob {
	
//	private static Log LOG = LogFactory.getLog(CSVDataExportJob.class);

	@Autowired
	private RecordManager recordManager;
	
	private File outputFile;
	private CSVDataExportParameters parameters;
	
	public CSVDataExportJob() {
		parameters = new CSVDataExportParameters();
	}
	
	@Override
	protected void buildTasks() throws Throwable {
		addTask(new CSVDataExportTask());
	}

	public File getOutputFile() {
		return outputFile;
	}

	public void setOutputFile(File outputFile) {
		this.outputFile = outputFile;
	}

	public CSVDataExportParameters getParameters() {
		return parameters;
	}
	
	public void setParameters(CSVDataExportParameters parameters) {
		this.parameters = parameters;
	}
	
	private class CSVDataExportTask extends Task {
		
		@Override
		protected void initializeInternalVariables() throws Throwable {
			super.initializeInternalVariables();
		}
		
		@Override
		protected long countTotalItems() {
			int totalRecords = recordManager.countRecords(parameters.getRecordFilter());
			Collection<EntityDefinition> entitiesToExport = getEntitiesToExport();
			int result = totalRecords * entitiesToExport.size();
			return result;
		}
		
		@Override
		protected void execute() throws Throwable {
			BufferedOutputStream bufferedOutputStream = null;
			ZipOutputStream zipOS = null;
			try {
				FileOutputStream fileOutputStream = new FileOutputStream(outputFile);
				bufferedOutputStream = new BufferedOutputStream(fileOutputStream);
				
				Collection<EntityDefinition> entities = getEntitiesToExport();
				
				if ( entities.size() == 1 && ! parameters.isAlwaysGenerateZipFile() ) {
					//export entity into a single csv file 
					EntityDefinition entity = entities.iterator().next();
					exportData(bufferedOutputStream, entity.getId());
				} else {
					//export entities into a zip file containing different csv files
					zipOS = new ZipOutputStream(bufferedOutputStream);
					EntryNameGenerator entryNameGenerator = new EntryNameGenerator();
					for (EntityDefinition entity : entities) {
						if (isRunning()) {
							String entryName = entryNameGenerator.generateEntryName(entity);
							ZipEntry entry = new ZipEntry(entryName);
							zipOS.putNextEntry(entry);
							exportData(zipOS, entity.getId());
							zipOS.closeEntry();
						}
					}
				}
			} finally {
				IOUtils.close(zipOS);
				IOUtils.close(bufferedOutputStream);
			}
		}
		
		private void exportData(OutputStream outputStream, int entityDefId) throws InvalidExpressionException, IOException, RecordPersistenceException {
			Writer outputWriter = new OutputStreamWriter(outputStream, OpenForisIOUtils.UTF_8);
			RecordFilter recordFilter = parameters.getRecordFilter();
			CSVDataExportColumnProviderGenerator csvDataExportColumnProviderGenerator = new CSVDataExportColumnProviderGenerator(recordFilter.getSurvey(), parameters);
			DataTransformation transform = csvDataExportColumnProviderGenerator.generateDataTransformation(entityDefId);
			
			@SuppressWarnings("resource")
			//closing modelWriter will close referenced output stream
			ModelCsvWriter modelWriter = new ModelCsvWriter(outputWriter, transform, parameters.getNodeFilter());
			modelWriter.printColumnHeadings();
			
			CollectSurvey survey = recordFilter.getSurvey();
			List<CollectRecordSummary> summaries = recordManager.loadSummaries(recordFilter);
			for (CollectRecordSummary s : summaries) {
				if ( isRunning() ) {
					CollectRecord record = recordManager.load(survey, s.getId(), recordFilter.getStepGreaterOrEqual(), false);
					modelWriter.printData(record);
					incrementProcessedItems();
				} else {
					break;
				}
			}
			modelWriter.flush();
		}
		
		private Collection<EntityDefinition> getEntitiesToExport() {
			final Collection<EntityDefinition> result = new ArrayList<EntityDefinition>();
			RecordFilter recordFilter = parameters.getRecordFilter();
			Schema schema = recordFilter.getSurvey().getSchema();
			if ( parameters.getEntityId() == null ) {
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
				EntityDefinition entity = (EntityDefinition) schema.getDefinitionById(parameters.getEntityId());
				result.add(entity);
			}
			return result;
		}
	}
	
	public static class EntryNameGenerator {
		
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
		
		public Map<String, EntityDefinition> generateMultipleEntitesEntryMap(CollectSurvey survey) {
			final Map<String, EntityDefinition> result = new LinkedHashMap<String, EntityDefinition>();
			survey.getSchema().traverse(new NodeDefinitionVisitor() {
				public void visit(NodeDefinition def) {
					if (def instanceof EntityDefinition && def.isMultiple()) {
						EntityDefinition entityDef = (EntityDefinition) def;
						String entryName = generateEntryName(entityDef);
						result.put(entryName, entityDef);
					}
				}
			});
			return result;
		}
	}
	
}

