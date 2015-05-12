package org.openforis.collect.presenter {
	import org.openforis.collect.event.InputFieldEvent;
	import org.openforis.collect.ui.component.detail.EntityDataGroupItemRenderer;
	
	/**
	 * @author S. Ricci
	 */
	public class EntityDataGroupItemPresenter extends MultipleNodesDataGroupItemPresenter {
		
		public function EntityDataGroupItemPresenter(view:EntityDataGroupItemRenderer) {
			super(view);
		}
		
		private function get view():EntityDataGroupItemRenderer {
			return EntityDataGroupItemRenderer(_view);
		}
		
		override protected function fieldFocusInHandler(event:InputFieldEvent):void {
			if ( view.entity != null && 
				view.entity.id == event.parentEntityId ) {
				//show backgroud
				view.selectedBackgroundObject.visible = true;
			}
		}
		
		override protected function fieldFocusOutHandler(event:InputFieldEvent):void {
			//show backgroud
			view.selectedBackgroundObject.visible = false;
		}
		
	}
}