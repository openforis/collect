package org.openforis.collect.model.recordUpdater;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.openforis.collect.metamodel.CollectAnnotations;
import org.openforis.collect.metamodel.SurveyTarget;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.Nodes;
import org.openforis.collect.model.RecordUpdater.RecordUpdateConfiguration;
import org.openforis.commons.collection.CollectionUtils;
import org.openforis.commons.collection.Predicate;
import org.openforis.commons.collection.Visitor;
import org.openforis.idm.metamodel.AttributeDefault;
import org.openforis.idm.metamodel.AttributeDefinition;
import org.openforis.idm.metamodel.CodeAttributeDefinition;
import org.openforis.idm.metamodel.IdmInterpretationError;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.Survey;
import org.openforis.idm.model.Attribute;
import org.openforis.idm.model.CodeAttribute;
import org.openforis.idm.model.Entity;
import org.openforis.idm.model.Node;
import org.openforis.idm.model.NodePointer;
import org.openforis.idm.model.NodePointers;
import org.openforis.idm.model.NodeVisitor;
import org.openforis.idm.model.Record;
import org.openforis.idm.model.Value;
import org.openforis.idm.model.expression.ExpressionEvaluator;
import org.openforis.idm.model.expression.InvalidExpressionException;

public class RecordDependentsUpdater {

	protected static final int MAX_DEPENDENT_NODE_VISITING_COUNT = 5;
	private static final Integer MAX_ATTRIBUTE_EVALUATION_COUNT = 5;
	private static final Integer MAX_ATTRIBUTE_EVALUATION_COUNT_CE = 50;

	private RecordUpdateConfiguration configuration;

	public RecordDependentsUpdater(RecordUpdateConfiguration configuration) {
		this.configuration = configuration;
	}
	
	public RecordDependentsUpdateResult updateDependents(CollectRecord record, NodePointer nodePointer) {
		return updateDependents(record, nodePointer, false);
	}

	
	public RecordDependentsUpdateResult updateDependents(CollectRecord record, NodePointer nodePointer, boolean nodePointerValueUpdated) {
		return updateDependents(record, Arrays.asList(nodePointer), nodePointerValueUpdated);
	}

	public RecordDependentsUpdateResult updateDependentsAndSelf(CollectRecord record, NodePointer nodePointer) {
		return updateDependentsAndSelf(record, Arrays.asList(nodePointer));
	}

	public RecordDependentsUpdateResult updateDependentsAndSelf(CollectRecord record, Collection<NodePointer> nodePointers) {
		// update relevance first
		Set<NodePointer> updatedRelevancePointers = new LinkedHashSet<NodePointer>();
		
		RelevanceUpdater relevanceUpdater = new RelevanceUpdater();
		
		for (NodePointer nodePointer : nodePointers) {
			if (relevanceUpdater.update(nodePointer)) {
				updatedRelevancePointers.add(nodePointer);
			}
		}
		
		RecordDependentsUpdateResult updateDependentsResult = updateDependents(record, nodePointers, false);
		updateDependentsResult.updatedRelevancePointers.addAll(updatedRelevancePointers);

		return updateDependentsResult;
	}
	
	public RecordDependentsUpdateResult updateDependents(CollectRecord record, Collection<NodePointer> nodePointers) {
		return updateDependents(record, nodePointers, false);
	}
	
	public RecordDependentsUpdateResult updateDependents(CollectRecord record, Collection<NodePointer> nodePointers, boolean nodePointerValueUpdated) {
		final Queue<NodePointer> queue = new UniqueQueue<NodePointer>();
		queue.addAll(nodePointers);
		
		final Counter<NodePointer> visitingCounter = new Counter<NodePointer>(MAX_DEPENDENT_NODE_VISITING_COUNT);
		CollectSurvey survey = (CollectSurvey) record.getSurvey();
		final Counter<Attribute<?, ?>> evaluationCounter = new Counter<Attribute<?, ?>>(
				survey.isCollectEarth() ? MAX_ATTRIBUTE_EVALUATION_COUNT_CE : MAX_ATTRIBUTE_EVALUATION_COUNT);

		final Set<NodePointer> totalUpdatedRelevancePointers = new LinkedHashSet<NodePointer>();
		final List<Attribute<?, ?>> totalUpdatedAttributes = new ArrayList<Attribute<?, ?>>();
		final Set<NodePointer> updatedRelevancePointersCurrentIteration = new HashSet<NodePointer>();
		final List<Attribute<?, ?>> updatedAttributesCurrentIteration = new ArrayList<Attribute<?, ?>>();
		
		if (nodePointerValueUpdated) {
			for (NodePointer nodePointer : nodePointers) {
				if (nodePointer.getChildDefinition() instanceof AttributeDefinition) {
					updatedAttributesCurrentIteration.addAll(filterAttributes(nodePointer.getNodes()));
				}
			}
		}
		
		final RelevanceUpdater relevanceUpdater = new RelevanceUpdater();
		
		while (!queue.isEmpty()) {
			final NodePointer visitedNodePointer = queue.remove();
			
			visitingCounter.increment(visitedNodePointer);
			
			final Visitor<NodePointer> nodePointerDependentVisitor = new Visitor<NodePointer>() {
				public void visit(NodePointer nodePointerDependent) {
					if (!nodePointerDependent.equals(visitedNodePointer) && !visitingCounter.isLimitReached(nodePointerDependent)) {
						queue.add(nodePointerDependent);
					}
				}
			};
		
			// relevance
			if (relevanceUpdater.update(visitedNodePointer)) {
				updatedRelevancePointersCurrentIteration.add(visitedNodePointer);
			}
			record.visitRelevanceDependencies(visitedNodePointer, nodePointerDependentVisitor);

			// default values
			if (visitedNodePointer.getChildDefinition() instanceof AttributeDefinition) {
				@SuppressWarnings("rawtypes")
				Collection nodes = visitedNodePointer.getNodes();
				@SuppressWarnings("unchecked")
				List<Attribute<?, ?>> recalculatedAttributes = recalculateValuesIfNecessary(nodes, evaluationCounter);
				updatedAttributesCurrentIteration.addAll(recalculatedAttributes);
			}
			record.visitDefaultValueDependencies(visitedNodePointer, nodePointerDependentVisitor);
			
			// clear not relevant attributes
			if (configuration.isClearNotRelevantAttributes()) {
				Collection<Attribute<?, ?>> nonRelevantAttributesCleared = clearNonRelevantAttributes(record, updatedRelevancePointersCurrentIteration);
				updatedAttributesCurrentIteration.addAll(nonRelevantAttributesCleared);
			}
			
			// clear dependent code attributes
			if (configuration.isClearDependentCodeAttributes()) {
				Set<CodeAttribute> clearedCodeAttributes = clearDependentCodeAttributes(visitedNodePointer, updatedAttributesCurrentIteration, nodePointerDependentVisitor);
				updatedAttributesCurrentIteration.addAll(clearedCodeAttributes);
			}
			totalUpdatedRelevancePointers.addAll(updatedRelevancePointersCurrentIteration);
			totalUpdatedAttributes.addAll(updatedAttributesCurrentIteration);

			updatedRelevancePointersCurrentIteration.clear();
			updatedAttributesCurrentIteration.clear();
		}
		return new RecordDependentsUpdateResult(totalUpdatedRelevancePointers, totalUpdatedAttributes);
	}
	
	private boolean calculateRelevance(NodePointer nodePointer) {
		NodeDefinition childDef = nodePointer.getChildDefinition();
		String expr = childDef.getRelevantExpression();
		if (StringUtils.isBlank(expr)) {
			return true;
		}
		try {
			Entity entity = nodePointer.getEntity();
			Survey survey = entity.getSurvey();
			ExpressionEvaluator expressionEvaluator = survey.getContext().getExpressionEvaluator();
			return expressionEvaluator.evaluateBoolean(entity, null, expr);
		} catch (InvalidExpressionException e) {
			throw new IdmInterpretationError(childDef.getPath() + " - Unable to evaluate expression: " + expr, e);
		}
	}

	private List<Attribute<?, ?>> recalculateValuesIfNecessary(Collection<Attribute<?, ?>> attributesToRecalculate, Counter<Attribute<?, ?>> evaluationCounter) {
		List<Attribute<?, ?>> updatedAttributes = new ArrayList<Attribute<?, ?>>();
		for (Attribute<?, ?> attr : attributesToRecalculate) {
			if (!evaluationCounter.isLimitReached(attr)) {
				evaluationCounter.increment(attr);
				Attribute<?, ?> updatedAttribute = recalculateValueIfNecessary(attr);
				if (updatedAttribute != null) {
					updatedAttributes.add(updatedAttribute);
				}
			}
		}
		return updatedAttributes;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private Attribute<?, ?> recalculateValueIfNecessary(Attribute attr) {
		AttributeDefinition def = (AttributeDefinition) attr.getDefinition();
		if (def.getAttributeDefaults().isEmpty()) {
			// ignore attributes without default values specified
			return null;
		}
		CollectSurvey survey = def.getSurvey();
		CollectAnnotations annotations = survey.getAnnotations();
		boolean relevant = isChildRelevant(attr.getParent(), def);
		
		if (relevant && !def.isCalculated() && !attr.isEmpty() && attr.isUserSpecified() && !attr.isDefaultValueApplied()) {
			// do not update attributes updated by the user
			return null;
		}
		Value previousValue = attr.getValue();
		Value newValue;
		if (relevant || configuration.isAlwaysEvaluateCalculatedAttributes()) {
			if (def.isCalculated() && !annotations.isCalculatedOnlyOneTime(def) || attr.isEmpty()) {
				// calculate value or default value
				newValue = recalculateValue(attr);
			} else {
				// keep old value
				return null;
			}
		} else if (!attr.isEmpty() && attr.isDefaultValueApplied() || (attr.isUserSpecified() && configuration.isClearNotRelevantAttributes())) {
			// clear non relevant attributes
			newValue = null;
		} else {
			// keep old value
			return null;
		}
		if ((previousValue != newValue) && (previousValue == null || !previousValue.equals(newValue))) {
			attr.setValue(newValue);
			boolean defaultValueApplied = newValue != null;
			attr.setDefaultValueApplied(defaultValueApplied);
			attr.updateSummaryInfo();
			return attr;
		}
		return null;
	}

	private Value recalculateValue(Attribute<?, ?> attribute) {
		try {
			AttributeDefinition defn = attribute.getDefinition();
			List<AttributeDefault> attributeDefaults = defn.getAttributeDefaults();
			for (AttributeDefault attributeDefault : attributeDefaults) {
				if (attributeDefault.evaluateCondition(attribute)) {
					return attributeDefault.evaluate(attribute);
				}
			}
			return null;
		} catch (InvalidExpressionException e) {
			throw new IllegalStateException(
					String.format("Invalid expression for calculated attribute %s", attribute.getPath()));
		}
	}
	
	private List<Attribute<?, ?>> clearNonRelevantAttributes(CollectRecord record, Collection<NodePointer> nodePointers) {
		final List<Attribute<?, ?>> updatedAttributes = new ArrayList<Attribute<?, ?>>();
		
		NodeVisitor valueClearVisitor = new NodeVisitor() {
			public void visit(Node<?> node, int idx) {
				if (node instanceof Attribute && node.isUserSpecified()) {
					Attribute<?, ?> attr = (Attribute<?, ?>) node;
					attr.clearValue();
					updatedAttributes.add(attr);
				}
			}
		};
		
		for (NodePointer nodePointer : nodePointers) {
			Entity entity = nodePointer.getEntity();
			NodeDefinition childDefinition = nodePointer.getChildDefinition();
			if (!entity.isRelevant() || !isChildRelevant(entity, childDefinition)) {
				List<Node<?>> nodes = nodePointer.getNodes();
				for (Node<?> node: nodes) {
					if (node instanceof Attribute) {
						valueClearVisitor.visit(node, 0);
					} else {
						((Entity) node).traverseDescendants(valueClearVisitor);
					}
				}
			}
		}
		return updatedAttributes;
	}

	private boolean isChildRelevant(Entity parentEntity, NodeDefinition childDefinition) {
		CollectSurvey survey = childDefinition.getSurvey();
		
		return parentEntity.isRelevant(childDefinition) ||
		// backwards compatibility with old Collect Earth surveys: hidden calculated
		// attributes were marked as always not-relevant
				survey.getAnnotations().getSurveyTarget() == SurveyTarget.COLLECT_EARTH
						&& childDefinition instanceof AttributeDefinition
						&& ((AttributeDefinition) childDefinition).isCalculated()
						&& StringUtils.trimToEmpty(childDefinition.getRelevantExpression()).equalsIgnoreCase("false()");
	}
	
	private Set<CodeAttribute> clearDependentCodeAttributes(Collection<CodeAttribute> codeAttributes) {
		Set<CodeAttribute> clearedCodeAttributes = new HashSet<CodeAttribute>();
		for (CodeAttribute codeAttribute : codeAttributes) {
			Record record = codeAttribute.getRecord();
			Set<CodeAttribute> dependentCodeAttributes = record.determineDependentChildCodeAttributes(codeAttribute);
			Set<CodeAttribute> updatedDependentCodeAttributes = clearUserSpecifiedAttributes(dependentCodeAttributes);
			clearedCodeAttributes.addAll(updatedDependentCodeAttributes);
		}
		return clearedCodeAttributes;
	}
	
	private Set<CodeAttribute> clearDependentCodeAttributes(NodePointer visitedNodePointer,
			Collection<Attribute<?,?>> updatedAttributesCurrentIteration, Visitor<NodePointer> nodePointerDependentVisitor) {
		Collection<CodeAttribute> updatedCodeAttributes = Nodes.filterCodeAttributes(updatedAttributesCurrentIteration);
		if (visitedNodePointer.getChildDefinition() instanceof CodeAttributeDefinition) {
			Collection<CodeAttribute> visitedEmptyCodeAttributes = Nodes.filterCodeAttributes(visitedNodePointer.getNodes());
			CollectionUtils.filter(visitedEmptyCodeAttributes, new Predicate<CodeAttribute>() {
				public boolean evaluate(CodeAttribute codeAttr) {
					return codeAttr.isEmpty();
				}
			});
			updatedCodeAttributes.addAll(visitedEmptyCodeAttributes);
		}
		Set<CodeAttribute> clearedCodeAttributes = clearDependentCodeAttributes(updatedCodeAttributes);
		Set<NodePointer> clearedNodePointers = NodePointers.nodesToPointers(clearedCodeAttributes);
		for (NodePointer clearedNodePointer : clearedNodePointers) {
			nodePointerDependentVisitor.visit(clearedNodePointer);
		}
		return clearedCodeAttributes;
	}
	
	private <T extends Node<?>> Collection<Attribute<?, ?>> filterAttributes(Collection<T> nodes) {
		Collection<Attribute<?,?>> attributes = new ArrayList<Attribute<?,?>>();
		for (Node<?> node : nodes) {
			if (node instanceof Attribute) {
				attributes.add((Attribute<?,?>) node);
			}
		}
		return attributes;
	}

	private <A extends Attribute<?, ?>> Set<A> clearUserSpecifiedAttributes(Set<A> attributes) {
		Set<A> updatedAttributes = new HashSet<A>();
		for (A attr : attributes) {
			if (attr.isUserSpecified() && ! attr.isEmpty()) {
				attr.clearValue();
				attr.updateSummaryInfo();
				updatedAttributes.add(attr);
			}
		}
		return updatedAttributes;
	}
	
	private class RelevanceUpdater {
		
		public boolean update(NodePointer nodePointer) {
			Entity entity = nodePointer.getEntity();
			NodeDefinition childDef = nodePointer.getChildDefinition();
			boolean oldRelevance = isChildRelevant(entity, childDef);
			boolean relevance = calculateRelevance(nodePointer);
			if (oldRelevance != relevance) {
				entity.setRelevant(childDef, relevance);
				return true;
			}
			return false;
		}
	}
	
	public static class RecordDependentsUpdateResult {

		Set<NodePointer> updatedRelevancePointers;
		List<Attribute<?, ?>> updatedAttributes;

		public RecordDependentsUpdateResult(Set<NodePointer> updatedRelevancePointers,
				List<Attribute<?, ?>> updatedAttributes) {
			this.updatedRelevancePointers = updatedRelevancePointers;
			this.updatedAttributes = updatedAttributes;
		}

		public Set<NodePointer> getUpdatedRelevancePointers() {
			return updatedRelevancePointers;
		}

		public List<Attribute<?, ?>> getUpdatedAttributes() {
			return updatedAttributes;
		}

	}
	
	private static class Counter<K> {
		private final Map<K, Integer> countByKey = new HashMap<K, Integer>();
		private final int maxCount;
		
		public Counter(int maxCount) {
			this.maxCount = maxCount;
		}
		
		public int increment(K key) {
			int count = getCount(key);
			countByKey.put(key, ++count);
			return count;
		}
	
		public int getCount(K key) {
			Integer count = countByKey.get(key);
			return count == null ? 0: count;
		}
		
		public boolean isLimitReached(K key) {
			return getCount(key) == maxCount;
		}
	}
}
