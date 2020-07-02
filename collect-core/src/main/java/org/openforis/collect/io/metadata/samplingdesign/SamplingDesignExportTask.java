package org.openforis.collect.io.metadata.samplingdesign;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.openforis.collect.io.metadata.ReferenceDataExportTask;
import org.openforis.collect.manager.SamplingDesignManager;
import org.openforis.collect.model.SamplingDesignItem;
import org.openforis.collect.model.SamplingDesignSummaries;
import org.openforis.idm.metamodel.ReferenceDataSchema;
import org.openforis.idm.metamodel.ReferenceDataSchema.ReferenceDataDefinition;
import org.openforis.idm.metamodel.ReferenceDataSchema.SamplingPointDefinition;
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
public class SamplingDesignExportTask extends ReferenceDataExportTask {

	@Autowired
	private SamplingDesignManager samplingDesignManager;

	@Override
	protected long countTotalItems() {
		return samplingDesignManager.countBySurvey(survey.getId());
	}

	@Override
	protected List<String> getHeaders() {
		List<String> colNames = new ArrayList<String>();
		colNames.addAll(Arrays.asList(SamplingDesignFileColumn.LEVEL_COLUMN_NAMES));
		colNames.add(SamplingDesignFileColumn.X.getColumnName());
		colNames.add(SamplingDesignFileColumn.Y.getColumnName());
		colNames.add(SamplingDesignFileColumn.SRS_ID.getColumnName());

		// info columns
		List<ReferenceDataDefinition.Attribute> infoAttributes = getSamplingPointInfoAttributes();
		for (ReferenceDataDefinition.Attribute attribute : infoAttributes) {
			colNames.add(attribute.getName());
		}
		return colNames;
	}

	@Override
	protected void writeItems() {
		SamplingDesignSummaries summaries = samplingDesignManager.loadBySurvey(survey.getId());

		List<SamplingDesignItem> items = summaries.getRecords();
		for (SamplingDesignItem item : items) {
			writeItem(item);
			incrementProcessedItems();
		}
	}

	private List<ReferenceDataDefinition.Attribute> getSamplingPointInfoAttributes() {
		ReferenceDataSchema referenceDataSchema = survey.getReferenceDataSchema();
		SamplingPointDefinition samplingPoint = referenceDataSchema == null ? null
				: referenceDataSchema.getSamplingPointDefinition();
		if (samplingPoint == null) {
			return Collections.emptyList();
		} else {
			List<ReferenceDataDefinition.Attribute> infoAttributes = samplingPoint.getAttributes(false);
			return infoAttributes;
		}
	}

	protected void writeItem(SamplingDesignItem item) {
		List<String> lineValues = new ArrayList<String>();
		List<String> levelCodes = item.getLevelCodes();
		SamplingDesignFileColumn[] levelColumns = SamplingDesignFileColumn.LEVEL_COLUMNS;
		for (int level = 1; level <= levelColumns.length; level++) {
			String levelCode = level <= levelCodes.size() ? item.getLevelCode(level) : "";
			lineValues.add(levelCode);
		}
		lineValues.add(item.getX().toString());
		lineValues.add(item.getY().toString());
		lineValues.add(item.getSrsId());

		// write info columns
		List<ReferenceDataDefinition.Attribute> infoAttributes = getSamplingPointInfoAttributes();
		for (int i = 0; i < infoAttributes.size(); i++) {
			lineValues.add(item.getInfoAttribute(i));
		}
		writer.writeNext(lineValues);
	}

	public void setSamplingDesignManager(SamplingDesignManager samplingDesignManager) {
		this.samplingDesignManager = samplingDesignManager;
	}

}