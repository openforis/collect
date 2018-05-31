package org.openforis.collect.designer.component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.openforis.collect.designer.component.BasicTreeModel.AbstractNode;
import org.openforis.collect.designer.component.SchemaTreeModel.SchemaNodeData;
import org.openforis.collect.designer.component.SchemaTreeModel.SchemaTreeNode;
import org.openforis.collect.designer.util.Predicate;
import org.openforis.collect.manager.SurveyManager;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.SurveySummary;
import org.openforis.collect.model.User;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.ModelVersion;
import org.openforis.idm.metamodel.SurveyObject;

/**
 * 
 * @author S. Ricci
 *
 */
public abstract class SurveyObjectTreeModelCreator {

	protected SurveyManager surveyManager;
	protected User loggedUser;
	protected ModelVersion version;
	private Predicate<SurveyObject> disabledNodePredicate;
	protected Predicate<SurveyObject> includeNodePredicate;
	protected boolean includeEmptyNodes;
	protected boolean includeRootEntity;
	protected String labelLanguage;

	public SurveyObjectTreeModelCreator(SurveyManager surveyManager, User loggedUser, ModelVersion version,
			Predicate<SurveyObject> disabledNodePredicate,
			Predicate<SurveyObject> includeNodePredicate,
			boolean includeRootEntity, boolean includeEmptyNodes, String labelLanguage) {
		super();
		this.surveyManager = surveyManager;
		this.loggedUser = loggedUser;
		this.version = version;
		this.disabledNodePredicate = disabledNodePredicate;
		this.includeNodePredicate = includeNodePredicate;
		this.includeRootEntity = includeRootEntity;
		this.includeEmptyNodes = includeEmptyNodes;
		this.labelLanguage = labelLanguage;
	}

	public SchemaTreeModel createModel(boolean includeSurveys) {
		List<SurveySummary> surveys = new ArrayList<SurveySummary>(surveyManager.getSurveySummaries(labelLanguage, loggedUser));
		surveys.addAll(surveyManager.loadTemporarySummaries(labelLanguage, false, loggedUser));

		//sort summaries by name
		Collections.sort(surveys, new Comparator<SurveySummary>() {
			@Override
			public int compare(SurveySummary o1, SurveySummary o2) {
				return new CompareToBuilder()
					.append(o1.getName(), o2.getName())
					.append(o1.getProjectName(), o2.getProjectName())
					.append(!o1.isTemporary(), !o2.isTemporary())
					.toComparison();
			}
		});
		List<SchemaTreeNode> surveyNodes = new ArrayList<SchemaTreeNode>(surveys.size());
		for (SurveySummary survey : surveys) {
			String label = survey.getName() + (survey.getProjectName() != null ? " - " + survey.getProjectName() : "" ) + 
					(survey.isTemporary() ? " (temporary)" : "");
			surveyNodes.add(new SchemaTreeNode(new SchemaNodeData(survey, null, label, true, true), Collections.emptyList()));
		}
		SchemaTreeNode root = new SchemaTreeNode(null, surveyNodes);
		return new SchemaTreeModel(this, root, null, labelLanguage);
	}
	
	public SchemaTreeModel createModel(EntityDefinition rootEntity) {
		if ( rootEntity != null && (version == null || version.isApplicable(rootEntity)) ) {
			Collection<SchemaTreeNode> firstLevelTreeNodes = createFirstLevelNodes(rootEntity);
			SchemaTreeNode root = new SchemaTreeNode(null, firstLevelTreeNodes);
			return new SchemaTreeModel(this, root, rootEntity, labelLanguage);
		} else {
			return null;
		}
	}
	
	public AbstractNode<SchemaNodeData> createSurveyRootEntityNode(int surveyId) {
		CollectSurvey survey = surveyManager.loadSurvey(surveyId);
		EntityDefinition rootEntity = survey.getSchema().getFirstRootEntityDefinition();
		AbstractNode<SchemaNodeData> rootEntityNode = createNode(new SchemaNodeData(rootEntity, false, false, labelLanguage), false);
		return rootEntityNode;
	}
	
	public AbstractNode<SchemaNodeData> createNode(SchemaNodeData data, boolean defineEmptyChildrenForLeaves) {
		SchemaTreeNode node = null;
		SurveyObject surveyObject = data.getSurveyObject();
		if ( includeNodePredicate == null || includeNodePredicate.evaluate(surveyObject) ) {
			List<SchemaTreeNode> childNodes = createChildNodes(surveyObject);
			//create result
			if ( childNodes == null ) {
				node = new SchemaTreeNode(data);
			} else if ( childNodes.isEmpty() ) {
				if ( includeEmptyNodes ) {
					if ( defineEmptyChildrenForLeaves ) {
						node = new SchemaTreeNode(data, Collections.emptyList());
					} else {
						node = new SchemaTreeNode(data);
					}
				} else {
					node = null;
				}
			} else {
				node = new SchemaTreeNode(data, childNodes);
			}
		}
		if ( node != null && disabledNodePredicate != null ) {
			if ( disabledNodePredicate.evaluate(surveyObject) ) {
				node.setDisabled(true);
			}
		}
		return node;
	}

	protected abstract List<SchemaTreeNode> createFirstLevelNodes(EntityDefinition rootEntity);

	protected abstract List<SchemaTreeNode> createChildNodes(SurveyObject surveyObject);
}
