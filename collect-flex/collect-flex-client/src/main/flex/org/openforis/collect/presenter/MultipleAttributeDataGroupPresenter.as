package org.openforis.collect.presenter {
	import flash.display.DisplayObject;
	import flash.events.MouseEvent;
	
	import mx.core.FlexGlobals;
	import mx.events.CloseEvent;
	import mx.managers.PopUpManager;
	
	import org.openforis.collect.ui.component.detail.MultipleAttributeDataGroupFormItem;
	
	/**
	 * @author Mino Togna
	 * 
	 * */

	public class MultipleAttributeDataGroupPresenter extends FormItemPresenter {
		
		public function MultipleAttributeDataGroupPresenter(view:MultipleAttributeDataGroupFormItem) {
			super(view);
		}
		
		private function get view():MultipleAttributeDataGroupFormItem {
			return MultipleAttributeDataGroupFormItem(_view);
		}
		
		override internal function initEventListeners():void {
			view.openPopupImage.addEventListener(MouseEvent.CLICK, openPopupImageClickHandler);
			view.popup.addEventListener(CloseEvent.CLOSE, closePopupHandler);
		}
		
		/**
		 * Open popup for editing the attribute values
		 * */
		internal function openPopupImageClickHandler(event:MouseEvent):void {
			PopUpManager.addPopUp(view.popup, FlexGlobals.topLevelApplication as DisplayObject, true);
			PopUpManager.centerPopUp(view.popup);
		}
		
		/**
		 * Close the popup
		 * */
		internal function closePopupHandler(event:CloseEvent):void {
			PopUpManager.removePopUp(view.popup);
		}
	}
}