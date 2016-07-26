package org.openforis.collect.io.data.csv;

import java.io.IOException;
import java.io.Writer;
import java.util.List;

import org.openforis.collect.io.data.NodeFilter;
import org.openforis.commons.io.csv.CsvWriter;
import org.openforis.idm.model.Node;
import org.openforis.idm.model.Record;
import org.openforis.idm.model.expression.AbsoluteModelPathExpression;
import org.openforis.idm.model.expression.ExpressionFactory;
import org.openforis.idm.model.expression.InvalidExpressionException;

/**
 * @author G. Miceli
 */
public class ModelCsvWriter extends CsvWriter {

	private DataTransformation xform;
	private AbsoluteModelPathExpression pivotExpression;
	private NodeFilter nodeFilter;
	
	public ModelCsvWriter(Writer writer, DataTransformation xform) throws IOException, InvalidExpressionException {
		this(writer, xform, null);
	}
	
	public ModelCsvWriter(Writer writer, DataTransformation xform, NodeFilter nodeFilter) throws IOException, InvalidExpressionException {
		super(writer, ',', '"');
		this.xform = xform;
		this.nodeFilter = nodeFilter;
		ExpressionFactory expressionFactory = new ExpressionFactory();
		this.pivotExpression = expressionFactory.createAbsoluteModelPathExpression(xform.getAxisPath());
	}

	public void printColumnHeadings() throws IOException {
		List<String> columnHeadings = xform.getColumnProvider().getColumnHeadings();
		writeHeaders(columnHeadings);
	}

	public void printRow(Node<?> n) {
		List<String> values = xform.getColumnProvider().extractValues(n);
		writeNext(values);
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
