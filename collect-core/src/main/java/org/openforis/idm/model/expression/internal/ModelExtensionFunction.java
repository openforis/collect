/**
 * 
 */
package org.openforis.idm.model.expression.internal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.jxpath.Function;
import org.apache.commons.jxpath.JXPathFunctionNotFoundException;
import org.apache.commons.jxpath.NodeSet;
import org.apache.commons.jxpath.ri.EvalContext;
import org.apache.commons.jxpath.ri.QName;
import org.apache.commons.jxpath.ri.axes.NodeSetContext;
import org.apache.commons.jxpath.ri.compiler.Expression;
import org.apache.commons.jxpath.ri.compiler.ExtensionFunction;

/**
 * @author M. Togna
 * 
 */
public class ModelExtensionFunction extends ExtensionFunction {

	public ModelExtensionFunction(QName functionName, Expression[] args) {
		super(functionName, args);
	}

	@Override
	public Object computeValue(EvalContext context) {
		Object[] parameters = null;
		if ( args != null ) {
			parameters = new Object[args.length];
			for ( int i = 0 ; i < args.length ; i++ ) {
				Expression expression = args[i];
				Iterator<?> computedValues = expression.iterate(context);
				parameters[i] = convert(computedValues);
			}
		}

		Object result = invoke(context, parameters);
		return result instanceof NodeSet ? new NodeSetContext(context, (NodeSet) result) : result;

	}

	private Object invoke(EvalContext context, Object[] parameters) {
		Function function = context.getRootContext().getFunction(getFunctionName(), parameters);
		if ( function == null ) {
			throw new JXPathFunctionNotFoundException("No such function: " + getFunctionName() + Arrays.asList(parameters));
		}
		Object result = function.invoke(context, parameters);
		return result;
	}

	public String getPrefix() {
		QName functionName = getFunctionName();
		return functionName.getPrefix();
	}

	public String getName() {
		QName functionName = getFunctionName();
		return functionName.getName();
	}

	public String getFullName() {
		QName functionName = getFunctionName();
		return functionName.getPrefix() + ":" + functionName.getName();
	}

	/**
	 * Convert any incoming context to a value.
	 * 
	 * @param object
	 *            Object to convert
	 * @return context value or <code>object</code> unscathed.
	 */
	private Object convert(Iterator<?> it) {
		List<Object> result = new ArrayList<Object>();
		while ( it.hasNext() ) {
			Object obj = it.next();
			result.add(obj);
		}
		switch ( result.size() ) {
		case 0:
			return null;
		case 1:
			return result.get(0);
		default:
			return result;
		}
	}

}
