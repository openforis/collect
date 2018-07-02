package org.openforis.collect.io.data.csv;

import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import org.openforis.collect.io.data.NodeFilter;
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
		List<String> columnHeadings = xform.getColumnProvider().getColumnHeadings();
		flatDataWriter.writeHeaders(columnHeadings);
	}

	public void printRow(Node<?> n) {
		List<String> values = xform.getColumnProvider().extractValues(n);
		flatDataWriter.writeNext(values);
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
