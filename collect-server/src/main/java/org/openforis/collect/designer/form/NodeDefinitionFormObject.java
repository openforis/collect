package org.openforis.collect.designer.form;

import org.openforis.collect.designer.model.AttributeType;
import org.openforis.collect.designer.model.NodeType;
import org.openforis.collect.metamodel.ui.UITab;
import org.openforis.idm.metamodel.ModelVersion;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.NodeLabel.Type;
import org.openforis.idm.metamodel.Prompt;
import org.zkoss.util.resource.Labels;

/**
 * 
 * @author S. Ricci
 *
 */
public abstract class NodeDefinitionFormObject<T extends NodeDefinition> extends SurveyObjectFormObject<T> {
	
	public static UITab INHERIT_TAB;
	{
		//init static variables
		INHERIT_TAB = new UITab();
		INHERIT_TAB.setName(Labels.getLabel("survey.configuration.tab.inherit"));
	};
	
	private String name;
	private String headingLabel;
	private String instanceLabel;
	private String numberLabel;
	private String interviewPromptLabel;
	private String paperPromptLabel;
	private String handheldPromptLabel;
	private String pcPromptLabel;
	private String description;
	private boolean multiple;
	private boolean required;
	private String requiredExpression;
	private String relevantExpression;
	private Object sinceVersion;
	private Object deprecatedVersion;
	private Integer minCount;
	private Integer maxCount;
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static NodeDefinitionFormObject<NodeDefinition> newInstance(NodeType nodeType, AttributeType attributeType) {
		NodeDefinitionFormObject<NodeDefinition> formObject = null;
		if ( nodeType != null ) {
			switch ( nodeType) {
			case ENTITY:
				formObject = new EntityDefinitionFormObject();
				break;
			case ATTRIBUTE:
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
				break;
			}
		}
		return formObject;
	}
	
	@Override
	public void loadFrom(T source, String languageCode) {
		name = source.getName();
		headingLabel = source.getLabel(Type.HEADING, languageCode);
		instanceLabel = source.getLabel(Type.INSTANCE, languageCode);
		numberLabel = source.getLabel(Type.NUMBER, languageCode);
		interviewPromptLabel = source.getPrompt(Prompt.Type.INTERVIEW, languageCode);
		paperPromptLabel = source.getPrompt(Prompt.Type.PAPER, languageCode);
		handheldPromptLabel = source.getPrompt(Prompt.Type.HANDHELD, languageCode);
		pcPromptLabel = source.getPrompt(Prompt.Type.PC, languageCode);
		description = source.getDescription(languageCode);
		multiple = source.isMultiple();
		Integer nodeMinCount = source.getMinCount();
		required = nodeMinCount != null && nodeMinCount.intValue() == 1;
		requiredExpression = source.getRequiredExpression();
		relevantExpression = source.getRelevantExpression();
		minCount = nodeMinCount;
		maxCount = source.getMaxCount();
		sinceVersion = source.getSinceVersion();
		deprecatedVersion = source.getDeprecatedVersion();
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
		if ( multiple ) {
			dest.setMinCount(minCount);
			dest.setMaxCount(maxCount);
//			dest.setRequired(null);
			dest.setRequiredExpression(null);
		} else {
			dest.setMinCount(null);
			dest.setMaxCount(null);
//			dest.setRequired(required);
			dest.setRequiredExpression(requiredExpression);
		}
		if ( sinceVersion != null && sinceVersion != VERSION_EMPTY_SELECTION ) {
			dest.setSinceVersion((ModelVersion) sinceVersion);
		} else {
			dest.setSinceVersion(null);
		}
		if ( deprecatedVersion != null && deprecatedVersion != VERSION_EMPTY_SELECTION ) {
			dest.setDeprecatedVersion((ModelVersion) deprecatedVersion);
		} else {
			dest.setDeprecatedVersion(null);
		}
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

}
