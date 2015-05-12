package org.openforis.idm.model.expression.internal;


import static java.util.Arrays.asList;
import static org.openforis.idm.path.Path.THIS_SYMBOL;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Locale;

import org.apache.commons.jxpath.ExpressionContext;
import org.apache.commons.jxpath.ri.model.beans.NullPointer;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.openforis.idm.geospatial.CoordinateOperationException;
import org.openforis.idm.geospatial.CoordinateOperations;
import org.openforis.idm.metamodel.Survey;
import org.openforis.idm.metamodel.validation.LookupProvider;
import org.openforis.idm.model.Coordinate;
import org.openforis.idm.model.Date;
import org.openforis.idm.model.Node;
import org.openforis.idm.model.Time;

/**
 * Custom xpath functions allowed into IDM
 *
 * @author M. Togna
 * @author D. Wiell
 * @author S. Ricci
 */
public class IDMFunctions extends CustomFunctions {
	private static final String LOCATION_ATTRIBUTE = "location";

	private enum TimeUnit {
		MINUTE, HOUR, DAY
	}
	
	public IDMFunctions(String namespace) {
		super(namespace);
		register("blank", 1, new CustomFunction() {
			public Object invoke(ExpressionContext expressionContext, Object[] objects) {
				return blank(objects[0]);
			}
		});

		register("index", 0, new CustomFunction() {
			public Object invoke(ExpressionContext expressionContext, Object[] objects) {
				return index(expressionContext);
			}
		});

		register("index", 1, new CustomFunction() {
			public Object invoke(ExpressionContext expressionContext, Object[] objects) {
				return index((Node<?>) objects[0]);
			}
		});

		register("position", 0, new CustomFunction(THIS_SYMBOL) {
			public Object invoke(ExpressionContext expressionContext, Object[] objects) {
				return position(expressionContext);
			}
		});

		register("position", 1, new CustomFunction() {
			public Object invoke(ExpressionContext expressionContext, Object[] objects) {
				return position((Node<?>) objects[0]);
			}
		});

		register("currentDate", 0, new CustomFunction() {
			public Object invoke(ExpressionContext expressionContext, Object[] objects) {
				return currentDate();
			}
		});

		register("currentTime", 0, new CustomFunction() {
			public Object invoke(ExpressionContext expressionContext, Object[] objects) {
				return currentTime();
			}
		});

		register("lookup", asList(4, 6, 8, 10), new CustomFunction() {
			public Object invoke(ExpressionContext expressionContext, Object[] objects) {
				String[] strings = toStringArray(objects);
				String[] keys = Arrays.copyOfRange(strings, 2, strings.length);
				String name = strings[0];
				String attribute = strings[1];
				return lookup(expressionContext, name, attribute, keys);
			}
		});

		register("samplingPointCoordinate", asList(1, 2, 3), new CustomFunction() {
			public Object invoke(ExpressionContext expressionContext, Object[] objects) {
				String[] strings = toStringArray(objects);
				return samplingPointCoordinateLookup(expressionContext, strings);
			}
		});

		register("samplingPointData", asList(2, 3, 4), new CustomFunction() {
			public Object invoke(ExpressionContext expressionContext, Object[] objects) {
				String[] strings = toStringArray(objects);
				String[] keys = Arrays.copyOfRange(strings, 1, strings.length);
				String attribute = strings[0];
				return samplingPointDataLookup(expressionContext, attribute, keys);
			}
		});
		
		register("distance", 2, new CustomFunction() {
			public Object invoke(ExpressionContext expressionContext, Object[] objects) {
				return distance(expressionContext, objects[0], objects[1]);
			}
		});
		
		register("datetime-diff", 5, new CustomFunction() {
			public Object invoke(ExpressionContext expressionContext, Object[] objects) {
				return dateTimeDifference(expressionContext, objects); 
//				return dateTimeDifference(expressionContext, (Date) objects[0], (Time) objects[1], 
//						(Date) objects[2], (Time) objects[3], (String) objects[4]);
			}
		});
	}

	private String[] toStringArray(Object[] objects) {
		String[] strings = new String[objects.length];
		for (int i = 0; i < objects.length; i++) {
			Object object = objects[i];
			if (object != null) {
				strings[i] = object.toString();
			}
		}
		return strings;
	}


	private static boolean blank(Object object) {
		return object == null || object instanceof NullPointer ||
				(object instanceof String && StringUtils.isBlank((String) object));
	}

	/**
	 * Returns the index of the context node
	 */
	private static int index(ExpressionContext context) {
		Node<?> node = (Node<?>) context.getContextNodePointer().getNode();
		return node.getIndex();
	}

	/**
	 * Returns the index of a node (starts from 0)
	 */
	private static int index(Node<?> node) {
		return node.getIndex();
	}

	/**
	 * Returns the position of the context node
	 */
	private static int position(ExpressionContext context) {
		Node<?> node = (Node<?>) context.getContextNodePointer().getNode();
		return node.getIndex() + 1;
	}

	/**
	 * Returns the position of a node (starts from 1)
	 */
	private static int position(Node<?> node) {
		return node.getIndex() + 1;
	}

	private static Date currentDate() {
		Date result = Date.parse(new java.util.Date());
		return result;
	}

	private static Time currentTime() {
		Time result = Time.parse(new java.util.Date());
		return result;
	}

	private static Coordinate samplingPointCoordinateLookup(ExpressionContext context, String... keys) {
		if (validateSamplingPointKeys(keys)) {
			LookupProvider lookupProvider = getLookupProvider(context);
			Survey survey = getSurvey(context);
			Coordinate coordinate = lookupProvider.lookupSamplingPointCoordinate(survey, keys);
			return coordinate;
		} else {
			return null;
		}
	}

	private static Object samplingPointDataLookup(ExpressionContext context, String attribute, String... keys) {
		if (validateSamplingPointKeys(keys)) {
			LookupProvider lookupProvider = getLookupProvider(context);
			Survey survey = getSurvey(context);
			Object data = lookupProvider.lookupSamplingPointData(survey, attribute, keys);
			return data;
		} else {
			return null;
		}
	}

	private static Object lookup(ExpressionContext context, String name, String attribute, String... keys) {
		LookupProvider lookupProvider = getLookupProvider(context);
		Survey survey = getSurvey(context);
		Object result = lookupProvider.lookup(survey, name, attribute, (Object[]) keys);
		if (result == null) {
			return null;
		} else if (LOCATION_ATTRIBUTE.equalsIgnoreCase(attribute)) {
			//convert to Coordinate
			Coordinate coordinate = Coordinate.parseCoordinate(result.toString());
			return coordinate;
		} else {
			return result;
		}
	}

	private static boolean validateSamplingPointKeys(String... keys) {
		if (keys == null || keys.length == 0) {
			return false;
		}
		for (String key : keys) {
			if (key == null) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Calculates the orthodromic distance between 2 coordinates (in meters)
	 */
	private static Double distance(ExpressionContext context, Object from, Object to) {
		if (from == null || to == null) {
			return null;
		}
		Coordinate fromC = from instanceof Coordinate ? (Coordinate) from: Coordinate.parseCoordinate(from);
		Coordinate toC = to instanceof Coordinate ? (Coordinate) to: Coordinate.parseCoordinate(to);
		if (! fromC.isComplete() || ! toC.isComplete()) {
			return null;
		}
		CoordinateOperations coordinateOperations = getSurvey(context).getContext().getCoordinateOperations();
		
		try {
			double distance = coordinateOperations.orthodromicDistance(fromC, toC);
			return distance;
		} catch (CoordinateOperationException e) {
			return null;
		}
	}

	private Object dateTimeDifference(ExpressionContext expressionContext, Object[] objects) {
		return dateTimeDifference(expressionContext, Date.fromNumericValue((Integer) objects[0]), Time.fromNumericValue((Integer) objects[1]), 
				Date.fromNumericValue((Integer) objects[2]), Time.fromNumericValue((Integer) objects[3]), (String) objects[4]);
	}
	
	private static Integer dateTimeDifference(ExpressionContext context, Date date1, Time time1, Date date2, Time time2, String timeUnit) {
		return dateTimeDifference(context, date1, time1, date2, time2, TimeUnit.valueOf(timeUnit.toUpperCase()));
	}
	
	private static Integer dateTimeDifference(ExpressionContext context, Date date1, Time time1, Date date2, Time time2, TimeUnit timeUnit) {
		if (date1.isComplete() && date2.isComplete() && time1.isComplete() && time2.isComplete()) {
			int calendarTruncateField = getCalendarField(timeUnit);
			
			Calendar cal1 = Calendar.getInstance(Locale.ENGLISH);
			cal1.setLenient(false);
			cal1.set(date1.getYear(), date1.getMonth() - 1, date1.getDay(), time1.getHour(), time1.getMinute());
			cal1 = DateUtils.truncate(cal1, calendarTruncateField);
			
			Calendar cal2 = Calendar.getInstance(Locale.ENGLISH);
			cal2.setLenient(false);
			cal2.set(date2.getYear(), date2.getMonth() - 1, date2.getDay(), time2.getHour(), time2.getMinute());
			cal2 = DateUtils.truncate(cal2, calendarTruncateField);
			
			long duration  = cal1.getTimeInMillis() - cal2.getTimeInMillis();
			
			long diff;

			switch (timeUnit) {
			case MINUTE:
				diff = java.util.concurrent.TimeUnit.MILLISECONDS.toMinutes(duration);
				break;
			case HOUR:
				diff = java.util.concurrent.TimeUnit.MILLISECONDS.toHours(duration);
				break;
			case DAY:
				diff = java.util.concurrent.TimeUnit.MILLISECONDS.toDays(duration);
				break;
			default:
				throw new IllegalArgumentException("Unsupported time unit: " + timeUnit);
			}
			return Long.valueOf(diff).intValue();
		} else {
			return null;
		}
	}

	private static int getCalendarField(TimeUnit timeUnit) {
		switch (timeUnit) {
		case MINUTE:
			return Calendar.MINUTE;
		case HOUR:
			return Calendar.HOUR_OF_DAY;
		case DAY:
			return Calendar.DAY_OF_MONTH;
		default:
			throw new IllegalArgumentException("Time unit not supported: " + timeUnit);
		}
	}
	
	private static LookupProvider getLookupProvider(ExpressionContext context) {
		ModelJXPathContext jxPathContext = (ModelJXPathContext) context.getJXPathContext();
		LookupProvider lookupProvider = jxPathContext.getLookupProvider();
		return lookupProvider;
	}

	private static Survey getSurvey(ExpressionContext context) {
		ModelJXPathContext jxPathContext = (ModelJXPathContext) context.getJXPathContext();
		Survey survey = jxPathContext.getSurvey();
		return survey;
	}
	
	public static void main(String[] args) {
		int diff = IDMFunctions.dateTimeDifference(null, new Date(2015, 10, 20), new Time(10, 30), new Date(2015, 5, 20), new Time(9, 0), TimeUnit.DAY);
		System.out.println(diff);
	}

}
