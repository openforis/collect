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

	public class MultipleAttributeDataGroupPresenter extends AbstractPresenter {
		
		private var _view:MultipleAttributeDataGroupFormItem;
		
		public function MultipleAttributeDataGroupPresenter(view:MultipleAttributeDataGroupFormItem) {
			this._view = view;
			
			super();
		}
		
		override internal function initEventListeners():void {
			_view.openPopupImage.addEventListener(MouseEvent.CLICK, openPopupImageClickHandler);
			_view.popup.addEventListener(CloseEvent.CLOSE, closePopupHandler);
		}
		
		/**
		 * Open popup for editing the attribute values
		 * */
		internal function openPopupImageClickHandler(event:MouseEvent):void {
			PopUpManager.addPopUp(_view.popup, FlexGlobals.topLevelApplication as DisplayObject, true);
			PopUpManager.centerPopUp(_view.popup);
		}
		
		/**
		 * Close the popup
		 * */
		internal function closePopupHandler(event:CloseEvent):void {
			PopUpManager.removePopUp(_view.popup);
		}
	}
}