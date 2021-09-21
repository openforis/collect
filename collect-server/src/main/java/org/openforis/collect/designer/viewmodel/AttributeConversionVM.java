package org.openforis.collect.designer.viewmodel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openforis.collect.designer.metamodel.AttributeTypeUtils;
import org.openforis.collect.designer.metamodel.NodeType;
import org.openforis.collect.designer.model.CheckType;
import org.openforis.collect.designer.util.ComponentUtil;
import org.openforis.collect.designer.util.MessageUtil;
import org.openforis.collect.designer.util.Resources;
import org.openforis.idm.metamodel.AttributeDefinition;
import org.openforis.idm.metamodel.AttributeType;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.validation.Check;
import org.zkoss.bind.BindContext;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.ExecutionArgParam;
import org.zkoss.bind.annotation.Init;
import org.zkoss.util.resource.Labels;
import org.zkoss.zul.Window;

/**
 * 
 * @author S. Ricci
 *
 */
public class AttributeConversionVM extends SurveyBaseVM {

	private Map<String,String> form;
	private AttributeDefinition attributeDefinition;
	private AttributeType originalAttributeType;
	
	public AttributeConversionVM() {
		form = new HashMap<String, String>();
	}
	
	public static Window openPopup(final AttributeDefinition attrDefn) {
		@SuppressWarnings("serial")
		Map<String, Object> args = new HashMap<String, Object>(){{
			put("attribute", attrDefn);
		}};
		Window popUp = openPopUp(Resources.Component.ATTRIBUTE_CONVERSION_PARAMETERS_POPUP.getLocation(), true, args);
		popUp.setTitle(Labels.getLabel("survey.schema.convert_node_popup_title", new String[]{NodeType.ATTRIBUTE.getLabel(), attrDefn.getName()}));
		return popUp;
	}
	
	@Init(superclass=false)
	public void init(@ExecutionArgParam("attribute") AttributeDefinition attrDefn) {
		super.init();
		this.attributeDefinition = attrDefn;
		this.originalAttributeType = AttributeType.valueOf(attrDefn);
	}

	@Command
	public void convert(@ContextParam(ContextType.BIND_CONTEXT) BindContext ctx) {
		if ( validateForm(ctx) ) {
			String typeLabel = form.get("type");
			AttributeType type = AttributeTypeUtils.fromLabel(typeLabel);
			AttributeDefinition convertedAttribute = new AttributeConverter().convert(attributeDefinition, type);
			dispatchNodeConvertedCommand(convertedAttribute);
			Window popUp = ComponentUtil.getClosest(ctx.getComponent(), Window.class);
			closePopUp(popUp);
		}
	}
	
	protected boolean validateForm(BindContext ctx) {
		String messageKey = null;
		String typeLabel = form.get("type");
		if (typeLabel == null) {
			messageKey = "survey.schema.node.conversion.select_type";
		}
		if ( messageKey == null ) {
			return true;
		} else {
			MessageUtil.showWarning(messageKey);
			return false;
		}
	}
	
	public List<String> getAttributeTypes() {
		AttributeType currentType = AttributeType.valueOf(attributeDefinition);
		AttributeType[] types = AttributeType.values();
		List<String> typeLabels = new ArrayList<String>(types.length);
		for (AttributeType type : types) {
			if (type != currentType) {
				typeLabels.add(AttributeTypeUtils.getLabel(type));
			}
		}
		return typeLabels;
	}
	
	public Map<String, String> getForm() {
		return form;
	}
	
	public void setForm(Map<String, String> form) {
		this.form = form;
	}
	
	public String getOriginalAttributeTypeLabel() {
		return AttributeTypeUtils.getLabel(originalAttributeType);
	}
	
	public AttributeDefinition getAttributeDefinition() {
		return attributeDefinition;
	}

	private static class AttributeConverter {
		
		public AttributeDefinition convert(AttributeDefinition attrDef, AttributeType toType) {
			int id = attrDef.getId();
			EntityDefinition parentEntityDef = attrDef.getParentEntityDefinition();
			int index = parentEntityDef.getChildDefinitionIndex(attrDef);
			
			AttributeDefinition newAttrDef = (AttributeDefinition) NodeType.createNodeDefinition(attrDef.getSurvey(), NodeType.ATTRIBUTE, toType, id);

			parentEntityDef.removeChildDefinition(attrDef);
			
			newAttrDef.setAnnotations(attrDef.getAnnotations());
			newAttrDef.setAttributeDefaults(attrDef.getAttributeDefaults());
			newAttrDef.setCalculated(attrDef.isCalculated());
			newAttrDef.setKey(attrDef.isKey());
			newAttrDef.setDeprecatedVersion(attrDef.getDeprecatedVersion());
			newAttrDef.setDescriptions(attrDef.getDescriptions());
			newAttrDef.setLabels(attrDef.getLabels());
			newAttrDef.setMaxCountExpression(attrDef.getMinCountExpression());
			newAttrDef.setMinCountExpression(attrDef.getMinCountExpression());
			newAttrDef.setName(attrDef.getName());
			newAttrDef.setMultiple(attrDef.isMultiple());
			newAttrDef.setPrompts(attrDef.getPrompts());
			newAttrDef.setRelevantExpression(attrDef.getRelevantExpression());
			newAttrDef.setSinceVersion(attrDef.getSinceVersion());
			
			for (Check<?> check : attrDef.getChecks()) {
				if (isApplicable(check, toType)) {
					newAttrDef.addCheck(check);
				}
			}
			parentEntityDef.addChildDefinition(newAttrDef);
			parentEntityDef.moveChildDefinition(newAttrDef, index);
			return newAttrDef;
		}

		private boolean isApplicable(Check<?> check, AttributeType type) {
			CheckType checkType = CheckType.valueOf(check);
			List<CheckType> compatibleTypes = CheckType.compatibleValues(type);
			return compatibleTypes.contains(checkType);
		}
		
	}
	
}
