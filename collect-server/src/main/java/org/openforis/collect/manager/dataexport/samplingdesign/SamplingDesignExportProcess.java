package org.openforis.collect.manager.dataexport.samplingdesign;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openforis.collect.io.metadata.samplingdesign.SamplingDesignFileColumn;
import org.openforis.collect.manager.SamplingDesignManager;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.SamplingDesignItem;
import org.openforis.collect.model.SamplingDesignSummaries;
import org.openforis.commons.io.csv.CsvWriter;
import org.openforis.idm.metamodel.ReferenceDataSchema;
import org.openforis.idm.metamodel.ReferenceDataSchema.ReferenceDataDefinition;
import org.openforis.idm.metamodel.ReferenceDataSchema.SamplingPointDefinition;

/**
 * 
 * @author S. Ricci
 *
 */
public class SamplingDesignExportProcess {
	
	private static final Logger LOG = LogManager.getLogger(SamplingDesignExportProcess.class);
	
	private SamplingDesignManager samplingDesignManager;
	
	public SamplingDesignExportProcess(SamplingDesignManager samplingDesignManager) {
		super();
		this.samplingDesignManager = samplingDesignManager;
	}

	public void exportToCSV(OutputStream out, CollectSurvey survey) {
		CsvWriter writer = null;
		try {
			writer = new CsvWriter(out);
			SamplingDesignSummaries summaries = samplingDesignManager.loadBySurvey(survey.getId());
				
			ArrayList<String> colNames = new ArrayList<String>();
			colNames.addAll(Arrays.asList(SamplingDesignFileColumn.LEVEL_COLUMN_NAMES));
			colNames.add(SamplingDesignFileColumn.X.getColumnName());
			colNames.add(SamplingDesignFileColumn.Y.getColumnName());
			colNames.add(SamplingDesignFileColumn.SRS_ID.getColumnName());

			//info columns
			List<ReferenceDataDefinition.Attribute> infoAttributes = getInfoAttributes(survey);
			for (ReferenceDataDefinition.Attribute attribute : infoAttributes) {
				colNames.add(attribute.getName());
			}
			writer.writeHeaders(colNames);
			
			List<SamplingDesignItem> items = summaries.getRecords();
			for (SamplingDesignItem item : items) {
				writeSummary(writer, survey, item);
			}
		} catch (Exception e) {
			LOG.error(e);
		} finally {
			IOUtils.closeQuietly(writer);
		}
	}

	protected void writeSummary(CsvWriter writer, CollectSurvey survey, SamplingDesignItem item) {
		List<String> lineValues = new ArrayList<String>();
		
		//write level columns
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
		List<ReferenceDataDefinition.Attribute> infoAttributes = getInfoAttributes(survey);
		for (int i = 0; i < infoAttributes.size(); i++) {
			lineValues.add(item.getInfoAttribute(i));
		}
		writer.writeNext(lineValues);
	}

	private List<ReferenceDataDefinition.Attribute> getInfoAttributes(CollectSurvey survey) {
		ReferenceDataSchema referenceDataSchema = survey.getReferenceDataSchema();
		SamplingPointDefinition samplingPoint = referenceDataSchema == null ? null: referenceDataSchema.getSamplingPointDefinition();
		if ( samplingPoint == null ) {
			return Collections.emptyList();
		} else {
			List<ReferenceDataDefinition.Attribute> infoAttributes = samplingPoint.getAttributes(false);
			return infoAttributes;
		}
	}

}
