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

	public static void showConfirm(ConfirmHandler handler, String messageKey) {
		showConfirm(handler, messageKey, null);
	}

	public static void showConfirm(ConfirmHandler handler, String messageKey, Object[] args) {
		showConfirm(handler, messageKey, args, DEFAULT_CONFIRM_TITLE_KEY, null);
	}

	public static void showConfirm(ConfirmHandler handler, String messageKey, Object[] args, String titleKey, Object[] titleArgs) {
		String message = Labels.getLabel(messageKey, args);
		String title = Labels.getLabel(titleKey, titleArgs);
		EventListener<Event> eventListener = new ConfirmEventListener(handler);
		Messagebox.show(message, title, Messagebox.OK | Messagebox.CANCEL, Messagebox.QUESTION, eventListener );
	}
	
	public static class ConfirmEventListener implements EventListener<Event> {
		
		private ConfirmHandler handler;
		
		public ConfirmEventListener(ConfirmHandler handler) {
			super();
			this.handler = handler;
		}

		public void onEvent(Event evt) throws InterruptedException {
	        if (evt.getName().equals("onOK")) {
	            handler.onOk();
	        } else if (evt.getName().equals("onCancel")) {
	        	handler.onCancel();
	        }
	    }

	}

	public static interface ConfirmHandler {

		void onOk();

		void onCancel();

	}
}
