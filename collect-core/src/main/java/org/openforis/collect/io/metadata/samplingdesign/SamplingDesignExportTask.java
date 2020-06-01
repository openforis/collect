package org.openforis.collect.io.metadata.samplingdesign;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.poi.util.IOUtils;
import org.openforis.collect.manager.SamplingDesignManager;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.SamplingDesignItem;
import org.openforis.collect.model.SamplingDesignSummaries;
import org.openforis.commons.io.OpenForisIOUtils;
import org.openforis.commons.io.csv.CsvWriter;
import org.openforis.commons.io.excel.ExcelFlatValuesWriter;
import org.openforis.commons.io.flat.FlatDataWriter;
import org.openforis.concurrency.Task;
import org.openforis.idm.metamodel.ReferenceDataSchema;
import org.openforis.idm.metamodel.ReferenceDataSchema.ReferenceDataDefinition;
import org.openforis.idm.metamodel.ReferenceDataSchema.SamplingPointDefinition;
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
public class SamplingDesignExportTask extends Task {
	
	private SamplingDesignManager samplingDesignManager;

	//parameters
	private CollectSurvey survey;
	private OutputStream outputStream;
	private OutputFormat outputFormat = OutputFormat.CSV;

	public enum OutputFormat {
		CSV, EXCEL
	}
	
	@Override
	protected long countTotalItems() {
		Integer surveyId = survey.getId();
		int count = samplingDesignManager.countBySurvey(surveyId);
		return count;
	}
	
	@Override
	protected void execute() throws Throwable {
		if ( getTotalItems() > 0 ) {
			Integer surveyId = survey.getId();
			
			FlatDataWriter writer = null;
			try {
				if (outputFormat == OutputFormat.CSV) {
					OutputStreamWriter osWriter = new OutputStreamWriter(outputStream, Charset.forName(OpenForisIOUtils.UTF_8));
					writer = new CsvWriter(osWriter, ',', '"');
				} else {
					writer = new ExcelFlatValuesWriter(outputStream);
				}
				SamplingDesignSummaries summaries = samplingDesignManager.loadBySurvey(surveyId);
						
				ArrayList<String> colNames = getHeaders();
				writer.writeHeaders(colNames);
				
				List<SamplingDesignItem> items = summaries.getRecords();
				for (SamplingDesignItem item : items) {
					writeSummary(writer, item);
					incrementProcessedItems();
				}
			} finally {
				IOUtils.closeQuietly(writer);
			}
		}
	}

	private ArrayList<String> getHeaders() {
		ArrayList<String> colNames = new ArrayList<String>();
		colNames.addAll(Arrays.asList(SamplingDesignFileColumn.LEVEL_COLUMN_NAMES));
		colNames.add(SamplingDesignFileColumn.X.getColumnName());
		colNames.add(SamplingDesignFileColumn.Y.getColumnName());
		colNames.add(SamplingDesignFileColumn.SRS_ID.getColumnName());
		
		//info columns
		List<ReferenceDataDefinition.Attribute> infoAttributes = getSamplingPointInfoAttributes();
		for (ReferenceDataDefinition.Attribute attribute : infoAttributes) {
			colNames.add(attribute.getName());
		}
		return colNames;
	}

	private List<ReferenceDataDefinition.Attribute> getSamplingPointInfoAttributes() {
		ReferenceDataSchema referenceDataSchema = survey.getReferenceDataSchema();
		SamplingPointDefinition samplingPoint = referenceDataSchema == null ? null: referenceDataSchema.getSamplingPointDefinition();
		if ( samplingPoint == null ) {
			return Collections.emptyList();
		} else {
			List<ReferenceDataDefinition.Attribute> infoAttributes = samplingPoint.getAttributes(false);
			return infoAttributes;
		}
	}

	protected void writeSummary(FlatDataWriter writer, SamplingDesignItem item) {
		List<String> lineValues = new ArrayList<String>();
		List<String> levelCodes = item.getLevelCodes();
		SamplingDesignFileColumn[] levelColumns = SamplingDesignFileColumn.LEVEL_COLUMNS;
		for (int level = 1; level <= levelColumns.length; level++) {
			String levelCode = level <= levelCodes.size() ? item.getLevelCode(level): "";
			lineValues.add(levelCode);
		}
		lineValues.add(item.getX().toString());
		lineValues.add(item.getY().toString());
		lineValues.add(item.getSrsId());
		
		//write info columns
		List<ReferenceDataDefinition.Attribute> infoAttributes = getSamplingPointInfoAttributes();
		for (int i = 0; i < infoAttributes.size(); i++) {
			lineValues.add(item.getInfoAttribute(i));
		}
		writer.writeNext(lineValues);
	}

	public SamplingDesignManager getSamplingDesignManager() {
		return samplingDesignManager;
	}
	
	public void setSamplingDesignManager(
			SamplingDesignManager samplingDesignManager) {
		this.samplingDesignManager = samplingDesignManager;
	}
	
	public CollectSurvey getSurvey() {
		return survey;
	}

	public void setSurvey(CollectSurvey survey) {
		this.survey = survey;
	}
	
	public OutputStream getOutputStream() {
		return outputStream;
	}

	public void setOutputStream(OutputStream outputStream) {
		this.outputStream = outputStream;
	}
	
	public OutputFormat getOutputFormat() {
		return outputFormat;
	}
	
	public void setOutputFormat(OutputFormat outputFormat) {
		this.outputFormat = outputFormat;
	}

}