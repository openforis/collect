/**
 * 
 */
package org.openforis.collect.designer.composer;

import java.util.Locale;

import org.openforis.collect.designer.model.CheckType;
import org.openforis.idm.metamodel.AttributeDefinition;
import org.openforis.idm.metamodel.validation.Check;
import org.zkoss.bind.BindComposer;
import org.zkoss.bind.BindUtils;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ExecutionArgParam;
import org.zkoss.bind.annotation.Init;
import org.zkoss.zul.Window;

/**
 * @author S. Ricci
 *
 */
public class CheckPopUpComposer extends BindComposer<Window> {

	private static final String VALIDATE_CHECK_FORM_COMMAND = "validateCheckForm";
	private static final String APPLY_CHANGES_TO_EDITED_CHECK_COMMAND = "applyChangesToEditedCheck";
	private static final String CANCEL_CHANGES_TO_EDITED_CHECK_COMMAND = "cancelChangesToEditedCheck";

	private static final long serialVersionUID = 1L;

	private Check<?> check;
	
	@Init
	public void init(@ExecutionArgParam("parentDefinition") AttributeDefinition parentDefinition,
			@ExecutionArgParam("check") Check<?> check, 
			@ExecutionArgParam("newItem") Boolean newItem) {
		this.check = check;
	}
	
	@Override
	public void doAfterCompose(Window comp) throws Exception {
		super.doAfterCompose(comp);
		BindUtils.postGlobalCommand(null, null, VALIDATE_CHECK_FORM_COMMAND, null);
	}
	
	@Command
	public void applyChanges() {
		BindUtils.postGlobalCommand(null, null, APPLY_CHANGES_TO_EDITED_CHECK_COMMAND, null);
	}

	@Command
	public void cancelChanges() {
		BindUtils.postGlobalCommand(null, null, CANCEL_CHANGES_TO_EDITED_CHECK_COMMAND, null);
	}
	
	@Command
	public void close() {
		cancelChanges();
	}

	public String getTypeShort() {
		if ( check == null ) {
			return null;
		} else {
			CheckType type = CheckType.valueOf(check);
			return type.name().toLowerCase(Locale.ENGLISH);
		}
	}
	
	public String getTypeLabel() {
		if ( check == null ) {
			return null;
		} else {
			CheckType type = CheckType.valueOf(check);
			return type.getLabel();
		}
	}

}
