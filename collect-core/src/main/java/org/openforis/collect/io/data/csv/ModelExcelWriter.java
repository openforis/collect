package org.openforis.collect.io.data.csv;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

import org.openforis.collect.io.data.NodeFilter;
import org.openforis.commons.io.excel.ExcelFlatValuesWriter;
import org.openforis.idm.model.expression.InvalidExpressionException;

/**
 * @author G. Miceli
 */
public class ModelExcelWriter extends ModelWriter {

	public ModelExcelWriter(OutputStream output, DataTransformation xform) throws IOException, InvalidExpressionException {
		this(output, xform, null);
	}
	
	public ModelExcelWriter(OutputStream output, DataTransformation xform, NodeFilter nodeFilter) throws IOException, InvalidExpressionException {
		super(output, xform, nodeFilter);
	}
	
	@Override
	protected void initializeFlatDataWriter(OutputStream output) {
		try {
			flatDataWriter = new ExcelFlatValuesWriter(output);
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}

}