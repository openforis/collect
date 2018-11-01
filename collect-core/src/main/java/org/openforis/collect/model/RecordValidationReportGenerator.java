/**
 * 
 */
package org.openforis.collect.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;
import org.openforis.collect.model.validation.ValidationMessageBuilder;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.validation.ValidationResult;
import org.openforis.idm.metamodel.validation.ValidationResultFlag;
import org.openforis.idm.metamodel.validation.ValidationResults;
import org.openforis.idm.model.Attribute;
import org.openforis.idm.model.Entity;
import org.openforis.idm.model.Node;
import org.openforis.idm.model.NodeVisitor;

/**
 * @author S. Ricci
 *
 */
public class RecordValidationReportGenerator {

	//parameters
	private CollectRecord record;
	private RecordValidationCache validationCache;
	
	//transient instance variables
	private ValidationMessageBuilder messageBuilder;
	
	public RecordValidationReportGenerator(CollectRecord record) {
		this(record, ValidationMessageBuilder.createInstance());
	}
	
	public RecordValidationReportGenerator(CollectRecord record, ValidationMessageBuilder messageBuilder) {
		this.record = record;
		this.validationCache = record.getValidationCache();
		this.messageBuilder = messageBuilder;
	}
	
	public List<RecordValidationReportItem> generateValidationItems() {
		return generateValidationItems(ValidationMessageBuilder.DEFAULT_LOCALE);
	}
	
	public List<RecordValidationReportItem> generateValidationItems(Locale locale) {
		return generateValidationItems(locale, ValidationResultFlag.ERROR, true);
	}
	
	public List<RecordValidationReportItem> generateValidationItems(ValidationResultFlag level, boolean includeConfirmedErrors) {
		return generateValidationItems(ValidationMessageBuilder.DEFAULT_LOCALE, level, includeConfirmedErrors);
	}
	
	public List<RecordValidationReportItem> generateValidationItems(Locale locale, ValidationResultFlag level, boolean includeConfirmedErrors) {
		List<RecordValidationReportItem> result = new ArrayList<RecordValidationReportItem>();
		if ( level == ValidationResultFlag.ERROR ) {
			List<RecordValidationReportItem> skippedValuesItems = extractSkippedValuesValidationResultItems(locale);
			result.addAll(skippedValuesItems);
		}
		List<RecordValidationReportItem> attributeItems = extractAttributeValidationResultItems(locale, level, includeConfirmedErrors);
		result.addAll(attributeItems);
		List<RecordValidationReportItem> cardinalityItems = extractCardinalityValidationResultItems(locale, level, includeConfirmedErrors);
		result.addAll(cardinalityItems);
		return result;
	}

	protected List<RecordValidationReportItem> extractSkippedValuesValidationResultItems(
			Locale locale) {
		List<RecordValidationReportItem> result = new ArrayList<RecordValidationReportItem>();
		Set<Integer> skippedNodeIds = validationCache.getSkippedNodeIds();
		for (Integer nodeId : skippedNodeIds) {
			Attribute<?, ?> attr = (Attribute<?, ?>) record.getNodeByInternalId(nodeId);
			String path = getPath(attr);
			String prettyFormatPath = messageBuilder.getPrettyFormatPath(attr, locale);
			String message = messageBuilder.getReasonBlankNotSpecifiedMessage(locale);
			RecordValidationReportItem recordValidationItem = new RecordValidationReportItem(nodeId, path, prettyFormatPath, ValidationResultFlag.ERROR, message);
			result.add(recordValidationItem);
		}
		return result;
	}

	protected List<RecordValidationReportItem> extractCardinalityValidationResultItems(final Locale locale, 
			final ValidationResultFlag level, 
			final boolean includeConfirmedErrors) {
		final List<RecordValidationReportItem> result = new ArrayList<RecordValidationReportItem>();
		Entity rootEntity = record.getRootEntity();
		rootEntity.traverse(new NodeVisitor() {
			@Override
			public void visit(Node<? extends NodeDefinition> node, int idx) {
				if ( node instanceof Entity ) {
					Entity entity = (Entity) node;
					List<RecordValidationReportItem> items = extractCardinalityValidationItems(locale, entity, level, includeConfirmedErrors);
					result.addAll(items);
				}
			}
		});
		return result;
	}

	protected List<RecordValidationReportItem> extractCardinalityValidationItems(
			Locale locale, Entity entity, 
			ValidationResultFlag level, boolean includeConfirmedErrors) {
		List<RecordValidationReportItem> result = new ArrayList<RecordValidationReportItem>();
		if ( level == ValidationResultFlag.ERROR ) {
			List<RecordValidationReportItem> minCountErrorItems = createCardinalityValidationItems(
					locale, entity, ValidationResultFlag.ERROR, true);
			result.addAll(minCountErrorItems);
			if ( includeConfirmedErrors ) {
				List<RecordValidationReportItem> missingApprovedItems = createMissingApprovedItems(locale, entity);
				result.addAll(missingApprovedItems);
			}
			List<RecordValidationReportItem> maxCountErrorItems = createCardinalityValidationItems(locale, entity, ValidationResultFlag.ERROR, false);
			result.addAll(maxCountErrorItems);
		} else {
			List<RecordValidationReportItem> minCountWarningItems = createCardinalityValidationItems(
					locale, entity, ValidationResultFlag.WARNING, true);
			result.addAll(minCountWarningItems);
			List<RecordValidationReportItem> maxCountWarningItems = createCardinalityValidationItems(
					locale, entity, ValidationResultFlag.WARNING, false);
			result.addAll(maxCountWarningItems);
		}
		return result;
	}

	private List<RecordValidationReportItem> createMissingApprovedItems(Locale locale,
			Entity entity) {
		List<RecordValidationReportItem> result = new ArrayList<RecordValidationReportItem>();
		Integer entityId = entity.getInternalId();
		Set<NodeDefinition> minCountWarningChildDefs = validationCache.getMinCountWarningChildDefinitions(entityId);
		for (NodeDefinition childDef : minCountWarningChildDefs) {
			if ( record.isMissingApproved(entity, childDef) ) {
				RecordValidationReportItem item = createCardinalityValidationItem(
						locale, entity, childDef, ValidationResultFlag.ERROR, true);
				result.add(item);
			}
		}
		return result;
	}

	private List<RecordValidationReportItem> createCardinalityValidationItems(Locale locale, Entity entity, ValidationResultFlag flag, boolean minCount) {
		List<RecordValidationReportItem> result = new ArrayList<RecordValidationReportItem>();
		Integer entityId = entity.getInternalId();
		Set<NodeDefinition> childDefs = validationCache.getCardinalityFailedChildDefinitions(entityId, flag, minCount);
		for (NodeDefinition childDef : childDefs) {
			RecordValidationReportItem item = createCardinalityValidationItem(locale, entity, childDef, flag, minCount);
			result.add(item);
		}
		return result;
	}
	
	private RecordValidationReportItem createCardinalityValidationItem(
			final Locale locale, Entity entity,
			NodeDefinition childDef, ValidationResultFlag flag, boolean minCount) {
		String childName = childDef.getName();
		String path = getPath(entity) + "/" + childName;
		String prettyFormatPath = messageBuilder.getPrettyFormatPath(entity, childName, locale);
		String message = minCount ? messageBuilder.getMinCountValidationMessage(entity, childName, locale):
			messageBuilder.getMaxCountValidationMessage(entity, childName, locale);
		RecordValidationReportItem recordValidationItem = new RecordValidationReportItem(path, prettyFormatPath, flag, message);
		return recordValidationItem;
	}
	
	protected List<RecordValidationReportItem> extractAttributeValidationResultItems(Locale locale,
			ValidationResultFlag level, boolean includeConfirmedErrors) {
		List<RecordValidationReportItem> items = new ArrayList<RecordValidationReportItem>();
		Set<Entry<Integer,ValidationResults>> attributeValidationEntries = validationCache.getValidationResultsByAttributeId().entrySet();
		for (Entry<Integer, ValidationResults> entry : attributeValidationEntries) {
			Integer attrId = entry.getKey();
			List<RecordValidationReportItem> attributeItems = extractAttributeValidationResultItem(locale, attrId, level, includeConfirmedErrors);
			items.addAll(attributeItems);
		}
		return items;
	}

	protected List<RecordValidationReportItem> extractAttributeValidationResultItem(Locale locale, 
			Integer attrId, ValidationResultFlag level, boolean includeConfirmedErrors) {
		List<RecordValidationReportItem> items = new ArrayList<RecordValidationReportItem>();
		Attribute<?, ?> attr = (Attribute<?, ?>) record.getNodeByInternalId(attrId);
		ValidationResults validationResults = validationCache.getAttributeValidationResults(attrId);
		List<ValidationResult> failed = validationResults.getFailed();
		if ( CollectionUtils.isNotEmpty(failed) ) {
			String path = getPath(attr);
			String prettyFormatPath = messageBuilder.getPrettyFormatPath(attr, locale);
			for (ValidationResult validationResult : failed) {
				ValidationResultFlag flag = validationResult.getFlag();
				if ( isInLevel(flag, level) || flag == ValidationResultFlag.WARNING && includeConfirmedErrors && record.isErrorConfirmed(attr) ) {
					String message = messageBuilder.getValidationMessage(attr, validationResult, locale);
					RecordValidationReportItem recordValidationItem = new RecordValidationReportItem(attrId, path, prettyFormatPath, flag, message);
					items.add(recordValidationItem);
				}
			}
		}
		return items;
	}

	private String getPath(Node<?> node) {
		String path = node.getPath();
//		path = path.replaceFirst("\\w*/", "");
		return path;
	}
	
	private boolean isInLevel(ValidationResultFlag flag,
			ValidationResultFlag level) {
		switch (level) {
		case ERROR:
			return flag == ValidationResultFlag.ERROR;
		case WARNING:
			return flag == ValidationResultFlag.ERROR || flag == ValidationResultFlag.WARNING;
		default:
			return true;
		}
	}
	
}
