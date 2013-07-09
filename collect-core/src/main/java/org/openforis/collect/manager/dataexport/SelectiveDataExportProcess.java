package org.openforis.collect.manager.dataexport;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openforis.collect.csv.AutomaticColumnProvider;
import org.openforis.collect.csv.ColumnProvider;
import org.openforis.collect.csv.ColumnProviderChain;
import org.openforis.collect.csv.DataTransformation;
import org.openforis.collect.csv.ModelCsvWriter;
import org.openforis.collect.csv.NodePositionColumnProvider;
import org.openforis.collect.csv.PivotExpressionColumnProvider;
import org.openforis.collect.csv.SingleAttributeColumnProvider;
import org.openforis.collect.manager.CodeListManager;
import org.openforis.collect.manager.RecordManager;
import org.openforis.collect.manager.dataexport.DataExportStatus.Format;
import org.openforis.collect.manager.process.AbstractProcess;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.CollectRecord.Step;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.RecordSummarySortField;
import org.openforis.collect.persistence.RecordPersistenceException;
import org.openforis.idm.metamodel.AttributeDefinition;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.Schema;
import org.openforis.idm.model.expression.InvalidExpressionException;

/**
 * 
 * @author S. Ricci
 *
 */
public class SelectiveDataExportProcess extends AbstractProcess<Void, DataExportStatus> {
	
	private static Log LOG = LogFactory.getLog(SelectiveDataExportProcess.class);

	private RecordManager recordManager;
	private CodeListManager codeListManager;
	private File exportDirectory;
	private CollectSurvey survey;
	private String rootEntityName;
	private int entityId;
	private Step step;
	
	public SelectiveDataExportProcess(RecordManager recordManager,
			CodeListManager codeListManager, File exportDirectory,
			CollectSurvey survey, String rootEntityName, int entityId, Step step) {
		super();
		this.recordManager = recordManager;
		this.codeListManager = codeListManager;
		this.exportDirectory = exportDirectory;
		this.survey = survey;
		this.rootEntityName = rootEntityName;
		this.entityId = entityId;
		this.step = step;
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
			if ( zipOutputStream != null ) {
				zipOutputStream.closeEntry();
				zipOutputStream.flush();
				zipOutputStream.close();
			}
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
		status.setTotal(calculateTotal(summaries));
		int stepNumber = step.getStepNumber();
		for (CollectRecord s : summaries) {
			if ( status.isRunning() ) {
				if ( stepNumber == s.getStep().getStepNumber() ) {
					CollectRecord record = recordManager.load(survey, s.getId(), stepNumber);
					modelWriter.printData(record);
					status.incrementProcessed();
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
		EntityDefinition entityDefn = (EntityDefinition) schema.getDefinitionById(entityId);
		List<ColumnProvider> columnProviders = createAncestorsColumnsProvider(entityDefn);
		columnProviders.add(new AutomaticColumnProvider(codeListManager, entityDefn));
		ColumnProvider provider = new ColumnProviderChain(columnProviders);
		String axisPath = entityDefn.getPath();
		return new DataTransformation(axisPath, provider);
	}
	
	private List<ColumnProvider> createAncestorsColumnsProvider(EntityDefinition entityDefn) {
		List<ColumnProvider> columnProviders = new ArrayList<ColumnProvider>();
		EntityDefinition parentDefn = (EntityDefinition) entityDefn.getParentDefinition();
		int depth = 1;
		while ( parentDefn != null ) {
			ColumnProvider parentKeysColumnsProvider = createAncestorColumnProvider(parentDefn, depth);
			columnProviders.add(0, parentKeysColumnsProvider);
			parentDefn = (EntityDefinition) parentDefn.getParentDefinition();
			depth++;
		}
		return columnProviders;
	}
	
	private ColumnProvider createAncestorColumnProvider(EntityDefinition entityDefn, int depth) {
		List<AttributeDefinition> keyAttrDefns = entityDefn.getKeyAttributeDefinitions();
		List<ColumnProvider> providers = new ArrayList<ColumnProvider>();
		for (AttributeDefinition keyDefn : keyAttrDefns) {
			String columnName = createKeyAttributeColumnName(keyDefn);
			SingleAttributeColumnProvider keyColumnProvider = new SingleAttributeColumnProvider(keyDefn.getName(), columnName);
			providers.add(keyColumnProvider);
		}
		if ( entityDefn.getParentDefinition() != null ) {
			ColumnProvider positionColumnProvider = createPositionColumnProvider(entityDefn);
			providers.add(positionColumnProvider);
		}
		String expression = StringUtils.repeat("parent()", "/", depth);
		ColumnProvider result = new PivotExpressionColumnProvider(expression, providers.toArray(new ColumnProvider[1]));
		return result;
	}
	
	private ColumnProvider createPositionColumnProvider(EntityDefinition entityDefn) {
		String columnName = createPositionColumnName(entityDefn);
		NodePositionColumnProvider columnProvider = new NodePositionColumnProvider(columnName);
		return columnProvider;
	}
	
	private String createKeyAttributeColumnName(AttributeDefinition attrDefn) {
		StringBuilder sb = new StringBuilder();
		String name = attrDefn.getName();
		sb.append(name);
		EntityDefinition parent = (EntityDefinition) attrDefn.getParentDefinition();
		while ( parent != null ) {
			String parentName = parent.getName();
			sb.insert(0, '_').insert(0, parentName);
			parent = (EntityDefinition) parent.getParentDefinition();
		}
		return sb.toString();
	}
	
	private String createPositionColumnName(NodeDefinition nodeDefn) {
		StringBuilder sb = new StringBuilder();
		String name = nodeDefn.getName();
		sb.append(name);
		sb.append("_position");
		EntityDefinition parent = (EntityDefinition) nodeDefn.getParentDefinition();
		while ( parent != null ) {
			String parentName = parent.getName();
			sb.insert(0, '_').insert(0, parentName);
			parent = (EntityDefinition) parent.getParentDefinition();
		}
		return sb.toString();
	}
}

