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
import org.openforis.collect.io.data.csv.AutomaticColumnProvider;
import org.openforis.collect.io.data.csv.CSVExportConfiguration;
import org.openforis.collect.io.data.csv.ColumnProvider;
import org.openforis.collect.io.data.csv.ColumnProviderChain;
import org.openforis.collect.io.data.csv.ColumnProviders;
import org.openforis.collect.io.data.csv.DataTransformation;
import org.openforis.collect.io.data.csv.ModelCsvWriter;
import org.openforis.collect.io.data.csv.NodePositionColumnProvider;
import org.openforis.collect.io.data.csv.PivotExpressionColumnProvider;
import org.openforis.collect.manager.RecordManager;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.CollectRecord.Step;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.RecordFilter;
import org.openforis.collect.persistence.RecordPersistenceException;
import org.openforis.commons.io.OpenForisIOUtils;
import org.openforis.concurrency.Job;
import org.openforis.concurrency.Task;
import org.openforis.idm.metamodel.AttributeDefinition;
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
public class CSVDataExportJob extends Job {
	
//	private static Log LOG = LogFactory.getLog(CSVDataExportJob.class);

	@Autowired
	private RecordManager recordManager;
	
	private File outputFile;
	private RecordFilter recordFilter;
	private NodeFilter nodeFilter;
	private Integer entityId;
	private boolean alwaysGenerateZipFile;
	private CSVExportConfiguration configuration;
	
	public CSVDataExportJob() {
		alwaysGenerateZipFile = false;
		configuration = new CSVExportConfiguration();
	}
	
	@Override
	protected void buildTasks() throws Throwable {
		addTask(new CSVDataExportTask());
	}

	private class CSVDataExportTask extends Task {
		
		@Override
		protected void initializeInternalVariables() throws Throwable {
			super.initializeInternalVariables();
		}
		
		@Override
		protected long countTotalItems() {
			int totalRecords = recordManager.countRecords(recordFilter);
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
				
				if ( entities.size() == 1 && ! alwaysGenerateZipFile ) {
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
			DataTransformation transform = getTransform(entityDefId);
			
			@SuppressWarnings("resource")
			//closing modelWriter will close referenced output stream
			ModelCsvWriter modelWriter = new ModelCsvWriter(outputWriter, transform, nodeFilter);
			modelWriter.printColumnHeadings();
			
			CollectSurvey survey = recordFilter.getSurvey();
			Step step = recordFilter.getStepGreaterOrEqual();
			List<CollectRecord> summaries = recordManager.loadSummaries(recordFilter);
			for (CollectRecord s : summaries) {
				if ( isRunning() ) {
					CollectRecord record = recordManager.load(survey, s.getId(), step, false);
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
		
		protected DataTransformation getTransform(int entityDefId) throws InvalidExpressionException {
			List<ColumnProvider> columnProviders = new ArrayList<ColumnProvider>();
			
			CollectSurvey survey = recordFilter.getSurvey();
			Schema schema = survey.getSchema();
			EntityDefinition entityDefn = (EntityDefinition) schema.getDefinitionById(entityDefId);
			
			//entity children columns
			AutomaticColumnProvider entityColumnProvider = createEntityColumnProvider(entityDefn);

			//ancestor columns
			columnProviders.addAll(createAncestorsColumnsProvider(entityDefn));
			
			//position column
			if ( isPositionColumnRequired(entityDefn) ) {
				columnProviders.add(createPositionColumnProvider(entityDefn));
			}
			columnProviders.add(entityColumnProvider);
			
			//create data transformation
			ColumnProvider provider = new ColumnProviderChain(configuration, columnProviders);
			String axisPath = entityDefn.getPath();
			return new DataTransformation(axisPath, provider);
		}

		protected AutomaticColumnProvider createEntityColumnProvider(EntityDefinition entityDefn) {
			AutomaticColumnProvider entityColumnProvider = new AutomaticColumnProvider(configuration, "", entityDefn, null);
			return entityColumnProvider;
		}
		
		private List<ColumnProvider> createAncestorsColumnsProvider(EntityDefinition entityDefn) {
			List<ColumnProvider> columnProviders = new ArrayList<ColumnProvider>();
			EntityDefinition ancestorDefn = (EntityDefinition) entityDefn.getParentDefinition();
			while ( ancestorDefn != null ) {
				if (ancestorDefn.isMultiple()) {
					ColumnProvider parentKeysColumnsProvider = createAncestorColumnProvider(entityDefn, ancestorDefn);
					columnProviders.add(0, parentKeysColumnsProvider);
				}
				ancestorDefn = ancestorDefn.getParentEntityDefinition();
			}
			return columnProviders;
		}
		
		private ColumnProvider createAncestorColumnProvider(EntityDefinition contextEntityDefn, EntityDefinition ancestorEntityDefn) {
			List<ColumnProvider> providers = new ArrayList<ColumnProvider>();
			if ( configuration.isIncludeAllAncestorAttributes() ) {
				AutomaticColumnProvider ancestorEntityColumnProvider = new AutomaticColumnProvider(configuration, ancestorEntityDefn.getName() + "_", ancestorEntityDefn);
				providers.add(0, ancestorEntityColumnProvider);
			} else {
				//include only key attributes
				List<AttributeDefinition> keyAttrDefns = ancestorEntityDefn.getKeyAttributeDefinitions();
				for (AttributeDefinition keyDefn : keyAttrDefns) {
					String relativePath = contextEntityDefn.getRelativePath(ancestorEntityDefn);
					
					ColumnProvider keyColumnProvider = ColumnProviders.createAttributeProvider(configuration, keyDefn);
					String headingPrefix = keyDefn.getParentEntityDefinition().getName() + "_";
					PivotExpressionColumnProvider columnProvider = new PivotExpressionColumnProvider(configuration, relativePath, headingPrefix, keyColumnProvider);
					providers.add(columnProvider);
				}
				if ( isPositionColumnRequired(ancestorEntityDefn) ) {
					String relativePath = contextEntityDefn.getRelativePath(ancestorEntityDefn);
					ColumnProvider positionColumnProvider = createPositionColumnProvider(ancestorEntityDefn);
					PivotExpressionColumnProvider columnProvider = new PivotExpressionColumnProvider(configuration, relativePath, "", positionColumnProvider);
					providers.add(columnProvider);
				}
			}
			return new ColumnProviderChain(configuration, providers);
		}
		
		private boolean isPositionColumnRequired(EntityDefinition entityDefn) {
			return entityDefn.getParentDefinition() != null && entityDefn.isMultiple() && entityDefn.getKeyAttributeDefinitions().isEmpty();
		}
		
		private ColumnProvider createPositionColumnProvider(EntityDefinition entityDefn) {
			String columnName = calculatePositionColumnName(entityDefn);
			return new NodePositionColumnProvider(columnName);
		}
		
		private String calculatePositionColumnName(EntityDefinition nodeDefn) {
			return "_" + nodeDefn.getName() + "_position";
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
	
	public CSVExportConfiguration getConfiguration() {
		return configuration;
	}
	
	public void setConfiguration(CSVExportConfiguration configuration) {
		this.configuration = configuration;
	}

	public boolean isAlwaysGenerateZipFile() {
		return alwaysGenerateZipFile;
	}

	public void setAlwaysGenerateZipFile(boolean alwaysGenerateZipFile) {
		this.alwaysGenerateZipFile = alwaysGenerateZipFile;
	}

	public void setNodeFilter(NodeFilter nodeFilter) {
		this.nodeFilter = nodeFilter;
	}
	
}

