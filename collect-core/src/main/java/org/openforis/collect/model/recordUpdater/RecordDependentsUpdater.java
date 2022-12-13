package org.openforis.collect.model.recordUpdater;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.openforis.collect.metamodel.CollectAnnotations;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.RecordUpdater.RecordUpdateConfiguration;
import org.openforis.commons.collection.Visitor;
import org.openforis.idm.metamodel.AttributeDefault;
import org.openforis.idm.metamodel.AttributeDefinition;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.IdmInterpretationError;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.Survey;
import org.openforis.idm.model.Attribute;
import org.openforis.idm.model.Entity;
import org.openforis.idm.model.Node;
import org.openforis.idm.model.NodePointer;
import org.openforis.idm.model.Value;
import org.openforis.idm.model.expression.ExpressionEvaluator;
import org.openforis.idm.model.expression.InvalidExpressionException;

public class RecordDependentsUpdater {

	protected static final int MAX_DEPENDENT_NODE_VISITING_COUNT = 2;

	private RecordUpdateConfiguration configuration;

	public RecordDependentsUpdater(RecordUpdateConfiguration configuration) {
		this.configuration = configuration;
	}
	
	public RecordDependentsUpdateResult updateDependentsAndSelf(CollectRecord record, NodePointer nodePointer) {
		return updateDependentsAndSelf(record, Arrays.asList(nodePointer));
	}
	
	public RecordDependentsUpdateResult updateDependentsAndSelf(CollectRecord record, Collection<NodePointer> nodePointers) {
		final Queue<NodePointer> queue = new LinkedList<NodePointer>();
		queue.addAll(nodePointers);

		final Set<NodePointer> updatedRelevancePointers = new LinkedHashSet<NodePointer>();
		final List<Attribute<?, ?>> updatedAttributes = new ArrayList<Attribute<?, ?>>();

		final Map<NodePointer, Integer> visitingCountByNodePointer = new HashMap<NodePointer, Integer>();

		while (!queue.isEmpty()) {
			NodePointer visitedNodePointer = queue.remove();
			Integer visitingCount = visitingCountByNodePointer.containsKey(visitedNodePointer)
					? visitingCountByNodePointer.get(visitedNodePointer)
					: 0;
			visitingCountByNodePointer.put(visitedNodePointer, visitingCount + 1);

			final Visitor<NodePointer> nodePointerDependentVisitor = new Visitor<NodePointer>() {
				public void visit(NodePointer nodePointerDependent) {
					Integer visitedCount = visitingCountByNodePointer.get(nodePointerDependent);
					if (visitedCount == null || visitedCount < MAX_DEPENDENT_NODE_VISITING_COUNT) {
						queue.add(nodePointerDependent);
					}
				}
			};
			// update relevance
			Set<NodePointer> updatedDependentRelevancePointers = updateRelevanceDependents(record, visitedNodePointer, nodePointerDependentVisitor);
			updatedRelevancePointers.addAll(updatedDependentRelevancePointers);

			// update default values
			Set<Attribute<?, ?>> dependentAttributesUpdated = updateDependentDefaultValues(
					record, visitedNodePointer, updatedDependentRelevancePointers, nodePointerDependentVisitor);
			updatedAttributes.addAll(dependentAttributesUpdated);
		}
		return new RecordDependentsUpdateResult(updatedRelevancePointers, updatedAttributes);
	}

	private Set<NodePointer> updateRelevanceDependents(CollectRecord record, NodePointer nodePointer,
			final Visitor<NodePointer> nodePointerDependentVisitor) {
		final Set<NodePointer> updatedRelevancePointers = new LinkedHashSet<NodePointer>();

		// update relevance
		record.visitRelevanceDependenciesAndSelf(nodePointer, new Visitor<NodePointer>() {
			public void visit(NodePointer nodePointerVisited) {
				Entity entity = nodePointerVisited.getEntity();
				NodeDefinition childDef = nodePointerVisited.getChildDefinition();
				boolean oldRelevance = entity.isRelevant() && entity.isRelevant(childDef);
				boolean relevance = entity.isRelevant() && calculateRelevance(nodePointerVisited);
				if (oldRelevance != relevance) {
					entity.setRelevant(childDef, relevance);

					nodePointerDependentVisitor.visit(nodePointerVisited);
					updatedRelevancePointers.add(nodePointerVisited);

					if (childDef instanceof EntityDefinition) {
						List<Node<?>> childNodes = nodePointerVisited.getNodes();
						for (Node<?> childNode : childNodes) {
							Entity childEntity = (Entity) childNode;
							EntityDefinition childDefinition = childEntity.getDefinition();
							List<NodeDefinition> nestedChildDefinitions = childDefinition.getChildDefinitions();
							for (NodeDefinition nodestedChildDefinition : nestedChildDefinitions) {
								updatedRelevancePointers.add(new NodePointer(childEntity, nodestedChildDefinition));
							}

						}
					}
				}
			}
		});
		return updatedRelevancePointers;
	}

	private Set<Attribute<?, ?>> updateDependentDefaultValues(CollectRecord record, NodePointer nodePointer,
			Collection<NodePointer> updatedRelevancePointers, final Visitor<NodePointer> nodePointerDependentVisitor) {
		final Set<Attribute<?, ?>> updatedAttributes = new HashSet<Attribute<?, ?>>();

		Visitor<NodePointer> defaultValueApplyVisitor = new Visitor<NodePointer>() {
			@SuppressWarnings("unchecked")
			public void visit(NodePointer nodePointerVisited) {
				@SuppressWarnings("rawtypes")
				Collection nodes = nodePointerVisited.getNodes();
				List<Attribute<?, ?>> recalculatedAttributes = recalculateValues(nodes);
				updatedAttributes.addAll(recalculatedAttributes);

				nodePointerDependentVisitor.visit(nodePointerVisited);
			}
		};
		
		for (NodePointer updatedRelevancePointer : updatedRelevancePointers) {
			NodeDefinition referencedNodeDef = updatedRelevancePointer.getChildDefinition();
			if (referencedNodeDef instanceof AttributeDefinition
					&& !((AttributeDefinition) referencedNodeDef).getAttributeDefaults().isEmpty()) {
				defaultValueApplyVisitor.visit(updatedRelevancePointer);
			}
		}

		record.visitDefaultValueDependenciesAndSelf(nodePointer, defaultValueApplyVisitor);

		return updatedAttributes;
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

	private List<Attribute<?, ?>> recalculateValues(Collection<Attribute<?, ?>> attributesToRecalculate) {
		List<Attribute<?, ?>> updatedAttributes = new ArrayList<Attribute<?, ?>>();
		for (Attribute<?, ?> attr : attributesToRecalculate) {
			Attribute<?, ?> updatedAttribute = recalculateValueIfNecessary(attr);
			if (updatedAttribute != null) {
				updatedAttributes.add(updatedAttribute);
			}
		}
		return updatedAttributes;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private Attribute<?, ?> recalculateValueIfNecessary(Attribute attr) {
		CollectSurvey survey = (CollectSurvey) attr.getSurvey();
		CollectAnnotations annotations = survey.getAnnotations();
		Value previousValue = attr.getValue();
		Value newValue;
		if (!attr.isRelevant() && !attr.isEmpty()
				&& ((configuration.isClearNotRelevantAttributes() && attr.isUserSpecified()) || attr.isDefaultValueApplied())) {
			newValue = null;
		} else if (!annotations.isCalculatedOnlyOneTime(attr.getDefinition()) || attr.isEmpty()) {
			newValue = recalculateValue(attr);
		} else {
			newValue = previousValue;
		}
		if (!((previousValue == newValue) || (previousValue != null && previousValue.equals(newValue)))) {
			attr.setValue(newValue);
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
					Value value = attributeDefault.evaluate(attribute);
					return value;
				}
			}
			return null;
		} catch (InvalidExpressionException e) {
			throw new IllegalStateException(
					String.format("Invalid expression for calculated attribute %s", attribute.getPath()));
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
}
