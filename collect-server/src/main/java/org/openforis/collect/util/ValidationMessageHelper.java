package org.openforis.collect.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.openforis.collect.model.validation.RecordKeyUniquenessValidator;
import org.openforis.collect.model.validation.SpecifiedValidator;
import org.openforis.idm.metamodel.AttributeDefinition;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.NodeLabel.Type;
import org.openforis.idm.metamodel.Schema;
import org.openforis.idm.metamodel.Survey;
import org.openforis.idm.metamodel.SurveyContext;
import org.openforis.idm.metamodel.validation.CodeParentValidator;
import org.openforis.idm.metamodel.validation.CodeValidator;
import org.openforis.idm.metamodel.validation.ComparisonCheck;
import org.openforis.idm.metamodel.validation.CoordinateValidator;
import org.openforis.idm.metamodel.validation.DateValidator;
import org.openforis.idm.metamodel.validation.DistanceCheck;
import org.openforis.idm.metamodel.validation.ExternalCodeValidator;
import org.openforis.idm.metamodel.validation.IntegerRangeValidator;
import org.openforis.idm.metamodel.validation.NumberValueUnitValidator;
import org.openforis.idm.metamodel.validation.NumericRangeUnitValidator;
import org.openforis.idm.metamodel.validation.PatternCheck;
import org.openforis.idm.metamodel.validation.RealRangeValidator;
import org.openforis.idm.metamodel.validation.TimeValidator;
import org.openforis.idm.metamodel.validation.UniquenessCheck;
import org.openforis.idm.metamodel.validation.ValidationResult;
import org.openforis.idm.metamodel.validation.ValidationResultFlag;
import org.openforis.idm.metamodel.validation.ValidationRule;
import org.openforis.idm.model.Attribute;
import org.openforis.idm.model.Entity;
import org.openforis.idm.model.Field;
import org.openforis.idm.model.Node;
import org.openforis.idm.model.Record;
import org.openforis.idm.model.expression.ExpressionFactory;
import org.openforis.idm.model.expression.ModelPathExpression;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * @author S. Ricci
 *
 */
public class ValidationMessageHelper {

	private static final String VALIDATION_COMPARE_PREFIX = "validation.compare.";
	private static final String PATH_SEPARATOR = " / ";
	private static final String KEY_LABEL_SEPARATOR = "-";
	@Autowired
	private MessageBundleHelper messageBundleHelper;
	
	public String getValidationMessage(Attribute<?, ?> attribute, ValidationResult validationResult, Locale locale) {
		String key = getMessageKey(attribute, validationResult);
		String[] args = getMessageArgs(attribute, validationResult, locale);
		String result = messageBundleHelper.getMessage(key, args, locale);
		return result;
	}
	
	public String getMaxCountValidationMessage(NodeDefinition defn) {
		Integer maxCount = defn.getMaxCount();
		Integer[] args = new Integer[]{maxCount > 0 ? maxCount: 1};
		String message = messageBundleHelper.getMessage("edit.validation.maxCount", args);
		return message;
	}

	public String getMinCountValidationMessage(NodeDefinition defn) {
		Integer minCount = defn.getMinCount();
		Integer[] args = new Integer[]{minCount > 0 ? minCount: 1};
		String message = messageBundleHelper.getMessage("validation.minCount", args);
		return message;
	}
	
	protected String getMessageKey(Attribute<?, ?> attribute, ValidationResult validationResult) {
		String key = null;
		ValidationRule<?> validator = validationResult.getValidator();
		if(validator instanceof CodeValidator) {
			key = "validation.codeError";
		} else if(validator instanceof CodeParentValidator) {
			key = "validation.codeParentError";
		} else if(validator instanceof ComparisonCheck) {
			key = "validation.comparisonError";
		} else if(validator instanceof CoordinateValidator) {
			if ( attribute.isFilled() ) {
				key = "validation.coordinateError";
			} else {
				key = "validation.incompleteCoordinateError";
			}
		} else if(validator instanceof DateValidator) {
			if ( attribute.isFilled() ) {
				key = "validation.dateError";
			} else {
				key = "validation.incompleteDateError";
			}
		} else if(validator instanceof DistanceCheck) {
			key = "validation.distanceError";
		} else if(validator instanceof ExternalCodeValidator) {
			key = "validation.externalCodeError";
		} else if(validator instanceof IntegerRangeValidator) {
			key = "validation.integerRangeError";
		} else if(validator instanceof PatternCheck) {
			key = "validation.patternError";
		} else if(validator instanceof RealRangeValidator) {
			key = "validation.realRangeError";
		} else if(validator instanceof RecordKeyUniquenessValidator) {
			key = "validation.recordKeyUniquenessError";
		} else if(validator instanceof SpecifiedValidator) {
			if(validationResult.getFlag() == ValidationResultFlag.ERROR) {
				key = "validation.specifiedError";
			} else {
				key = "validation.requiredField";
			}
		} else if(validator instanceof TimeValidator) {
			if ( attribute.isFilled() ) {
				key = "validation.timeError";
			} else {
				key = "validation.incompleteTimeError";
			}
		} else if(validator instanceof UniquenessCheck) {
			key = "validation.uniquenessError";
		} else if(validator instanceof NumberValueUnitValidator || validator instanceof NumericRangeUnitValidator) {
			key = "validation.unitNotSpecifiedError";
		}
		return key;
	}
	
	
	protected String[] getMessageArgs(Attribute<?, ?> attribute, ValidationResult validationResult, Locale locale) {
		ValidationRule<?> validator = validationResult.getValidator();
		String[] result = null;
		if(validator instanceof ComparisonCheck) {
			ComparisonCheck check = (ComparisonCheck) validator;
			ArrayList<String> args = new ArrayList<String>();
			Map<String, String> expressions = new HashMap<String, String>();
			expressions.put("lt", check.getLessThanExpression());
			expressions.put("lte", check.getLessThanOrEqualsExpression());
			expressions.put("gt", check.getGreaterThanExpression());
			expressions.put("gte", check.getGreaterThanOrEqualsExpression());
			
			String labelText = getPrettyLabelText(attribute.getDefinition(), locale);
			args.add(labelText);
			for (String key : expressions.keySet()) {
				String expression = expressions.get(key);
				if(expression != null) {
					String arg, argPart1, argPart2;
					argPart1 = key;
					argPart2 = getComparisonCheckMessageArg(attribute, expression, locale);
					arg = argPart1 + ";" + argPart2;
					args.add(arg);
				}
			}
			result = args.toArray(new String[0]);
		}
		return result;
	}
	
	protected String getComparisonCheckMessageArg(Attribute<?,?> attribute, String expression, Locale locale) {
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
				NodeDefinition nodeDefinition = schema.getDefinitionByPath(absolutePath);
				String label = getPrettyLabelText(nodeDefinition, locale);
				result = result.replaceAll(nodeDefinition.getName(), label);
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return result;
	}
	
	protected String getComparisonCheckMessage(Attribute<?,?> attribute, ValidationResult validationResult, Locale locale) {
		String[] messageArgs = getMessageArgs(attribute, validationResult, locale);
		String nodeLabel = messageArgs[0];
		String[] adaptedArgs = new String[messageArgs.length];
		for (int i = 0; i < messageArgs.length; i++) {
			String arg = messageArgs[i];
			String[] argParts = arg.split(";");
			String op = argParts[0];
			String value = argParts[1];
			String opMessageKey = VALIDATION_COMPARE_PREFIX + op;
			String operator = messageBundleHelper.getMessage(opMessageKey, null, locale);
			String argAdapted = StringUtils.join(new String[]{operator, value}, " ");
			adaptedArgs[i] = argAdapted;
		}
		String andOperator = " " + messageBundleHelper.getMessage(VALIDATION_COMPARE_PREFIX + "and", null, locale) + " ";
		String argsConcat = StringUtils.join(adaptedArgs, andOperator);
		String messageKey = getMessageKey(attribute, validationResult);
		String result = messageBundleHelper.getMessage(messageKey, new String[]{nodeLabel, argsConcat}, locale);
		return result;
	}

	public String getInstanceLabelText(NodeDefinition definition, Locale locale) {
		return getInstanceLabelText(definition, locale.getLanguage());
	}
	
	public String getInstanceLabelText(NodeDefinition definition, String language) {
		return getLabelText(definition, language, Type.INSTANCE);
	}

	protected String getLabelText(NodeDefinition definition, String language,
			Type type) {
		String result = definition.getLabel(type, language);
		if ( result == null ) {
			result = definition.getLabel(type, null);
		}
		return result;
	}
	
	public String getPrettyLabelText(NodeDefinition definition, Locale locale) {
		return getPrettyLabelText(definition, locale.getLanguage());
	}
	
	public String getPrettyLabelText(NodeDefinition definition, String language) {
		String result = getLabelText(definition, language, new Type[]{Type.INSTANCE, Type.HEADING});
		if ( result == null ) {
			result = definition.getName();
		}
		return result;
	}
	
	private String getLabelText(NodeDefinition definition, String language,
			Type[] types) {
		for (Type type : types) {
			String result = getLabelText(definition, language, type);
			if ( result != null ) {
				return result;
			}
		}
		return null;
	}

	public String getPrettyFormatPath(Node<? extends NodeDefinition> node, Locale locale) {
		NodeDefinition defn = node.getDefinition();
		String label = getPrettyLabelText(defn, locale);
		if ( defn instanceof EntityDefinition ) {
			String keyText = getKeyText((Entity) node, locale);
			if ( StringUtils.isBlank(keyText) ) {
				label += "[" + (node.getIndex() + 1) + "]";
			} else {
				label += " " + keyText;
			}
		}
		if ( node.getParent() == null || node.getParent().getParent() == null ) {
			return label;
		} else {
			return getPrettyFormatPath(node.getParent(), locale) + PATH_SEPARATOR + label;
		}
	}
	
	public String getPrettyFormatPath(Entity parentEntity, String childName,
			Locale locale) {
		EntityDefinition parentEntityDefn = parentEntity.getDefinition();
		NodeDefinition childDefn = parentEntityDefn.getChildDefinition(childName);
		String label = getPrettyLabelText(childDefn, locale);
		if ( parentEntity.getParent() != null && parentEntity.getParent() != null ) {
			String parentEntityPath = getPrettyFormatPath(parentEntity, locale);
			return parentEntityPath + PATH_SEPARATOR + label;
		} else {
			return label;
		}
	}

	public String getKeyText(Entity entity, Locale locale) {
		EntityDefinition defn = entity.getDefinition();
		List<AttributeDefinition> keyDefns = defn.getKeyAttributeDefinitions();
		if ( ! keyDefns.isEmpty() ) {
			List<String> shortKeyParts = new ArrayList<String>();
			List<String> fullKeyParts = new ArrayList<String>();
			for (AttributeDefinition keyDefn : keyDefns) {
				Attribute<?, ?> keyAttr = (Attribute<?, ?>) entity.get(keyDefn.getName(), 0);
				if ( keyAttr != null ) {
					Object keyValue = getKeyLabelPart(keyAttr);
					if ( keyValue != null && StringUtils.isNotBlank(keyValue.toString()) ) {
						shortKeyParts.add(keyValue.toString());
						String label = getPrettyLabelText(keyDefn, locale);
						String fullKeyPart = label + " " + keyValue;
						fullKeyParts.add(fullKeyPart);
					}
				}
			}
			return StringUtils.join(shortKeyParts, KEY_LABEL_SEPARATOR);
		} else if ( entity.getParent() != null ) {
			return "" + (entity.getIndex() + 1);
		} else {
			return null;
		}
	}
	
	private Object getKeyLabelPart(Attribute<?, ?> attribute) {
		Object result = null;
		Field<?> field = attribute.getField(0);
		Object value = field.getValue();
		result = value;
		return result;
	}

}
