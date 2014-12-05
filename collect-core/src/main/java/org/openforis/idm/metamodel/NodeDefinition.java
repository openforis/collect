/**
 * 
 */
package org.openforis.idm.metamodel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.openforis.idm.model.Node;
import org.openforis.idm.model.NodePathPointer;
import org.openforis.idm.path.InvalidPathException;
import org.openforis.idm.path.Path;

/**
 * @author G. Miceli
 * @author M. Togna
 */
public abstract class NodeDefinition extends VersionableSurveyObject {
	private static final long serialVersionUID = 1L;

	private NodeDefinition parentDefinition;
	private String name;
	private String relevantExpression;
	private String requiredExpression;
	private boolean multiple;
	private String minCountExpression;
	private String maxCountExpression;
	private NodeLabelMap labels;
	private PromptMap prompts;
	private LanguageSpecificTextMap descriptions;
	private String path;
	
	NodeDefinition(Survey survey, int id) {
		super(survey, id);
	}

	public abstract Node<?> createNode();

	/**
	 * Initializes internal variables.
	 * It will be called after survey unmarshalling is complete
	 */
	protected void init() {}
	
	public boolean hasDependencies() {
		return false;
	}
	
	public NodeDefinition getDefinitionByPath(String path) throws InvalidPathException {
		Path p = Path.parse(path);
		return p.evaluate(this);
	}
	
	@Override
	void detach() {
		Schema schema = getSchema();
		if ( schema != null ) {
			schema.detach(this);
		}
		super.detach();
	}
	
	public void rename(String newName) {
		String oldName = this.name;
		EntityDefinition parent = getParentEntityDefinition();
		if ( parent != null ) {
			parent.renameChild(oldName, newName);
		}
		this.name = newName;
		resetPath();
	}

	public String getName() {
		return this.name;
	}

	public String getRelevantExpression() {
		return this.relevantExpression;
	}

	public String getRequiredExpression() {
		return requiredExpression;
	}

	/**
	 * This property must be true for root entities
	 */
	public boolean isMultiple() {
		return multiple;
	}

	public String getMinCountExpression() {
		return minCountExpression;
	}

	public boolean hasMinCount() {
		return minCountExpression != null;
	}
	
	public String getMaxCountExpression() {
		return maxCountExpression;
	}
	
	public boolean hasMaxCount() {
		return maxCountExpression != null;
	}
	
	public List<NodeLabel> getLabels() {
		if ( this.labels == null ) {
			return Collections.emptyList();
		} else {
			return labels.values();
		}
	}
	
	/**
	 * Return the label of the specified type in the default language
	 */
	public String getLabel(NodeLabel.Type type) {
		String defaultLanguage = getSurvey().getDefaultLanguage();
		return getLabel(type, defaultLanguage);
	}
	
	/**
	 * Return the label of the specified type in the specified language
	 */
	public String getLabel(NodeLabel.Type type, String language) {
		return labels == null ? null: labels.getText(type, language);
	}
	
	public void setLabel(NodeLabel.Type type, String language, String text) {
		if ( labels == null ) {
			labels = new NodeLabelMap();
		}
		labels.setText(type, language, text);
	}

	public void addLabel(NodeLabel label) {
		if ( labels == null ) {
			labels = new NodeLabelMap();
		}
		labels.add(label);
	}

	public void removeLabel(NodeLabel.Type type, String language) {
		if (labels != null ) {
			labels.remove(type, language);
		}
	}

	public List<Prompt> getPrompts() {
		if ( this.prompts == null ) {
			return Collections.emptyList();
		} else {
			return this.prompts.values();
		}
	}
	
	public String getPrompt(Prompt.Type type, String language) {
		return prompts == null ? null: prompts.getText(type, language);
	}
	
	public void setPrompt(Prompt.Type type, String language, String text) {
		if ( prompts == null ) {
			prompts = new PromptMap();
		}
		prompts.setText(type, language, text);
	}

	public void addPrompt(Prompt prompt) {
		if ( prompts == null ) {
			prompts = new PromptMap();
		}
		prompts.add(prompt);
	}

	public void removePrompt(Prompt.Type type, String language) {
		if (prompts != null ) {
			prompts.remove(type, language);
		}
	}
	
	public List<LanguageSpecificText> getDescriptions() {
		if ( this.descriptions == null ) {
			return Collections.emptyList();
		} else {
			return this.descriptions.values();
		}
	}
	
	public String getDescription() {
		String defaultLanguage = getSurvey().getDefaultLanguage();
		return getDescription(defaultLanguage);
	}

	public String getDescription(String language) {
		if ( language == null ) {
			language = getSurvey().getDefaultLanguage();
		}
		return descriptions == null ? null: descriptions.getText(language);
	}
	
	public void setDescription(String language, String description) {
		if ( descriptions == null ) {
			descriptions = new LanguageSpecificTextMap();
		}
		descriptions.setText(language, description);
	}
	
	public void addDescription(LanguageSpecificText description) {
		if ( descriptions == null ) {
			descriptions = new LanguageSpecificTextMap();
		}
		descriptions.add(description);
	}

	public void removeDescription(String language) {
		descriptions.remove(language);
	}
	
	public String getPath() {
		if ( path == null ) {
			updatePath();
		}
		return path;
	}
	
	protected void updatePath() {
		StringBuilder sb = new StringBuilder(64);
		if ( parentDefinition != null ) {
			sb.append(parentDefinition.getPath());
		}
		sb.append('/');
		sb.append(getName());
		this.path = sb.toString();
	}
	
	protected void resetPath() {
		this.path = null;
	}
	
	public NodeDefinition getParentDefinition() {
		return this.parentDefinition;
	}
	
	protected void setParentDefinition(NodeDefinition parentDefinition) {
		this.parentDefinition = parentDefinition;
		resetPath();
	}
	
	public EntityDefinition getRootEntity() {
		NodeDefinition ptr = this;
		while ( ptr.getParentDefinition() != null ) {
			ptr = ptr.getParentDefinition();
		}
		return (EntityDefinition) ptr;
	}
	
	public EntityDefinition getParentEntityDefinition() {
		NodeDefinition currentParent = getParentDefinition();
		while (currentParent != null ) {
			if ( currentParent instanceof EntityDefinition ) {
				return (EntityDefinition) currentParent;
			} else {
				currentParent = currentParent.getParentDefinition();
			}
		}
		return null;
	}
	
	public boolean isDescendantOf(EntityDefinition entityDefn) {
		NodeDefinition parent = getParentDefinition();
		while ( parent != null ) {
			if ( parent == entityDefn ) {
				return true;
			}
			parent = parent.getParentEntityDefinition();
		}
		return false;
	}

	public List<EntityDefinition> getAncestorEntityDefinitions() {
		List<EntityDefinition> result = new ArrayList<EntityDefinition>();
		EntityDefinition currentParent = getParentEntityDefinition();
		while ( currentParent != null ) {
			result.add(0, currentParent);
			currentParent = currentParent.getParentEntityDefinition();
		}
		return result;
	}
	
	public EntityDefinition getNearestCommonAncestor(NodeDefinition nodeDefinition) {
		List<EntityDefinition> thisAncestorEntityDefinitions = this.getAncestorEntityDefinitions();
		List<EntityDefinition> otherAncestors = nodeDefinition.getAncestorEntityDefinitions();
		EntityDefinition lastCommonAncestor = null;
		for (int i = 0; i < thisAncestorEntityDefinitions.size(); i++) {
			if ( otherAncestors.size() > i ) {
				EntityDefinition thisAncestor = thisAncestorEntityDefinitions.get(i);
				EntityDefinition otherAncestor = otherAncestors.get(i);
				if ( thisAncestor == otherAncestor ) {
					lastCommonAncestor = thisAncestor;
				} else {
					break;
				}
			} else {
				break;
			}
		}
		return lastCommonAncestor;
	}
	
	public EntityDefinition getNearestAncestorMultipleEntity() {
		EntityDefinition currentParent = getParentEntityDefinition();
		while ( currentParent != null && ! currentParent.isRoot() && ! currentParent.isMultiple() ) {
			currentParent = currentParent.getParentEntityDefinition();
		}
		return currentParent;
	}

	public String getRelativePath(NodeDefinition target) {
		String result = Path.getRelativePath(getPath(), target.getPath());
		return result;
	}

	public Set<NodePathPointer> getRelevantExpressionDependencies() {
		Survey survey = getSurvey();
		return survey.getRelevanceDependencies(this);
	}
	
	public List<NodeDefinition> getRelevancyDependentDefinitions() {
		List<NodeDefinition> result = new ArrayList<NodeDefinition>();
		for (NodePathPointer nodePathPointer : this.getRelevantExpressionDependencies()) {
			result.add(nodePathPointer.getReferencedNodeDefinition());
		}
		return result;
	}

	public Set<NodePathPointer> getRequiredExpressionDependencies() {
		Survey survey = getSurvey();
		return survey.getRequiredDependencies(this);
	}

	public List<NodeDefinition> getRequirenessDependentDefinitions() {
		List<NodeDefinition> result = new ArrayList<NodeDefinition>();
		for (NodePathPointer nodePathPointer : this.getRequiredExpressionDependencies()) {
			result.add(nodePathPointer.getReferencedNodeDefinition());
		}
		return result;
	}
	
	public Set<NodePathPointer> getCalculatedValueDependencies() {
		Survey survey = getSurvey();
		return survey.getCalculatedValueDependencies(this);
	}

	public List<NodeDefinition> getCalculatedValueDependentDefinitions() {
		List<NodeDefinition> result = new ArrayList<NodeDefinition>();
		for (NodePathPointer nodePathPointer : this.getCalculatedValueDependencies()) {
			result.add(nodePathPointer.getReferencedNodeDefinition());
		}
		return result;
	}
	
	public Set<NodePathPointer> getCheckDependencies() {
		Survey survey = getSurvey();
		return survey.getValidationDependencies(this);
	}
	
	public List<NodeDefinition> getCheckDependentDefinitions() {
		List<NodeDefinition> result = new ArrayList<NodeDefinition>();
		for (NodePathPointer nodePathPointer : this.getCheckDependencies()) {
			result.add(nodePathPointer.getReferencedNodeDefinition());
		}
		return result;
	}
	
	public void setName(String name) {
		this.name = name;
		resetPath();
	}

	public void setRelevantExpression(String relevantExpression) {
		this.relevantExpression = relevantExpression;
	}

	public void setRequiredExpression(String requiredExpression) {
		this.requiredExpression = requiredExpression;
	}

	/**
	 * This property is meaningless for root entities
	 * @param multiple 
	 */
	public void setMultiple(boolean multiple) {
		this.multiple = multiple;
	}

	public void setMinCountExpression(String expression) {
		this.minCountExpression = expression;
	}

	public void setMaxCountExpression(String expression) {
		this.maxCountExpression = expression;
	}
	
	public boolean isAlwaysRequired() {
		return minCountExpression != null && "1".equals(minCountExpression);
	}
	
	public void setAlwaysRequired() {
		this.minCountExpression = "1";
	}
	
	@Override
	public String toString() {
		return name;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((descriptions == null) ? 0 : descriptions.hashCode());
		result = prime * result + getId();
		result = prime * result + ((labels == null) ? 0 : labels.hashCode());
		result = prime * result + ((maxCountExpression == null) ? 0 : maxCountExpression.hashCode());
		result = prime * result + ((minCountExpression == null) ? 0 : minCountExpression.hashCode());
		result = prime * result + (multiple ? 1231 : 1237);
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((prompts == null) ? 0 : prompts.hashCode());
		result = prime * result + ((relevantExpression == null) ? 0 : relevantExpression.hashCode());
		result = prime * result + ((requiredExpression == null) ? 0 : requiredExpression.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		NodeDefinition other = (NodeDefinition) obj;
		if (descriptions == null) {
			if (other.descriptions != null)
				return false;
		} else if (!descriptions.equals(other.descriptions))
			return false;
		if (getId() != other.getId())
			return false;
		if (labels == null) {
			if (other.labels != null)
				return false;
		} else if (!labels.equals(other.labels))
			return false;
		if (maxCountExpression == null) {
			if (other.maxCountExpression != null)
				return false;
		} else if (!maxCountExpression.equals(other.maxCountExpression))
			return false;
		if (minCountExpression == null) {
			if (other.minCountExpression != null)
				return false;
		} else if (!minCountExpression.equals(other.minCountExpression))
			return false;
		if (multiple!=other.multiple)
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (prompts == null) {
			if (other.prompts != null)
				return false;
		} else if (!prompts.equals(other.prompts))
			return false;
		if (relevantExpression == null) {
			if (other.relevantExpression != null)
				return false;
		} else if (!relevantExpression.equals(other.relevantExpression))
			return false;
		if (requiredExpression == null) {
			if (other.requiredExpression != null)
				return false;
		} else if (!requiredExpression.equals(other.requiredExpression))
			return false;
		return true;
	}

}
