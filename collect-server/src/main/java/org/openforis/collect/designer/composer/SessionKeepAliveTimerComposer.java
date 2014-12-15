package org.openforis.collect.designer.composer;

import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.SerializableEventListener;
import org.zkoss.zk.ui.util.Composer;
import org.zkoss.zul.Timer;

/**
 * 
 * @author S. Ricci
 *
 */
public class SessionKeepAliveTimerComposer implements Composer<Timer> {
	
	public void doAfterCompose(Timer comp) throws Exception {
		comp.addEventListener("onTimer", new SerializableEventListener<Event>() {
			private static final long serialVersionUID = 1L;
			@Override
			public void onEvent(Event event) throws Exception {
				//does nothing
			}
		});
	}

}
