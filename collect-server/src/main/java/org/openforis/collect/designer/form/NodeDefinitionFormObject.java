package org.openforis.collect.designer.form;

import org.openforis.collect.designer.model.AttributeType;
import org.openforis.collect.designer.model.NamedObject;
import org.openforis.collect.designer.model.NodeType;
import org.openforis.collect.metamodel.ui.UIOptions;
import org.openforis.collect.metamodel.ui.UITab;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.idm.metamodel.ModelVersion;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.NodeLabel.Type;
import org.openforis.idm.metamodel.Prompt;

/**
 * 
 * @author S. Ricci
 *
 */
public abstract class NodeDefinitionFormObject<T extends NodeDefinition> extends SurveyObjectFormObject<T> {
	
	public static NamedObject INHERIT_TAB;
	
	static {
		//init static variables
		INHERIT_TAB = new NamedObject("survey.schema.node.tab.inherited");
	};
	//generic
	private String name;
	private String description;
	private boolean multiple;
	private boolean required;
	private String requiredExpression;
	private String relevantExpression;
	private Integer minCount;
	private Integer maxCount;
	//labels
	private String headingLabel;
	private String instanceLabel;
	private String numberLabel;
	private String interviewPromptLabel;
	private String paperPromptLabel;
	private String handheldPromptLabel;
	private String pcPromptLabel;
	//versioning
	private Object sinceVersion;
	private Object deprecatedVersion;
	//layout
	private Object tab;
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static NodeDefinitionFormObject<NodeDefinition> newInstance(NodeType nodeType, AttributeType attributeType) {
		NodeDefinitionFormObject<NodeDefinition> formObject = null;
		if ( nodeType != null ) {
			switch ( nodeType) {
			case ENTITY:
				formObject = new EntityDefinitionFormObject();
				break;
			case ATTRIBUTE:
				return (NodeDefinitionFormObject<NodeDefinition>) newInstance(attributeType);
			}
		}
		return formObject;
	}
	
	@SuppressWarnings({ "rawtypes" })
	public static AttributeDefinitionFormObject<?> newInstance(AttributeType attributeType) {
		AttributeDefinitionFormObject<?> formObject = null;
		if ( attributeType != null ) {
			switch (attributeType) {
			case BOOLEAN:
				formObject = new BooleanAttributeDefinitionFormObject();
				break;
			case CODE:
				formObject = new CodeAttributeDefinitionFormObject();
				break;
			case COORDINATE:
				formObject = new CoordinateAttributeDefinitionFormObject();
				break;
			case DATE:
				formObject = new DateAttributeDefinitionFormObject();
				break;
			case FILE:
				formObject = new FileAttributeDefinitionFormObject();
				break;
			case NUMBER:
				formObject = new NumberAttributeDefinitionFormObject();
				break;
			case RANGE:
				formObject = new RangeAttributeDefinitionFormObject();
				break;
			case TAXON:
				formObject = new TaxonAttributeDefinitionFormObject();
				break;
			case TEXT:
				formObject = new TextAttributeDefinitionFormObject();
				break;
			case TIME:
				formObject = new TimeAttributeDefinitionFormObject();
				break;
			default:
				throw new IllegalStateException("Attribute type not supported");
			}
		}
		return formObject;
	}
	
	@Override
	public void loadFrom(T source, String language, String defaultLanguage) {
		//generic
		name = source.getName();
		multiple = source.isMultiple();
		Integer nodeMinCount = source.getMinCount();
		required = nodeMinCount != null && nodeMinCount.intValue() > 0;
		requiredExpression = source.getRequiredExpression();
		relevantExpression = source.getRelevantExpression();
		minCount = nodeMinCount;
		maxCount = source.getMaxCount();
		//labels
		headingLabel = getLabel(source, Type.HEADING, language, defaultLanguage);
		instanceLabel = getLabel(source, Type.INSTANCE, language, defaultLanguage);
		numberLabel = getLabel(source, Type.NUMBER, language, defaultLanguage);
		interviewPromptLabel = getPrompt(source, Prompt.Type.INTERVIEW, language, defaultLanguage);
		paperPromptLabel = getPrompt(source, Prompt.Type.PAPER, language, defaultLanguage);
		handheldPromptLabel = getPrompt(source, Prompt.Type.HANDHELD, language, defaultLanguage);
		pcPromptLabel = getPrompt(source, Prompt.Type.PC, language, defaultLanguage);
		description = getDescription(source, language, defaultLanguage);
		//versioning
		sinceVersion = source.getSinceVersion();
		deprecatedVersion = source.getDeprecatedVersion();
		//layout
		UIOptions uiOptions = getUIOptions(source);
		tab = uiOptions.getTab(source, false);
		if ( tab == null ) {
			tab = INHERIT_TAB;
		}
	}

	protected String getLabel(T source, Type type, String languageCode, String defaultLanguage) {
		String result = source.getLabel(type, languageCode);
		if ( result == null && languageCode != null && languageCode.equals(defaultLanguage) ) {
			//try to get the label associated to default language
			result = source.getLabel(type, null);
		}
		return result;
	}

	protected String getPrompt(T source, Prompt.Type type, String languageCode, String defaultLanguage) {
		String result = source.getPrompt(type, languageCode);
		if ( result == null && languageCode != null && languageCode.equals(defaultLanguage) ) {
			//try to get the label associated to default language
			result = source.getPrompt(type, null);
		}
		return result;
	}
	
	protected String getDescription(T source, String languageCode, String defaultLanguage) {
		String result = source.getDescription(languageCode);
		if ( result == null && languageCode != null && languageCode.equals(defaultLanguage) ) {
			//try to get the label associated to default language
			result = source.getDescription(null);
		}
		return result;
	}
	
	@Override
	public void saveTo(T dest, String languageCode) {
		dest.setName(name);
		dest.setLabel(Type.HEADING, languageCode, headingLabel);
		dest.setLabel(Type.INSTANCE, languageCode, instanceLabel);
		dest.setLabel(Type.NUMBER, languageCode, numberLabel);
		dest.setPrompt(Prompt.Type.HANDHELD, languageCode, handheldPromptLabel);
		dest.setPrompt(Prompt.Type.INTERVIEW, languageCode, interviewPromptLabel);
		dest.setPrompt(Prompt.Type.PAPER, languageCode, paperPromptLabel);
		dest.setPrompt(Prompt.Type.PC, languageCode, pcPromptLabel);
		dest.setDescription(languageCode, description);
		dest.setMultiple(multiple);
		dest.setMinCount(null);
		dest.setMaxCount(null);
		dest.setRequiredExpression(null);
		if ( multiple ) {
			dest.setMinCount(minCount);
			dest.setMaxCount(maxCount);
		} else {
			dest.setRequiredExpression(requiredExpression);
		}
		dest.setSinceVersion(null);
		dest.setDeprecatedVersion(null);
		if ( sinceVersion != null && sinceVersion != VERSION_EMPTY_SELECTION ) {
			dest.setSinceVersion((ModelVersion) sinceVersion);
		}
		if ( deprecatedVersion != null && deprecatedVersion != VERSION_EMPTY_SELECTION ) {
			dest.setDeprecatedVersion((ModelVersion) deprecatedVersion);
		}
		UIOptions uiOptions = getUIOptions(dest);
		if ( tab == null || tab == INHERIT_TAB ) {
			uiOptions.removeTabAssociation(dest);
		} else {
			uiOptions.associateWithTab(dest, (UITab) tab);
		}
	}
	
	@Override
	protected void reset() {
		//TODO
	}
	
	protected UIOptions getUIOptions(NodeDefinition nodeDefn) {
		CollectSurvey survey = (CollectSurvey) nodeDefn.getSurvey();
		UIOptions uiOptions = survey.getUIOptions();
		return uiOptions;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getHeadingLabel() {
		return headingLabel;
	}
	
	public void setHeadingLabel(String headingLabel) {
		this.headingLabel = headingLabel;
	}
	
	public String getInstanceLabel() {
		return instanceLabel;
	}
	
	public void setInstanceLabel(String instanceLabel) {
		this.instanceLabel = instanceLabel;
	}
	
	public String getNumberLabel() {
		return numberLabel;
	}
	
	public void setNumberLabel(String numberLabel) {
		this.numberLabel = numberLabel;
	}
	
	public String getDescription() {
		return description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}
	
	public boolean isMultiple() {
		return multiple;
	}
	
	public void setMultiple(boolean multiple) {
		this.multiple = multiple;
	}
	
	public Object getSinceVersion() {
		return sinceVersion;
	}
	
	public void setSinceVersion(Object sinceVersion) {
		this.sinceVersion = sinceVersion;
	}
	
	public Object getDeprecatedVersion() {
		return deprecatedVersion;
	}
	
	public void setDeprecatedVersion(Object deprecatedVersion) {
		this.deprecatedVersion = deprecatedVersion;
	}

	public String getInterviewPromptLabel() {
		return interviewPromptLabel;
	}

	public void setInterviewPromptLabel(String interviewPromptLabel) {
		this.interviewPromptLabel = interviewPromptLabel;
	}

	public String getPaperPromptLabel() {
		return paperPromptLabel;
	}

	public void setPaperPromptLabel(String paperPromptLabel) {
		this.paperPromptLabel = paperPromptLabel;
	}

	public String getHandheldPromptLabel() {
		return handheldPromptLabel;
	}

	public void setHandheldPromptLabel(String handheldPromptLabel) {
		this.handheldPromptLabel = handheldPromptLabel;
	}

	public String getPcPromptLabel() {
		return pcPromptLabel;
	}

	public void setPcPromptLabel(String pcPromptLabel) {
		this.pcPromptLabel = pcPromptLabel;
	}

	public boolean isRequired() {
		return required;
	}

	public void setRequired(boolean required) {
		this.required =  required;
	}

	public String getRequiredExpression() {
		return requiredExpression;
	}

	public void setRequiredExpression(String requiredExpression) {
		this.requiredExpression = requiredExpression;
	}

	public String getRelevantExpression() {
		return relevantExpression;
	}

	public void setRelevantExpression(String relevantExpression) {
		this.relevantExpression = relevantExpression;
	}

	public Integer getMinCount() {
		return minCount;
	}

	public void setMinCount(Integer minCount) {
		this.minCount = minCount;
	}

	public Integer getMaxCount() {
		return maxCount;
	}

	public void setMaxCount(Integer maxCount) {
		this.maxCount = maxCount;
	}

	public Object getTab() {
		return tab;
	}

	public void setTab(Object tab) {
		this.tab = tab;
	}

}
