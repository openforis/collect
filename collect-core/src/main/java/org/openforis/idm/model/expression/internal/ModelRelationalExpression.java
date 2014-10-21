/**
 * 
 */
package org.openforis.idm.model.expression.internal;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

import org.apache.commons.jxpath.Pointer;
import org.apache.commons.jxpath.ri.EvalContext;
import org.apache.commons.jxpath.ri.axes.InitialContext;
import org.apache.commons.jxpath.ri.axes.SelfContext;
import org.apache.commons.jxpath.ri.compiler.CoreOperation;
import org.apache.commons.jxpath.ri.compiler.Expression;
import org.apache.commons.jxpath.ri.model.NodePointer;
import org.apache.commons.jxpath.ri.model.VariablePointer;
import org.apache.commons.lang3.StringUtils;
import org.openforis.idm.model.NumericRange;

/**
 * @author M. Togna
 * 
 */
public class ModelRelationalExpression extends CoreOperation {

	private boolean normalizeNumbers = false;
	
	protected enum Operation {
		LT("<"), LTE("<="), GT(">"), GTE(">="), EQ("="), NOTEQ("!=");

		private String xpathOperator;

		private Operation(final String xpathOperator) {
			this.xpathOperator = xpathOperator;
		}

		@Override
		public String toString() {
			return xpathOperator;
		}

	}

	private Operation operation;

	protected ModelRelationalExpression(Operation operation, Expression... args) {
		super(args);
		this.operation = operation;
	}

	public Object computeValue(EvalContext context) {
		Object arg0 = args[0].compute(context);
		Object arg1 = args[1].compute(context);
		return compute(arg0, arg1);
	}

	/**
	 * Compare left to right.
	 * 
	 * @param left
	 *            left operand
	 * @param right
	 *            right operand
	 * @return operation success/failure
	 */
	protected boolean compute(Object left, Object right) {
		left = reduce(left);
		right = reduce(right);

		if ( left instanceof InitialContext ) {
			((InitialContext) left).reset();
		}
		if ( right instanceof InitialContext ) {
			((InitialContext) right).reset();
		}
		if ( left instanceof Iterator && right instanceof Iterator ) {
			return findMatch((Iterator<?>) left, (Iterator<?>) right);
		}
		if ( left instanceof Iterator ) {
			return containsMatch((Iterator<?>) left, right);
		}
		if ( right instanceof Iterator ) {
			return containsMatch(left, (Iterator<?>) right);
		}

		Object leftValue = getValue(left);
		Object rightValue = getValue(right);

		return internalCompute(leftValue, rightValue);
	}

	private boolean internalCompute(Object leftValue, Object rightValue) {
		if( leftValue  == null || rightValue == null) {
			return false;
		}
		
		if ( leftValue instanceof String && rightValue instanceof String ) {
			return evaluate((String) leftValue, (String) rightValue, operation);
		}
		if ( leftValue instanceof Number && rightValue instanceof NumericRange ) {
			return computeRange((Number) leftValue, (NumericRange<?>) rightValue, operation);
		}
		if ( leftValue instanceof NumericRange && rightValue instanceof Number ) {
			return computeRange((NumericRange<?>) leftValue, (Number) rightValue, operation);
		}
		if ( leftValue instanceof Number || rightValue instanceof Number ) {
			Double left = getDoubleValue(leftValue);
			Double right = getDoubleValue(rightValue);
			return evaluate(left, right, operation);
		}

		return false;
	}

	private <T extends Comparable<T>> boolean evaluate(T leftValue, T rightValue, Operation operation) {
		int compare = leftValue.compareTo(rightValue);
		
		switch ( operation ) {
			case EQ :
				return compare == 0;
			case NOTEQ :
				return compare != 0;
			case GT :
				return compare > 0;
			case GTE :
				return compare >= 0;
			case LT :
				return compare < 0;
			case LTE :
				return compare <= 0;
			default :
				return false;
		}
	}

	// min > x && max < x
	private boolean computeRange(NumericRange<? extends Number> leftValue, Number rightValue, Operation operation) {
		Number min = leftValue.getFrom();
		Number max = leftValue.getTo();

		switch ( operation ) {
			case EQ :
				return computeNumber(min, rightValue, operation) && computeNumber(min, rightValue, operation);
			case NOTEQ :
				return !(computeNumber(min, rightValue, operation) && computeNumber(min, rightValue, operation));
			case GT :
			case GTE :
				return computeNumber(min, rightValue, operation);
			case LT :
			case LTE :
				return computeNumber(max, rightValue, operation);
			default :
				return false;
		}
	}

	// a > min && b < max
	private boolean computeRange(Number leftValue, NumericRange<? extends Number> rightValue, Operation operation) {
		Number min = rightValue.getFrom();
		Number max = rightValue.getTo();

		switch ( operation ) {
			case EQ :
				return computeNumber(leftValue, min, operation) && computeNumber(leftValue, max, operation);
			case NOTEQ :
				return !(computeNumber(leftValue, min, operation) && computeNumber(leftValue, max, operation));
			case GT :
			case GTE :
				return computeNumber(leftValue, min, operation);
			case LT :
			case LTE :
				return computeNumber(leftValue, max, operation);
			default :
				return false;
		}
	}

	private boolean computeNumber(Number leftValue, Number rightValue, Operation operation) {
		double left = leftValue.doubleValue();
		double right = rightValue.doubleValue();

		switch ( operation ) {
			case EQ :
				return left == right;
			case NOTEQ :
				return left != right;
			case GT :
				return left > right;
			case GTE :
				return left >= right;
			case LT :
				return left < right;
			case LTE :
				return left <= right;
			default :
				return false;
		}
	}

	private double getDoubleValue(Object object) {
		if ( object instanceof Number ) {
			return ((Number) object).doubleValue();
		}

		if ( object instanceof Boolean ) {
			return ((Boolean) object).booleanValue() ? 0.0 : 1.0;
		}
		if ( object instanceof String ) {
			if ( StringUtils.isBlank((String) object) ) {
				return 0.0;
			}
			try {
				return Double.parseDouble((String) object);
			} catch ( NumberFormatException ex ) {
				return Double.NaN;
			}
		}
		if ( object instanceof NodePointer ) {
			return getDoubleValue(((NodePointer) object).getValue());
		}
		if ( object instanceof EvalContext ) {
			EvalContext ctx = (EvalContext) object;
			Pointer ptr = ctx.getSingleNodePointer();
			return ptr == null ? Double.NaN : getDoubleValue(ptr);
		}
		throw new RuntimeException("Cannot convert " + object.toString() + " to a double value");
	}

	private Object getValue(Object object) {
		if ( object instanceof Number || object instanceof NumericRange || object instanceof String ) {
			return object;
		}
		if ( object instanceof Boolean ) {
			return ((Boolean) object).booleanValue() ? 0.0 : 1.0;
		}
		if ( object instanceof NodePointer ) {
			if(object instanceof VariablePointer && normalizeNumbers){
				ModelNodePointer valuePointer = (ModelNodePointer) ((NodePointer) object).getValuePointer();
				valuePointer.setNormalizeNumbers(true);
			}
			return getValue(((NodePointer) object).getValue());
		}
		if ( object instanceof EvalContext ) {
			EvalContext ctx = (EvalContext) object;
			Pointer ptr = ctx.getSingleNodePointer();
			return ptr == null ? Double.NaN : getValue(ptr);
		}
		return null;
	}

	// private String getStringValue(Object object) {
	// if ( object instanceof Pointer ) {
	// object = ((Pointer) object).getValue();
	// }
	//
	// return object.toString();
	// }

	@Override
	public String getSymbol() {
		return operation.toString();
	}

	@Override
	protected boolean isSymmetric() {
		return false;
	}

	@Override
	protected int getPrecedence() {
		return RELATIONAL_EXPR_PRECEDENCE;
	}

	/**
	 * Reduce an operand for comparison.
	 * 
	 * @param o
	 *            Object to reduce
	 * @return reduced operand
	 */
	private Object reduce(Object o) {
		if ( o instanceof SelfContext ) {
			o = ((EvalContext) o).getSingleNodePointer();
		}
		if ( o instanceof Collection ) {
			o = ((Collection<?>) o).iterator();
		}
		return o;
	}

	/**
	 * Learn whether there is an intersection between two Iterators.
	 * 
	 * @param lit
	 *            left Iterator
	 * @param rit
	 *            right Iterator
	 * @return whether a match was found
	 */
	private boolean findMatch(Iterator<?> lit, Iterator<?> rit) {
		HashSet<Object> left = new HashSet<Object>();
		while ( lit.hasNext() ) {
			left.add(lit.next());
		}
		while ( rit.hasNext() ) {
			if ( containsMatch(left.iterator(), rit.next()) ) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Learn whether any element returned from an Iterator matches a given value.
	 * 
	 * @param it
	 *            Iterator
	 * @param value
	 *            to look for
	 * @return whether a match was found
	 */
	private boolean containsMatch(Iterator<?> it, Object value) {
		while ( it.hasNext() ) {
			Object element = it.next();
			if ( compute(element, value) ) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Learn whether any element returned from an Iterator matches a given value.
	 * 
	 * @param it
	 *            Iterator
	 * @param value
	 *            to look for
	 * @return whether a match was found
	 */
	private boolean containsMatch(Object value, Iterator<?> it) {
		while ( it.hasNext() ) {
			Object element = it.next();
			if ( compute(value, element) ) {
				return true;
			}
		}
		return false;
	}

	void setNormalizeNumbers(boolean normalizeNumbers) {
		this.normalizeNumbers = normalizeNumbers;
	}
	
	
	boolean isNormalizeNumbers() {
		return normalizeNumbers;
	}
}
