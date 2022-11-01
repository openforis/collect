package org.openforis.collect.io.data.csv;

import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.openforis.collect.io.data.NodeFilter;
import org.openforis.collect.io.data.csv.Column.DataType;
import org.openforis.commons.io.flat.Field;
import org.openforis.commons.io.flat.Field.Type;
import org.openforis.commons.io.flat.FlatDataWriter;
import org.openforis.idm.model.Node;
import org.openforis.idm.model.Record;
import org.openforis.idm.model.expression.AbsoluteModelPathExpression;
import org.openforis.idm.model.expression.ExpressionFactory;
import org.openforis.idm.model.expression.InvalidExpressionException;

/**
 * @author G. Miceli
 */
public abstract class ModelWriter implements Closeable {

	private DataTransformation xform;
	private AbsoluteModelPathExpression pivotExpression;
	private NodeFilter nodeFilter;
	protected FlatDataWriter flatDataWriter;
	
	public ModelWriter(OutputStream output, DataTransformation xform) throws IOException, InvalidExpressionException {
		this(output, xform, null);
	}
	
	public ModelWriter(OutputStream output, DataTransformation xform, NodeFilter nodeFilter) throws IOException, InvalidExpressionException {
		this.xform = xform;
		this.nodeFilter = nodeFilter;
		ExpressionFactory expressionFactory = new ExpressionFactory();
		this.pivotExpression = expressionFactory.createAbsoluteModelPathExpression(xform.getAxisPath());
		initializeFlatDataWriter(output);
	}

	protected abstract void initializeFlatDataWriter(OutputStream output);
	
	public void close() throws IOException {
		flatDataWriter.close();
	}
	
	public void flush() throws IOException {
		flatDataWriter.flush();
	}

	public void printColumnHeadings() throws IOException {
		List<Column> columns = xform.getColumnProvider().getColumns();
		List<Field> fields = new ArrayList<Field>(columns.size());
		List<String> headers = new ArrayList<String>(columns.size());
		for (Column column : columns) {
			headers.add(column.getHeader());
			fields.add(new Field(column.getHeader(), toFieldType(column.getDataType())));
		}
		flatDataWriter.writeHeaders(headers);
		
		// set fields after writing headers: during header writing, all fields will be considered as strings
		flatDataWriter.setFields(fields);
	}

	private Type toFieldType(DataType dataType) {
		switch(dataType) {
		case DATE:
			return Type.DATE;
		case DECIMAL:
			return Type.DECIMAL;
		case INTEGER:
			return Type.INTEGER;
		case IMAGE_BYTE_ARRAY:
			return Type.IMAGE_BYTE_ARRAY;
		case STRING:
		default:
			return Type.STRING;
		}
	}

	public void printRow(Node<?> n) {
		List<Object> values = xform.getColumnProvider().extractValues(n);
		flatDataWriter.writeNext(values.toArray(new Object[values.size()]));
	}

	public int printData(Record record) throws InvalidExpressionException {
		int cnt = 0;
		List<Node<?>> rowNodes = pivotExpression.iterate(record);
		if ( rowNodes != null ) {
			for (Node<?> n : rowNodes) {
				if (nodeFilter == null || nodeFilter.accept(n)) {
					printRow(n);
				}
				cnt++;
			}
		}
		return cnt;
	}
}
