package org.openforis.collect.manager.dataexport.samplingdesign;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openforis.collect.manager.SamplingDesignManager;
import org.openforis.collect.manager.samplingdesignimport.SamplingDesignFileColumn;
import org.openforis.collect.model.SamplingDesignItem;
import org.openforis.collect.model.SamplingDesignSummaries;
import org.openforis.commons.io.csv.CsvWriter;

/**
 * 
 * @author S. Ricci
 *
 */
public class SamplingDesignExportProcess {
	
	private final Log log = LogFactory.getLog(SamplingDesignExportProcess.class);
	
	private SamplingDesignManager samplingDesignManager;
	
	public SamplingDesignExportProcess(SamplingDesignManager samplingDesignManager) {
		super();
		this.samplingDesignManager = samplingDesignManager;
	}

	public void exportToCSV(OutputStream out, int surveyId, boolean work) {
		CsvWriter writer = null;
		try {
			writer = new CsvWriter(out);
			SamplingDesignSummaries summaries = work ? 
				samplingDesignManager.loadBySurveyWork(surveyId): 
				samplingDesignManager.loadBySurvey(surveyId);;
			ArrayList<String> colNames = new ArrayList<String>();
			colNames.addAll(Arrays.asList(SamplingDesignFileColumn.LEVEL_COLUMN_NAMES));
			colNames.add(SamplingDesignFileColumn.X.getColumnName());
			colNames.add(SamplingDesignFileColumn.Y.getColumnName());
			colNames.add(SamplingDesignFileColumn.SRS_ID.getColumnName());
			writer.writeHeaders(colNames.toArray(new String[0]));
			List<SamplingDesignItem> items = summaries.getRecords();
			for (SamplingDesignItem item : items) {
				writeSummary(writer, item);
			}
		} catch (Exception e) {
			log.error(e);
		} finally {
			IOUtils.closeQuietly(writer);
		}
	}

	protected void writeSummary(CsvWriter writer, SamplingDesignItem item) {
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
		writer.writeNext(lineValues.toArray(new String[0]));
	}

}
