/**
 * 
 */
package org.openforis.collect.designer.viewmodel;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openforis.collect.designer.form.CodeListItemFormObject;
import org.openforis.collect.designer.form.FormObject;
import org.openforis.collect.designer.util.MessageUtil;
import org.openforis.collect.manager.CodeListManager;
import org.openforis.collect.model.FileWrapper;
import org.openforis.idm.metamodel.CodeListItem;
import org.openforis.idm.metamodel.PersistedCodeListItem;
import org.zkoss.bind.BindContext;
import org.zkoss.bind.BindUtils;
import org.zkoss.bind.Binder;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.ExecutionArgParam;
import org.zkoss.bind.annotation.Init;
import org.zkoss.image.AImage;
import org.zkoss.image.Image;
import org.zkoss.util.media.Media;
import org.zkoss.zk.ui.event.UploadEvent;
import org.zkoss.zk.ui.select.annotation.WireVariable;

/**
 * 
 * @author S. Ricci
 *
 */
public class CodeListItemVM extends SurveyObjectBaseVM<CodeListItem> {

	public static final String ITEM_ARG = "item";
	public static final String PARENT_ITEM_ARG = "parentItem";
	public static final String ENUMERATING_CODE_LIST_ARG = "enumeratingCodeList";
	private static final int MAX_IMAGE_SIZE_KBYTES = 300;
	private static final int MAX_IMAGE_SIZE_BYTES = 1024 * MAX_IMAGE_SIZE_KBYTES;

	@WireVariable
	private CodeListManager codeListManager;

	private AImage image;
	private boolean imageModified;
	private FileWrapper newImageFileWrapper;
	
	@Init(superclass=false)
	public void init(@ExecutionArgParam(ITEM_ARG) CodeListItem item) {
		super.init();
		setEditedItem(item);
		commitChangesOnApply = false;
		imageModified = false;
	}
	
	@Override
	protected void addNewItemToSurvey() {
		//do nothing, performed by CodeListVM
	}
	
	@Override
	protected void deleteItemFromSurvey(CodeListItem item) {
		//do nothing, performed by CodeListVM
	}
	
	@Override
	protected List<CodeListItem> getItemsInternal() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	protected void moveSelectedItemInSurvey(int indexTo) {
		//managed by CodeListsVM
	}
	
	@Override
	protected CodeListItem createItemInstance() {
		//items instantiated in CodeListEditVM
		return null;
	}
	
	@Override
	public void setEditedItem(CodeListItem editedItem) {
		super.setEditedItem(editedItem);
		if (editedItem != null && editedItem instanceof PersistedCodeListItem && 
				((PersistedCodeListItem) editedItem).getSystemId() != null) {
			FileWrapper fileWrapper = codeListManager.loadImageContent((PersistedCodeListItem) editedItem);
			if (fileWrapper != null) {
				try {
					image = new AImage(fileWrapper.getFileName(), fileWrapper.getContent());
				} catch (IOException e) {
				}
			}
		}
	}
	
	@Override
	protected FormObject<CodeListItem> createFormObject() {
		return new CodeListItemFormObject();
	}
	
	@Command
	public void apply(@ContextParam(ContextType.BINDER) Binder binder) {
		if ( isCurrentFormValid() ) {
			commitChanges(binder);
			postClosePopUpCommand(false, imageModified, newImageFileWrapper);
		} else {
			checkCanLeaveForm(new CanLeaveFormConfirmHandler() {
				@Override
				public void onOk(boolean confirmed) {
					postClosePopUpCommand(confirmed);
				}
			});
		}
	}
	
	@Command
	public void cancel(@ContextParam(ContextType.BINDER) Binder binder) {
		undoLastChanges(binder.getView());
		postClosePopUpCommand(true);
	}
	
	@Command("imageUpload")
	public void onImageUpload(@ContextParam(ContextType.BIND_CONTEXT) BindContext ctx) {
		UploadEvent upEvent = null;
		Object objUploadEvent = ctx.getTriggerEvent();
		if (objUploadEvent != null && (objUploadEvent instanceof UploadEvent)) {
			upEvent = (UploadEvent) objUploadEvent;
		}
		if (checkValidImage(upEvent)) {
			Media media = upEvent.getMedia();
			image = (AImage) media;	// Initialize the bind object to
			// show image in zul page and
			// Notify it also
			newImageFileWrapper = new FileWrapper(media.getByteData(), media.getName());
			imageModified = true;
			notifyChange("image", "imageModified");
		}
	}
	
	private boolean checkValidImage(UploadEvent upEvent) {
		if (upEvent != null) {
			Media media = upEvent.getMedia();
			if (media instanceof Image) {
				int lengthofImage = media.getByteData().length;
				if (lengthofImage > MAX_IMAGE_SIZE_BYTES) {
					MessageUtil.showInfo("survey.code_list.image.error.max_size_exceeded", MAX_IMAGE_SIZE_KBYTES);
					return false;
				} else {
					return true;
				}
			} else {
				MessageUtil.showInfo("survey.code_list.image.error.not_an_image");
				return false;
			}
		} else {
			//Upload Event Is not Coming
			return false;
		}
	}

	@Command("removeImage")
	public void removeImage() {
		imageModified = true;
		image = null;
		newImageFileWrapper = null;
		notifyChange("image", "imageModified");
	}
	
	private void postClosePopUpCommand(boolean undoChanges) {
		postClosePopUpCommand(undoChanges, false, null);
	}
	
	private void postClosePopUpCommand(boolean undoChanges, boolean imageModified, FileWrapper imageFileWrapper) {
		Map<String, Object> args = new HashMap<String, Object>();
		args.put("undoChanges", undoChanges);
		args.put("imageModified", imageModified);
		args.put("imageFileWrapper", imageFileWrapper);
		BindUtils.postGlobalCommand(null, null, CodeListsVM.CLOSE_CODE_LIST_ITEM_POP_UP_COMMAND, args);
	}	

	public AImage getImage() {
		return image;
	}
	
	public boolean isImageModified() {
		return imageModified;
	}
	
	public CodeListManager getCodeListManager() {
		return codeListManager;
	}
	
}
