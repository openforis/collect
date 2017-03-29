package org.openforis.collect.designer.util;

import org.apache.commons.lang3.StringUtils;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Messagebox.ClickEvent;

/**
 * 
 * @author S. Ricci
 *
 */
public class MessageUtil {

	protected static final String DEFAULT_CONFIRM_TITLE_KEY = "global.confirm.title";
	protected static final String DEFAULT_INFO_TITLE_KEY = "global.message.title.info";
	protected static final String DEFAULT_WARNING_TITLE_KEY = "global.message.title.warning";
	protected static final String DEFAULT_ERROR_TITLE_KEY = "global.message.title.error";

	public enum MessageType {
		INFO, WARNING, ERROR;
	}
	
	public static void showInfo(String messageKey, Object... args) {
		showMessage(MessageType.INFO, messageKey, args);
	}
	
	public static void showError(String messageKey, Object... args) {
		showMessage(MessageType.ERROR, messageKey, args);
	}
	
	public static void showWarning(String messageKey, Object... args) {
		showMessage(MessageType.WARNING, messageKey, args);
	}

	public static void showMessage(MessageType type, String messageKey, Object... args) {
		String titleKey;
		switch ( type ) {
		case ERROR:
			titleKey = DEFAULT_ERROR_TITLE_KEY;
			break;
		case WARNING:
			titleKey = DEFAULT_WARNING_TITLE_KEY;
			break;
		default:
			titleKey = DEFAULT_INFO_TITLE_KEY;
			break;
		}
		showMessage(type, messageKey, args, titleKey, null);
	}

	public static void showMessage(MessageType type, String messageKey, Object[] args, String titleKey, Object[] titleArgs) {
		String message = getLabel(messageKey, args);
		if ( message == null ) {
			message = messageKey;
		}
		String title = getLabel(titleKey, titleArgs);
		if ( title == null ) {
			title = titleKey;
		}
		String icon;
		switch ( type ) {
		case ERROR:
			icon = Messagebox.ERROR;
			break;
		case WARNING:
			icon = Messagebox.EXCLAMATION;
			break;
		default:
			icon = Messagebox.INFORMATION;
			break;
		}
		Messagebox.show(message, title, Messagebox.OK, icon);
	}

	public static void showConfirm(ConfirmHandler handler, String messageKey) {
		showConfirm(handler, messageKey, null);
	}

	public static void showConfirm(ConfirmHandler handler, String messageKey, Object[] args) {
		showConfirm(handler, messageKey, args, DEFAULT_CONFIRM_TITLE_KEY, null, (String) null, (String) null);
	}

	public static void showConfirm(ConfirmHandler handler, String messageKey, Object[] args, String titleKey, Object[] titleArgs, String okLabelKey, String cancelLabelKey) {
		ConfirmParams params = new ConfirmParams(handler, messageKey);
		params.setMessageArgs(args);
		params.setTitleKey(titleKey);
		params.setTitleArgs(titleArgs);
		params.setOkLabelKey(okLabelKey);
		params.setCancelLabelKey(cancelLabelKey);
		showConfirm(params);
	}
	
	public static void showConfirm(ConfirmParams params) {
		String message = getLabel(params.getMessageKey(), params.getMessageArgs());
		String title = getLabel(params.getTitleKey() == null ? "global.confirm.title": params.getTitleKey(), params.getTitleArgs());
		
		EventListener<ClickEvent> eventListener = new ConfirmEventListener(params.getConfirmHandler());
		//Messagebox.show(message, title, Messagebox.OK | Messagebox.CANCEL, Messagebox.QUESTION, eventListener );
		
		String okLabel = getLabel(params.getOkLabelKey());
		String cancelLabel = getLabel(params.getCancelLabelKey());
		
		String[] btnLabels = null;
		if ( StringUtils.isNotBlank(okLabel) ) {
			if (StringUtils.isNotBlank(cancelLabel) ) {
				btnLabels = new String[] {okLabel, cancelLabel};
			} else {
				btnLabels = new String[] {okLabel};
			}
		}
		Messagebox.show(message, 
				title,
			    new Messagebox.Button[] {Messagebox.Button.OK, Messagebox.Button.CANCEL},
			    btnLabels,
			    Messagebox.QUESTION, 
			    Messagebox.Button.CANCEL, 
			    eventListener);
	}
	
	protected static String getLabel(String key, Object... args) {
		String label = Labels.getLabel(key, args);
		if ( label != null ) {
			label = label.replaceAll("\\\\n", "\n");
		}
		return label;
	}
	
	static class ConfirmEventListener implements EventListener<ClickEvent> {
		
		private ConfirmHandler handler;
		
		public ConfirmEventListener(ConfirmHandler handler) {
			super();
			this.handler = handler;
		}

		public void onEvent(ClickEvent evt) throws InterruptedException {
	        String evtName = evt.getName();
			if ( "onOK".equals(evtName) ) {
	            handler.onOk();
	        } else if (handler instanceof CompleteConfirmHandler && "onCancel".equals(evtName)) {
	        	((CompleteConfirmHandler) handler).onCancel();
	        }
	    }

	}

	public interface ConfirmHandler {
		void onOk();
	}
	
	public interface CompleteConfirmHandler extends ConfirmHandler {
		void onCancel();
	}
	
	public static class ConfirmParams {
		private ConfirmHandler confirmHandler;
		private String messageKey;
		private Object[] messageArgs;
		private String titleKey;
		private Object[] titleArgs;
		private String okLabelKey;
		private String cancelLabelKey;
		
		public ConfirmParams(ConfirmHandler confirmHandler) {
			this(confirmHandler, null);
		}
		
		public ConfirmParams(ConfirmHandler confirmHandler, String messageKey) {
			this.confirmHandler = confirmHandler;
			this.messageKey = messageKey;
		}

		public void setMessage(String key, Object... args) {
			this.messageKey = key;
			this.messageArgs = args;
		}
		
		public void setTitle(String key, Object... args) {
			this.titleKey = key;
			this.titleArgs = args;
		}
		
		public ConfirmHandler getConfirmHandler() {
			return confirmHandler;
		}

		public String getMessageKey() {
			return messageKey;
		}

		public void setMessageKey(String messageKey) {
			this.messageKey = messageKey;
		}

		public String getTitleKey() {
			return titleKey;
		}

		public void setTitleKey(String titleKey) {
			this.titleKey = titleKey;
		}

		public Object[] getTitleArgs() {
			return titleArgs;
		}

		public void setTitleArgs(Object[] titleArgs) {
			this.titleArgs = titleArgs;
		}

		public String getOkLabelKey() {
			return okLabelKey;
		}

		public void setOkLabelKey(String okLabelKey) {
			this.okLabelKey = okLabelKey;
		}

		public String getCancelLabelKey() {
			return cancelLabelKey;
		}

		public void setCancelLabelKey(String cancelLabelKey) {
			this.cancelLabelKey = cancelLabelKey;
		}

		public Object[] getMessageArgs() {
			return messageArgs;
		}
		
		public void setMessageArgs(Object[] messageArgs) {
			this.messageArgs = messageArgs;
		}
		
	}
	
}
