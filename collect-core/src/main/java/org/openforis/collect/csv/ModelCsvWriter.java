package org.openforis.collect.csv;

import java.io.IOException;
import java.io.Writer;
import java.util.List;

import org.openforis.idm.model.Node;
import org.openforis.idm.model.Record;
import org.openforis.idm.model.expression.AbsoluteModelPathExpression;
import org.openforis.idm.model.expression.ExpressionFactory;
import org.openforis.idm.model.expression.InvalidExpressionException;
import org.openforis.idm.model.expression.internal.MissingValueException;

/**
 * @author G. Miceli
 * @deprecated replaced with idm-transform api
 */
@Deprecated
public class ModelCsvWriter extends CsvWriter {

	private DataTransformation xform;
	private AbsoluteModelPathExpression pivotExpression;
	
	public ModelCsvWriter(Writer writer, DataTransformation xform) throws IOException, InvalidExpressionException {
		super(writer);
		this.xform = xform;
		ExpressionFactory expressionFactory = new ExpressionFactory();
		this.pivotExpression = expressionFactory.createAbsoluteModelPathExpression(xform.getAxisPath());
	}

	public void printColumnHeadings() throws IOException {
		printCsvLine(xform.getColumnProvider().getColumnHeadings());
	}

	public void printRow(Node<?> n) {
		printCsvLine(xform.getColumnProvider().extractValues(n));
	}

	public int printData(Record record) throws InvalidExpressionException {
		int cnt = 0;
		try {
			List<Node<?>> rowNodes = pivotExpression.iterate(record);
			if ( rowNodes != null ) {
				for (Node<?> n : rowNodes) {
					printRow(n);
					cnt++;
				}
			}
		} catch ( MissingValueException e ) {
		}
		return cnt;
	}
}
