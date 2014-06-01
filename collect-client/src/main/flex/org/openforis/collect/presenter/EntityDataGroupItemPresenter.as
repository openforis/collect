package org.openforis.collect.presenter {
	import flash.events.Event;
	
	import mx.states.OverrideBase;
	
	import org.openforis.collect.Application;
	import org.openforis.collect.event.InputFieldEvent;
	import org.openforis.collect.model.proxy.AttributeProxy;
	import org.openforis.collect.model.proxy.EntityProxy;
	import org.openforis.collect.ui.component.detail.EntityDataGroupItemRenderer;
	import org.openforis.collect.ui.component.input.InputField;
	import org.openforis.collect.util.UIUtil;
	
	/**
	 * @author S. Ricci
	 */
	public class EntityDataGroupItemPresenter extends MultipleNodesDataGroupItemPresenter {
		
		public function EntityDataGroupItemPresenter(view:EntityDataGroupItemRenderer) {
			super(view);
		}
		
		override protected function fieldFocusInHandler(event:InputFieldEvent):void {
			if ( EntityDataGroupItemRenderer(_view).entity != null && 
				EntityDataGroupItemRenderer(_view).entity.id == event.parentEntityId ) {
				//show backgroud
				_view.selectedBackgroundObject.visible = true;
			}
		}
		
		override protected function fieldFocusOutHandler(event:InputFieldEvent):void {
			//show backgroud
			_view.selectedBackgroundObject.visible = false;
		}
		
	}
}