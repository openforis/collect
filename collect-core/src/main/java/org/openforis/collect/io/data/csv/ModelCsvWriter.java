package org.openforis.collect.io.data.csv;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

import org.openforis.collect.io.data.NodeFilter;
import org.openforis.commons.io.OpenForisIOUtils;
import org.openforis.commons.io.csv.CsvWriter;
import org.openforis.idm.model.expression.InvalidExpressionException;

/**
 * @author G. Miceli
 */
public class ModelCsvWriter extends ModelWriter {

	private static final char DEFAULT_SEPARATOR = ',';
	private static final char DEFAULT_QUOTECHAR = '"';

	public ModelCsvWriter(OutputStream output, DataTransformation xform) throws IOException, InvalidExpressionException {
		this(output, xform, null);
	}
	
	public ModelCsvWriter(OutputStream output, DataTransformation xform, NodeFilter nodeFilter) throws IOException, InvalidExpressionException {
		super(output, xform, nodeFilter);
	}
	
	@Override
	protected void initializeFlatDataWriter(OutputStream output) {
		try {
			flatDataWriter = new CsvWriter(output, OpenForisIOUtils.UTF_8, 
					DEFAULT_SEPARATOR, DEFAULT_QUOTECHAR);
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}

}