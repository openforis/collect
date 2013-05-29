/**
 * 
 */
package org.openforis.collect.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.openforis.collect.model.validation.ValidationMessageBuilder;
import org.openforis.commons.collection.CollectionUtils;
import org.openforis.idm.metamodel.validation.ValidationResult;
import org.openforis.idm.metamodel.validation.ValidationResultFlag;
import org.openforis.idm.metamodel.validation.ValidationResults;
import org.openforis.idm.model.Attribute;
import org.openforis.idm.model.Entity;
import org.openforis.idm.model.Node;

/**
 * @author S. Ricci
 *
 */
public class RecordValidationResult {

	private CollectRecord record;
	private Map<Integer, ValidationResults> validationResultByAttributeId;
	private Map<Integer, Boolean> relevanceByNodeId;
	private Map<Integer, Boolean> requirenessByNodeId;
	
	public RecordValidationResult(CollectRecord record) {
		this.record = record;
		validationResultByAttributeId = new HashMap<Integer, ValidationResults>();
		relevanceByNodeId = new HashMap<Integer, Boolean>();
		requirenessByNodeId = new HashMap<Integer, Boolean>();
	}

	public void addValidationResult(Attribute<?, ?> attribute,
			ValidationResults results) {
		ValidationResults validationResults = validationResultByAttributeId.get(attribute.getInternalId());
		if ( validationResults == null ) {
			validationResultByAttributeId.put(attribute.getInternalId(), results);
		} else {
			for (ValidationResult result : results.getFailed()) {
				validationResults.addResult(result);
			}
		}
	}

	public List<RecordValidationItem> createPrintableItems(ValidationMessageBuilder messageBuilder) {
		List<RecordValidationItem> result = new ArrayList<RecordValidationItem>();
		List<RecordValidationItem> attributeItems = createAttributeValidationResultItems(messageBuilder);
		result.addAll(attributeItems);
		return result;
	}

	protected List<RecordValidationItem> createAttributeValidationResultItems(
			ValidationMessageBuilder messageBuilder) {
		List<RecordValidationItem> items = new ArrayList<RecordValidationItem>();
		Set<Entry<Integer,ValidationResults>> attributeValidationEntries = validationResultByAttributeId.entrySet();
		for (Entry<Integer, ValidationResults> entry : attributeValidationEntries) {
			Integer attrId = entry.getKey();
			List<RecordValidationItem> attributeItems = createAttributeValidationResultItem(messageBuilder, attrId);
			items.addAll(attributeItems);
		}
		return items;
	}

	protected List<RecordValidationItem> createAttributeValidationResultItem(
			ValidationMessageBuilder messageBuilder,
			Integer attrId) {
		List<RecordValidationItem> items = new ArrayList<RecordValidationItem>();
		Attribute<?, ?> attr = (Attribute<?, ?>) record.getNodeByInternalId(attrId);
		//String path = attr.getPath();
		String path = messageBuilder.getPrettyFormatPath(attr);
		ValidationResults validationResults = validationResultByAttributeId.get(attrId);
		List<ValidationResult> failed = validationResults.getFailed();
		for (ValidationResult validationResult : failed) {
			String message = messageBuilder.getValidationMessage(attr, validationResult);
			RecordValidationItem recordValidationItem = new RecordValidationItem(attrId, path, validationResult.getFlag(), message);
			items.add(recordValidationItem);
		}
		return items;
	}
	
	public void setRelevant(Node<?> node, boolean relevant) {
		relevanceByNodeId.put(node.getInternalId(), relevant);
	}

	public void setRequired(Node<?> node, boolean required) {
		requirenessByNodeId.put(node.getInternalId(), required);
	}
	
	public Map<Integer, ValidationResults> getNodeIdToValidationResult() {
		return CollectionUtils.unmodifiableMap(validationResultByAttributeId);
	}
	
	public Map<Integer, Boolean> getNodeIdToRelevant() {
		return CollectionUtils.unmodifiableMap(relevanceByNodeId);
	}
	
	public Map<Integer, Boolean> getNodeIdToRequired() {
		return CollectionUtils.unmodifiableMap(requirenessByNodeId);
	}
	
}
