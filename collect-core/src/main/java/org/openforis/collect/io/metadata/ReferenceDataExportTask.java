package org.openforis.collect.io.metadata;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.commons.io.OpenForisIOUtils;
import org.openforis.commons.io.csv.CsvWriter;
import org.openforis.commons.io.excel.ExcelFlatValuesWriter;
import org.openforis.commons.io.flat.FlatDataWriter;
import org.openforis.concurrency.Task;

public abstract class ReferenceDataExportTask extends Task {

	// parameters
	protected CollectSurvey survey;
	protected ReferenceDataExportOutputFormat outputFormat = ReferenceDataExportOutputFormat.CSV;
	protected OutputStream outputStream;
	protected boolean closeWriterOnEnd = true; // False if writing an entry in a zipOutputStream

	// temporary
	protected FlatDataWriter writer;

	@Override
	protected void execute() throws Throwable {
		initWriter();

		writer.writeHeaders(getHeaders());

		if (getTotalItems() > 0) {
			writeItems();
		}
	}

	@Override
	protected void afterExecuteInternal() {
		super.afterExecuteInternal();
		if (closeWriterOnEnd) {
			IOUtils.closeQuietly(writer);
		} else if (writer != null) {
			try {
				writer.flush();
			} catch (IOException e) {
				// Ignore it
			}
		}
	}

	protected abstract List<String> getHeaders();

	protected abstract void writeItems();

	private void initWriter() throws UnsupportedEncodingException {
		if (outputFormat == ReferenceDataExportOutputFormat.CSV) {
			OutputStreamWriter osWriter = new OutputStreamWriter(outputStream, Charset.forName(OpenForisIOUtils.UTF_8));
			this.writer = new CsvWriter(osWriter, ',', '"');
		} else {
			this.writer = new ExcelFlatValuesWriter(outputStream);
		}
	}

	public void setSurvey(CollectSurvey survey) {
		this.survey = survey;
	}

	public void setOutputFormat(ReferenceDataExportOutputFormat outputFormat) {
		this.outputFormat = outputFormat;
	}

	public void setOutputStream(OutputStream outputStream) {
		this.outputStream = outputStream;
	}

	public void setCloseWriterOnEnd(boolean closeWriterOnEnd) {
		this.closeWriterOnEnd = closeWriterOnEnd;
	}
}
