package org.openforis.collect.model.validation;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.openforis.collect.manager.MessageSource;
import org.openforis.collect.manager.ResourceBundleMessageSource;
import org.openforis.collect.model.CollectRecord;
import org.openforis.idm.metamodel.AttributeDefinition;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.NodeLabel.Type;
import org.openforis.idm.metamodel.Schema;
import org.openforis.idm.metamodel.Survey;
import org.openforis.idm.metamodel.SurveyContext;
import org.openforis.idm.metamodel.validation.Check;
import org.openforis.idm.metamodel.validation.Check.Flag;
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
import org.openforis.idm.metamodel.validation.TaxonVernacularLanguageValidator;
import org.openforis.idm.metamodel.validation.TimeValidator;
import org.openforis.idm.metamodel.validation.UniquenessCheck;
import org.openforis.idm.metamodel.validation.ValidationResult;
import org.openforis.idm.metamodel.validation.ValidationResultFlag;
import org.openforis.idm.metamodel.validation.ValidationResults;
import org.openforis.idm.metamodel.validation.ValidationRule;
import org.openforis.idm.model.Attribute;
import org.openforis.idm.model.Entity;
import org.openforis.idm.model.Field;
import org.openforis.idm.model.Node;
import org.openforis.idm.model.Record;
import org.openforis.idm.model.TaxonAttribute;
import org.openforis.idm.model.expression.ExpressionFactory;
import org.openforis.idm.model.expression.ModelPathExpression;
import org.springframework.context.i18n.LocaleContextHolder;

/**
 * 
 * @author S. Ricci
 *
 */
public class ValidationMessageBuilder {

	private static final String VALIDATION_COMPARE_MESSAGES_PREFIX = "validation.compare.";
	private static final String AND_OPERATOR_MESSAGE_KEY = VALIDATION_COMPARE_MESSAGES_PREFIX + "and";
	private static final String MULTIPLE_MESSAGE_ARGS_SEPARATOR = ";";
	private static final String PATH_SEPARATOR = "/";
	private static final String PRETTY_PATH_SEPARATOR = " / ";
	private static final String RECORD_KEYS_LABEL_SEPARATOR = "-";
	
	private MessageSource messageBundle;
	
	protected ValidationMessageBuilder(MessageSource messageBundle) {
		super();
		this.messageBundle = messageBundle;
	}
	
	public static ValidationMessageBuilder createInstance() {
		return createInstance(new ResourceBundleMessageSource());
	}

	public static ValidationMessageBuilder createInstance(MessageSource messageBundle) {
		return new ValidationMessageBuilder(messageBundle);
	}

	public String getValidationMessage(Attribute<?, ?> attribute, ValidationResult validationResult) {
		ValidationRule<?> validator = validationResult.getValidator();
		if ( validator instanceof Check ) {
			String message = getCustomMessage((Check<?>) validator);
			if (message != null ) {
				return message;
			}
		}
		if ( validator instanceof ComparisonCheck ) {
			return getComparisonCheckMessage(attribute, validationResult);
		} else {
			String key = getMessageKey(attribute, validationResult);
			if ( key != null ) {
				Object[] args = getMessageArgs(attribute, validationResult);
				String result = messageBundle.getMessage(key, args);
				return result;
			} else {
				return validator.getClass().getSimpleName();
			}
		}
	}
	
	public String getReasonBlankNotSpecifiedMessage() {
		String message = messageBundle.getMessage("validation.specifiedError");
		return message;
	}

	private String getCustomMessage(Check<?> check) {
		Locale locale = messageBundle.getCurrentLocale();
		String langCode = locale == null ? null: locale.getLanguage();
		String customMessage = check.getMessage(langCode);
		if ( customMessage == null ) {
			customMessage = check.getMessage(null);
		}
		return customMessage;
	}
	
	public List<String> getValidationMessages(Attribute<?,?> attribute, ValidationResults validationResults, Flag flag) {
		List<String> result = new ArrayList<String>();
		List<ValidationResult> items = flag == Flag.ERROR ? validationResults.getErrors(): validationResults.getWarnings();
		if ( items != null ) {
			for (ValidationResult validationResult : items) {
				String message = getValidationMessage(attribute, validationResult);
				if ( ! result.contains(message) ) {
					result.add(message);
				}
			}
		}
		return result;
	}
	
	public String getMaxCountValidationMessage(NodeDefinition defn) {
		Integer maxCount = defn.getMaxCount();
		Object[] args = new Integer[]{maxCount > 0 ? maxCount: 1};
		String message = messageBundle.getMessage("validation.maxCount", args);
		return message;
	}

	public String getMaxCountValidationMessage(Entity parentEntity, String childName) {
		EntityDefinition defn = parentEntity.getDefinition();
		NodeDefinition childDefn = defn.getChildDefinition(childName);
		return getMaxCountValidationMessage(childDefn);
	}

	public String getMinCountValidationMessage(Entity parentEntity, String childName) {
		int effectiveMinCount = parentEntity.getEffectiveMinCount(childName);
		Object[] args = new Integer[]{effectiveMinCount};
		String message = messageBundle.getMessage("validation.minCount", args);
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
		} else if ( validator instanceof TaxonVernacularLanguageValidator ) {
			TaxonAttribute taxonAttr = (TaxonAttribute) attribute;
			if ( StringUtils.isNotBlank(taxonAttr.getVernacularName()) && 
					StringUtils.isBlank(taxonAttr.getLanguageCode()) ) {
				key = "validator.taxon.missingVernacularLanguage";
			} else {
				key = "validator.taxon.vernacularLanguageNotRequired";
			}
		} else if(validator instanceof UniquenessCheck) {
			key = "validation.uniquenessError";
		} else if(validator instanceof NumberValueUnitValidator || validator instanceof NumericRangeUnitValidator) {
			key = "validation.unitNotSpecifiedError";
		}
		return key;
	}
	
	protected String[] getMessageArgs(Attribute<?, ?> attribute, ValidationResult validationResult) {
		ValidationRule<?> validator = validationResult.getValidator();
		String[] result = null;
		if(validator instanceof ComparisonCheck) {
			ComparisonCheck check = (ComparisonCheck) validator;
			ArrayList<String> args = new ArrayList<String>();
			String labelText = getPrettyLabelText(attribute.getDefinition());
			args.add(labelText);
			Map<String, String> expressions = new HashMap<String, String>();
			expressions.put("lt", check.getLessThanExpression());
			expressions.put("lte", check.getLessThanOrEqualsExpression());
			expressions.put("gt", check.getGreaterThanExpression());
			expressions.put("gte", check.getGreaterThanOrEqualsExpression());
			for (String key : expressions.keySet()) {
				String expression = expressions.get(key);
				if(expression != null) {
					String argPart1 = key;
					String argPart2 = getComparisonCheckMessageArg(attribute, expression);
					String arg = StringUtils.join(argPart1, MULTIPLE_MESSAGE_ARGS_SEPARATOR, argPart2);
					args.add(arg);
				}
			}
			result = args.toArray(new String[0]);
		}
		return result;
	}
	
	protected String getComparisonCheckMessageArg(Attribute<?,?> attribute, String expression) {
		if ( StringUtils.isNotBlank(expression) ) {
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
					String absolutePath = parentDefinition.getPath() + PATH_SEPARATOR + path;
					NodeDefinition nodeDefinition = schema.getDefinitionByPath(absolutePath);
					String label = getPrettyLabelText(nodeDefinition);
					result = result.replaceAll(nodeDefinition.getName(), label);
				}
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
			return result;
		} else {
			return expression;
		}
	}
	
	protected String getComparisonCheckMessage(Attribute<?,?> attribute, ValidationResult validationResult) {
		String[] messageArgs = getMessageArgs(attribute, validationResult);
		String nodeLabel = messageArgs[0];
		String[] adaptedArgs = new String[messageArgs.length - 1];
		for (int i = 1; i < messageArgs.length; i++) {
			String arg = messageArgs[i];
			String[] argParts = arg.split(MULTIPLE_MESSAGE_ARGS_SEPARATOR);
			String op = argParts[0];
			String value = argParts[1];
			String opMessageKey = VALIDATION_COMPARE_MESSAGES_PREFIX + op;
			String operator = messageBundle.getMessage(opMessageKey);
			String argAdapted = StringUtils.join(new String[]{operator, value}, " ");
			adaptedArgs[i - 1] = argAdapted;
		}
		String andOperator = StringUtils.join(" ", messageBundle.getMessage(AND_OPERATOR_MESSAGE_KEY), " ");
		String argsConcat = StringUtils.join(adaptedArgs, andOperator);
		String messageKey = getMessageKey(attribute, validationResult);
		String result = messageBundle.getMessage(messageKey, new Object[]{nodeLabel, argsConcat});
		return result;
	}

	public String getRecordKey(CollectRecord record) {
		record.updateRootEntityKeyValues();
		List<String> rootEntityKeyValues = record.getRootEntityKeyValues();
		List<String> cleanedKeys = new ArrayList<String>();
		for (String key : rootEntityKeyValues) {
			if ( StringUtils.isNotBlank(key) ) {
				cleanedKeys.add(key);
			}
		}
		String result = StringUtils.join(cleanedKeys, RECORD_KEYS_LABEL_SEPARATOR);
		return result;
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
	
	public String getPrettyLabelText(NodeDefinition definition) {
		Locale locale = LocaleContextHolder.getLocale();
		String language = locale.getLanguage();
		return getPrettyLabelText(definition, language);
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

	public String getPrettyFormatPath(Node<? extends NodeDefinition> node) {
		NodeDefinition defn = node.getDefinition();
		String label = getPrettyLabelText(defn);
		if ( defn instanceof EntityDefinition ) {
			String keyText = getKeyText((Entity) node);
			if ( StringUtils.isBlank(keyText) ) {
				label += "[" + (node.getIndex() + 1) + "]";
			} else {
				label += " " + keyText;
			}
		}
		if ( node.getParent() == null || node.getParent().getParent() == null ) {
			return label;
		} else {
			return getPrettyFormatPath(node.getParent()) + PRETTY_PATH_SEPARATOR + label;
		}
	}
	
	public String getPrettyFormatPath(Entity parentEntity, String childName) {
		EntityDefinition parentEntityDefn = parentEntity.getDefinition();
		NodeDefinition childDefn = parentEntityDefn.getChildDefinition(childName);
		String label = getPrettyLabelText(childDefn);
		if ( parentEntity.getParent() != null && parentEntity.getParent() != null ) {
			String parentEntityPath = getPrettyFormatPath(parentEntity);
			return parentEntityPath + PRETTY_PATH_SEPARATOR + label;
		} else {
			return label;
		}
	}

	public String getKeyText(Entity entity) {
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
						String label = getPrettyLabelText(keyDefn);
						String fullKeyPart = label + " " + keyValue;
						fullKeyParts.add(fullKeyPart);
					}
				}
			}
			return StringUtils.join(shortKeyParts, RECORD_KEYS_LABEL_SEPARATOR);
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
