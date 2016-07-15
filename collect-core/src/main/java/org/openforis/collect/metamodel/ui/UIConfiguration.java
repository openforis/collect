package org.openforis.collect.metamodel.ui;

import static org.openforis.collect.metamodel.ui.UIConfigurationConstants.UI_CONFIGURATION_TYPE;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.openforis.collect.model.CollectSurvey;
import org.openforis.commons.collection.CollectionUtils;
import org.openforis.idm.metamodel.ApplicationOptions;

/**
 * 
 * @author S. Ricci
 *
 */
public class UIConfiguration implements ApplicationOptions, Serializable {

	private static final long serialVersionUID = 1L;
	
	private CollectSurvey survey;
	private List<UIFormSet> formSets;
	private Map<Integer, UIModelObject> modelObjectsById;
	private Map<Integer, UIModelObject> modelObjectsByNodeDefinitionId;
	private int lastId;
	
	public UIConfiguration() {
		this.modelObjectsById = new HashMap<Integer, UIModelObject>();
		this.modelObjectsByNodeDefinitionId = new HashMap<Integer, UIModelObject>();
	}
	
	public UIConfiguration(CollectSurvey survey) {
		this();
		this.survey = survey;
	}
	
	@Override
	public String getType() {
		return UI_CONFIGURATION_TYPE;
	}
	
	public CollectSurvey getSurvey() {
		return survey;
	}

	public void setSurvey(CollectSurvey survey) {
		this.survey = survey;
	}
	
	synchronized 
	protected int nextId() {
		return ++lastId;
	}
	
	public UIFormSet createFormSet() {
		return createFormSet(nextId());
	}
	
	public UIFormSet createFormSet(int id) {
		return new UIFormSet(this, id);
	}

	public List<UIFormSet> getFormSets() {
		return CollectionUtils.unmodifiableList(formSets);
	}
	
	public UIFormSet getMainFormSet() {
		if ( formSets == null || formSets.isEmpty()) {
			return null;
		} else {
			return formSets.get(0);
		}
	}
	
	public UIFormSet getFormSetByRootEntityId(int entityId) {
		if ( formSets != null ) {
			for (UIFormSet formSet : formSets) {
				if ( formSet.getRootEntityDefinition().getId() == entityId ) {
					return formSet;
				}
			}
		}
		return null;
	}
	
	public void addFormSet(UIFormSet formSet) {
		if ( formSets == null ) {
			formSets = new ArrayList<UIFormSet>();
		}
		formSets.add(formSet);
		attachItem(formSet);
	}
	
	public void removeFormSet(UIFormSet formSet) {
		formSets.remove(formSet);
		detachItem(formSet);
	}
	
	public UIModelObject getModelObjectById(int id) {
		return modelObjectsById.get(id);
	}

	public UIModelObject getModelObjectByNodeDefinitionId(int id) {
		return modelObjectsByNodeDefinitionId.get(id);
	}

	// Pre-order depth-first traversal from here down
	public void traverse(UIModelObjectVisitor visitor) {
		Deque<UIModelObject> stack = new LinkedList<UIModelObject>();
		
		// Initialize stack with form sets
		stack.addAll(getFormSets());

		while ( ! stack.isEmpty() ) {
			UIModelObject obj = stack.pop();
			
			// Pre-order operation
			visitor.visit(obj);
			
			if ( visitor.isStopped() ) {
				break;
			}
			
			if ( obj instanceof UIFormSection ) {
				UIFormSection section = (UIFormSection) obj;
				List<UIFormComponent> children = section.getChildren();
				for (UIFormComponent child : children) {
					stack.push((UIModelObject) child);
				}
				List<UIForm> forms = section.getForms();
				for (UIForm form : forms) {
					stack.push(form);
				}
			}
			if ( obj instanceof UITableHeadingContainer ) {
				List<UITableHeadingComponent> headingComponents = ((UITableHeadingContainer) obj).getHeadingComponents();
				for (UITableHeadingComponent tableHeadingComponent : headingComponents) {
					stack.push(tableHeadingComponent);
				}
			}
		}		
	}
	
	public void attachItem(Identifiable item) {
		int id = item.getId();
		if ( modelObjectsById.containsKey(id) ) {
			throw new IllegalArgumentException(String.format("UI object with id %d already attached to model", id));
		}
		modelObjectsById.put(id, (UIModelObject) item);

		if ( item instanceof NodeDefinitionUIComponent ) {
			int nodeDefnId = ((NodeDefinitionUIComponent) item).getNodeDefinitionId();
			if ( modelObjectsByNodeDefinitionId.containsKey(nodeDefnId) ) {
				throw new IllegalArgumentException(String.format("UI object associated to node definition with id %d already attached to model", nodeDefnId));
			}
			modelObjectsByNodeDefinitionId.put(nodeDefnId, (UIModelObject) item);
		}
	}
	
	public void detachItem(Identifiable item) {
		modelObjectsById.remove(item.getId());
		if ( item instanceof NodeDefinitionUIComponent ) {
			modelObjectsByNodeDefinitionId.remove(((NodeDefinitionUIComponent) item).getNodeDefinitionId());
		}
	}
	
	public static abstract class UIModelObjectVisitor {
		
		private boolean stopped;
		private UIModelObject lastItem = null;
		
		public UIModelObjectVisitor() {
			this.stopped = false;
		}
		
		public abstract void visit(UIModelObject object);

		void stop() {
			this.stopped = true;
		}

		public boolean isStopped() {
			return stopped;
		}
		
		public UIModelObject getLastItem() {
			return lastItem;
		}
		
		void setLastItem(UIModelObject lastItem) {
			this.lastItem = lastItem;
		}
		
	}
}
