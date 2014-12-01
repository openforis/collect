package org.openforis.idm.model.expression.internal;

import org.apache.commons.jxpath.ExpressionContext;
import org.apache.commons.lang3.ObjectUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * @author S. Ricci
 * @author D. Wiell
 */
public class MathFunctions extends CustomFunctions {

	public MathFunctions(String namespace) {
		super(namespace);
		register("PI", 0, new CustomFunction() {
			public Object invoke(ExpressionContext expressionContext, Object[] objects) {
				return PI();
			}
		});
		register("abs", 1, new SingleArgMathFunction() {
			public Number execute(Number number) {
				return abs(number);
			}
		});
		register("pow", 2, new CustomFunction() {
			public Object invoke(ExpressionContext expressionContext, Object[] objects) {
				return pow(objects[0], objects[1]);
			}
		});
		register("min", 1, new CustomFunction() {
			public Object invoke(ExpressionContext expressionContext, Object[] objects) {
				return min(objects[0]);
			}
		});
		register("max", 1, new CustomFunction() {
			public Object invoke(ExpressionContext expressionContext, Object[] objects) {
				return max(objects[0]);
			}
		});
		register("rad", 1, new SingleArgMathFunction() {
			public Number execute(Number number) {
				return rad(number);
			}
		});
		register("deg", 1, new SingleArgMathFunction() {
			public Number execute(Number number) {
				return deg(number);
			}
		});
		register("sin", 1, new SingleArgMathFunction() {
			public Number execute(Number number) {
				return sin(number);
			}
		});
		register("sinrad", 1, new SingleArgMathFunction() {
			public Number execute(Number number) {
				return sinrad(number);
			}
		});
		register("cos", 1, new SingleArgMathFunction() {
			public Number execute(Number number) {
				return cos(number);
			}
		});
		register("cosrad", 1, new SingleArgMathFunction() {
			public Number execute(Number number) {
				return cosrad(number);
			}
		});
		register("tan", 1, new SingleArgMathFunction() {
			public Number execute(Number number) {
				return tan(number);
			}
		});
		register("tanrad", 1, new SingleArgMathFunction() {
			public Number execute(Number number) {
				return tanrad(number);
			}
		});
		register("tanrad", 1, new SingleArgMathFunction() {
			public Number execute(Number number) {
				return tanrad(number);
			}
		});
	}

	private static double PI() {
		return Math.PI;
	}

	private static Double abs(Number value) {
		return Math.abs(value.doubleValue());
	}

	@SuppressWarnings("unchecked")
	private static Object pow(Object base, Object exponent) {
		if (base == null || exponent == null) {
			return null;
		}
		if (!(exponent instanceof Number)) {
			throw new IllegalArgumentException("Exponent not a number");
		}
		Number exponentValue = (Number) exponent;
		return base instanceof Iterable
				? powForCollectionOfBases((Iterable<Object>) base, exponentValue)
				: powForSingleBase(base, exponentValue);
	}

	private static Iterable<Number> powForCollectionOfBases(Iterable<Object> base, Number exponent) {
		List<Number> result = new ArrayList<Number>();
		for (Object number : base) {
			result.add(
					powForSingleBase(number, exponent)
			);
		}
		return result;
	}

	private static Double powForSingleBase(Object base, Number exponent) {
		if (!(base instanceof Number)) {
			throw new IllegalArgumentException("base is not a number");
		}
		Number baseNumber = (Number) base;
		return Math.pow(baseNumber.doubleValue(), exponent.doubleValue());
	}

	private static Object min(Object values) {
		return compare(values, true);
	}

	private static Object max(Object values) {
		return compare(values, false);
	}

	@SuppressWarnings("unchecked")
	private static Object compare(Object value, boolean min) {
		if (value instanceof Collection) {
			return compare((Collection<Object>) value, min);
		} else {
			return value;
		}
	}

	private static Object compare(Collection<Object> values, boolean min) {
		if (values == null || values.isEmpty()) {
			return null;
		}
		Object result = null;
		Iterator<Object> it = values.iterator();

		while (it.hasNext() && result == null) {
			result = it.next();
		}
		while (it.hasNext()) {
			Object value = it.next();
			if (value != null) {
				@SuppressWarnings("unchecked")
				int compareTo = ObjectUtils.compare((Comparable<Object>) value, (Comparable<Object>) result);
				if ((min && compareTo < 0) ||
						(!min && compareTo > 0)) {
					result = value;
				}
			}
		}
		return result;
	}

	private static Double rad(Number angle) {
		return Math.toRadians(angle.doubleValue());
	}

	private static Double deg(Number angleInRadians) {
		return Math.toDegrees(angleInRadians.doubleValue());
	}

	private static Double sin(Number angleInDegrees) {
		double angleInRadians = Math.toRadians(angleInDegrees.doubleValue());
		return Math.sin(angleInRadians);
	}

	private static Double sinrad(Number angleInRadians) {
		return Math.sin(angleInRadians.doubleValue());
	}

	private static Double cos(Number angleInDegrees) {
		double angleInRadians = Math.toRadians(angleInDegrees.doubleValue());
		return Math.cos(angleInRadians);
	}

	private static Double cosrad(Number angleInRadians) {
		return angleInRadians == null ? null : Math.cos(angleInRadians.doubleValue());
	}

	private static Double tan(Number angleInDegrees) {
		double angleInRadians = Math.toRadians(angleInDegrees.doubleValue());
		return Math.tan(angleInRadians);
	}

	private static Double tanrad(Number angleInRadians) {
		return Math.tan(angleInRadians.doubleValue());
	}


	private abstract static class SingleArgMathFunction extends CustomFunction {
		@SuppressWarnings("unchecked")
		public final Object invoke(ExpressionContext context, Object[] objects) {
			Object numberOrNumbers = objects[0];
			return numberOrNumbers instanceof Iterable
					? executeOnNumbers((Iterable<Object>) numberOrNumbers)
					: executeOnNumber(numberOrNumbers);
		}

		public abstract Number execute(Number number);

		private Iterable<Number> executeOnNumbers(Iterable<Object> numbers) {
			List<Number> result = new ArrayList<Number>();
			for (Object number : numbers)
				result.add(
						executeOnNumber(number)
				);
			return result;
		}

		private Number executeOnNumber(Object number) {
			if (number == null) {
				return null;
			}
			if (!(number instanceof Number)) {
				throw new IllegalArgumentException("Argument not a number");
			}
			return execute((Number) number);
		}
	}
}
