package org.openforis.collect.designer.viewmodel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openforis.collect.designer.util.MessageUtil.CompleteConfirmHandler;
import org.openforis.collect.designer.util.MessageUtil.ConfirmHandler;
import org.openforis.collect.designer.util.PopUpUtil;
import org.openforis.collect.designer.util.Resources;
import org.openforis.idm.metamodel.CodeList;
import org.openforis.idm.metamodel.CodeListItem;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.SurveyObject;
import org.zkoss.bind.annotation.ExecutionArgParam;
import org.zkoss.bind.annotation.Init;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.IdSpace;
import org.zkoss.zk.ui.Path;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Window;

/**
 * 
 * @author S. Ricci
 *
 */
public class SurveyErrorsPopUpVM {

	private static final String ON_CLICK_EVENT = "onClick";
	private static final String OK_BUTTON_ID = "okBtn";
	private static final String CANCEL_BUTTON_ID = "cancelBtn";
	
	private String title;
	private String message;
	private List<SurveyObjectError> schemaErrors;

	public static Window openPopUp(String title, String message, List<? extends SurveyObject> nodesWithErrors, 
			final ConfirmHandler confirmHandler) {
		return openPopUp(title, message, nodesWithErrors, confirmHandler, false);
	}
	
	public static Window openPopUp(String title, String message, List<? extends SurveyObject> nodesWithErrors, 
			final ConfirmHandler confirmHandler, boolean hideCancelButton) {
		List<SurveyObjectError> errors = createErrors(nodesWithErrors);
		Map<String, Object> args = new HashMap<String, Object>();
		args.put("title", title);
		args.put("message", message);
		args.put("errors", errors);
		args.put("hideCancelButton", hideCancelButton);
		Window result = PopUpUtil.openPopUp(Resources.Component.CONFIRM_SURVEY_ERRORS_POPUP.getLocation(), true, args);
		initEventListeners(result, confirmHandler);
		return result;
	}

	protected static void initEventListeners(Window popUp, final ConfirmHandler confirmHandler) {
		IdSpace idSpace = popUp.getSpaceOwner();
		Component okButton = Path.getComponent(idSpace, OK_BUTTON_ID);
		okButton.addEventListener(ON_CLICK_EVENT, new EventListener<Event>() {
			public void onEvent(Event arg0) throws Exception {
				confirmHandler.onOk();
			};
		});
		Component cancelButton = Path.getComponent(idSpace, CANCEL_BUTTON_ID);
		cancelButton.addEventListener(ON_CLICK_EVENT, new EventListener<Event>() {
			public void onEvent(Event arg0) throws Exception {
				if ( confirmHandler instanceof CompleteConfirmHandler ) {
					((CompleteConfirmHandler) confirmHandler).onCancel();
				}
			};
		});
	}

	protected static List<SurveyObjectError> createErrors(List<? extends SurveyObject> items) {
		List<SurveyObjectError> errors = new ArrayList<SurveyErrorsPopUpVM.SurveyObjectError>();
		for (SurveyObject item : items) {
			String path = null;
			String message = null;
			if ( item instanceof NodeDefinition ) {
				path = ((NodeDefinition) item).getPath();
			} else if ( item instanceof CodeList ) {
				path = ((CodeList) item).getName();
			} else if ( item instanceof CodeListItem ) {
				CodeListItem codeListItem = (CodeListItem) item;
				path = getPath(codeListItem);
			}
			SurveyObjectError error = new SurveyObjectError(path, message);
			errors.add(error);
		}
		return errors;
	}

	protected static String getPath(CodeListItem codeListItem) {
		CodeList codeList = codeListItem.getCodeList();
		StringBuilder sb = new StringBuilder();
		CodeListItem currentItem = codeListItem;
		while ( currentItem != null ) {
			sb.insert(0, currentItem.getCode());
			sb.insert(0, "/");
			currentItem = currentItem.getParentItem();
		}
		sb.insert(0, codeList.getName());
		return sb.toString();
	}

	@Init
	public void init(@ExecutionArgParam("title") String title, 
			@ExecutionArgParam("message") String message, 
			@ExecutionArgParam("errors") List<SurveyObjectError> errors) {
		this.title = title;
		this.message = message;
		this.schemaErrors = errors;
	}
	
	public List<SurveyObjectError> getSchemaErrors() {
		return new ListModelList<SurveyObjectError>(schemaErrors);
	}
	
	public String getTitle() {
		return title;
	}
	
	public String getMessage() {
		return message;
	}
	
	public static class SurveyObjectError {
		
		private String path;
		private String message;

		public SurveyObjectError(String path, String message) {
			super();
			this.path = path;
			this.message = message;
		}

		public String getPath() {
			return path;
		}

		public String getMessage() {
			return message;
		}

	}
	
}
