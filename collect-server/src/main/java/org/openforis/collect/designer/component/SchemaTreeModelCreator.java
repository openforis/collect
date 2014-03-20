package org.openforis.collect.designer.component;

import java.util.ArrayList;
import java.util.List;

import org.openforis.collect.designer.component.BasicTreeModel.AbstractNode;
import org.openforis.collect.designer.component.SchemaTreeModel.SchemaNodeData;
import org.openforis.collect.designer.component.SchemaTreeModel.SchemaTreeNode;
import org.openforis.collect.designer.util.Predicate;
import org.openforis.idm.metamodel.AttributeDefinition;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.ModelVersion;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.SurveyObject;

/**
 * 
 * @author S. Ricci
 *
 */
public class SchemaTreeModelCreator extends SurveyObjectTreeModelCreator {
	
	public SchemaTreeModelCreator(ModelVersion version,
			Predicate<SurveyObject> includeNodePredicate,
			boolean includeEmptyNodes, String labelLanguage) {
		this(version, null, includeNodePredicate, includeEmptyNodes, labelLanguage);
	}

	public SchemaTreeModelCreator(ModelVersion version,
			Predicate<SurveyObject> disabledNodePredicate,
			Predicate<SurveyObject> includeNodePredicate,
			boolean includeEmptyNodes, String labelLanguage) {
		super(version, disabledNodePredicate, includeNodePredicate, includeEmptyNodes, labelLanguage);
	}

	@Override
	protected List<AbstractNode<SchemaNodeData>> createFirstLevelNodes(EntityDefinition rootEntity) {
		List<AbstractNode<SchemaNodeData>> result = createChildNodes(rootEntity);
		return result;
	}

	@Override
	protected List<AbstractNode<SchemaNodeData>> createChildNodes(SurveyObject surveyObject) {
		List<AbstractNode<SchemaNodeData>> childNodes = new ArrayList<AbstractNode<SchemaNodeData>>();
		if ( surveyObject instanceof EntityDefinition ) {
			List<NodeDefinition> childDefinitions = ((EntityDefinition) surveyObject).getChildDefinitions();
			for (NodeDefinition nodeDefn : childDefinitions) {
				if ( version == null || version.isApplicable(nodeDefn) ) {
					SchemaNodeData data = new SchemaNodeData(nodeDefn, nodeDefn.getName(), false, false);
					SchemaTreeNode childNode = (SchemaTreeNode) createNode(data, false);
					childNodes.add(childNode);
				}
			}
		} else if ( surveyObject instanceof AttributeDefinition ) {
			//no nested nodes for attributes
			childNodes = null;
		}
		return childNodes;
	}

}
