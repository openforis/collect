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

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.util.IOUtils;
import org.openforis.collect.csv.AutomaticColumnProvider;
import org.openforis.collect.csv.ColumnProvider;
import org.openforis.collect.csv.ColumnProviderChain;
import org.openforis.collect.csv.DataTransformation;
import org.openforis.collect.csv.ModelCsvWriter;
import org.openforis.collect.csv.NodePositionColumnProvider;
import org.openforis.collect.csv.PivotExpressionColumnProvider;
import org.openforis.collect.csv.SingleAttributeColumnProvider;
import org.openforis.collect.io.data.DataExportStatus.Format;
import org.openforis.collect.manager.RecordManager;
import org.openforis.collect.manager.process.AbstractProcess;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.CollectRecord.Step;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.persistence.RecordPersistenceException;
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
public class CSVDataExportProcess extends AbstractProcess<Void, DataExportStatus> {
	
	private static Log LOG = LogFactory.getLog(CSVDataExportProcess.class);

	@Autowired
	private RecordManager recordManager;
	
	private File outputFile;
	private CollectSurvey survey;
	private String rootEntityName;
	private Integer entityId;
	private Step step;
	private boolean includeAllAncestorAttributes;
	private boolean alwaysGenerateZipFile;
	
	public CSVDataExportProcess() {
		includeAllAncestorAttributes = false;
		alwaysGenerateZipFile = false;
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
		FileOutputStream fileOutputStream = null;
		ZipOutputStream zipOS = null;
		if ( outputFile.exists() ) {
			outputFile.delete();
			outputFile.createNewFile();
		}
		try {
			status.setTotal(calculateTotal());
			fileOutputStream = new FileOutputStream(outputFile);
			BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream);
			
			Collection<EntityDefinition> entities = getEntitiesToExport();
			
			if ( entities.size() == 1 && ! alwaysGenerateZipFile ) {
				//export entity into a single csv file 
				EntityDefinition entity = entities.iterator().next();
				exportData(bufferedOutputStream, entity.getId());
				bufferedOutputStream.flush();
				IOUtils.close(bufferedOutputStream);
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
			if ( zipOS != null ) {
				zipOS.flush();
				zipOS.close();
			}
		}
		//System.out.println("Exported "+rowsCount+" rows from "+read+" records in "+(duration/1000)+"s ("+(duration/rowsCount)+"ms/row).");
	}

	private String calculateOutputFileName() {
		return "data.zip";
		/*
		StringBuilder sb = new StringBuilder();
		sb.append(survey.getName());
		sb.append("_");
		sb.append(rootEntityName);
		sb.append("_");
		sb.append("csv_data");
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		String today = formatter.format(new Date());
		sb.append(today);
		sb.append(".zip");
		return sb.toString();
		*/
	}

	private void exportData(OutputStream outputStream, int entityId) throws InvalidExpressionException, IOException, RecordPersistenceException {
		Writer outputWriter = new OutputStreamWriter(outputStream);
		DataTransformation transform = getTransform(entityId);
		
		@SuppressWarnings("resource")
		//closing modelWriter will close referenced output stream
		ModelCsvWriter modelWriter = new ModelCsvWriter(outputWriter, transform);
		modelWriter.printColumnHeadings();
		
		int stepNumber = step.getStepNumber();
		List<CollectRecord> summaries = recordManager.loadSummaries(survey, rootEntityName);
		for (CollectRecord s : summaries) {
			if ( status.isRunning() ) {
				if ( stepNumber <= s.getStep().getStepNumber() ) {
					CollectRecord record = recordManager.load(survey, s.getId(), stepNumber);
					modelWriter.printData(record);
					status.incrementProcessed();
				}
			} else {
				break;
			}
		}
	}
	
	private Collection<EntityDefinition> getEntitiesToExport() {
		final Collection<EntityDefinition> result = new ArrayList<EntityDefinition>();
		Schema schema = survey.getSchema();
		if ( entityId == null ) {
			EntityDefinition rootEntity = schema.getRootEntityDefinition(rootEntityName);
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
		Schema schema = survey.getSchema();
		EntityDefinition rootEntity = schema.getRootEntityDefinition(rootEntityName);
		int stepNumber = step.getStepNumber();
		int totalRecords = recordManager.countRecords(survey, rootEntity.getId(), stepNumber);
		Collection<EntityDefinition> entitiesToExport = getEntitiesToExport();
		int result = totalRecords * entitiesToExport.size();
		return result;
	}

	private DataTransformation getTransform(int entityId) throws InvalidExpressionException {
		List<ColumnProvider> columnProviders = new ArrayList<ColumnProvider>();
		
		Schema schema = survey.getSchema();
		EntityDefinition entityDefn = (EntityDefinition) schema.getDefinitionById(entityId);
		
		//entity children columns
		AutomaticColumnProvider entityColumnProvider = new AutomaticColumnProvider(entityDefn);

		//ancestor columns
		columnProviders.addAll(createAncestorsColumnsProvider(entityDefn));
		//position column
		if ( isPositionColumnRequired(entityDefn) ) {
			columnProviders.add(createPositionColumnProvider(entityDefn));
		}
		columnProviders.add(entityColumnProvider);
		
		//create data transformation
		ColumnProvider provider = new ColumnProviderChain(columnProviders);
		String axisPath = entityDefn.getPath();
		return new DataTransformation(axisPath, provider);
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
		EntityDefinition entityDefn = (EntityDefinition) schema.getDefinitionById(entityId);
		List<ColumnProvider> columnProviders = new ArrayList<ColumnProvider>();
		columnProviders.addAll(createAncestorsColumnsProvider(entityDefn));
		if ( isPositionColumnRequired(entityDefn) ) {
			columnProviders.add(createPositionColumnProvider(entityDefn));
		}
		columnProviders.add(new AutomaticColumnProvider(entityDefn));
		ColumnProvider provider = new ColumnProviderChain(columnProviders);
		String axisPath = entityDefn.getPath();
		return new DataTransformation(axisPath, provider);
	}

	private List<ColumnProvider> createAncestorsColumnsProvider(EntityDefinition entityDefn) {
		List<ColumnProvider> columnProviders = new ArrayList<ColumnProvider>();
		EntityDefinition ancestorDefn = (EntityDefinition) entityDefn.getParentDefinition();
		int depth = 1;
		while ( ancestorDefn != null ) {
			ColumnProvider parentKeysColumnsProvider = createAncestorColumnProvider(ancestorDefn, depth);
			columnProviders.add(0, parentKeysColumnsProvider);
			ancestorDefn = ancestorDefn.getParentEntityDefinition();
			depth++;
		}
		return columnProviders;
	}
	
	private ColumnProvider createAncestorColumnProvider(EntityDefinition entityDefn, int depth) {
		List<ColumnProvider> providers = new ArrayList<ColumnProvider>();
		String pivotExpression = StringUtils.repeat("parent()", "/", depth);
		if ( includeAllAncestorAttributes ) {
			AutomaticColumnProvider ancestorEntityColumnProvider = new AutomaticColumnProvider(entityDefn.getName() + "_", entityDefn);
			providers.add(0, ancestorEntityColumnProvider);
		} else {
			//include only key attributes
			List<AttributeDefinition> keyAttrDefns = entityDefn.getKeyAttributeDefinitions();
			for (AttributeDefinition keyDefn : keyAttrDefns) {
				String columnName = calculateAncestorKeyColumnName(keyDefn, false);
				SingleAttributeColumnProvider keyColumnProvider = new SingleAttributeColumnProvider(keyDefn.getName(), columnName);
				providers.add(keyColumnProvider);
			}
			if ( isPositionColumnRequired(entityDefn) ) {
				ColumnProvider positionColumnProvider = createPositionColumnProvider(entityDefn);
				providers.add(positionColumnProvider);
			}
		}
		ColumnProvider result = new PivotExpressionColumnProvider(pivotExpression, providers.toArray(new ColumnProvider[0]));
		return result;
	}
	
	private boolean isPositionColumnRequired(EntityDefinition entityDefn) {
		return entityDefn.getParentDefinition() != null && entityDefn.isMultiple() && entityDefn.getKeyAttributeDefinitions().isEmpty();
	}
	
	private ColumnProvider createPositionColumnProvider(EntityDefinition entityDefn) {
		String columnName = calculatePositionColumnName(entityDefn);
		NodePositionColumnProvider columnProvider = new NodePositionColumnProvider(columnName);
		return columnProvider;
	}
	
	private String calculateAncestorKeyColumnName(AttributeDefinition attrDefn, boolean includeAllAncestors) {
		EntityDefinition parent = attrDefn.getParentEntityDefinition();
		return parent.getName() + "_" + attrDefn.getName();
	}
	
	private String calculatePositionColumnName(EntityDefinition nodeDefn) {
		return "_" + nodeDefn.getName() + "_position";
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

	public Integer getEntityId() {
		return entityId;
	}

	public void setEntityId(Integer entityId) {
		this.entityId = entityId;
	}

	public Step getStep() {
		return step;
	}

	public void setStep(Step step) {
		this.step = step;
	}

	public boolean isIncludeAllAncestorAttributes() {
		return includeAllAncestorAttributes;
	}

	public void setIncludeAllAncestorAttributes(boolean includeAllAncestorAttributes) {
		this.includeAllAncestorAttributes = includeAllAncestorAttributes;
	}

	public boolean isAlwaysGenerateZipFile() {
		return alwaysGenerateZipFile;
	}

	public void setAlwaysGenerateZipFile(boolean alwaysGenerateZipFile) {
		this.alwaysGenerateZipFile = alwaysGenerateZipFile;
	}

}

