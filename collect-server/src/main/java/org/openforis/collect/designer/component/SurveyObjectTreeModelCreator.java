package org.openforis.collect.designer.component;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.openforis.collect.designer.component.BasicTreeModel.AbstractNode;
import org.openforis.collect.designer.component.SchemaTreeModel.SchemaNodeData;
import org.openforis.collect.designer.component.SchemaTreeModel.SchemaTreeNode;
import org.openforis.collect.designer.util.Predicate;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.ModelVersion;
import org.openforis.idm.metamodel.SurveyObject;

/**
 * 
 * @author S. Ricci
 *
 */
public abstract class SurveyObjectTreeModelCreator {

	protected ModelVersion version;
	private Predicate<SurveyObject> disabledNodePredicate;
	protected Predicate<SurveyObject> includeNodePredicate;
	protected boolean includeEmptyNodes;
	protected String labelLanguage;

	public SurveyObjectTreeModelCreator(ModelVersion version,
			Predicate<SurveyObject> disabledNodePredicate,
			Predicate<SurveyObject> includeNodePredicate,
			boolean includeEmptyNodes, String labelLanguage) {
		super();
		this.version = version;
		this.disabledNodePredicate = disabledNodePredicate;
		this.includeNodePredicate = includeNodePredicate;
		this.includeEmptyNodes = includeEmptyNodes;
		this.labelLanguage = labelLanguage;
	}

	public SchemaTreeModel createModel(EntityDefinition rootEntity) {
		if ( rootEntity != null && (version == null || version.isApplicable(rootEntity)) ) {
			Collection<AbstractNode<SchemaNodeData>> firstLevelTreeNodes = createFirstLevelNodes(rootEntity);
			SchemaTreeNode root = new SchemaTreeNode(null, firstLevelTreeNodes);
			SchemaTreeModel result = new SchemaTreeModel(this, root, rootEntity, labelLanguage);
			return result;
		} else {
			return null;
		}
	}
	
	public AbstractNode<SchemaNodeData> createNode(SchemaNodeData data, boolean defineEmptyChildrenForLeaves) {
		SchemaTreeNode node = null;
		SurveyObject surveyObject = data.getSurveyObject();
		if ( includeNodePredicate == null || includeNodePredicate.evaluate(surveyObject) ) {
			List<AbstractNode<SchemaNodeData>> childNodes = createChildNodes(surveyObject);
			//create result
			if ( childNodes == null ) {
				node = new SchemaTreeNode(data);
			} else if ( childNodes.isEmpty() ) {
				if ( includeEmptyNodes ) {
					if ( defineEmptyChildrenForLeaves ) {
						node = new SchemaTreeNode(data, Collections.<AbstractNode<SchemaNodeData>>emptyList());
					} else {
						node = new SchemaTreeNode(data);
					}
				} else {
					node = null;
				}
			} else {
				node = new SchemaTreeNode(data, (List<AbstractNode<SchemaNodeData>>) childNodes);
			}
		}
		if ( node != null && disabledNodePredicate != null ) {
			if ( disabledNodePredicate.evaluate(surveyObject) ) {
				node.setDisabled(true);
			}
		}
		return node;
	}

	protected abstract List<AbstractNode<SchemaNodeData>> createFirstLevelNodes(EntityDefinition rootEntity);

	protected abstract List<AbstractNode<SchemaNodeData>> createChildNodes(SurveyObject surveyObject);
}
