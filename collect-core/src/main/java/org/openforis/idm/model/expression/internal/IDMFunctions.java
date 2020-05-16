package org.openforis.idm.model.expression.internal;


import static java.util.Arrays.asList;
import static org.openforis.idm.path.Path.THIS_SYMBOL;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.apache.commons.jxpath.ExpressionContext;
import org.apache.commons.jxpath.ri.compiler.Constant;
import org.apache.commons.jxpath.ri.compiler.Expression;
import org.apache.commons.jxpath.ri.model.beans.NullPointer;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.openforis.collect.service.CollectSpeciesListService;
import org.openforis.commons.collection.CollectionUtils;
import org.openforis.commons.collection.Predicate;
import org.openforis.commons.lang.Numbers;
import org.openforis.idm.metamodel.Languages;
import org.openforis.idm.metamodel.Languages.Standard;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.ReferenceDataSchema.ReferenceDataDefinition.Attribute;
import org.openforis.idm.metamodel.ReferenceDataSchema.SamplingPointDefinition;
import org.openforis.idm.metamodel.ReferenceDataSchema.TaxonomyDefinition;
import org.openforis.idm.metamodel.SpeciesListService;
import org.openforis.idm.metamodel.Survey;
import org.openforis.idm.metamodel.SurveyObject;
import org.openforis.idm.metamodel.TaxonAttributeDefinition;
import org.openforis.idm.metamodel.expression.ExpressionValidator.ExpressionValidationResult;
import org.openforis.idm.metamodel.expression.ExpressionValidator.ExpressionValidationResultFlag;
import org.openforis.idm.metamodel.validation.LookupProvider;
import org.openforis.idm.model.Coordinate;
import org.openforis.idm.model.Date;
import org.openforis.idm.model.Node;
import org.openforis.idm.model.Time;
import org.openforis.idm.model.expression.ExpressionFactory;

/**
 * Custom xpath functions allowed into IDM
 *
 * @author M. Togna
 * @author D. Wiell
 * @author S. Ricci
 */
public class IDMFunctions extends CustomFunctions {
	
	public static final String SAMPLING_POINT_COORDINATE_FUNCTION_NAME = "samplingPointCoordinate";
	public static final String LATLONG_FUNCTION_NAME = "latlong";
	
	private static final String LOCATION_ATTRIBUTE = "location";

	private enum TimeUnit {
		MINUTE, HOUR, DAY
	}
	
	public IDMFunctions(String namespace) {
		super(namespace);
		
		register("array", new CustomFunction() {
			public Object invoke(ExpressionContext expressionContext, Object[] objects) {
				return objects;
			}
		});
		
		register("blank", new CustomFunction(1) {
			public Object invoke(ExpressionContext expressionContext, Object[] objects) {
				return blank(objects[0]);
			}
		});
		
		register("not-blank", new CustomFunction(1) {
			public Object invoke(ExpressionContext expressionContext, Object[] objects) {
				return ! blank(objects[0]);
			}
		});

		register("index", new CustomFunction(0) {
			public Object invoke(ExpressionContext expressionContext, Object[] objects) {
				return index(expressionContext);
			}
		});

		register("index", new CustomFunction(1) {
			public Object invoke(ExpressionContext expressionContext, Object[] objects) {
				return index((Node<?>) objects[0]);
			}
		});

		register("position", new CustomFunction(0, THIS_SYMBOL) {
			public Object invoke(ExpressionContext expressionContext, Object[] objects) {
				return position(expressionContext);
			}
		});

		register("position", new CustomFunction(1) {
			public Object invoke(ExpressionContext expressionContext, Object[] objects) {
				return position((Node<?>) objects[0]);
			}
		});

		register("distinct-values", new CustomFunction(1) {
			public Object invoke(ExpressionContext expressionContext, Object[] objects) {
				return distinctValues(objects[0]);
			}
		});

		register("count-distinct", new CustomFunction(1) {
			public Object invoke(ExpressionContext expressionContext, Object[] objects) {
				return countDistinct(objects[0]);
			}
		});

		register("contains", new CustomFunction(2) {
			public Object invoke(ExpressionContext expressionContext, Object[] objects) {
				return contains(objects[0], objects[1]);
			}
		});
		
		register("currentDate", new CustomFunction(0) {
			public Object invoke(ExpressionContext expressionContext, Object[] objects) {
				return currentDate();
			}
		});

		register("currentTime", new CustomFunction(0) {
			public Object invoke(ExpressionContext expressionContext, Object[] objects) {
				return currentTime();
			}
		});

		register("lookup", new CustomFunction(asList(4, 6, 8, 10)) {
			public Object invoke(ExpressionContext expressionContext, Object[] objects) {
				String[] strings = toStringArray(objects);
				String[] keys = Arrays.copyOfRange(strings, 2, strings.length);
				String name = strings[0];
				String attribute = strings[1];
				return lookup(expressionContext, name, attribute, keys);
			}
		});

		register(SAMPLING_POINT_COORDINATE_FUNCTION_NAME, new CustomFunction(asList(1, 2, 3)) {
			public Object invoke(ExpressionContext expressionContext, Object[] objects) {
				String[] strings = toStringArray(objects);
				return samplingPointCoordinateLookup(expressionContext, strings);
			}
		});

		register("samplingPointData", new CustomFunction(asList(1, 2, 3, 4)) {
			public Object invoke(ExpressionContext expressionContext, Object[] objects) {
				String[] strings = toStringArray(objects);
				String[] keys = Arrays.copyOfRange(strings, 1, strings.length);
				String attribute = strings[0];
				return samplingPointDataLookup(expressionContext, attribute, keys);
			}
			protected ExpressionValidationResult performArgumentValidation(NodeDefinition contextNodeDef,
					Expression[] arguments) {
				Expression firstArgument = arguments[0];
				Survey survey = contextNodeDef.getSurvey();
				SamplingPointDefinition samplingPointDefinition = survey.getReferenceDataSchema().getSamplingPointDefinition();
				if (firstArgument instanceof Constant) {
					Object val = ((Constant) firstArgument).computeValue(null);
					if (val instanceof String) {
						Attribute attr = samplingPointDefinition.getAttribute((String) val);
						if (attr != null && ! attr.isKey()) {
							return new ExpressionValidationResult();
						}
					}
				}
				return new ExpressionValidationResult(ExpressionValidationResultFlag.ERROR, 
						String.format("First argument must be a valid sampling point attribute (valid attributes are: %s)",
								samplingPointDefinition.getAttributeNames()));
			}
		});
		
		register("speciesListData", new CustomFunction(3) {
			public Object invoke(ExpressionContext expressionContext, Object[] objects) {
				String[] strings = toStringArray(objects);
				String taxonomyName = strings[0];
				String attribute = strings[1];
				String speciesCode = strings[2];
				
				if (StringUtils.isAnyBlank(taxonomyName, attribute, speciesCode)) {
					return null;
				} else {
					Survey survey = getSurvey(expressionContext);
					SpeciesListService speciesListService = getSpeciesListService(expressionContext);
					return speciesListService.loadSpeciesListData(survey, taxonomyName, attribute, speciesCode);
				}
			}
			@Override
			protected ExpressionValidationResult performArgumentValidation(NodeDefinition contextNodeDef,
					Expression[] arguments) {
				Expression speciesListNameExpr = arguments[0];
				ExpressionValidationResult validationResult = validateSpeciesListName(contextNodeDef, speciesListNameExpr);
				if (validationResult.isOk()) {
					String speciesListName = (String) ((Constant) speciesListNameExpr).computeValue(null);
					Expression attributeNameExpr = arguments[1];
					validationResult = validateAttribute(contextNodeDef, speciesListName, attributeNameExpr);
					if (validationResult.isOk()) {
						Expression speciesExpr = arguments[2];
						return validateSpeciesCode(contextNodeDef, speciesExpr);
					}
				}
				return validationResult;
			}
			
			private ExpressionValidationResult validateSpeciesListName(NodeDefinition contextNodeDef,
					Expression expression) {
				String taxonomyName = "";
				if (expression instanceof Constant) {
					Object val = ((Constant) expression).computeValue(null);
					if (val instanceof String) {
						taxonomyName = (String) val;
						Survey survey = contextNodeDef.getSurvey();
						List<String> speciesListNames = survey.getContext().getSpeciesListService().loadSpeciesListNames(survey);
						if (speciesListNames.contains(taxonomyName)) {
							return new ExpressionValidationResult();
						}
					}
				}
				return new ExpressionValidationResult(ExpressionValidationResultFlag.ERROR, 
						String.format("First argument (\"%s\") is not a valid taxonomy name", taxonomyName));
			}
			
			private ExpressionValidationResult validateAttribute(NodeDefinition contextNodeDef, String taxonomyName,
					Expression attributeNameExpr) {
				try {
					String attributeName = extractConstantValue(attributeNameExpr);
					if (Arrays.binarySearch(CollectSpeciesListService.GENERIC_ATTRIBUTES, attributeName) >= 0 
							|| isExtraAttribute(contextNodeDef, taxonomyName, attributeName)
							|| isLanguageCode(attributeName)) {
						return new ExpressionValidationResult();
					} else {
						return new ExpressionValidationResult(ExpressionValidationResultFlag.ERROR, 
								String.format("Second argument (\"%s\") is not a valid attribute for this taxonomy."
										+ "\nExpected values are: \n%s", 
										attributeName, Arrays.asList(
												StringUtils.join(CollectSpeciesListService.GENERIC_ATTRIBUTES, ", "),
												"\n",
												getExtraAttributeNames(contextNodeDef, taxonomyName),
												"\n",
												"ISO 639-3 language code (for vernacular names. E.g. eng, swa, fra)"
								)));
					}
				} catch (Exception e) {
					return new ExpressionValidationResult(ExpressionValidationResultFlag.ERROR, 
							String.format("Error in second argument: %s", e.getMessage()));
				}
			}
			
			private boolean isLanguageCode(String value) {
				return Languages.exists(Standard.ISO_639_3, value);
			}
			
			private boolean isExtraAttribute(SurveyObject contextNodeDef, String taxonomyName, String attributeName) {
				Survey survey = contextNodeDef.getSurvey();
				TaxonomyDefinition taxonDefinition = survey.getReferenceDataSchema().getTaxonomyDefinition(taxonomyName);
				Attribute attribute = taxonDefinition.getAttribute(attributeName);
				return attribute != null;
			}
			
			private List<String> getExtraAttributeNames(SurveyObject contextNodeDef, String taxonomyName) {
				Survey survey = contextNodeDef.getSurvey();
				TaxonomyDefinition taxonDefinition = survey.getReferenceDataSchema().getTaxonomyDefinition(taxonomyName);
				return taxonDefinition.getAttributeNames();
			}
			
			private ExpressionValidationResult validateSpeciesCode(NodeDefinition contextNodeDef,
					Expression expression) {
				if (expression instanceof ModelLocationPath) {
					ExpressionFactory expressionFactory = contextNodeDef.getSurvey().getContext().getExpressionFactory();
					Set<String> referencedPaths = expressionFactory.getReferencedPathEvaluator().determineReferencedPaths(expression);
					for (String referencedPath : referencedPaths) {
						NodeDefinition referencedDef = contextNodeDef.getDefinitionByPath(referencedPath);
						if (! (referencedDef instanceof TaxonAttributeDefinition)) {
							return new ExpressionValidationResult(ExpressionValidationResultFlag.ERROR, 
									String.format("Third argument (\"%s\") is not a valid path to a taxon attribute", expression.toString()));
						}
					}
				}
				return new ExpressionValidationResult();
			}
			
			private String extractConstantValue(Expression expr) throws IllegalArgumentException {
				if (expr instanceof Constant) {
					Object val = ((Constant) expr).computeValue(null);
					if (val instanceof String) {
						return (String) val;
					}
				}
				throw new IllegalArgumentException("Expected Constant, found " + expr.getClass().getName());
			}
		});
		
		//deprecated
		register("distance", new CustomFunction(2) {
			public Object invoke(ExpressionContext expressionContext, Object[] objects) {
				return GeoFunctions.distance(expressionContext, objects[0], objects[1]);
			}
			@Override
			protected ExpressionValidationResult performArgumentValidation(NodeDefinition contextNodeDef,
					Expression[] arguments) {
				return super.performArgumentValidation(contextNodeDef, arguments);
			}
		});
		
		register("datetime-diff", new CustomFunction(4) {
			public Object invoke(ExpressionContext expressionContext, Object[] objects) {
				return dateTimeDifference(expressionContext, (Integer) objects[0], (Integer) objects[1], 
						(Integer) objects[2], (Integer) objects[3]);
			}
		});

		register("datetime-diff", new CustomFunction(5) {
			public Object invoke(ExpressionContext expressionContext, Object[] objects) {
				return dateTimeDifference(expressionContext, (Integer) objects[0], (Integer) objects[1], 
						(Integer) objects[2], (Integer) objects[3], (String) objects[4]);
			}
		});
		
		//deprecated geo functions (use "geo" namespace instead)
		register("distance", new CustomFunction(2) {
			public Object invoke(ExpressionContext expressionContext, Object[] objects) {
				return GeoFunctions.distance(expressionContext, objects[0], objects[1]);
			}
		});
		
		register("latlong", GeoFunctions.LAT_LONG_FUNCTION);
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

	private static Object distinctValues(Object obj) {
		if (obj instanceof Collection) {
			Set<Object> result = new LinkedHashSet<Object>((Collection<?>) obj);
			CollectionUtils.filter(result, new Predicate<Object>() {
				public boolean evaluate(Object item) {
					return item != null && 
							(!(item instanceof String) || StringUtils.isNotBlank((String) item));
				}
			});
			if (result.isEmpty()) {
				return null;
			} else {
				return new ArrayList<Object>(result);
			}
		} else {
			return obj;
		}
	}
	
	private static int countDistinct(Object obj) {
		Object values = distinctValues(obj);
		if (values == null) {
			return 0;
		} else if (values instanceof Collection) {
			return ((Collection<?>) values).size();
		} else {
			return 1;
		}
	}
	
	@SuppressWarnings("rawtypes")
	private static boolean contains(Object values, Object item) {
		if (values == null) {
			return false;
		} else if (values instanceof Collection) {
			if (Numbers.isNumber(item)) {
				Double itemDouble = Numbers.toDoubleObject(item);
				for (Object value : (Collection) values) {
					if (itemDouble.equals(Numbers.toDoubleObject(value))) {
						return true;
					}
				}
				return false;
			} else {
				return ((Collection) values).contains(item);
			}
		} else if (Numbers.isNumber(values) && Numbers.isNumber(item)) {
			return Numbers.toDoubleObject(values).equals(Numbers.toDoubleObject(item));
		} else {
			return values.equals(item);
		}
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

	private Object dateTimeDifference(ExpressionContext expressionContext, Integer date1, Integer time1, Integer date2, Integer time2) {
		return dateTimeDifference(expressionContext, date1, time1, date2, time2, TimeUnit.MINUTE.name());
	}
	
	private Object dateTimeDifference(ExpressionContext expressionContext, Integer date1, Integer time1, Integer date2, Integer time2, String timeUnit) {
		return dateTimeDifference(expressionContext, Date.fromNumericValue(date1), Time.fromNumericValue(time1), 
				Date.fromNumericValue(date2), Time.fromNumericValue(time2), timeUnit);
	}
	
	private static Integer dateTimeDifference(ExpressionContext context, Date date1, Time time1, Date date2, Time time2, String timeUnit) {
		return dateTimeDifference(context, date1, time1, date2, time2, TimeUnit.valueOf(timeUnit.toUpperCase(Locale.ENGLISH)));
	}
	
	private static Integer dateTimeDifference(ExpressionContext context, Date date1, Time time1, Date date2, Time time2, TimeUnit timeUnit) {
		if (date1 != null && date1.isComplete() && date2 != null && date2.isComplete() && 
				time1 != null && time1.isComplete() && time2 != null && time2.isComplete()) {
			Calendar cal1 = getCalendar(date1, time1, timeUnit);
			Calendar cal2 = getCalendar(date2, time2, timeUnit);
			
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
	
	private static Calendar getCalendar(Date date2, Time time2, TimeUnit timeUnit) {
		int calendarTruncateField = getCalendarField(timeUnit);
		Calendar cal2 = Calendar.getInstance(Locale.ENGLISH);
		cal2.setLenient(false);
		cal2.set(date2.getYear(), date2.getMonth() - 1, date2.getDay(), time2.getHour(), time2.getMinute());
		cal2 = DateUtils.truncate(cal2, calendarTruncateField);
		return cal2;
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
	
	
}
