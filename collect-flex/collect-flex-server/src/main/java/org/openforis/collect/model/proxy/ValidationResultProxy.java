/**
 * 
 */
package org.openforis.collect.model.proxy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.granite.messaging.amf.io.util.externalizer.annotation.ExternalizedProperty;
import org.openforis.collect.Proxy;
import org.openforis.collect.metamodel.proxy.LanguageSpecificTextProxy;
import org.openforis.collect.model.validation.RecordKeyUniquenessValidator;
import org.openforis.collect.model.validation.SpecifiedValidator;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.NodeLabel;
import org.openforis.idm.metamodel.NodeLabel.Type;
import org.openforis.idm.metamodel.Schema;
import org.openforis.idm.metamodel.Survey;
import org.openforis.idm.metamodel.SurveyContext;
import org.openforis.idm.metamodel.validation.Check;
import org.openforis.idm.metamodel.validation.CodeValidator;
import org.openforis.idm.metamodel.validation.ComparisonCheck;
import org.openforis.idm.metamodel.validation.CoordinateValidator;
import org.openforis.idm.metamodel.validation.DateValidator;
import org.openforis.idm.metamodel.validation.DistanceCheck;
import org.openforis.idm.metamodel.validation.ExternalCodeValidator;
import org.openforis.idm.metamodel.validation.IntegerRangeValidator;
import org.openforis.idm.metamodel.validation.NumericRangeUnitValidator;
import org.openforis.idm.metamodel.validation.NumberValueUnitValidator;
import org.openforis.idm.metamodel.validation.PatternCheck;
import org.openforis.idm.metamodel.validation.RealRangeValidator;
import org.openforis.idm.metamodel.validation.TimeValidator;
import org.openforis.idm.metamodel.validation.UniquenessCheck;
import org.openforis.idm.metamodel.validation.ValidationResult;
import org.openforis.idm.metamodel.validation.ValidationResultFlag;
import org.openforis.idm.metamodel.validation.ValidationRule;
import org.openforis.idm.model.Attribute;
import org.openforis.idm.model.Entity;
import org.openforis.idm.model.Record;
import org.openforis.idm.model.expression.ExpressionFactory;
import org.openforis.idm.model.expression.InvalidExpressionException;
import org.openforis.idm.model.expression.ModelPathExpression;

/**
 * @author M. Togna
 * 
 */
public class ValidationResultProxy implements Proxy {

	private transient ValidationResult validationResult;
	private transient Attribute<?, ?> attribute;
	private static Map<ValidationRule<?>, String[]> messageArgsCache;

	static {
		messageArgsCache = new HashMap<ValidationRule<?>, String[]>();
	}
	
	public ValidationResultProxy(Attribute<?, ?> attribute, ValidationResult validationResult) {
		this.attribute = attribute;
		this.validationResult = validationResult;
	}

	public static List<ValidationResultProxy> fromList(Attribute<?, ?> attribute, List<ValidationResult> list) {
		if (list != null) {
			List<ValidationResultProxy> proxies = new ArrayList<ValidationResultProxy>();
			for (ValidationResult validationResults : list) {
				proxies.add(new ValidationResultProxy(attribute, validationResults));
			}
			return proxies;
		} else {
			return Collections.emptyList();
		}
	}

	@ExternalizedProperty
	public String getRuleName() {
		return validationResult.getValidator().getClass().getSimpleName();
	}
	
	@ExternalizedProperty
	public String getMessageKey() {
		String key = null;
		ValidationRule<?> validator = validationResult.getValidator();
		if(validator instanceof CodeValidator) {
			key = "edit.validation.codeError";
		} else if(validator instanceof ComparisonCheck) {
			key = "edit.validation.comparisonError";
		} else if(validator instanceof CoordinateValidator) {
			if ( attribute.isFilled() ) {
				key = "edit.validation.coordinateError";
			} else {
				key = "edit.validation.incompleteCoordinateError";
			}
		} else if(validator instanceof DateValidator) {
			if ( attribute.isFilled() ) {
				key = "edit.validation.dateError";
			} else {
				key = "edit.validation.incompleteDateError";
			}
		} else if(validator instanceof DistanceCheck) {
			key = "edit.validation.distanceError";
		} else if(validator instanceof ExternalCodeValidator) {
			key = "edit.validation.externalCodeError";
		} else if(validator instanceof IntegerRangeValidator) {
			key = "edit.validation.integerRangeError";
		} else if(validator instanceof PatternCheck) {
			key = "edit.validation.patternError";
		} else if(validator instanceof RealRangeValidator) {
			key = "edit.validation.realRangeError";
		} else if(validator instanceof RecordKeyUniquenessValidator) {
			key = "edit.validation.recordKeyUniquenessError";
		} else if(validator instanceof SpecifiedValidator) {
			if(validationResult.getFlag() == ValidationResultFlag.ERROR) {
				key = "edit.validation.specifiedError";
			} else {
				key = "edit.validation.requiredField";
			}
		} else if(validator instanceof TimeValidator) {
			if ( attribute.isFilled() ) {
				key = "edit.validation.timeError";
			} else {
				key = "edit.validation.incompleteTimeError";
			}
		} else if(validator instanceof UniquenessCheck) {
			key = "edit.validation.uniquenessError";
		} else if(validator instanceof NumberValueUnitValidator || validator instanceof NumericRangeUnitValidator) {
			key = "edit.validation.unitNotSpecifiedError";
		}
		return key;
	}
	
	@ExternalizedProperty
	public String[] getMessageArgs() {
		ValidationRule<?> validator = validationResult.getValidator();
		String[] result = messageArgsCache.get(validator);
		if(result == null) {
			if(validator instanceof ComparisonCheck) {
				ComparisonCheck check = (ComparisonCheck) validator;
				ArrayList<String> args = new ArrayList<String>();
				Map<String, String> expressions = new HashMap<String, String>();
				expressions.put("lt", check.getLessThanExpression());
				expressions.put("lte", check.getLessThanOrEqualsExpression());
				expressions.put("gt", check.getGreaterThanExpression());
				expressions.put("gte", check.getGreaterThanOrEqualsExpression());
				
				String labelText = getInstanceLabelText(attribute.getDefinition());
				args.add(labelText);
				for (String key : expressions.keySet()) {
					String expression = expressions.get(key);
					if(expression != null) {
						String arg, argPart1, argPart2;
						argPart1 = key;
						argPart2 = getComparisonCheckMessageArg(expression);
						arg = argPart1 + ";" + argPart2;
						args.add(arg);
					}
				}
				result = args.toArray(new String[0]);
				messageArgsCache.put(validator, result);
			}
		}
		return result;
	}
	
	private String getComparisonCheckMessageArg(String expression) {
		String result = expression;
		Record record = attribute.getRecord();
		Survey survey = record.getSurvey();
		Schema schema = survey.getSchema();
		SurveyContext recordContext = record.getSurveyContext();
		ExpressionFactory expressionFactory = recordContext.getExpressionFactory();
		try {
			Entity parentEntity = attribute.getParent();
			EntityDefinition parentDefinition = parentEntity.getDefinition();
			ModelPathExpression modelPathExpression = expressionFactory.createModelPathExpression(expression);
			List<String> referencedPaths = modelPathExpression.getReferencedPaths();
			for (String path : referencedPaths) {
				String absolutePath = parentDefinition.getPath() + "/" + path;
				NodeDefinition nodeDefinition = schema.getByPath(absolutePath);
				String label = getInstanceLabelText(nodeDefinition);
				result = result.replaceAll(nodeDefinition.getName(), label);
			}
		} catch (InvalidExpressionException e) {
		}
		return result;
	}
	
	private String getInstanceLabelText(NodeDefinition definition) {
		String result = null;
		List<NodeLabel> labels = definition.getLabels();
		for (NodeLabel label : labels) {
			if(label != null && label.getType() != null && label.getType() == Type.INSTANCE) {
				result = label.getText();
			}
		}
		if(result == null && labels.size() > 0) {
			NodeLabel label = labels.get(0);
			result = label.getText();
		}
		return result;
	}
	
	@ExternalizedProperty
	public List<LanguageSpecificTextProxy> getMessages() {
		ValidationRule<?> validator = validationResult.getValidator();
		if(validator instanceof Check<?>) {
			return LanguageSpecificTextProxy.fromList(((Check<?>) validator).getMessages());
		} else {
			return Collections.emptyList();
		}
	}
}
