/**
 * 
 */
package org.openforis.collect.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

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

	private CollectRecord record;
	private RecordValidationCache validationCache;
	
	public RecordValidationReportGenerator(CollectRecord record) {
		this.record = record;
		this.validationCache = record.getValidationCache();
	}
	
	public List<RecordValidationReportItem> generateValidationItems() {
		return generateValidationItems(ValidationMessageBuilder.createInstance(), ValidationResultFlag.ERROR, true);
	}
	
	public List<RecordValidationReportItem> generateValidationItems(ValidationMessageBuilder messageBuilder,
			ValidationResultFlag level, boolean includeConfirmedErrors) {
		List<RecordValidationReportItem> result = new ArrayList<RecordValidationReportItem>();
		if ( level == ValidationResultFlag.ERROR ) {
			List<RecordValidationReportItem> skippedValuesItems = extractSkippedValuesValidationResultItems(
					messageBuilder);
			result.addAll(skippedValuesItems);
		}
		List<RecordValidationReportItem> attributeItems = extractAttributeValidationResultItems(
				messageBuilder, level, includeConfirmedErrors);
		result.addAll(attributeItems);
		List<RecordValidationReportItem> cardinalityItems = extractCardinalityValidationResultItems(
				messageBuilder, level, includeConfirmedErrors);
		result.addAll(cardinalityItems);
		return result;
	}

	protected List<RecordValidationReportItem> extractSkippedValuesValidationResultItems(
			ValidationMessageBuilder messageBuilder) {
		List<RecordValidationReportItem> result = new ArrayList<RecordValidationReportItem>();
		Set<Integer> skippedNodeIds = validationCache.getSkippedNodeIds();
		for (Integer nodeId : skippedNodeIds) {
			Attribute<?, ?> attr = (Attribute<?, ?>) record.getNodeByInternalId(nodeId);
			String path = messageBuilder.getPrettyFormatPath(attr);
			String message = messageBuilder.getReasonBlankNotSpecifiedMessage();
			RecordValidationReportItem recordValidationItem = new RecordValidationReportItem(nodeId, path, ValidationResultFlag.ERROR, message);
			result.add(recordValidationItem);
		}
		return result;
	}

	protected List<RecordValidationReportItem> extractCardinalityValidationResultItems(
			final ValidationMessageBuilder messageBuilder, final ValidationResultFlag level, 
			final boolean includeConfirmedErrors) {
		final List<RecordValidationReportItem> result = new ArrayList<RecordValidationReportItem>();
		Entity rootEntity = record.getRootEntity();
		rootEntity.traverse(new NodeVisitor() {
			@Override
			public void visit(Node<? extends NodeDefinition> node, int idx) {
				if ( node instanceof Entity ) {
					Entity entity = (Entity) node;
					List<RecordValidationReportItem> items = extractCardinalityValidationItems(messageBuilder,
							entity, level, includeConfirmedErrors);
					result.addAll(items);
				}
			}
		});
		return result;
	}

	protected List<RecordValidationReportItem> extractCardinalityValidationItems(
			ValidationMessageBuilder messageBuilder, Entity entity, 
			ValidationResultFlag level, boolean includeConfirmedErrors) {
		List<RecordValidationReportItem> result = new ArrayList<RecordValidationReportItem>();
		if ( level == ValidationResultFlag.ERROR ) {
			List<RecordValidationReportItem> minCountErrorItems = createCardinalityValidationItems(
					messageBuilder, entity, ValidationResultFlag.ERROR, true);
			result.addAll(minCountErrorItems);
			if ( includeConfirmedErrors ) {
				List<RecordValidationReportItem> missingApprovedItems = createMissingApprovedItems(messageBuilder, entity);
				result.addAll(missingApprovedItems);
			}
			List<RecordValidationReportItem> maxCountErrorItems = createCardinalityValidationItems(
					messageBuilder, entity, ValidationResultFlag.ERROR, false);
			result.addAll(maxCountErrorItems);
		} else {
			List<RecordValidationReportItem> minCountWarningItems = createCardinalityValidationItems(
					messageBuilder, entity, ValidationResultFlag.WARNING, true);
			result.addAll(minCountWarningItems);
			List<RecordValidationReportItem> maxCountWarningItems = createCardinalityValidationItems(
					messageBuilder, entity, ValidationResultFlag.WARNING, false);
			result.addAll(maxCountWarningItems);
		}
		return result;
	}

	private List<RecordValidationReportItem> createMissingApprovedItems(
			ValidationMessageBuilder messageBuilder, Entity entity) {
		List<RecordValidationReportItem> result = new ArrayList<RecordValidationReportItem>();
		Integer entityId = entity.getInternalId();
		Set<String> minCountWarningChildNames = validationCache.getMinCountWarningChildNames(entityId);
		for (String childName : minCountWarningChildNames) {
			if ( record.isMissingApproved(entity, childName) ) {
				RecordValidationReportItem item = createCardinalityValidationItem(
						messageBuilder, entity, childName, ValidationResultFlag.ERROR, true);
				result.add(item);
			}
		}
		return result;
	}

	private List<RecordValidationReportItem> createCardinalityValidationItems(ValidationMessageBuilder messageBuilder,
			Entity entity, ValidationResultFlag flag, boolean minCount) {
		List<RecordValidationReportItem> result = new ArrayList<RecordValidationReportItem>();
		Integer entityId = entity.getInternalId();
		Set<String> childNames = validationCache.getCardinalityFailedChildNames(entityId, flag, minCount);
		for (String childName : childNames) {
			RecordValidationReportItem item = createCardinalityValidationItem(
					messageBuilder, entity, childName, flag, minCount);
			result.add(item);
		}
		return result;
	}
	
	private RecordValidationReportItem createCardinalityValidationItem(
			final ValidationMessageBuilder messageBuilder, Entity entity,
			String childName, ValidationResultFlag flag, boolean minCount) {
		String path = messageBuilder.getPrettyFormatPath(entity, childName);
		String message = minCount ? messageBuilder.getMinCountValidationMessage(entity, childName):
			messageBuilder.getMaxCountValidationMessage(entity, childName);
		RecordValidationReportItem recordValidationItem = new RecordValidationReportItem(path, flag, message);
		return recordValidationItem;
	}
	
	protected List<RecordValidationReportItem> extractAttributeValidationResultItems(
			ValidationMessageBuilder messageBuilder, ValidationResultFlag level, boolean includeConfirmedErrors) {
		List<RecordValidationReportItem> items = new ArrayList<RecordValidationReportItem>();
		Set<Entry<Integer,ValidationResults>> attributeValidationEntries = validationCache.getValidationResultsByAttributeId().entrySet();
		for (Entry<Integer, ValidationResults> entry : attributeValidationEntries) {
			Integer attrId = entry.getKey();
			List<RecordValidationReportItem> attributeItems = extractAttributeValidationResultItem(messageBuilder, 
					attrId, level, includeConfirmedErrors);
			items.addAll(attributeItems);
		}
		return items;
	}

	protected List<RecordValidationReportItem> extractAttributeValidationResultItem(
			ValidationMessageBuilder messageBuilder,
			Integer attrId, ValidationResultFlag level, boolean includeConfirmedErrors) {
		List<RecordValidationReportItem> items = new ArrayList<RecordValidationReportItem>();
		Attribute<?, ?> attr = (Attribute<?, ?>) record.getNodeByInternalId(attrId);
		//String path = attr.getPath();
		String path = messageBuilder.getPrettyFormatPath(attr);
		ValidationResults validationResults = validationCache.getAttributeValidationResults(attrId);
		List<ValidationResult> failed = validationResults.getFailed();
		for (ValidationResult validationResult : failed) {
			ValidationResultFlag flag = validationResult.getFlag();
			if ( isInLevel(flag, level) || flag == ValidationResultFlag.WARNING && includeConfirmedErrors && record.isErrorConfirmed(attr) ) {
				String message = messageBuilder.getValidationMessage(attr, validationResult);
				RecordValidationReportItem recordValidationItem = new RecordValidationReportItem(attrId, path, flag, message);
				items.add(recordValidationItem);
			}
		}
		return items;
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
