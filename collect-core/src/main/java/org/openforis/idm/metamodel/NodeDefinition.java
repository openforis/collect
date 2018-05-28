/**
 * 
 */
package org.openforis.idm.metamodel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.openforis.commons.lang.Numbers;
import org.openforis.idm.metamodel.NodeLabel.Type;
import org.openforis.idm.model.Node;
import org.openforis.idm.model.NodePathPointer;
import org.openforis.idm.model.expression.Expressions;
import org.openforis.idm.path.InvalidPathException;
import org.openforis.idm.path.Path;

/**
 * @author G. Miceli
 * @author M. Togna
 */
public abstract class NodeDefinition extends VersionableSurveyObject {

	private static final Pattern MIN_COUNT_FROM_REQUIRED_EXPRESSION_PATTERN = Pattern.compile("number\\((.+)\\)");
	private static final String MIN_COUNT_FROM_REQUIRED_EXPRESSION_FORMAT = "number(%s)";
	private static final String ALWAYS_REQUIRED_MIN_COUNT_EXPRESSION = "1";

	private static final long serialVersionUID = 1L;

	private NodeDefinition parentDefinition;
	private String name;
	private String relevantExpression;
	private boolean multiple;
	private String minCountExpression;
	private String maxCountExpression;
	private NodeLabelMap labels;
	private PromptMap prompts;
	private LanguageSpecificTextMap descriptions;
	
	//calculated properties
	private transient String path;
	private transient Integer fixedMaxCount;
	private transient Integer fixedMinCount;
	private transient boolean alwaysRequired;
	
	NodeDefinition(Survey survey, int id) {
		super(survey, id);
	}

	NodeDefinition(NodeDefinition nodeDef, int id) {
		super(nodeDef, id);
		this.parentDefinition = nodeDef.parentDefinition;
		this.name = nodeDef.name;
		this.relevantExpression = nodeDef.relevantExpression;
		this.multiple = nodeDef.multiple;
		setMinCountExpression(nodeDef.minCountExpression);
		setMaxCountExpression(nodeDef.maxCountExpression);
		this.labels = nodeDef.labels == null ? null: new NodeLabelMap(nodeDef.labels);
		this.prompts = nodeDef.prompts == null ? null: new PromptMap(nodeDef.prompts);
		this.descriptions = nodeDef.descriptions == null ? null: new LanguageSpecificTextMap(nodeDef.descriptions);
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
	
	public boolean isAlwaysRelevant() {
		return StringUtils.isBlank(this.relevantExpression) || Expressions.TRUE_FUNCTION.equals(this.relevantExpression);
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

	public String getMaxCountExpression() {
		return maxCountExpression;
	}
	
	public List<NodeLabel> getLabels() {
		if ( this.labels == null ) {
			return Collections.emptyList();
		} else {
			return labels.values();
		}
	}
	
	public void setLabels(List<NodeLabel> labels) {
		if (labels == null) {
			this.labels = null;
		}
		this.labels = new NodeLabelMap();
		for (NodeLabel nodeLabel : labels) {
			addLabel(nodeLabel);
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
	
	/**
	 * If the label in the specified language is not found returns the label in the survey default language,
	 * otherwise returns the name of the node definition
	 */
	public String getFailSafeLabel(String language) {
		return getFailSafeLabel(Type.INSTANCE, language);
	}
	
	/**
	 * If the label in the specified language is not found returns the label in the survey default language,
	 * otherwise returns the name of the node definition
	 */
	public String getFailSafeLabel(NodeLabel.Type type, String language) {
		if (labels == null) {
			return null;
		} else {
			String label = labels.getFailSafeText(type, language, getSurvey().getDefaultLanguage());
			return label == null ? name : label;
		}
	}

	public String getFailSafeLabel(NodeLabel.Type... types) {
		return getFailSafeLabel(getSurvey().getDefaultLanguage(), types);
	}
	
	public String getFailSafeLabel(String language, NodeLabel.Type... types) {
		for (NodeLabel.Type type : types) {
			String label = getLabel(type, language);
			if (label != null) {
				return label;
			}
		}
		return name;
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
	
	public void setPrompts(List<Prompt> prompts) {
		if (prompts == null) {
			this.prompts = null;
		}
		this.prompts = new PromptMap();
		for (Prompt prompt : prompts) {
			addPrompt(prompt);
		}
	}
	
	public String getPrompt(Prompt.Type type, String language) {
		return prompts == null ? null: prompts.getText(type, language);
	}

	/**
	 * Returns the prompt in the specified language or the one in the survey default language
	 */
	public String getFailSafePrompt(Prompt.Type type, String language) {
		return prompts == null ? null : prompts.getFailSafeText(type, language, getSurvey().getDefaultLanguage());
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
	
	public void setDescriptions(List<LanguageSpecificText> descriptions) {
		if (descriptions == null) {
			this.descriptions = null;
		}
		this.descriptions = new LanguageSpecificTextMap();
		for (LanguageSpecificText description : descriptions) {
			addDescription(description);
		}
	}
	
	public String getDescription() {
		return getDescription(null);
	}

	public String getDescription(String language) {
		return descriptions == null ? null: descriptions.getText(language, getSurvey().getDefaultLanguage());
	}
	
	public String getFailSafeDescription(String language) {
		return descriptions == null ? null : descriptions.getFailSafeText(language, getSurvey().getDefaultLanguage());
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
		sb.append(Path.SEPARATOR);
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

	/**
	 * Returns the ancestor definitions from the nearest parent entity to the root
	 */
	public List<EntityDefinition> getAncestorEntityDefinitions() {
		List<EntityDefinition> result = new ArrayList<EntityDefinition>();
		EntityDefinition currentParent = getParentEntityDefinition();
		while ( currentParent != null ) {
			result.add(currentParent);
			currentParent = currentParent.getParentEntityDefinition();
		}
		return result;
	}

	/**
	 * Returns the ancestor entity definitions (from bottom to top) up to the specified one (exclusive).
	 */
	public List<EntityDefinition> getAncestorEntityDefinitionsUpTo(EntityDefinition upToEntityDef) {
		List<EntityDefinition> result = new ArrayList<EntityDefinition>();
		EntityDefinition currentParent = getParentEntityDefinition();
		while ( currentParent != null && currentParent != upToEntityDef ) {
			result.add(currentParent);
			currentParent = currentParent.getParentEntityDefinition();
		}
		return result;
	}
	
	/**
	 * Returns the ancestor definitions from the root to the nearest parent entity
	 */
	public List<EntityDefinition> getAncestorEntityDefinitionsInReverseOrder() {
		List<EntityDefinition> result = getAncestorEntityDefinitions();
		if (result.size() > 1) {
			Collections.reverse(result); 
		}
		return result;
	}
	
	public EntityDefinition getNearestCommonAncestor(NodeDefinition nodeDefinition) {
		List<EntityDefinition> thisAncestorEntityDefinitions = this.getAncestorEntityDefinitions();
		List<EntityDefinition> otherAncestors = nodeDefinition.getAncestorEntityDefinitions();
		
		for (EntityDefinition thisAncestor : thisAncestorEntityDefinitions) {
			if (otherAncestors.contains(thisAncestor)) {
				return thisAncestor;
			}
		}
		return null;
	}
	
	public EntityDefinition getNearestAncestorMultipleEntity() {
		EntityDefinition currentParent = getParentEntityDefinition();
		while ( currentParent != null && ! currentParent.isRoot() && ! currentParent.isMultiple() ) {
			currentParent = currentParent.getParentEntityDefinition();
		}
		return currentParent;
	}

	public String getRelativePath(NodeDefinition target) {
		return Path.getRelativePath(getPath(), target.getPath());
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
	
	public Set<NodeDefinition> getRelevancySourceDefinitions() {
		return getSurvey().getRelevanceSourceNodeDefinitions(this);
	}
	
	public Set<NodePathPointer> getMinCountDependencies() {
		Survey survey = getSurvey();
		return survey.getMinCountDependencies(this);
	}

	public Set<NodePathPointer> getMaxCountDependencies() {
		Survey survey = getSurvey();
		return survey.getMaxCountDependencies(this);
	}
	
	public List<NodeDefinition> getMinCountDependentDefinitions() {
		List<NodeDefinition> result = new ArrayList<NodeDefinition>();
		for (NodePathPointer nodePathPointer : getMinCountDependencies()) {
			result.add(nodePathPointer.getReferencedNodeDefinition());
		}
		return result;
	}
	
	public List<NodeDefinition> getMaxCountDependentDefinitions() {
		List<NodeDefinition> result = new ArrayList<NodeDefinition>();
		for (NodePathPointer nodePathPointer : getMaxCountDependencies()) {
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

	public String extractRequiredExpression() {
		if (minCountExpression == null) {
			return null;
		}
		Matcher matcher = MIN_COUNT_FROM_REQUIRED_EXPRESSION_PATTERN.matcher(minCountExpression);
		if (matcher.matches()) {
			String expression = matcher.group(1);
			return expression;
		} else {
			return null;
		}
	}
	
	public void setRequiredExpression(String requiredExpression) {
		String minCountExp = requiredExpression == null ? null: String.format(MIN_COUNT_FROM_REQUIRED_EXPRESSION_FORMAT, requiredExpression);
		setMinCountExpression(minCountExp);
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
		updateMinCountRelativeFields();
	}

	public void setMaxCountExpression(String expression) {
		this.maxCountExpression = expression;
		updateMaxCountRelativeFields();
	}
	
	public boolean isAlwaysRequired() {
		return alwaysRequired;
	}
	
	public void setAlwaysRequired() {
		setMinCountExpression(ALWAYS_REQUIRED_MIN_COUNT_EXPRESSION);
	}
	
	public Integer getFixedMinCount() {
		return fixedMinCount;
	}
	
	public Integer getFixedMaxCount() {
		return fixedMaxCount;
	}
	
	protected void updateMaxCountRelativeFields() {
		if (StringUtils.isBlank(maxCountExpression)) {
			fixedMaxCount = null;
		} else {
			fixedMaxCount = Numbers.toIntegerObject(maxCountExpression);
		}
	}

	protected void updateMinCountRelativeFields() {
		if (StringUtils.isBlank(minCountExpression)) {
			alwaysRequired = false;
			fixedMinCount = null;
		} else if (ALWAYS_REQUIRED_MIN_COUNT_EXPRESSION.equals(minCountExpression)) {
			alwaysRequired = true;
			fixedMinCount = 1;
		} else {
			alwaysRequired = false;
			fixedMinCount = Numbers.toIntegerObject(minCountExpression);
		}
	}

	@Override
	public String toString() {
		return name;
	}
	
	@Override
	public boolean deepEquals(Object obj) {
		if (this == obj)
			return true;
		if (!super.deepEquals(obj))
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
		return true;
	}

}
