/**
 * 
 */
package org.openforis.collect.designer.viewmodel;

import org.openforis.collect.designer.model.AttributeType;
import org.openforis.idm.metamodel.AttributeDefinition;
import org.openforis.idm.metamodel.EntityDefinition;
import org.zkoss.bind.annotation.AfterCompose;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.ExecutionArgParam;
import org.zkoss.bind.annotation.Init;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.Include;

/**
 * @author S. Ricci
 *
 */
public class AttributePopUpVM extends BaseVM {

	@Wire
	private Include attributeDetailsInclude;
	
	private EntityDefinition parentEntity;
	private Boolean newItem;
	private AttributeDefinition item;
	
	@AfterCompose
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view){
		 Selectors.wireComponents(view, this, false);
		 Selectors.wireEventListeners(view, this);
		 refreshNodeForm();
	}

	@Init
	public void init(@ExecutionArgParam("parentEntity") EntityDefinition parentEntity,
			@ExecutionArgParam("item") AttributeDefinition item, 
			@ExecutionArgParam("newItem") Boolean newItem) {
		if ( item != null ) {
			this.parentEntity = parentEntity;
			this.newItem = newItem;
			this.item = item;
		}
	}
	
	protected void refreshNodeForm() {
		String type = getAttributeType();
		String detailUrl = "survey_edit/schema/attribute_" + type + ".zul";
		attributeDetailsInclude.setDynamicProperty("parentEntity", parentEntity);
		attributeDetailsInclude.setDynamicProperty("newItem", newItem);
		attributeDetailsInclude.setDynamicProperty("item", item);
		attributeDetailsInclude.setSrc(detailUrl);
	}

	public String getAttributeType() {
		if ( item == null ) {
			return null;
		} else {
			AttributeType type = AttributeType.valueOf(item);
			return type.name().toLowerCase();
		}
	}

}
