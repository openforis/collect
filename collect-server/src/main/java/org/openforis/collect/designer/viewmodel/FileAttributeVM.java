package org.openforis.collect.designer.viewmodel;

import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.FileAttributeDefinition;
import org.zkoss.bind.Binder;
import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.ExecutionArgParam;
import org.zkoss.bind.annotation.Init;

public class FileAttributeVM extends AttributeVM<FileAttributeDefinition> {
	
	private static final String FILE_TYPE_FIELD = "fileType";

	@Init(superclass=false)
	public void init(@ExecutionArgParam("parentEntity") EntityDefinition parentEntity, 
			@ExecutionArgParam("item") FileAttributeDefinition attributeDefn, 
			@ExecutionArgParam("newItem") Boolean newItem) {
		super.initInternal(parentEntity, attributeDefn, newItem);
	}

	@Command
	public void typeChanged(@ContextParam(ContextType.BINDER) Binder binder, 
			@BindingParam(FILE_TYPE_FIELD) String fileType) {
		setTempFormObjectFieldValue(FILE_TYPE_FIELD, fileType);
		dispatchApplyChangesCommand(binder);
		SchemaVM.dispatchEditedNodeTypeChangedGlobalCommand();
	}
}
