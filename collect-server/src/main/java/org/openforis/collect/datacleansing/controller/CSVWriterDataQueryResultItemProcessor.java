package org.openforis.collect.datacleansing.controller;

import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.openforis.collect.datacleansing.DataQuery;
import org.openforis.collect.datacleansing.DataQueryResultItem;
import org.openforis.collect.datacleansing.controller.DataQueryController.AttributeQueryResultItemProcessor;
import org.openforis.commons.io.OpenForisIOUtils;
import org.openforis.commons.io.csv.CsvWriter;
import org.openforis.idm.metamodel.AttributeDefinition;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.NodeLabel.Type;
import org.openforis.idm.model.Value;

/**
 * 
 * @author S. Ricci
 *
 */
public class CSVWriterDataQueryResultItemProcessor extends AttributeQueryResultItemProcessor implements Closeable {
	
	//output
	private File tempFile;
	//transient
	private CsvWriter csvWriter;
	
	public CSVWriterDataQueryResultItemProcessor(DataQuery query) {
		super(query);
		init();
	}
	
	private void init() {
		try {
			tempFile = File.createTempFile("collect-data-cleansing-query", ".csv");
			csvWriter = new CsvWriter(new FileOutputStream(tempFile), OpenForisIOUtils.UTF_8, ',', '"');
			writeCSVHeader();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	private void writeCSVHeader() {
		List<String> headers = new ArrayList<String>();
		EntityDefinition rootEntity = query.getEntityDefinition().getRootEntity();
		List<AttributeDefinition> keyAttributeDefinitions = rootEntity.getKeyAttributeDefinitions();
		for (AttributeDefinition def : keyAttributeDefinitions) {
			String keyLabel = def.getLabel(Type.INSTANCE);
			if (StringUtils.isBlank(keyLabel)) {
				keyLabel = def.getName();
			}
			headers.add(keyLabel);
		}
		headers.add("Path");
		AttributeDefinition attrDef = (AttributeDefinition) query.getSchema().getDefinitionById(query.getAttributeDefinitionId());
		headers.addAll(extractFieldHeaders(attrDef));
		csvWriter.writeHeaders(headers);
	}

	private List<String> extractFieldHeaders(AttributeDefinition attrDef) {
		List<String> fieldNames = attrDef.getFieldNames();
		List<String> headers = new ArrayList<String>(fieldNames.size());
		String attrName = attrDef.getName();
		if (fieldNames.size() > 1) {
			for (String fieldName : fieldNames) {
				headers.add(attrName + "_" + fieldName);
			}
		} else {
			headers.add(attrName);
		}
		return headers;
	}
	
	@Override
	public void process(DataQueryResultItem item) {
		List<String> lineValues = new ArrayList<String>();
		lineValues.addAll(item.getRecordKeyValues());
		lineValues.add(item.extractNodePath());
		AttributeDefinition attrDef = item.getAttributeDefinition();
		Value value = item.extractAttributeValue();
		Map<String, Object> valueMap = value == null ? null : value.toMap();
		List<String> fieldNames = attrDef.getFieldNames();
		for (String fieldName : fieldNames) {
			Object fieldValue = valueMap == null ? null : valueMap.get(fieldName);
			lineValues.add(fieldValue == null ? "": fieldValue.toString());
		}
		csvWriter.writeNext(lineValues);
	}
	
	@Override
	public void close() throws IOException {
		csvWriter.close();
	}

	public File getOutputFile() {
		return tempFile;
	}
}