/**
 * 
 */
package org.openforis.collect.designer.composer;

import java.util.Locale;

import org.openforis.idm.metamodel.AttributeDefinition;
import org.openforis.idm.metamodel.AttributeType;
import org.openforis.idm.metamodel.EntityDefinition;
import org.zkoss.bind.BindComposer;
import org.zkoss.bind.annotation.ExecutionArgParam;
import org.zkoss.bind.annotation.Init;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.Include;
import org.zkoss.zul.Window;

/**
 * @author S. Ricci
 *
 */
public class AttributePopUpComposer extends BindComposer<Window> {

	private static final long serialVersionUID = 1L;

	@Wire
	private Include attributeDetailsInclude;
	
	private EntityDefinition parentEntity;
	private Boolean newItem;
	private AttributeDefinition item;
	
	@Override
	public void doAfterCompose(Window comp) throws Exception {
		super.doAfterCompose(comp);
		Selectors.wireComponents(comp, this, false);
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
			return type.name().toLowerCase(Locale.ENGLISH);
		}
	}

}
