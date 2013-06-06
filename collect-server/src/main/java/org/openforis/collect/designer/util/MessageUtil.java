package org.openforis.collect.designer.util;

import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zul.Messagebox;

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
	
	public static void showInfo(String messageKey) {
		showMessage(MessageType.INFO, messageKey);
	}

	public static void showInfo(String messageKey, String arg) {
		showInfo(messageKey, new String[] {arg});
	}
	
	public static void showInfo(String messageKey, Object[] args) {
		showMessage(MessageType.INFO, messageKey, args);
	}
	
	public static void showError(String messageKey) {
		showMessage(MessageType.ERROR, messageKey);
	}

	public static void showError(String messageKey, String arg) {
		showError(messageKey, new String[] {arg});
	}
	
	public static void showError(String messageKey, Object[] args) {
		showMessage(MessageType.ERROR, messageKey, args);
	}
	
	public static void showWarning(String messageKey, String arg) {
		showWarning(messageKey, new String[] {arg});
	}
	
	public static void showWarning(String messageKey, Object[] args) {
		showMessage(MessageType.WARNING, messageKey, args);
	}

	public static void showWarning(String messageKey) {
		showMessage(MessageType.WARNING, messageKey);
	}
	
	public static void showMessage(MessageType type, String messageKey) {
		showMessage(type, messageKey, null);
	}
	
	public static void showMessage(MessageType type, String messageKey, Object[] args) {
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
		showConfirm(handler, messageKey, args, DEFAULT_CONFIRM_TITLE_KEY, null);
	}

	public static void showConfirm(ConfirmHandler handler, String messageKey, Object[] args, String titleKey, Object[] titleArgs) {
		String message = getLabel(messageKey, args);
		String title = getLabel(titleKey, titleArgs);
		EventListener<Event> eventListener = new ConfirmEventListener(handler);
		Messagebox.show(message, title, Messagebox.OK | Messagebox.CANCEL, Messagebox.QUESTION, eventListener );
	}

	protected static String getLabel(String key, Object[] args) {
		String label = Labels.getLabel(key, args);
		if ( label != null ) {
			label = label.replaceAll("\\\\n", "\n");
		}
		return label;
	}
	
	static class ConfirmEventListener implements EventListener<Event> {
		
		private ConfirmHandler handler;
		
		public ConfirmEventListener(ConfirmHandler handler) {
			super();
			this.handler = handler;
		}

		public void onEvent(Event evt) throws InterruptedException {
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
}
