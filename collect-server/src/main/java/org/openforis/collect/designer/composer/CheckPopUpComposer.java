/**
 * 
 */
package org.openforis.collect.designer.composer;

import org.openforis.collect.designer.model.CheckType;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.validation.Check;
import org.zkoss.bind.BindComposer;
import org.zkoss.bind.annotation.ExecutionArgParam;
import org.zkoss.bind.annotation.Init;
import org.zkoss.zul.Window;

/**
 * @author S. Ricci
 *
 */
public class CheckPopUpComposer extends BindComposer<Window> {

	private static final long serialVersionUID = 1L;

	private Check<?> check;
	
	@Init
	public void init(@ExecutionArgParam("parentDefinition") NodeDefinition parentDefinition,
			@ExecutionArgParam("check") Check<?> check, 
			@ExecutionArgParam("newItem") Boolean newItem) {
		if ( check != null ) {
			this.check = check;
		}
	}
	
	public String getTypeShort() {
		if ( check == null ) {
			return null;
		} else {
			CheckType type = CheckType.valueOf(check);
			return type.name().toLowerCase();
		}
	}

}
