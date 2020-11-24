package org.openforis.collect.metamodel.view;

import org.openforis.collect.designer.metamodel.NodeType;

public abstract class NodeDefView extends SurveyObjectView {

	private String name;
	private String label;
	private NodeType type;
	private boolean key;
	private boolean multiple;
	private boolean hideWhenNotRelevant;
	private Integer sinceVersionId;
	private Integer deprecatedVersionId;

	public NodeDefView(int id, String name, String label, NodeType type, boolean key, boolean multiple) {
		super();
		this.id = id;
		this.name = name;
		this.label = label;
		this.type = type;
		this.key = key;
		this.multiple = multiple;
	}

	public String getName() {
		return name;
	}

	public String getLabel() {
		return label;
	}

	public NodeType getType() {
		return type;
	}

	public boolean isKey() {
		return key;
	}

	public boolean isMultiple() {
		return multiple;
	}

	public boolean isHideWhenNotRelevant() {
		return hideWhenNotRelevant;
	}

	public void setHideWhenNotRelevant(boolean hideWhenNotRelevant) {
		this.hideWhenNotRelevant = hideWhenNotRelevant;
	}

	public Integer getSinceVersionId() {
		return sinceVersionId;
	}

	public void setSinceVersionId(Integer sinceVersionId) {
		this.sinceVersionId = sinceVersionId;
	}

	public Integer getDeprecatedVersionId() {
		return deprecatedVersionId;
	}

	public void setDeprecatedVersionId(Integer deprecatedVersionId) {
		this.deprecatedVersionId = deprecatedVersionId;
	}

}