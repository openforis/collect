package org.openforis.collect.io.data.csv.columnProviders;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.openforis.collect.io.data.csv.CSVDataExportParameters;
import org.openforis.idm.model.Node;
import org.openforis.idm.model.expression.ExpressionFactory;
import org.openforis.idm.model.expression.InvalidExpressionException;
import org.openforis.idm.model.expression.ModelPathExpression;

/**
 * @author G. Miceli
 */
public class PivotExpressionColumnProvider extends ColumnProviderChain {
	private ModelPathExpression expression;

	public PivotExpressionColumnProvider(CSVDataExportParameters config, String expression, ColumnProvider... providers) {
		this(config, expression, "", Arrays.asList(providers));
	}

	public PivotExpressionColumnProvider(CSVDataExportParameters config, String expression, String headingPrefix, ColumnProvider... providers) {
		this(config, expression, headingPrefix, Arrays.asList(providers));
	}
	
	public PivotExpressionColumnProvider(CSVDataExportParameters config, String expression, String headingPrefix, List<ColumnProvider> providers) {
		super(config, null, headingPrefix, providers);
		try {
			ExpressionFactory ef = new ExpressionFactory();
			this.expression = ef.createModelPathExpression(expression);
		} catch (InvalidExpressionException e) {
			throw new IllegalArgumentException();
		}
	}

	public List<Object> extractValues(Node<?> n) {
		try {
			List<Object> v = new ArrayList<Object>();
			Node<?> axis = expression.evaluate(n, n);
			if ( axis == null ) {
				return emptyValues();
			} else {
				for (ColumnProvider p : getColumnProviders()) {
					v.addAll(p.extractValues(axis));
				}
				return v;
			}
		} catch (InvalidExpressionException e) {
			throw new IllegalArgumentException(e);
		}
	}
}
